package com.ericsson.soiv.testcases.scenes.cbio17;

import com.ericsson.jive.core.execution.*;
import com.ericsson.soiv.fixtures.SoivFixture;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.CustomerInfo;
import com.ericsson.soiv.utils.TestResultInfo;
import com.ericsson.soiv.utils.TransactionSpecification;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedHashMap;

import static com.ericsson.soiv.utils.JsonHelper.getRequestedJsonElement;
import static com.ericsson.soiv.utils.JsonHelper.searchKeyAndUpdateJson;
import static com.ericsson.soiv.utils.http.ReadBusinessData.readBusinessCacheData;

// EMODRED Created on 29-April-2019

@Fixture(SoivFixture.class)
public class PrepaidPOCSOptionalData extends SoivTestBase {
    private TestResultInfo resultInfo = new TestResultInfo();
    private CustomerInfo customerInfo = new CustomerInfo();
    private TransactionSpecification ts = new TransactionSpecification();

    @Test
    @Id("00000004")
    @Tags("OT-Regression, OT-Prepaid, EC-Controlled, OT-CBiO17")
    @Description("TS704_Prepaid_PO_CS_Optional_Data")
    public void prepaidPoCsOptionalData() throws IOException {

        LinkedHashMap<String, String> businessCache = new LinkedHashMap<>(
                readBusinessCacheData(this.getTestCase().getId()));

        customerInfo.setCustomerBillCycle(businessCache.get("CUSTOMER.BILLCYCLE"));
        customerInfo.setOrderMode(businessCache.get("ORDER.MODE"));
        customerInfo.setRequester(businessCache.get("ORDER.REQUESTER"));

        LinkedHashMap<String, String> productOfferings = new LinkedHashMap<String, String>();
        productOfferings.put("basic", businessCache.get("PO.ID_BASIC"));
        productOfferings.put("optional", businessCache.get("PO.ID_OPTIONAL"));
        ts.setProductOfferingIds(productOfferings);

        customerInfo.setNpCodePub(businessCache.get("MSISDN.NPCODEPUB"));
        customerInfo.setPlCodePub(businessCache.get("MSISDN.PLCODEPUB"));
        customerInfo.setSubmIdPub(businessCache.get("MSISDN.SUBMIDPUB"));
        customerInfo.setHlCode(businessCache.get("MSISDN.HLCODE"));
        customerInfo.setRsCode(businessCache.get("MSISDN.RSCODE"));
        customerInfo.setCsControlled(businessCache.get("MSISDN.CSCONTROLLED"));

        Steps.PROVISION.eoc.createCustomerContractBasicPO("Create Customer and Contract with Basic PO", this,
                customerInfo, ts, resultInfo).run();

        ts.setJsonFileName("AddOptionalPOonBasic.json");
        Steps.PROTOCOLS.businessLogicEOC.addOptionalPOonBasicPO("Provision : Add Optional PO in to Basic PO", this,
                customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision : Submit Shopping Cart", this, customerInfo, ts, resultInfo).run();

        /* for Policy Verificaton */

        resultInfo = Steps.CHARGING_ONLINE.simulator
                .onlineGx("Protocol : Online Gx get policy from simulator", this, resultInfo, customerInfo, ts).run();

        String policyValue = resultInfo.getResult().toString();
        Assert.assertTrue(
                policyValue.contains("\"Max-Requested-Bandwidth-DL\":" + businessCache.get("BANDWIDTH.DL_INITIAL")
                        + ",\"Max-Requested-Bandwidth-UL\":" + businessCache.get("BANDWIDTH.UL_INITIAL")));

        /* Run Data call and verify the balance */

        ts.setExpectedCost(businessCache.get("USAGE.EXPECTEDCOST_1")); // This is only for the CS Charging Validation
        ts.setCharge(businessCache.get("USAGE.SETCHARGE_1")); // This will be used to validate getAccountBalance after
                                                              // Charging
        ts.setRatingGroup(businessCache.get("USAGE.RATING_GROUP_1"));

        runDataUsageAndCheckBalance(ts);

        /* =================For Personalized usage step================= */

        ts.setJsonFileName("createShoppingCart.json");
        Steps.PROVISION.eoc.createShoppingCart("Provision : Create Shopping Cart For Personalized usage", this,
                customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC.getShoppingCart("Protocol : Get Shopping Cart For Personalized usage", this,
                customerInfo, ts, resultInfo).run();

        String pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("optional")), "id");
        customerInfo.setPooiId(pooiId);

        Steps.PROTOCOLS.businessLogicEOC
                .getPooi("Getting the POOI For Personalized usage", this, customerInfo, ts, resultInfo).run();

        /* updating the values in POOI */

        JSONObject getPOOIRequestBody = searchKeyAndUpdateJson(ts.getResponseBody(),
                businessCache.get("PERSONALIZE_ACTION_KEY"), businessCache.get("PERSONALIZE_ACTION_VALUE"));
        getPOOIRequestBody = updateProductCharacteristics(getPOOIRequestBody.toString(), "DQoS",
                Integer.parseInt(businessCache.get("PERSONALIZE.DQOS_VALUE")));
        getPOOIRequestBody = updateProductCharacteristics(getPOOIRequestBody.toString(), "DQoSI",
                Integer.parseInt(businessCache.get("PERSONALIZE.DQOSI_VALUE")));
        ts.setResponseBody(getPOOIRequestBody.toString());

        /* Update POOI Request with submitting the shopping cart */

        Steps.PROTOCOLS.businessLogicEOC.updatePooi("Updation of Quality of Service Values for Personalization", this,
                customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC.submitShoppingCart("Provision : Submit Shopping Cart For Personalized usage",
                this, customerInfo, ts, resultInfo).run();

        /* for Policy Verificaton */

        resultInfo = Steps.CHARGING_ONLINE.simulator
                .onlineGx("Protocol : Online Gx get policy from simulator", this, resultInfo, customerInfo, ts).run();
        policyValue = resultInfo.getResult().toString();
        Assert.assertTrue(
                policyValue.contains("\"Max-Requested-Bandwidth-DL\":" + businessCache.get("BANDWIDTH.DL_PERSONALIZE")
                        + ",\"Max-Requested-Bandwidth-UL\":" + businessCache.get("BANDWIDTH.UL_PERSONALIZE")));

        /* Run Data call and verify the balance */

        ts.setExpectedCost(businessCache.get("USAGE.EXPECTEDCOST_2")); // This is only for the CS Charging Validation
        ts.setCharge(businessCache.get("USAGE.SETCHARGE_2")); // This will be used to validate getAccountBalance after
                                                              // Charging
        ts.setRatingGroup(businessCache.get("USAGE.RATING_GROUP_2"));

        runDataUsageAndCheckBalance(ts);

        /* =================For De-Personalized usage step================= */

        ts.setJsonFileName("createShoppingCart.json");
        Steps.PROVISION.eoc.createShoppingCart("Provision : Create Shopping Cart For De-Personalized usage", this,
                customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC.getShoppingCart("Protocol : Get Shopping Cart For De-Personalized usage", this,
                customerInfo, ts, resultInfo).run();

        pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("optional")), "id");
        customerInfo.setPooiId(pooiId);

        Steps.PROTOCOLS.businessLogicEOC
                .getPooi("Getting the POOI For De-Personalized usage", this, customerInfo, ts, resultInfo).run();

        // * updating the values in POOI */

        getPOOIRequestBody = searchKeyAndUpdateJson(ts.getResponseBody(), businessCache.get("DEPERSONALIZE_ACTION_KEY"),
                businessCache.get("DEPERSONALIZE_ACTION_VALUE"));
        getPOOIRequestBody = updateProductCharacteristics(getPOOIRequestBody.toString(), "DQoS",
                Integer.parseInt(businessCache.get("DEPERSONALIZE.DQOS_VALUE")));
        getPOOIRequestBody = updateProductCharacteristics(getPOOIRequestBody.toString(), "DQoSI",
                Integer.parseInt(businessCache.get("DEPERSONALIZE.DQOS_VALUE")));
        ts.setResponseBody(getPOOIRequestBody.toString());

        /* Update POOI Request with submitting the shopping cart */

        Steps.PROTOCOLS.businessLogicEOC.updatePooi("Updation of Quality of Service Values for De-Personalization ",
                this, customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC.submitShoppingCart(
                "Provision : Submit Shopping Cart For De-Personalized usage", this, customerInfo, ts, resultInfo).run();

        /* for Policy Verifications */

        resultInfo = Steps.CHARGING_ONLINE.simulator
                .onlineGx("Protocol : Online Gx get policy from simulator", this, resultInfo, customerInfo, ts).run();
        policyValue = resultInfo.getResult().toString();
        Assert.assertTrue(
                policyValue.contains("\"Max-Requested-Bandwidth-DL\":" + businessCache.get("BANDWIDTH.DL_DEPERSONALIZE")
                        + ",\"Max-Requested-Bandwidth-UL\":" + businessCache.get("BANDWIDTH.UL_DEPERSONALIZE")));

        /* ===================Step to terminate the PO============================ */

        ts.setActionCode("Deactivate");
        ts.setReasonCode("DUNND");
        Steps.PROVISION.eoc.updateOptionalPO("DeActivate Optional PO From Contract", this, customerInfo, ts, resultInfo)
                .run();
    }

    private void runDataUsageAndCheckBalance(TransactionSpecification ts) {

        Steps.PROTOCOLS.businessLogicCS.getAccountBalanceAndDate("Protocol : Before - Get Account Balance from CS",
                this, customerInfo, ts, resultInfo).run();
        BigDecimal getBalanceBefore = ts.getAccountBalance();
        Steps.CHARGING_ONLINE.simulator
                .onlineGy("Protocol : Online Data Usage CIP IP Simulator", this, resultInfo, customerInfo, ts).run();
        Steps.PROTOCOLS.businessLogicCS.getAccountBalanceAndDate("Protocol : After - Get Account Balance from CS", this,
                customerInfo, ts, resultInfo).run();
        BigDecimal getBalanceAfter = ts.getAccountBalance();
        Jive.log("Balance Before : " + getBalanceBefore + " Balance After : " + getBalanceAfter);
        Assert.assertEquals(getBalanceBefore.subtract(new BigDecimal(ts.getCharge())), getBalanceAfter);
        Jive.log("OK : Expected Balance " + ts.getCharge() + "Matched Successfully!");

    }

    public JSONObject updateProductCharacteristics(String json, String entityToUpdate, int a) {
        JSONObject jsonObject = new JSONObject(json);
        JSONObject product = jsonObject.getJSONObject("product"); // product
        JSONArray productCharacteristics = product.getJSONArray("productCharacteristics"); // product characteristics

        for (int i = 0; i < productCharacteristics.length(); i++) {

            if (productCharacteristics.getJSONObject(i).getString("name").equals(entityToUpdate)) {
                productCharacteristics.getJSONObject(i).remove("value");
                productCharacteristics.getJSONObject(i).put("value", a);
            }
        }

        product.put("productCharacteristics", productCharacteristics);
        jsonObject.put("product", product);
        return jsonObject;
    }
}
