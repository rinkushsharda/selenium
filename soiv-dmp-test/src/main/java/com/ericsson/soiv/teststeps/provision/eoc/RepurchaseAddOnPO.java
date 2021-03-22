package com.ericsson.soiv.teststeps.provision.eoc;

import com.ericsson.jive.core.execution.TestStepBase;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.CustomerInfo;
import com.ericsson.soiv.utils.TestResultInfo;
import com.ericsson.soiv.utils.TransactionSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.ericsson.soiv.utils.JsonHelper.getRequestedJsonElement;

public class RepurchaseAddOnPO extends TestStepBase<TestResultInfo> {
    private static Logger LOG = LoggerFactory.getLogger(RepurchaseAddOnPO.class);
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
    public RepurchaseAddOnPO(String title, SoivTestBase soivTestBase, CustomerInfo customerInfo,
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
        Steps.PROVISION.eoc.createShoppingCart("Provision : Create Shopping Cart For Repurchase", soivTestBase, customerInfo, tSpec, resultInfo)
                .run();

        // Set Basic PO Relies On
        Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart To get Basic PO", soivTestBase, customerInfo, tSpec, resultInfo).run();

        String pooiId = getRequestedJsonElement(tSpec,String.valueOf(tSpec.getProductOfferingIds().get("basic")),"id");
        tSpec.setBasicPoReferenceId(pooiId);

        tSpec.setJsonFileName("AddOnPOonBasic.json");
        Steps.PROTOCOLS.businessLogicEOC.addAddOnPOonBasicPO("Protocol : Update Add On PO in to Basic PO",soivTestBase,customerInfo,tSpec,resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision : Submit Shopping Cart For Re Purchase", soivTestBase, customerInfo, tSpec, resultInfo).run();

        return resultInfo;
    }

    public SoivTestBase getSoivTestBase() {
        return soivTestBase;
    }

    public RepurchaseAddOnPO setSoivTestBase(SoivTestBase soivTestBase) {
        this.soivTestBase = soivTestBase;
        return this;
    }

    public CustomerInfo getCustomerInfo() {
        return customerInfo;
    }

    public RepurchaseAddOnPO setCustomerInfo(CustomerInfo customerInfo) {
        this.customerInfo = customerInfo;
        return this;
    }

    public TransactionSpecification getCallInfo() {
        return tSpec;
    }

    public RepurchaseAddOnPO setTransactionSpec(TransactionSpecification tSpec) {
        this.tSpec = tSpec;
        return this;
    }

    public RepurchaseAddOnPO setName(String name) {
        setTitle(name);
        return this;
    }

}
