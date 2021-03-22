package com.ericsson.soiv.teststeps.protocols.bscs.businesslogic;

import com.ericsson.jive.core.execution.Jive;
import com.ericsson.jive.core.execution.TestStepBase;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpMethod;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpResponse;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.*;
import com.jayway.jsonpath.JsonPath;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadCustomer extends TestStepBase<TestResultInfo> {
    private static Logger LOG = LoggerFactory.getLogger(ReadCustomer.class);
    private SoivTestBase soivTestBase = null;
    private TestResultInfo resultInfo = null;
    private CustomerInfo customerInfo = null;
    private TransactionSpecification tSpec = null;
    private ConvertXMLToJson xmlToJson = new ConvertXMLToJson();

    public ReadCustomer(String title, SoivTestBase soivTestBase, CustomerInfo customerInfo, TransactionSpecification tSpec, TestResultInfo resultInfo) {
        super(title);
        this.resultInfo = resultInfo;
        this.customerInfo = customerInfo;
        this.soivTestBase = soivTestBase;
        this.tSpec = tSpec;

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
                "ReadCustomer.xml");

        body = RawMessages.replaceVal(body, "customerID", customerInfo.getCustomerId());

        resultInfo = Steps.PROTOCOLS.bscs.sendBscsRequest(this.getClass().getName(), resultInfo, HttpMethod.POST, 200,
                "/wsi/services/",body).run();

        HttpResponse response = (HttpResponse) resultInfo.getResult();
        String respBody = (String) response.getBody().getValue();
        tSpec.setResponseBody(xmlToJson.convertXMLToJSON(respBody));

        String getCustomerStatus = JsonPath.read(tSpec.getResponseBody(),
                "$.SOAP-ENV:Envelope.SOAP-ENV:Body.customerReadResponse.csStatus.content");

        Jive.log("Get Customer : "+customerInfo.getCustomerId() +" Status : "+getCustomerStatus);
        Assert.assertEquals(getCustomerStatus,"a");

        String getCustomerBillCycle = JsonPath.read(tSpec.getResponseBody(),
                "$.SOAP-ENV:Envelope.SOAP-ENV:Body.customerReadResponse.csBillcycle.content");

        Jive.log("Get Customer : "+customerInfo.getCustomerId() +" Bill Cycle : "+getCustomerBillCycle);
        customerInfo.setCustomerBillCycle(getCustomerBillCycle);

        return resultInfo;
    }

    public SoivTestBase getSoivTestBase() {
        return soivTestBase;
    }

    public ReadCustomer setSoivTestBase(SoivTestBase soivTestBase) {
        this.soivTestBase = soivTestBase;
        return this;
    }

}
