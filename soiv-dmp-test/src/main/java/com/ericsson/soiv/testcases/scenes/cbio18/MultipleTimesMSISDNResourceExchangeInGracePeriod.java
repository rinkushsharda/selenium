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
import com.ericsson.jive.core.protocols.http.messagetypes.HttpMethod;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpResponse;
import com.ericsson.soiv.fixtures.SoivFixture;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.CustomerInfo;
import com.ericsson.soiv.utils.JsonHelper;
import com.ericsson.soiv.utils.TestResultInfo;
import com.ericsson.soiv.utils.TransactionSpecification;
import com.jayway.jsonpath.JsonPath;

//Created By EMODRED 06-Aug-2019

@Fixture (SoivFixture.class)
public class MultipleTimesMSISDNResourceExchangeInGracePeriod extends SoivTestBase
{
    private TestResultInfo resultInfo = new TestResultInfo();
    private CustomerInfo customerInfo = new CustomerInfo();
    private TransactionSpecification ts = new TransactionSpecification();
    private LinkedHashMap<String, String> productOfferings = new LinkedHashMap<String, String>();

    @Test
    @Id ("00000072")
    @Tags ("OT-Regression, OT-Postpaid, EC-Controlled, OT-CBiO18")
    @Description ("Logical Resource Exchange GSM Technology with MSISDN Resource and APN Rating 22B_1")
    public void multipleTimesMSISDNResourceExchangeInGracePeriod() throws IOException, InterruptedException {

        LinkedHashMap<String, String> businessCache = new LinkedHashMap<>(
                readBusinessCacheData(this.getTestCase().getId()));

        customerInfo.setCustomerBillCycle(businessCache.get("CUSTOMER.BILLCYCLE"));
        customerInfo.setOrderMode(businessCache.get("ORDER.MODE"));
        customerInfo.setRequester(businessCache.get("ORDER.REQUESTER"));
        ts.setInvoicePdfPageNo(Integer.parseInt(businessCache.get("INVOICE.PAGE_1")));
        ts.setRemoveInvoicePdfFile(Boolean.getBoolean(businessCache.get("INVOICE.REMOVE")));

        /* ====================================== */
        /* ================GSM CS PO============= */
        /* ====================================== */
        productOfferings.put("basic", businessCache.get("PO.ID_BASIC"));
        productOfferings.put("optional", businessCache.get("PO.ID_OPTIONAL_APN"));
        customerInfo.setOrderMode(businessCache.get("ORDER.MODE"));
        customerInfo.setRequester(businessCache.get("ORDER.REQUESTER"));
        ts.setProductOfferingIds(productOfferings);
        customerInfo.setNpCodePub(businessCache.get("MSISDN.NPCODEPUB"));
        customerInfo.setPlCodePub(businessCache.get("MSISDN.PLCODEPUB"));
        customerInfo.setSubmIdPub(businessCache.get("MSISDN.SUBMIDPUB"));
        customerInfo.setHlCode(businessCache.get("MSISDN.HLCODE"));
        customerInfo.setRsCode(businessCache.get("MSISDN.RSCODE"));
        customerInfo.setCsControlled(businessCache.get("MSISDN.CSCONTROLLED"));
        Steps.PROVISION.eoc.createCustomerContractBasicPO("Create Customer and Contract with Basic PO", this,
                customerInfo, ts, resultInfo).run();

        /***************************
         * Adding GSM-APN optional PO
         ***********************************/

        Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart To get Basic POOI", this, customerInfo, ts, resultInfo)
                .run();
        String pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("basic")), "id");
        ts.setBasicPoReferenceId(pooiId);
        customerInfo.setLogicalResourceType("APN");
        customerInfo.setNpCodePub(businessCache.get("APN.NPCODEPUB"));
        customerInfo.setPlCodePub(businessCache.get("APN.PLCODEPUB"));
        customerInfo.setSubmIdPub(businessCache.get("APN.SUBMIDPUB"));
        customerInfo.setHlCode(businessCache.get("APN.HLCODE"));
        customerInfo.setRsCode(businessCache.get("APN.RSCODE"));
        customerInfo.setCsControlled(businessCache.get("APN.CSCONTROLLED"));
        ts.setJsonFileName("selectLogicalResource.xml");
        resultInfo = Steps.PROTOCOLS.businessLogicBSCS
                .selectLogicalResource("Protocol : Get Free APN Number", this, customerInfo, ts, resultInfo)
                .run();
        Jive.log("Selected APN : " + customerInfo.getresource().toString());
        customerInfo.setAPN(customerInfo.getresource().toString());
        customerInfo.setMarketType("GSM");
        ts.setJsonFileName("AddOptionalPOonBasic.json");
        Steps.PROTOCOLS.businessLogicEOC.addOptionalPOonBasicPO("Provision : Add Optional PO in to Basic PO", this,
                customerInfo, ts, resultInfo).run();
        /***************************
         * Updating the Optional PO with the resources APN
         ****************************/
        JSONObject jsonObject = JsonHelper.updatePOOiResources(ts.getResponseBody(), "APN", customerInfo.getAPN(),
                "value");
        ts.setResponseBody(jsonObject.toString());
        Steps.PROTOCOLS.businessLogicEOC
                .updatePooi("Provision : Update the Optional  PO for resources APN", this, customerInfo, ts, resultInfo)
                .run();
        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision : Submit Shopping Cart for Resources in Optional PO", this, customerInfo,
                        ts, resultInfo)
                .run();

        /* ===Usage for GSM APN==== */
        customerInfo.setCallingParty(customerInfo.getAPN());
        ts.setExpectedCost(businessCache.get("APN.USAGE_EXPECTED_COST"));
        Steps.CHARGING_ONLINE.simulator
                .dccGyData("Protocol : Dcc Gy data call for APN resource", this, resultInfo, customerInfo, ts)
                .run();
        /* ===Usage for GSM MSISDN==== */
        ts.setExpectedCost(businessCache.get("MSISDN.USAGE_EXPECTED_COST"));
        ts.setRatingGroup(businessCache.get("MSISDN.USAGE.RATING_GROUP"));
        Steps.CHARGING_ONLINE.simulator
                .onlineGy("Protocol : Data call for MSISDN number", this, resultInfo, customerInfo, ts)
                .run();

        /* ===Exchanging MSISDN Resources=== */
        customerInfo.setNpCodePub(businessCache.get("MSISDN.NPCODEPUB"));
        customerInfo.setPlCodePub(businessCache.get("MSISDN.PLCODEPUB"));
        customerInfo.setSubmIdPub(businessCache.get("MSISDN.SUBMIDPUB"));
        customerInfo.setHlCode(businessCache.get("MSISDN.HLCODE"));
        customerInfo.setRsCode(businessCache.get("MSISDN.RSCODE"));
        customerInfo.setCsControlled(businessCache.get("MSISDN.CSCONTROLLED"));
        ts.setJsonFileName("selectLogicalResource.xml");
        resultInfo = Steps.PROTOCOLS.businessLogicBSCS
                .selectLogicalResource("Protocol : Get Free MSISDN Number", this, customerInfo, ts, resultInfo)
                .run();
        customerInfo.setMsisdn(customerInfo.getresource().toString());
        String msisdn = customerInfo.getMsisdn();
        Jive.log("Selected MSISDN for Exchange : " + msisdn);
        /* ===========Exchanging Resources==================== */
        ts.setJsonFileName("createShoppingCart.json");
        Steps.PROVISION.eoc
                .createShoppingCart("Creating Shopping Cart with existing Customer", this, customerInfo, ts, resultInfo)
                .run();
        Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart To get Basic POOI", this, customerInfo, ts, resultInfo)
                .run();
        /* ===MSISDN=== */
        pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("basic")), "id");
        customerInfo.setPooiId(pooiId);
        Steps.PROTOCOLS.businessLogicEOC
                .getPooi("Getting the POOI To add the exchange Resource Number", this, customerInfo, ts, resultInfo)
                .run();
        JSONObject getPOOIRequestBody = searchKeyAndUpdateJson(ts.getResponseBody(), "action", "Modify");
        ts.setResponseBody(getPOOIRequestBody.toString());
        jsonObject = JsonHelper.updatePOOi(ts.getResponseBody(), "resourceNumberExchange", customerInfo.getMsisdn());
        ts.setResponseBody(jsonObject.toString());
        Steps.PROTOCOLS.businessLogicEOC
                .updatePooi("Provision : Update the basic pooi with MSISDN Exchange Resource Number", this,
                        customerInfo, ts, resultInfo)
                .run();
        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision : Submit Shopping Cart with Exchange resource Number", this,
                        customerInfo, ts, resultInfo)
                .run();

        /* ===Usage for GSM APN==== */
        customerInfo.setCallingParty(customerInfo.getAPN());
        ts.setExpectedCost(businessCache.get("APN.USAGE_EXPECTED_COST"));
        Steps.CHARGING_ONLINE.simulator
                .dccGyData("Protocol : Dcc Gy data call for APN resource", this, resultInfo, customerInfo, ts)
                .run();
        /* ===Usage for GSM MSISDN==== */
        ts.setExpectedCost(businessCache.get("MSISDN.USAGE_EXPECTED_COST"));
        ts.setRatingGroup(businessCache.get("MSISDN.USAGE.RATING_GROUP"));
        Steps.CHARGING_ONLINE.simulator
                .onlineGy("Protocol : Data call for MSISDN number", this, resultInfo, customerInfo, ts)
                .run();
        Thread.sleep(60000);
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
        /* ===Exchanging MSISDN Resources=== */
        customerInfo.setNpCodePub(businessCache.get("MSISDN.NPCODEPUB"));
        customerInfo.setPlCodePub(businessCache.get("MSISDN.PLCODEPUB"));
        customerInfo.setSubmIdPub(businessCache.get("MSISDN.SUBMIDPUB"));
        customerInfo.setHlCode(businessCache.get("MSISDN.HLCODE"));
        customerInfo.setRsCode(businessCache.get("MSISDN.RSCODE"));
        customerInfo.setCsControlled(businessCache.get("MSISDN.CSCONTROLLED"));
        ts.setJsonFileName("selectLogicalResource.xml");
        resultInfo = Steps.PROTOCOLS.businessLogicBSCS
                .selectLogicalResource("Protocol : Get Free MSISDN Number", this, customerInfo, ts, resultInfo)
                .run();

        customerInfo.setMsisdn(customerInfo.getresource().toString());
        msisdn = customerInfo.getMsisdn();
        Jive.log("Selected MSISDN for Exchange : " + msisdn);
        /* ===========Exchanging Resources==================== */
        ts.setJsonFileName("createShoppingCart.json");
        Steps.PROVISION.eoc
                .createShoppingCart("Creating Shopping Cart with existing Customer", this, customerInfo, ts, resultInfo)
                .run();
        Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart To get Basic POOI", this, customerInfo, ts, resultInfo)
                .run();
        /* ===MSISDN=== */
        pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("basic")), "id");
        customerInfo.setPooiId(pooiId);
        Steps.PROTOCOLS.businessLogicEOC
                .getPooi("Getting the POOI To add the exchange Resource Number", this, customerInfo, ts, resultInfo)
                .run();
        getPOOIRequestBody = searchKeyAndUpdateJson(ts.getResponseBody(), "action", "Modify");
        ts.setResponseBody(getPOOIRequestBody.toString());
        jsonObject = JsonHelper.updatePOOi(ts.getResponseBody(), "resourceNumberExchange", customerInfo.getMsisdn());
        ts.setResponseBody(jsonObject.toString());
        Steps.PROTOCOLS.businessLogicEOC
                .updatePooi("Provision : Update the basic pooi with MSISDN Exchange Resource Number", this,
                        customerInfo, ts, resultInfo)
                .run();

        resultInfo = Steps.PROTOCOLS.eoc.sendEocRequest(this.getClass().getName(), resultInfo, HttpMethod.POST, 404,
                "/eoc/on/v1/sc/" + customerInfo.getShoppingCartId() + "/present").run();

        HttpResponse response = (HttpResponse) resultInfo.getResult();
        String respBody = (String) response.getBody().getValue();
        if (respBody.contains("cannot be exchanged because it is still within the Resource Exchange Grace Period"))
        {
            Jive.log("OK : Resource can't be exchange multiple times in grace period verified successfully");
        }
        else
        {
            Jive.failAndContinue("Failed : Resource is exchanging multiple times during gance period");
        }

    }

}
