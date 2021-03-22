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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateOptionalProductOffering extends TestStepBase<TestResultInfo> {
    private static Logger LOG = LoggerFactory.getLogger(UpdateOptionalProductOffering.class);
    private SoivTestBase soivTestBase = null;
    private TestResultInfo resultInfo = null;
    private CustomerInfo customerInfo = null;
    private TransactionSpecification tSpec = null;

    public UpdateOptionalProductOffering(String title, SoivTestBase soivTestBase, CustomerInfo customerInfo, TransactionSpecification tSpec, TestResultInfo resultInfo) {
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

        Jive.log("Updated JSON : " + tSpec.getResponseBody());

        resultInfo = Steps.PROTOCOLS.eoc.sendEocRequest(this.getClass().getName(), resultInfo, HttpMethod.PUT, 200,
                "/eoc/on/v1/sc/"+customerInfo.getShoppingCartId()+"/pooi/" + customerInfo.getPooiId(), tSpec.getResponseBody()).run();

        HttpResponse response = (HttpResponse) resultInfo.getResult();
        String respBody = (String)response.getBody().getValue();
        String basicPoID = JsonPath.read(respBody,"$.id");
        tSpec.setBasicPoReferenceId(basicPoID);
        tSpec.setResponseBody(tSpec.getResponseBody());
        return resultInfo;

    }


    public SoivTestBase getSoivTestBase() {
        return soivTestBase;
    }

    public UpdateOptionalProductOffering setSoivTestBase(SoivTestBase soivTestBase) {
        this.soivTestBase = soivTestBase;
        return this;
    }

}
