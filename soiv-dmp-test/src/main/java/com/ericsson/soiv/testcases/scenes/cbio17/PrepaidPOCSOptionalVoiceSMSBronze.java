package com.ericsson.soiv.testcases.scenes.cbio17;

import com.ericsson.jive.core.execution.*;
import com.ericsson.soiv.fixtures.SoivFixture;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.Constants;
import com.ericsson.soiv.utils.CustomerInfo;
import com.ericsson.soiv.utils.TestResultInfo;
import com.ericsson.soiv.utils.TransactionSpecification;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedHashMap;

import static com.ericsson.soiv.utils.JsonHelper.*;
import static com.ericsson.soiv.utils.http.ReadBusinessData.readBusinessCacheData;

// ZMUKMAN Created on 01-May-2019

@Fixture(SoivFixture.class)
public class PrepaidPOCSOptionalVoiceSMSBronze extends SoivTestBase {
    private TestResultInfo resultInfo = new TestResultInfo();
    private CustomerInfo customerInfo = new CustomerInfo();
    private TransactionSpecification ts = new TransactionSpecification();

    @Test
    @Id("00000015")
    @Tags("OT-Regression, OT-Prepaid, EC-Controlled, OT-CBiO17")
    @Description("TS705 - Prepaid_PO_CS_Optional_Voice_SMS_Bronze")
    public void prepaidPoCsOptionalVoiceSmsBronze() throws IOException {

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

        Steps.PROVISION.eoc.createCustomerContractBasicPO("Provision : Create Customer and Contract with Basic PO",
                this, customerInfo, ts, resultInfo).run();

        ts.setJsonFileName("AddOptionalPOonBasic.json");
        Steps.PROTOCOLS.businessLogicEOC.addOptionalPOonBasicPO("Provision : Add Optional PO in to Basic PO", this,
                customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision : Submit Shopping Cart", this, customerInfo, ts, resultInfo).run();

        // Run SMS and Voice Usage for National Number
        ts.setCharge(businessCache.get("USAGE.SETCHARGE_NATIONAL")); // This will be used to validate getAccountBalance
                                                                     // after Charging
        ts.setCalledNumber(Constants.NATIONAL_NUMBER_1);
        ts.setDuration(Integer.parseInt(businessCache.get("USAGE.DURATION")));

        runUsageAndCheckBalance(ts, "National", businessCache.get("USAGE.EXPECTEDCOST_VOICE_NATIONAL"),
                businessCache.get("USAGE.EXPECTEDCOST_SMS_NATIONAL"));

        // Run SMS and Voice Usage for International Number
        ts.setCharge(businessCache.get("USAGE.SETCHARGE_INTERNATIONAL"));
        ts.setCalledNumber(Constants.INTERNATIONAL_NUMBER_EUROPE1_NR1);
        ts.setDuration(Integer.parseInt(businessCache.get("USAGE.DURATION")));
        runUsageAndCheckBalance(ts, "International", businessCache.get("USAGE.EXPECTEDCOST_VOICE_INTERNATIONAL"),
                businessCache.get("USAGE.EXPECTEDCOST_SMS_INTERNATIONAL"));

        /* =================For Personalized usage step================= */

        ts.setJsonFileName("createShoppingCart.json");
        Steps.PROVISION.eoc.createShoppingCart("Provision : Create Shopping Cart For Personalized usage", this,
                customerInfo, ts, resultInfo).run();

        resultInfo = Steps.PROTOCOLS.businessLogicEOC.getShoppingCart(
                "Protocol : Get Shopping Cart For Personalized usage", this, customerInfo, ts, resultInfo).run();

        String pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("optional")), "id");
        customerInfo.setPooiId(pooiId);

        resultInfo = Steps.PROTOCOLS.businessLogicEOC
                .getPooi("Getting the POOI For Personalized usage", this, customerInfo, ts, resultInfo).run();

        /* updating the values in POOI */

        JSONObject getPOOIRequestBody = searchKeyAndUpdateJson(ts.getResponseBody(),
                businessCache.get("PERSONALIZE.KEY_ACTION"), businessCache.get("PERSONALIZE.VALUE_MODIFY"));
        getPOOIRequestBody = requestForPersonalizedPrice(getPOOIRequestBody.toString(),
                businessCache.get("PERSONALIZE.ELEMENTKEY_1"), businessCache.get("PERSONALIZE.ELEMENTVALUE_1"));
        getPOOIRequestBody = requestForPersonalizedPrice(getPOOIRequestBody.toString(),
                businessCache.get("PERSONALIZE.ELEMENTKEY_2"), businessCache.get("PERSONALIZE.ELEMENTVALUE_2"));
        ts.setResponseBody(getPOOIRequestBody.toString());

        /* Update POOI Request with submitting the shopping cart */

