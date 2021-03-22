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
import com.ericsson.soiv.utils.CustomerInfo;
import com.ericsson.soiv.utils.TestResultInfo;
import com.ericsson.soiv.utils.TransactionSpecification;

//Created by ZEAHDCE 22 May 2019
@Fixture(SoivFixture.class)
public class SolutionValidationCSPostpaidOptionalBundle extends SoivTestBase {
    private TestResultInfo resultInfo = new TestResultInfo();
    private CustomerInfo customerInfo = new CustomerInfo();
    private TransactionSpecification ts = new TransactionSpecification();
    private LinkedHashMap<String, String> productOfferings = new LinkedHashMap<String, String>();
    LinkedHashMap<String, String> businessCache = null;

    @Test
    @Id("00000046")
    @Tags("OT-Regression, OT-Prepaid, EC-Controlled, OT-CBiO17")
    @Description("TS799 - Solution Validation CS Optional Bundled PO")
    public void solutionValidationCSPostpaidOptionalBundle() throws IOException {

        businessCache = new LinkedHashMap<>(readBusinessCacheData(this.getTestCase().getId()));
        customerInfo.setCustomerBillCycle(businessCache.get("CUSTOMER.BILLCYCLE"));
        customerInfo.setOrderMode(businessCache.get("ORDER.MODE"));
        customerInfo.setRequester(businessCache.get("ORDER.REQUESTER"));

        productOfferings.put("basic", businessCache.get("PO.ID_BASIC_OPTIONAL_BUNDLE"));
        productOfferings.put("optional", businessCache.get("PO.ID_OPTIONAL_BUNDLE"));
        productOfferings.put("addon", businessCache.get("PO.ID_ADDON_OPTIONAL_BUNDLE"));
        productOfferings.put("OPTIONAL_BUNDLE_Messaging", businessCache.get("PO.ID_OPTIONAL_BUNDLE_Messaging"));

        ts.setProductOfferingIds(productOfferings);

        /******************************
         * Create Contract with Basic PO and Submit the cart
         **************************/
        customerInfo.setNpCodePub(businessCache.get("MSISDN.NPCODEPUB"));
        customerInfo.setPlCodePub(businessCache.get("MSISDN.PLCODEPUB"));
        customerInfo.setSubmIdPub(businessCache.get("MSISDN.SUBMIDPUB"));
        customerInfo.setHlCode(businessCache.get("MSISDN.HLCODE"));
        customerInfo.setRsCode(businessCache.get("MSISDN.RSCODE"));
        customerInfo.setCsControlled(businessCache.get("MSISDN.CSCONTROLLED"));

        Steps.PROVISION.eoc.createCustomerContractBasicPO("Create Customer and Contract with Basic PO", this,
                customerInfo, ts, resultInfo).run();
        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Submit shopping cart for Basic PO ", this, customerInfo, ts, resultInfo).run();

        /******************************
         * Create Contract with Optional Bundled PO with Add on PO and Submit the cart
         **************************/

        ts.setJsonFileName("createShoppingCart.json");
        Steps.PROVISION.eoc.createShoppingCart("Provision : Create Shopping Cart For Optional Bundled PO and Add on PO",
                this, customerInfo, ts, resultInfo).run();

        // Set Basic PO Relies On
        Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart To get Basic PO", this, customerInfo, ts, resultInfo)
                .run();

        String pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("basic")), "id");
        ts.setBasicPoReferenceId(pooiId);

        ts.setJsonFileName("AddOptionalPOonBasic.json");
        Steps.PROTOCOLS.businessLogicEOC.addOptionalPOonBasicPO("Provision : Add Optional Bundled PO in to Basic PO",
                this, customerInfo, ts, resultInfo).run();

        // Set Bundle PO Child of
        Steps.PROTOCOLS.businessLogicEOC.getShoppingCart("Protocol : Get Shopping Cart To get Optional Bundled PO",
                this, customerInfo, ts, resultInfo).run();

        pooiId = getRequestPooiId(ts, String.valueOf(ts.getProductOfferingIds().get("optional")), "id");

        ts.setBasicPoReferenceId(pooiId);

        ts.setJsonFileName("AddOnPOonBundle.json");

        Steps.PROTOCOLS.businessLogicEOC.addAddOnPOonBasicPO("Provision : Update Add On PO in to Optional Bundled PO",
                this, customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision : Submit Shopping Cart For Optional Bundled PO and Add on PO", this,
                        customerInfo, ts, resultInfo)
                .run();

        /******************************
         * Create Contract with another instance of same Add on PO and Submit the cart
         **************************/

        ts.setJsonFileName("createShoppingCart.json");
        Steps.PROVISION.eoc
                .createShoppingCart("Provision : Create Shopping Cart For another instance of same Add on PO", this,
                        customerInfo, ts, resultInfo)
                .run();

        // Set Bundle PO Child of
        Steps.PROTOCOLS.businessLogicEOC.getShoppingCart("Protocol : Get Shopping Cart To get Optional Bundled PO",
                this, customerInfo, ts, resultInfo).run();

        pooiId = getRequestPooiId(ts, String.valueOf(ts.getProductOfferingIds().get("optional")), "id");

        ts.setBasicPoReferenceId(pooiId);

        ts.setJsonFileName("AddOnPOonBundle.json");

        Steps.PROTOCOLS.businessLogicEOC
                .addAddOnPOonBasicPO("Provision : Add another instance of addOn PO to the Optional Bundled PO", this,
                        customerInfo, ts, resultInfo)
                .run();

        Steps.PROTOCOLS.businessLogicEOC.submitShoppingCart(
                "Provision : Submit Shopping Cart For another instance of addOn PO to the Optional Bundled PO", this,
                customerInfo, ts, resultInfo).run();

        /******************************
         * Create Contract for two instance of AddOn PO to the Optional Bundled PO and
         * Submit the cart
         **************************/

        ts.setJsonFileName("createShoppingCart.json");
        Steps.PROVISION.eoc.createShoppingCart("Provision : Create Shopping Cart For Optional Bundled PO and Add on PO",
                this, customerInfo, ts, resultInfo).run();

        // Set Bundle PO Child of
        Steps.PROTOCOLS.businessLogicEOC.getShoppingCart("Protocol : Get Shopping Cart To get Optional Bundled PO",
                this, customerInfo, ts, resultInfo).run();

        pooiId = getRequestPooiId(ts, String.valueOf(ts.getProductOfferingIds().get("optional")), "id");

        ts.setBasicPoReferenceId(pooiId);

        ts.setJsonFileName("AddOnPOonBundle.json");

        Steps.PROTOCOLS.businessLogicEOC.addAddOnPOonBasicPO("Provision : Update Add On PO in to Optional Bundled PO",
                this, customerInfo, ts, resultInfo).run();

        // Set Bundle PO Child of
        Steps.PROTOCOLS.businessLogicEOC.getShoppingCart("Protocol : Get Shopping Cart To get Optional Bundled PO",
                this, customerInfo, ts, resultInfo).run();

        pooiId = getRequestPooiId(ts, String.valueOf(ts.getProductOfferingIds().get("optional")), "id");

        ts.setBasicPoReferenceId(pooiId);

        ts.setJsonFileName("AddOnPOonBundle.json");

        Steps.PROTOCOLS.businessLogicEOC
                .addAddOnPOonBasicPO("Provision : Update another instance of addOn PO in to Optional Bundled PO", this,
                        customerInfo, ts, resultInfo)
                .run();

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision : Submit Shopping Cart for Optional Bundled PO and Add on PO", this,
                        customerInfo, ts, resultInfo)
                .run();

        /************************************************
         * OnlineCIP and BalanceValidation
         ********************/
        ts.setRatingGroup(businessCache.get("RATING.GROUP_OPTIONAL_BUNDLE"));
        ts.setCharge(businessCache.get("USAGE.CHARGE_OPTIONAL_BUNDLE"));
        ts.setExpectedCost(businessCache.get("USAGE.COST_OPTIONAL_BUNDLE")); // This is only for the CS Charging
                                                                             // Validation
        onlineGYCallAndBalanceVerification();

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

        if (readInvoiceData.contains("GRAND TOTAL " + ts.getInvoiceCharge())) {
            Jive.log("OK : Invoice PDF Charges : " + ts.getInvoiceCharge() + " are Validated Successfully");
        } else {
            Jive.failAndContinue("FAILED : Invoice PDF Charges  : " + ts.getInvoiceCharge() + " Not Validated, Please Check the Invoice Content : "
                    + readInvoiceData);
        }
        // Onetime Charge PO_CS_Optional_SolVal_O1 - 0.00% 0.00 € 0.00 €
       /* if (readInvoiceData.contains("Onetime Charge " + productOfferings.get("optional") + " - 0.00% 0.00 € ")) {
            Jive.log("OK : Invoice one time charges Validated for Bundled PO Successfully");
        } else {
            Jive.fail(
                    "FAILED : Invoice PDF one time Charges are Not Validated for Bundled PO Successfully! Please Check the Invoice Content : "
                            + readInvoiceData);
        }

        if ((readInvoiceData
                .contains("Recurring Charge " + productOfferings.get("OPTIONAL_BUNDLE_Messaging") + " - 0.00% 0.00 € "))
                && (readInvoiceData
                        .contains("Recurring Charge" + productOfferings.get("optional") + " - 0.00% 0.00 € "))) {

            Jive.log("OK : Invoice Recurring charges Validated for Optional Bundled PO Successfully");

        } else {
            Jive.fail(
                    "FAILED : Invoice PDF Recurring Charges are Not Validated for Optional Bundled PO Successfully! Please Check the Invoice Content : "
                            + readInvoiceData);
        }


        */
        /******************************
         * Deletion of Optional Bundled PO and repurchase the same Optional Bundled PO
         **************************/
        ts.setActionCode("Delete");

        ts.setProductOfferingIds(productOfferings);
        Steps.PROVISION.eoc.updateOptionalPO("Update POOI for Delete of Optional Bundled PO for repurchase again", this,
                customerInfo, ts, resultInfo).run();

        Steps.PROVISION.eoc.repurchaseOptionalPO("Repurchase the same Bundled optional PO to the Basic PO", this,
                customerInfo, ts, resultInfo).run();
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

}
