package com.ericsson.soiv.testcases.scenes.cbio18;

import static com.ericsson.soiv.utils.HelperClass.getRandomNumberWithinRange;
import static com.ericsson.soiv.utils.JsonHelper.updatePOOi;
import static com.ericsson.soiv.utils.http.ReadBusinessData.readBusinessCacheData;

import java.io.IOException;
import java.util.LinkedHashMap;

import org.json.JSONObject;
import org.junit.Test;

import com.ericsson.jive.core.execution.Description;
import com.ericsson.jive.core.execution.Fixture;
import com.ericsson.jive.core.execution.Id;
import com.ericsson.jive.core.execution.Tags;
import com.ericsson.soiv.fixtures.SoivFixture;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.Constants;
import com.ericsson.soiv.utils.CustomerInfo;
import com.ericsson.soiv.utils.JsonHelper;
import com.ericsson.soiv.utils.TestResultInfo;
import com.ericsson.soiv.utils.TransactionSpecification;
import com.jayway.jsonpath.JsonPath;

//Created by ZEAHDCE on 17-July-2019

@Fixture(SoivFixture.class)
public class PrepaidCSPOwithTabletAndGSMTechnology extends SoivTestBase {
    private TestResultInfo resultInfo = new TestResultInfo();
    private CustomerInfo customerInfo = new CustomerInfo();
    private TransactionSpecification ts = new TransactionSpecification();

