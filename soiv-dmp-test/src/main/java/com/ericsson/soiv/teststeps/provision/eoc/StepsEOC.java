package com.ericsson.soiv.teststeps.provision.eoc;

import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.protocols.eoc.businesslogic.GetShoppingCart;
import com.ericsson.soiv.utils.CustomerInfo;
import com.ericsson.soiv.utils.TestResultInfo;
import com.ericsson.soiv.utils.TransactionSpecification;

public class StepsEOC {
    /**
     * This is a test step.
     * 
     * @param title The title
     * @param soivTestBase The TestBase
     * @param customerInfo The customer information object
     * @param resultInfo The result object
     * @param tSpec Transaction Specification
     * @return CreateShoppingCart
     */

    public CreateShoppingCart createShoppingCart(String title, SoivTestBase soivTestBase, CustomerInfo customerInfo, TransactionSpecification tSpec,
                                                 TestResultInfo resultInfo) {
        return new CreateShoppingCart(title, soivTestBase, customerInfo, tSpec, resultInfo);
    }

    public AddPoInShoppingCart addPoInShoppingCart(String title, SoivTestBase soivTestBase, CustomerInfo customerInfo, TransactionSpecification tSpec,
                                                   TestResultInfo resultInfo) {
        return new AddPoInShoppingCart(title, soivTestBase, customerInfo, tSpec, resultInfo);
    }

    public GetShoppingCart getShoppingCart(String title, SoivTestBase soivTestBase, CustomerInfo customerInfo, TransactionSpecification tSpec,
                                           TestResultInfo resultInfo) {
        return new GetShoppingCart(title, soivTestBase, customerInfo, tSpec, resultInfo);
    }

    public ManageContractStatus manageContractStatus(String title, SoivTestBase soivTestBase, CustomerInfo customerInfo, TransactionSpecification tSpec,
                                                     TestResultInfo resultInfo) {
        return new ManageContractStatus(title, soivTestBase, customerInfo, tSpec, resultInfo);
    }

    public CreateCustomerContractBasicPO createCustomerContractBasicPO(String title, SoivTestBase soivTestBase, CustomerInfo customerInfo, TransactionSpecification tSpec,
                                                                       TestResultInfo resultInfo) {
        return new CreateCustomerContractBasicPO(title, soivTestBase, customerInfo, tSpec, resultInfo);
    }

    public UpdateOptionalPO updateOptionalPO(String title, SoivTestBase soivTestBase, CustomerInfo customerInfo, TransactionSpecification tSpec,
                                                 TestResultInfo resultInfo) {
        return new UpdateOptionalPO(title, soivTestBase, customerInfo, tSpec, resultInfo);
    }

    public RepurchaseOptionalPO repurchaseOptionalPO(String title, SoivTestBase soivTestBase, CustomerInfo customerInfo, TransactionSpecification tSpec,
                                             TestResultInfo resultInfo) {
        return new RepurchaseOptionalPO(title, soivTestBase, customerInfo, tSpec, resultInfo);
    }

    public BarringOfSubscription barringOfSubscription(String title, SoivTestBase soivTestBase, CustomerInfo customerInfo, TransactionSpecification tSpec,
                                                     TestResultInfo resultInfo) {
        return new BarringOfSubscription(title, soivTestBase, customerInfo, tSpec, resultInfo);
    }

    public ManageResourcesInContract manageResourcesInContract(String title, SoivTestBase soivTestBase, CustomerInfo customerInfo, TransactionSpecification tSpec,
                                                            TestResultInfo resultInfo) {
        return new ManageResourcesInContract(title, soivTestBase, customerInfo, tSpec, resultInfo);
    }
    public RepurchaseAddOnPO repurchaseAddOnPO(String title, SoivTestBase soivTestBase, CustomerInfo customerInfo, TransactionSpecification tSpec,
                                                     TestResultInfo resultInfo) {
        return new RepurchaseAddOnPO(title, soivTestBase, customerInfo, tSpec, resultInfo);
    }
}
