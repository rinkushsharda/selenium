package com.ericsson.soiv.testcases.scenes.cbio17;

import com.ericsson.jive.core.execution.*;
import com.ericsson.soiv.fixtures.SoivFixture;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.*;
import com.ericsson.soiv.utils.Constants;
import com.jayway.jsonpath.JsonPath;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import java.io.IOException;
import java.util.LinkedHashMap;

import static com.ericsson.soiv.utils.JsonHelper.*;
import static com.ericsson.soiv.utils.http.ReadBusinessData.readBusinessCacheData;

//Created By ZKAPSAR 22-May-2019

@Fixture(SoivFixture.class)
public class PostpaidPOCSAddOnVoice extends SoivTestBase {
    private TestResultInfo resultInfo = new TestResultInfo();
    private CustomerInfo customerInfo = new CustomerInfo();
    private TransactionSpecification ts = new TransactionSpecification();
    private LinkedHashMap<String, String> productOfferings = new LinkedHashMap<String, String>();
    private LinkedHashMap<String, String> businessCache = null;
    private String productId = null;
    private boolean foundPo = false;

    @Test
    @Id("00000034")
    @Tags("OT-Regression, OT-Postpaid, EC-Controlled, OT-CBiO17")
    @Description("TS766_Postpaid_PO_CS_AddOn_Voice")
    public void postpaidPoCsAddOnVoice() throws IOException {

        // Initialize Test Level Variables
        businessCache = new LinkedHashMap<>(readBusinessCacheData(this.getTestCase().getId()));
        int count = 0;
        String priceVoicePerMinuteNational = businessCache.get("PRICEVOICEPERMIN.NATIONAL");
        String priceVoicePerMinuteInternational = businessCache.get("PRICEVOICEPERMIN.INTERNATIONAL");
        String contractHistoryString = null;

        customerInfo.setCustomerBillCycle(businessCache.get("CUSTOMER.BILLCYCLE"));
        customerInfo.setOrderMode(businessCache.get("ORDER.MODE"));
        customerInfo.setRequester(businessCache.get("ORDER.REQUESTER"));

        productOfferings.put("basic", businessCache.get("PO.ID_BASIC"));
        productOfferings.put("addon", businessCache.get("PO.ID_ADDON"));
        ts.setProductOfferingIds(productOfferings);

        // Contracting with Basic PO
        customerInfo.setNpCodePub(businessCache.get("MSISDN.NPCODEPUB"));
        customerInfo.setPlCodePub(businessCache.get("MSISDN.PLCODEPUB"));
        customerInfo.setSubmIdPub(businessCache.get("MSISDN.SUBMIDPUB"));
        customerInfo.setHlCode(businessCache.get("MSISDN.HLCODE"));
        customerInfo.setRsCode(businessCache.get("MSISDN.RSCODE"));
        customerInfo.setCsControlled(businessCache.get("MSISDN.CSCONTROLLED"));

        Steps.PROVISION.eoc.createCustomerContractBasicPO("Create Customer and Contract with Basic PO", this,
                customerInfo, ts, resultInfo).run();

        // Adding AddOn PO in Shopping Cart
        ts.setJsonFileName("AddOnPOonBasic.json");
        Steps.PROTOCOLS.businessLogicEOC
                .addAddOnPOonBasicPO("Provision : Update Add On PO in to Basic PO", this, customerInfo, ts, resultInfo)
                .run();

        Steps.PROTOCOLS.businessLogicEOC.submitShoppingCart("Submit shopping cart", this, customerInfo, ts, resultInfo)
                .run();

        Steps.PROTOCOLS.businessLogicEOC.getShoppingCart("Get shopping cart", this, customerInfo, ts, resultInfo).run();

        productId = JsonPath.read(ts.getResponseBody(), "$.items[1].item.product.productId");

        Steps.PROVISION.eoc.repurchaseAddOnPO("Repurchase AddOn PO", this, customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC.getShoppingCart("Get shopping cart", this, customerInfo, ts, resultInfo).run();

        customerInfo.setProductID(productId);

        productId = getProductId(ts, productOfferings.get("addon"), "productId");

        customerInfo.setProductID("1");

        Steps.PROTOCOLS.businessLogicCS
                .getDaAccountBalance("Get DA Account Balance from CS", this, customerInfo, ts, resultInfo).run();
        if (JsonPath.read(ts.getResponseBody(), "$.dedicatedAccountInformation[*].dedicatedAccountID").toString()
                .split(",").length == 2) {
            if (JsonPath.read(ts.getResponseBody(), "$.dedicatedAccountInformation[0].dedicatedAccountID").toString()
                    .equals(JsonPath.read(ts.getResponseBody(), "$.dedicatedAccountInformation[1].dedicatedAccountID")
                            .toString())) {
                Jive.log("Two Offers with Same PO Correctly Active on MSISDN" + customerInfo.getMsisdn());
            }
        }
        // Update DA Acount Balance to 10, to Maintain Balance for running Voice Call
        ts.setDedicatedAccountID(businessCache.get("DA.ACCOUNTID"));
        ts.setDedicatedAcountValue(Integer.parseInt(businessCache.get("DA.VALUE")));
        Steps.PROTOCOLS.businessLogicCS.updateAccountDA("Update DA Account", this, customerInfo, ts, resultInfo).run();

        // Running Voice Call on National Number to check Balance is deducted from DA
        // Account
        ts.setExpectedCost(businessCache.get("USAGE.COST_NATIONAL_1"));
        ts.setCharge(businessCache.get("USAGE.CHARGE_NATIONAL_1"));
        ts.setDuration(Integer.parseInt(businessCache.get("USAGE.DURATION_1")));
        ts.setCalledNumber(Constants.NATIONAL_NUMBER_1);
        runUsageFromDA();

        // Running Voice Call on InterNational Number to check Balance is deducted from
        // DA Account
        ts.setExpectedCost(businessCache.get("USAGE.COST_INTERNATIONAL_1"));
        ts.setCharge(businessCache.get("USAGE.CHARGE_INTERNATIONAL_1"));
        ts.setDuration(Integer.parseInt(businessCache.get("USAGE.DURATION_1")));
        ts.setCalledNumber(Constants.INTERNATIONAL_NUMBER_EUROPE1_NR1);
        runUsageFromDA();

        // Again Update DA Acount Balance to 10, to Maintain Balance for running Voice
        // Call
        ts.setDedicatedAccountID(businessCache.get("DA.ACCOUNTID"));
        ts.setDedicatedAcountValue(Integer.parseInt(businessCache.get("DA.VALUE")));
        Steps.PROTOCOLS.businessLogicCS.updateAccountDA("Update DA Account", this, customerInfo, ts, resultInfo).run();

        // Personalize the Price Parameters of Add On PO
        personalizePriceForDA(priceVoicePerMinuteNational, priceVoicePerMinuteInternational);

        // Running Voice Call to consume all DA Balance
        ts.setExpectedCost(businessCache.get("USAGE.COST_NATIONAL_2"));
        ts.setCharge(businessCache.get("USAGE.CHARGE_NATIONAL_2"));
        ts.setDuration(Integer.parseInt(businessCache.get("USAGE.DURATION_2")));
        ts.setCalledNumber(Constants.NATIONAL_NUMBER_1);
        runUsageFromDA();

        // Deactivate or Delete Add On PO

        deactivateAddOnPO();

        // Check on CS if DA Acount is removed from Customer

        Steps.PROTOCOLS.businessLogicCS
                .getDaAccountBalance("Get DA Account Balance from CS", this, customerInfo, ts, resultInfo).run();
        if (ts.getResponseBody().contains("dedicatedAccountInformation")) {
            Jive.failAndContinue("Add On PO Not Removed Successfully...DA Account Not Removed..Please Check.. Failing...");
        }

        Steps.PROTOCOLS.businessLogicCS.getOffers("Get Offers from CS", this, customerInfo, ts, resultInfo).run();

        // Validate on CS(though Offers) that Add On PO Is successfully Deleted
        if (JsonHelper.checkPoOnOffer(ts, productOfferings.get("addon")) == false) {
            Jive.log("Add ON PO is Correctly Deleted..Offers Removed.. Verified From CS");
        } else {
            Jive.failAndContinue("Add On PO Not Removed Successfully...Offers Not Removed..Please Check.. Failing...");
        }

        // Validate that Add On PO Is successfully Deleted from BSCS
        Steps.PROTOCOLS.businessLogicBSCS
                .getPoStatusBSCS("Read PO Status from BSCS", this, customerInfo, ts, resultInfo).run();

        count = JsonPath.read(ts.getResponseBody(),
                "$.SOAP-ENV:Envelope.SOAP-ENV:Body.productCfsReadResponse.productCfsReadOutputDTO.products.productCfsReadOutputProductDTO[*].productId.content")
                .toString().split(",").length;
        for (int i = 0; i < count; i++) {
            if (JsonPath.read(ts.getResponseBody(),
                    "$.SOAP-ENV:Envelope.SOAP-ENV:Body.productCfsReadResponse.productCfsReadOutputDTO.products.productCfsReadOutputProductDTO["
                            + i + "].productOfferingId.content")
                    .equals(productOfferings.get("addon"))) {
                if ("d".equals(JsonPath.read(ts.getResponseBody(),
                        "$.SOAP-ENV:Envelope.SOAP-ENV:Body.productCfsReadResponse.productCfsReadOutputDTO.products.productCfsReadOutputProductDTO["
                                + i + "].productStatus.content"))) {
                    Jive.log("Add On PO is Deleted Successfully");
                    foundPo = true;
                    break;
                }
            }
        }
        if (!foundPo) {
            Jive.failAndContinue("AddOn PO Not deactivated Successfully");
        }

        // Verify Contract History in BSCS after Deleting PO
        Steps.PROTOCOLS.businessLogicBSCS
                .getContractHistory("Read Contract History from BSCS", this, customerInfo, ts, resultInfo).run();

        contractHistoryString = businessCache.get("CONTRACT.HISTORYSTRING")
                .replace("CONTRACTID", customerInfo.getContractId()).replace("PRODUCTID", customerInfo.getProductId());

        if (!JsonPath
                .read(ts.getResponseBody(),
                        "$.SOAP-ENV:Envelope.SOAP-ENV:Body.ticklersSearchResponse.result.item[*].tickLdes.content")
                .toString().contains(contractHistoryString)) {
            Jive.failAndContinue("Contract Deletion Not Successful as Per Contract History in BSCS");
        } else {
            Jive.log("Contract Deletion Successful as Per Contract History in BSCS");
        }

        // Validation of Bill
        verifyBill();

    }

    private void personalizePriceForDA(String voicePerMinuteNational, String voicePerMinuteInterNational) {
        ts.setJsonFileName("createShoppingCart.json");
        Steps.PROVISION.eoc.createShoppingCart("Provision : Create Shopping Cart For Personalized usage", this,
                customerInfo, ts, resultInfo).run();

        resultInfo = Steps.PROTOCOLS.businessLogicEOC.getShoppingCart(
                "Protocol : Get Shopping Cart For Personalized usage", this, customerInfo, ts, resultInfo).run();

        customerInfo.setProductID(productId);

        String pooiId = getPooiId(ts, String.valueOf(ts.getProductOfferingIds().get("addon")), "id");
        customerInfo.setPooiId(pooiId);

        Steps.PROTOCOLS.businessLogicEOC
                .getPooi("Getting the POOI For Personalized usage", this, customerInfo, ts, resultInfo).run();

        JSONObject getPOOIRequestBody = searchKeyAndUpdateJson(ts.getResponseBody(), "action", "Modify");
        getPOOIRequestBody = requestForPersonalizedPrice(getPOOIRequestBody.toString(), "PriceVoicePerMinuteNational",
                voicePerMinuteNational);
        getPOOIRequestBody = requestForPersonalizedPrice(getPOOIRequestBody.toString(), "PriceVoicePerMinuteInternat",
                voicePerMinuteInterNational);

        ts.setResponseBody(getPOOIRequestBody.toString());

        Steps.PROTOCOLS.businessLogicEOC
                .updatePooi("Provision : Personalization of Price Values", this, customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC.submitShoppingCart("Provision : Submit Shopping Cart For Personalized usage",
                this, customerInfo, ts, resultInfo).run();

    }

    private void runUsageFromDA() {
        int balanceBefore;
        Steps.PROTOCOLS.businessLogicCS
                .getDaAccountBalance("Get DA Account Balance from CS", this, customerInfo, ts, resultInfo).run();
        ts.setDedicatedAccountBalance(Integer.parseInt(
                JsonPath.read(ts.getResponseBody(), "$.dedicatedAccountInformation[0].dedicatedAccountValue1")));
        balanceBefore = ts.getDedicatedAccountBalance().intValue();
        Steps.CHARGING_ONLINE.simulator.onlineCip("Protocol Voice Usage", this, resultInfo, customerInfo, ts).run();
        Steps.PROTOCOLS.businessLogicCS
                .getDaAccountBalance("Get DA Account Balance from CS", this, customerInfo, ts, resultInfo).run();
        ts.setDedicatedAccountBalance(Integer.parseInt(
                JsonPath.read(ts.getResponseBody(), "$.dedicatedAccountInformation[0].dedicatedAccountValue1")));
        if (balanceBefore - ts.getDedicatedAccountBalance().intValue() == Integer.parseInt(ts.getCharge())) {
            Jive.log("Correct Balance Deducted from DA after National Voice Call");
        } else {
            Jive.failAndContinue("InCorrect Balance Deducted from DA after National Voice Call... Please Check");

        }
    }

    private void deactivateAddOnPO() {

        JSONObject jsonObj = null;

        ts.setJsonFileName("createShoppingCart.json");
        Steps.PROVISION.eoc.createShoppingCart("Provision : Create Shopping Cart for Deactivating PO", this,
                customerInfo, ts, resultInfo).run();
        Steps.PROVISION.eoc
                .getShoppingCart("Protocol : Get Shopping Cart Deactivating PO", this, customerInfo, ts, resultInfo)
                .run();
        customerInfo.setProductID(productId);

        customerInfo.setPooiId(getPooiId(ts, productOfferings.get("addon"), "id"));

        Steps.PROTOCOLS.businessLogicEOC.getPooi("Getting the POOI Deactivating PO", this, customerInfo, ts, resultInfo)
                .run();

        jsonObj = JsonHelper.searchKeyAndUpdateJson(ts.getResponseBody(), "action", "Delete");
        ts.setResponseBody(jsonObj.toString());
        Steps.PROTOCOLS.businessLogicEOC
                .updatePooi("Provision : Update POI for Deactivating Add On PO", this, customerInfo, ts, resultInfo)
                .run();

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision : Submit Shopping Cart for Deactivating Add On PO", this, customerInfo,
                        ts, resultInfo)
                .run();

    }

    private void verifyBill() {

        try {

            Thread.sleep(Long.valueOf(businessCache.get("THREAD.SLEEP_INVOICE")));
        } catch (Exception e) {
            Jive.fail("Failed while on Sleep");
        }
        ts.setInvoicePdfPageNo(Integer.parseInt(businessCache.get("INVOICE.PAGE_1")));
        ts.setInvoiceCharge(businessCache.get("INVOICE.CHARGE_1"));
        ts.setJsonFileName("sendBillGenerateRequest.xml");

        Steps.BILL_GENERATION.stepsBilling
                .sendInvoiceRequest("Protocol : Send Bill Process Create Request ", this, resultInfo, customerInfo, ts)
                .run();

        resultInfo = Steps.BILL_GENERATION.stepsBilling.getInvoice(
                "Validation : Get Invoice PDF and Validate Charges in PDF ", this, resultInfo, customerInfo, ts).run();

        if (!resultInfo.getResult().toString().contains(ts.getInvoiceCharge())) {
            Jive.failAndContinue("Invoice One Time Charge In Bill Is Not Correct");
        }
        Jive.log("Invoice One Time Charge Successfully Verified");

        ts.setInvoiceCharge(businessCache.get("INVOICE.CHARGE_2"));
        if (!resultInfo.getResult().toString().contains(ts.getInvoiceCharge())) {
            Jive.failAndContinue("Invoice Total Charge " +businessCache.get("INVOICE.CHARGE_2")+ "In BIll Is Not Correct");
        }
        Jive.log("Invoice Total Charge" +businessCache.get("INVOICE.CHARGE_2")+ "Successfully Verified");
    }

    private String getPooiId(TransactionSpecification tSpec, String poName, String requestedKey) {
        String output = null;

        try {
            JSONObject jsonObject = new JSONObject(tSpec.getResponseBody());
            JSONArray items = jsonObject.getJSONArray("items");

            for (int i = items.length() - 1; i >= 0; i--) {
                JSONObject item = items.getJSONObject(i);
                JSONObject it = item.getJSONObject("item");
                JSONObject po = it.getJSONObject("productOffering");
                JSONObject product = it.getJSONObject("product");
                if (po.get("id").equals(poName)) {
                    if (product.get("productId").equals(customerInfo.getProductId())) {
                        output = it.get(requestedKey).toString();
                        Jive.log("Requested PO : " + poName + " And Product Id : " + product.get("productId"));
                        break;
                    }
                }
            }
            Jive.log("OK : Output For Request Key - " + requestedKey + " :  " + output);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return output;
    }

    private String getProductId(TransactionSpecification tSpec, String poName, String requestedKey) {
        String output = null;
        try {
            JSONObject jsonObject = new JSONObject(tSpec.getResponseBody());
            JSONArray items = jsonObject.getJSONArray("items");

            for (int i = 0; i <= items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                JSONObject it = item.getJSONObject("item");
                JSONObject po = it.getJSONObject("productOffering");
                JSONObject product = it.getJSONObject("product");
                if (po.get("id").equals(poName)) {
                    if (!product.get("productId").equals(customerInfo.getProductId())) {
                        output = product.get(requestedKey).toString();
                        Jive.log("Requested PO : " + poName + " And Product Id : " + product.get("productId"));
                        break;
                    }
                }
            }
            Jive.log("OK : Output For Request Key - " + requestedKey + " :  " + output);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return output;
    }
}
