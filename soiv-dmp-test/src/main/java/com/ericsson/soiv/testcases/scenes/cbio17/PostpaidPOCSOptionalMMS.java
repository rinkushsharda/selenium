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

import static com.ericsson.soiv.utils.http.ReadBusinessData.readBusinessCacheData;

//ZMUKMAN Created on 14-May-2019

@Fixture(SoivFixture.class)
public class PostpaidPOCSOptionalMMS extends SoivTestBase {
    private TestResultInfo resultInfo = new TestResultInfo();
    private CustomerInfo customerInfo = new CustomerInfo();
    private TransactionSpecification ts = new TransactionSpecification();

    @Test
    @Id("00000028")
    @Tags("OT-Regression, OT-Postpaid, EC-Controlled, OT-CBiO17")
    @Description("TS763 - Postpaid PO CS Optional MMS")
    public void postpaidPOCSOptionalMMS() throws InterruptedException, IOException {

        LinkedHashMap<String, String> businessCache = new LinkedHashMap<>(
                readBusinessCacheData(this.getTestCase().getId()));

        customerInfo.setCustomerBillCycle(businessCache.get("CUSTOMER.BILLCYCLE"));
        customerInfo.setOrderMode(businessCache.get("ORDER.MODE"));
        customerInfo.setRequester(businessCache.get("ORDER.REQUESTER"));

        ts.setInvoiceCharge(businessCache.get("INVOICE.CHARGE"));
        ts.setInvoicePdfPageNo(Integer.parseInt(businessCache.get("INVOICE.PAGENO")));
        ts.setRemoveInvoicePdfFile(Boolean.valueOf(businessCache.get("INVOICE.REMOVE")));

        LinkedHashMap<String, String> productOfferings = new LinkedHashMap<>();
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
         * Consume National MMS
         **************************/
        customerInfo.setServiceIdentifier(Integer.parseInt(businessCache.get("SERVICE.IDENTIFIER_MMS")));
        ts.setDuration(Integer.parseInt(businessCache.get("USAGE.DURATION_NATIONAL")));
        ts.setExpectedCost(businessCache.get("USAGE.COST_NATIONAL"));
        ts.setCharge(businessCache.get("USAGE.CHARGE_NATIONAL"));
        ts.setCalledNumber(Constants.NATIONAL_NUMBER_1);
        runUsageAndCheckBalance(ts, "National");

        /*****************************
         * Consume International MMS
         **************************/
        customerInfo.setServiceIdentifier(Integer.parseInt(businessCache.get("SERVICE.IDENTIFIER_MMS")));
        ts.setDuration(Integer.parseInt(businessCache.get("USAGE.DURATION_INTERNATIONAL")));
        ts.setExpectedCost(businessCache.get("USAGE.COST_INTERNATIONAL"));
        ts.setCharge(businessCache.get("USAGE.CHARGE_INTERNATIONAL"));
        ts.setCalledNumber(Constants.INTERNATIONAL_NUMBER_EUROPE1_NR1);
        runUsageAndCheckBalance(ts, "International");

        /*****************************
         * Bill Generation
         **************************/
        Thread.sleep(Long.parseLong(businessCache.get("THREAD.SLEEP_USAGE")));
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

        /*****************************
         * Optional PO De Activation
         **************************/
        ts.setActionCode("Deactivate");
        ts.setReasonCode("DUNND");
        Steps.PROVISION.eoc.updateOptionalPO("DeActivate Optional PO From Contract", this, customerInfo, ts, resultInfo)
                .run();

        /*****************************
         * Consume National MMS To Validate PO De Activated Successfully
         **************************/
        customerInfo.setServiceIdentifier(Integer.parseInt(businessCache.get("SERVICE.IDENTIFIER_MMS")));
        ts.setDuration(Integer.parseInt(businessCache.get("USAGE.DURATION_NATIONAL")));
        ts.setExpectedValidationMessage(businessCache.get("USAGE.EXPECTEDMESSAGE_MMS"));
        ts.setCharge("0");
        ts.setCalledNumber(Constants.NATIONAL_NUMBER_1);
        runUsageAndCheckBalance(ts, "National");

    }

    private void runUsageAndCheckBalance(TransactionSpecification ts, String message) {

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
