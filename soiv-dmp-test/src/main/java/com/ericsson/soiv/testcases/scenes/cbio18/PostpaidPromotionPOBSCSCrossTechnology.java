package com.ericsson.soiv.testcases.scenes.cbio18;

import static com.ericsson.soiv.utils.JsonHelper.getRequestedJsonElement;
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
import com.ericsson.soiv.utils.HelperClass;
import com.ericsson.soiv.utils.JsonHelper;
import com.ericsson.soiv.utils.RawMessages;
import com.ericsson.soiv.utils.TestResultInfo;
import com.ericsson.soiv.utils.TransactionSpecification;
import com.jayway.jsonpath.JsonPath;

//ZEAHDCE Created on 02-August-2019

@Fixture(SoivFixture.class)
public class PostpaidPromotionPOBSCSCrossTechnology extends SoivTestBase {

    private TestResultInfo resultInfo = new TestResultInfo();
    private CustomerInfo customerInfo = new CustomerInfo();
    private TransactionSpecification ts = new TransactionSpecification();
    private CalculateDateTime calculateDateTime = new CalculateDateTime();

    @Test
    @Id("00000051")
    @Tags("OT-Regression, OT-Postpaid,BSCS-Controlled, OT-CBiO18")
    @Description("Cross Technology Product Offering with Promotion PO")
    public void postpaidPOBSCScrossTechnology() throws IOException {
        LinkedHashMap<String, String> businessCache = new LinkedHashMap<>(
                readBusinessCacheData(this.getTestCase().getId()));
        customerInfo.setCustomerBillCycle(businessCache.get("CUSTOMER.BILLCYCLE"));
        customerInfo.setOrderMode(businessCache.get("ORDER.MODE"));
        customerInfo.setRequester(businessCache.get("ORDER.REQUESTER"));

        LinkedHashMap<String, String> productOfferings = new LinkedHashMap<String, String>();
        productOfferings.put("basic", businessCache.get("PO.ID_BASIC"));
        productOfferings.put("optional", businessCache.get("PO.ID_OPTIONAL_GSM"));

        ts.setProductOfferingIds(productOfferings);

        /*****************************
         * createCustomerContractBasicPO
         ***************************/

        resultInfo = Steps.PROVISION.bscs
                .createCustomer("Provision : Create Customer", this, customerInfo, ts, resultInfo).run();

        ts.setJsonFileName("createShoppingCart.json");
        resultInfo = Steps.PROVISION.eoc
                .createShoppingCart("Provision : Create Shopping Cart", this, customerInfo, ts, resultInfo).run();

        /****************************
         * Adding the Basic PO in the Cart
         ***********************************/

        customerInfo.setMarketType("ISD");

        customerInfo.setMarket(businessCache.get("BASICPO.MARKET"));
        customerInfo.setSubMarket(businessCache.get("BASICPO.SUB_MARKET"));
        customerInfo.setNetwork(businessCache.get("BASICPO.NETWORK"));

        ts.setJsonFileName("addProductOfferingShoppingCart.json");
        resultInfo = Steps.PROVISION.eoc
                .addPoInShoppingCart("Provision : Add Basic PO in Shopping Cart", this, customerInfo, ts, resultInfo)
                .run();

        /****************************
         * Add the First Optional PO in the Shopping Cart
         ***********************/

        Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart to get Basic PO", this, customerInfo, ts, resultInfo)
                .run();

        String pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("basic")), "id");

        ts.setBasicPoReferenceId(pooiId);

        customerInfo.setMarketType("ISD");

        customerInfo.setMarket(businessCache.get("OPTIONALPO.MARKET"));
        customerInfo.setSubMarket(businessCache.get("OPTIONALPO.SUB_MARKET"));
        customerInfo.setNetwork(businessCache.get("OPTIONALPO.NETWORK"));

        ts.setJsonFileName("AddOptionalPOonBasic.json");
        Steps.PROTOCOLS.businessLogicEOC.addOptionalPOonBasicPO("Provision : Add First Optional PO in to Basic PO",
                this, customerInfo, ts, resultInfo).run();

        /***************************
         * Handling for the resources Serial Number and LinkedPort Number
         ***********************************/

        customerInfo.setSerialNumberType("ISD");

        customerInfo.setPlCodePub(businessCache.get("SERIALNo.PLCODEPUB"));
        customerInfo.setSubmIdPub(businessCache.get("SERIALNo.SUBMIDPUB"));
        customerInfo.setHlCode(businessCache.get("SERIALNo.HLCODE"));

        ts.setJsonFileName("selectSerialNumber.xml");
        Steps.PROTOCOLS.businessLogicBSCS
                .getFreeSerialNumber("Get Free Serial Number and  Port Number", this, customerInfo, ts, resultInfo)
                .run();

        /****************
         * Handling for the resource MSISDN
         **************************************************/

        customerInfo.setLogicalResourceType("GSM");
        customerInfo.setNpCodePub(businessCache.get("MSISDN.NPCODEPUB"));
        customerInfo.setPlCodePub(businessCache.get("MSISDN.PLCODEPUB"));
        customerInfo.setSubmIdPub(businessCache.get("MSISDN.SUBMIDPUB"));
        customerInfo.setHlCode(businessCache.get("MSISDN.HLCODE"));
        customerInfo.setRsCode(businessCache.get("MSISDN.RSCODE"));
        customerInfo.setCsControlled(businessCache.get("MSISDN.CSCONTROLLED"));

        ts.setJsonFileName("selectLogicalResource.xml");
        resultInfo = Steps.PROTOCOLS.businessLogicBSCS
                .selectLogicalResource("Protocol : Get Free MSISDN Number", this, customerInfo, ts, resultInfo).run();

        customerInfo.setMsisdn(customerInfo.getresource().toString());

        /*******************
         * Get the shopping Cart and Update Resources(IMSI,MSISDN and Serial Number) in
         * Optional PO
         **************************************************/

        Steps.PROVISION.eoc.getShoppingCart("Protocol : Get shopping cart for getting Optional PO ", this, customerInfo,
                ts, resultInfo).run();

        pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("optional")), "id");

        customerInfo.setPooiId(pooiId);
        Steps.PROTOCOLS.businessLogicEOC.getPooi("Getting the Optional POOI ", this, customerInfo, ts, resultInfo)
                .run();

        Jive.log("msisdn is " + customerInfo.getMsisdn());
        Jive.log("IMSI is " + customerInfo.getLinkedPortNumber());
        Jive.log("simcardClass " + customerInfo.getSerialNumber());

        JSONObject jsonobject = JsonHelper.updatePOOi(ts.getResponseBody(), "resourceNumber", customerInfo.getMsisdn());

        jsonobject = JsonHelper.updatePOOiResources(jsonobject.toString(), "IMSI", customerInfo.getLinkedPortNumber(),
                "value");

        jsonobject = JsonHelper.updatePOOiResources(jsonobject.toString(), "simCardClass",
                customerInfo.getSerialNumber(), "name");

        Integer sdpId = ts.getSdpId();
        jsonobject = JsonHelper.updatePOOi(jsonobject.toString(), "SDP_ID", sdpId.toString());

        ts.setResponseBody(jsonobject.toString());
        Steps.PROTOCOLS.businessLogicEOC
                .updatePooi("Update the First Optional PO with Resources(IMSI,MSISDN and Serial Number)", this,
                        customerInfo, ts, resultInfo)
                .run();

        /****************************
         * Add the Second Optional PO in the Shopping Cart
         ***********************/

        productOfferings.put("optional", businessCache.get("PO.ID_OPTIONAL_ISDN"));
        ts.setProductOfferingIds(productOfferings);

        Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart To get Basic PO", this, customerInfo, ts, resultInfo)
                .run();

        pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("basic")), "id");

        ts.setBasicPoReferenceId(pooiId);

        customerInfo.setMarketType("ISD");

        customerInfo.setMarket(businessCache.get("OPTIONALPO.MARKET"));
        customerInfo.setSubMarket(businessCache.get("OPTIONALPO.SUB_MARKET"));
        customerInfo.setNetwork(businessCache.get("OPTIONALPO.NETWORK"));

        ts.setJsonFileName("AddOptionalPOonBasic.json");
        Steps.PROTOCOLS.businessLogicEOC.addOptionalPOonBasicPO("Provision : Add Second Optional PO in to Basic PO",
                this, customerInfo, ts, resultInfo).run();

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

        /*****************************
         * Handling for the resource OLID
         ***********************************/

        ts.setJsonFileName("portSearch.xml");
        customerInfo.setSubmIdPub(businessCache.get("OLID.PORT.SUBMIDPUB"));
        customerInfo.setPortNpPub(businessCache.get("OLID.PORT.PORTNPPUB"));
        customerInfo.setPlCodePub(businessCache.get("OLID.PORT.PLCODEPUB"));
        customerInfo.setHlCode(businessCache.get("OLID.PORT.HLCODE"));

        Steps.PROTOCOLS.businessLogicBSCS.portSearch("Search Port Number for OLID ", this, customerInfo, ts, resultInfo)
                .run();
        Long olidNumber = JsonPath.read(ts.getResponseBody(),
                "$.SOAP-ENV:Envelope.SOAP-ENV:Body.portsSearchResponse.returnedPorts.item.portnum.content");
        log("Selected OLID : " + olidNumber);

        customerInfo.setOlidNumber(olidNumber);

        /*******************
         * Get the shopping Cart for optional PO and update Optional PO with
         * resources(ISDN,OLID)
         **************************************************/

        Steps.PROVISION.eoc
                .getShoppingCart("Get shopping cart for updation for resources(ISDN,OLID) in the Second Optional PO ",
                        this, customerInfo, ts, resultInfo)
                .run();

        pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("optional")), "id");

        customerInfo.setPooiId(pooiId);
        Steps.PROTOCOLS.businessLogicEOC.getPooi("Getting the optional POOI ", this, customerInfo, ts, resultInfo)
                .run();

        jsonobject = JsonHelper.updatePOOi(ts.getResponseBody(), "resourceNumber", customerInfo.getIsdn());

        jsonobject = JsonHelper.updatePOOiResources(jsonobject.toString(), "OLID",
                (customerInfo.getOlidNumber()).toString(), "value");

        jsonobject = JsonHelper.updatePOOi(jsonobject.toString(), "mainDirNum",
                businessCache.get("MAINDIRECTORY.NUMBER"));

        ts.setResponseBody(jsonobject.toString());
        Steps.PROTOCOLS.businessLogicEOC.updatePooi("Update the Second Optional PO for resources(ISDN,OLID)", this,
                customerInfo, ts, resultInfo).run();

        /****************************
         * Add the Third Optional PO in the Shopping Cart
         ***********************/

        productOfferings.put("optional", businessCache.get("PO.ID_OPTIONAL_IP_TV"));
        ts.setProductOfferingIds(productOfferings);

        Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart To get Basic PO", this, customerInfo, ts, resultInfo)
                .run();

        pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("basic")), "id");

        ts.setBasicPoReferenceId(pooiId);

        customerInfo.setMarketType("ISD");

        customerInfo.setMarket(businessCache.get("OPTIONALPO.MARKET"));
        customerInfo.setSubMarket(businessCache.get("OPTIONALPO.SUB_MARKET"));
        customerInfo.setNetwork(businessCache.get("OPTIONALPO.NETWORK"));

        ts.setJsonFileName("AddOptionalPOonBasic.json");
        Steps.PROTOCOLS.businessLogicEOC.addOptionalPOonBasicPO("Provision : Add Third Optional PO in to Basic PO",
                this, customerInfo, ts, resultInfo).run();

        /****************************
         * Handling for the resource IAID
         ***********************************/

        ts.setJsonFileName("portSearch.xml");

        customerInfo.setSubmIdPub(businessCache.get("IAID.PORT.SUBMIDPUB"));
        customerInfo.setPortNpPub(businessCache.get("IAID.PORT.PORTNPPUB"));
        customerInfo.setPlCodePub(businessCache.get("IAID.PORT.PLCODEPUB"));
        customerInfo.setHlCode(businessCache.get("IAID.PORT.HLCODE"));

        Steps.PROTOCOLS.businessLogicBSCS.portSearch("Search Port Number for IAI ", this, customerInfo, ts, resultInfo)
                .run();

        String iaidNumber = JsonPath.read(ts.getResponseBody(),
                "$.SOAP-ENV:Envelope.SOAP-ENV:Body.portsSearchResponse.returnedPorts.item.portnum.content");

        customerInfo.setIaidNumber(iaidNumber);

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

        /**************************
         * Update the Third Optional PO with resources(IPV4,IAID) and Submit the
         * Shopping Cart
         ********************/

        Steps.PROVISION.eoc
                .getShoppingCart("Get shopping cart for updation for resources(IPV4,IAID) in the Third Optional PO ",
                        this, customerInfo, ts, resultInfo)
                .run();

        pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("optional")), "id");

        customerInfo.setPooiId(pooiId);
        Steps.PROTOCOLS.businessLogicEOC.getPooi("Getting the optional POOI ", this, customerInfo, ts, resultInfo)
                .run();

        JSONObject jsonObject = JsonHelper.updatePOOiResources(ts.getResponseBody(), "IAI",
                customerInfo.getIaidNumber(), "value");

        jsonObject = JsonHelper.updatePOOiResources(jsonObject.toString(), "IPV4", customerInfo.getIpv4(), "value");
        jsonObject = JsonHelper.updatePOOi(jsonObject.toString(), "deviceID", businessCache.get("DEVICEID"));

        ts.setResponseBody(jsonObject.toString());

        Steps.PROTOCOLS.businessLogicEOC
                .updatePooi("Update the Third Optional PO  for resources(IPV4,IAI and deviceID)", this, customerInfo,
                        ts, resultInfo)
                .run();

        /****************************
         * Add the Fourth Optional PO in the Shopping Cart
         ***********************/

        productOfferings.put("optional", businessCache.get("PO.ID_OPTIONAL_PROMO"));
        ts.setProductOfferingIds(productOfferings);

        Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart To get Basic PO", this, customerInfo, ts, resultInfo)
                .run();

        pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("basic")), "id");

        ts.setBasicPoReferenceId(pooiId);

        customerInfo.setMarketType("ISD");

        customerInfo.setMarket(businessCache.get("OPTIONALPO.MARKET"));
        customerInfo.setSubMarket(businessCache.get("OPTIONALPO.SUB_MARKET"));
        customerInfo.setNetwork(businessCache.get("OPTIONALPO.NETWORK"));

        ts.setJsonFileName("AddOptionalPOonBasic.json");
        Steps.PROTOCOLS.businessLogicEOC.addOptionalPOonBasicPO("Provision : Add Fourth Optional PO in to Basic PO",
                this, customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Submit Shopping cart with One Basic PO and Four Optional PO", this, customerInfo,
                        ts, resultInfo)
                .run();

        /***************************************
         * OfflineBSCS Voice Call with MSISDN
         ******************************/
        ts.setUdrTextFileName("voice_udr.txt");

        String body = RawMessages.getBodyFromTextFilePlacedInTestStep(this.getClass().getName(),
                "protocols/bscs/udrTemplates", ts.getUdrTextFileName()).toString();

        body = body.replace("{{IMSI}}", customerInfo.getLinkedPortNumber());
        body = body.replace("{{MSISDN}}", customerInfo.getMsisdn());
        body = body.replace("{{DATE}}", calculateDateTime.getCurrentDateTime());
        body = body.replace("{{PLCODE}}", customerInfo.getPlCodePub());
        body = body.replace("{{DURATION}}", businessCache.get("CALL.DURATION"));
        body = body.replace("{{NETWORK}}", businessCache.get("CALL.NETWORK"));
        body = body.replace("{{TYPE}}", businessCache.get("CALL.TYPE"));

        ts.setResponseBody(body);

        Steps.CHARGING_OFFLINE.stepsChargingOfflineBscs
                .offlineVoice("Protocol : BSCS - Offline Voicee Usage", this, resultInfo, customerInfo, ts).run();

        /***************************************
         * OfflineBSCS Voice Call with ISDN
         ******************************/
        ts.setUdrTextFileName("voice_udr.txt");

        body = RawMessages.getBodyFromTextFilePlacedInTestStep(this.getClass().getName(), "protocols/bscs/udrTemplates",
                ts.getUdrTextFileName()).toString();

        body = body.replace("{{IMSI}}", customerInfo.getLinkedPortNumber());
        body = body.replace("{{MSISDN}}", customerInfo.getIsdn());
        body = body.replace("{{DATE}}", calculateDateTime.getCurrentDateTime());
        body = body.replace("{{PLCODE}}", customerInfo.getPlCodePub());
        body = body.replace("{{DURATION}}", businessCache.get("CALL.DURATION"));
        body = body.replace("{{NETWORK}}", businessCache.get("CALL.NETWORK"));
        body = body.replace("{{TYPE}}", businessCache.get("CALL.TYPE"));

        ts.setResponseBody(body);
        String searchMSISDN = customerInfo.getMsisdn();
        customerInfo.setMsisdn(customerInfo.getIsdn());

        Steps.CHARGING_OFFLINE.stepsChargingOfflineBscs
                .offlineVoice("Protocol : BSCS - Offline Voicee Usage", this, resultInfo, customerInfo, ts).run();

        /***************************************
         * OfflineBSCS Gy Call with IP
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
         * Verification the charges and Discount in the Bill
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

        if (readInvoiceData.contains("GRAND TOTAL " + ts.getInvoiceCharge())
                && (readInvoiceData.contains(businessCache.get("DISCOUNT.AMOUNT")))) {
            Jive.log("OK : Invoice PDF Charges : " + ts.getInvoiceCharge() + " are Validated Successfully");
        } else {
           Jive.failAndContinue("FAILED : Invoice PDF Charges : "+ ts.getInvoiceCharge() + "are Not Validated Successfully! Please Check the Invoice Content : "
                    + readInvoiceData);
        }
        /*****************************
         * Verification the resources in the Bill
         ******************/
        if ((readInvoiceData.contains(HelperClass.iptoHexaDecimal(customerInfo.getIpv4())))
                && (readInvoiceData.contains(searchMSISDN))
                && (readInvoiceData.contains(customerInfo.getSerialNumber())))
        {
            Jive.log("OK : Invoice PDF Resources IP- " + customerInfo.getIpv4() + " MSISDN- " + customerInfo.getMsisdn()
                    + " SimNumber- " + customerInfo.getSerialNumber() + "Validated Successfully");

        } else {
            Jive.fail("FAILD : Invoice PDF Resources IP- " + customerInfo.getIpv4() + " MSISDN- " + customerInfo.getMsisdn()
                    + " SimNumber- " + customerInfo.getSerialNumber() + "NOT Validated Successfully");
        }

    }
}