        Steps.PROTOCOLS.businessLogicEOC
                .updatePooi("Updation of Price Values for Personalization", this, customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC.submitShoppingCart("Provision : Submit Shopping Cart For Personalized usage",
                this, customerInfo, ts, resultInfo).run();

        // Run SMS and Voice Usage for National Number
        ts.setCharge(businessCache.get("PERSONLAIZE.USAGE.SETCHARGE_NATIONAL")); // This will be used to validate
                                                                                 // getAccountBalance after Charging
        ts.setCalledNumber(Constants.NATIONAL_NUMBER_1);
        ts.setDuration(Integer.parseInt(businessCache.get("USAGE.DURATION")));

        runUsageAndCheckBalance(ts, "National", businessCache.get("PERSONLAIZE.USAGE.EXPECTEDCOST_VOICE_NATIONAL"),
                businessCache.get("PERSONLAIZE.USAGE.EXPECTEDCOST_SMS_NATIONAL"));

        // Run SMS and Voice Usage for International Number
        ts.setCharge(businessCache.get("PERSONLAIZE.USAGE.SETCHARGE_INTERNATIONAL"));
        ts.setCalledNumber(Constants.INTERNATIONAL_NUMBER_EUROPE1_NR1);
        ts.setDuration(Integer.parseInt(businessCache.get("USAGE.DURATION")));
        runUsageAndCheckBalance(ts, "International",
                businessCache.get("PERSONLAIZE.USAGE.EXPECTEDCOST_VOICE_INTERNATIONAL"),
                businessCache.get("PERSONLAIZE.USAGE.EXPECTEDCOST_SMS_INTERNATIONAL"));

        /* =================For De - Personalized usage step================= */

        ts.setJsonFileName("createShoppingCart.json");
        Steps.PROVISION.eoc.createShoppingCart("Provision : Create Shopping Cart For DePersonalized usage", this,
                customerInfo, ts, resultInfo).run();

        resultInfo = Steps.PROTOCOLS.businessLogicEOC.getShoppingCart(
                "Protocol : Get Shopping Cart For DePersonalized usage", this, customerInfo, ts, resultInfo).run();

        pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("optional")), "id");
        customerInfo.setPooiId(pooiId);

        resultInfo = Steps.PROTOCOLS.businessLogicEOC
                .getPooi("Getting the POOI For DePersonalized usage", this, customerInfo, ts, resultInfo).run();

        /* updating the values in POOI */

        getPOOIRequestBody = searchKeyAndUpdateJson(ts.getResponseBody(), businessCache.get("DEPERSONALIZE.KEY_ACTION"),
                businessCache.get("DEPERSONALIZE.VALUE_MODIFY"));
        getPOOIRequestBody = requestForPersonalizedPrice(getPOOIRequestBody.toString(),
                businessCache.get("DEPERSONALIZE.ELEMENTKEY_1"), businessCache.get("DEPERSONALIZE.ELEMENTVALUE_1"));
        getPOOIRequestBody = requestForPersonalizedPrice(getPOOIRequestBody.toString(),
                businessCache.get("DEPERSONALIZE.ELEMENTKEY_2"), businessCache.get("DEPERSONALIZE.ELEMENTVALUE_2"));
        ts.setResponseBody(getPOOIRequestBody.toString());

        /* Update POOI Request with submitting the shopping cart */

        Steps.PROTOCOLS.businessLogicEOC
                .updatePooi("Updation of Price Values for DePersonalization", this, customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC.submitShoppingCart("Provision : Submit Shopping Cart For DePersonalized usage",
                this, customerInfo, ts, resultInfo).run();

        // Run SMS and Voice Usage for National Number after DePersonalized
        ts.setCharge(businessCache.get("DEPERSONLAIZE.USAGE.SETCHARGE_NATIONAL")); // This will be used to validate
                                                                                   // getAccountBalance after Charging
        ts.setCalledNumber(Constants.NATIONAL_NUMBER_1);
        ts.setDuration(Integer.parseInt(businessCache.get("USAGE.DURATION")));

        runUsageAndCheckBalance(ts, "National", businessCache.get("DEPERSONLAIZE.USAGE.EXPECTEDCOST_VOICE_NATIONAL"),
                businessCache.get("DEPERSONLAIZE.USAGE.EXPECTEDCOST_SMS_NATIONAL"));

        // Run SMS and Voice Usage for International Number
        ts.setCharge(businessCache.get("DEPERSONLAIZE.USAGE.SETCHARGE_INTERNATIONAL"));
        ts.setCalledNumber(Constants.INTERNATIONAL_NUMBER_EUROPE1_NR1);
        ts.setDuration(Integer.parseInt(businessCache.get("USAGE.DURATION")));
        runUsageAndCheckBalance(ts, "International",
                businessCache.get("DEPERSONLAIZE.USAGE.EXPECTEDCOST_VOICE_INTERNATIONAL"),
                businessCache.get("DEPERSONLAIZE.USAGE.EXPECTEDCOST_SMS_INTERNATIONAL"));

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
