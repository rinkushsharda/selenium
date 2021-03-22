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

//Created By EMODRED 26-June-2019

@Fixture (SoivFixture.class)
public class PostpaidPOInternetTechIpIaid extends SoivTestBase
{
    private TestResultInfo resultInfo = new TestResultInfo();
    private CustomerInfo customerInfo = new CustomerInfo();
    private TransactionSpecification ts = new TransactionSpecification();
    private LinkedHashMap<String, String> productOfferings = new LinkedHashMap<String, String>();
    private CalculateDateTime calculateDateTime = new CalculateDateTime();

    @Test
    @Id ("00000064")
    @Tags ("OT-Regression, OT-Postpaid, BSCS-Controlled, OT-CBiO18")
    @Description ("ResourceHandling Internet Technology with IP resource:BUC22A_A")
    public void postpaidPOInternetTechIpIaid() throws IOException
    {
        LinkedHashMap<String, String> businessCache = new LinkedHashMap<>(readBusinessCacheData(this.getTestCase().getId()));

        customerInfo.setCustomerBillCycle(businessCache.get("CUSTOMER.BILLCYCLE"));
        customerInfo.setOrderMode(businessCache.get("ORDER.MODE"));
        customerInfo.setRequester(businessCache.get("ORDER.REQUESTER"));

        /********** BSCS PO ******************/
        /****************************
         * Basic BSCS_Basic_IP_IAID
         ***********************************/
        /***********************************/

        productOfferings.put("basic", businessCache.get("PO.ID_BASIC_IP_IAID"));
        ts.setProductOfferingIds(productOfferings);
        /******************************
         * createCustomerContractBasicPO
         ***************************/
        Steps.PROVISION.bscs.createCustomer("Provision : Create Customer", this, customerInfo, ts, resultInfo).run();
        ts.setJsonFileName("createShoppingCart.json");
        Steps.PROVISION.eoc.createShoppingCart("Provision : Create Shopping Cart", this, customerInfo, ts, resultInfo)
                .run();
        customerInfo.setLogicalResourceType("IPV4");
        customerInfo.setNpCodePub(businessCache.get("IPV4.NPCODEPUB"));
        customerInfo.setPlCodePub(businessCache.get("IPV4.PLCODEPUB"));
        customerInfo.setSubmIdPub(businessCache.get("IPV4.SUBMIDPUB"));
        customerInfo.setHlCode(businessCache.get("IPV4.HLCODE"));
        customerInfo.setRsCode(businessCache.get("IPV4.RSCODE"));
        customerInfo.setCsControlled(businessCache.get("IPV4.CSCONTROLLED"));
        ts.setJsonFileName("selectLogicalResource.xml");
        resultInfo = Steps.PROTOCOLS.businessLogicBSCS
                .selectLogicalResource("Protocol : Get Free IP Number", this, customerInfo, ts, resultInfo)
                .run();
        Jive.log("Selected IP : " + customerInfo.getresource().toString());
        customerInfo.setIpv4(customerInfo.getresource().toString());

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
        JSONObject jsonObject = JsonHelper.updatePOOi(ts.getResponseBody(), "resourceNumber", customerInfo.getIpv4());
        jsonObject = JsonHelper.updatePOOiResources(jsonObject.toString(), "IAI",
                (customerInfo.getIaidNumber()).toString(), "value");
        ts.setResponseBody(jsonObject.toString());
        Steps.PROTOCOLS.businessLogicEOC.updatePooi("updating the basic PO with IP resource", this, customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision : Submit Shopping Cart For Customer A : " + customerInfo.getCustomerId(),
                        this, customerInfo, ts, resultInfo)
                .run();
        ts.setUdrTextFileName("ISDN_IP_IAI_UDR.txt");
        String body = RawMessages.getBodyFromTextFilePlacedInTestStep(this.getClass().getName(), "protocols/bscs/udrTemplates",
                ts.getUdrTextFileName()).toString();
        body = body.replace("{{IPV4}}", customerInfo.getIpv4());
        body = body.replace("{{IAID}}", customerInfo.getIaidNumber());
        body = body.replace("{{DATE}}", calculateDateTime.getCurrentDateTime());
        ts.setResponseBody(body);
        Steps.CHARGING_OFFLINE.stepsChargingOfflineBscs
                .offlineGy("Protocol : BSCS - Offline Internet IP Usage", this, resultInfo, customerInfo, ts)
                .run();

        /* =================Adding second resource================= */

        ts.setJsonFileName("createShoppingCart.json");
        resultInfo = Steps.PROVISION.eoc.createShoppingCart("Provision : Create Shopping Cart For Optional APN & IP PO",
                this, customerInfo, ts, resultInfo).run();
        Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart To get Basic POOI", this, customerInfo, ts, resultInfo)
                .run();
        String pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("basic")), "id");
        ts.setBasicPoReferenceId(pooiId);
        customerInfo.setPooiId(pooiId);
        Steps.PROTOCOLS.businessLogicEOC
                .getPooi("Getting the POOI For Personalized usage", this, customerInfo, ts, resultInfo)
                .run();
        JSONObject getPOOIRequestBody = searchKeyAndUpdateJson(ts.getResponseBody(), "action", "Modify");
        ts.setResponseBody(getPOOIRequestBody.toString());
        Steps.PROTOCOLS.businessLogicEOC.updatePooi("Updation of POOI with Second Resource", this,
                customerInfo, ts, resultInfo).run();
        Steps.PROTOCOLS.businessLogicEOC.getResources("Get All Resource ", this, customerInfo, ts, resultInfo).run();
        String getRfsId = JsonPath.read(ts.getResponseBody(), "$.[1].rfs.id");
        ts.setResourceParentId(getRfsId);
        String resourceSpecification = JsonPath.read(ts.getResponseBody(), "$.[1].resourceSpecification");
        ts.setResourceSpecification(resourceSpecification);
        customerInfo.setLogicalResourceType("IPV4");
        customerInfo.setNpCodePub(businessCache.get("IPV4.NPCODEPUB"));
        customerInfo.setPlCodePub(businessCache.get("IPV4.PLCODEPUB"));
        customerInfo.setSubmIdPub(businessCache.get("IPV4.SUBMIDPUB"));
        customerInfo.setHlCode(businessCache.get("IPV4.HLCODE"));
        customerInfo.setRsCode(businessCache.get("IPV4.RSCODE"));
        customerInfo.setCsControlled(businessCache.get("IPV4.CSCONTROLLED"));
        ts.setJsonFileName("selectLogicalResource.xml");
        resultInfo = Steps.PROTOCOLS.businessLogicBSCS
                .selectLogicalResource("Protocol : Get Free IP Number", this, customerInfo, ts, resultInfo)
                .run();
        Jive.log("Selected ExtraIP : " + customerInfo.getresource().toString());
        customerInfo.setIpv4(customerInfo.getresource().toString());
        Steps.PROTOCOLS.businessLogicEOC.addIpv4Resources("Provision: Adding Extra Resource ", this, customerInfo, ts, resultInfo).run();
        Steps.PROTOCOLS.businessLogicEOC.submitShoppingCart(
                "Provision : Submit Shopping Cart with Extra Resource : " + customerInfo.getCustomerId(), this,
                customerInfo, ts, resultInfo).run();

        /* ===Usage for Internet IP==== */
        ts.setInvoiceCharge(businessCache.get("INVOICE.CHARGE_IPV4"));
        ts.setInvoicePdfPageNo(Integer.parseInt(businessCache.get("INVOICE.PAGENO")));
        ts.setRemoveInvoicePdfFile(Boolean.getBoolean(businessCache.get("INVOICE.REMOVE")));
        /* ========for second ip====== */
        ts.setUdrTextFileName("ISDN_IP_IAI_UDR.txt");
        body = RawMessages.getBodyFromTextFilePlacedInTestStep(this.getClass().getName(), "protocols/bscs/udrTemplates",
                ts.getUdrTextFileName()).toString();
        body = body.replace("{{IPV4}}", customerInfo.getIpv4());
        body = body.replace("{{IAID}}", customerInfo.getIaidNumber());
        body = body.replace("{{DATE}}", calculateDateTime.getCurrentDateTime());
        ts.setResponseBody(body);
        Steps.CHARGING_OFFLINE.stepsChargingOfflineBscs
                .offlineGy("Protocol : BSCS - Offline Internet ExtraIP Usage :" + customerInfo.getIpv4(), this, resultInfo, customerInfo,
                        ts)
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
           Jive.failAndContinue("FAILED : Invoice PDF Charges : " + ts.getInvoiceCharge() + " are Not Validated Successfully!! Please Check the Invoice Content : "
                    + readInvoiceData);
        }
    }

}
