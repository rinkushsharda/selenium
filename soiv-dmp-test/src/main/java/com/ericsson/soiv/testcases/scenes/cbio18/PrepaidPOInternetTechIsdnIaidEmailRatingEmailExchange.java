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
import com.ericsson.soiv.utils.CustomerInfo;
import com.ericsson.soiv.utils.JsonHelper;
import com.ericsson.soiv.utils.TestResultInfo;
import com.ericsson.soiv.utils.TransactionSpecification;
import com.jayway.jsonpath.JsonPath;

//Created By EMODRED 06-Aug-2019

@Fixture (SoivFixture.class)
public class PrepaidPOInternetTechIsdnIaidEmailRatingEmailExchange extends SoivTestBase
{
    private TestResultInfo resultInfo = new TestResultInfo();
    private CustomerInfo customerInfo = new CustomerInfo();
    private TransactionSpecification ts = new TransactionSpecification();
    private LinkedHashMap<String, String> productOfferings = new LinkedHashMap<String, String>();

    @Test
    @Id ("00000070")
    @Tags ("OT-Regression, OT-Postpaid, EC-Controlled, OT-CBiO18,PENDING-Update")
    @Description ("ResourceHandlingSupportToMUltipleTechnologiesGSM BUC22A_A")
    public void prepaidPOInternetTechIsdnIaidEmailRatingEmailExchange() throws IOException
    {
        LinkedHashMap<String, String> businessCache = new LinkedHashMap<>(readBusinessCacheData(this.getTestCase().getId()));

        customerInfo.setCustomerBillCycle(businessCache.get("CUSTOMER.BILLCYCLE"));
        customerInfo.setOrderMode(businessCache.get("ORDER.MODE"));
        customerInfo.setRequester(businessCache.get("ORDER.REQUESTER"));

        /********** CS PO ******************/
        /****************************
         * Basic ISDn IAID with optional Email PO
         ***********************************/
        /***********************************/

        productOfferings.put("basic", businessCache.get("PO.ID_BASIC_ISDN_IAID"));
        customerInfo.setOrderMode(businessCache.get("ORDER.MODE"));
        customerInfo.setRequester(businessCache.get("ORDER.REQUESTER"));
        ts.setProductOfferingIds(productOfferings);

        resultInfo = Steps.PROVISION.bscs
                .createCustomer("Provision : Create Customer", this, customerInfo, ts, resultInfo)
                .run();
        ts.setJsonFileName("createShoppingCart.json");
        resultInfo = Steps.PROVISION.eoc
                .createShoppingCart("Provision : Create Shopping Cart", this, customerInfo, ts, resultInfo)
                .run();

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
        customerInfo.setIsdn(customerInfo.getresource().toString());
        Jive.log("Selected ISDN : " + customerInfo.getIsdn());

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

        JSONObject jsonObject = JsonHelper.updatePOOi(ts.getResponseBody(), "resourceNumber", customerInfo.getIsdn());
        jsonObject = JsonHelper.updatePOOiResources(jsonObject.toString(), "IAI",
                (customerInfo.getIaidNumber()).toString(), "value");
        jsonObject = JsonHelper.updatePOOi(jsonObject.toString(), "hlrcode", businessCache.get("HLRCODE.VALUE"));
        jsonObject = JsonHelper.updatePOOi(jsonObject.toString(), "SDP_IP", businessCache.get("SDP_ID.VALUE"));
        jsonObject = JsonHelper.updatePOOi(jsonObject.toString(), "prefix", businessCache.get("PREFIX.VALUE"));

        ts.setResponseBody(jsonObject.toString());
        Steps.PROTOCOLS.businessLogicEOC.updatePooi("updating the basic PO with ISDN resource", this, customerInfo, ts, resultInfo).run();

        /* ========Adding optional Email rating PO over Basic PO============== */
        productOfferings.put("optional", businessCache.get("PO.ID_OPTIONAL_EMAIL_RATING"));
        ts.setProductOfferingIds(productOfferings);
        Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart To get Basic POOI", this, customerInfo, ts, resultInfo)
                .run();
        String pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("basic")), "id");
        ts.setBasicPoReferenceId(pooiId);

        /***************************
         * Handling for the resource Email
         ***********************************/
        long number1 = (long) Math.floor(Math.random() * 90L) + 10L;
        long number2 = (long) Math.floor(Math.random() * 9_000L) + 1_000L;
        String randomEmail = number1 + "." + number2 + "@mail.com";
        Jive.log("Selected Email CS1 : " + randomEmail);
        customerInfo.setemail(randomEmail);
        ts.setJsonFileName("AddOptionalPOonBasic.json");
        Steps.PROTOCOLS.businessLogicEOC.addOptionalPOonBasicPO("Provision : Add Optional PO in to Basic PO", this,
                customerInfo, ts, resultInfo).run();

        jsonObject = JsonHelper.updatePOOiResources(ts.getResponseBody(), "Email", customerInfo.getemail(), "value");
        jsonObject = JsonHelper.updatePOOi(jsonObject.toString(), "hlrcode", businessCache.get("HLRCODE.VALUE"));
        ts.setResponseBody(jsonObject.toString());
        Steps.PROTOCOLS.businessLogicEOC.updatePooi("Provision : Update the Optional  PO for resources Email", this,
                customerInfo, ts, resultInfo).run();
        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Submit cart for Resources in Optional PO", this, customerInfo, ts, resultInfo)
                .run();

        /*=====Usage of ISDN=========*/
        customerInfo.setMsisdn(customerInfo.getIsdn());
        ts.setExpectedCost(businessCache.get("ISDN.USAGE_EXPECTED_COST"));
        ts.setRatingGroup(businessCache.get("USAGE.RATING_GROUP"));
        Steps.CHARGING_ONLINE.simulator
                .onlineGy("Protocol : Data call for ISDN number", this, resultInfo, customerInfo, ts)
                .run();
        /* =================usage has to be DONE For Email Resource============== */
        /*===================PENDING==================*/

        /*===========Exchanging Email Resource============*/
        ts.setJsonFileName("createShoppingCart.json");
        Steps.PROVISION.eoc.createShoppingCart("Creating Shopping Cart with existing Customer", this, customerInfo, ts, resultInfo).run();
        Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart To check", this, customerInfo, ts, resultInfo)
                .run();

        /*===Email===*/
        number1 = (long) Math.floor(Math.random() * 90L) + 10L;
        number2 = (long) Math.floor(Math.random() * 9_000L) + 1_000L;
        randomEmail = number1 + "." + number2 + "@mail.com";
        Jive.log("Selected Email Exchange : " + randomEmail);
        customerInfo.setemail(randomEmail);

        pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("optional")), "id");
        customerInfo.setPooiId(pooiId);
        Steps.PROTOCOLS.businessLogicEOC
                .getPooi("Getting the POOI To add the exchange Email Resource Number", this, customerInfo, ts, resultInfo)
                .run();
        JSONObject getPOOIRequestBody = searchKeyAndUpdateJson(ts.getResponseBody(), "action", "Modify");
        ts.setResponseBody(getPOOIRequestBody.toString());
        jsonObject =
                JsonHelper.updatePOOi(ts.getResponseBody(), "resourceNumberExchange", customerInfo.getemail());
        ts.setResponseBody(jsonObject.toString());
        Steps.PROTOCOLS.businessLogicEOC.updatePooi("Provision : Update the Optional POOI with Exchange Resource Number", this,
                customerInfo, ts, resultInfo).run();
        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Submit cart with Exchange resource Number", this, customerInfo, ts, resultInfo)
                .run();

        /* =================usage has to be DONE For Exchanged Email Resource============== */
        /*===================PENDING==================*/

    }

}
