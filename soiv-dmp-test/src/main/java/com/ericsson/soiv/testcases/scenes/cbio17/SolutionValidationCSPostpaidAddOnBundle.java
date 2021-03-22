package com.ericsson.soiv.testcases.scenes.cbio17;

import static com.ericsson.soiv.utils.JsonHelper.getRequestedJsonElement;
import static com.ericsson.soiv.utils.JsonHelper.getRequestPooiId;
import static com.ericsson.soiv.utils.JsonHelper.searchKeyAndUpdateJson;
import static com.ericsson.soiv.utils.http.ReadBusinessData.readBusinessCacheData;

import java.io.IOException;
import java.util.LinkedHashMap;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import com.ericsson.jive.core.execution.Description;
import com.ericsson.jive.core.execution.Fixture;
import com.ericsson.jive.core.execution.Id;
import com.ericsson.jive.core.execution.Jive;
import com.ericsson.jive.core.execution.Tags;
import com.ericsson.soiv.fixtures.SoivFixture;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.CustomerInfo;
import com.ericsson.soiv.utils.TestResultInfo;
import com.ericsson.soiv.utils.TransactionSpecification;

//Created by ZEAHDCE 22 May 2019
@Fixture(SoivFixture.class)
public class SolutionValidationCSPostpaidAddOnBundle extends SoivTestBase {
    private TestResultInfo resultInfo = new TestResultInfo();
    private CustomerInfo customerInfo = new CustomerInfo();
    private TransactionSpecification ts = new TransactionSpecification();
    private LinkedHashMap<String, String> productOfferings = new LinkedHashMap<String, String>();

