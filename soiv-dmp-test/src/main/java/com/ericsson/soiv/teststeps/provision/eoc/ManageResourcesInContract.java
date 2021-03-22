package com.ericsson.soiv.teststeps.provision.eoc;

import com.ericsson.jive.core.execution.Jive;
import com.ericsson.jive.core.execution.TestStepBase;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.*;
import com.jayway.jsonpath.JsonPath;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.ericsson.soiv.utils.JsonHelper.getRequestedJsonElement;
import static com.ericsson.soiv.utils.JsonHelper.searchKeyAndUpdateJson;

public class ManageResourcesInContract extends TestStepBase<TestResultInfo> {
    private static Logger LOG = LoggerFactory.getLogger(ManageResourcesInContract.class);
    private SoivTestBase soivTestBase = null;
    private CustomerInfo customerInfo = null;
    private TransactionSpecification tSpec = null;
    private TestResultInfo resultInfo = null;

    /**
     * This is a test step.
     * @param title The title
     * @param soivTestBase The TestBase
     * @param customerInfo The customer information object
     * @param tSpec The transaction specification object
     * @param resultInfo The result information object
     */
    public ManageResourcesInContract(String title, SoivTestBase soivTestBase, CustomerInfo customerInfo,
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

        /* =================For Personalized Adding Devices================= */

        tSpec.setJsonFileName("createShoppingCart.json");
        Steps.PROVISION.eoc
                .createShoppingCart("Provision : Create Shopping Cart For Personalized Resource", soivTestBase, customerInfo, tSpec, resultInfo)
                .run();

        Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart For Personalized Resource", soivTestBase, customerInfo, tSpec, resultInfo)
                .run();

        String pooiId = getRequestedJsonElement(tSpec, String.valueOf(tSpec.getProductOfferingIds().get("optional")), "id");
        customerInfo.setPooiId(pooiId);

        resultInfo = Steps.PROTOCOLS.businessLogicEOC.getPooi("Getting the POOI For Personalized Resource", soivTestBase, customerInfo, tSpec, resultInfo)
                .run();

        JSONObject getPOOIRequestBody = searchKeyAndUpdateJson(tSpec.getResponseBody(), tSpec.getActionCode(),
                tSpec.getActionValue());

        /* Update POOI Request with submitting the shopping cart */
        tSpec.setResponseBody(getPOOIRequestBody.toString());

        Steps.PROTOCOLS.businessLogicEOC
                .updatePooi("Updation Action Values for Personalization", soivTestBase, customerInfo, tSpec, resultInfo)
                .run();

        resultInfo = Steps.PROTOCOLS.businessLogicEOC.getPooi("Getting the POOI For Personalized Resource", soivTestBase, customerInfo, tSpec, resultInfo)
                .run();

        tSpec.setResourceSpecification(JsonPath.read(tSpec.getResponseBody(),"$.resources[0].resourceSpecification"));
        tSpec.setResourceParentId(JsonPath.read(tSpec.getResponseBody(),"$.resources[0].parentId"));

        if("Add".equalsIgnoreCase(tSpec.getResourceAction())) {
            for (int i=0; i<tSpec.getResourceCardinality(); i++) {
                tSpec.setDeviceId(Constants.DEVICEID + HelperClass.generateRandomBigIntegerFromRange("1","100000"));
                Steps.PROTOCOLS.businessLogicEOC.addResources("Add Resource No : "+i, soivTestBase, customerInfo, tSpec, resultInfo)
                        .run();
            }
        }

        if("Delete".equalsIgnoreCase(tSpec.getResourceAction())) {

            Steps.PROTOCOLS.businessLogicEOC.getResources("Get All Resource Before Remove",soivTestBase,customerInfo,tSpec,resultInfo)
                    .run();

            String getResourceId = JsonPath.read(tSpec.getResponseBody(),"$[0].id");
            Jive.log("Set First Device Resource Id To Remove : "+getResourceId);
            tSpec.setResourceId(getResourceId);

            Steps.PROTOCOLS.businessLogicEOC.deleteResources("Delete Resource : "+tSpec.getResourceId(), soivTestBase, customerInfo, tSpec, resultInfo)
                    .run();
        }

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision : Submit Shopping Cart For Personalized Resource", soivTestBase, customerInfo, tSpec, resultInfo)
                .run();


        return resultInfo;
    }

    public SoivTestBase getSoivTestBase() {
        return soivTestBase;
    }

    public ManageResourcesInContract setSoivTestBase(SoivTestBase soivTestBase) {
        this.soivTestBase = soivTestBase;
        return this;
    }

    public CustomerInfo getCustomerInfo() {
        return customerInfo;
    }

    public ManageResourcesInContract setCustomerInfo(CustomerInfo customerInfo) {
        this.customerInfo = customerInfo;
        return this;
    }

    public TransactionSpecification getCallInfo() {
        return tSpec;
    }

    public ManageResourcesInContract setTransactionSpec(TransactionSpecification tSpec) {
        this.tSpec = tSpec;
        return this;
    }

    public ManageResourcesInContract setName(String name) {
        setTitle(name);
        return this;
    }

}
