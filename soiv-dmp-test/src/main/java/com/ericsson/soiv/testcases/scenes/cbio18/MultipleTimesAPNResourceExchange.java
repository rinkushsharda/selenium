package com.ericsson.soiv.testcases.scenes.cbio18;

import static com.ericsson.soiv.utils.JsonHelper.getRequestedJsonElement;
import static com.ericsson.soiv.utils.JsonHelper.searchKeyAndUpdateJson;
import static com.ericsson.soiv.utils.http.ReadBusinessData.readBusinessCacheData;

import java.io.IOException;
import java.util.LinkedHashMap;

import org.json.JSONObject;
import org.junit.Test;

import com.ericsson.jive.core.execution.Description;
import com.ericsson.jive.core.execution.Fixture;
import com.ericsson.jive.core.execution.Id;
import com.ericsson.jive.core.execution.Jive;
import com.ericsson.jive.core.execution.Tags;
import com.ericsson.soiv.fixtures.SoivFixture;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.CalculateDateTime;
import com.ericsson.soiv.utils.CustomerInfo;
import com.ericsson.soiv.utils.JsonHelper;
import com.ericsson.soiv.utils.RawMessages;
import com.ericsson.soiv.utils.TestResultInfo;
import com.ericsson.soiv.utils.TransactionSpecification;
import com.jayway.jsonpath.JsonPath;

//Created By EMODRED 6-Aug-2019

@Fixture (SoivFixture.class)
public class MultipleTimesAPNResourceExchange extends SoivTestBase
{
    private TestResultInfo resultInfo = new TestResultInfo();
    private CustomerInfo customerInfo = new CustomerInfo();
    private TransactionSpecification ts = new TransactionSpecification();
    private LinkedHashMap<String, String> productOfferings = new LinkedHashMap<String, String>();
    private CalculateDateTime calculateDateTime = new CalculateDateTime();

