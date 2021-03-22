package com.ericsson.soiv.teststeps.provision.eoc;

import com.ericsson.jive.core.execution.TestStepBase;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.ericsson.soiv.utils.JsonHelper.*;

public class BarringOfSubscription extends TestStepBase<TestResultInfo> {
    private static Logger LOG = LoggerFactory.getLogger(BarringOfSubscription.class);
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
    public BarringOfSubscription(String title, SoivTestBase soivTestBase, CustomerInfo customerInfo,
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

        tSpec.setJsonFileName("createShoppingCart.json");
        Steps.PROVISION.eoc.createShoppingCart("Provision : Create Shopping Cart For Barring Subscription", soivTestBase, customerInfo, tSpec, resultInfo)
                .run();

        resultInfo = Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart For Barring Subscription", soivTestBase, customerInfo, tSpec, resultInfo).run();

        String pooiId = getRequestedJsonElement(tSpec, String.valueOf(tSpec.getProductOfferingIds().get("basic")), "id");
        customerInfo.setPooiId(pooiId);

        resultInfo =Steps.PROTOCOLS.businessLogicEOC.getPooi("Get the BASIC POOI For Barring Subscription", soivTestBase, customerInfo, tSpec, resultInfo)
                .run();

        JSONObject getResponseBody = searchKeyAndUpdateJson(tSpec.getResponseBody(), "action", "Modify");

        getResponseBody = insertValues(getResponseBody.toString(), tSpec.getServiceCharacteristicsName(), tSpec.getServiceCharacteristicsValue() , "services",
                "serviceCharacteristics", "name");
        tSpec.setResponseBody(getResponseBody.toString());


        Steps.PROTOCOLS.businessLogicEOC
                .updatePooi("Personalization of Service Characteristics ", soivTestBase, customerInfo, tSpec, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision : Submit Shopping Cart For Personalization ", soivTestBase, customerInfo, tSpec, resultInfo).run();


        resultInfo = Steps.PROTOCOLS.businessLogicBSCS.getPoStatusBSCS("Read Contract For Verification : "+customerInfo.getContractId(), soivTestBase, customerInfo,tSpec, resultInfo)
                .run();
        return resultInfo;
    }

    public SoivTestBase getSoivTestBase() {
        return soivTestBase;
    }

    public BarringOfSubscription setSoivTestBase(SoivTestBase soivTestBase) {
        this.soivTestBase = soivTestBase;
        return this;
    }

    public CustomerInfo getCustomerInfo() {
        return customerInfo;
    }

    public BarringOfSubscription setCustomerInfo(CustomerInfo customerInfo) {
        this.customerInfo = customerInfo;
        return this;
    }

    public TransactionSpecification getCallInfo() {
        return tSpec;
    }

    public BarringOfSubscription setTransactionSpec(TransactionSpecification tSpec) {
        this.tSpec = tSpec;
        return this;
    }

    public BarringOfSubscription setName(String name) {
        setTitle(name);
        return this;
    }

}