    @Test
    @Id("00000068")
    @Tags("OT-Regression, OT-Prepaid,EC-Controlled, OT-CBiO18,PENDING-Update")
    @Description("Cross Technology Product Offering with Tablet And GSM Technology")
    public void prepaidPOCScrossTechnology() throws IOException {

        LinkedHashMap<String, String> businessCache = new LinkedHashMap<>(
                readBusinessCacheData(this.getTestCase().getId()));
        customerInfo.setCustomerBillCycle(businessCache.get("CUSTOMER.BILLCYCLE"));
        customerInfo.setOrderMode(businessCache.get("ORDER.MODE"));
        customerInfo.setRequester(businessCache.get("ORDER.REQUESTER"));

        LinkedHashMap<String, String> productOfferings = new LinkedHashMap<String, String>();
        productOfferings.put("basic", businessCache.get("PO.ID_BASIC"));
        ts.setProductOfferingIds(productOfferings);

        /*****************************
         * createCustomerContractBasicPO
         ***************************/
        customerInfo.setLogicalResourceType("ISDN");
        customerInfo.setNpCodePub(businessCache.get("ISDN.NPCODEPUB"));
        customerInfo.setPlCodePub(businessCache.get("ISDN.PLCODEPUB"));
        customerInfo.setSubmIdPub(businessCache.get("ISDN.SUBMIDPUB"));
        customerInfo.setHlCode(businessCache.get("ISDN.HLCODE"));
        customerInfo.setRsCode(businessCache.get("ISDN.RSCODE"));
        customerInfo.setCsControlled(businessCache.get("ISDN.CSCONTROLLED"));

        resultInfo = Steps.PROVISION.bscs
                .createCustomer("Provision : Create Customer", this, customerInfo, ts, resultInfo).run();

        ts.setJsonFileName("createShoppingCart.json");
        resultInfo = Steps.PROVISION.eoc
                .createShoppingCart("Provision : Create Shopping Cart", this, customerInfo, ts, resultInfo).run();

        /***************************
         * Handling for the resource ISDN
         ***********************************/

        ts.setJsonFileName("selectLogicalResource.xml");
        Steps.PROTOCOLS.businessLogicBSCS
                .selectLogicalResource("Protocol : Get Free ISDN  Number", this, customerInfo, ts, resultInfo).run();

        customerInfo.setIsdn(customerInfo.getresource().toString());

        /***************************
         * Handling for the resources Serial Number and LinkedPort Number
         ***********************************/

        customerInfo.setSerialNumberType("ISD");

        customerInfo.setPlCodePub(businessCache.get("SERIALNo.PLCODEPUB"));
        customerInfo.setSubmIdPub(businessCache.get("SERIALNo.SUBMIDPUB"));
        customerInfo.setHlCode(businessCache.get("SERIALNo.HLCODE"));

        ts.setJsonFileName("selectSerialNumber.xml");
        Steps.PROTOCOLS.businessLogicBSCS
                .getFreeSerialNumber("Get Free Serial Number and  Port Number", this, customerInfo, ts, resultInfo)
                .run();

        /****************************
         * Handling for the resource IPV4
         ***********************************/

        customerInfo.setNpCodePub(businessCache.get("IPV4.NPCODEPUB"));
        customerInfo.setPlCodePub(businessCache.get("IPV4.PLCODEPUB"));
        customerInfo.setSubmIdPub(businessCache.get("IPV4.SUBMIDPUB"));
        customerInfo.setHlCode(businessCache.get("IPV4.HLCODE"));
        customerInfo.setRsCode(businessCache.get("IPV4.RSCODE"));
        customerInfo.setCsControlled(businessCache.get("IPV4.CSCONTROLLED"));

        ts.setJsonFileName("selectLogicalResource.xml");
        resultInfo = Steps.PROTOCOLS.businessLogicBSCS
                .selectLogicalResource("Protocol : Get Free IPV4 ", this, customerInfo, ts, resultInfo).run();

        
        customerInfo.setIpv4(customerInfo.getresource().toString());

        /****************************
         * Adding the Basic PO in the Cart
         ***********************************/
        customerInfo.setMarketType("ISD");

        customerInfo.setMarket(businessCache.get("BASICPO.MARKET"));
        customerInfo.setSubMarket(businessCache.get("BASICPO.SUB_MARKET"));
        customerInfo.setNetwork(businessCache.get("BASICPO.NETWORK"));

        ts.setJsonFileName("addProductOfferingShoppingCart.json");
        Steps.PROVISION.eoc
                .addPoInShoppingCart("Provision : Add Basic PO in Shopping Cart", this, customerInfo, ts, resultInfo)
                .run();

        ts.setBasicPoReferenceId(customerInfo.getPooiId());

        /***************************
         * Updating the Basic PO with the resources IMSI,ISDN and simCardClass
         ***********************************/

        updateTabletCharacteristics();
        JSONObject jsonobject = JsonHelper.updatePOOi(ts.getResponseBody(), "resourceNumber", customerInfo.getIsdn());

        jsonobject = JsonHelper.updatePOOi(jsonobject.toString(), "SDP_ID", businessCache.get("SDP_ID"));

        jsonobject = JsonHelper.updatePOOiResources(jsonobject.toString(), "simCardClass",
                customerInfo.getSerialNumber(), "name");

        jsonobject = JsonHelper.updatePOOiResources(jsonobject.toString(), "IPV4", customerInfo.getIpv4(), "value");
        jsonobject = JsonHelper.updatePOOiResources(jsonobject.toString(), "IMSI", customerInfo.getLinkedPortNumber(),
                "value");
        ts.setResponseBody(jsonobject.toString());

        Steps.PROTOCOLS.businessLogicEOC
                .updatePooi("Update the Basic  PO for resources(ISDN,IMSI,Serial Number and IPV4))", this, customerInfo,
                        ts, resultInfo)
                .run();

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision: Submit Shopping Cart for Basic PO ", this, customerInfo, ts, resultInfo)
                .run();

        /************************************
         * Usage via FFS Pending
         *************************************/

    }

    private void updateTabletCharacteristics() {
        JSONObject body = null;
        body = updatePOOi(ts.getResponseBody(), "Tab_Model", Constants.TABLETMODEL);
        body = updatePOOi(body.toString(), "Tab_Maker", Constants.TABLETMAKER);
        body = updatePOOi(body.toString(), "Tab_Color", Constants.TABLETCOLOR);
        body = updatePOOi(body.toString(), "deviceID", Constants.DEVICEID + getRandomNumberWithinRange(10, 999999));
        ts.setResponseBody(body.toString());
    }

}
