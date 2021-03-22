package com.ericsson.soiv.testcases.scenes.cbio17;

import com.ericsson.jive.core.execution.*;
import com.ericsson.soiv.fixtures.SoivFixture;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.CustomerInfo;
import com.ericsson.soiv.utils.TestResultInfo;
import com.ericsson.soiv.utils.TransactionSpecification;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.LinkedHashMap;

import static com.ericsson.soiv.utils.JsonHelper.getRequestedJsonElement;
import static com.ericsson.soiv.utils.JsonHelper.searchKeyAndUpdateJson;
import static com.ericsson.soiv.utils.http.ReadBusinessData.readBusinessCacheData;

//Created By ZMUKMAN 01-May-2019

@Fixture(SoivFixture.class)
public class PostpaidPOBSCSAddOnPromotion extends SoivTestBase {
    private TestResultInfo resultInfo = new TestResultInfo();
    private CustomerInfo customerInfo = new CustomerInfo();
    private TransactionSpecification ts = new TransactionSpecification();

    @Test
    @Id("00000016")
    @Tags("OT-Regression, OT-Postpaid, EC-Controlled, OT-CBiO17")
    @Description("TS733 - Postpaid PO BSCS AddOn Promotion")
    public void postpaidPOBscsAddOnPromotion() throws IOException {

        LinkedHashMap<String, String> businessCache = new LinkedHashMap<>(
                readBusinessCacheData(this.getTestCase().getId()));
        customerInfo.setCustomerBillCycle(businessCache.get("CUSTOMER.BILLCYCLE"));
        customerInfo.setOrderMode(businessCache.get("ORDER.MODE"));
        customerInfo.setRequester(businessCache.get("ORDER.REQUESTER"));

        LinkedHashMap<String, String> productOfferings = new LinkedHashMap<>();
        productOfferings.put("basic", businessCache.get("PO.ID_BASIC"));
        productOfferings.put("addon", businessCache.get("PO.ID_ADDON"));
        ts.setProductOfferingIds(productOfferings);

        /*****************************
         * createCustomerContractBasicPO
         ***************************/
        customerInfo.setNpCodePub(businessCache.get("MSISDN.NPCODEPUB"));
        customerInfo.setPlCodePub(businessCache.get("MSISDN.PLCODEPUB"));
        customerInfo.setSubmIdPub(businessCache.get("MSISDN.SUBMIDPUB"));
        customerInfo.setHlCode(businessCache.get("MSISDN.HLCODE"));
        customerInfo.setRsCode(businessCache.get("MSISDN.RSCODE"));
        customerInfo.setCsControlled(businessCache.get("MSISDN.CSCONTROLLED"));

        Steps.PROVISION.eoc.createCustomerContractBasicPO("Create Customer and Contract with Basic PO", this,
                customerInfo, ts, resultInfo).run();

        /*****************************
         * Add addOnPO and Submit the Cart
         **************************/

        ts.setJsonFileName("AddOnPOonBasic.json");
        Steps.PROTOCOLS.businessLogicEOC
                .addAddOnPOonBasicPO("Provision : Update Add On PO in to Basic PO", this, customerInfo, ts, resultInfo)
                .run();
        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Submit shopping cart for Add on PO ", this, customerInfo, ts, resultInfo).run();

        /****************************
         * PO Termination with Update POII and submit the Cart
         ******************/

        ts.setJsonFileName("createShoppingCart.json");
        Steps.PROVISION.eoc.createShoppingCart("Provision : Create Shopping Cart for PO Termination", this,
                customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart for PO Termination", this, customerInfo, ts, resultInfo)
                .run();

        String pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("addon")), "id");
        customerInfo.setPooiId(pooiId);

        Steps.PROTOCOLS.businessLogicEOC.getPooi("Getting the POOI ", this, customerInfo, ts, resultInfo).run();

        ts.setActionCode("Delete");

        JSONObject getAction = searchKeyAndUpdateJson(ts.getResponseBody(), "action", ts.getActionCode());

        ts.setResponseBody(getAction.toString());

        Steps.PROTOCOLS.businessLogicEOC.updatePooi("Update For DeActivation PO ", this, customerInfo, ts, resultInfo)
                .run();

        Steps.PROTOCOLS.businessLogicEOC.submitShoppingCart("Provision : Submit Shopping Cart for PO Termination", this,
                customerInfo, ts, resultInfo).run();

        /****************************
         * Verification of the PO Status after PO Termination in Get Shopping Cart
         ******************/

        Steps.PROTOCOLS.businessLogicEOC.getShoppingCart("Protocol : Get Shopping Cart for getting PO Status", this,
                customerInfo, ts, resultInfo).run();

        String getActualAction = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("addon")),
                "action");
        Assert.assertEquals(ts.getActionCode(), getActualAction);
        Jive.log("OK : Expected & Actual Action : " + getActualAction + " Matched Successfully!");

    }

}
