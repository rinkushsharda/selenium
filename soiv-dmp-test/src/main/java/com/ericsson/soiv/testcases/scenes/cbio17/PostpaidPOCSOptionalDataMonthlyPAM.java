package com.ericsson.soiv.testcases.scenes.cbio17;

import static com.ericsson.soiv.utils.JsonHelper.getRequestedJsonElement;
import static com.ericsson.soiv.utils.JsonHelper.searchKeyAndUpdateJson;
import static com.ericsson.soiv.utils.JsonHelper.searchPamServiceId;
import static com.ericsson.soiv.utils.http.ReadBusinessData.readBusinessCacheData;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import org.json.JSONArray;
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
import com.ericsson.soiv.utils.JsonHelper;
import com.ericsson.soiv.utils.TestResultInfo;
import com.ericsson.soiv.utils.TransactionSpecification;
import com.jayway.jsonpath.JsonPath;
import static com.ericsson.soiv.utils.HelperClass.getNumberOfMatchingStringCount;

//Created By ZEAHDCE 10-May-2019
@Fixture(SoivFixture.class)
public class PostpaidPOCSOptionalDataMonthlyPAM extends SoivTestBase {
    private TestResultInfo resultInfo = new TestResultInfo();
    private CustomerInfo customerInfo = new CustomerInfo();
    private TransactionSpecification ts = new TransactionSpecification();

    @Test
    @Id("00000023")
    @Tags("OT-Regression, OT-Postpaid, EC-Controlled, OT-CBiO17")
    @Description("TS761_Postpaid_PO_CS_Optional_Data_Monthly_PAM")
    public void postpaidPOCSOptionalDataMonthlyPAM() throws InterruptedException, IOException {

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

        LinkedHashMap<String, String> productOfferings = new LinkedHashMap<String, String>();
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

        Steps.PROVISION.eoc.createCustomerContractBasicPO("Create Customer and Contract with Basic PO", this,
                customerInfo, ts, resultInfo).run();

        /******************************
         * Add optional PO and Submit the Cart
         **************************/

        ts.setJsonFileName("AddOptionalPOonBasic.json");
        Steps.PROTOCOLS.businessLogicEOC.addOptionalPOonBasicPO("Provision : Add Optional PO in to Basic PO", this,
                customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision : Submit Shopping Cart", this, customerInfo, ts, resultInfo).run();

        /******************************
         * Personalize Characteristics PTL and PTLI,DataLimitInByte and Submit Cart
         *************************/

        ts.setJsonFileName("createShoppingCart.json");
        Steps.PROVISION.eoc.createShoppingCart("Provision : Create Shopping Cart for Personalize usage", this,
                customerInfo, ts, resultInfo).run();
        Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart", this, customerInfo, ts, resultInfo).run();
        String pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("optional")), "id");
        customerInfo.setPooiId(pooiId);

        Steps.PROTOCOLS.businessLogicEOC.getPooi("Getting the POOI ", this, customerInfo, ts, resultInfo).run();

        JSONObject body = updateProductCharacteristics(ts.getResponseBody(), businessCache.get("PERSONALIZE.KEY1"),
                businessCache.get("PERSONALIZE.PTL"));
        body = updateProductCharacteristics(body.toString(), businessCache.get("PERSONALIZE.KEY2"),
                businessCache.get("PERSONALIZE.PTL1"));
        body = JsonHelper.requestForPersonalizedPrice(body.toString(), businessCache.get("PERSONALIZE.KEY3"),
                businessCache.get("PERSONALIZE.DataLimitInByte"));

        ts.setActionCode(businessCache.get("PERSONALIZE.VALUE_MODIFY"));

        JSONObject getAction = searchKeyAndUpdateJson(body.toString(), businessCache.get("PERSONALIZE.KEY_ACTION"),
                ts.getActionCode());

        ts.setResponseBody(getAction.toString());

