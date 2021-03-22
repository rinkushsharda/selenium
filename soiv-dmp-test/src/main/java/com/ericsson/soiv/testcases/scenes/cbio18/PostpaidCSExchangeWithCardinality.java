package com.ericsson.soiv.testcases.scenes.cbio18;

import com.ericsson.jive.core.execution.*;
import com.ericsson.soiv.fixtures.SoivFixture;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.*;
import com.jayway.jsonpath.JsonPath;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import java.io.IOException;
import java.util.LinkedHashMap;
import static com.ericsson.soiv.utils.JsonHelper.getRequestedJsonElement;
import static com.ericsson.soiv.utils.JsonHelper.searchKeyAndUpdateJson;
import static com.ericsson.soiv.utils.http.ReadBusinessData.readBusinessCacheData;

//Created By ZKAPSAR 11-July-2019

@Fixture(SoivFixture.class)
public class PostpaidCSExchangeWithCardinality extends SoivTestBase {
    private TestResultInfo resultInfo = new TestResultInfo();
    private CustomerInfo customerInfo = new CustomerInfo();
    private TransactionSpecification ts = new TransactionSpecification();
    private LinkedHashMap<String,String> businessCache = null;

    @Test
    @Id("00000066")
    @Tags("OT-Regression, OT-Postpaid, EC-Controlled, OT-CBiO18")
    @Description("POGSMCSOptional_IP_CRD_REG_88B--BUC8BB")
    public void postpaidCsExchangeWithCardinality() throws IOException, InterruptedException {

        businessCache = new LinkedHashMap<>(readBusinessCacheData(this.getTestCase().getId()));
        String checkIpv4FirstResourceOnBill=null;
        String ipv4[]=new String[Integer.parseInt(businessCache.get("RESOURCE.CARDINALITY"))];

        customerInfo.setCustomerBillCycle(businessCache.get("CUSTOMER.BILLCYCLE"));
        customerInfo.setOrderMode(businessCache.get("ORDER.MODE"));
        customerInfo.setRequester(businessCache.get("ORDER.REQUESTER"));
        ts.setInvoicePdfPageNo(Integer.parseInt(businessCache.get("INVOICE.PAGENO_1")));
        ts.setRemoveInvoicePdfFile(Boolean.valueOf(businessCache.get("INVOICE.REMOVE")));

        LinkedHashMap<String, String> productOfferings = new LinkedHashMap<String, String>();
        productOfferings.put("basic", businessCache.get("PO.ID_BASIC"));
        productOfferings.put("optional", businessCache.get("PO.ID_OPTIONAL"));
        productOfferings.put("optionalPOSwap", businessCache.get("PO.ID_OPTIONAL_SWAP"));

        ts.setProductOfferingIds(productOfferings);
        customerInfo.setNpCodePub(businessCache.get("MSISDN.NPCODEPUB"));
        customerInfo.setPlCodePub(businessCache.get("MSISDN.PLCODEPUB"));
        customerInfo.setSubmIdPub(businessCache.get("MSISDN.SUBMIDPUB"));
        customerInfo.setHlCode(businessCache.get("MSISDN.HLCODE"));
        customerInfo.setRsCode(businessCache.get("MSISDN.RSCODE"));
        customerInfo.setCsControlled(businessCache.get("MSISDN.CSCONTROLLED"));
        Steps.PROVISION.eoc.createCustomerContractBasicPO("Create Customer and Contract with Basic PO", this,
                customerInfo, ts, resultInfo).run();
         Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision : Submit Shopping Cart For Customer with Basic PO : "+customerInfo.getCustomerId(), this, customerInfo, ts, resultInfo).run();

// Add Optional Po on Basic PO With Resources IPV4 Resources

        ts.setJsonFileName("createShoppingCart.json");
        resultInfo = Steps.PROVISION.eoc
                .createShoppingCart("Provision : Create Shopping Cart For Optional IP PO", this, customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart To get Basic POOI", this, customerInfo, ts, resultInfo).run();

        String pooiId = getRequestedJsonElement(ts,String.valueOf(ts.getProductOfferingIds().get("basic")),"id");
        ts.setBasicPoReferenceId(pooiId);

// Resource IPV4 - Technology GSM

        customerInfo.setLogicalResourceType("IPV4");
        customerInfo.setNpCodePub(businessCache.get("IPV4.NPCODEPUB"));
        customerInfo.setPlCodePub(businessCache.get("IPV4.PLCODEPUB"));
        customerInfo.setSubmIdPub(businessCache.get("IPV4.SUBMIDPUB"));
        customerInfo.setHlCode(businessCache.get("IPV4.HLCODE"));
        customerInfo.setRsCode(businessCache.get("IPV4.RSCODE"));
        customerInfo.setCsControlled(businessCache.get("IPV4.CSCONTROLLED"));
        customerInfo.setSearchCount(Integer.parseInt(businessCache.get("RESOURCE.CARDINALITY")));
        ts.setJsonFileName("selectLogicalResource.xml");
        resultInfo = Steps.PROTOCOLS.businessLogicBSCS
                .selectLogicalResource("Protocol : Get Free IPV4 - GSM Resource", this, customerInfo, ts, resultInfo).run();

//Getting Resources(IP) and adding one Resource to Optional PO

        customerInfo.setIpv4(getResources(customerInfo.getresource().toString()).get(0).toString());
        Jive.log("Selected IPV4 : "+customerInfo.getIpv4());
        
        
       /* customerInfo.setIpv4(ipv4[0]);
        Jive.log("Selected IPV4 : "+ipv4[0]);*/
        checkIpv4FirstResourceOnBill=HelperClass.iptoHexaDecimal(customerInfo.getIpv4());
        customerInfo.setMarketType("GSM");
        ts.setJsonFileName("AddOptionalPOonBasic.json");
        Steps.PROTOCOLS.businessLogicEOC.addOptionalPOonBasicPO("Provision : Add Optional PO in to Basic PO", this,
                customerInfo, ts, resultInfo).run();

//Updating the Optional PO with the First resourc IPV4 Resource

        JSONObject jsonObject = JsonHelper.updatePOOiResources(ts.getResponseBody(), "IPV4", customerInfo.getIpv4(), "value");
            ts.setResponseBody(jsonObject.toString());
        Steps.PROTOCOLS.businessLogicEOC.updatePooi("Provision : Update the Optional PO for resources IP Resources", this, customerInfo, ts, resultInfo).run();

//Adding One More Resource
        for (int i=1;i<Integer.parseInt(businessCache.get("RESOURCE.CARDINALITY"));i++) {
            Steps.PROTOCOLS.businessLogicEOC.getShoppingCart("Submit cart for Resources in Optional PO", this, customerInfo, ts, resultInfo).run();

            pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("optional")), "id");
            customerInfo.setPooiId(pooiId);
            customerInfo.setIpv4(getResources(customerInfo.getresource().toString()).get(i).toString());
            Jive.log("Selected IPV4 : " + customerInfo.getIpv4());
            addIpv4Resources(i);
        }

//Trying to Add One More Resource.. It Should fail
        ts.setIfCheckForCardinality(true);
        ts.setExpectedResourceStatusCode(500);
        addIpv4Resources(Integer.parseInt(businessCache.get("RESOURCE.CARDINALITY"))+1);

//Verify Bill Before Swapping PO
        customerInfo.setIpv4(checkIpv4FirstResourceOnBill);
        Jive.log("Check Bill Before PO Swapping");
        verifyInvoicePDF(ts,"BEFORE");

//Retreiving APN resources for Swapping with APN PO

