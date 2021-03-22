package com.ericsson.soiv.testcases.scenes.cbio17;

import static com.ericsson.soiv.utils.HelperClass.toCheckCommunityId;
import static com.ericsson.soiv.utils.JsonHelper.getRequestedJsonElement;
import static com.ericsson.soiv.utils.JsonHelper.requestForPersonalizedPrice;
import static com.ericsson.soiv.utils.JsonHelper.searchKeyAndUpdateJson;
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
import com.ericsson.soiv.utils.TestResultInfo;
import com.ericsson.soiv.utils.TransactionSpecification;

// EMODRED Created on 22-May-2019

@Fixture(SoivFixture.class)
public class PostpaidPOCSOptionalVoiceCommunity extends SoivTestBase {
    private TestResultInfo resultInfo = new TestResultInfo();
    private CustomerInfo customerInfo = new CustomerInfo();
    private TransactionSpecification ts = new TransactionSpecification();

    @Test
    @Id("00000025")
    @Tags("OT-Regression, OT-Postpaid, EC-Controlled, OT-CBiO17")
    @Description("TS755_Postpaid_PO_CS_Optional_Voice_Community")
    public void postpaidPOCSOptionalVoiceCommunity() throws InterruptedException, IOException {
        LinkedHashMap<String, String> businessCache = new LinkedHashMap<>(
                readBusinessCacheData(this.getTestCase().getId()));

        customerInfo.setCustomerBillCycle(businessCache.get("CUSTOMER.BILLCYCLE"));
        customerInfo.setOrderMode(businessCache.get("ORDER.MODE"));
        customerInfo.setRequester(businessCache.get("ORDER.REQUESTER"));

        LinkedHashMap<String, String> productOfferings = new LinkedHashMap<String, String>();
        LinkedHashMap<String, String> customerMSISDNmap = new LinkedHashMap<String, String>();

        /*****************************
         * Creating first community contract with basic and Milan PO - Contract A
         **************************/
        productOfferings.put("basic", businessCache.get("PO.ID_BASIC"));
        productOfferings.put("optionalMilan", businessCache.get("PO.ID_OPTIONAL_MILAN"));
        productOfferings.put("optionalRoma", businessCache.get("PO.ID_OPTIONAL_ROMA"));
        productOfferings.put("optionalJuventus", businessCache.get("PO.ID_OPTIONAL_JUVENTUS"));
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
        productOfferings.put("optional", productOfferings.get("optionalMilan"));
        ts.setProductOfferingIds(productOfferings);
        Steps.PROTOCOLS.businessLogicEOC.addOptionalPOonBasicPO(
                "Provision : Add Optional Community Milan PO in to Basic PO", this, customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision :  Submit Shopping Cart for Milan Community : Contract A", this,
                        customerInfo, ts, resultInfo)
                .run();
        customerMSISDNmap.put("firstMSISDN", customerInfo.getMsisdn());
        ts.setCalledNumber(customerMSISDNmap.get("firstMSISDN"));
        customerMSISDNmap.put("firstCustomerId", customerInfo.getCustomerId());

        /*****************************
         * Creating second community contract with basic and Milan PO - Contract B
         **************************/

        Steps.PROVISION.eoc.createCustomerContractBasicPO("Create Customer and Contract with Basic PO", this,
                customerInfo, ts, resultInfo).run();

        ts.setJsonFileName("AddOptionalPOonBasic.json");
        Steps.PROTOCOLS.businessLogicEOC.addOptionalPOonBasicPO(
                "Provision : Add Optional Community Milan PO in to Basic PO", this, customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision :  Submit Shopping Cart for Milan Community : Contract B", this,
                        customerInfo, ts, resultInfo)
                .run();
        customerMSISDNmap.put("secondMSISDN", customerInfo.getMsisdn());
        customerMSISDNmap.put("secondCustomerId", customerInfo.getCustomerId());

        /*****************************
         * Doing Voice Usage From SecondMSISDN(Contract B) to FirstMSISDN (Contract A)
         * and generating Bill
         **************************/

        /* = Usage= */
        ts.setDuration(Integer.parseInt(businessCache.get("USAGE.DURATION_1")));
        ts.setExpectedCost(businessCache.get("USAGE.EXPECTEDCOST_1"));
        ts.setCharge(businessCache.get("USAGE.SETCHARGE_1"));
        runVoiceUsageAndCheckBalance(ts, "Community");

        /* = Bill = */
        Thread.sleep(Integer.parseInt(businessCache.get("THREAD.SLEEP_BILL")));
        ts.setInvoicePdfPageNo(Integer.parseInt(businessCache.get("INVOICE.PAGENO_1")));
        ts.setInvoiceCharge(businessCache.get("INVOICE.CHARGE_1"));
        ts.setRemoveInvoicePdfFile(Boolean.valueOf(businessCache.get("INVOICE.REMOVE"))); // If We Wants to keep Invoice
                                                                                          // then Set this Flag as FALSE
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

        /*****************************
         * Adding Roma Community PO to both the contracts - Contract A
         **************************/
        customerInfo.setMsisdn(customerMSISDNmap.get("firstMSISDN"));
        customerInfo.setCustomerId(customerMSISDNmap.get("firstCustomerId"));
        productOfferings.put("optional", productOfferings.get("optionalRoma"));
        ts.setProductOfferingIds(productOfferings);
        Steps.PROVISION.eoc
                .repurchaseOptionalPO("Purchase ROMA community optional PO", this, customerInfo, ts, resultInfo).run();

        /*****************************
         * Adding Juventus Community PO to both the contracts - Contract B
         **************************/
        customerInfo.setMsisdn(customerMSISDNmap.get("secondMSISDN"));
        customerInfo.setCustomerId(customerMSISDNmap.get("secondCustomerId"));
        Steps.PROVISION.eoc
                .repurchaseOptionalPO("Purchase ROMA community optional PO", this, customerInfo, ts, resultInfo).run();

        /*****************************
         * Personalizing the ROMA community PO per min Charges
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
        getPOOIRequestBody = requestForPersonalizedPrice(getPOOIRequestBody.toString(), "PricePerMinCommunity", "0.3");
        ts.setResponseBody(getPOOIRequestBody.toString());

        Steps.PROTOCOLS.businessLogicEOC
                .updatePooi("Updation of Price Values for Personalization", this, customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC.submitShoppingCart("Provision : Submit Shopping Cart For Personalized usage",
                this, customerInfo, ts, resultInfo).run();

        /*****************************
         * Doing Voice Usage From SecondMSISDN to firstMSISDN
         **************************/
        /* = Usage= */
        ts.setExpectedCost(businessCache.get("USAGE.EXPECTEDCOST_2"));
        ts.setCharge(businessCache.get("USAGE.SETCHARGE_2"));
        runVoiceUsageAndCheckBalance(ts, "Community");

        /*****************************
         * Adding Juventus Community PO to both the contracts - Contract A
         **************************/
        customerInfo.setMsisdn(customerMSISDNmap.get("firstMSISDN"));
        customerInfo.setCustomerId(customerMSISDNmap.get("firstCustomerId"));
        productOfferings.put("optional", productOfferings.get("optionalJuventus"));
        ts.setProductOfferingIds(productOfferings);
        Steps.PROVISION.eoc
                .repurchaseOptionalPO("Purchase Juventus community optional PO", this, customerInfo, ts, resultInfo)
                .run();

        /*****************************
         * Adding Juventus Community PO to both the contracts - Contract B
         **************************/
        customerInfo.setMsisdn(customerMSISDNmap.get("secondMSISDN"));
        customerInfo.setCustomerId(customerMSISDNmap.get("secondCustomerId"));
        Steps.PROVISION.eoc
                .repurchaseOptionalPO("Purchase Juventus community optional PO", this, customerInfo, ts, resultInfo)
                .run();

        /*****************************
         * Personalizing the Juventus community PO per min Charges
         **************************/

        ts.setJsonFileName("createShoppingCart.json");
        Steps.PROVISION.eoc.createShoppingCart("Provision : Create Shopping Cart For Personalized usage", this,
                customerInfo, ts, resultInfo).run();

        resultInfo = Steps.PROTOCOLS.businessLogicEOC.getShoppingCart(
                "Protocol : Get Shopping Cart For Personalized usage", this, customerInfo, ts, resultInfo).run();

        pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("optional")), "id");
        customerInfo.setPooiId(pooiId);

        resultInfo = Steps.PROTOCOLS.businessLogicEOC
                .getPooi("Getting the POOI For Personalized usage", this, customerInfo, ts, resultInfo).run();

        getPOOIRequestBody = searchKeyAndUpdateJson(ts.getResponseBody(), "action", "Modify");
        getPOOIRequestBody = requestForPersonalizedPrice(getPOOIRequestBody.toString(), "PricePerMinCommunity", "0.4");
        ts.setResponseBody(getPOOIRequestBody.toString());

        Steps.PROTOCOLS.businessLogicEOC
                .updatePooi("Updation of Price Values for Personalization", this, customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC.submitShoppingCart("Provision : Submit Shopping Cart For Personalized usage",
                this, customerInfo, ts, resultInfo).run();

        /*****************************
         * Optional PO ROMA De Activation and Re Activation
         **************************/

        /* = De-Activation = */
        ts.setActionCode("Deactivate");
        ts.setReasonCode("DUNND");
        productOfferings.put("optional", productOfferings.get("optionalRoma"));
        ts.setProductOfferingIds(productOfferings);
        Steps.PROVISION.eoc
                .updateOptionalPO("DeActivate ROMA Optional PO From Contract 2", this, customerInfo, ts, resultInfo)
                .run();

        /* = community ID assertion = */
        Steps.PROTOCOLS.businessLogicCS.getAccountDetails("Get community ID", this, customerInfo, ts, resultInfo).run();
        String getAccountDetailsBody = ts.getResponseBody();
        JSONObject jsonObject = new JSONObject(getAccountDetailsBody);
        JSONArray communityInformationCurrent = jsonObject.getJSONArray("communityInformationCurrent");
        toCheckCommunityId(communityInformationCurrent);

        /* = Re-Activation = */
        ts.setActionCode("Reactivate");
        ts.setReasonCode("DUNND");
        Steps.PROVISION.eoc
                .updateOptionalPO("DeActivate ROMA Optional PO From Contract 2", this, customerInfo, ts, resultInfo)
                .run();

        /* ===community ID assertion====== */
        Steps.PROTOCOLS.businessLogicCS.getAccountDetails("Get community ID", this, customerInfo, ts, resultInfo).run();
        getAccountDetailsBody = ts.getResponseBody();
        jsonObject = new JSONObject(getAccountDetailsBody);
        communityInformationCurrent = jsonObject.getJSONArray("communityInformationCurrent");
        toCheckCommunityId(communityInformationCurrent);

        /*****************************
         * Doing Voice Usage From SecondMSISDN to firstMSISDN and generating Bill
         **************************/

        /* = Usage = */
        ts.setExpectedCost(businessCache.get("USAGE.EXPECTEDCOST_3"));
        ts.setCharge(businessCache.get("USAGE.SETCHARGE_3"));
        runVoiceUsageAndCheckBalance(ts, "Community");

        /* = Bill = */
        Thread.sleep(Integer.parseInt(businessCache.get("THREAD.SLEEP_BILL")));
        ts.setInvoicePdfPageNo(Integer.parseInt(businessCache.get("INVOICE.PAGENO_2")));
        ts.setInvoiceCharge(businessCache.get("INVOICE.CHARGE_2"));
        ts.setRemoveInvoicePdfFile(Boolean.valueOf(businessCache.get("INVOICE.REMOVE"))); // If We Wants to keep Invoice
                                                                                          // then Set this Flag as FALSE
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

    private void runVoiceUsageAndCheckBalance(TransactionSpecification ts, String message) {

        Steps.PROTOCOLS.businessLogicCS.getAccountBalanceAndDate(
                "Protocol : Before Usage - Get Account Balance from CS", this, customerInfo, ts, resultInfo).run();

        BigDecimal getBalanceBefore = ts.getAccountBalance();

        Steps.CHARGING_ONLINE.simulator.onlineCip("Protocol : " + message + " - Usage Online Charging CIP IP Simulator",
                this, resultInfo, customerInfo, ts).run();

        Steps.PROTOCOLS.businessLogicCS.getAccountBalanceAndDate("Protocol : After Usage - Get Account Balance from CS",
                this, customerInfo, ts, resultInfo).run();

        BigDecimal getBalanceAfter = ts.getAccountBalance();
        Jive.log("Balance Before : " + getBalanceBefore + " Balance After : " + getBalanceAfter);

        Assert.assertEquals(getBalanceBefore.subtract(new BigDecimal(ts.getCharge())), getBalanceAfter);
        Jive.log("OK : Expected Charges " + ts.getCharge() + " Deducted Successfully!");
    }
}
