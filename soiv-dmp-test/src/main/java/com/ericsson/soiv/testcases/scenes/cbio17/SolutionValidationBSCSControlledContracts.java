package com.ericsson.soiv.testcases.scenes.cbio17;

import com.ericsson.jive.core.execution.*;
import com.ericsson.soiv.fixtures.SoivFixture;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.*;
import com.jayway.jsonpath.JsonPath;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.util.LinkedHashMap;

import static com.ericsson.soiv.utils.http.ReadBusinessData.readBusinessCacheData;

// ZKAPSAR Created on 20-May-2019

@Fixture(SoivFixture.class)
public class SolutionValidationBSCSControlledContracts extends SoivTestBase {

    private TestResultInfo resultInfo = new TestResultInfo();
    private CustomerInfo customerInfo = new CustomerInfo();
    private TransactionSpecification ts = new TransactionSpecification();
    private LinkedHashMap<String, String> productOfferings = new LinkedHashMap<String, String>();
    private LinkedHashMap<String, String> businessCache = null;
    private CalculateDateTime calculateDateTime = new CalculateDateTime();

    @Test
    @Id("00000033")
    @Tags("OT-Regression, OT-Postpaid, EC-Controlled, OT-CBiO17")
    @Description("TS750_Solution_Validation_BSCS")
    public void solutionValidationBscsControlledContracts() throws IOException {

        int count;
        businessCache = new LinkedHashMap<>(readBusinessCacheData(this.getTestCase().getId()));

        customerInfo.setCustomerBillCycle(businessCache.get("CUSTOMER.BILLCYCLE"));
        customerInfo.setOrderMode(businessCache.get("ORDER.MODE"));
        customerInfo.setRequester(businessCache.get("ORDER.REQUESTER"));

        productOfferings.put("basic", businessCache.get("PO.ID_BASIC"));
        productOfferings.put("addon", businessCache.get("PO.ID_ADDON"));
        productOfferings.put("basicVoicePO", businessCache.get("PO.ID_BASICVOICE"));
        ts.setProductOfferingIds(productOfferings);

        // Create Contract with with Basic Bundle
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

        ts.setJsonFileName("selectSerialNumber.xml");
        resultInfo = Steps.PROTOCOLS.businessLogicBSCS
                .getFreeSerialNumber("Protocol : Get Free Serial Number", this, customerInfo, ts, resultInfo).run();

        ts.setJsonFileName("selectLogicalResource.xml");
        resultInfo = Steps.PROTOCOLS.businessLogicBSCS
                .selectLogicalResource("Protocol : Get Free MSISDN Number", this, customerInfo, ts, resultInfo).run();

        customerInfo.setMsisdn(customerInfo.getresource().toString());
        ts.setJsonFileName("addProductOfferingShoppingCart.json");
        resultInfo = Steps.PROVISION.eoc
                .addPoInShoppingCart("Provision : Add PO in Shopping Cart", this, customerInfo, ts, resultInfo).run();

        Steps.PROVISION.eoc.getShoppingCart("Get Shooping Cart", this, customerInfo, ts, resultInfo).run();
        customerInfo.setPooiId(JsonHelper.getRequestPooiId(ts, productOfferings.get("basicVoicePO"), "id"));

        Steps.PROTOCOLS.businessLogicEOC.getPooi("Get POI For Basic Voice PO", this, customerInfo, ts, resultInfo)
                .run();
        resultInfo = Steps.PROTOCOLS.businessLogicEOC.updateProductOffering(
                "Provision : Update Basic PO for Basic Voice", this, customerInfo, ts, resultInfo).run();

        // Add AddOn PO
        ts.setJsonFileName("AddOnPOonBasic.json");
        Steps.PROTOCOLS.businessLogicEOC
                .addAddOnPOonBasicPO("Provision : Update Add On PO in to Basic PO", this, customerInfo, ts, resultInfo)
                .run();

        Steps.PROVISION.eoc.getShoppingCart("Get Shooping Cart", this, customerInfo, ts, resultInfo).run();

        System.out.println("shopping cart is " + ts.getResponseBody());

        Steps.PROTOCOLS.businessLogicEOC.submitShoppingCart("Submit shopping cart ", this, customerInfo, ts, resultInfo)
                .run();

        // Suspending the Contract
/*
        ts.setTargetState(businessCache.get("TARGET.STATE_SUSPEND"));
        ts.setActionCode(businessCache.get("ACTION.CODE_SUSPEND"));
        ts.setReasonCode(businessCache.get("REASON.CODE_SUSPEND"));
        ts.setIsAdminChargesEnabled(Boolean.valueOf(businessCache.get("ADMINCHARGES.ENABLE_SUSPEND")));

        Steps.PROVISION.eoc.manageContractStatus("Provision : Suspend Contract ", this, customerInfo, ts, resultInfo)
                .run();

        // Reactivate Contract

        ts.setTargetState(businessCache.get("TARGET.STATE_REACTIVATE"));
        ts.setActionCode(businessCache.get("ACTION.CODE_REACTIVATE"));
        ts.setReasonCode(businessCache.get("REASON.CODE_REACTIVATE"));
        ts.setIsAdminChargesEnabled(Boolean.valueOf(businessCache.get("ADMINCHARGES.ENABLE_REACTIVATE")));

        Steps.PROVISION.eoc
                .manageContractStatus("Provision : ReActivation Contract ", this, customerInfo, ts, resultInfo).run();
*/
        /***************************************
         * OfflineBSCS Voice Call
         ******************************/
        ts.setUdrTextFileName("voice_udr.txt");

        customerInfo.setPlCodePub("EUR01");

        String body = RawMessages.getBodyFromTextFilePlacedInTestStep(this.getClass().getName(),
                "protocols/bscs/udrTemplates", ts.getUdrTextFileName()).toString();

        body = body.replace("{{IMSI}}", customerInfo.getLinkedPortNumber());
        body = body.replace("{{MSISDN}}", customerInfo.getMsisdn());
        body = body.replace("{{DATE}}", calculateDateTime.getCurrentDateTime());
        body = body.replace("{{PLCODE}}", customerInfo.getPlCodePub());
        body = body.replace("{{DURATION}}", "3600");
        body = body.replace("{{NETWORK}}", "GSM");
        body = body.replace("{{TYPE}}", "TEL");

        ts.setResponseBody(body);
        Steps.CHARGING_OFFLINE.stepsChargingOfflineBscs
                .offlineVoice("Perform Some Usage : Online Voice", this, resultInfo, customerInfo, ts).run();

        /*******************************
         * OfflineBSCS Gy Call
         ******************************/

        ts.setUdrTextFileName("data_udr.txt");
        body = RawMessages.getBodyFromTextFilePlacedInTestStep(this.getClass().getName(), "protocols/bscs/udrTemplates",
                ts.getUdrTextFileName()).toString();

        body = body.replace("{{IMSI}}", customerInfo.getLinkedPortNumber());
        body = body.replace("{{MSISDN}}", customerInfo.getMsisdn());
        body = body.replace("{{DATE}}", calculateDateTime.getCurrentDateTime());
        body = body.replace("{{PLCODE}}", customerInfo.getPlCodePub());
        body = body.replace("{{DURATION}}", "102400");
        body = body.replace("{{NETWORK}}", "GSM");
        body = body.replace("{{TYPE}}", "TEL");

        ts.setResponseBody(body);

        Steps.CHARGING_OFFLINE.stepsChargingOfflineBscs
                .offlineGy("Protocol : BSCS - Offline Data Usage", this, resultInfo, customerInfo, ts).run();

        // Verification of Bill
        verifyBill();

        // Add On PO Deactivated
        deactivateAddOnPO("Deactivate Add On PO for " + customerInfo.getCustomerId());

        // Validate that Add On PO Is successfully Deleted
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
                } else {
                    Jive.failAndContinue("Add On PO Not Deleted Sucessfully... Please Check.. Failing");
                }
            }

        }
    }

    private void verifyBill() {

        try {
            Thread.sleep(Long.parseLong(businessCache.get("THREAD.SLEEP_INVOICE")));
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

        String readInvoiceData = (String) resultInfo.getResult();

        if (readInvoiceData.contains("GRAND TOTAL " + ts.getInvoiceCharge())){
            Jive.log("OK : Invoice PDF Charges : " + ts.getInvoiceCharge() + " are Validated Successfully");
        } else {
           Jive.failAndContinue("FAILED : Invoice PDF Charges : " + ts.getInvoiceCharge() + " are Not Validated Successfully!! Please Check the Invoice Content : "
                    + readInvoiceData);
        }
    }

    private void deactivateAddOnPO(String title) {

        Jive.log(title);
        JSONObject jsonObj ;
        ts.setJsonFileName("createShoppingCart.json");
        Steps.PROVISION.eoc.createShoppingCart("Provision : Create Shopping Cart for Deactivating PO", this,
                customerInfo, ts, resultInfo).run();
        Steps.PROVISION.eoc
                .getShoppingCart("Protocol : Get Shopping Cart Deactivating PO", this, customerInfo, ts, resultInfo)
                .run();

        customerInfo.setPooiId(JsonHelper.getRequestedJsonElement(ts, productOfferings.get("addon"), "id"));

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
}
