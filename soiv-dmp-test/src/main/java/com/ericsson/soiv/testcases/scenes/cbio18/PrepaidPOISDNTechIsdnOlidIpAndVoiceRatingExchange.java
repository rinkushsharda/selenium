package com.ericsson.soiv.testcases.scenes.cbio18;

import static com.ericsson.soiv.utils.JsonHelper.getRequestedJsonElement;
import static com.ericsson.soiv.utils.JsonHelper.searchKeyAndUpdateJson;
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
import com.ericsson.soiv.utils.CustomerInfo;
import com.ericsson.soiv.utils.JsonHelper;
import com.ericsson.soiv.utils.TestResultInfo;
import com.ericsson.soiv.utils.TransactionSpecification;
import com.jayway.jsonpath.JsonPath;

//Created By EMODRED 06-Aug-2019

@Fixture (SoivFixture.class)
public class PrepaidPOISDNTechIsdnOlidIpAndVoiceRatingExchange extends SoivTestBase
{
    private TestResultInfo resultInfo = new TestResultInfo();
    private CustomerInfo customerInfo = new CustomerInfo();
    private TransactionSpecification ts = new TransactionSpecification();

    @Test
    @Id ("00000061")
    @Tags ("OT-Regression, OT-Postpaid, EC-Controlled, OT-CBiO18, PENDING-Update")
    @Description ("ResourceHandling ISDN Technology:BUC22A_A")
    public void prepaidPOISDNTechIsdnOlidIpAndVoiceRatingExchange() throws IOException, SQLException, InterruptedException
    {

        LinkedHashMap<String, String> businessCache = new LinkedHashMap<>(
                readBusinessCacheData(this.getTestCase().getId()));
        customerInfo.setCustomerBillCycle(businessCache.get("CUSTOMER.BILLCYCLE"));
        customerInfo.setOrderMode(businessCache.get("ORDER.MODE"));
        customerInfo.setRequester(businessCache.get("ORDER.REQUESTER"));

        ts.setInvoicePdfPageNo(Integer.parseInt(businessCache.get("INVOICE.PAGENO")));
        ts.setRemoveInvoicePdfFile(Boolean.getBoolean(businessCache.get("INVOICE.REMOVE")));

        LinkedHashMap<String, String> productOfferings = new LinkedHashMap<String, String>();

        /* ====================== */
        /* ======for CS PO========= */
        /* ====================== */
        productOfferings.put("basic", businessCache.get("PO.ID_BASIC_ISDN_OLID"));
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
        ts.setJsonFileName("portSearch.xml");
        customerInfo.setSubmIdPub(businessCache.get("PORT.SUBMIDPUB"));
        customerInfo.setPortNpPub(businessCache.get("PORT.PORTNPPUB"));
        customerInfo.setPlCodePub(businessCache.get("PORT.PLCODEPUB"));
        customerInfo.setHlCode(businessCache.get("PORT.HLCODE"));

        Steps.PROTOCOLS.businessLogicBSCS.portSearch("Search Port Number for OLID ", this, customerInfo, ts, resultInfo)
                .run();
        Long olidNumber = JsonPath.read(ts.getResponseBody(),
                "$.SOAP-ENV:Envelope.SOAP-ENV:Body.portsSearchResponse.returnedPorts.item.portnum.content");
        customerInfo.setOlidNumber(olidNumber);
        Jive.log("Selected OLID : " + olidNumber);
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
        ts.setResponseBody(jsonobject.toString());
        Steps.PROTOCOLS.businessLogicEOC.updatePooi("updating the basic PO", this, customerInfo, ts, resultInfo).run();

        /* ====Adding OPtional PO over the Basic PO==== */
        productOfferings.put("optional_ip", businessCache.get("PO.ID_OPTIONAL_IP_RATING"));
        ts.setProductOfferingIds(productOfferings);
        Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart To get Basic POOI", this, customerInfo, ts, resultInfo)
                .run();
        String pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("basic")), "id");
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
        customerInfo.setIpv4(customerInfo.getresource().toString());
        Jive.log("Selected IP : " + customerInfo.getresource().toString());

        ts.setJsonFileName("AddOptionalPOonBasic.json");
        productOfferings.put("optional", productOfferings.get("optional_ip"));
        ts.setProductOfferingIds(productOfferings);
        Steps.PROTOCOLS.businessLogicEOC.addOptionalPOonBasicPO("Provision : Add Optional PO in to Basic PO", this,
                customerInfo, ts, resultInfo).run();
        /**************************
         * Updating the Optional PO with the resources APN and IPV4
         ****************************/
        JSONObject jsonObject = JsonHelper.updatePOOiResources(ts.getResponseBody(), "IPV4", customerInfo.getIpv4(),
                "value");
        ts.setResponseBody(jsonObject.toString());
        Steps.PROTOCOLS.businessLogicEOC.updatePooi("Provision : Update the Optional  PO for resources IPV4", this,
                customerInfo, ts, resultInfo).run();
        /* ====Adding 2nd OPtional PO over the Basic PO==== */
        productOfferings.put("optional_voice", businessCache.get("PO.ID_OPTIONAL_VOICE_RATING"));
        ts.setProductOfferingIds(productOfferings);
        Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart To get Basic POOI", this, customerInfo, ts, resultInfo)
                .run();
        pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("basic")), "id");
        ts.setBasicPoReferenceId(pooiId);
        ts.setJsonFileName("AddOptionalPOonBasic.json");
        productOfferings.put("optional", productOfferings.get("optional_voice"));
        ts.setProductOfferingIds(productOfferings);
        Steps.PROTOCOLS.businessLogicEOC.addOptionalPOonBasicPO("Provision : Add Optional PO in to Basic PO", this,
                customerInfo, ts, resultInfo).run();
        Steps.PROTOCOLS.businessLogicEOC.updatePooi("Provision : Update the Optional  PO for resources IPV4", this,
                customerInfo, ts, resultInfo).run();
        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Submit cart for Resources in Optional PO", this, customerInfo, ts, resultInfo)
                .run();

        resultInfo = Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart To get Basic POOI", this, customerInfo, ts, resultInfo)
                .run();
        /* =============Voice usage for ISDN number=========== */
        String isdnNumber = JsonPath.read(ts.getResponseBody(),
                "$.items[0].item.resources[0].resourceCharacteristics[12].value");
        customerInfo.setMsisdn(isdnNumber);
        ts.setDuration(Integer.parseInt(businessCache.get("ISDN.DURATION")));
        ts.setExpectedCost(businessCache.get("ISDN.USAGE_EXPECTED_COST"));
        Steps.CHARGING_ONLINE.simulator
                .onlineCip("Protocol : Data call for ISDN number", this, resultInfo, customerInfo, ts)
                .run();

        /* =======Data Call for IP Resource has to added here====== */

        /*=======ISDN Resource Exchange============*/

        /*==========ISDN Exchange==========*/

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
        Jive.log("Selected ISDN for Exchange: " + customerInfo.getIsdn());

        ts.setJsonFileName("createShoppingCart.json");
        Steps.PROVISION.eoc.createShoppingCart("Creating Shopping Cart with existing Customer", this, customerInfo, ts, resultInfo).run();
        Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart To check", this, customerInfo, ts, resultInfo)
                .run();

        pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("basic")), "id");
        customerInfo.setPooiId(pooiId);
        Steps.PROTOCOLS.businessLogicEOC
                .getPooi("Getting the POOI To add the exchange ISDN Resource Number", this, customerInfo, ts, resultInfo)
                .run();
        JSONObject getPOOIRequestBody = searchKeyAndUpdateJson(ts.getResponseBody(), "action", "Modify");
        ts.setResponseBody(getPOOIRequestBody.toString());
        jsonObject =
                JsonHelper.updatePOOiResourcesByIndex(ts.getResponseBody(), "resourceNumberExchange", customerInfo.getIsdn(),
                        "name", 0);
        ts.setResponseBody(jsonObject.toString());
        Steps.PROTOCOLS.businessLogicEOC.updatePooi("Provision : Update the Optional POOI with Exchange Resource Number", this,
                customerInfo, ts, resultInfo).run();
        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Submit cart with Exchange resource Number", this, customerInfo, ts, resultInfo)
                .run();

        /*==========IP Exchange==========*/

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
        customerInfo.setIpv4(customerInfo.getresource().toString());
        Jive.log("Selected IP for Exchange: " + customerInfo.getresource().toString());

        ts.setJsonFileName("createShoppingCart.json");
        Steps.PROVISION.eoc.createShoppingCart("Creating Shopping Cart with existing Customer", this, customerInfo, ts, resultInfo).run();
        Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart To check", this, customerInfo, ts, resultInfo)
                .run();

        pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("optional_ip")), "id");
        customerInfo.setPooiId(pooiId);
        Steps.PROTOCOLS.businessLogicEOC
                .getPooi("Getting the POOI To add the exchange IP Resource Number", this, customerInfo, ts, resultInfo)
                .run();
        getPOOIRequestBody = searchKeyAndUpdateJson(ts.getResponseBody(), "action", "Modify");
        ts.setResponseBody(getPOOIRequestBody.toString());
        jsonObject =
                JsonHelper.updatePOOiResourcesByIndex(ts.getResponseBody(), "resourceNumberExchange", customerInfo.getIpv4(),
                        "name", 0);
        ts.setResponseBody(jsonObject.toString());
        Steps.PROTOCOLS.businessLogicEOC.updatePooi("Provision : Update the Optional POOI with Exchange Resource Number", this,
                customerInfo, ts, resultInfo).run();
        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Submit cart with Exchange resource Number", this, customerInfo, ts, resultInfo)
                .run();

        /* =============Voice usage for ISDN number=========== */
        isdnNumber = customerInfo.getIsdn();
        customerInfo.setMsisdn(isdnNumber);
        ts.setDuration(Integer.parseInt(businessCache.get("ISDN.DURATION")));
        ts.setExpectedCost(businessCache.get("ISDN.USAGE_EXPECTED_COST"));
        Steps.CHARGING_ONLINE.simulator
                .onlineCip("Protocol : Data call for ISDN number", this, resultInfo, customerInfo, ts)
                .run();

        /* =======Data Call for IP Resource has to added here====== */

    }
}