        customerInfo.setLogicalResourceType("APN");
        customerInfo.setNpCodePub(businessCache.get("APN.NPCODEPUB"));
        customerInfo.setPlCodePub(businessCache.get("APN.PLCODEPUB"));
        customerInfo.setSubmIdPub(businessCache.get("APN.SUBMIDPUB"));
        customerInfo.setHlCode(businessCache.get("APN.HLCODE"));
        customerInfo.setRsCode(businessCache.get("APN.RSCODE"));
        customerInfo.setCsControlled(businessCache.get("APN.CSCONTROLLED"));

        ts.setJsonFileName("selectLogicalResource.xml");
        resultInfo = Steps.PROTOCOLS.businessLogicBSCS
                .selectLogicalResource("Protocol : Get Free APN - GSM Resource", this, customerInfo, ts, resultInfo).run();

        Object apn = JsonPath.read(ts.getResponseBody(),
                "$.SOAP-ENV:Envelope.SOAP-ENV:Body.genericDirectoryNumberSearchResponse.directorynumbers.item.dirnum.content");
        customerInfo.setAPN(String.valueOf(apn));
        Jive.log("Selected APN : "+apn);

//Start with Swapping PO
        ts.setJsonFileName("createShoppingCart.json");
        Steps.PROVISION.eoc.createShoppingCart("Provision : Create Shopping Cart For Swap of IP and APN PO",
                this, customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart for POOI of Basic And Optional PO", this, customerInfo, ts, resultInfo)
                .run();

        pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("basic")), "id");
        ts.setBasicPoReferenceId(pooiId);

        pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("optional")), "id");
        ts.setOptionalPoReferenceId(pooiId);

        ts.setJsonFileName("SwapProductOffering.json");
        Jive.log("optionalPOSwap"+ts.getProductOfferingIds().get("optionalPOSwap").toString());
        Steps.PROTOCOLS.businessLogicEOC.swapProductOffering("Protocol : Exchange Optional Product Offering",this,customerInfo,ts,resultInfo)
                .run();

        Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart for POOI of OptionalSwap PO", this, customerInfo, ts, resultInfo)
                .run();

        pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("optionalPOSwap")), "id");
        customerInfo.setPooiId(pooiId);

