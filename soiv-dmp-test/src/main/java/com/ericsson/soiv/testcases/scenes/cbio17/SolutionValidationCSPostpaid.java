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

import static com.ericsson.soiv.utils.JsonHelper.getRequestedJsonElement;
import static com.ericsson.soiv.utils.JsonHelper.searchKeyAndUpdateJson;
import static com.ericsson.soiv.utils.http.ReadBusinessData.readBusinessCacheData;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedHashMap;

// Created By EMODRED 5-May-2019

@Fixture(SoivFixture.class)
public class SolutionValidationCSPostpaid extends SoivTestBase {
    private TestResultInfo resultInfo = new TestResultInfo();
    private CustomerInfo customerInfo = new CustomerInfo();
    private TransactionSpecification ts = new TransactionSpecification();

    @Test
    @Id("00000076")
    @Tags("OT-Regression, OT-Postpaid, BSCS-Controlled, OT-CBiO17")
    @Description("TS799_Solution_Validation_CS_Postpaid")
    public void solutionValidationCSPostpaid() throws InterruptedException, IOException {
        LinkedHashMap<String, String> businessCache = new LinkedHashMap<>(
                readBusinessCacheData(this.getTestCase().getId()));
        customerInfo.setCustomerBillCycle(businessCache.get("CUSTOMER.BILLCYCLE"));
        customerInfo.setOrderMode(businessCache.get("ORDER.MODE"));
        customerInfo.setRequester(businessCache.get("ORDER.REQUESTER"));

        LinkedHashMap<String, String> productOfferings = new LinkedHashMap<String, String>();
        productOfferings.put("basic", businessCache.get("PO.ID_BASIC"));
        ts.setProductOfferingIds(productOfferings);

        customerInfo.setNpCodePub(businessCache.get("MSISDN.NPCODEPUB"));
        customerInfo.setPlCodePub(businessCache.get("MSISDN.PLCODEPUB"));
        customerInfo.setSubmIdPub(businessCache.get("MSISDN.SUBMIDPUB"));
        customerInfo.setHlCode(businessCache.get("MSISDN.HLCODE"));
        customerInfo.setRsCode(businessCache.get("MSISDN.RSCODE"));
        customerInfo.setCsControlled(businessCache.get("MSISDN.CSCONTROLLED"));

        Steps.PROVISION.eoc.createCustomerContractBasicPO("Create Customer and Contract with Basic PO", this,
                customerInfo, ts, resultInfo).run();

        /* =======Adding 1st Optional PO========= */
        productOfferings.put("optional", businessCache.get("PO.ID_OPTIONAL_DATA"));
        ts.setProductOfferingIds(productOfferings);
        ts.setJsonFileName("AddOptionalPOonBasic.json");
        Steps.PROTOCOLS.businessLogicEOC.addOptionalPOonBasicPO("Provision : Add Optional PO in to Basic PO", this,
                customerInfo, ts, resultInfo).run();

        /* =======Adding 2nd Optional PO========= */
        productOfferings.put("optional", businessCache.get("PO.ID_OPTIONAL_MESSAGING"));
        ts.setProductOfferingIds(productOfferings);
        ts.setJsonFileName("AddOptionalPOonBasic.json");
        Steps.PROTOCOLS.businessLogicEOC.addOptionalPOonBasicPO("Provision : Add Optional PO in to Basic PO", this,
                customerInfo, ts, resultInfo).run();

        /* =======Adding 3rd Optional PO========= */
        productOfferings.put("optional", businessCache.get("PO.ID_OPTIONAL_MMS"));
        ts.setProductOfferingIds(productOfferings);
        ts.setJsonFileName("AddOptionalPOonBasic.json");
        Steps.PROTOCOLS.businessLogicEOC.addOptionalPOonBasicPO("Provision : Add Optional PO in to Basic PO", this,
                customerInfo, ts, resultInfo).run();

        /* =======Adding 4th Optional PO========= */
        productOfferings.put("optional", businessCache.get("PO.ID_OPTIONAL_VOICE"));
        ts.setProductOfferingIds(productOfferings);
        ts.setJsonFileName("AddOptionalPOonBasic.json");
        Steps.PROTOCOLS.businessLogicEOC.addOptionalPOonBasicPO("Provision : Add Optional PO in to Basic PO", this,
                customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision : Submit Shopping Cart", this, customerInfo, ts, resultInfo).run();

        /* ===========Executing the MMS usage======================= */
        customerInfo.setServiceIdentifier(Integer.parseInt(businessCache.get("SERVICE.IDENTIFIER_MMS")));
        ts.setDuration(Integer.parseInt(businessCache.get("MMS_USAGE.DURATION_NATIONAL")));
        ts.setExpectedCost(businessCache.get("MMS_USAGE.COST_NATIONAL"));
        ts.setCharge(businessCache.get("MMS_USAGE.CHARGE_NATIONAL"));
        ts.setCalledNumber(Constants.NATIONAL_NUMBER_1);
        runMMSUsageAndCheckBalance(ts, "National");

        /* ===========Executing the SMS usage======================= */
        ts.setCharge(businessCache.get("USAGE.SMS_CHARGE_1")); // This will be used to validate getAccountBalance after
                                                               // Charging
        ts.setCalledNumber(Constants.NATIONAL_NUMBER_1);
        ts.setDuration(Integer.parseInt(businessCache.get("USAGE.SMS_DURATION_1")));

        runSMSUsageAndCheckBalance(ts, "National", businessCache.get("USAGE.SMS_EXPECTEDCOST_1"));

        /* ====UsageCounterAdj======= */
        customerInfo.setUsageCounterId(Integer.parseInt(businessCache.get("USAGE.COUNTER_ID_1")));
        ts.setusageCounterValueNew(businessCache.get("USAGE.COUNTER_NEW_VALUE_1"));
        customerInfo.setProductID("");

        Steps.PROTOCOLS.businessLogicCS.usageCounterAdjustment("Protocol : Before Usage - Get Account Balance from CS",
                this, customerInfo, ts, resultInfo).run();

        /* ===========Executing the SMS usage======================= */
        ts.setCharge(businessCache.get("USAGE.SMS_CHARGE_2")); // This will be used to validate getAccountBalance after
                                                               // Charging
        ts.setCalledNumber(Constants.NATIONAL_NUMBER_1);
        ts.setDuration(Integer.parseInt(businessCache.get("USAGE.SMS_DURATION_2")));

        runSMSUsageAndCheckBalance(ts, "National", businessCache.get("USAGE.SMS_EXPECTEDCOST_2"));

        /* for Policy Verificaton */

        resultInfo = Steps.CHARGING_ONLINE.simulator
                .onlineGx("Protocol : Online Gx get policy from simulator", this, resultInfo, customerInfo, ts).run();
        String policyValue = resultInfo.getResult().toString();
        Assert.assertTrue(
                policyValue.contains("\"Max-Requested-Bandwidth-DL\":12000000,\"Max-Requested-Bandwidth-UL\":5000000"));

        /* ============ Run Data call and verify the balance============ */

        ts.setExpectedCost(businessCache.get("USAGE.DATA_EXPECTEDCOST_1")); // This is only for the CS Charging
                                                                            // Validation
        ts.setCharge(businessCache.get("USAGE.DATA_CHARGE_1")); // This will be used to validate getAccountBalance after
                                                                // Charging
        ts.setRatingGroup(businessCache.get("USAGE.DATA_RATINGGROUP_1"));

        runDataUsageAndCheckBalance(ts);

        /* ====UsageCounterAdj======= */

        customerInfo.setUsageCounterId(Integer.parseInt(businessCache.get("USAGE.COUNTER_ID_2")));
        ts.setusageCounterValueNew(businessCache.get("USAGE.COUNTER_NEW_VALUE_2"));
        customerInfo.setProductID("");

        Steps.PROTOCOLS.businessLogicCS.usageCounterAdjustment("Protocol : Before Usage - Get Account Balance from CS",
                this, customerInfo, ts, resultInfo).run();

        /* for Policy Verificaton */

        resultInfo = Steps.CHARGING_ONLINE.simulator
                .onlineGx("Protocol : Online Gx get policy from simulator", this, resultInfo, customerInfo, ts).run();
        policyValue = resultInfo.getResult().toString();
        Assert.assertTrue(
                policyValue.contains("\"Max-Requested-Bandwidth-DL\":12000000,\"Max-Requested-Bandwidth-UL\":5000000"));

        /* ============ Run Data call and verify the balance============ */

        ts.setExpectedCost(businessCache.get("USAGE.DATA_EXPECTEDCOST_2")); // This is only for the CS Charging
                                                                            // Validation
        ts.setCharge(businessCache.get("USAGE.DATA_CHARGE_2")); // This will be used to validate getAccountBalance after
                                                                // Charging
        ts.setRatingGroup(businessCache.get("USAGE.DATA_RATINGGROUP_2"));

        runDataUsageAndCheckBalance(ts);

        /* ====UsageCounterAdj======= */

        customerInfo.setUsageCounterId(Integer.parseInt(businessCache.get("USAGE.COUNTER_ID_3")));
        ts.setusageCounterValueNew(businessCache.get("USAGE.COUNTER_NEW_VALUE_3"));
        customerInfo.setProductID("");

        Steps.PROTOCOLS.businessLogicCS.usageCounterAdjustment("Protocol : Before Usage - Get Account Balance from CS",
                this, customerInfo, ts, resultInfo).run();

        /* ============ Run Data call and verify the balance============ */

        ts.setExpectedValidationMessage("DIAMETER_END_USER_SERVICE_DENIED"); // This is only for the CS Charging
                                                                             // Validation
        ts.setCharge(businessCache.get("USAGE.DATA_CHARGE_3")); // This will be used to validate getAccountBalance after
                                                                // Charging
        ts.setRatingGroup(businessCache.get("USAGE.DATA_RATINGGROUP_3"));

        runDataUsageAndCheckBalance(ts);

        /* =====PAM execution======= */

        Steps.PROTOCOLS.businessLogicCS.getAccountDetails("Get Account Details", this, customerInfo, ts, resultInfo)
                .run();
        String respBody = ts.getResponseBody();

        /* ======1st PO Service ID======= */
        int pamServiceID = JsonPath.read(respBody, "$.pamInformationList[0].pamServiceID");
        customerInfo.setPamServiceId(pamServiceID);
        Steps.PROTOCOLS.businessLogicCS
                .runPam("Protocol : Resetting PAM to update the SMS counter", this, customerInfo, ts, resultInfo).run();
        /* ======2nd PO Service ID======= */
        pamServiceID = JsonPath.read(respBody, "$.pamInformationList[1].pamServiceID");
        customerInfo.setPamServiceId(pamServiceID);
        Steps.PROTOCOLS.businessLogicCS
                .runPam("Protocol : Resetting PAM to update the SMS counter", this, customerInfo, ts, resultInfo).run();

        /* ======3rd PO Service ID======= */
        pamServiceID = JsonPath.read(respBody, "$.pamInformationList[2].pamServiceID");
        customerInfo.setPamServiceId(pamServiceID);
        Steps.PROTOCOLS.businessLogicCS
                .runPam("Protocol : Resetting PAM to update the SMS counter", this, customerInfo, ts, resultInfo).run();

        /* ======4th PO Service ID======= */
        pamServiceID = JsonPath.read(respBody, "$.pamInformationList[3].pamServiceID");
        customerInfo.setPamServiceId(pamServiceID);
        Steps.PROTOCOLS.businessLogicCS
                .runPam("Protocol : Resetting PAM to update the SMS counter", this, customerInfo, ts, resultInfo).run();

        /* ===========Executing the SMS usage======================= */

        ts.setCharge(businessCache.get("USAGE.SMS_CHARGE_3")); // This will be used to validate getAccountBalance after
                                                               // Charging
        ts.setCalledNumber(Constants.NATIONAL_NUMBER_1);
        ts.setDuration(Integer.parseInt(businessCache.get("USAGE.SMS_DURATION_3")));

        runSMSUsageAndCheckBalance(ts, "National", businessCache.get("USAGE.SMS_EXPECTEDCOST_3"));

        /* ============ Run Data call and verify the balance============ */

        ts.setExpectedCost(businessCache.get("USAGE.DATA_EXPECTEDCOST_4")); // This is only for the CS Charging
                                                                            // Validation
        ts.setCharge(businessCache.get("USAGE.DATA_CHARGE_4")); // This will be used to validate getAccountBalance after
                                                                // Charging
        ts.setRatingGroup(businessCache.get("USAGE.DATA_RATINGGROUP_4"));

        runDataUsageAndCheckBalance(ts);

        /* ===================Step to Generate Bill============================ */

        Thread.sleep(Integer.parseInt(businessCache.get("BILL.THREAD_SLEEP_1")));
        ts.setInvoicePdfPageNo(Integer.parseInt(businessCache.get("BILL.INVOICE_PDF_PAGE_1")));
        ts.setInvoiceCharge(businessCache.get("BILL.INVOICE_CHARGE_1"));
        ts.setRemoveInvoicePdfFile(false); // If We Wants to keep Invoice then Set this Flag as FALSE
        ts.setJsonFileName("sendBillGenerateRequest.xml");
        Steps.BILL_GENERATION.stepsBilling
                .sendInvoiceRequest("Protocol : Send Bill Process Create Request", this, resultInfo, customerInfo, ts)
                .run();

        resultInfo = Steps.BILL_GENERATION.stepsBilling
                .getInvoice("Validation : Get Invoice PDF and Validate Charges(net) in PDF", this, resultInfo,
                        customerInfo, ts)
                .run();

        String readInvoiceData = (String) resultInfo.getResult();

        if (readInvoiceData.contains("GRAND TOTAL " + ts.getInvoiceCharge())) {
            Jive.log("OK : Invoice PDF Charges : " + ts.getInvoiceCharge() + " are Validated Successfully");
        } else {
           Jive.failAndContinue("FAILED : Invoice PDF Charges : " + ts.getInvoiceCharge() + " are Not Validated Successfully!! Please Check the Invoice Content : "
                    + readInvoiceData);
        }

        /* ============ Run Data call and verify the balance============ */

        ts.setExpectedCost(businessCache.get("USAGE.DATA_EXPECTEDCOST_5")); // This is only for the CS Charging
                                                                            // Validation
        ts.setCharge(businessCache.get("USAGE.DATA_CHARGE_5")); // This will be used to validate getAccountBalance after
                                                                // Charging
        ts.setRatingGroup(businessCache.get("USAGE.DATA_RATINGGROUP_5"));

        runDataUsageAndCheckBalance(ts);

        /* ===================Step to De-Activate the PO============================ */

        productOfferings.put("optional", "PO_CS_OptionalDataMonthlyPAM_O1");
        ts.setProductOfferingIds(productOfferings);

        ts.setJsonFileName("createShoppingCart.json");
        Steps.PROVISION.eoc.createShoppingCart("Provision : Create Shopping Cart to terminate the PO", this,
                customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart to terminate the PO", this, customerInfo, ts, resultInfo)
                .run();

        String pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("optional")), "id");
        customerInfo.setPooiId(pooiId);

        Steps.PROTOCOLS.businessLogicEOC
                .getPooi("Getting the POOI To Deactivate PO", this, customerInfo, ts, resultInfo).run();

        ts.setActionCode("Deactivate");
        ts.setReasonCode("DUNND");

        JSONObject getAction = searchKeyAndUpdateJson(ts.getResponseBody(), "action", ts.getActionCode());
        JSONObject reasonCode = searchKeyAndUpdateJson(getAction.toString(), "reasonCode", ts.getReasonCode());

        ts.setResponseBody(reasonCode.toString());

        /* Update POOI Request with submitting the shopping cart */

        Steps.PROTOCOLS.businessLogicEOC.updatePooi("Update For DeActivation PO ", this, customerInfo, ts, resultInfo)
                .run();

        Steps.PROTOCOLS.businessLogicEOC.submitShoppingCart("Provision : Submit Shopping Cart to terminate the PO",
                this, customerInfo, ts, resultInfo).run();

        /* asserting the action by comparing the status */

        Steps.PROTOCOLS.businessLogicEOC.getShoppingCart("Protocol : Get Shopping Cart to verify terminated PO status",
                this, customerInfo, ts, resultInfo).run();

        String getActualAction = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("optional")),
                "action");
        Assert.assertEquals(ts.getActionCode(), getActualAction);
        Jive.log("OK : Expected & Actual Action : " + getActualAction + " Matched Successfully!");

        /* ===================Step to Re-Activate the PO============================ */

        ts.setJsonFileName("createShoppingCart.json");
        Steps.PROVISION.eoc.createShoppingCart("Provision : Create Shopping Cart to terminate the PO", this,
                customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart to terminate the PO", this, customerInfo, ts, resultInfo)
                .run();

        pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("optional")), "id");
        customerInfo.setPooiId(pooiId);

        Steps.PROTOCOLS.businessLogicEOC
                .getPooi("Getting the POOI To Deactivate PO", this, customerInfo, ts, resultInfo).run();

        ts.setActionCode("Reactivate");
        ts.setReasonCode("DUNNA");

        getAction = searchKeyAndUpdateJson(ts.getResponseBody(), "action", ts.getActionCode());
        reasonCode = searchKeyAndUpdateJson(getAction.toString(), "reasonCode", ts.getReasonCode());

        ts.setResponseBody(reasonCode.toString());

        /* Update POOI Request with submitting the shopping cart */

        Steps.PROTOCOLS.businessLogicEOC.updatePooi("Update For DeActivation PO ", this, customerInfo, ts, resultInfo)
                .run();

        Steps.PROTOCOLS.businessLogicEOC.submitShoppingCart("Provision : Submit Shopping Cart to terminate the PO",
                this, customerInfo, ts, resultInfo).run();

        /* asserting the action by comparing the status */

        Steps.PROTOCOLS.businessLogicEOC.getShoppingCart("Protocol : Get Shopping Cart to verify terminated PO status",
                this, customerInfo, ts, resultInfo).run();

        getActualAction = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("optional")),
                "action");
        Assert.assertEquals(ts.getActionCode(), getActualAction);
        Jive.log("OK : Expected & Actual Action : " + getActualAction + " Matched Successfully!");

        /* ============ Run Data call and verify the balance============ */

        ts.setExpectedCost(businessCache.get("USAGE.DATA_EXPECTEDCOST_6")); // This is only for the CS Charging
                                                                            // Validation
        ts.setCharge(businessCache.get("USAGE.DATA_CHARGE_6")); // This will be used to validate getAccountBalance after
                                                                // Charging
        ts.setRatingGroup(businessCache.get("USAGE.DATA_RATINGGROUP_6"));

        runDataUsageAndCheckBalance(ts);

        /* ===================Step to Generate Bill============================ */
        Thread.sleep(Integer.parseInt(businessCache.get("BILL.THREAD_SLEEP_2")));
        ts.setInvoicePdfPageNo(Integer.parseInt(businessCache.get("BILL.INVOICE_PDF_PAGE_2")));
        ts.setInvoiceCharge(businessCache.get("BILL.INVOICE_CHARGE_2"));
        ts.setRemoveInvoicePdfFile(false); // If We Wants to keep Invoice then Set this Flag as FALSE
        ts.setJsonFileName("sendBillGenerateRequest.xml");
        Steps.BILL_GENERATION.stepsBilling
                .sendInvoiceRequest("Protocol : Send Bill Process Create Request", this, resultInfo, customerInfo, ts)
                .run();

        resultInfo = Steps.BILL_GENERATION.stepsBilling
                .getInvoice("Validation : Get Invoice PDF and Validate Charges(net) in PDF", this, resultInfo,
                        customerInfo, ts)
                .run();

        readInvoiceData = (String) resultInfo.getResult();

        if (readInvoiceData.contains("GRAND TOTAL " + ts.getInvoiceCharge())) {
            Jive.log("OK : Invoice PDF Charges : " + ts.getInvoiceCharge() + " are Validated Successfully");
        } else {
           Jive.failAndContinue("FAILED : Invoice PDF Charges : " + ts.getInvoiceCharge() + " are Not Validated Successfully!! Please Check the Invoice Content : "
                    + readInvoiceData);
        }

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

    private void runSMSUsageAndCheckBalance(TransactionSpecification ts, String message, String smsExpectedCost) {

        Steps.PROTOCOLS.businessLogicCS.getAccountBalanceAndDate(
                "Protocol : Before Usage - Get Account Balance from CS", this, customerInfo, ts, resultInfo).run();

        BigDecimal getBalanceBefore = ts.getAccountBalance();

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

    private void runMMSUsageAndCheckBalance(TransactionSpecification ts, String message) {

        Steps.PROTOCOLS.businessLogicCS.getAccountBalanceAndDate(
                "Protocol : Before Usage - Get Account Balance from CS", this, customerInfo, ts, resultInfo).run();

        BigDecimal getBalanceBefore = ts.getAccountBalance();

        Steps.CHARGING_ONLINE.simulator.onlineScapV2SSU("Protocol : Online " + message + " MMS Usage CIP IP Simulator",
                this, resultInfo, customerInfo, ts).run();

        Steps.PROTOCOLS.businessLogicCS.getAccountBalanceAndDate("Protocol : After Usage - Get Account Balance from CS",
                this, customerInfo, ts, resultInfo).run();

        BigDecimal getBalanceAfter = ts.getAccountBalance();
        Jive.log("Balance Before : " + getBalanceBefore + " Balance After : " + getBalanceAfter);

        Assert.assertEquals(getBalanceBefore.subtract(new BigDecimal(ts.getCharge())), getBalanceAfter);
        Jive.log("OK : Expected Charges " + ts.getCharge() + " Deducted Successfully!");
    }
}
