package com.ericsson.soiv.teststeps.protocols.eoc.businesslogic;

import com.ericsson.jive.core.execution.Jive;
import com.ericsson.jive.core.execution.TestStepBase;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpMethod;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpResponse;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.CustomerInfo;
import com.ericsson.soiv.utils.RawMessages;
import com.ericsson.soiv.utils.TestResultInfo;
import com.ericsson.soiv.utils.TransactionSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateCustomerChangeOrderAdminCharge extends TestStepBase<TestResultInfo> {
    private static Logger LOG = LoggerFactory.getLogger(CreateCustomerChangeOrderAdminCharge.class);
    private SoivTestBase soivTestBase = null;
    private CustomerInfo customerInfo = null;
    private TransactionSpecification tSpec = null;
    private TestResultInfo resultInfo = null;

    /**
     * This is a test step.
     *  @param title The title
     * @param soivTestBase The TestBase
     * @param customerInfo The customer information object
     * @param tSpec The transaction specification object
     * @param resultInfo The result information object
     */
    public CreateCustomerChangeOrderAdminCharge(String title, SoivTestBase soivTestBase, CustomerInfo customerInfo,
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

        String body = RawMessages.getBodyFromJsonFilePlacedInTestStep(this.getClass().getName(), "protocols/eoc",
                "CreateCustomerChangeOrderAdminCharge.json");


        body = RawMessages.replaceVal(body, "orderOwner", customerInfo.getRequester());

        body = RawMessages.replaceVal(body, "scID", customerInfo.getShoppingCartId());

        body = RawMessages.replaceVal(body, "contractId", customerInfo.getContractId());

        body = RawMessages.replaceVal(body, "eventCode", tSpec.getEventCode());

        body = RawMessages.replaceVal(body, "price", tSpec.getPrice());

        body = RawMessages.replaceVal(body, "customerID", customerInfo.getCustomerId());

        resultInfo = Steps.PROTOCOLS.eoc.sendEocRequest(this.getClass().getName(), resultInfo, HttpMethod.POST, 201,

                "/eoc/on/v1/sc/" + customerInfo.getShoppingCartId() + "/ccoi",body).run();

        HttpResponse response = (HttpResponse) resultInfo.getResult();
        String respBody = (String)response.getBody().getValue();
        Jive.log(" CCOI Response Body : "+respBody);

        return resultInfo;

    }

    public SoivTestBase getSoivTestBase() {
        return soivTestBase;
    }

    public CreateCustomerChangeOrderAdminCharge setSoivTestBase(SoivTestBase soivTestBase) {
        this.soivTestBase = soivTestBase;
        return this;
    }

}
