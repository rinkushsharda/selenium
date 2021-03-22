package com.ericsson.soiv.testcases.scenes.cbio17;

import com.ericsson.jive.core.execution.*;
import com.ericsson.soiv.fixtures.SoivFixture;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.Constants;
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

//Created By ZEAHDCE 09-May-2019
@Fixture(SoivFixture.class)
public class PostpaidPOCSOptionalVoiceSMSOnlineFAF extends SoivTestBase {
    private TestResultInfo resultInfo = new TestResultInfo();
    private CustomerInfo customerInfo = new CustomerInfo();
    private TransactionSpecification ts = new TransactionSpecification();

    @Test
    @Id("00000021")
    @Tags("OT-Regression, OT-Postpaid, EC-Controlled, OT-CBiO17")
    @Description("TS754 - Postpaid_PO_CS_Optional_Voice_SMS_Online_FAF")
    public void postpaidPOCSOptionalVoiceSMSOnlineFAF() throws IOException {

        LinkedHashMap<String, String> businessCache = new LinkedHashMap<>(
                readBusinessCacheData(this.getTestCase().getId()));
        customerInfo.setCustomerBillCycle(businessCache.get("CUSTOMER.BILLCYCLE"));
        customerInfo.setOrderMode(businessCache.get("ORDER.MODE"));
        customerInfo.setRequester(businessCache.get("ORDER.REQUESTER"));

        ts.setInvoiceCharge(businessCache.get("INVOICE.CHARGE")); // This Charges will be used to validate Invoice PDF
                                                                  // Charges
        ts.setInvoicePdfPageNo(Integer.parseInt(businessCache.get("INVOICE.PAGENO")));
        ts.setRemoveInvoicePdfFile(Boolean.parseBoolean(businessCache.get("INVOICE.REMOVE"))); // If We Wants to keep
                                                                                               // Invoice then Set this
                                                                                               // Flag as FALSE

        LinkedHashMap productOfferings = new LinkedHashMap();
        productOfferings.put("basic", businessCache.get("PO.ID_BASIC"));
        productOfferings.put("optional", businessCache.get("PO.ID_OPTIONAL"));

        ts.setProductOfferingIds(productOfferings);

        /******************************
         * createCustomerContractBasicPO
         ***************************/

        customerInfo.setNpCodePub(businessCache.get("MSISDN.NPCODEPUB"));
        customerInfo.setPlCodePub(businessCache.get("MSISDN.PLCODEPUB"));
        customerInfo.setSubmIdPub(businessCache.get("MSISDN.SUBMIDPUB"));
        customerInfo.setHlCode(businessCache.get("MSISDN.HLCODE"));
        customerInfo.setRsCode(businessCache.get("MSISDN.RSCODE"));
        customerInfo.setCsControlled(businessCache.get("MSISDN.CSCONTROLLED"));

        Steps.PROVISION.eoc.createCustomerContractBasicPO("Provision : Create Customer and Contract with Basic PO",
                this, customerInfo, ts, resultInfo).run();

        /******************************
         * Add Optional PO and Submit Cart
         ***************************/

        ts.setJsonFileName("AddOptionalPOonBasic.json");
        Steps.PROTOCOLS.businessLogicEOC.addOptionalPOonBasicPO("Provision : Add Optional PO in to Basic PO", this,
                customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision : Submit Shopping Cart", this, customerInfo, ts, resultInfo).run();

        /**************
         * Personalize Usage for Characteristics MSISDNListSMS and MSISDNListVoice by
         * adding two numbers
         *************************/

        ts.setJsonFileName("createShoppingCart.json");
        Steps.PROVISION.eoc.createShoppingCart("Provision : Create Shopping Cart for Personalize usage", this,
                customerInfo, ts, resultInfo).run();
        Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart", this, customerInfo, ts, resultInfo).run();
        String pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("optional")), "id");
        customerInfo.setPooiId(pooiId);

        Steps.PROTOCOLS.businessLogicEOC.getPooi("Getting the POOI ", this, customerInfo, ts, resultInfo).run();

        JSONArray jSONArray = new JSONArray();
        jSONArray.put(0, Constants.NATIONAL_NUMBER_2);
        jSONArray.put(1, Constants.NATIONAL_NUMBER_3);

