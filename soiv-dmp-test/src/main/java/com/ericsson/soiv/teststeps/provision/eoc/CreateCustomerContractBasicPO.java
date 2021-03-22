package com.ericsson.soiv.teststeps.provision.eoc;

import com.ericsson.jive.core.execution.TestStepBase;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.CustomerInfo;
import com.ericsson.soiv.utils.TestResultInfo;
import com.ericsson.soiv.utils.TransactionSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateCustomerContractBasicPO extends TestStepBase<TestResultInfo> {
    private static Logger LOG = LoggerFactory.getLogger(CreateCustomerContractBasicPO.class);
    private SoivTestBase soivTestBase = null;
    private CustomerInfo customerInfo = null;
    private TransactionSpecification tSpec = null;
    private TestResultInfo resultInfo = null;

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
    public CreateCustomerContractBasicPO(String title, SoivTestBase soivTestBase, CustomerInfo customerInfo,
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

        resultInfo = Steps.PROVISION.bscs
                .createCustomer("Provision : Create Customer", soivTestBase, customerInfo, tSpec, resultInfo).run();

        tSpec.setJsonFileName("createShoppingCart.json");
        resultInfo = Steps.PROVISION.eoc
                .createShoppingCart("Provision : Create Shopping Cart", soivTestBase, customerInfo, tSpec, resultInfo)
                .run();

        tSpec.setJsonFileName("selectSerialNumber.xml");
        resultInfo = Steps.PROTOCOLS.businessLogicBSCS
                .getFreeSerialNumber("Protocol : Get Free Serial Number", soivTestBase, customerInfo, tSpec, resultInfo)
                .run();

        tSpec.setJsonFileName("selectLogicalResource.xml");
        resultInfo = Steps.PROTOCOLS.businessLogicBSCS.selectLogicalResource("Protocol : Get Free MSISDN Number",
                soivTestBase, customerInfo, tSpec, resultInfo).run();
        tSpec.setJsonFileName("addProductOfferingShoppingCart.json");
        customerInfo.setMsisdn(customerInfo.getresource().toString());
        resultInfo = Steps.PROVISION.eoc.addPoInShoppingCart("Provision : Add PO in Shopping Cart", soivTestBase,
                customerInfo, tSpec, resultInfo).run();

        resultInfo = Steps.PROTOCOLS.businessLogicEOC
                .updateProductOffering("Provision : Update Basic PO", soivTestBase, customerInfo, tSpec, resultInfo)
                .run();

        return resultInfo;
    }

    public SoivTestBase getSoivTestBase() {
        return soivTestBase;
    }

    public CreateCustomerContractBasicPO setSoivTestBase(SoivTestBase soivTestBase) {
        this.soivTestBase = soivTestBase;
        return this;
    }

    public CustomerInfo getCustomerInfo() {
        return customerInfo;
    }

    public CreateCustomerContractBasicPO setCustomerInfo(CustomerInfo customerInfo) {
        this.customerInfo = customerInfo;
        return this;
    }

    public TransactionSpecification getCallInfo() {
        return tSpec;
    }

    public CreateCustomerContractBasicPO setTransactionSpec(TransactionSpecification tSpec) {
        this.tSpec = tSpec;
        return this;
    }

    public CreateCustomerContractBasicPO setName(String name) {
        setTitle(name);
        return this;
    }

}
