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
import org.junit.Assert;
import org.junit.Test;

import static com.ericsson.soiv.utils.http.ReadBusinessData.readBusinessCacheData;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedHashMap;

// ZMUKMAN Created on 02-May-2019

@Fixture(SoivFixture.class)
public class PrepaidPOCSOptionalFreeDailySMS extends SoivTestBase {
    private TestResultInfo resultInfo = new TestResultInfo();
    private CustomerInfo customerInfo = new CustomerInfo();
    private TransactionSpecification ts = new TransactionSpecification();

    @Test
    @Id("00000017")
    @Tags("OT-Regression, OT-Prepaid, EC-Controlled, OT-CBiO17")
    @Description("TS703_Prepaid_PO_CS_Optional_Free_Daily_SMS")
    public void prepaidPoCsOptionalFreeDailySms() throws IOException {

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

        /*****************************
         * Consume 1 Free SMS from 10
         **************************/
        // Set the Expected Cost and Charges for the SMS Usage
        ts.setExpectedCost(businessCache.get("USAGE.EXPECTEDCOST"));
        ts.setCharge(businessCache.get("USAGE.SETCHARGE"));
        ts.setCalledNumber(Constants.NATIONAL_NUMBER_1);
        runUsageAndCheckBalance(ts, "National");

        /*****************************
         * Get Usage Counter And Do Adjustment for 10 (Free SMS)
         **************************/

        Steps.PROTOCOLS.businessLogicCS
                .getThreshholdAndCounters("Get the Usage Counter information", this, customerInfo, ts, resultInfo)
                .run();

        int usageCounterId = JsonPath.read(ts.getResponseBody(),
                "$.usageCounterUsageThresholdInformation[0].usageCounterID");

        customerInfo.setUsageCounterId(usageCounterId);
        ts.setusageCounterValueNew(businessCache.get("USAGE.USAGECOUNTER"));

        // This RequestedURI is for the Single Product , If Multiple Products then Set
        // the Product Id
        // and Then remove below requested URI.

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

        /*****************************
         * Run SMS Usage And Verify Service Denied Response
         **************************/

        // Set the Expected Response Message and Charges for the SMS Usage
        ts.setExpectedValidationMessage(businessCache.get("USAGE.EXPECTEDMESSAGE"));
        ts.setCharge(businessCache.get("USAGE.SETCHARGE"));
        ts.setCalledNumber(Constants.NATIONAL_NUMBER_1);
        runUsageAndCheckBalance(ts, "National");

        /*****************************
         * Run PAM So it Reset the SMS Counter(10) Then Run SMS Usage Again
         **************************/

        Steps.PROTOCOLS.businessLogicCS.getAccountDetails("Get PAM Service ID", this, customerInfo, ts, resultInfo)
                .run();
        int pamServiceID = JsonPath.read(ts.getResponseBody(), "$.pamInformationList[0].pamServiceID");

        customerInfo.setPamServiceId(pamServiceID);

        Steps.PROTOCOLS.businessLogicCS.runPam("Run PAM and Reset SMS Counter", this, customerInfo, ts, resultInfo)
                .run();

        // Set the Expected Cost and Charges for the SMS Usage
        ts.setExpectedCost(businessCache.get("USAGE.EXPECTEDCOST"));
        ts.setCharge(businessCache.get("USAGE.SETCHARGE"));
        ts.setCalledNumber(Constants.NATIONAL_NUMBER_1);
        runUsageAndCheckBalance(ts, "National");

        /*****************************
         * Verify Usage Counter
         **************************/

        Steps.PROTOCOLS.businessLogicCS.getThreshholdAndCounters("Validate the Usage Counter After SMS Usage ", this,
                customerInfo, ts, resultInfo).run();

        usageCounterUpdatedValue = JsonPath.read(ts.getResponseBody(),
                "$.usageCounterUsageThresholdInformation[0].usageCounterValue");
        Assert.assertEquals(businessCache.get("USAGE.EXPECTED_COUNTER_VALUE"), usageCounterUpdatedValue);
        Jive.log("OK : Expected Usage Counter " + usageCounterUpdatedValue + " Matched Successfully!");
    }

    private void runUsageAndCheckBalance(TransactionSpecification ts, String message) {

        Steps.PROTOCOLS.businessLogicCS.getAccountBalanceAndDate(
                "Protocol : Before Usage - Get Account Balance from CS", this, customerInfo, ts, resultInfo).run();

        BigDecimal getBalanceBefore = ts.getAccountBalance();

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