    @Test
    @Id("00000047")
    @Tags("OT-Regression, OT-Prepaid, EC-Controlled, OT-CBiO17")
    @Description("TS799 - Solution Validation CS AddOn Bundled PO")
    public void solutionValidationCSPostpaidAddOnBundle() throws IOException {
        LinkedHashMap<String, String> businessCache = new LinkedHashMap<>(
                readBusinessCacheData(this.getTestCase().getId()));
        customerInfo.setCustomerBillCycle(businessCache.get("CUSTOMER.BILLCYCLE"));
        customerInfo.setOrderMode(businessCache.get("ORDER.MODE"));
        customerInfo.setRequester(businessCache.get("ORDER.REQUESTER"));

        productOfferings.put("basic", businessCache.get("PO.ID_BASIC_ADDON_BUNDLE"));

        productOfferings.put("addon", businessCache.get("PO.ID_ADDON_ADDON_BUNDLE"));

        ts.setProductOfferingIds(productOfferings);

        /******************************
         * Create Contract with Basic PO and Submit the cart
         **************************/
        
        customerInfo.setNpCodePub(businessCache.get("MSISDN.NPCODEPUB"));
        customerInfo.setPlCodePub(businessCache.get("MSISDN.PLCODEPUB"));
        customerInfo.setSubmIdPub(businessCache.get("MSISDN.SUBMIDPUB"));
        customerInfo.setHlCode(businessCache.get("MSISDN.HLCODE"));
        customerInfo.setRsCode(businessCache.get("MSISDN.RSCODE"));
        customerInfo.setCsControlled(businessCache.get("MSISDN.CSCONTROLLED"));

        Steps.PROVISION.eoc.createCustomerContractBasicPO("Create Customer and Contract with Basic PO", this,
                customerInfo, ts, resultInfo).run();
        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Submit shopping cart for Basic PO ", this, customerInfo, ts, resultInfo).run();

        /******************************
         * Create Contract with addOn Bundled PO with addOn PO and Submit the cart
         **************************/

        ts.setJsonFileName("createShoppingCart.json");
        Steps.PROVISION.eoc.createShoppingCart("Provision : Create Shopping Cart For addOn Bundled PO and addOn PO",
                this, customerInfo, ts, resultInfo).run();

        // Set Basic PO Relies On
        Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart To get Basic PO", this, customerInfo, ts, resultInfo)
                .run();

        String pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("basic")), "id");
        ts.setBasicPoReferenceId(pooiId);

        ts.setJsonFileName("AddOnPOonBasic.json");

        Steps.PROTOCOLS.businessLogicEOC.addAddOnPOonBasicPO("Provision : Update addOn Bundled PO in to Basic PO", this,
                customerInfo, ts, resultInfo).run();

        // Set Bundle PO Child of
        Steps.PROTOCOLS.businessLogicEOC.getShoppingCart("Protocol : Get Shopping Cart To get addOn Bundled PO", this,
                customerInfo, ts, resultInfo).run();

        pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("addon")), "id");
        ts.setBasicPoReferenceId(pooiId); // set the addOn Bundled PO pooiId

        ts.setJsonFileName("AddOnPOonBundle.json");

        productOfferings.put("addon", businessCache.get("PO.ID_AddOn"));
        ts.setProductOfferingIds(productOfferings);
        Steps.PROTOCOLS.businessLogicEOC.addAddOnPOonBasicPO("Provision : Update addOn PO in to addOn Bundled PO", this,
                customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision : Submit Shopping Cart For addOn Bundled and addOn PO", this,
                        customerInfo, ts, resultInfo)
                .run();

        /******************************
         * Create Contract with another instance of addOn Bundled PO, addOn PO and
         * Submit the cart
         **************************/

        ts.setJsonFileName("createShoppingCart.json");
        productOfferings.put("addon", businessCache.get("PO.ID_ADDON_ADDON_BUNDLE"));
        ts.setProductOfferingIds(productOfferings);

        Steps.PROVISION.eoc.createShoppingCart(
                "Provision : Create Shopping Cart For another instance of addOn Bundled PO and addOn PO", this,
                customerInfo, ts, resultInfo).run();

        // Set Basic PO Relies On
        Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart To get Basic PO", this, customerInfo, ts, resultInfo)
                .run();

        pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("basic")), "id");
        ts.setBasicPoReferenceId(pooiId);

        ts.setJsonFileName("AddOnPOonBasic.json");

        Steps.PROTOCOLS.businessLogicEOC.addAddOnPOonBasicPO("Provision : Update addOn Bundled PO in to Basic PO", this,
                customerInfo, ts, resultInfo).run();

        // Set Bundle PO Child of
        Steps.PROTOCOLS.businessLogicEOC.getShoppingCart("Protocol : Get Shopping Cart To get addOn Bundled PO", this,
                customerInfo, ts, resultInfo).run();

        pooiId = getRequestPooiId(ts, String.valueOf(ts.getProductOfferingIds().get("addon")), "id");

        ts.setBasicPoReferenceId(pooiId);

        productOfferings.put("addon", businessCache.get("PO.ID_AddOn"));
        ts.setProductOfferingIds(productOfferings);

        ts.setJsonFileName("AddOnPOonBundle.json");

        Steps.PROTOCOLS.businessLogicEOC.addAddOnPOonBasicPO("Provision : Update addOn PO in to addOn Bundled PO", this,
                customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision : Submit Shopping Cart For addOn Bundled PO and addOn PO", this,
                        customerInfo, ts, resultInfo)
                .run();

        /***************************
         * Delete the first instance of the addOn PO And Verify the same
         *******************************/
        productOfferings.put("addon", businessCache.get("PO.ID_AddOn"));
        ts.setProductOfferingIds(productOfferings);
        ts.setJsonFileName("createShoppingCart.json");
        Steps.PROVISION.eoc.createShoppingCart("Provision : Create Shopping Cart for addOn PO Termination", this,
                customerInfo, ts, resultInfo).run();
        Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart", this, customerInfo, ts, resultInfo).run();

        pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("addon")), "id");
        customerInfo.setPooiId(pooiId);

        Steps.PROTOCOLS.businessLogicEOC.getPooi("Getting the POOI of addOn PO", this, customerInfo, ts, resultInfo)
                .run();
        ts.setActionCode(businessCache.get("ACTION.CODE_DELETE"));

        JSONObject getAction = searchKeyAndUpdateJson(ts.getResponseBody(), "action", ts.getActionCode());

        ts.setResponseBody(getAction.toString());

        Steps.PROTOCOLS.businessLogicEOC
                .updatePooi("Update For DeActivation addOn PO ", this, customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC.submitShoppingCart(
                "Provision : Submit Shopping Cart for addOn PO Deactivation", this, customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC.getShoppingCart("Protocol : Get Shopping Cart for Checking addOn PO Status",
                this, customerInfo, ts, resultInfo).run();

        String getActualAction = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("addon")),
                "action");
        Assert.assertEquals(ts.getActionCode(), getActualAction);
        Jive.log("OK : Expected & Actual Action : " + getActualAction + " Matched Successfully!");

        /***************************
         * Delete the second instance of the addOn PO And Verify the same
         *******************************/

        productOfferings.put("addon", businessCache.get("PO.ID_AddOn"));
        ts.setProductOfferingIds(productOfferings);
        ts.setJsonFileName("createShoppingCart.json");
        Steps.PROVISION.eoc
                .createShoppingCart("Provision : Create Shopping Cart for Another instance of addOn PO Termination",
                        this, customerInfo, ts, resultInfo)
                .run();
        Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart", this, customerInfo, ts, resultInfo).run();

        pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("addon")), "id");
        customerInfo.setPooiId(pooiId);

        Steps.PROTOCOLS.businessLogicEOC.getPooi("Getting the addOn POOI ", this, customerInfo, ts, resultInfo).run();

        ts.setActionCode(businessCache.get("ACTION.CODE_DELETE"));

        getAction = searchKeyAndUpdateJson(ts.getResponseBody(), "action", ts.getActionCode());

        ts.setResponseBody(getAction.toString());

        Steps.PROTOCOLS.businessLogicEOC.updatePooi("Update For DeActivation of Another Instance of addOn PO ", this,
                customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision : Submit Shopping Cart for Another Instance of addOn PO Deactivation",
                        this, customerInfo, ts, resultInfo)
                .run();

        Steps.PROTOCOLS.businessLogicEOC.getShoppingCart("Protocol : Get Shopping Cart for Checking addOn PO Status",
                this, customerInfo, ts, resultInfo).run();
        getActualAction = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("addon")),
                "action");
        Assert.assertEquals(ts.getActionCode(), getActualAction);
        Jive.log("OK : Expected & Actual Action : " + getActualAction + " Matched Successfully!");

        /********************
         * Add the addOn PO again to the addOn Bundled PO
         ********************************/

        ts.setJsonFileName("createShoppingCart.json");
        productOfferings.put("addon", businessCache.get("PO.ID_ADDON_ADDON_BUNDLE"));
        ts.setProductOfferingIds(productOfferings);

        Steps.PROVISION.eoc.createShoppingCart("Provision : Create Shopping Cart For adding again addOn PO", this,
                customerInfo, ts, resultInfo).run();

        // Set Basic PO Relies On
        Steps.PROTOCOLS.businessLogicEOC.getShoppingCart("Protocol : Get Shopping Cart To get addOn Bundled PO", this,
                customerInfo, ts, resultInfo).run();

        pooiId = getRequestPooiId(ts, String.valueOf(ts.getProductOfferingIds().get("addon")), "id");
        ts.setBasicPoReferenceId(pooiId);

        productOfferings.put("addon", businessCache.get("PO.ID_AddOn"));
        ts.setProductOfferingIds(productOfferings);

        ts.setJsonFileName("AddOnPOonBundle.json"); // AddOnPOonBundle

        ts.setExpectedStatusCode(Integer.parseInt(businessCache.get("EXPECTED.STATUS.CODE_ADDON_BUNDLE")));
        Steps.PROTOCOLS.businessLogicEOC.addAddOnPOonBasicPO("Provision : Update addOn PO in to addOn Bundled PO", this,
                customerInfo, ts, resultInfo).run();
    }

}