//Updating the Swapped PO with the resources APN
        Steps.PROTOCOLS.businessLogicEOC.getPooi("Protocol : Get OptionalSwap Po To Update APN Resource",this,customerInfo,ts,resultInfo).run();

        jsonObject = JsonHelper.updatePOOiResources(ts.getResponseBody(), "APN", customerInfo.getAPN(), "value");

        ts.setResponseBody(jsonObject.toString());
        Steps.PROTOCOLS.businessLogicEOC.updatePooi("Provision : Update the Optional PO for APN ", this, customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision : Submit Shopping Cart For Swap In", this, customerInfo, ts, resultInfo)
                .run();

//Verify Bill After Swapping PO
        Jive.log("Check Bill After PO Swapping");
        verifyInvoicePDF(ts,"AFTER");

// Optional_IP PO Should Be Deleted From the Contract
        verifyPOStatus(productOfferings.get("optional"),"d");

// Optional_APN PO After Swap Should Be Active In the Contract
        verifyPOStatus(productOfferings.get("optionalPOSwap"),"a");

    }

    private void verifyPOStatus(String poName, String poStatus){

        Steps.PROTOCOLS.businessLogicBSCS.getPoStatusBSCS("Read PO Status from BSCS", this, customerInfo,ts, resultInfo)
                .run();

        int count = JsonPath.read(ts.getResponseBody(), "$.SOAP-ENV:Envelope.SOAP-ENV:Body.productCfsReadResponse.productCfsReadOutputDTO.products.productCfsReadOutputProductDTO[*].productId.content").toString().split(",").length;
        for (int i = 0; i < count; i++) {
            if (JsonPath.read(ts.getResponseBody(), "$.SOAP-ENV:Envelope.SOAP-ENV:Body.productCfsReadResponse.productCfsReadOutputDTO.products.productCfsReadOutputProductDTO[" + i + "].productOfferingId.content").equals(poName)) {
                if (poStatus.equals(JsonPath.read(ts.getResponseBody(), "$.SOAP-ENV:Envelope.SOAP-ENV:Body.productCfsReadResponse.productCfsReadOutputDTO.products.productCfsReadOutputProductDTO[" + i + "].productStatus.content"))) {
                    Jive.log(poName+" : PO Status Successfully Verified to "+poStatus);
                    break;
                } else {
                    if((i+1)==count) {
                        Jive.fail(poName + " : PO Status Not Successfully Verified to " + poStatus + "Failing...");
                    }
                }
            }

        }
    }
    private void verifyInvoicePDF(TransactionSpecification ts,String message) throws InterruptedException {
        Thread.sleep(Long.parseLong(businessCache.get("THREAD.SLEEP_USAGE")));
        ts.setJsonFileName("sendBillGenerateRequest.xml");

        Steps.BILL_GENERATION.stepsBilling.sendInvoiceRequest("Protocol : Send Bill Process Create Request " + message, this, resultInfo, customerInfo, ts).run();

        resultInfo = Steps.BILL_GENERATION.stepsBilling.getInvoice("Validation : Get Invoice PDF and Validate GRAND TOTAL in PDF", this, resultInfo, customerInfo, ts).run();
        String readInvoiceData = (String) resultInfo.getResult();

        if("BEFORE".equals(message)) {

            if (readInvoiceData.contains(businessCache.get("PO.ID_OPTIONAL"))) {
                Jive.log("OK : PO Name on Bill "+businessCache.get("PO.ID_OPTIONAL")+" Validated Successfully");
            } else {
                Jive.log("PO Name on Bill "+customerInfo.getIpv4()+" Not Validated Successfully");
            }
             if (readInvoiceData.contains(customerInfo.getIpv4())){
                Jive.log("OK IP Address on Bill "+customerInfo.getIpv4()+" Validated Successfully");
            }else
             {
                 Jive.fail("IP Address on Bill "+customerInfo.getIpv4()+" Not Validated Successfully");

             }
        }

        if("AFTER".equals(message)) {
            if (readInvoiceData.contains(businessCache.get("PO.ID_OPTIONAL"))) {
                Jive.log("OK : PO Name on Bill "+businessCache.get("PO.ID_OPTIONAL")+" Validated Successfully");
            } else {
                Jive.log("PO Name on Bill "+customerInfo.getIpv4()+" Not Validated Successfully");
            }
                if (readInvoiceData.contains(businessCache.get("PO.ID_OPTIONAL_SWAP"))) {
                Jive.log("OK : SWAP PO Name on Bill "+businessCache.get("PO.ID_OPTIONAL")+" Validated Successfully");
            } else {
                Jive.log("PO Name on Bill "+customerInfo.getIpv4()+" Not Validated Successfully");
            }
            if (readInvoiceData.contains(customerInfo.getAPN())){
                Jive.log("OK APN on Bill "+customerInfo.getIpv4()+" Validated Successfully");
            }else
            {
                Jive.fail("APN Address on Bill "+customerInfo.getIpv4()+" Not Validated Successfully");

            }
        }

    }
    private void addIpv4Resources(int countResource){

        String pooiId=null;
        Object ipv4Address=null;
        String cardinalityCheckString=businessCache.get("CARDINALITY.CHECKSTRING");

        cardinalityCheckString=cardinalityCheckString.replace("CARDINALITY",businessCache.get("RESOURCE.CARDINALITY"));

        if(ts.getIfCheckForCardinality() && countResource==Integer.parseInt(businessCache.get("RESOURCE.CARDINALITY"))+1) {
        ts.setJsonFileName("createShoppingCart.json");
        Steps.PROVISION.eoc
               .createShoppingCart("Provision : Create Shopping Cart For Adding Resources", this, customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart ", this, customerInfo, ts, resultInfo).run();

        pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("optional")), "id");
        customerInfo.setPooiId(pooiId);
        Steps.PROTOCOLS.businessLogicEOC.getPooi("Get POOi",this,customerInfo,ts,resultInfo).run();

        JSONObject getPOOIRequestBody = searchKeyAndUpdateJson(ts.getResponseBody(), "action", "Modify");
        ts.setResponseBody(getPOOIRequestBody.toString());
        Steps.PROTOCOLS.businessLogicEOC.updatePooi("Updation of Action", this, customerInfo, ts, resultInfo).run();
         customerInfo.setLogicalResourceType("IPV4");
        customerInfo.setNpCodePub(businessCache.get("IPV4.NPCODEPUB"));
        customerInfo.setPlCodePub(businessCache.get("IPV4.PLCODEPUB"));
        customerInfo.setSubmIdPub(businessCache.get("IPV4.SUBMIDPUB"));
        customerInfo.setHlCode(businessCache.get("IPV4.HLCODE"));
        customerInfo.setRsCode(businessCache.get("IPV4.RSCODE"));
        customerInfo.setCsControlled(businessCache.get("IPV4.CSCONTROLLED"));
        customerInfo.setSearchCount(1);

        ts.setJsonFileName("selectLogicalResource.xml");
        resultInfo = Steps.PROTOCOLS.businessLogicBSCS
                .selectLogicalResource("Protocol : Get Free IPV4 - GSM Resource", this, customerInfo, ts, resultInfo).run();

            ipv4Address = JsonPath.read(ts.getResponseBody(),
                "$.SOAP-ENV:Envelope.SOAP-ENV:Body.genericDirectoryNumberSearchResponse.directorynumbers.item.dirnum.content").toString();

            customerInfo.setIpv4(String.valueOf(ipv4Address));
            Jive.log("Selected IPV4 : " + String.valueOf(ipv4Address));
        }

        Steps.PROTOCOLS.businessLogicEOC.getResources("Get IPV4 Resources",this,customerInfo,ts,resultInfo).run();

        ts.setResourceParentId(JsonPath.read(ts.getResponseBody(),"$.[0].rfs.id"));
        ts.setResourceSpecification(JsonPath.read(ts.getResponseBody(),"$.[0].resourceSpecification"));


        Steps.PROTOCOLS.businessLogicEOC.addIpv4Resources("Add IPV4 Resource",this,customerInfo,ts,resultInfo).run();
        if(ts.getIfCheckForCardinality() && countResource==Integer.parseInt(businessCache.get("RESOURCE.CARDINALITY"))+1) {
            if (!ts.getResponseBody().contains(cardinalityCheckString)) {
                Jive.fail("Able To ADD More Resources than Actual Cardinality.. Failing");
            }
            Jive.log("Correctly Verified.. Not Able to Add More than "+businessCache.get("RESOURCE.CARDINALITY")+" Resources");
        }
        if (!ts.getIfCheckForCardinality() && countResource==Integer.parseInt(businessCache.get("RESOURCE.CARDINALITY"))-1) {
            Steps.PROTOCOLS.businessLogicEOC.submitShoppingCart("Submit cart for Resources in Optional PO", this, customerInfo, ts, resultInfo).run();
        }

    }
    
    private JSONArray getResources(String resource) {
        JSONArray resources = new JSONArray(resource);
        return resources;

    }
}