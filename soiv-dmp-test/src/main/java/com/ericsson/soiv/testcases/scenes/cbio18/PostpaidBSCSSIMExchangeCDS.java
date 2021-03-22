package com.ericsson.soiv.testcases.scenes.cbio18;

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
import static com.ericsson.soiv.utils.JsonHelper.*;
import static com.ericsson.soiv.utils.http.ReadBusinessData.readBusinessCacheData;

//Created By ZKAPSAR 31st-July-2019

@Fixture(SoivFixture.class)
public class PostpaidBSCSSIMExchangeCDS extends SoivTestBase {
    private TestResultInfo resultInfo = new TestResultInfo();
    private CustomerInfo customerInfo = new CustomerInfo();
    private TransactionSpecification ts = new TransactionSpecification();
    private LinkedHashMap<String,String> businessCache = null;
    private CalculateDateTime calculateDateTime = new CalculateDateTime();

    @Test
    @Id("00000041")
    @Tags("OT-Regression, OT-Postpaid, EC-Controlled, OT-CBiO18")
    @Description("SIM Exchange BSCS Controlled BUC22B_2")
    public void postpaidBscsSimExchangeCds() throws IOException, InterruptedException {

        JSONObject body = null;
        String udrBody=null;
        businessCache = new LinkedHashMap<>(readBusinessCacheData(this.getTestCase().getId()));
        String[] isdn =new String [Integer.parseInt(businessCache.get("SEARCH.COUNT"))];

        customerInfo.setCustomerBillCycle(businessCache.get("CUSTOMER.BILLCYCLE"));
        customerInfo.setOrderMode(businessCache.get("ORDER.MODE"));
        customerInfo.setRequester(businessCache.get("ORDER.REQUESTER"));
        ts.setInvoicePdfPageNo(Integer.parseInt(businessCache.get("INVOICE.PAGENO_1")));
        ts.setRemoveInvoicePdfFile(Boolean.valueOf(businessCache.get("INVOICE.REMOVE")));

        LinkedHashMap<String, String> productOfferings = new LinkedHashMap<String, String>();
        productOfferings.put("basic", businessCache.get("PO.ID_BASIC"));
        productOfferings.put("optional", businessCache.get("PO.ID_OPTIONAL"));

        customerInfo.setMarketType(businessCache.get("MARKET.TYPE"));
        customerInfo.setMarket(businessCache.get("BASICPO.MARKET"));
        customerInfo.setSubMarket(businessCache.get("BASICPO.SUB_MARKET"));

        customerInfo.setNetwork(businessCache.get("BASICPO.NETWORK"));
        customerInfo.setSerialNumberType(businessCache.get("SIM.NUMBERTYPE"));
        customerInfo.setSearchCount(Integer.parseInt(businessCache.get("SEARCH.COUNT")));

        customerInfo.setLogicalResourceType("CDS");
        customerInfo.setNpCodePub(businessCache.get("CDS.NPCODEPUB"));
        customerInfo.setPlCodePub(businessCache.get("CDS.PLCODEPUB"));
        customerInfo.setSubmIdPub(businessCache.get("CDS.SUBMIDPUB"));
        customerInfo.setHlCode(businessCache.get("CDS.HLCODE"));
        customerInfo.setRsCode(businessCache.get("CDS.RSCODE"));
        customerInfo.setCsControlled(businessCache.get("CDS.CSCONTROLLED"));

//Contracting with Basic PO
        ts.setProductOfferingIds(productOfferings);
        resultInfo = Steps.PROVISION.bscs
                .createCustomer("Provision : Create Customer", this, customerInfo, ts, resultInfo).run();

        ts.setJsonFileName("createShoppingCart.json");
        resultInfo = Steps.PROVISION.eoc
                .createShoppingCart("Provision : Create Shopping Cart", this, customerInfo, ts, resultInfo).run();

        ts.setJsonFileName("addProductOfferingShoppingCart.json");
        resultInfo = Steps.PROVISION.eoc.addPoInShoppingCart("Provision : Add PO in Shopping Cart", this, customerInfo, ts, resultInfo).run();
        String respBody=ts.getResponseBody();

//Getting multiple ISDN IMSI SIM
        ts.setJsonFileName("selectLogicalResource.xml");
        resultInfo = Steps.PROTOCOLS.businessLogicBSCS
                .selectLogicalResource("Protocol : Get Free ISDN Number", this, customerInfo, ts, resultInfo).run();

         isdn[0] = JsonPath.read(ts.getResponseBody(),
                "$.SOAP-ENV:Envelope.SOAP-ENV:Body.genericDirectoryNumberSearchResponse.directorynumbers.item[0].dirnum.content").toString();

        isdn[1] = JsonPath.read(ts.getResponseBody(),
                "$.SOAP-ENV:Envelope.SOAP-ENV:Body.genericDirectoryNumberSearchResponse.directorynumbers.item[1].dirnum.content").toString();
        customerInfo.setIsdn(isdn[0]);

        Jive.log("Primary ISDN "+isdn[0]);

        ts.setJsonFileName("selectSerialNumber.xml");
        Steps.PROTOCOLS.businessLogicBSCS.getFreeSerialNumber("Get three Free Serial Number and three Port Number",
                this, customerInfo, ts, resultInfo).run();
        String[] linkedPorts = getResources(customerInfo.getLinkedPortNumber());

        String[] serialNumbers = getResources(customerInfo.getSerialNumber());

        customerInfo.setMsisdn(customerInfo.getIsdn());
        customerInfo.setSerialNumber(serialNumbers[0]);
        customerInfo.setLinkedPortNumber(linkedPorts[0]);

        ts.setResponseBody(respBody);
        resultInfo = Steps.PROTOCOLS.businessLogicEOC.getShoppingCart("Get Shopping Cart", this, customerInfo, ts, resultInfo)
                .run();
        Steps.PROTOCOLS.businessLogicEOC.getPooi("Get Basic Poi",this,customerInfo,ts,resultInfo).run();

//Adding in Basic PO
        body = JsonHelper.updatePOOiResources(ts.getResponseBody(), "E.164",  customerInfo.getMsisdn(),"value");
        body = JsonHelper.updatePOOiResources(body.toString(), "IMSI",  customerInfo.getLinkedPortNumber(),"value");
        body = JsonHelper.updatePOOi(body.toString(), "deviceID",  customerInfo.getSerialNumber());
        body = JsonHelper.updatePOOi(body.toString(), "mainDirNum",  "false");

        ts.setResponseBody(body.toString());
        Steps.PROTOCOLS.businessLogicEOC.updatePooi("Update Basic Poi",this,customerInfo,ts,resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision : Submit Shopping Cart For Customer with Basic PO : "+customerInfo.getCustomerId(), this, customerInfo, ts, resultInfo).run();

//Adding ISDN SIM IMSI in Optional PO
        ts.setJsonFileName("createShoppingCart.json");
        resultInfo = Steps.PROVISION.eoc
                .createShoppingCart("Provision : Create Shopping Cart For Optional PO", this, customerInfo, ts, resultInfo)
                .run();

        Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart To get Basic POOI", this, customerInfo, ts, resultInfo)
                .run();
        customerInfo.setIsdn(isdn[1]);
        customerInfo.setMsisdn(customerInfo.getIsdn());
        customerInfo.setSerialNumber(serialNumbers[1]);
        customerInfo.setLinkedPortNumber(linkedPorts[1]);

        String pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("basic")), "id");
        ts.setBasicPoReferenceId(pooiId);

