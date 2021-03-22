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

import static com.ericsson.soiv.utils.JsonHelper.getRequestedJsonElement;
import static com.ericsson.soiv.utils.http.ReadBusinessData.readBusinessCacheData;

//Created By ZMUKMAN 26-June-2019

@Fixture(SoivFixture.class)
public class PostpaidBasicOptionalExchangeResources extends SoivTestBase {
    private TestResultInfo resultInfo = new TestResultInfo();
    private CustomerInfo customerInfo = new CustomerInfo();
    private TransactionSpecification ts = new TransactionSpecification();
    private LinkedHashMap<String, String> businessCache = null;

    @Test
    @Id("00000054")
    @Tags("OT-Regression, OT-Postpaid, EC-Controlled, OT-CBiO18")
    @Description("PostpaidBasicOptionalExchangeResources UC06 BUC8BB")
    public void postpaidBasicOptionalExchangeResources() throws IOException, InterruptedException {

        businessCache = new LinkedHashMap<>(readBusinessCacheData(this.getTestCase().getId()));

        customerInfo.setCustomerBillCycle(businessCache.get("CUSTOMER.BILLCYCLE"));
        customerInfo.setOrderMode(businessCache.get("ORDER.MODE"));
        customerInfo.setRequester(businessCache.get("ORDER.REQUESTER"));
        ts.setInvoicePdfPageNo(Integer.parseInt(businessCache.get("INVOICE.PAGENO")));
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

        Steps.PROTOCOLS.businessLogicEOC.submitShoppingCart("Provision : Submit Shopping Cart Without Exchange", this,
                customerInfo, ts, resultInfo).run();

        /****************************
         * Add Optional Po on Basic PO With Resources APN and IPV4
         ****************************/
        ts.setJsonFileName("createShoppingCart.json");
        resultInfo = Steps.PROVISION.eoc.createShoppingCart("Provision : Create Shopping Cart For Optional APN & IP PO",
                this, customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart To get Basic POOI", this, customerInfo, ts, resultInfo)
                .run();

        String pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("basic")), "id");
        ts.setBasicPoReferenceId(pooiId);

        /****************************
         * Resource IPV4 - Technology GSM
         ****************************/
        customerInfo.setLogicalResourceType("IPV4");
        customerInfo.setNpCodePub(businessCache.get("IPV4.NPCODEPUB"));
        customerInfo.setPlCodePub(businessCache.get("IPV4.PLCODEPUB"));
        customerInfo.setSubmIdPub(businessCache.get("IPV4.SUBMIDPUB"));
        customerInfo.setHlCode(businessCache.get("IPV4.HLCODE"));
        customerInfo.setRsCode(businessCache.get("IPV4.RSCODE"));
        customerInfo.setCsControlled(businessCache.get("IPV4.CSCONTROLLED"));

        ts.setJsonFileName("selectLogicalResource.xml");
        resultInfo = Steps.PROTOCOLS.businessLogicBSCS
                .selectLogicalResource("Protocol : Get Free IPV4 - GSM Resource", this, customerInfo, ts, resultInfo)
                .run();

        customerInfo.setIpv4(customerInfo.getresource().toString());
        Jive.log("Selected IPV4 : " + customerInfo.getIpv4());

        /****************************
         * Resource APN - Technology GSM
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
                .selectLogicalResource("Protocol : Get Free APN - GSM Resource", this, customerInfo, ts, resultInfo)
                .run();

        customerInfo.setAPN(customerInfo.getresource().toString());
        Jive.log("Selected APN : " + customerInfo.getAPN());

        customerInfo.setMarketType("GSM");
        ts.setJsonFileName("AddOptionalPOonBasic.json");
        Steps.PROTOCOLS.businessLogicEOC.addOptionalPOonBasicPO("Provision : Add Optional PO in to Basic PO", this,
                customerInfo, ts, resultInfo).run();

        /***************************
         * Updating the Optional PO with the resources APN and IPV4
         ****************************/
        JSONObject jsonObject = JsonHelper.updatePOOiResources(ts.getResponseBody(), "APN", customerInfo.getAPN(),
                "value");
        jsonObject = JsonHelper.updatePOOiResources(jsonObject.toString(), "IPV4", customerInfo.getIpv4(), "value");

