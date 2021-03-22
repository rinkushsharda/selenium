package com.ericsson.soiv.teststeps.provision.eoc;

import com.ericsson.jive.core.execution.TestStepBase;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpMethod;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpResponse;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.*;
import com.jayway.jsonpath.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddPoInShoppingCart extends TestStepBase<TestResultInfo> {
    private static Logger LOG = LoggerFactory.getLogger(AddPoInShoppingCart.class);
    private SoivTestBase soivTestBase = null;
    private CustomerInfo customerInfo = null;
    private TransactionSpecification tSpec = null;
    private TestResultInfo resultInfo = null;
    private CalculateDateTime getCurrentDate = new CalculateDateTime();

    /**
     * This is a test step.
     * 
     * @param title
     *            The title
     * @param soivTestBase
     *            The TestBase
     * @param customerInfo
     *            The customer information object
     * @param tSpec
     *            The transaction specification object
     * @param resultInfo
     *            The result information object
     */
    public AddPoInShoppingCart(String title, SoivTestBase soivTestBase, CustomerInfo customerInfo,
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
                tSpec.getJsonFileName());
        body = RawMessages.replaceVal(body, "scID", customerInfo.getShoppingCartId());
        body = RawMessages.replaceVal(body, "customerID", customerInfo.getCustomerId());
        body = RawMessages.replaceVal(body, "coDate", getCurrentDate.getCurrentDate());
        body = RawMessages.replaceVal(body, "sdpID", Integer.toString(tSpec.getSdpId()));
        body = RawMessages.replaceVal(body, "contractID", "CONTR00000"+customerInfo.getShoppingCartId());
        body = RawMessages.replaceVal(body, "Basic_PO_ID", String.valueOf(tSpec.getProductOfferingIds().get("basic")));

        if ("GSM".equals(customerInfo.getMarketType())) {
            body = RawMessages.replaceVal(body, "MARKET", "GSM");
            body = RawMessages.replaceVal(body, "SUBMARKET", "GSM");
            body = RawMessages.replaceVal(body, "NETWORK", "EUR01");

        } else {
            body = RawMessages.replaceVal(body, "MARKET", customerInfo.getMarket());
            body = RawMessages.replaceVal(body, "SUBMARKET", customerInfo.getSubMarket());
            body = RawMessages.replaceVal(body, "NETWORK", customerInfo.getNetwork());

        }

        resultInfo = Steps.PROTOCOLS.eoc.sendEocRequest(this.getClass().getName(), resultInfo, HttpMethod.POST, 201,
                "/eoc/on/v1/sc/" + customerInfo.getShoppingCartId() + "/pooi", body).run();

        HttpResponse response = (HttpResponse) resultInfo.getResult();
        String respBody = (String) response.getBody().getValue();

        String contractId = JsonPath.read(respBody, "$.relatedEntities[0].reference");
        String pooIid = JsonPath.read(respBody, "$.id");

        tSpec.setResponseBody(respBody);
        customerInfo.setContractId(contractId);
        customerInfo.setPooiId(pooIid);

        return resultInfo;
    }

    public SoivTestBase getSoivTestBase() {
        return soivTestBase;
    }

    public AddPoInShoppingCart setSoivTestBase(SoivTestBase soivTestBase) {
        this.soivTestBase = soivTestBase;
        return this;
    }

    public CustomerInfo getCustomerInfo() {
        return customerInfo;
    }

    public AddPoInShoppingCart setCustomerInfo(CustomerInfo customerInfo) {
        this.customerInfo = customerInfo;
        return this;
    }

    public TransactionSpecification getCallInfo() {
        return tSpec;
    }

    public AddPoInShoppingCart setTransactionSpec(TransactionSpecification tSpec) {
        this.tSpec = tSpec;
        return this;
    }

    public AddPoInShoppingCart setName(String name) {
        setTitle(name);
        return this;
    }

}