        JSONObject jSONObject = updateProductCharacteristics(ts.getResponseBody(),
                businessCache.get("PERSONALIZE.MSISDNListSMS"), jSONArray);
        jSONObject = updateProductCharacteristics(jSONObject.toString(),
                businessCache.get("PERSONALIZE.MSISDNListVoice"), jSONArray);

        ts.setActionCode(businessCache.get("PERSONALIZE.VALUE_MODIFY"));
        ts.setResponseBody(jSONObject.toString());

        JSONObject getAction = searchKeyAndUpdateJson(ts.getResponseBody(), businessCache.get("PERSONALIZE.KEY_ACTION"),
                ts.getActionCode());

        ts.setResponseBody(getAction.toString());

        Steps.PROTOCOLS.businessLogicEOC.updatePooi(
                "Update For Personalization of the MSISDNListSMS and MSISDNListVoice Characteristics" + " by adding "
                        + Constants.NATIONAL_NUMBER_2 + "and " + Constants.NATIONAL_NUMBER_3,
                this, customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC.submitShoppingCart("Provision : Submit Shopping Cart for Personalize Usage",
                this, customerInfo, ts, resultInfo).run();

        /*****
         * Run SMS and Voice Usage for National Number after personalization
         ****************/

        ts.setCharge(businessCache.get("USAGE.SETCHARGE_AFTER_PERSONALIZE")); // This will be used to validate
                                                                              // getAccountBalance after Charging
        ts.setCalledNumber(Constants.NATIONAL_NUMBER_3);
        ts.setDuration(Integer.parseInt(businessCache.get("USAGE.SETDURATION")));

        runUsageAndCheckBalance(ts, "National", "0", "0");

        /*****************************
         * Verification the charges in the Bill
         ******************/

        ts.setJsonFileName("sendBillGenerateRequest.xml");
        Steps.BILL_GENERATION.stepsBilling
                .sendInvoiceRequest("Protocol : Send Bill Process Create Request", this, resultInfo, customerInfo, ts)
                .run();

        resultInfo = Steps.BILL_GENERATION.stepsBilling
                .getInvoice("Validation : Get Invoice PDF and Validate PDF", this, resultInfo, customerInfo, ts).run();
        String readInvoiceData = (String) resultInfo.getResult();

        if (readInvoiceData.contains("GRAND TOTAL " + ts.getInvoiceCharge())) {
            Jive.log("OK : Invoice PDF Charges : " + ts.getInvoiceCharge() + " are Validated Successfully");
        } else {
           Jive.failAndContinue("FAILED : Invoice PDF Charges : " + ts.getInvoiceCharge() + " are Not Validated Successfully!! Please Check the Invoice Content : "
                    + readInvoiceData);
        }

        /**********
         * Personalize Usage for Characteristics MSISDNListSMS and MSISDNListVoice by
         * removing existing number and adding a new number
         ***********/

        ts.setJsonFileName("createShoppingCart.json");
        Steps.PROVISION.eoc.createShoppingCart("Provision : Create Shopping Cart for Personalize usage", this,
                customerInfo, ts, resultInfo).run();
        Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart", this, customerInfo, ts, resultInfo).run();
        pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("optional")), "id");
        customerInfo.setPooiId(pooiId);

        Steps.PROTOCOLS.businessLogicEOC.getPooi("Getting the POOI ", this, customerInfo, ts, resultInfo).run();

        jSONArray.put(0, Constants.NATIONAL_NUMBER_2);
        jSONArray.put(1, Constants.NATIONAL_NUMBER_4);

        JSONObject j1 = updateProductCharacteristics(ts.getResponseBody(),
                businessCache.get("PERSONALIZE.MSISDNListSMS"), jSONArray);
        j1 = updateProductCharacteristics(j1.toString(), businessCache.get("PERSONALIZE.MSISDNListVoice"), jSONArray);

        ts.setActionCode(businessCache.get("PERSONALIZE.VALUE_MODIFY"));
        ts.setResponseBody(j1.toString());

        jSONObject = searchKeyAndUpdateJson(ts.getResponseBody(), businessCache.get("PERSONALIZE.KEY_ACTION"),
                ts.getActionCode());

        ts.setResponseBody(jSONObject.toString());

        Steps.PROTOCOLS.businessLogicEOC.updatePooi(
                "Update For Personalization for Characteristics  MSISDNListSMS and MSISDNListVoice by by removing existing number "
                        + Constants.NATIONAL_NUMBER_2 + " and adding new number " + Constants.NATIONAL_NUMBER_4,
                this, customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC.submitShoppingCart("Provision : Submit Shopping Cart for Personalize Usage",
                this, customerInfo, ts, resultInfo).run();

        // Run SMS and Voice Usage for National Number
        ts.setCharge(businessCache.get("USAGE.SETCHARGE_AFTER_PERSONALIZE")); // This will be used to validate
                                                                              // getAccountBalance after Charging
        ts.setCalledNumber(Constants.NATIONAL_NUMBER_4);
        ts.setDuration(Integer.parseInt(businessCache.get("USAGE.SETDURATION")));

        runUsageAndCheckBalance(ts, "National", "0", "0");

        /**** Deactivated the PO and Validated the PO Status ******/

        ts.setActionCode(businessCache.get("ACTION.CODE_DEACTIVATE"));
        ts.setReasonCode(businessCache.get("REASON.CODE_DUNND"));
        Steps.PROVISION.eoc.updateOptionalPO("DeActivate Free Voice Optional", this, customerInfo, ts, resultInfo)
                .run();

        /**** Reactivated the PO and Validated the PO Status ******/
        ts.setActionCode(businessCache.get("ACTION.CODE_REACTIVATE"));
        ts.setReasonCode(businessCache.get("REASON.CODE_DUNND"));

        Steps.PROVISION.eoc.updateOptionalPO("Reactivate Free Voice Optional", this, customerInfo, ts, resultInfo)
                .run();

    }

    // Method to update Product Characteristics MSISDNListSMS and MSISDNListVoice
    public JSONObject updateProductCharacteristics(String json, String searchValue, JSONArray insert) {

        JSONObject jsonObject = new JSONObject(json);
        JSONObject product = jsonObject.getJSONObject("product"); // product
        JSONArray productCharacteristics = product.getJSONArray("productCharacteristics"); // product characteristics

        for (int i = 0; i < productCharacteristics.length(); i++) {

            if (productCharacteristics.getJSONObject(i).getString("name").equals(searchValue)) {
                productCharacteristics.getJSONObject(i).remove("value");
                productCharacteristics.getJSONObject(i).put("value", insert);
            }
        }

        product.put("productCharacteristics", productCharacteristics);
        jsonObject.put("product", product);
        return jsonObject;
    }

    private void runUsageAndCheckBalance(TransactionSpecification ts, String message, String voiceExpectedCost,
            String smsExpectedCost) {

        Steps.PROTOCOLS.businessLogicCS.getAccountBalanceAndDate(
                "Protocol : Before Usage - Get Account Balance from CS", this, customerInfo, ts, resultInfo).run();

        BigDecimal getBalanceBefore = ts.getAccountBalance();

        ts.setExpectedCost(voiceExpectedCost);
        Steps.CHARGING_ONLINE.simulator.onlineCip("Protocol : " + message + " - Usage Online Charging CIP IP Simulator",
                this, resultInfo, customerInfo, ts).run();

        ts.setExpectedCost(smsExpectedCost);
        Steps.CHARGING_ONLINE.simulator
                .onlineSms("Protocol : " + message + " - Usage Online SMS", this, resultInfo, customerInfo, ts).run();

        Steps.PROTOCOLS.businessLogicCS.getAccountBalanceAndDate("Protocol : After Usage - Get Account Balance from CS",
                this, customerInfo, ts, resultInfo).run();

        BigDecimal getBalanceAfter = ts.getAccountBalance();
        Jive.log("Balance Before : " + getBalanceBefore + " Balance After : " + getBalanceAfter);

        Assert.assertEquals(getBalanceBefore.subtract(new BigDecimal(ts.getCharge())), getBalanceAfter);
        Jive.log("OK : Expected Charges " + ts.getCharge() + " Deducted Successfully!");
    }
}