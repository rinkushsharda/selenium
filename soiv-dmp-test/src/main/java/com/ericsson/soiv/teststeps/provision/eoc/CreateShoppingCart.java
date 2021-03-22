package com.ericsson.soiv.teststeps.provision.eoc;

import com.ericsson.jive.core.execution.TestStepBase;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpMethod;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpResponse;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.CustomerInfo;
import com.ericsson.soiv.utils.RawMessages;
import com.ericsson.soiv.utils.TestResultInfo;
import com.ericsson.soiv.utils.TransactionSpecification;
import com.jayway.jsonpath.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class CreateShoppingCart extends TestStepBase<TestResultInfo> {
    private static Logger LOG = LoggerFactory.getLogger(CreateShoppingCart.class);
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
    public CreateShoppingCart(String title, SoivTestBase soivTestBase, CustomerInfo customerInfo,
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

        String body = RawMessages.getBodyFromJsonFilePlacedInTestCase(this.getClass().getName(),
                    "provision" , tSpec.getJsonFileName());

        String generateShoppingCartId = UUID.randomUUID().toString();
        generateShoppingCartId = generateShoppingCartId.substring(0, Math.min(generateShoppingCartId.length(), 32));

        body = RawMessages.replaceVal(body, "requestID", generateShoppingCartId);
        body = RawMessages.replaceVal(body, "customerBillCycle", customerInfo.getCustomerBillCycle());
        body = RawMessages.replaceVal(body, "customerID", customerInfo.getCustomerId());
        body = RawMessages.replaceVal(body, "mode", customerInfo.getOrderMode());
        body = RawMessages.replaceVal(body, "requester", customerInfo.getRequester());
        body = RawMessages.replaceVal(body, "salesChannel", customerInfo.getSalesChannel());

        resultInfo = Steps.PROTOCOLS.eoc.sendEocRequest(this.getClass().getName(), resultInfo, HttpMethod.POST, 201,
                "/eoc/on/v1/sc/",body).run();

        HttpResponse response = (HttpResponse) resultInfo.getResult();
        String respBody = (String)response.getBody().getValue();
        String shoppingCartId = JsonPath.read(respBody,"$.id");
        customerInfo.setShoppingCartId(shoppingCartId);
        LOG.info("shoppingCartId : "+shoppingCartId);
        return resultInfo;
    }

    public SoivTestBase getSoivTestBase() {
        return soivTestBase;
    }

    public CreateShoppingCart setSoivTestBase(SoivTestBase soivTestBase) {
        this.soivTestBase = soivTestBase;
        return this;
    }

    public CustomerInfo getCustomerInfo() {
        return customerInfo;
    }

    public CreateShoppingCart setCustomerInfo(CustomerInfo customerInfo) {
        this.customerInfo = customerInfo;
        return this;
    }

    public TransactionSpecification getCallInfo() {
        return tSpec;
    }

    public CreateShoppingCart setTransactionSpec(TransactionSpecification tSpec) {
        this.tSpec = tSpec;
        return this;
    }

    public CreateShoppingCart setName(String name) {
        setTitle(name);
        return this;
    }

}
