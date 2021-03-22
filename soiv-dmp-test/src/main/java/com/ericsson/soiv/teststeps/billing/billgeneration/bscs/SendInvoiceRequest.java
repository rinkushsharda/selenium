package com.ericsson.soiv.teststeps.billing.billgeneration.bscs;

import com.ericsson.jive.core.execution.Jive;
import com.ericsson.jive.core.execution.TestStepBase;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpMethod;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpResponse;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.*;
import com.jayway.jsonpath.JsonPath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendInvoiceRequest extends TestStepBase<TestResultInfo> {
    private static Logger LOG = LoggerFactory.getLogger(SendInvoiceRequest.class);
    private SoivTestBase soivTestBase = null;
    private TestResultInfo resultInfo = null;
    private CustomerInfo customerInfo = null;
    private TransactionSpecification tSpec = null;
    private ConvertXMLToJson xmlToJson = new ConvertXMLToJson();
    private CalculateDateTime calculateDateTime = new CalculateDateTime();

    public SendInvoiceRequest(String title, SoivTestBase soivTestBase, TestResultInfo resultInfo,
            CustomerInfo customerInfo, TransactionSpecification tSpec) {
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
        Jive.log("Inside BillGenerate Test Step");

        String body = RawMessages.getBodyFromJsonFilePlacedInTestStep(this.getClass().getName(), "protocols/bscs",
                tSpec.getJsonFileName());

        body = RawMessages.replaceVal(body, "customerID", customerInfo.getCustomerId());
        body = RawMessages.replaceVal(body, "simulation", "true");

        if (tSpec.getBillEndDate() != null) {
            body = RawMessages.replaceVal(body, "billPeriodEndDate", tSpec.getBillEndDate());
        } else {
            body = RawMessages.replaceVal(body, "billPeriodEndDate", calculateDateTime.getNextDayDate());
        }

        Integer csId = 0;
        String respBody = null;
        HttpResponse response;

        try {
            Steps.BILL_GENERATION.stepsBillingRaw.sendBillingRequest("Send Bill Generate Request", resultInfo,
                    HttpMethod.POST, 200, "/wsi/services", body).run();
            Jive.log("Please Wait .. Sending Bill Generate request For Invoice Processing to get Usage from BSCS");
            Thread.sleep(120000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        resultInfo = Steps.BILL_GENERATION.stepsBillingRaw.sendBillingRequest("Send Bill Generate Request", resultInfo,
                HttpMethod.POST, 200, "/wsi/services", body).run();

        response = (HttpResponse) resultInfo.getResult();
        respBody = (String) response.getBody().getValue();
        respBody = xmlToJson.convertXMLToJSON(respBody);

        csId = JsonPath.read(respBody,
                "$.SOAP-ENV:Envelope.SOAP-ENV:Body.billprocessCreateResponse.resultList.item.csId.content");

        Jive.log("Processing Done : CSID : " + csId);
        customerInfo.setCSID(csId.toString());

        return resultInfo;
    }

    public SoivTestBase getSoivTestBase() {
        return soivTestBase;
    }

    public SendInvoiceRequest setSoivTestBase(SoivTestBase soivTestBase) {
        this.soivTestBase = soivTestBase;
        return this;
    }

}
