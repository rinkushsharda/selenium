package com.ericsson.soiv.teststeps.provision.bscs;

import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.utils.CustomerInfo;
import com.ericsson.soiv.utils.TestResultInfo;
import com.ericsson.soiv.utils.TransactionSpecification;

public class StepsBSCS {
    /**
     * This is a test step.
     * 
     * @param title The title
     * @param soivTestBase The TestBase
     * @param customerInfo The customer information object
     * @param resultInfo The result object
     * @param tSpec Transaction Specification
     * @return CreateParty
     */

    public CreateCustomer createCustomer(String title, SoivTestBase soivTestBase, CustomerInfo customerInfo, TransactionSpecification tSpec,
                                         TestResultInfo resultInfo) {
        return new CreateCustomer(title, soivTestBase, customerInfo, tSpec, resultInfo);
    }

    public ChangeCustomerBillingCycle changeCustomerBillingCycle(String title, SoivTestBase soivTestBase, CustomerInfo customerInfo, TransactionSpecification tSpec,
                                         TestResultInfo resultInfo) {
        return new ChangeCustomerBillingCycle(title, soivTestBase, customerInfo, tSpec, resultInfo);
    }
}
