package com.ericsson.soiv.testcases.scenes.cbio17;

import com.ericsson.jive.core.execution.*;
import com.ericsson.soiv.fixtures.SoivFixture;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.Constants;
import com.ericsson.soiv.utils.CustomerInfo;
import com.ericsson.soiv.utils.TestResultInfo;
import com.ericsson.soiv.utils.TransactionSpecification;
import com.jayway.jsonpath.JsonPath;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedHashMap;

import static com.ericsson.soiv.utils.JsonHelper.*;
import static com.ericsson.soiv.utils.JsonHelper.requestForPersonalizedPrice;
import static com.ericsson.soiv.utils.http.ReadBusinessData.readBusinessCacheData;

// ZMUKMAN Created on 13-May-2019

@Fixture(SoivFixture.class)
public class PostpaidPOCSOptionalVoiceMonthly extends SoivTestBase {
    private TestResultInfo resultInfo = new TestResultInfo();
    private CustomerInfo customerInfo = new CustomerInfo();
    private TransactionSpecification ts = new TransactionSpecification();

    @Test
    @Id("00000024")
    @Tags("OT-Regression, OT-Postpaid, EC-Controlled, OT-CBiO17")
    @Description("TS757_Postpaid_PO_CS_Optional_Voice_Monthly")
    public void postpaidPOCSOptionalVoiceMonthly() throws InterruptedException, IOException {

        LinkedHashMap<String, String> businessCache = new LinkedHashMap<>(
                readBusinessCacheData(this.getTestCase().getId()));
        customerInfo.setCustomerBillCycle(businessCache.get("CUSTOMER.BILLCYCLE"));
        customerInfo.setOrderMode(businessCache.get("ORDER.MODE"));
        customerInfo.setRequester(businessCache.get("ORDER.REQUESTER"));

        ts.setInvoiceCharge(businessCache.get("INVOICE.CHARGE"));
        ts.setInvoicePdfPageNo(Integer.parseInt(businessCache.get("INVOICE.PAGENO")));
        ts.setRemoveInvoicePdfFile(Boolean.valueOf(businessCache.get("INVOICE.REMOVE")));

        // Personalized Characteristics Values
        String voiceLimitRateChange = businessCache.get("SERVICE.CHARACTERISTICS_VOICE");
        String voiceMonthlyLimit = businessCache.get("SERVICE.CHARACTERISTICS_VOICE_MONTHLY");

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

        /*****************************
         * Personalized Price Characteristics
         **************************/

        ts.setJsonFileName("createShoppingCart.json");
        Steps.PROVISION.eoc.createShoppingCart("Provision : Create Shopping Cart For Personalized usage", this,
                customerInfo, ts, resultInfo).run();

        resultInfo = Steps.PROTOCOLS.businessLogicEOC.getShoppingCart(
                "Protocol : Get Shopping Cart For Personalized usage", this, customerInfo, ts, resultInfo).run();

        String pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("optional")), "id");
        customerInfo.setPooiId(pooiId);

        resultInfo = Steps.PROTOCOLS.businessLogicEOC
                .getPooi("Getting the POOI For Personalized usage", this, customerInfo, ts, resultInfo).run();

        JSONObject getPOOIRequestBody = searchKeyAndUpdateJson(ts.getResponseBody(), "action", "Modify");
        getPOOIRequestBody = requestForPersonalizedPrice(getPOOIRequestBody.toString(), "VoiceLimitRateChange",
                voiceLimitRateChange);
        getPOOIRequestBody = requestForPersonalizedPrice(getPOOIRequestBody.toString(), "VoiceMonthlyLimit",
                voiceMonthlyLimit);
        ts.setResponseBody(getPOOIRequestBody.toString());

        Steps.PROTOCOLS.businessLogicEOC
                .updatePooi("Provision : Personalization of Price Values", this, customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC.submitShoppingCart("Provision : Submit Shopping Cart For Personalized usage",
                this, customerInfo, ts, resultInfo).run();

        /***************************
         * National Usage Free Usage
         **************************/
        ts.setExpectedCost(businessCache.get("USAGE.EXPECTEDCOST_NATIONAL_FREE"));
        ts.setCharge(businessCache.get("USAGE.EXPECTEDCHARGE_NATIONAL_FREE"));
        ts.setDuration(Integer.parseInt(businessCache.get("USAGE.DURATION_NATIONAL_FREE")));
        ts.setCalledNumber(Constants.NATIONAL_NUMBER_1);
        runUsageAndCheckBalance(ts, "National Free - Usage on Contract : " + customerInfo.getMsisdn());

        /*****************************
         * International Usage Free Usage
         **************************/
        ts.setExpectedCost(businessCache.get("USAGE.EXPECTEDCOST_INTERNATIONAL_FREE"));
        ts.setCharge(businessCache.get("USAGE.EXPECTEDCHARGE_INTERNATIONAL_FREE"));
        ts.setDuration(Integer.parseInt(businessCache.get("USAGE.DURATION_INTERNATIONAL_FREE")));
        ts.setCalledNumber(Constants.INTERNATIONAL_NUMBER_EUROPE1_NR1);
        runUsageAndCheckBalance(ts, "International Free - Usage on Contract : " + customerInfo.getMsisdn());

        /***************************
         * National Usage Paid Usage
         **************************/
        ts.setExpectedCost(businessCache.get("USAGE.EXPECTEDCOST_NATIONAL_PAID"));
        ts.setCharge(businessCache.get("USAGE.EXPECTEDCHARGE_NATIONAL_PAID"));
        ts.setDuration(Integer.parseInt(businessCache.get("USAGE.DURATION_NATIONAL_PAID")));
        ts.setCalledNumber(Constants.NATIONAL_NUMBER_1);
        runUsageAndCheckBalance(ts, "National Paid - Usage on Contract : " + customerInfo.getMsisdn());

        /*****************************
         * International Usage Paid Usage
         **************************/
        ts.setExpectedCost(businessCache.get("USAGE.EXPECTEDCOST_INTERNATIONAL_PAID"));
        ts.setCharge(businessCache.get("USAGE.EXPECTEDCHARGE_INTERNATIONAL_PAID"));
        ts.setDuration(Integer.parseInt(businessCache.get("USAGE.DURATION_INTERNATIONAL_PAID")));
        ts.setCalledNumber(Constants.INTERNATIONAL_NUMBER_EUROPE1_NR1);
        runUsageAndCheckBalance(ts, "International Paid - Usage on Contract : " + customerInfo.getMsisdn());

        /*****************************
         * National Usage To Check MAX Limit Check
         **************************/
        ts.setExpectedValidationMessage(businessCache.get("USAGE.EXPECTEDMESSAGE"));
        ts.setCharge(businessCache.get("USAGE.EXPECTEDCHARGE_NATIONAL_FREE"));
        ts.setDuration(Integer.parseInt(businessCache.get("USAGE.DURATION_NATIONAL_FREE")));
        ts.setCalledNumber(Constants.NATIONAL_NUMBER_1);
        runUsageAndCheckBalance(ts, "National - Usage on Contract For Max Limit Check : " + customerInfo.getMsisdn());

        /*****************************
         * Get Usage Counter And Do Adjustment for 10 (Free Minutes)
         **************************/

        Steps.PROTOCOLS.businessLogicCS
                .getThreshholdAndCounters("Get the Usage Counter information", this, customerInfo, ts, resultInfo)
                .run();

        int usageCounterId = JsonPath.read(ts.getResponseBody(),
                "$.usageCounterUsageThresholdInformation[0].usageCounterID");

        customerInfo.setUsageCounterId(usageCounterId);
        ts.setusageCounterValueNew(businessCache.get("USAGE.COUNTER_VALUE"));

        ts.setRequestedUri("/csadmin/request_handler.php?request=UsageCounters%2FUpdateUsageCounters&subscriberNumber="
                + customerInfo.getMsisdn() + "&transactionCurrency=&updateUsageCounterForMultiUser=&usageCounterID="
                + customerInfo.getUsageCounterId() + "&usageCounterValueNew=" + ts.getusageCounterValueNew()
                + "&adjustmentUsageCounterValueRelative=&usageCounterMonetaryValueNew=&adjustmentUsageCounterMonetaryValueRelative=&associatedPartyID=&"
                + "productID=&action=Send&returnFormat=json");

        Steps.PROTOCOLS.businessLogicCS
                .usageCounterAdjustment("Update the Usage Counter To Free Limit : " + ts.getusageCounterValueNew(),
                        this, customerInfo, ts, resultInfo)
                .run();

        String usageCounterUpdatedValue = JsonPath.read(ts.getResponseBody(),
                "$.usageCounterUsageThresholdInformation[0].usageCounterValue");

        Assert.assertEquals(ts.getusageCounterValueNew(), usageCounterUpdatedValue);

        /*****************************
         * Run Usage After Counter Adjustment
         **************************/
        ts.setExpectedCost(businessCache.get("USAGE.EXPECTEDCOST_NATIONAL"));
        ts.setCharge(businessCache.get("USAGE.EXPECTEDCHARGE_NATIONAL"));
        ts.setDuration(Integer.parseInt(businessCache.get("USAGE.DURATION_NATIONAL")));
        ts.setCalledNumber(Constants.NATIONAL_NUMBER_1);
        runUsageAndCheckBalance(ts, "National Free - Usage on Contract After Adjustment: " + customerInfo.getMsisdn());

        /*****************************
         * Run PAM To Reset the Counter (ZERO)
         **************************/

        Steps.PROTOCOLS.businessLogicCS.getAccountDetails("Get PAM Service ID", this, customerInfo, ts, resultInfo)
                .run();
        int pamServiceID = JsonPath.read(ts.getResponseBody(), "$.pamInformationList[0].pamServiceID");

        customerInfo.setPamServiceId(pamServiceID);

        Steps.PROTOCOLS.businessLogicCS
                .runPam("Run PAM To Reset Voice Counter Value as ZERO", this, customerInfo, ts, resultInfo).run();

        /*****************************
         * Bill Generation
         **************************/
        Thread.sleep(Long.parseLong(businessCache.get("THREAD.SLEEP_USAGE")));
        ts.setJsonFileName("sendBillGenerateRequest.xml");

        Steps.BILL_GENERATION.stepsBilling
                .sendInvoiceRequest("Protocol : Send Bill Process Create Request", this, resultInfo, customerInfo, ts)
                .run();

        resultInfo = Steps.BILL_GENERATION.stepsBilling
                .getInvoice("Validation : Get Invoice PDF and Validate Amount To Pay in PDF", this, resultInfo,
                        customerInfo, ts)
                .run();
        String readInvoiceData = (String) resultInfo.getResult();

        if (readInvoiceData.contains("GRAND TOTAL " + ts.getInvoiceCharge())) {
            Jive.log("OK : Invoice PDF Charges : " + ts.getInvoiceCharge() + " are Validated Successfully");
        } else {
           Jive.failAndContinue("FAILED : Invoice PDF Charges : " + ts.getInvoiceCharge() + " are Not Validated Successfully!! Please Check the Invoice Content : "
                    + readInvoiceData);
        }

        /*****************************
         * DePersonalized Price Characteristics and Set to Zero
         **************************/

        ts.setJsonFileName("createShoppingCart.json");
        Steps.PROVISION.eoc.createShoppingCart("Provision : Create Shopping Cart For DePersonalized ", this,
                customerInfo, ts, resultInfo).run();

        resultInfo = Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart For DePersonalized ", this, customerInfo, ts, resultInfo)
                .run();

        pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("optional")), "id");
        customerInfo.setPooiId(pooiId);

        resultInfo = Steps.PROTOCOLS.businessLogicEOC
                .getPooi("Getting the POOI For DePersonalized ", this, customerInfo, ts, resultInfo).run();

        getPOOIRequestBody = searchKeyAndUpdateJson(ts.getResponseBody(), "action", "Modify");
        getPOOIRequestBody = requestForPersonalizedPrice(getPOOIRequestBody.toString(), "VoiceLimitRateChange", "0");
        getPOOIRequestBody = requestForPersonalizedPrice(getPOOIRequestBody.toString(), "VoiceMonthlyLimit", "0");
        ts.setResponseBody(getPOOIRequestBody.toString());

        Steps.PROTOCOLS.businessLogicEOC
                .updatePooi("Provision : DePersonalization of Price Values", this, customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC.submitShoppingCart("Provision : Submit Shopping Cart For DePersonalized ",
                this, customerInfo, ts, resultInfo).run();

        ts.setActionCode(businessCache.get("ACTION.CODE_DEACTIVATE"));
        ts.setReasonCode(businessCache.get("REASON.CODE_DEACTIVATE"));
        Steps.PROVISION.eoc.updateOptionalPO("DeActivate Free Voice Optional PO", this, customerInfo, ts, resultInfo)
                .run();

        /*****************************
         * National Usage After De Activation of PO
         **************************/
        ts.setExpectedValidationMessage(businessCache.get("USAGE.EXPECTEDMESSAGE"));
        ts.setCharge(businessCache.get("USAGE.EXPECTEDCHARGE_NATIONAL"));
        ts.setDuration(Integer.parseInt(businessCache.get("USAGE.DURATION_NATIONAL")));
        ts.setCalledNumber(Constants.NATIONAL_NUMBER_1);
        runUsageAndCheckBalance(ts, "National - Usage on Contract : " + customerInfo.getMsisdn());
    }

    private void runUsageAndCheckBalance(TransactionSpecification ts, String message) {

        Steps.PROTOCOLS.businessLogicCS.getAccountBalanceAndDate(
                "Protocol : Before Usage - Get Account Balance from CS", this, customerInfo, ts, resultInfo).run();

        BigDecimal getBalanceBefore = ts.getAccountBalance();

        Steps.CHARGING_ONLINE.simulator
                .onlineCip("Protocol : " + message + " - Online Voice", this, resultInfo, customerInfo, ts).run();

        Steps.PROTOCOLS.businessLogicCS.getAccountBalanceAndDate("Protocol : After Usage - Get Account Balance from CS",
                this, customerInfo, ts, resultInfo).run();

        BigDecimal getBalanceAfter = ts.getAccountBalance();
        Jive.log("Balance Before : " + getBalanceBefore + " Balance After : " + getBalanceAfter);

        Assert.assertEquals(getBalanceBefore.subtract(new BigDecimal(ts.getCharge())), getBalanceAfter);
        Jive.log("OK : Expected Charges " + ts.getCharge() + " Deducted Successfully!");
    }
}
