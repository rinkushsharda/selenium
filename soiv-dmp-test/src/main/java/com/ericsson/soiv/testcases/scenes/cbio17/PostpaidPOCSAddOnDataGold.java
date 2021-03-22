package com.ericsson.soiv.testcases.scenes.cbio17;

import com.ericsson.jive.core.execution.*;
import com.ericsson.soiv.fixtures.SoivFixture;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.CustomerInfo;
import com.ericsson.soiv.utils.TestResultInfo;
import com.ericsson.soiv.utils.TransactionSpecification;
import com.jayway.jsonpath.JsonPath;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Set;

import static com.ericsson.soiv.utils.JsonHelper.getRequestedJsonElement;
import static com.ericsson.soiv.utils.JsonHelper.searchKeyAndUpdateJson;
import static com.ericsson.soiv.utils.http.ReadBusinessData.readBusinessCacheData;

//Created By ZEAHDCE 30-April-2019

@Fixture(SoivFixture.class)
public class PostpaidPOCSAddOnDataGold extends SoivTestBase {
    private TestResultInfo resultInfo = new TestResultInfo();
    private CustomerInfo customerInfo = new CustomerInfo();
    private TransactionSpecification ts = new TransactionSpecification();

    @Test
    @Id("00000012")
    @Tags("OT-Regression, OT-Postpaid, EC-Controlled, OT-CBiO17")
    @Description("TS765 - postpaid PO CS AddOn Data gold")
    public void postpaidPoCsAddOnDataGold() throws IOException {

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
        ts.setRatingGroup(businessCache.get("USAGE.RATINGGROUP"));
        String usageCounterValueNew = businessCache.get("USAGE.COUNTERVALUE.NEW");
        String oneTimeChargeReplaceValue = businessCache.get("PERSONALIZE.ONETIME_CHARGE");

        LinkedHashMap<String, String> productOfferings = new LinkedHashMap<String, String>();
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

        /*****************************
         * onlineGYCallAndBalanceVerificationAfterCall
         **************************/

        ts.setExpectedCost(businessCache.get("USAGE.EXPECTEDCOST")); // This is only for the CS Charging Validation
        ts.setCharge(businessCache.get("USAGE.SETCHARGE")); // This will be used to validate getAccountBalance after
        // Charging
        onlineGYCallAndBalanceVerification();

        /*****************************
         * Usage Counter Adjustment
         **************************/

        Steps.PROTOCOLS.businessLogicCS
                .getThreshholdAndCounters("Get the Usage Counter information", this, customerInfo, ts, resultInfo)
                .run();

        int usageCounterId = JsonPath.read(ts.getResponseBody(),
                "$.usageCounterUsageThresholdInformation[0].usageCounterID");

        Integer productId = JsonPath.read(ts.getResponseBody(), "$.usageCounterUsageThresholdInformation[0].productID");

        customerInfo.setUsageCounterId(usageCounterId);
        customerInfo.setOfferProductID(productId);
        ts.setusageCounterValueNew(usageCounterValueNew);

        resultInfo = Steps.PROTOCOLS.businessLogicCS
                .usageCounterAdjustment("Update the Usage Counter", this, customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicCS.getThreshholdAndCounters("Validate the Usage Counter updated Value", this,
                customerInfo, ts, resultInfo).run();

        String usageCounterUpdatedValue = JsonPath.read(ts.getResponseBody(),
                "$.usageCounterUsageThresholdInformation[0].usageCounterValue");

        Assert.assertEquals(usageCounterValueNew, usageCounterUpdatedValue);

        /*****************************
         * Personalize Usage for Characteristics One Time Charge and Submit Cart
         *************************/

        ts.setJsonFileName("createShoppingCart.json");
        Steps.PROVISION.eoc.createShoppingCart("Provision : Create Shopping Cart for Personalize usage", this,
                customerInfo, ts, resultInfo).run();
        Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart", this, customerInfo, ts, resultInfo).run();
        String pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("addon")), "id");
        customerInfo.setPooiId(pooiId);

        Steps.PROTOCOLS.businessLogicEOC.getPooi("Getting the POOI ", this, customerInfo, ts, resultInfo).run();

        JSONObject j = oneTimeChargeChange(ts.getResponseBody(), "0.55", oneTimeChargeReplaceValue);

        ts.setActionCode(businessCache.get("PERSONALIZE.VALUE_MODIFY"));
        ts.setResponseBody(j.toString());

