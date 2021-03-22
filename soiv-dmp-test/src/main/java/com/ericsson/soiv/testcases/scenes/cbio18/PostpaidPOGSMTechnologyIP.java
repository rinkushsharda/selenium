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
public class PostpaidPOGSMTechnologyIP extends SoivTestBase
{
    private TestResultInfo resultInfo = new TestResultInfo();
    private CustomerInfo customerInfo = new CustomerInfo();
    private TransactionSpecification ts = new TransactionSpecification();
    private LinkedHashMap<String, String> productOfferings = new LinkedHashMap<String, String>();
    private CalculateDateTime calculateDateTime = new CalculateDateTime();

    @Test
    @Id ("00000056")
    @Tags ("OT-Regression, OT-Postpaid, BSCS-Controlled, OT-CBiO18")
    @Description ("ResourceHandlingSupportToMUltipleTechnologiesGSM IP Resource:BUC22A_A")
    public void postpaidPOGSMTechnologyIP() throws IOException
    {

        LinkedHashMap<String, String> businessCache = new LinkedHashMap<>(readBusinessCacheData(this.getTestCase().getId()));

        customerInfo.setCustomerBillCycle(businessCache.get("CUSTOMER.BILLCYCLE"));
        customerInfo.setOrderMode(businessCache.get("ORDER.MODE"));
        customerInfo.setRequester(businessCache.get("ORDER.REQUESTER"));
        ts.setInvoicePdfPageNo(Integer.parseInt(businessCache.get("INVOICE.PAGE_1")));
        ts.setRemoveInvoicePdfFile(Boolean.getBoolean(businessCache.get("INVOICE.REMOVE")));

        /* ====================== */
        /* ======for IP ========= */
        /* ====================== */

        productOfferings.put("basic", businessCache.get("PO.ID_BASIC_IP"));
        ts.setProductOfferingIds(productOfferings);
        /******************************
         * createCustomerContractBasicPO
         ***************************/

        Steps.PROVISION.bscs.createCustomer("Provision : Create Customer", this, customerInfo, ts, resultInfo).run();

        ts.setJsonFileName("createShoppingCart.json");
        Steps.PROVISION.eoc.createShoppingCart("Provision : Create Shopping Cart", this, customerInfo, ts, resultInfo)
                .run();

        /***************************
         * Handling for the resource GSM-IP
         ***********************************/

        customerInfo.setLogicalResourceType("IPV4");
        customerInfo.setNpCodePub(businessCache.get("IPV4.NPCODEPUB"));
        customerInfo.setPlCodePub(businessCache.get("IPV4.PLCODEPUB"));
        customerInfo.setSubmIdPub(businessCache.get("IPV4.SUBMIDPUB"));
        customerInfo.setHlCode(businessCache.get("IPV4.HLCODE"));
        customerInfo.setRsCode(businessCache.get("IPV4.RSCODE"));
        customerInfo.setCsControlled(businessCache.get("IPV4.CSCONTROLLED"));

        ts.setJsonFileName("selectLogicalResource.xml");
        resultInfo = Steps.PROTOCOLS.businessLogicBSCS
                .selectLogicalResource("Protocol : Get Free IP Resource Number", this, customerInfo, ts, resultInfo)
                .run();
        log("Selected IP : " + customerInfo.getresource().toString());
        customerInfo.setIpv4(customerInfo.getresource().toString());

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

        JSONObject jsonobject = JsonHelper.updatePOOi(ts.getResponseBody(), "resourceNumber", customerInfo.getIpv4());
        jsonobject = JsonHelper.updatePOOi(jsonobject.toString(), "IMSI", customerInfo.getLinkedPortNumber());
        jsonobject = JsonHelper.updatePOOi(jsonobject.toString(), "serialNumber", customerInfo.getSerialNumber());

        ts.setResponseBody(jsonobject.toString());
        Steps.PROTOCOLS.businessLogicEOC.updatePooi("updating the basic PO with IP Resource Number", this, customerInfo, ts, resultInfo)
                .run();

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision : Submit Shopping Cart For the Customer : " + customerInfo.getCustomerId(),
                        this, customerInfo, ts, resultInfo)
                .run();

        /* ===Usage for IP GSM==== */

        ts.setUdrTextFileName("BSCS_IP_UDR.txt");

        String body = RawMessages.getBodyFromTextFilePlacedInTestStep(this.getClass().getName(), "protocols/bscs/udrTemplates",
                ts.getUdrTextFileName()).toString();
        body = body.replace("{{IMSI}}", customerInfo.getLinkedPortNumber());
        body = body.replace("{{IP}}", customerInfo.getIpv4());
        body = body.replace("{{DATE}}", calculateDateTime.getCurrentDateTime());
        ts.setResponseBody(body);

        Steps.CHARGING_OFFLINE.stepsChargingOfflineBscs
                .offlineGy("Protocol : BSCS - Offline Usage for Resource : " + customerInfo.getIpv4(), this, resultInfo, customerInfo, ts)
                .run();

        ts.setInvoiceCharge(businessCache.get("INVOICE.CHARGE_IP"));
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
