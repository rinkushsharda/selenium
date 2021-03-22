package com.ericsson.soiv.teststeps.protocols.eoc.businesslogic;

import com.ericsson.jive.core.execution.Jive;
import com.ericsson.jive.core.execution.TestStepBase;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpMethod;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpResponse;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.CustomerInfo;
import com.ericsson.soiv.utils.TestResultInfo;
import com.ericsson.soiv.utils.TransactionSpecification;
import com.jayway.jsonpath.JsonPath;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.ericsson.soiv.utils.JsonHelper.updatePOOi;

public class UpdateProductOffering extends TestStepBase<TestResultInfo> {
    private static Logger LOG = LoggerFactory.getLogger(UpdateProductOffering.class);
    private SoivTestBase soivTestBase = null;
    private TestResultInfo resultInfo = null;
    private CustomerInfo customerInfo = null;
    private TransactionSpecification tSpec = null;

    public UpdateProductOffering(String title, SoivTestBase soivTestBase, CustomerInfo customerInfo, TransactionSpecification tSpec, TestResultInfo resultInfo) {
        super(title);
        this.resultInfo = resultInfo;
        this.customerInfo = customerInfo;
        this.tSpec = tSpec;
        this.soivTestBase = soivTestBase;

        if (!resultInfo.getShouldContinue()) {
            LOG.error(title + " - Not executed due to previous error");
        }
    }

    @Override
    public TestResultInfo execute() {
        if (!resultInfo.getShouldContinue()) {
            SoivTestBase.failAndContinue("Step not executed due to a previous error");
            return resultInfo;
        }
        LOG.info("Step title: " + getTitle());
        JSONObject body = null;

        body = updatePOOi(tSpec.getResponseBody(), "resourceNumber",  customerInfo.getMsisdn());
        body = updatePOOi(body.toString(), "SDP_ID",  Integer.toString(tSpec.getSdpId()));
        body = updatePOOi(body.toString(), "serialNumber",  customerInfo.getSerialNumber());
        body = updatePOOi(body.toString(), "imsi",  customerInfo.getLinkedPortNumber());


        Jive.log("Updated JSON : " + body);

        resultInfo = Steps.PROTOCOLS.eoc.sendEocRequest(this.getClass().getName(), resultInfo, HttpMethod.PUT, 200,
                "/eoc/on/v1/sc/"+customerInfo.getShoppingCartId()+"/pooi/" + customerInfo.getPooiId(), String.valueOf(body)).run();

        HttpResponse response = (HttpResponse) resultInfo.getResult();
        String respBody = (String)response.getBody().getValue();
        String basicPoID = JsonPath.read(respBody,"$.id");
        tSpec.setBasicPoReferenceId(basicPoID);
        tSpec.setResponseBody(body.toString());
        return resultInfo;

    }

    public SoivTestBase getSoivTestBase() {
        return soivTestBase;
    }

    public UpdateProductOffering setSoivTestBase(SoivTestBase soivTestBase) {
        this.soivTestBase = soivTestBase;
        return this;
    }

}
