package com.ericsson.soiv.teststeps.protocols.bscs.businesslogic;

import com.ericsson.jive.core.execution.TestStepBase;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpMethod;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpResponse;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetAccountBalance extends TestStepBase<TestResultInfo> {
    private static Logger LOG = LoggerFactory.getLogger(GetAccountBalance.class);
    private SoivTestBase soivTestBase = null;
    private TestResultInfo resultInfo = null;
    private CustomerInfo customerInfo = null;
    private ConvertXMLToJson xmlToJson = new ConvertXMLToJson();
    private TransactionSpecification tSpec=null;

    public GetAccountBalance(String title, SoivTestBase soivTestBase, CustomerInfo customerInfo, TransactionSpecification tSpec, TestResultInfo resultInfo) {
        super(title);
        this.resultInfo = resultInfo;
        this.customerInfo = customerInfo;
        this.soivTestBase = soivTestBase;
        this.tSpec=tSpec;
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

        String body = RawMessages.getBodyFromJsonFilePlacedInTestStep(this.getClass().getName(), "protocols/bscs",
                "GetAccountBalance.xml");

        body = RawMessages.replaceVal(body, "contractID", customerInfo.getContractId());

        resultInfo = Steps.PROTOCOLS.bscs.sendBscsRequest(this.getClass().getName(), resultInfo, HttpMethod.POST, 200,
                "/wsi/services/",body).run();
        HttpResponse response = (HttpResponse) resultInfo.getResult();
        String respBody = (String) response.getBody().getValue();
        tSpec.setResponseBody(xmlToJson.convertXMLToJSON(respBody));

    return resultInfo;
    }

    public SoivTestBase getSoivTestBase() {
        return soivTestBase;
    }

    public GetAccountBalance setSoivTestBase(SoivTestBase soivTestBase) {
        this.soivTestBase = soivTestBase;
        return this;
    }

}
