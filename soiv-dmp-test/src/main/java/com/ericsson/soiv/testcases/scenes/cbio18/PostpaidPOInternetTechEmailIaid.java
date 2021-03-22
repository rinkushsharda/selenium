package com.ericsson.soiv.testcases.scenes.cbio18;

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

//Created By EMODRED 26-June-2019

@Fixture (SoivFixture.class)
public class PostpaidPOInternetTechEmailIaid extends SoivTestBase
{
    private TestResultInfo resultInfo = new TestResultInfo();
    private CustomerInfo customerInfo = new CustomerInfo();
    private TransactionSpecification ts = new TransactionSpecification();
    private LinkedHashMap<String, String> productOfferings = new LinkedHashMap<String, String>();
    private CalculateDateTime calculateDateTime = new CalculateDateTime();

    @Test
    @Id ("00000062")
    @Tags ("OT-Regression, OT-Postpaid, BSCS-Controlled, OT-CBiO18")
    @Description ("ResourceHandling Internet Technology with Email Resource:BUC22A_A")
    public void postpaidPOInternetTechEmailIaid() throws IOException
    {
        LinkedHashMap<String, String> businessCache = new LinkedHashMap<>(readBusinessCacheData(this.getTestCase().getId()));

        customerInfo.setCustomerBillCycle(businessCache.get("CUSTOMER.BILLCYCLE"));
        customerInfo.setOrderMode(businessCache.get("ORDER.MODE"));
        customerInfo.setRequester(businessCache.get("ORDER.REQUESTER"));
        productOfferings.put("basic", businessCache.get("PO.ID_BASIC_EMAIL"));
        ts.setProductOfferingIds(productOfferings);

        /******************************
         * createCustomerContractBasicPO
         ***************************/
        Steps.PROVISION.bscs.createCustomer("Provision : Create Customer", this, customerInfo, ts, resultInfo).run();
        ts.setJsonFileName("createShoppingCart.json");
        Steps.PROVISION.eoc.createShoppingCart("Provision : Create Shopping Cart", this, customerInfo, ts, resultInfo)
                .run();

        /***************************
         * Handling for the resource Email
         ***********************************/
        customerInfo.setLogicalResourceType("EMAIL");
        customerInfo.setNpCodePub(businessCache.get("EMAIL.NPCODEPUB"));
        customerInfo.setPlCodePub(businessCache.get("EMAIL.PLCODEPUB"));
        customerInfo.setSubmIdPub(businessCache.get("EMAIL.SUBMIDPUB"));
        customerInfo.setHlCode(businessCache.get("EMAIL.HLCODE"));
        customerInfo.setRsCode(businessCache.get("EMAIL.RSCODE"));
        customerInfo.setCsControlled(businessCache.get("EMAIL.CSCONTROLLED"));

        ts.setJsonFileName("selectLogicalResource.xml");
        resultInfo = Steps.PROTOCOLS.businessLogicBSCS
                .selectLogicalResource("Protocol : Get Free Email Resource", this, customerInfo, ts, resultInfo)
                .run();
        customerInfo.setemail(customerInfo.getresource().toString());
        Jive.log("Selected EMAIL : " + customerInfo.getemail());

        /****************************
         * Handling for the resource IAID
         ***********************************/

        ts.setJsonFileName("portSearch.xml");

        customerInfo.setSubmIdPub(businessCache.get("PORT.SUBMIDPUB"));
        customerInfo.setPortNpPub(businessCache.get("PORT.PORTNPPUB"));
        customerInfo.setPlCodePub(businessCache.get("PORT.PLCODEPUB"));
        customerInfo.setHlCode(businessCache.get("PORT.HLCODE"));

        Steps.PROTOCOLS.businessLogicBSCS.portSearch("Protocol : Get Free Port Number for IAI ", this, customerInfo, ts, resultInfo)
                .run();
        String iaidNumber = JsonPath.read(ts.getResponseBody(),
                "$.SOAP-ENV:Envelope.SOAP-ENV:Body.portsSearchResponse.returnedPorts.item.portnum.content");
        customerInfo.setIaidNumber(iaidNumber);
        Jive.log("Selected IAID : " + customerInfo.getIaidNumber());

        /****************************
         * Adding the Basic PO in the Cart
         ***********************************/
        customerInfo.setMarketType("IAI");
        customerInfo.setMarket(businessCache.get("BASICPO.MARKET"));
        customerInfo.setSubMarket(businessCache.get("BASICPO.SUB_MARKET"));
        customerInfo.setNetwork(businessCache.get("BASICPO.NETWORK"));

        ts.setJsonFileName("addProductOfferingShoppingCart.json");
        resultInfo = Steps.PROVISION.eoc
                .addPoInShoppingCart("Provision : Add PO in Shopping Cart", this, customerInfo, ts, resultInfo)
                .run();
        JSONObject jsonObject = JsonHelper.updatePOOi(ts.getResponseBody(), "resourceNumber", customerInfo.getemail());
        jsonObject = JsonHelper.updatePOOiResources(jsonObject.toString(), "IAI",
                (customerInfo.getIaidNumber()).toString(), "value");
        ts.setResponseBody(jsonObject.toString());
        Steps.PROTOCOLS.businessLogicEOC.updatePooi("updating the basic PO", this, customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision : Submit Shopping Cart For Customer : " + customerInfo.getCustomerId(),
                        this, customerInfo, ts, resultInfo)
                .run();
        /* ===Usage for EMAIL IAI==== */

        ts.setInvoiceCharge(businessCache.get("INVOICE.CHARGE"));
        ts.setInvoicePdfPageNo(Integer.parseInt(businessCache.get("INVOICE.PAGENO")));
        ts.setRemoveInvoicePdfFile(Boolean.getBoolean(businessCache.get("INVOICE.REMOVE")));
        ts.setUdrTextFileName("INT_BSCS_EMAIL.txt");

        String body = RawMessages.getBodyFromTextFilePlacedInTestStep(this.getClass().getName(),
                "protocols/bscs/udrTemplates", ts.getUdrTextFileName()).toString();
        body = body.replace("{{EMAIL}}", customerInfo.getemail());
        body = body.replace("{{IAID}}", customerInfo.getIaidNumber());
        body = body.replace("{{DATE}}", calculateDateTime.getCurrentDateTime());
        ts.setResponseBody(body);

        Steps.CHARGING_OFFLINE.stepsChargingOfflineBscs
                .offlineGy("Protocol : BSCS - Offline EMAIL Usage :"+customerInfo.getemail(), this, resultInfo, customerInfo, ts)
                .run();
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
           Jive.failAndContinue("FAILED : Invoice PDF Charges : "+ ts.getInvoiceCharge() +"are Not Validated Successfully! Please Check the Invoice Content : "
                    + readInvoiceData);
        }

    }

}