        ts.setJsonFileName("AddOptionalPOonBasic.json");
        Steps.PROTOCOLS.businessLogicEOC.addOptionalPOonBasicPO("Provision : Add Optional PO in to Basic PO", this,
                customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC.getPooi("Get Optional Poi",this,customerInfo,ts,resultInfo).run();

//Updating resources
        body = JsonHelper.updatePOOiResources(ts.getResponseBody(), "E.164",  customerInfo.getMsisdn(),"value");
        body = JsonHelper.updatePOOiResources(body.toString(), "IMSI",  customerInfo.getLinkedPortNumber(),"value");
        body = JsonHelper.updatePOOi(body.toString(), "deviceID",  customerInfo.getSerialNumber());
        body = JsonHelper.updatePOOi(body.toString(), "mainDirNum",  "false");
        ts.setResponseBody(body.toString());
       Steps.PROTOCOLS.businessLogicEOC
          .updatePooi("Provision : Update the Optional PO ", this, customerInfo, ts, resultInfo)
               .run();
        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision: Submit Shopping Cart for Optional PO ", this,customerInfo, ts, resultInfo).run();

//Verify Bill Before SIM CHange
        Jive.log("Verifying Bill for SIM "+customerInfo.getSerialNumber());
        verifyInvoicePDF(customerInfo,"BEFORE");

//Doing SIM Change
        ts.setJsonFileName("createShoppingCart.json");
        resultInfo = Steps.PROVISION.eoc
                .createShoppingCart("Provision : Create Shopping Cart for SIM Exchange", this, customerInfo, ts, resultInfo)
                .run();

        Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart for SIM Exchange", this, customerInfo, ts, resultInfo)
                .run();

         pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("optional")), "id");
        customerInfo.setPooiId(pooiId);
        Jive.log("OLD SIM IS"+customerInfo.getSerialNumber());
        ts.setJsonFileName("selectSerialNumber.xml");
        customerInfo.setSearchCount(1);
        Steps.PROTOCOLS.businessLogicBSCS.getFreeSerialNumber("Get three Free Serial Number and three Port Number",
                this, customerInfo, ts, resultInfo).run();

        Jive.log("NEW SIM IS"+customerInfo.getSerialNumber());

