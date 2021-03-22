package com.ericsson.soiv.teststeps.provision.bscs;

import com.ericsson.jive.core.execution.TestStepBase;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpMethod;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpResponse;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.*;
import com.jayway.jsonpath.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateCustomer extends TestStepBase<TestResultInfo> {
    private static Logger LOG = LoggerFactory.getLogger(CreateCustomer.class);
    private SoivTestBase soivTestBase = null;
    private CustomerInfo customerInfo = null;
    private TransactionSpecification tSpec = null;
    private TestResultInfo resultInfo = null;
    private ConvertXMLToJson xmlToJson = new ConvertXMLToJson();

    /**
     * This is a test step.
     *  @param title The title
     * @param soivTestBase The TestBase
     * @param customerInfo The customer information object
     * @param tSpec The transaction specification object
     * @param resultInfo The result information object
     */
    public CreateCustomer(String title, SoivTestBase soivTestBase, CustomerInfo customerInfo,
                          TransactionSpecification tSpec, TestResultInfo resultInfo) {
        super(title);
        this.soivTestBase = soivTestBase;
        this.customerInfo = customerInfo;
        this.tSpec = tSpec;
        this.resultInfo = resultInfo;

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
        String body = RawMessages.getBodyFromJsonFilePlacedInTestCase(this.getClass().getName(), "provision",
                "createCustomer.xml");

        body = RawMessages.replaceVal(body, "customerBillCycle", customerInfo.getCustomerBillCycle());

        resultInfo = Steps.PROTOCOLS.bscs.sendBscsRequest(this.getClass().getName(), resultInfo, HttpMethod.POST, 200,
                "/wsi/services/ws_CIL_6_CustomerCreateService.wsdl/",body).run();

        HttpResponse response = (HttpResponse) resultInfo.getResult();
        String respBody = (String)response.getBody().getValue();
        respBody = xmlToJson.convertXMLToJSON(respBody);
        String customerIdBSCS = JsonPath.read(respBody,"$.SOAP-ENV:Envelope.SOAP-ENV:Body.customerCreateResponse.customerNew.csIdPub.content");
        customerInfo.setCustomerId(customerIdBSCS);
        LOG.info("customerIdBSCS : "+customerIdBSCS);
        return resultInfo;
    }

    public SoivTestBase getSoivTestBase() {
        return soivTestBase;
    }

    public CreateCustomer setSoivTestBase(SoivTestBase soivTestBase) {
        this.soivTestBase = soivTestBase;
        return this;
    }

    public CustomerInfo getCustomerInfo() {
        return customerInfo;
    }

    public CreateCustomer setCustomerInfo(CustomerInfo customerInfo) {
        this.customerInfo = customerInfo;
        return this;
    }

    public TransactionSpecification getCallInfo() {
        return tSpec;
    }

    public CreateCustomer setTransactionSpec(TransactionSpecification tSpec) {
        this.tSpec = tSpec;
        return this;
    }

    public CreateCustomer setName(String name) {
        setTitle(name);
        return this;
    }

}
