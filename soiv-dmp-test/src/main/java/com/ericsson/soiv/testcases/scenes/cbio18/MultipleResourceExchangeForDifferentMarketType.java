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

// Created by EMODRED on 06-August-2019
@Fixture (SoivFixture.class)
public class MultipleResourceExchangeForDifferentMarketType extends SoivTestBase
{

    private TestResultInfo resultInfo = new TestResultInfo();
    private CustomerInfo customerInfo = new CustomerInfo();
    private TransactionSpecification ts = new TransactionSpecification();
    private LinkedHashMap<String, String> productOfferings = new LinkedHashMap<String, String>();

    @Test
    @Id ("00000075")
    @Tags ("OT-Regression, OT-Postpaid,BSCS-Controlled, OT-CBiO18,PENDING-Update")
    @Description ("Multiple Resource exchange for different market type")
    public void multipleResourceExchangeForDifferentMarketType() throws IOException
    {

        LinkedHashMap<String, String> businessCache = new LinkedHashMap<>(
                readBusinessCacheData(this.getTestCase().getId()));

        customerInfo.setCustomerBillCycle(businessCache.get("CUSTOMER.BILLCYCLE"));
        customerInfo.setOrderMode(businessCache.get("ORDER.MODE"));
        customerInfo.setRequester(businessCache.get("ORDER.REQUESTER"));

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

        /***************************
         * Adding GSM IP Optional PO
         ***********************************/
        productOfferings.put("optional", businessCache.get("PO.ID_OPTIONAL_IP"));
        ts.setProductOfferingIds(productOfferings);
        Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart To get Basic POOI", this, customerInfo, ts, resultInfo)
                .run();
        pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("basic")), "id");
        ts.setBasicPoReferenceId(pooiId);
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
        customerInfo.setMarketType("GSM");
        ts.setJsonFileName("AddOptionalPOonBasic.json");
        Steps.PROTOCOLS.businessLogicEOC.addOptionalPOonBasicPO("Provision : Add Optional PO in to Basic PO", this,
                customerInfo, ts, resultInfo).run();
        /**************************
         * Updating the Optional PO with the resources APN and IPV4
         ****************************/
        jsonObject = JsonHelper.updatePOOiResources(ts.getResponseBody(), "IPV4", customerInfo.getIpv4(), "value");
        ts.setResponseBody(jsonObject.toString());
        Steps.PROTOCOLS.businessLogicEOC.updatePooi("Provision : Update the Optional  PO for resources IPV4", this,
                customerInfo, ts, resultInfo).run();
        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Submit cart for Resources in Optional PO", this, customerInfo, ts, resultInfo)
                .run();

        //		/* ===Usage for GSM APN==== */
        //		customerInfo.setCallingParty(customerInfo.getAPN());
        //		Steps.CHARGING_ONLINE.simulator
        //				.dccGyData("Protocol : Dcc Gy data call for APN resource", this, resultInfo, customerInfo, ts).run();

        /* ===Usage for GSM IP==== */

        /* =========PENDING========= */

        /*
         * ==========================================The second market
         * referance====================================================
         */
        /* ====================== */
        /* ======for CS PO========= */
        /* ====================== */
        productOfferings.put("basic", businessCache.get("PO.ID_BASIC_ISDN_OLID"));
        ts.setProductOfferingIds(productOfferings);
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
        customerInfo.setMarketType("ISD");
        customerInfo.setMarket(businessCache.get("BASICPO.MARKET"));
        customerInfo.setSubMarket(businessCache.get("BASICPO.SUB_MARKET"));
        customerInfo.setNetwork(businessCache.get("BASICPO.NETWORK"));

        ts.setJsonFileName("addProductOfferingShoppingCart.json");
        resultInfo = Steps.PROVISION.eoc
                .addPoInShoppingCart("Provision : Add PO in Shopping Cart", this, customerInfo, ts, resultInfo)
                .run();
        JSONObject jsonobject = JsonHelper.updatePOOi(ts.getResponseBody(), "resourceNumber", customerInfo.getIsdn());
        jsonobject = JsonHelper.updatePOOi(jsonobject.toString(), "SDP_IP", "1");
        jsonobject = JsonHelper.updatePOOiResources(jsonobject.toString(), "OLID",
                (customerInfo.getOlidNumber()).toString(), "value");

        //        jsonobject = JsonHelper.updatePOOi(jsonobject.toString(), "hlrcode", businessCache.get("HLRCODE.VALUE"));
        //        jsonObject = JsonHelper.updatePOOi(jsonObject.toString(), "SDP_IP", businessCache.get("SDP_ID.VALUE"));
        //        jsonobject = JsonHelper.updatePOOi(jsonobject.toString(), "prefix", businessCache.get("PREFIX.VALUE"));

        ts.setResponseBody(jsonobject.toString());
        Steps.PROTOCOLS.businessLogicEOC.updatePooi("updating the basic PO", this, customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision : Submit Shopping Cart For Customer A : " + customerInfo.getCustomerId(),
                        this, customerInfo, ts, resultInfo)
                .run();

        /*===========For second resource in second contract=====*/

        productOfferings.put("optional", businessCache.get("PO.ID_OPTIONAL_IP_RATING"));
        ts.setProductOfferingIds(productOfferings);
        ts.setJsonFileName("createShoppingCart.json");
        resultInfo = Steps.PROVISION.eoc.createShoppingCart("Provision : Create Shopping Cart For Optional APN & IP PO",
                this, customerInfo, ts, resultInfo).run();
        Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart To get Basic POOI", this, customerInfo, ts, resultInfo)
                .run();
        pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("basic")), "id");
        ts.setBasicPoReferenceId(pooiId);
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

        ts.setJsonFileName("AddOptionalPOonBasic.json");
        Steps.PROTOCOLS.businessLogicEOC.addOptionalPOonBasicPO("Provision : Add Optional PO in to Basic PO", this,
                customerInfo, ts, resultInfo).run();
        /**************************
         * Updating the Optional PO with the resources APN and IPV4
         ****************************/
        jsonObject = JsonHelper.updatePOOiResources(ts.getResponseBody(), "IPV4", customerInfo.getIpv4(),
                "value");
        ts.setResponseBody(jsonObject.toString());
        Steps.PROTOCOLS.businessLogicEOC.updatePooi("Provision : Update the Optional  PO for resources IPV4", this,
                customerInfo, ts, resultInfo).run();
        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Submit cart for Resources in Optional PO", this, customerInfo, ts, resultInfo)
                .run();

        /*======Exchange of APN resource=========*/

        //        ts.setJsonFileName("createShoppingCart.json");
        //        Steps.PROVISION.eoc.createShoppingCart("Creating Shopping Cart with existing Customer", this, customerInfo, ts, resultInfo).run();
        productOfferings.put("optional", businessCache.get("PO.ID_OPTIONAL_APN"));

        /*========Exchanging APN resource=====*/
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
        Jive.log("Selected APN for Exchange : " + customerInfo.getresource().toString());
        customerInfo.setAPN(customerInfo.getresource().toString());

        /*===========Exchanging Resources====================*/
        ts.setJsonFileName("createShoppingCart.json");
        Steps.PROVISION.eoc.createShoppingCart("Creating Shopping Cart with existing Customer", this, customerInfo, ts, resultInfo).run();
        Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart To get Optional POOI", this, customerInfo, ts, resultInfo)
                .run();

        /*===APN===*/
        pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("optional")), "id");
        customerInfo.setPooiId(pooiId);
        Steps.PROTOCOLS.businessLogicEOC
                .getPooi("Getting the POOI To add the exchange Resource Number", this, customerInfo, ts, resultInfo)
                .run();
        JSONObject getPOOIRequestBody = searchKeyAndUpdateJson(ts.getResponseBody(), "action", "Modify");
        ts.setResponseBody(getPOOIRequestBody.toString());
        jsonObject =
                JsonHelper.updatePOOi(ts.getResponseBody(), "resourceNumberExchange", customerInfo.getAPN());
        ts.setResponseBody(jsonObject.toString());
        Steps.PROTOCOLS.businessLogicEOC.updatePooi("Provision : Update the Optional POOI with Exchange Resource Number", this,
                customerInfo, ts, resultInfo).run();
        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Submit cart with Exchange resource Number", this, customerInfo, ts, resultInfo)
                .run();

    }
}
