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
import static com.ericsson.soiv.utils.http.ReadBusinessData.readBusinessCacheData;

// ZMUKMAN Created on 20-May-2019

@Fixture(SoivFixture.class)
public class PostpaidPOCSOptionalMessagingMonthly extends SoivTestBase {
    private TestResultInfo resultInfo = new TestResultInfo();
    private CustomerInfo customerInfo = new CustomerInfo();
    private TransactionSpecification ts = new TransactionSpecification();

    @Test
    @Id("00000030")
    @Tags("OT-Regression, OT-Postpaid, EC-Controlled, OT-CBiO17")
    @Description("TS759_Postpaid_PO_CS_Optional_Messaging_Monthly")
    public void postpaidPOCSOptionalMessageMonthly() throws InterruptedException, IOException {

        LinkedHashMap<String, String> businessCache = new LinkedHashMap<>(
                readBusinessCacheData(this.getTestCase().getId()));
        customerInfo.setCustomerBillCycle(businessCache.get("CUSTOMER.BILLCYCLE"));
        customerInfo.setOrderMode(businessCache.get("ORDER.MODE"));
        customerInfo.setRequester(businessCache.get("ORDER.REQUESTER"));

        LinkedHashMap<String, String> productOfferings = new LinkedHashMap<>();
        productOfferings.put("basic", businessCache.get("PO.ID_BASIC"));
        productOfferings.put("optional", businessCache.get("PO.ID_OPTIONAL"));
        ts.setProductOfferingIds(productOfferings);

        ts.setInvoiceCharge(businessCache.get("INVOICE.CHARGE"));
        ts.setInvoicePdfPageNo(Integer.parseInt(businessCache.get("INVOICE.PAGENO")));
        ts.setRemoveInvoicePdfFile(Boolean.valueOf(businessCache.get("INVOICE.REMOVE")));

        // Personalized Characteristics Values
        String smsRecurringCountLimit = businessCache.get("RECURRINGCOUNT.LIMIT");
        String smsRecurringAmountMonthly = businessCache.get("RECURRINGAMOUNT.MONTHLY");

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
         * National Usage
         **************************/
        ts.setCharge(businessCache.get("USAGE.CHARGE_SMS_MMS"));
        ts.setDuration(Integer.parseInt(businessCache.get("USAGE.DURATION_SMS_MMS")));
        ts.setCalledNumber(Constants.NATIONAL_NUMBER_1);
        runUsageAndCheckBalance(ts, "National - Usage on Contract : " + customerInfo.getMsisdn(),
                businessCache.get("USAGE.COST_SMS"), businessCache.get("USAGE.COST_MMS"));

        /*****************************
         * Run PAM To Reset the SMS Counter (ZERO)
         **************************/

        Steps.PROTOCOLS.businessLogicCS
                .getAccountDetails("Get PAM Service ID For SMS", this, customerInfo, ts, resultInfo).run();

        int pamServiceID = searchPamServiceId(ts.getResponseBody(),
                Integer.parseInt(businessCache.get("PAM.CLASS_ID_SMS")));

        customerInfo.setPamServiceId(pamServiceID);

        Steps.PROTOCOLS.businessLogicCS
                .runPam("Run PAM To Reset SMS Counter Value as ZERO", this, customerInfo, ts, resultInfo).run();

        /*****************************
         * Run PAM To Reset the MMS Counter (ZERO)
         **************************/

        Steps.PROTOCOLS.businessLogicCS
                .getAccountDetails("Get PAM Service ID For MMS", this, customerInfo, ts, resultInfo).run();

        pamServiceID = searchPamServiceId(ts.getResponseBody(),
                Integer.parseInt(businessCache.get("PAM.CLASS_ID_MMS")));

        customerInfo.setPamServiceId(pamServiceID);

        Steps.PROTOCOLS.businessLogicCS
                .runPam("Run PAM To Reset SMS Counter Value as ZERO", this, customerInfo, ts, resultInfo).run();

        /*****************************
         * National Usage Again to do the Usage Counter Adjustments
         **************************/
        ts.setCharge(businessCache.get("USAGE.CHARGE_SMS_MMS"));
        ts.setDuration(Integer.parseInt(businessCache.get("USAGE.DURATION_SMS_MMS")));
        ts.setCalledNumber(Constants.NATIONAL_NUMBER_1);
        runUsageAndCheckBalance(ts, "National - Usage on Contract : " + customerInfo.getMsisdn(),
                businessCache.get("USAGE.COST_SMS"), businessCache.get("USAGE.COST_MMS"));

        /*****************************
         * Get Usage Counter And Do Adjustment for 10 SMS
         **************************/

        Steps.PROTOCOLS.businessLogicCS
                .getThreshholdAndCounters("Get the Usage Counter information", this, customerInfo, ts, resultInfo)
                .run();

        int smsUsageCounterId = JsonPath.read(ts.getResponseBody(),
                "$.usageCounterUsageThresholdInformation[0].usageCounterID");

        int mmsUsageCounterId = JsonPath.read(ts.getResponseBody(),
                "$.usageCounterUsageThresholdInformation[1].usageCounterID");

        updateUsageCounter(smsUsageCounterId, businessCache.get("USAGE.COUNTER_ID_SMS"));

        updateUsageCounter(mmsUsageCounterId, businessCache.get("USAGE.COUNTER_ID_MMS"));

        /*****************************
         * Bill Generation And Verify Total Amount with SMS and MMS Usage
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

        if (readInvoiceData.contains("GRAND TOTAL " + ts.getInvoiceCharge()))
            /*    && (readInvoiceData.contains("SMS " + productOfferings.get("optional") + " - 0.00% 0.00 € "
                        + businessCache.get("INVOICE.READLINE_SMS")))
                && (readInvoiceData.contains("MMS " + productOfferings.get("optional") + " - 0.00% 0.00 € "
                        + businessCache.get("INVOICE.READLINE_MMS")))) */{
            Jive.log("OK : Invoice PDF Charges : " + ts.getInvoiceCharge() + " are Validated Successfully");
        } else {
           Jive.failAndContinue("FAILED : Invoice PDF Charges : " + ts.getInvoiceCharge() + " are Not Validated Successfully!! Please Check the Invoice Content : "
                    + readInvoiceData);
        }

        /*****************************
         * Personalized Price Characteristics
         **************************/

          ts.setJsonFileName("createShoppingCart.json");
          Steps.PROVISION.eoc.createShoppingCart("Provision : Create Shopping Cart For Personalized Price",this, customerInfo, ts, resultInfo) .run();

          resultInfo = Steps.PROTOCOLS.businessLogicEOC.getShoppingCart("Protocol : Get Shopping Cart For Personalized Price", this, customerInfo, ts, resultInfo).run();

         String pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("optional")), "id");
          customerInfo.setPooiId(pooiId);

          resultInfo = Steps.PROTOCOLS.businessLogicEOC.getPooi("Getting the POOI For Personalized Price", this, customerInfo, ts, resultInfo) .run();

          JSONObject getPOOIRequestBody = searchKeyAndUpdateJson(ts.getResponseBody(),"action", "Modify");
          getPOOIRequestBody = requestForPersonalizedPrice(getPOOIRequestBody.toString(), "RECAMNT_PERS",smsRecurringCountLimit);
          getPOOIRequestBody = requestForPersonalizedPrice(getPOOIRequestBody.toString(), "RECAMNT_BC_PERS",smsRecurringAmountMonthly);
         ts.setResponseBody(getPOOIRequestBody.toString());

          Steps.PROTOCOLS.businessLogicEOC.updatePooi("Provision : Personalization of Price Values", this,customerInfo, ts, resultInfo).run();

          Steps.PROTOCOLS.businessLogicEOC.submitShoppingCart("Provision : Submit Shopping Cart For Personalized Price", this, customerInfo, ts, resultInfo).run();

        ts.setActionCode("Deactivate");
        ts.setReasonCode("DUNND");
        Steps.PROVISION.eoc.updateOptionalPO("DeActivate Optional PO From Contract", this, customerInfo, ts, resultInfo)
                .run();
    }

    private void updateUsageCounter(int usageCounterId, String usageCounterValueNew) {

        customerInfo.setUsageCounterId(usageCounterId);
        ts.setusageCounterValueNew(usageCounterValueNew);

        ts.setRequestedUri("/csadmin/request_handler.php?request=UsageCounters%2FUpdateUsageCounters&subscriberNumber="
                + customerInfo.getMsisdn() + "&transactionCurrency=&updateUsageCounterForMultiUser=&usageCounterID="
                + customerInfo.getUsageCounterId() + "&usageCounterValueNew=" + ts.getusageCounterValueNew()
                + "&adjustmentUsageCounterValueRelative=&usageCounterMonetaryValueNew=&adjustmentUsageCounterMonetaryValueRelative=&associatedPartyID=&productID="
                + "&action=Send&returnFormat=json");

        Steps.PROTOCOLS.businessLogicCS.usageCounterAdjustment("Update the Usage Counter To Free Limit : 10", this,
                customerInfo, ts, resultInfo).run();

        String usageCounterUpdatedValue = JsonPath.read(ts.getResponseBody(),
                "$.usageCounterUsageThresholdInformation[0].usageCounterValue");

        Assert.assertEquals(ts.getusageCounterValueNew(), usageCounterUpdatedValue);
    }

    private void runUsageAndCheckBalance(TransactionSpecification ts, String message, String smsCost, String mmsCost) {

        Steps.PROTOCOLS.businessLogicCS.getAccountBalanceAndDate(
                "Protocol : Before Usage - Get Account Balance from CS", this, customerInfo, ts, resultInfo).run();

        BigDecimal getBalanceBefore = ts.getAccountBalance();

        ts.setExpectedCost(smsCost);
        Steps.CHARGING_ONLINE.simulator
                .onlineSms("Protocol : " + message + " - Online SMS", this, resultInfo, customerInfo, ts).run();

        ts.setExpectedCost(mmsCost);
        Steps.CHARGING_ONLINE.simulator
                .onlineScapV2SSU("Protocol : " + message + " - Online MMS", this, resultInfo, customerInfo, ts).run();

        Steps.PROTOCOLS.businessLogicCS.getAccountBalanceAndDate("Protocol : After Usage - Get Account Balance from CS",
                this, customerInfo, ts, resultInfo).run();

        BigDecimal getBalanceAfter = ts.getAccountBalance();
        Jive.log("Balance Before : " + getBalanceBefore + " Balance After : " + getBalanceAfter);

        Assert.assertEquals(getBalanceBefore.subtract(new BigDecimal(ts.getCharge())), getBalanceAfter);
        Jive.log("OK : Expected Charges " + ts.getCharge() + " Deducted Successfully!");
    }
}