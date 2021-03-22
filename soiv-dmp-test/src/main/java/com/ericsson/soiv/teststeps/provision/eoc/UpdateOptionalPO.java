package com.ericsson.soiv.teststeps.provision.eoc;

import com.ericsson.jive.core.execution.Jive;
import com.ericsson.jive.core.execution.TestStepBase;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.CustomerInfo;
import com.ericsson.soiv.utils.TestResultInfo;
import com.ericsson.soiv.utils.TransactionSpecification;
import org.json.JSONObject;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.ericsson.soiv.utils.JsonHelper.getRequestPooiId;
import static com.ericsson.soiv.utils.JsonHelper.searchKeyAndUpdateJson;

public class UpdateOptionalPO extends TestStepBase<TestResultInfo> {
    private static Logger LOG = LoggerFactory.getLogger(UpdateOptionalPO.class);
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
    public UpdateOptionalPO(String title, SoivTestBase soivTestBase, CustomerInfo customerInfo,
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
        Steps.PROVISION.eoc.createShoppingCart("Provision : Create Shopping Cart to Deactivate/ReActivate the PO", soivTestBase, customerInfo, tSpec, resultInfo)
                .run();

        Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart to Update the PO", soivTestBase, customerInfo, tSpec, resultInfo).run();

        String pooiId = getRequestPooiId(tSpec, tSpec.getProductOfferingIds().get("optional").toString(), "id");
        customerInfo.setPooiId(pooiId);

        Steps.PROTOCOLS.businessLogicEOC.getPooi("Getting the POOI To Update the PO", soivTestBase, customerInfo, tSpec, resultInfo).run();

        JSONObject getAction = searchKeyAndUpdateJson(tSpec.getResponseBody(), "action", tSpec.getActionCode());
        JSONObject getReasonCode = searchKeyAndUpdateJson(getAction.toString(), "reasonCode", tSpec.getReasonCode());

        tSpec.setResponseBody(getReasonCode.toString());

        /* Update POOI Request with submitting the shopping cart */

        Steps.PROTOCOLS.businessLogicEOC.updatePooi("Update For DeActivation/ReActivation PO ", soivTestBase, customerInfo, tSpec, resultInfo)
                .run();

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision : Submit Shopping Cart to terminate the PO", soivTestBase, customerInfo, tSpec, resultInfo).run();

        /* asserting the action by comparing the status */

        Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart to verify terminated PO status", soivTestBase, customerInfo, tSpec, resultInfo).run();

        String getActualAction = getRequestPooiId(tSpec, String.valueOf(tSpec.getProductOfferingIds().get("optional")),
                "action");
        Assert.assertEquals(tSpec.getActionCode(), getActualAction);
        Jive.log("OK : Expected & Actual Action : " + getActualAction + " Matched Successfully!");

        return resultInfo;
    }

    public SoivTestBase getSoivTestBase() {
        return soivTestBase;
    }

    public UpdateOptionalPO setSoivTestBase(SoivTestBase soivTestBase) {
        this.soivTestBase = soivTestBase;
        return this;
    }

    public CustomerInfo getCustomerInfo() {
        return customerInfo;
    }

    public UpdateOptionalPO setCustomerInfo(CustomerInfo customerInfo) {
        this.customerInfo = customerInfo;
        return this;
    }

    public TransactionSpecification getCallInfo() {
        return tSpec;
    }

    public UpdateOptionalPO setTransactionSpec(TransactionSpecification tSpec) {
        this.tSpec = tSpec;
        return this;
    }

    public UpdateOptionalPO setName(String name) {
        setTitle(name);
        return this;
    }

}
