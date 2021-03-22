package com.ericsson.soiv.testcases.scenes.cbio18;

import static com.ericsson.soiv.utils.JsonHelper.getRequestedJsonElement;
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

//Created By EMODRED 26-June-2019

@Fixture (SoivFixture.class)
public class PrepaidPOGSMTechnologyMsisdnApnIpRating extends SoivTestBase
{
    private TestResultInfo resultInfo = new TestResultInfo();
    private CustomerInfo customerInfo = new CustomerInfo();
    private TransactionSpecification ts = new TransactionSpecification();
    private LinkedHashMap<String, String> productOfferings = new LinkedHashMap<String, String>();

    @Test
    @Id ("00000058")
    @Tags ("OT-Regression, OT-Postpaid, EC-Controlled, OT-CBiO18, PENDING-Update")
    @Description ("ResourceHandling GSM Technology with MSISDN Resource:BUC22A_A")
    public void prepaidPOGSMTechnologyMsisdnApnIpRating() throws IOException
    {

        LinkedHashMap<String, String> businessCache = new LinkedHashMap<>(readBusinessCacheData(this.getTestCase().getId()));

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

        /* ===Usage for GSM APN==== */
        customerInfo.setCallingParty(customerInfo.getAPN());
        Steps.CHARGING_ONLINE.simulator
                .dccGyData("Protocol : Dcc Gy data call for APN resource", this, resultInfo, customerInfo, ts)
                .run();
        /*=====Failure case has to be handled in DCC protocal=============*/
        /* ===Usage for GSM IP==== */

        /*=========PENDING=========*/

    }

}
