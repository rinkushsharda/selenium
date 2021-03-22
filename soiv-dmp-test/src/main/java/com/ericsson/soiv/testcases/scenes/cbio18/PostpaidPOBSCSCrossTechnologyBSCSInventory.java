package com.ericsson.soiv.testcases.scenes.cbio18;

import com.ericsson.jive.core.execution.*;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpResponse;
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

// Created by ZEAHDCE on 27-June-2019
@Fixture(SoivFixture.class)
public class PostpaidPOBSCSCrossTechnologyBSCSInventory extends SoivTestBase {
    private TestResultInfo resultInfo = new TestResultInfo();
    private CustomerInfo customerInfo = new CustomerInfo();
    private TransactionSpecification ts = new TransactionSpecification();
    private CalculateDateTime calculateDateTime = new CalculateDateTime();

    @Test
    @Id("00000049")
    @Tags("OT-Regression, OT-Postpaid, BSCS-Controlled, OT-CBiO18")
    @Description("Cross Technology Product Offering BSCS Inventory")
    public void postpaidPOBSCScrossTechnology() throws IOException {

        LinkedHashMap<String, String> businessCache = new LinkedHashMap<>(
                readBusinessCacheData(this.getTestCase().getId()));
        customerInfo.setCustomerBillCycle(businessCache.get("CUSTOMER.BILLCYCLE"));
        customerInfo.setOrderMode(businessCache.get("ORDER.MODE"));
        customerInfo.setRequester(businessCache.get("ORDER.REQUESTER"));

        LinkedHashMap<String, String> productOfferings = new LinkedHashMap<String, String>();
        productOfferings.put("basic", businessCache.get("PO.ID_BASIC"));
        productOfferings.put("optional", businessCache.get("PO.ID_OPTIONAL"));

        ts.setProductOfferingIds(productOfferings);

        /*****************************
         * createCustomerContractBasicPO
         ***************************/

        resultInfo = Steps.PROVISION.bscs
                .createCustomer("Provision : Create Customer", this, customerInfo, ts, resultInfo).run();

        ts.setJsonFileName("createShoppingCart.json");
        resultInfo = Steps.PROVISION.eoc
                .createShoppingCart("Provision : Create Shopping Cart", this, customerInfo, ts, resultInfo).run();

        /***************************
         * Handling for the resource ISDN
         ***********************************/

        customerInfo.setLogicalResourceType("ISDN");
        customerInfo.setNpCodePub(businessCache.get("ISDN.NPCODEPUB"));
        customerInfo.setPlCodePub(businessCache.get("ISDN.PLCODEPUB"));
        customerInfo.setSubmIdPub(businessCache.get("ISDN.SUBMIDPUB"));
        customerInfo.setHlCode(businessCache.get("ISDN.HLCODE"));
        customerInfo.setRsCode(businessCache.get("ISDN.RSCODE"));
        customerInfo.setCsControlled(businessCache.get("ISDN.CSCONTROLLED"));

        ts.setJsonFileName("selectLogicalResource.xml");
        resultInfo = Steps.PROTOCOLS.businessLogicBSCS
                .selectLogicalResource("Protocol : Get Free ISDN Number", this, customerInfo, ts, resultInfo).run();

        customerInfo.setIsdn(customerInfo.getresource().toString());

        /****************************
         * Handling for the resource IPV4
         ***********************************/

        customerInfo.setNpCodePub(businessCache.get("IPV4.NPCODEPUB"));
        customerInfo.setPlCodePub(businessCache.get("IPV4.PLCODEPUB"));
        customerInfo.setSubmIdPub(businessCache.get("IPV4.SUBMIDPUB"));
        customerInfo.setHlCode(businessCache.get("IPV4.HLCODE"));
        customerInfo.setRsCode(businessCache.get("IPV4.RSCODE"));
        customerInfo.setCsControlled(businessCache.get("IPV4.CSCONTROLLED"));

        ts.setJsonFileName("selectLogicalResource.xml");
        resultInfo = Steps.PROTOCOLS.businessLogicBSCS
                .selectLogicalResource("Protocol : Get Free IPV4 ", this, customerInfo, ts, resultInfo).run();

        customerInfo.setIpv4(customerInfo.getresource().toString());

        /***************************
         * Handling for the resource Serial Number and LinkedPort Number
         ***********************************/

        customerInfo.setSerialNumberType("ISD");

        customerInfo.setPlCodePub(businessCache.get("SERIALNo.PLCODEPUB"));
        customerInfo.setSubmIdPub(businessCache.get("SERIALNo.SUBMIDPUB"));
        customerInfo.setHlCode(businessCache.get("SERIALNo.HLCODE"));

        ts.setJsonFileName("selectSerialNumber.xml");
        Steps.PROTOCOLS.businessLogicBSCS
                .getFreeSerialNumber("Get Free Serial Number and Port Number", this, customerInfo, ts, resultInfo)
                .run();

        /****************************
         * Handling for the resource IAID
         ***********************************/

        ts.setJsonFileName("portSearch.xml");

        customerInfo.setSubmIdPub(businessCache.get("PORT.SUBMIDPUB"));
        customerInfo.setPortNpPub(businessCache.get("PORT.PORTNPPUB"));
        customerInfo.setPlCodePub(businessCache.get("PORT.PLCODEPUB"));
        customerInfo.setHlCode(businessCache.get("PORT.HLCODE"));

        Steps.PROTOCOLS.businessLogicBSCS.portSearch("Search Port Number for IAI ", this, customerInfo, ts, resultInfo)
                .run();

        String iaidNumber = JsonPath.read(ts.getResponseBody(),
                "$.SOAP-ENV:Envelope.SOAP-ENV:Body.portsSearchResponse.returnedPorts.item.portnum.content");

        customerInfo.setIaidNumber(iaidNumber);

        /****************************
         * Adding the Basic PO in the Cart
         ***********************************/
        customerInfo.setMarketType("ISD");

        customerInfo.setMarket(businessCache.get("BASICPO.MARKET"));
        customerInfo.setSubMarket(businessCache.get("BASICPO.SUB_MARKET"));
        customerInfo.setNetwork(businessCache.get("BASICPO.NETWORK"));

        ts.setJsonFileName("addProductOfferingShoppingCart.json");
        resultInfo = Steps.PROVISION.eoc
                .addPoInShoppingCart("Provision : Add PO in Shopping Cart", this, customerInfo, ts, resultInfo).run();

        HttpResponse response = (HttpResponse) resultInfo.getResult();
        String respBody = (String) response.getBody().getValue();
        String basicPoID = JsonPath.read(respBody, "$.id");
        ts.setBasicPoReferenceId(basicPoID);

        /****************************
         * Updating the Basic PO with the resources IAID,IPV4 and deviceID
         ***********************************/

        JSONObject jsonObject = JsonHelper.updatePOOiResources(ts.getResponseBody(), "IAI",
                customerInfo.getIaidNumber(), "value");

        jsonObject = JsonHelper.updatePOOiResources(jsonObject.toString(), "IPV4", customerInfo.getIpv4(), "value");

        jsonObject = JsonHelper.updatePOOi(jsonObject.toString(), "deviceID", "SetupBox");

        ts.setResponseBody(jsonObject.toString());

        Steps.PROTOCOLS.businessLogicEOC.updatePooi("updating the basic PO  for resources(IPV4,IAI and deviceID)", this,
                customerInfo, ts, resultInfo).run();

        /****************************
         * Adding the Optional PO in the Cart
         ***********************************/

        customerInfo.setMarketType("ISD");

        customerInfo.setMarket(businessCache.get("OPTIONALPO.MARKET"));
        customerInfo.setSubMarket(businessCache.get("OPTIONALPO.SUB_MARKET"));
        customerInfo.setNetwork(businessCache.get("OPTIONALPO.NETWORK"));

        ts.setJsonFileName("AddOptionalPOonBasic.json");
        Steps.PROTOCOLS.businessLogicEOC.addOptionalPOonBasicPO("Provision : Add Optional PO in to Basic PO", this,
                customerInfo, ts, resultInfo).run();

        /***************************
         * Updating the Optional PO with the resources IMSI,ISDN and simCardClass and
         * Submit the cart
         ***********************************/

        JSONObject jsonobject = JsonHelper.updatePOOi(ts.getResponseBody(), "resourceNumber", customerInfo.getIsdn());

        jsonobject = JsonHelper.updatePOOiResources(jsonobject.toString(), "IMSI", customerInfo.getLinkedPortNumber(),
                "value");

        jsonobject = JsonHelper.updatePOOiResources(jsonobject.toString(), "simCardClass",
                customerInfo.getSerialNumber(), "name");

        ts.setResponseBody(jsonobject.toString());
        Steps.PROTOCOLS.businessLogicEOC
                .updatePooi("updating the Optional  PO for resources(ISDN,IMSI and Serial Number)", this, customerInfo,
                        ts, resultInfo)
                .run();

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("submit cart for Resources", this, customerInfo, ts, resultInfo).run();

        /***************************************
         * OfflineBSCS Voice Call
         ******************************/
        ts.setUdrTextFileName("voice_udr.txt");

        String body = RawMessages.getBodyFromTextFilePlacedInTestStep(this.getClass().getName(),
                "protocols/bscs/udrTemplates", ts.getUdrTextFileName()).toString();

        body = body.replace("{{IMSI}}", customerInfo.getLinkedPortNumber());
        body = body.replace("{{MSISDN}}", customerInfo.getIsdn());
        body = body.replace("{{DATE}}", calculateDateTime.getCurrentDateTime());
        body = body.replace("{{PLCODE}}", customerInfo.getPlCodePub());
        body = body.replace("{{DURATION}}", "0");
        body = body.replace("{{NETWORK}}", "ISD");
        body = body.replace("{{TYPE}}", "CBBMV");

        ts.setResponseBody(body);

        customerInfo.setMsisdn(customerInfo.getIsdn());

        Steps.CHARGING_OFFLINE.stepsChargingOfflineBscs
                .offlineVoice("Protocol : BSCS - Offline Voicee Usage", this, resultInfo, customerInfo, ts).run();

        /***************************************
         * OfflineBSCS Gy Call
         ******************************/

        ts.setUdrTextFileName("ip_udr.txt");
        body = RawMessages.getBodyFromTextFilePlacedInTestStep(this.getClass().getName(), "protocols/bscs/udrTemplates",
                ts.getUdrTextFileName()).toString();

        body = body.replace("{{IPV4}}", HelperClass.iptoHexaDecimal(customerInfo.getIpv4()));
        body = body.replace("{{PORT}}", customerInfo.getIaidNumber());
        body = body.replace("{{DATE}}", calculateDateTime.getCurrentDateTime());
        ts.setResponseBody(body);

        Steps.CHARGING_OFFLINE.stepsChargingOfflineBscs
                .offlineGy("Protocol : BSCS - Offline Data Usage", this, resultInfo, customerInfo, ts).run();

        /*****************************
         * Verification the charges in the Bill
         ******************/

        ts.setInvoiceCharge(businessCache.get("INVOICE.CHARGE"));
        ts.setInvoicePdfPageNo(Integer.parseInt(businessCache.get("INVOICE.PAGENO_1")));
        ts.setRemoveInvoicePdfFile(Boolean.getBoolean(businessCache.get("INVOICE.REMOVE")));
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
           Jive.failAndContinue("FAILED : Invoice PDF Charges : "+ ts.getInvoiceCharge() +" are Not Validated Successfully! Please Check the Invoice Content : "
                    + readInvoiceData);
        }

        /*****************************
         * Verification the resources in the Bill
         ******************/

        ts.setInvoicePdfPageNo(Integer.parseInt(businessCache.get("INVOICE.PAGENO_3")));
        resultInfo = Steps.BILL_GENERATION.stepsBilling.getInvoice(
                "Validation : Get Invoice PDF and Validate Resources in PDF", this, resultInfo, customerInfo, ts).run();
        readInvoiceData = (String) resultInfo.getResult();
        if ((readInvoiceData.contains(HelperClass.iptoHexaDecimal(customerInfo.getIpv4())))
                && (readInvoiceData.contains(customerInfo.getIsdn() ))
                && (readInvoiceData.contains(customerInfo.getSerialNumber())))
        {
            Jive.log("OK : Invoice PDF Resources IP- " + customerInfo.getIpv4() + " isdn- " + customerInfo.getIsdn()
                    + " SimNumber- " + customerInfo.getSerialNumber() + "Validated Successfully");

        } else {
           Jive.failAndContinue("FAILED : Invoice PDF Resources IP- " + customerInfo.getIpv4() + " isdn- " + customerInfo.getIsdn()
                    + " SimNumber- " + customerInfo.getSerialNumber() + "NOT Validated Successfully");
        }
    }
}
