package com.ericsson.soiv.teststeps.provision.bscs;

import com.ericsson.jive.core.execution.Jive;
import com.ericsson.jive.core.execution.TestStepBase;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChangeCustomerBillingCycle extends TestStepBase<TestResultInfo> {
    private static Logger LOG = LoggerFactory.getLogger(ChangeCustomerBillingCycle.class);
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
    public ChangeCustomerBillingCycle(String title, SoivTestBase soivTestBase, CustomerInfo customerInfo,
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

        tSpec.setJsonFileName("createShoppingCartChangeBillCycle.json");

        Steps.PROVISION.eoc.createShoppingCart("Provision : Create Shopping Cart Change Bill Cycle: "+customerInfo.getCustomerId(), soivTestBase, customerInfo, tSpec, resultInfo).run();

        Steps.PROVISION.eoc.getShoppingCart("Provision : Get Shopping Cart For Customer : "+customerInfo.getCustomerId(),soivTestBase,customerInfo,tSpec,resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC.createBillCycleChangeOrderRequest("Change Bill Cycle : "+customerInfo.getCustomerNewBillCycle(),soivTestBase,customerInfo,tSpec,resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision : Submit Shopping Cart", soivTestBase, customerInfo, tSpec, resultInfo).run();

        Steps.PROTOCOLS.businessLogicBSCS.readCustomer("Protocol : Read Customer to Verify Bill Cycle",soivTestBase,customerInfo,tSpec,resultInfo)
                .run();

        return resultInfo;
    }

    public SoivTestBase getSoivTestBase() {
        return soivTestBase;
    }

    public ChangeCustomerBillingCycle setSoivTestBase(SoivTestBase soivTestBase) {
        this.soivTestBase = soivTestBase;
        return this;
    }

    public CustomerInfo getCustomerInfo() {
        return customerInfo;
    }

    public ChangeCustomerBillingCycle setCustomerInfo(CustomerInfo customerInfo) {
        this.customerInfo = customerInfo;
        return this;
    }

    public TransactionSpecification getCallInfo() {
        return tSpec;
    }

    public ChangeCustomerBillingCycle setTransactionSpec(TransactionSpecification tSpec) {
        this.tSpec = tSpec;
        return this;
    }

    public ChangeCustomerBillingCycle setName(String name) {
        setTitle(name);
        return this;
    }

}