        ts.setResponseBody(jsonObject.toString());
        Steps.PROTOCOLS.businessLogicEOC.updatePooi("Provision : Update the Optional  PO for resources IPV4 and APN",
                this, customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Submit cart for Resources in Optional PO", this, customerInfo, ts, resultInfo)
                .run();

        /*****************************
         * Steps for Optional PO Exchange Remove APN and Active IPV4
         ****************************/

        ts.setJsonFileName("createShoppingCart.json");
        Steps.PROVISION.eoc.createShoppingCart(
                "Provision : Create Shopping Cart For Exchange Of More CFSS to Less CFSS Optional PO", this,
                customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart for POOI of Basic And Optional PO", this, customerInfo,
                        ts, resultInfo)
                .run();

        pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("basic")), "id");
        ts.setBasicPoReferenceId(pooiId);

        pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("optional")), "id");
        ts.setOptionalPoReferenceId(pooiId);

        ts.setJsonFileName("SwapProductOffering.json");
        Jive.log("optionalPOSwap" + ts.getProductOfferingIds().get("optionalPOSwap").toString());
        Steps.PROTOCOLS.businessLogicEOC.swapProductOffering("Protocol : Exchange Optional Product Offering", this,
                customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision : Submit Shopping Cart For Swap In", this, customerInfo, ts, resultInfo)
                .run();

        // Optional_APN PO Should Be Deleted From the Contract
        verifyPOStatus(productOfferings.get("optional"), "d");

        // Optional_IP PO After Swap Should Be Active In the Contract
        verifyPOStatus(productOfferings.get("optionalPOSwap"), "a");

        /*****************************
         * Bill Generation After PO Exchange
         **************************/
        ts.setInvoiceCharge(businessCache.get("INVOICE.CHARGE"));
        verifyInvoicePDF(ts, "After PO Exchange");

    }

    private void verifyInvoicePDF(TransactionSpecification ts, String message) throws InterruptedException {
        Thread.sleep(Long.parseLong(businessCache.get("THREAD.SLEEP_USAGE")));
        ts.setJsonFileName("sendBillGenerateRequest.xml");

        Steps.BILL_GENERATION.stepsBilling.sendInvoiceRequest("Protocol : Send Bill Process Create Request " + message,
                this, resultInfo, customerInfo, ts).run();

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

    }

    private void verifyPOStatus(String poName, String poStatus) {

        Steps.PROTOCOLS.businessLogicBSCS
                .getPoStatusBSCS("Read PO Status from BSCS", this, customerInfo, ts, resultInfo).run();

        int count = JsonPath.read(ts.getResponseBody(),
                "$.SOAP-ENV:Envelope.SOAP-ENV:Body.productCfsReadResponse.productCfsReadOutputDTO.products.productCfsReadOutputProductDTO[*].productId.content")
                .toString().split(",").length;
        for (int i = 0; i < count; i++) {
            if (JsonPath.read(ts.getResponseBody(),
                    "$.SOAP-ENV:Envelope.SOAP-ENV:Body.productCfsReadResponse.productCfsReadOutputDTO.products.productCfsReadOutputProductDTO["
                            + i + "].productOfferingId.content")
                    .equals(poName)) {
                if (poStatus.equals(JsonPath.read(ts.getResponseBody(),
                        "$.SOAP-ENV:Envelope.SOAP-ENV:Body.productCfsReadResponse.productCfsReadOutputDTO.products.productCfsReadOutputProductDTO["
                                + i + "].productStatus.content"))) {
                    Jive.log(poName + " : PO is Deleted Successfully");
                } else {
                    Jive.fail(poName + " : PO Not Deleted Successfully. Kindly Check EOC Order Status");
                }
            }

        }
    }

}