        JSONObject getAction = searchKeyAndUpdateJson(ts.getResponseBody(), businessCache.get("PERSONALIZE.KEY_ACTION"),
                ts.getActionCode());

        ts.setResponseBody(getAction.toString());

        Steps.PROTOCOLS.businessLogicEOC.updatePooi("Update For Personalization of the OneTimeCharge Characteristics ",
                this, customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC.submitShoppingCart("Provision : Submit Shopping Cart for Personalize Usage",
                this, customerInfo, ts, resultInfo).run();

        /******************
         * Verify the Personalize usage for Characteristics One Time Charge on SDP
         **********************/

        resultInfo = Steps.PROTOCOLS.businessLogicCS
                .getOffers("Get One time Charge from SDP ", this, customerInfo, ts, resultInfo).run();
        Double onetimeCharge = new Double(getOneTimeCharge(ts.getResponseBody())) / 100;

        Assert.assertEquals(oneTimeChargeReplaceValue, onetimeCharge.toString());

        /*****************************
         * onlineGYCallAndBalanceVerificationAfterCall
         **************************/

        ts.setCharge(businessCache.get("USAGE.SETCHARGE_AFTER_PERSONALIZE"));
        ts.setExpectedCost(businessCache.get("USAGE.EXPECTEDCOST_AFTER_PERSONALIZE")); // This is only for the CS
        // Charging Validation
        onlineGYCallAndBalanceVerification();

        /****************************
         * PO Termination with Update POII and submit the Cart
         ******************/

        ts.setJsonFileName("createShoppingCart.json");
        Steps.PROVISION.eoc.createShoppingCart("Provision : Create Shopping Cart for PO Termination", this,
                customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart", this, customerInfo, ts, resultInfo).run();

        pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("addon")), "id");
        customerInfo.setPooiId(pooiId);

        Steps.PROTOCOLS.businessLogicEOC.getPooi("Getting the POOI ", this, customerInfo, ts, resultInfo).run();

        ts.setActionCode(businessCache.get("ACTION.CODE_DELETE"));

        getAction = searchKeyAndUpdateJson(ts.getResponseBody(), "action", ts.getActionCode());

        ts.setResponseBody(getAction.toString());

        Steps.PROTOCOLS.businessLogicEOC.updatePooi("Update For DeActivation PO ", this, customerInfo, ts, resultInfo)
                .run();

        Steps.PROTOCOLS.businessLogicEOC.submitShoppingCart("Provision : Submit Shopping Cart for PO Deactivation",
                this, customerInfo, ts, resultInfo).run();

        /****************************
         * Verification of the PO Status after PO Termination in Get Shopping Cart
         ******************/

        /* asserting the action by comparing the status */
        Steps.PROTOCOLS.businessLogicEOC.getShoppingCart("Protocol : Get Shopping Cart for Checking PO Status", this,
                customerInfo, ts, resultInfo).run();

