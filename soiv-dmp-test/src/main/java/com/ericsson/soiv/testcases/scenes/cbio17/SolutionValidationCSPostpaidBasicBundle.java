package com.ericsson.soiv.testcases.scenes.cbio17;

import static com.ericsson.soiv.utils.JsonHelper.getRequestPooiId;
import static com.ericsson.soiv.utils.JsonHelper.getRequestedJsonElement;
import static com.ericsson.soiv.utils.http.ReadBusinessData.readBusinessCacheData;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedHashMap;

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
import com.ericsson.soiv.utils.Constants;
import com.ericsson.soiv.utils.CustomerInfo;
import com.ericsson.soiv.utils.TestResultInfo;
import com.ericsson.soiv.utils.TransactionSpecification;

// Created by ZEAHDCE 16 May 2019

@Fixture(SoivFixture.class)
public class SolutionValidationCSPostpaidBasicBundle extends SoivTestBase {

    private TestResultInfo resultInfo = new TestResultInfo();
    private CustomerInfo customerInfo = new CustomerInfo();
    private TransactionSpecification ts = new TransactionSpecification();

    @Test
    @Id("00000029")
    @Tags("OT-Regression, OT-Prepaid, EC-Controlled, OT-CBiO17")
    @Description("TS799 - Solution Validation CS Basic Bundled PO")
    public void solutionValidationCSPostpaidBasicBundle() throws IOException {

        LinkedHashMap<String, String> businessCache = new LinkedHashMap<>(
                readBusinessCacheData(this.getTestCase().getId()));
        customerInfo.setCustomerBillCycle(businessCache.get("CUSTOMER.BILLCYCLE"));
        customerInfo.setOrderMode(businessCache.get("ORDER.MODE"));
        customerInfo.setRequester(businessCache.get("ORDER.REQUESTER"));

        /******************************
         * createCustomer Shopping Cart And Add BasicBundled PO in Shopping Cart
         ***************************/

        LinkedHashMap<String, String> productOfferings = new LinkedHashMap<String, String>();
        productOfferings.put("basic", businessCache.get("PO.ID_BASIC_BASIC_BUNDLE"));
        productOfferings.put("basic_in_bundled_PO", businessCache.get("PO.ID_BASIC_IN_BUNDLED_PO"));
        productOfferings.put("optional", businessCache.get("PO.ID_OPTIONAL_IN_BUNDLED_PO"));

        ts.setProductOfferingIds(productOfferings);

        LinkedHashMap<String, String> setPdfValidationCharge = new LinkedHashMap<String, String>();
        setPdfValidationCharge.put("basic_in_bundled_PO_OTC5",
                businessCache.get("INVOICE.CHARGE_BASIC_IN_BUNDLE_OTC5"));
        setPdfValidationCharge.put("basic_in_bundled_PO_OTC7",
                businessCache.get("INVOICE.CHARGE_BASIC_IN_BUNDLE_OTC7"));
        setPdfValidationCharge.put("basic_in_bundled_PO_OTC10",
                businessCache.get("INVOICE.CHARGE_BASIC_IN_BUNDLE_OTC10"));
        setPdfValidationCharge.put("basic_PO_OTC10", businessCache.get("INVOICE.CHARGE_BASIC_OTC10"));
        setPdfValidationCharge.put("basic_PO_RC100", businessCache.get("INVOICE.CHARGE_BASIC_RC100"));

        ts.setInvoiceValidationCharge(setPdfValidationCharge);
        
        customerInfo.setNpCodePub(businessCache.get("MSISDN.NPCODEPUB"));
        customerInfo.setPlCodePub(businessCache.get("MSISDN.PLCODEPUB"));
        customerInfo.setSubmIdPub(businessCache.get("MSISDN.SUBMIDPUB"));
        customerInfo.setHlCode(businessCache.get("MSISDN.HLCODE"));
        customerInfo.setRsCode(businessCache.get("MSISDN.RSCODE"));
        customerInfo.setCsControlled(businessCache.get("MSISDN.CSCONTROLLED"));

        resultInfo = Steps.PROVISION.bscs
                .createCustomer("Provision : Create Customer", this, customerInfo, ts, resultInfo).run();

        ts.setJsonFileName("createShoppingCart.json");
        resultInfo = Steps.PROVISION.eoc
                .createShoppingCart("Provision : Create Shopping Cart", this, customerInfo, ts, resultInfo).run();

        ts.setJsonFileName("addProductOfferingShoppingCart.json");
        resultInfo = Steps.PROVISION.eoc.addPoInShoppingCart("Provision : Add Basic Bundled PO in Shopping Cart", this,
                customerInfo, ts, resultInfo).run();

        /******************************
         * GetShoppingCart And GetBasicPOFromBundledPO, Update the Basic PO And Submit
         * the Cart
         **************************/
        

        ts.setJsonFileName("selectSerialNumber.xml");
        resultInfo = Steps.PROTOCOLS.businessLogicBSCS
                .getFreeSerialNumber("Protocol : Get Free Serial Number", this, customerInfo, ts, resultInfo).run();

        ts.setJsonFileName("selectLogicalResource.xml");
        resultInfo = Steps.PROTOCOLS.businessLogicBSCS
                .selectLogicalResource("Protocol : Get Free MSISDN Number", this, customerInfo, ts, resultInfo).run();
        customerInfo.setMsisdn(customerInfo.getresource().toString());

        Steps.PROVISION.eoc.getShoppingCart("Get shopping cart ", this, customerInfo, ts, resultInfo).run();

        String pooiId = getRequestedJsonElement(ts,
                String.valueOf(ts.getProductOfferingIds().get("basic_in_bundled_PO")), "id");
        customerInfo.setPooiId(pooiId);

        Steps.PROTOCOLS.businessLogicEOC.getPooi("Getting the POOI ", this, customerInfo, ts, resultInfo).run();

        resultInfo = Steps.PROTOCOLS.businessLogicEOC
                .updateProductOffering("Provision : Update Basic PO", this, customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Submit Shopping cart for Basic Bundled PO", this, customerInfo, ts, resultInfo)
                .run();

        /**************************
         * Add Optional PO to the Basic Bundled PO and Submit the Cart
         *************/

        ts.setJsonFileName("createShoppingCart.json");
        Steps.PROVISION.eoc
                .createShoppingCart("Provision : Create Shopping Cart For Adding Optional PO into Basic Bundled PO",
                        this, customerInfo, ts, resultInfo)
                .run();

        Steps.PROTOCOLS.businessLogicEOC.getShoppingCart("Protocol : Get Shopping Cart To get Basic Bundled PO", this,
                customerInfo, ts, resultInfo).run();

        pooiId = getRequestPooiId(ts, String.valueOf(ts.getProductOfferingIds().get("basic")), "id");

        ts.setBasicPoReferenceId(pooiId);

        ts.setJsonFileName("OptionalPOonBundle.json");
        // Set Bundle PO Child of
        Steps.PROTOCOLS.businessLogicEOC.addOptionalPOonBasicPO("Provision : Add Optional PO in to Basic Bundled PO",
                this, customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision : Submit Shopping Cart For Optional PO into Basic Bundled PO", this,
                        customerInfo, ts, resultInfo)
                .run();

        /************************************************
         * OnlineCIP and BalanceValidation
         ********************/
        ts.setExpectedCost(businessCache.get("USAGE.COST_BASIC_BUNDLE"));
        ts.setCharge(businessCache.get("USAGE.CHARGE_BASIC_BUNDLE"));
        ts.setCalledNumber(Constants.NATIONAL_NUMBER_1);
        ts.setDuration(Integer.parseInt(businessCache.get("USAGE.DURATION_BASIC_BUNDLE")));

        onlineUsageAndBalanceVerification();

        /*********************
         * DateCalculation for Invoice And Verification the One Time Charge,Recurring
         * Charge and Usage in the Invoice
         ************************/

        ts.setInvoiceCharge(businessCache.get("INVOICE.CHARGE_BASIC_BUNDLE")); // This Charges will be used to validate
                                                                               // Invoice PDF Charges
        ts.setInvoicePdfPageNo(Integer.parseInt(businessCache.get("INVOICE.PAGENO_BASIC_BUNDLE")));
        ts.setRemoveInvoicePdfFile(Boolean.parseBoolean(businessCache.get("INVOICE.REMOVE_BASIC_BUNDLE"))); // If We
                                                                                                            // Wants to
                                                                                                            // keep
                                                                                                            // Invoice
                                                                                                            // then Set
                                                                                                            // this Flag
                                                                                                            // as FALSE

        ts.setJsonFileName("sendBillGenerateRequest.xml");

        Steps.BILL_GENERATION.stepsBilling
                .sendInvoiceRequest("Protocol : Send Bill Process Create Request", this, resultInfo, customerInfo, ts)
                .run();

        resultInfo = Steps.BILL_GENERATION.stepsBilling
                .getInvoice("Validation : Get Invoice PDF and Validate PDF", this, resultInfo, customerInfo, ts).run();
        String readInvoiceData = (String) resultInfo.getResult();
    /*     if ((readInvoiceData.contains("Onetime Charge " + productOfferings.get("basic") + " - 0.00% 0.00 € "
                + ts.getInvoiceValidationCharge().get("basic_PO_OTC10") + " €"))
                && (readInvoiceData
                .contains("Onetime Charge " + productOfferings.get("basic_in_bundled_PO") + " - 0.00% 0.00 € "
                        + ts.getInvoiceValidationCharge().get("basic_in_bundled_PO_OTC10") + " €"))){
            Jive.log("OK : Invoice one time charges Validated for Bundled PO Successfully");
        } else {
            Jive.fail(
                    "FAILED : Invoice PDF one time Charges are Not Validated for Bundled PO Successfully! Please Check the Invoice Content : "
                            + readInvoiceData);
        }

       if (readInvoiceData.contains("Recurring Charge " + productOfferings.get("basic") + " - 0.00% 0.00 € "
                + ts.getInvoiceValidationCharge().get("basic_PO_RC100") + " €")) {
            Jive.log("OK : Invoice Recurring charges Validated for Bundled PO Successfully");
        } else {
            Jive.fail(
                    "FAILED : Invoice PDF Recurring Charges are Not Validated for Bundled PO Successfully! Please Check the Invoice Content : "
                            + readInvoiceData);
        }*/

        if (readInvoiceData.contains("GRAND TOTAL " + ts.getInvoiceCharge())) {
            Jive.log("OK : Invoice PDF Charges : " + ts.getInvoiceCharge() + " are Validated Successfully");
        } else {
           Jive.failAndContinue("FAILED : Invoice PDF Charges : " + ts.getInvoiceCharge() + " are Not Validated Successfully!! Please Check the Invoice Content : "
                    + readInvoiceData);
        }

    }

    private void onlineUsageAndBalanceVerification() {

        Steps.PROTOCOLS.businessLogicCS.getAccountBalanceAndDate("Protocol : Before - Get Account Balance from CS",
                this, customerInfo, ts, resultInfo).run();
        BigDecimal getBalanceBefore = ts.getAccountBalance();

        Jive.log("Balance Before is " + getBalanceBefore);

        Steps.CHARGING_ONLINE.simulator.onlineCip("Protocol : National - Usage Online Charging CIP IP Simulator ", this,
                resultInfo, customerInfo, ts).run();

        Steps.PROTOCOLS.businessLogicCS.getAccountBalanceAndDate("Protocol : After - Get Account Balance from CS", this,
                customerInfo, ts, resultInfo).run();

        BigDecimal getBalanceAfter = ts.getAccountBalance();
        Jive.log("Balance Before : " + getBalanceBefore + " Balance After : " + getBalanceAfter);

        Assert.assertEquals(getBalanceBefore.subtract(new BigDecimal(ts.getCharge())), getBalanceAfter);
        Jive.log("OK : Expected Balance " + ts.getCharge() + "Matched Successfully!");
    }

}