    @Test
    @Id ("00000071")
    @Tags ("OT-Regression, OT-Postpaid, BSCS-Controlled, OT-CBiO18")
    @Description ("Logical Resource Exchange GSM Technology with MSISDN Resource and APN Rating 22B_1")
    public void multipleTimesAPNResourceExchange() throws IOException
    {
        LinkedHashMap<String, String> businessCache = new LinkedHashMap<>(
                readBusinessCacheData(this.getTestCase().getId()));

        customerInfo.setCustomerBillCycle(businessCache.get("CUSTOMER.BILLCYCLE"));
        customerInfo.setOrderMode(businessCache.get("ORDER.MODE"));
        customerInfo.setRequester(businessCache.get("ORDER.REQUESTER"));
        ts.setInvoicePdfPageNo(Integer.parseInt(businessCache.get("INVOICE.PAGE_1")));
        ts.setRemoveInvoicePdfFile(Boolean.getBoolean(businessCache.get("INVOICE.REMOVE")));

        productOfferings.put("basic", businessCache.get("PO.ID_BASIC_APN"));
        ts.setProductOfferingIds(productOfferings);

        /******************************
         * createCustomerContractBasicPO
         ***************************/

        Steps.PROVISION.bscs.createCustomer("Provision : Create Customer", this, customerInfo, ts, resultInfo).run();
        ts.setJsonFileName("createShoppingCart.json");
        Steps.PROVISION.eoc.createShoppingCart("Provision : Create Shopping Cart", this, customerInfo, ts, resultInfo)
                .run();

        /***************************
         * Handling for the resource GSM-APN
         ***********************************/

        customerInfo.setLogicalResourceType("APN");
        customerInfo.setNpCodePub(businessCache.get("APN.NPCODEPUB"));
        customerInfo.setPlCodePub(businessCache.get("APN.PLCODEPUB"));
        customerInfo.setSubmIdPub(businessCache.get("APN.SUBMIDPUB"));
        customerInfo.setHlCode(businessCache.get("APN.HLCODE"));
        customerInfo.setRsCode(businessCache.get("APN.RSCODE"));
        customerInfo.setCsControlled(businessCache.get("APN.CSCONTROLLED"));

        ts.setJsonFileName("selectLogicalResource.xml");
        resultInfo = Steps.PROTOCOLS.businessLogicBSCS
                .selectLogicalResource("Protocol : Get Free GSM-APN Number", this, customerInfo, ts, resultInfo)
                .run();
        log("Selected APN : " + customerInfo.getresource().toString());
        customerInfo.setAPN(customerInfo.getresource().toString());

        /***************************
         * Handling for the resource Serial Number and LinkedPort Number
         ***********************************/

        customerInfo.setSerialNumberType("GSM");
        ts.setJsonFileName("selectSerialNumber.xml");
        Steps.PROTOCOLS.businessLogicBSCS.getFreeSerialNumber("Get Free Serial Number and Port Number for APN", this,
                customerInfo, ts, resultInfo).run();

        /****************************
         * Adding the Optional PO in the Cart
         ***********************************/

        customerInfo.setMarketType("GSM");
        ts.setJsonFileName("addProductOfferingShoppingCart.json");
        resultInfo = Steps.PROVISION.eoc
                .addPoInShoppingCart("Provision : Add PO in Shopping Cart", this, customerInfo, ts, resultInfo)
                .run();

        JSONObject jsonobject = JsonHelper.updatePOOi(ts.getResponseBody(), "resourceNumber", customerInfo.getAPN());
        jsonobject = JsonHelper.updatePOOi(jsonobject.toString(), "IMSI", customerInfo.getLinkedPortNumber());
        jsonobject = JsonHelper.updatePOOi(jsonobject.toString(), "serialNumber", customerInfo.getSerialNumber());

        ts.setResponseBody(jsonobject.toString());
        Steps.PROTOCOLS.businessLogicEOC
                .updatePooi("Provision : Update the basic PO with APN resource", this, customerInfo, ts, resultInfo)
                .run();

        Steps.PROTOCOLS.businessLogicEOC.submitShoppingCart(
                "Provision : Submit Shopping Cart For above Customer : " + customerInfo.getCustomerId(), this,
                customerInfo, ts, resultInfo).run();

        /* ===Usage for GSM APN==== */

        ts.setUdrTextFileName("BSCS_APN_UDR.txt");

        String body = RawMessages.getBodyFromTextFilePlacedInTestStep(this.getClass().getName(),
                "protocols/bscs/udrTemplates", ts.getUdrTextFileName()).toString();
        body = body.replace("{{IMSI}}", customerInfo.getLinkedPortNumber());
        body = body.replace("{{APN}}", customerInfo.getAPN());
        body = body.replace("{{DATE}}", calculateDateTime.getCurrentDateTime());
        ts.setResponseBody(body);

        Steps.CHARGING_OFFLINE.stepsChargingOfflineBscs
                .offlineGy("Protocol : BSCS - Offline Usage", this, resultInfo, customerInfo, ts)
                .run();

        /* ============Apn Exchange for the first time=============== */

        /***************************
         * Handling for the resource GSM-APN
         ***********************************/

        customerInfo.setLogicalResourceType("APN");
        customerInfo.setNpCodePub(businessCache.get("APN.NPCODEPUB"));
        customerInfo.setPlCodePub(businessCache.get("APN.PLCODEPUB"));
        customerInfo.setSubmIdPub(businessCache.get("APN.SUBMIDPUB"));
        customerInfo.setHlCode(businessCache.get("APN.HLCODE"));
        customerInfo.setRsCode(businessCache.get("APN.RSCODE"));
        customerInfo.setCsControlled(businessCache.get("APN.CSCONTROLLED"));

        ts.setJsonFileName("selectLogicalResource.xml");
        resultInfo = Steps.PROTOCOLS.businessLogicBSCS.selectLogicalResource(
                "Protocol : Get Free GSM-APN Number for 1st exchange", this, customerInfo, ts, resultInfo).run();
        log("Selected APN for Exchange 1: " + customerInfo.getresource().toString());
        customerInfo.setAPN(customerInfo.getresource().toString());

        /* ===========Exchanging Resources==================== */
        ts.setJsonFileName("createShoppingCart.json");
        Steps.PROVISION.eoc
                .createShoppingCart("Provision : Creating Shopping Cart with existing Customer", this, customerInfo, ts, resultInfo)
                .run();
        Steps.PROTOCOLS.businessLogicEOC.getShoppingCart("Protocol : Get Shopping Cart To get Optional POOI", this,
                customerInfo, ts, resultInfo).run();

        /* ===APN=== */
        String pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("basic")), "id");
        customerInfo.setPooiId(pooiId);
        Steps.PROTOCOLS.businessLogicEOC
                .getPooi("Getting the POOI To add the exchange Resource Number", this, customerInfo, ts, resultInfo)
                .run();
        JSONObject getPOOIRequestBody = searchKeyAndUpdateJson(ts.getResponseBody(), "action", "Modify");
        ts.setResponseBody(getPOOIRequestBody.toString());
        JSONObject jsonObject = JsonHelper.updatePOOi(ts.getResponseBody(), "resourceNumberExchange",
                customerInfo.getAPN());
        ts.setResponseBody(jsonObject.toString());
        Steps.PROTOCOLS.businessLogicEOC
                .updatePooi("Provision : Update the Optional POOI with Exchange Resource Number", this, customerInfo,
                        ts, resultInfo)
                .run();
        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision : Submit Shopping Cart  with Exchange resource Number", this,
                        customerInfo, ts, resultInfo)
                .run();
        /* ===Usage for GSM APN==== */

        ts.setUdrTextFileName("BSCS_APN_UDR.txt");

        body = RawMessages.getBodyFromTextFilePlacedInTestStep(this.getClass().getName(), "protocols/bscs/udrTemplates",
                ts.getUdrTextFileName()).toString();
        body = body.replace("{{IMSI}}", customerInfo.getLinkedPortNumber());
        body = body.replace("{{APN}}", customerInfo.getAPN());
        body = body.replace("{{DATE}}", calculateDateTime.getCurrentDateTime());
        ts.setResponseBody(body);

        Steps.CHARGING_OFFLINE.stepsChargingOfflineBscs
                .offlineGy("Protocol : BSCS - Offline Usage for 1st APN", this, resultInfo, customerInfo, ts)
                .run();

        /* ============Apn Exchange for the 2nd time=============== */

        /***************************
         * Handling for the resource GSM-APN
         ***********************************/

        customerInfo.setLogicalResourceType("APN");
        customerInfo.setNpCodePub(businessCache.get("APN.NPCODEPUB"));
        customerInfo.setPlCodePub(businessCache.get("APN.PLCODEPUB"));
        customerInfo.setSubmIdPub(businessCache.get("APN.SUBMIDPUB"));
        customerInfo.setHlCode(businessCache.get("APN.HLCODE"));
        customerInfo.setRsCode(businessCache.get("APN.RSCODE"));
        customerInfo.setCsControlled(businessCache.get("APN.CSCONTROLLED"));

        ts.setJsonFileName("selectLogicalResource.xml");
        resultInfo = Steps.PROTOCOLS.businessLogicBSCS.selectLogicalResource(
                "Protocol : Get Free GSM-APN Number for 2nd Exchange ", this, customerInfo, ts, resultInfo).run();
        log("Selected APN for Exchange 2: " + customerInfo.getresource().toString());
        customerInfo.setAPN(customerInfo.getresource().toString());

        /* ===========Exchanging Resources==================== */
        ts.setJsonFileName("createShoppingCart.json");
        Steps.PROVISION.eoc
                .createShoppingCart("Provision : Creating Shopping Cart with existing Customer", this, customerInfo, ts, resultInfo)
                .run();
        Steps.PROTOCOLS.businessLogicEOC.getShoppingCart("Protocol : Get Shopping Cart To get Optional POOI", this,
                customerInfo, ts, resultInfo).run();

        /* ===APN=== */
        pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("basic")), "id");
        customerInfo.setPooiId(pooiId);
        Steps.PROTOCOLS.businessLogicEOC
                .getPooi("Getting the POOI To add the exchange Resource Number", this, customerInfo, ts, resultInfo)
                .run();
        getPOOIRequestBody = searchKeyAndUpdateJson(ts.getResponseBody(), "action", "Modify");
        ts.setResponseBody(getPOOIRequestBody.toString());
        jsonObject = JsonHelper.updatePOOi(ts.getResponseBody(), "resourceNumberExchange", customerInfo.getAPN());
        ts.setResponseBody(jsonObject.toString());
        Steps.PROTOCOLS.businessLogicEOC
                .updatePooi("Provision : Update the Optional POOI with Exchange Resource Number", this, customerInfo,
                        ts, resultInfo)
                .run();
        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision : Submit Shopping Cart with Exchange resource Number", this,
                        customerInfo, ts, resultInfo)
                .run();
        /* ===Usage for GSM APN==== */

        ts.setUdrTextFileName("BSCS_APN_UDR.txt");

        body = RawMessages.getBodyFromTextFilePlacedInTestStep(this.getClass().getName(), "protocols/bscs/udrTemplates",
                ts.getUdrTextFileName()).toString();
        body = body.replace("{{IMSI}}", customerInfo.getLinkedPortNumber());
        body = body.replace("{{APN}}", customerInfo.getAPN());
        body = body.replace("{{DATE}}", calculateDateTime.getCurrentDateTime());
        ts.setResponseBody(body);

        Steps.CHARGING_OFFLINE.stepsChargingOfflineBscs
                .offlineGy("Protocol : BSCS - Offline Usage for 2nd APN", this, resultInfo, customerInfo, ts)
                .run();
        ts.setInvoiceCharge(businessCache.get("INVOICE.CHARGE_APN"));
        ts.setJsonFileName("sendBillGenerateRequest.xml");
        Steps.BILL_GENERATION.stepsBilling
                .sendInvoiceRequest("Protocol : Send Bill Process Create Request", this, resultInfo, customerInfo, ts)
                .run();

        resultInfo = Steps.BILL_GENERATION.stepsBilling
                .getInvoice("Validation : Get Invoice PDF and Validate GRAND TOTAL in PDF", this, resultInfo,
                        customerInfo, ts)
                .run();
        String readInvoiceData = (String) resultInfo.getResult();

        if (readInvoiceData.contains("GRAND TOTAL " + ts.getInvoiceCharge()))
        {
            Jive.log("OK : Invoice PDF Charges : " + ts.getInvoiceCharge() + " are Validated Successfully");
        }
        else
        {
           Jive.failAndContinue("FAILED : Invoice PDF Charges : " + ts.getInvoiceCharge() + " are Not Validated Successfully!! Please Check the Invoice Content : "
                    + readInvoiceData);
        }

    }

}