//Get Optional POI
        Steps.PROTOCOLS.businessLogicEOC.getPooi("Get Optional POOI",this,customerInfo,ts,resultInfo).run();

//Updating New Device ID or new Serial Number
        body =JsonHelper.updatePOOi(ts.getResponseBody(),"deviceIdExchange",customerInfo.getSerialNumber());
        body= searchKeyAndUpdateJson(body.toString(), "action", "Modify");
        ts.setResponseBody(body.toString());
        Steps.PROTOCOLS.businessLogicEOC.updatePooi("Update Optional POOI",this,customerInfo,ts,resultInfo).run();
        Steps.PROTOCOLS.businessLogicEOC.submitShoppingCart("Provision: Submit Shopping Cart for SIM Exchange", this, customerInfo, ts, resultInfo).run();

//Making Offline Voice Call
        customerInfo.setMsisdn(isdn[0]);
        ts.setUdrTextFileName("CDS_SIM_Exchange_UDR.txt");
        udrBody = RawMessages.getBodyFromTextFilePlacedInTestStep(this.getClass().getName(),
                "protocols/bscs/udrTemplates", ts.getUdrTextFileName()).toString();

        udrBody = udrBody.replace("{{IMSI}}", customerInfo.getLinkedPortNumber());
        udrBody = udrBody.replace("{{MSISDN}}", customerInfo.getMsisdn());
        udrBody = udrBody.replace("{{DATE}}", calculateDateTime.getCurrentDateTime());
        udrBody = udrBody.replace("{{PLCODE}}", businessCache.get("USAGE.PLCODE"));
        udrBody = udrBody.replace("{{DURATION}}", businessCache.get("USAGE.DURATION"));
        udrBody = udrBody.replace("{{NETWORK}}", businessCache.get("USAGE.NETWORK"));
        udrBody = udrBody.replace("{{TYPE}}", businessCache.get("USAGE.TYPE"));
        ts.setResponseBody(udrBody);
        Steps.CHARGING_OFFLINE.stepsChargingOfflineBscs.offlineVoice("Offline Call after Exchanged SIM",this,resultInfo,customerInfo,ts).run();
        Jive.log("Verifying Bill for SIM "+customerInfo.getSerialNumber());
        verifyInvoicePDF(customerInfo,"AFTER");
    }

    private void verifyInvoicePDF(CustomerInfo customerInfo,String message) throws InterruptedException {
        String verifyString=null;
        verifyString=businessCache.get("VERIFY.STRING").replace("SIMNO",customerInfo.getSerialNumber());

        Thread.sleep(Long.parseLong(businessCache.get("THREAD.SLEEP_INVOICE")));
        ts.setJsonFileName("sendBillGenerateRequest.xml");

        Steps.BILL_GENERATION.stepsBilling.sendInvoiceRequest("Protocol : Send Bill Process Create Request " , this, resultInfo, customerInfo, ts).run();

        resultInfo = Steps.BILL_GENERATION.stepsBilling.getInvoice("Validation : Get Invoice PDF and Validate GRAND TOTAL in PDF", this, resultInfo, customerInfo, ts).run();
        String readInvoiceData = (String) resultInfo.getResult();

            if (readInvoiceData.contains(verifyString)){
                Jive.log("SIM Number Verified on Bill");
            }
            else
            {
                Jive.failAndContinue("SIM Number Not Verified on BIll.. Failing..");
            }
            if ("AFTER".equals(message)){
                if (readInvoiceData.contains(businessCache.get("INVOICE.CHARGE"))){
                    Jive.log("INVOICE CHARGES VALIDATED SUCCESSFULL");
                }
                else{
                    Jive.failAndContinue("INVOICE CHARGES NOT CORRECT ON BILL.. FAILING.. PLEASE CHECK");
                }
            }

    }
    private String[] getResources(String resource) {
        resource = resource.replace("[", "");
        resource = resource.replace("]", "");
        String[] resources = resource.split(",");
        return resources;

    }
}
