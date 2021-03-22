package com.ericsson.soiv.testcases.scenes.cbio17;

import com.ericsson.jive.core.execution.*;
import com.ericsson.soiv.fixtures.SoivFixture;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.Constants;
import com.ericsson.soiv.utils.CustomerInfo;
import com.ericsson.soiv.utils.TestResultInfo;
import com.ericsson.soiv.utils.TransactionSpecification;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedHashMap;

import static com.ericsson.soiv.utils.HelperClass.getNumberOfMatchingStringCount;
import static com.ericsson.soiv.utils.http.ReadBusinessData.readBusinessCacheData;

// ZMUKMAN Created on 09-May-2019

@Fixture(SoivFixture.class)
public class PostpaidPOCSOptionalFreeUnitsVoice extends SoivTestBase {
    private TestResultInfo resultInfo = new TestResultInfo();
    private CustomerInfo customerInfo = new CustomerInfo();
    private TransactionSpecification ts = new TransactionSpecification();
    private LinkedHashMap<String, String> productOfferings = new LinkedHashMap<String, String>();

    @Test
    @Id("00000022")
    @Tags("OT-Regression, OT-Postpaid, EC-Controlled, OT-CBiO17")
    @Description("TS762_Postpaid_PO_CS_Optional_Free_Units_Voice")
    public void postpaidPoCsOptionalFreeUnitsVoice() throws InterruptedException, IOException {

        LinkedHashMap<String, String> businessCache = new LinkedHashMap<>(
                readBusinessCacheData(this.getTestCase().getId()));

        customerInfo.setCustomerBillCycle(businessCache.get("CUSTOMER.BILLCYCLE"));
        customerInfo.setOrderMode(businessCache.get("ORDER.MODE"));
        customerInfo.setRequester(businessCache.get("ORDER.REQUESTER"));

        ts.setInvoiceCharge(businessCache.get("INVOICE.CHARGE"));
        ts.setInvoicePdfPageNo(Integer.parseInt(businessCache.get("INVOICE.PAGENO")));
        ts.setRemoveInvoicePdfFile(Boolean.valueOf(businessCache.get("INVOICE.REMOVE")));

        productOfferings.put("basic", businessCache.get("PO.ID_BASIC"));
        productOfferings.put("optional", businessCache.get("PO.ID_OPTIONAL"));
        productOfferings.put("additionalOptionalPo", businessCache.get("PO.ID_ADDITIONAL_OPTIONAL"));
        ts.setProductOfferingIds(productOfferings);

        /*****************************
         * Create Contract A
         **************************/
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

        Steps.PROTOCOLS.businessLogicEOC.submitShoppingCart("Provision : Submit Shopping Cart For Contract A", this,
                customerInfo, ts, resultInfo).run();

        /*****************************
         * Set First MSISDN and Contract Id
         **************************/
        Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart To Set Contract ID", this, customerInfo, ts, resultInfo)
                .run();

        String setFirstContract = customerInfo.getContractId();
        String setFirstMSISDN = customerInfo.getMsisdn();
        String setFirstCustomerId = customerInfo.getCustomerId();

        /*****************************
         * Update Contract A with Free Voice Usage
         **************************/
        customerInfo.setContractId(setFirstContract);
        productOfferings.put("optional", productOfferings.get("additionalOptionalPo"));
        ts.setProductOfferingIds(productOfferings);
        Steps.PROVISION.eoc.repurchaseOptionalPO("Purchase Free Voice Optional PO on Contract A", this, customerInfo,
                ts, resultInfo).run();

        /*****************************
         * Consume 100/3600 Free Minutes Of Contract A
         **************************/
        ts.setExpectedCost(businessCache.get("USAGE.EXPECTEDCOST_FREE"));
        ts.setCharge(businessCache.get("USAGE.EXPECTEDCHARGE_FREE"));
        ts.setDuration(Integer.parseInt(businessCache.get("USAGE.DURATION_FREE")));
        ts.setCalledNumber(Constants.NATIONAL_NUMBER_1);
        customerInfo.setMsisdn(setFirstMSISDN);
        runUsageAndCheckBalance(ts, "National - Usage on Contract A : " + setFirstMSISDN);

        /*****************************
         * Create Contract B
         **************************/
        Steps.PROVISION.eoc.createCustomerContractBasicPO("Create Customer and Contract with Basic PO", this,
                customerInfo, ts, resultInfo).run();

        ts.setJsonFileName("AddOptionalPOonBasic.json");
        Steps.PROTOCOLS.businessLogicEOC.addOptionalPOonBasicPO("Provision : Add Optional PO in to Basic PO", this,
                customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC.submitShoppingCart("Provision : Submit Shopping Cart For Contract B", this,
                customerInfo, ts, resultInfo).run();

        String setSecondMSISDN = customerInfo.getMsisdn();
        String setSecondCustomerId = customerInfo.getCustomerId();

        /*****************************
         * Consume 100/3600 Free Minutes of Contract B
         **************************/
        ts.setExpectedCost(businessCache.get("USAGE.EXPECTEDCOST_FREE"));
        ts.setCharge(businessCache.get("USAGE.EXPECTEDCHARGE_FREE"));
        ts.setDuration(Integer.parseInt(businessCache.get("USAGE.DURATION_FREE")));
        ts.setCalledNumber(Constants.NATIONAL_NUMBER_1);
        customerInfo.setMsisdn(setSecondMSISDN);
        runUsageAndCheckBalance(ts, "National - Usage on Contract B : " + setSecondMSISDN);

         /*****************************
         * Optional PO De Activation and Re Activation
         **************************/

        ts.setActionCode(businessCache.get("ACTION.CODE_DEACTIVATE"));
        ts.setReasonCode(businessCache.get("REASON.CODE_DEACTIVATE"));
        productOfferings.put("optional", productOfferings.get("additionalOptionalPo"));
        ts.setProductOfferingIds(productOfferings);
        Steps.PROVISION.eoc.updateOptionalPO("DeActivate Free Voice Optional PO From Contract A", this, customerInfo,
                ts, resultInfo).run();

        ts.setActionCode(businessCache.get("ACTION.CODE_REACTIVATE"));
        ts.setReasonCode(businessCache.get("REASON.CODE_REACTIVATE"));
        productOfferings.put("optional", productOfferings.get("additionalOptionalPo"));
        ts.setProductOfferingIds(productOfferings);
        Steps.PROVISION.eoc.updateOptionalPO("ReActivate Free Voice Optional PO From Contract A", this, customerInfo,
                ts, resultInfo).run();

        /*****************************
         * Do Some Usage for the Contracts B that Contributes to the Free Units
         **************************/
        ts.setExpectedCost(businessCache.get("USAGE.EXPECTEDCOST_FREE"));
        ts.setCharge(businessCache.get("USAGE.EXPECTEDCHARGE_FREE"));
        ts.setDuration(Integer.parseInt(businessCache.get("USAGE.DURATION_FREE_1")));
        ts.setCalledNumber(Constants.NATIONAL_NUMBER_1);
        customerInfo.setMsisdn(setSecondMSISDN);
        runUsageAndCheckBalance(ts, "National - Usage on Contract B : " + setSecondMSISDN);

        /*****************************
         * Free Voice Optional PO Re Purchase Steps 1. DeActivate 2.Assign on Contract
         * A, 3. Usage , 4. De Activate and 5. Assign again
         **************************/
        ts.setActionCode(businessCache.get("ACTION.CODE_DEACTIVATE"));
        ts.setReasonCode(businessCache.get("REASON.CODE_DEACTIVATE"));
        productOfferings.put("optional", productOfferings.get("additionalOptionalPo"));
        ts.setProductOfferingIds(productOfferings);
        Steps.PROVISION.eoc.updateOptionalPO("DeActivate Free Voice Optional PO From Contract A", this, customerInfo,
                ts, resultInfo).run();

        customerInfo.setContractId(setFirstContract);
        productOfferings.put("optional", productOfferings.get("additionalOptionalPo"));
        ts.setProductOfferingIds(productOfferings);
        Steps.PROVISION.eoc.repurchaseOptionalPO("Assign Again Free Voice Optional PO on Basic", this, customerInfo, ts,
                resultInfo).run();

        /*****************************
         * Consume 100/3400 free Minutes for First MSISDN
         **************************/
        ts.setExpectedCost(businessCache.get("USAGE.EXPECTEDCOST_FREE"));
        ts.setCharge(businessCache.get("USAGE.EXPECTEDCHARGE_FREE"));
        ts.setDuration(Integer.parseInt(businessCache.get("USAGE.DURATION_FREE")));
        ts.setCalledNumber(Constants.NATIONAL_NUMBER_1);
        customerInfo.setMsisdn(setFirstMSISDN);
        runUsageAndCheckBalance(ts, "National - Usage on Contract A : " + setFirstMSISDN);

        ts.setActionCode(businessCache.get("ACTION.CODE_DEACTIVATE"));
        ts.setReasonCode(businessCache.get("REASON.CODE_DEACTIVATE"));
        productOfferings.put("optional", productOfferings.get("additionalOptionalPo"));
        ts.setProductOfferingIds(productOfferings);
        Steps.PROVISION.eoc.updateOptionalPO("DeActivate Free Voice Optional Again For Repurchase on Contract A", this,
                customerInfo, ts, resultInfo).run();

        customerInfo.setContractId(setFirstContract);
        productOfferings.put("optional", productOfferings.get("additionalOptionalPo"));
        ts.setProductOfferingIds(productOfferings);
        Steps.PROVISION.eoc.repurchaseOptionalPO("RePurchase Free Voice Optional PO on Contract A", this, customerInfo,
                ts, resultInfo).run();

        /*****************************
         * Consume all Remaining free Minutes for First MSISDN
         **************************/
        ts.setExpectedCost(businessCache.get("USAGE.EXPECTEDCOST_FREE"));
        ts.setCharge(businessCache.get("USAGE.EXPECTEDCHARGE_FREE"));
        ts.setDuration(Integer.parseInt(businessCache.get("USAGE.DURATION_FREE_2")));
        ts.setCalledNumber(Constants.NATIONAL_NUMBER_1);
        customerInfo.setMsisdn(setFirstMSISDN);
        runUsageAndCheckBalance(ts, "National - Usage on Contract A : " + setFirstMSISDN);

        /*****************************
         * Bill Generation CUSTOMER A
         **************************/
        Thread.sleep(Long.parseLong(businessCache.get("THREAD.SLEEP_USAGE")));
        invoiceValidation(setFirstCustomerId);

        /*****************************
         * Bill Generation CUSTOMER B
         **************************/
        Thread.sleep(Long.parseLong(businessCache.get("THREAD.SLEEP_USAGE")));
        invoiceValidation(setSecondCustomerId);
    }

    private void runUsageAndCheckBalance(TransactionSpecification ts, String message) {

        Steps.PROTOCOLS.businessLogicCS.getAccountBalanceAndDate(
                "Protocol : Before Usage - Get Account Balance from CS", this, customerInfo, ts, resultInfo).run();

        BigDecimal getBalanceBefore = ts.getAccountBalance();

        Steps.CHARGING_ONLINE.simulator.onlineCip("Protocol : " + message + " -  Online Charging CIP IP Simulator",
                this, resultInfo, customerInfo, ts).run();
        Steps.PROTOCOLS.businessLogicCS.getAccountBalanceAndDate("Protocol : After Usage - Get Account Balance from CS",
                this, customerInfo, ts, resultInfo).run();

        BigDecimal getBalanceAfter = ts.getAccountBalance();
        Jive.log("Balance Before : " + getBalanceBefore + " Balance After : " + getBalanceAfter);

        Assert.assertEquals(getBalanceBefore.subtract(new BigDecimal(ts.getCharge())), getBalanceAfter);
        Jive.log("OK : Expected Charges " + ts.getCharge() + " Deducted Successfully!");
    }

    private void invoiceValidation(String customerId){
        customerInfo.setCustomerId(customerId);
        ts.setJsonFileName("sendBillGenerateRequest.xml");

        Steps.BILL_GENERATION.stepsBilling
                .sendInvoiceRequest("Protocol : Send Bill Process Create Request For Customer : " +customerInfo.getCustomerId(), this, resultInfo, customerInfo, ts)
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