        Steps.PROTOCOLS.businessLogicEOC
                .updatePooi("Update POOI For Modification of Characteristics   ", this, customerInfo, ts, resultInfo)
                .run();

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision : Submit Shopping Cart after Characteristics modification ", this,
                        customerInfo, ts, resultInfo)
                .run();

        /******************************
         * Online Gx get policy from simulator before call and Validate UL and DL
         *************************/

        onlineGxAndULDLVerification("\"Max-Requested-Bandwidth-DL\":50000000,\"Max-Requested-Bandwidth-UL\":20000000");

        /***********
         * onlineGYCallAndBalanceVerificationAfterCall And Online Gx get policy from
         * simulator after call and Validate UL and DL
         **************************/

        ts.setCharge(businessCache.get("USAGE.SETCHARGE"));
        ts.setExpectedCost(businessCache.get("USAGE.EXPECTEDCOST")); // This is only for the CS Charging Validation
        onlineGYCallAndBalanceVerification();
        onlineGxAndULDLVerification("\"Max-Requested-Bandwidth-DL\":12000000,\"Max-Requested-Bandwidth-UL\":5000000");

        /******************************
         * Run PAM So it Reset the Usage Counter
         **************************/

        Steps.PROTOCOLS.businessLogicCS.getAccountDetails("Get PAM Service ID", this, customerInfo, ts, resultInfo)
                .run();

        int pamServiceID = searchPamServiceId(ts.getResponseBody(), 4);

        customerInfo.setPamServiceId(pamServiceID);

        Steps.PROTOCOLS.businessLogicCS.runPam("Run PAM and Reset Usage Counter", this, customerInfo, ts, resultInfo)
                .run();

        /*****************************
         * Run Online Gy and get Usage Counter and update the usage counter and validate
         * the update usage counter
         **************************/

        ts.setExpectedCost(businessCache.get("USAGE.EXPECTEDCOST")); // This is only for the CS Charging Validation
        Steps.CHARGING_ONLINE.simulator
                .onlineGy("Protocol : Online Data Usage CIP IP Simulator", this, resultInfo, customerInfo, ts).run();

        Steps.PROTOCOLS.businessLogicCS.getThreshholdAndCounters("Validate the Usage Counter After Data Usage ", this,
                customerInfo, ts, resultInfo).run();

        int usageCounterId = JsonPath.read(ts.getResponseBody(),
                "$.usageCounterUsageThresholdInformation[0].usageCounterID");

        customerInfo.setUsageCounterId(usageCounterId);

        ts.setusageCounterValueNew(usageCounterValueNew);
        ts.setRequestedUri("/csadmin/request_handler.php?request=UsageCounters%2FUpdateUsageCounters&subscriberNumber="
                + customerInfo.getMsisdn() + "&transactionCurrency=&updateUsageCounterForMultiUser=&usageCounterID="
                + customerInfo.getUsageCounterId() + "&usageCounterValueNew=" + ts.getusageCounterValueNew()
                + "&adjustmentUsageCounterValueRelative=&usageCounterMonetaryValueNew=&adjustmentUsageCounterMonetaryValueRelative=&associatedPartyID=&productID=&action=Send&returnFormat=json");

        resultInfo = Steps.PROTOCOLS.businessLogicCS
                .usageCounterAdjustment("Update the Usage Counter", this, customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicCS.getThreshholdAndCounters("Validate the Usage Counter updated Value", this,
                customerInfo, ts, resultInfo).run();

        String usageCounterUpdatedValue = JsonPath.read(ts.getResponseBody(),
                "$.usageCounterUsageThresholdInformation[0].usageCounterValue");

        Assert.assertEquals(usageCounterValueNew, usageCounterUpdatedValue);

        /*****************************
         * Verification the charges in the Bill
         ******************/

        verifyBill();

        /******************
         * onlineGxforULandDLVerification before Call
         **************************/

        onlineGxAndULDLVerification("\"Max-Requested-Bandwidth-DL\":50000000,\"Max-Requested-Bandwidth-UL\":20000000");

        /******************************
         * Personalize PTL and PTLI,DataLimitInByte and Submit Cart
         *************************/

        ts.setJsonFileName("createShoppingCart.json");
        Steps.PROVISION.eoc.createShoppingCart("Provision : Create Shopping Cart for Personalize usage", this,
                customerInfo, ts, resultInfo).run();
        Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart", this, customerInfo, ts, resultInfo).run();
        pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("optional")), "id");
        customerInfo.setPooiId(pooiId);

        Steps.PROTOCOLS.businessLogicEOC.getPooi("Getting the POOI ", this, customerInfo, ts, resultInfo).run();

        body = updateProductCharacteristics(ts.getResponseBody(), businessCache.get("PERSONALIZE.KEY1"),
                businessCache.get("DEPERSONALIZE.PTL"));
        body = updateProductCharacteristics(body.toString(), businessCache.get("PERSONALIZE.KEY2"),
                businessCache.get("DEPERSONALIZE.PTL1"));

        body = JsonHelper.requestForPersonalizedPrice(body.toString(), businessCache.get("PERSONALIZE.KEY3"),
                businessCache.get("DEPERSONALIZE.DataLimitInByte"));

        ts.setActionCode(businessCache.get("PERSONALIZE.VALUE_MODIFY"));

        getAction = searchKeyAndUpdateJson(body.toString(), businessCache.get("PERSONALIZE.KEY_ACTION"),
                ts.getActionCode());

        ts.setResponseBody(getAction.toString());

        Steps.PROTOCOLS.businessLogicEOC
                .updatePooi("Update POOI For Modification of Characteristics", this, customerInfo, ts, resultInfo)
                .run();

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision : Submit Shopping Cart after Characteristics modification", this,
                        customerInfo, ts, resultInfo)
                .run();

        /***********
         * onlineGYCallAndBalanceVerificationAfterCall And Online Gx get policy from
         * simulator after call and Validate UL and DL
         **************************/

        ts.setExpectedCost(businessCache.get("USAGE.EXPECTEDCOST"));
        onlineGYCallAndBalanceVerification();
        onlineGxAndULDLVerification("\"Max-Requested-Bandwidth-DL\":12000000,\"Max-Requested-Bandwidth-UL\":5000000");

        /**** Deactivated the PO and Validated the PO Status ******/

        ts.setActionCode(businessCache.get("ACTION.CODE_DEACTIVATE"));
        ts.setReasonCode(businessCache.get("REASON.CODE_DUNND"));
        Steps.PROVISION.eoc
                .updateOptionalPO("Update POOI for DeActivate of Optional PO ", this, customerInfo, ts, resultInfo)
                .run();

        /**** Reactivated the PO and Validated the PO Status ******/

        ts.setActionCode(businessCache.get("ACTION.CODE_REACTIVATE"));
        ts.setReasonCode(businessCache.get("REASON.CODE_DUNND"));
        Steps.PROVISION.eoc
                .updateOptionalPO("Update POOI for ReActivate of Optional PO", this, customerInfo, ts, resultInfo)
                .run();

        /* ===================Step to Re Purchase the PO============================ */

        /**** Deactivated the PO and Validated the PO Status ******/
        ts.setActionCode(businessCache.get("ACTION.CODE_DEACTIVATE"));
        ts.setReasonCode(businessCache.get("REASON.CODE_DUNND"));

        ts.setProductOfferingIds(productOfferings);
        Steps.PROVISION.eoc.updateOptionalPO("Update POOI for DeActivate of Optional PO for repurchase", this,
                customerInfo, ts, resultInfo).run();

        Steps.PROVISION.eoc.repurchaseOptionalPO("re purchase optional PO", this, customerInfo, ts, resultInfo).run();

        /***********
         * onlineGYCallAndBalanceVerificationAfterCall
         ******************************************/

        ts.setExpectedCost(businessCache.get("USAGE.EXPECTEDCOST"));
        onlineGYCallAndBalanceVerification();

        ts.setActionCode(businessCache.get("ACTION.CODE_DEACTIVATE"));
        ts.setReasonCode(businessCache.get("REASON.CODE_DUNND"));
        ts.setProductOfferingIds(productOfferings);
        Steps.PROVISION.eoc.updateOptionalPO("Update POOI for DeActivate of Optional PO for repurchase again", this,
                customerInfo, ts, resultInfo).run();

        Steps.PROVISION.eoc
                .repurchaseOptionalPO("assign again the same optional PO", this, customerInfo, ts, resultInfo).run();

        /***********
         * onlineGYCallAndBalanceVerificationAfterCall
         ******************************************/
        ts.setExpectedCost(businessCache.get("USAGE.EXPECTEDCOST"));
        onlineGYCallAndBalanceVerification();

        /*****************************
         * Verification the charges in the Bill
         ******************/
        Thread.sleep(Long.valueOf(businessCache.get("THREAD.SLEEP_INVOICE")));
        ts.setJsonFileName("sendBillGenerateRequest.xml");

        Steps.BILL_GENERATION.stepsBilling
                .sendInvoiceRequest("Protocol : Send Bill Process Create Request", this, resultInfo, customerInfo, ts)
                .run();

        resultInfo = Steps.BILL_GENERATION.stepsBilling
                .getInvoice("Validation : Get Invoice PDF and Validate Optional PO name Occurrence", this, resultInfo,
                        customerInfo, ts)
                .run();
        String readInvoiceData = (String) resultInfo.getResult();

        int getCount = getNumberOfMatchingStringCount("Basic Data PO_CS_OptionalDataMonthlyPAM_O1", readInvoiceData);
        Assert.assertEquals(3, getCount);
        Jive.log("OK : Matching String Occurrence Found - " + getCount
                + " Times! Validation Of Invoice is done Successfully!");

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

    public void onlineGxAndULDLVerification(String expectedBandwidth) {
        resultInfo = Steps.CHARGING_ONLINE.simulator.onlineGx(
                "Protocol : Online Gx get policy from simulator before call", this, resultInfo, customerInfo, ts).run();

        String policyValue = resultInfo.getResult().toString();
        Assert.assertTrue(policyValue.contains(expectedBandwidth));
    }

    // Method to update Product Quality of Service Characteristics
    public JSONObject updateProductCharacteristics(String json, String entityToUpdate, String a) {
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

    public void verifyBill() {
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
           Jive.failAndContinue("FAILED : Invoice PDF Charges : " + ts.getInvoiceCharge() + " are Not Validated Successfully!! Please Check the Invoice Content : "
                    + readInvoiceData);
        }

    }

}
