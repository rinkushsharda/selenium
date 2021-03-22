package com.ericsson.soiv.testcases.scenes.cbio18;

import static com.ericsson.soiv.utils.http.ReadBusinessData.readBusinessCacheData;

import java.io.IOException;
import java.sql.SQLException;
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

@Fixture (SoivFixture.class)
public class PostpaidPOISDNTechIsdnOlid extends SoivTestBase
{
    private TestResultInfo resultInfo = new TestResultInfo();
    private CustomerInfo customerInfo = new CustomerInfo();
    private TransactionSpecification ts = new TransactionSpecification();
    private CalculateDateTime calculateDateTime = new CalculateDateTime();

    @Test
    @Id ("00000073")
    @Tags ("OT-Regression, OT-Postpaid, BSCS-Controlled, OT-CBiO18")
    @Description ("Resource Handling ISDN Technology ISDN-OLID:BUC22A_A")
    public void postpaidPOISDNTechIsdnOlid() throws IOException, SQLException, InterruptedException
    {

        LinkedHashMap<String, String> businessCache = new LinkedHashMap<>(
                readBusinessCacheData(this.getTestCase().getId()));
        customerInfo.setCustomerBillCycle(businessCache.get("CUSTOMER.BILLCYCLE"));
        customerInfo.setOrderMode(businessCache.get("ORDER.MODE"));
        customerInfo.setRequester(businessCache.get("ORDER.REQUESTER"));

        ts.setInvoicePdfPageNo(Integer.parseInt(businessCache.get("INVOICE.PAGE_1")));
        ts.setRemoveInvoicePdfFile(Boolean.getBoolean(businessCache.get("INVOICE.REMOVE")));

        LinkedHashMap<String, String> productOfferings = new LinkedHashMap<String, String>();
        productOfferings.put("basic", businessCache.get("PO.ID_BASIC"));

        ts.setProductOfferingIds(productOfferings);

        /*****************************
         * createCustomerContractBasicPO
         ***************************/

        resultInfo = Steps.PROVISION.bscs
                .createCustomer("Provision : Create Customer", this, customerInfo, ts, resultInfo)
                .run();
        ts.setJsonFileName("createShoppingCart.json");
        resultInfo = Steps.PROVISION.eoc
                .createShoppingCart("Provision : Create Shopping Cart", this, customerInfo, ts, resultInfo)
                .run();

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
                .selectLogicalResource("Protocol : Get Free ISDN Number", this, customerInfo, ts, resultInfo)
                .run();
        log("Selected ISDN : " + customerInfo.getresource().toString());
        customerInfo.setIsdn(customerInfo.getresource().toString());

        /****************************
         * Handling for the resource OLID
         ***********************************/

        ts.setJsonFileName("portSearch.xml");
        customerInfo.setSubmIdPub(businessCache.get("PORT.SUBMIDPUB"));
        customerInfo.setPortNpPub(businessCache.get("PORT.PORTNPPUB"));
        customerInfo.setPlCodePub(businessCache.get("PORT.PLCODEPUB"));
        customerInfo.setHlCode(businessCache.get("PORT.HLCODE"));

        Steps.PROTOCOLS.businessLogicBSCS.portSearch("Search Port Number for OLID ", this, customerInfo, ts, resultInfo)
                .run();
        Long olidNumber = JsonPath.read(ts.getResponseBody(),
                "$.SOAP-ENV:Envelope.SOAP-ENV:Body.portsSearchResponse.returnedPorts.item.portnum.content");
        log("Selected OLID : " + olidNumber);
        customerInfo.setOlidNumber(olidNumber);

        /****************************
         * Adding the Basic PO in the Cart
         ***********************************/
        customerInfo.setMarketType("ISD");
        customerInfo.setMarket(businessCache.get("BASICPO.MARKET"));
        customerInfo.setSubMarket(businessCache.get("BASICPO.SUB_MARKET"));
        customerInfo.setNetwork(businessCache.get("BASICPO.NETWORK"));

        ts.setJsonFileName("addProductOfferingShoppingCart.json");
        resultInfo = Steps.PROVISION.eoc
                .addPoInShoppingCart("Provision : Add PO in Shopping Cart", this, customerInfo, ts, resultInfo)
                .run();
        JSONObject jsonobject = JsonHelper.updatePOOi(ts.getResponseBody(), "resourceNumber", customerInfo.getIsdn());
        jsonobject = JsonHelper.updatePOOiResources(jsonobject.toString(), "OLID",
                (customerInfo.getOlidNumber()).toString(), "value");
        ts.setResponseBody(jsonobject.toString());
        Steps.PROTOCOLS.businessLogicEOC.updatePooi("updating the basic PO with ISDN Resource", this, customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision : Submit Shopping Cart For Customer : " + customerInfo.getCustomerId(),
                        this, customerInfo, ts, resultInfo)
                .run();

        /* ===Usage for GSM APN==== */

        ts.setUdrTextFileName("BSCS_ISDN_UDR.txt");
        String body = RawMessages.getBodyFromTextFilePlacedInTestStep(this.getClass().getName(),
                "protocols/bscs/udrTemplates", ts.getUdrTextFileName()).toString();
        body = body.replace("{{ISDN}}", customerInfo.getIsdn());
        body = body.replace("{{OLID}}", (customerInfo.getOlidNumber()).toString());
        body = body.replace("{{DATE}}", calculateDateTime.getCurrentDateTime());
        ts.setResponseBody(body);

        Steps.CHARGING_OFFLINE.stepsChargingOfflineBscs
                .offlineGy("Protocol : BSCS - Offline Usage for Resource : " + customerInfo.getIsdn(), this, resultInfo, customerInfo, ts)
                .run();
        ts.setInvoiceCharge(businessCache.get("INVOICE.CHARGE"));
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