        String getActualAction = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("addon")),
                "action");
        Assert.assertEquals(ts.getActionCode(), getActualAction);
        Jive.log("OK : Expected & Actual Action : " + getActualAction + " Matched Successfully!");

        /****************************
         * Verification the charges in the Bill
         ******************/

        ts.setJsonFileName("sendBillGenerateRequest.xml");

        Steps.BILL_GENERATION.stepsBilling
                .sendInvoiceRequest("Protocol : Send Bill Process Create Request", this, resultInfo, customerInfo, ts)
                .run();

        resultInfo = Steps.BILL_GENERATION.stepsBilling
                .getInvoice("Validation : Get Invoice PDF and Validate GRAND TOTAL in PDF", this, resultInfo,
                        customerInfo, ts)
                .run();
        String readInvoiceData = (String) resultInfo.getResult();

        if (readInvoiceData.contains("GRAND TOTAL " + ts.getInvoiceCharge())) {
            Jive.log("OK : Invoice PDF Charges : " + ts.getInvoiceCharge() + " are Validated Successfully");
        } else {
            Jive.failAndContinue("FAILED : Invoice PDF Charges  : " + ts.getInvoiceCharge() + " Not Validated, Please Check the Invoice Content : "
                    + readInvoiceData);
        }

    }

    public static JSONObject oneTimeChargeChange(String json, String searchValue, String insertValue) {
        JSONObject jsonObject = new JSONObject(json);

        JSONObject product = jsonObject.getJSONObject("product"); // product json
        JSONArray productPrices = product.getJSONArray("productPrice"); // product price json
        int l = productPrices.length();
        for (int r = 0; r < l; r++) {
            JSONObject productPrice = productPrices.getJSONObject(r);

            JSONArray characteristics = productPrice.getJSONArray("characteristics");
            for (int i = 0; i < characteristics.length(); i++) {
                try {
                    if (characteristics.getJSONObject(i).get("value").equals(searchValue)) {
                        JSONObject characteristic = characteristics.getJSONObject(i);

                        Set<String> allKeys = characteristic.keySet();
                        LinkedHashMap linkedHashMap = new LinkedHashMap();
                        for (String a1 : allKeys) {
                            linkedHashMap.put(a1, characteristic.get(a1));
                        }
                        linkedHashMap.put("value", insertValue);
                        characteristics.put(i, linkedHashMap);
                        break;
                    }
                }catch (Exception e){
                    if (characteristics.getJSONObject(i).equals(searchValue)) {
                        JSONObject characteristic = characteristics.getJSONObject(i);

                        Set<String> allKeys = characteristic.keySet();
                        LinkedHashMap linkedHashMap = new LinkedHashMap();
                        for (String a1 : allKeys) {
                            linkedHashMap.put(a1, characteristic.get(a1));
                        }
                        linkedHashMap.put("value", insertValue);
                        characteristics.put(i, linkedHashMap);
                        break;
                    }
                }
            }
            productPrice.remove("characteristics");
            productPrice.put("characteristics", characteristics);
            productPrices.remove(r);
            productPrices.put(productPrice);
        }
        product.remove("productPrice");
        product.put("productPrice", productPrices);
        jsonObject.remove("product");
        jsonObject.put("product", product);
        return jsonObject;
    }

    public void onlineGYCallAndBalanceVerification() {
        Steps.PROTOCOLS.businessLogicCS.getAccountBalanceAndDate("Protocol : Before - Get Account Balance from CS",
                this, customerInfo, ts, resultInfo).run();
        BigDecimal getBalanceBefore = ts.getAccountBalance();

        Jive.log("Balance Before is " + getBalanceBefore);

        Steps.CHARGING_ONLINE.simulator
                .onlineGy("Protocol : Online Data Usage CIP IP Simulator", this, resultInfo, customerInfo, ts).run();

        Steps.PROTOCOLS.businessLogicCS.getAccountBalanceAndDate("Protocol : After - Get Account Balance from CS", this,
                customerInfo, ts, resultInfo).run();

        BigDecimal getBalanceAfter = ts.getAccountBalance();
        Jive.log("Balance Before : " + getBalanceBefore + " Balance After : " + getBalanceAfter);

        Assert.assertEquals(getBalanceBefore.subtract(new BigDecimal(ts.getCharge())), getBalanceAfter);
        Jive.log("OK : Expected Balance " + ts.getCharge() + "Matched Successfully!");
    }

    private String getOneTimeCharge(String response) {
        String treeParameterValueNumber = null;
        JSONArray treeParameterSetInformationList;
        try {
            treeParameterSetInformationList = new JSONObject(response).getJSONArray("offerInformation")
                    .getJSONObject(0).getJSONArray("treeParameterSetInformationList");
        }catch (Exception e) {
            treeParameterSetInformationList = new JSONObject(response).getJSONArray("offerInformation")
                    .getJSONObject(1).getJSONArray("treeParameterSetInformationList");
        }
            for (int i = 0; i < treeParameterSetInformationList.length(); i++) {
                JSONObject treeParameter = treeParameterSetInformationList.getJSONObject(i);
                JSONArray treeParameterInformation = treeParameter.getJSONArray("treeParameterInformation");
                for (int j = 0; j < treeParameterInformation.length(); j++) {
                    JSONObject treeParameterInfo = treeParameterInformation.getJSONObject(j);
                    if ("OneTimeCharge".equals(treeParameterInfo.get("treeParameterName"))) {
                        JSONObject treeParameterValueDecimal = treeParameterInfo.getJSONObject("treeParameterValueDecimal");
                        treeParameterValueNumber = treeParameterValueDecimal.get("treeParameterValueNumber").toString();
                        break;
                    }
                }
            }
        return treeParameterValueNumber;
    }

}