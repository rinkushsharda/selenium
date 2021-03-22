package com.ericsson.soiv.testcases.scenes.cbio18;

import com.ericsson.jive.core.execution.Fixture;

import static com.ericsson.soiv.utils.HelperClass.getRandomNumberWithinRange;
import static com.ericsson.soiv.utils.JsonHelper.getRequestedJsonElement;
import static com.ericsson.soiv.utils.JsonHelper.searchKeyAndUpdateJson;
import static com.ericsson.soiv.utils.JsonHelper.updatePOOi;
import static com.ericsson.soiv.utils.http.ReadBusinessData.readBusinessCacheData;
import java.io.IOException;
import java.util.LinkedHashMap;
import org.json.JSONObject;
import org.junit.Test;
import com.ericsson.jive.core.execution.Description;
import com.ericsson.jive.core.execution.Id;
import com.ericsson.jive.core.execution.Jive;
import com.ericsson.jive.core.execution.Tags;
import com.ericsson.soiv.fixtures.SoivFixture;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.CalculateDateTime;
import com.ericsson.soiv.utils.Constants;
import com.ericsson.soiv.utils.CustomerInfo;
import com.ericsson.soiv.utils.JsonHelper;
import com.ericsson.soiv.utils.RawMessages;
import com.ericsson.soiv.utils.TestResultInfo;
import com.ericsson.soiv.utils.TransactionSpecification;
import com.jayway.jsonpath.JsonPath;

//Created by ZEAHDCE on 17-July-2019

@Fixture(SoivFixture.class)
public class PostpaidPOBSCSCrossTechnologyMultiSim extends SoivTestBase {
    private TestResultInfo resultInfo = new TestResultInfo();
    private CustomerInfo customerInfo = new CustomerInfo();
    private TransactionSpecification ts = new TransactionSpecification();
    private CalculateDateTime calculateDateTime = new CalculateDateTime();
    private LinkedHashMap<String, String> businessCache = null;

    @Test
    @Id("00000063")
    @Tags("OT-Regression, OT-Postpaid, BSCS-Controlled, OT-CBiO18")
    @Description("Cross Technology Product Offering MultiSim")
    public void postpaidPOBSCScrossTechnology() throws IOException {

        businessCache = new LinkedHashMap<>(readBusinessCacheData(this.getTestCase().getId()));
        customerInfo.setCustomerBillCycle(businessCache.get("CUSTOMER.BILLCYCLE"));
        customerInfo.setOrderMode(businessCache.get("ORDER.MODE"));
        customerInfo.setRequester(businessCache.get("ORDER.REQUESTER"));

        LinkedHashMap<String, String> productOfferings = new LinkedHashMap<String, String>();
        productOfferings.put("basic", businessCache.get("PO.ID_BASIC"));
        productOfferings.put("optional", businessCache.get("PO.ID_OPTIONAL_Mobile_Dev"));

        ts.setProductOfferingIds(productOfferings);

        /*****************************
         * createCustomerContractBasicPO
         ***************************/

        resultInfo = Steps.PROVISION.bscs
                .createCustomer("Provision : Create Customer", this, customerInfo, ts, resultInfo).run();

        ts.setJsonFileName("createShoppingCart.json");
        resultInfo = Steps.PROVISION.eoc
                .createShoppingCart("Provision : Create Shopping Cart", this, customerInfo, ts, resultInfo).run();

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
                .selectLogicalResource("Protocol : Get Free ISDN Number", this, customerInfo, ts, resultInfo).run();

        customerInfo.setIsdn(customerInfo.getresource().toString());

        /***************************
         * Handling for the multiple Serial Number and LinkedPort Number
         ***********************************/

        customerInfo.setSerialNumberType("ISD");
        customerInfo.setSearchCount(Integer.parseInt(businessCache.get("RESOURCE.SEARCHCOUNT")));
        customerInfo.setPlCodePub(businessCache.get("SERIALNo.PLCODEPUB"));
        customerInfo.setSubmIdPub(businessCache.get("SERIALNo.SUBMIDPUB"));
        customerInfo.setHlCode(businessCache.get("SERIALNo.HLCODE"));

        ts.setJsonFileName("selectSerialNumber.xml");
        Steps.PROTOCOLS.businessLogicBSCS.getFreeSerialNumber("Get three Free Serial Number and three Port Number",
                this, customerInfo, ts, resultInfo).run();

        String[] linkedPorts = getResources(customerInfo.getLinkedPortNumber());

        String[] serialNumbers = getResources(customerInfo.getSerialNumber());

        /****************************
         * Adding the Basic PO in the Cart
         ***********************************/
        customerInfo.setMarketType("ISD");

        customerInfo.setMarket(businessCache.get("BASICPO.MARKET"));
        customerInfo.setSubMarket(businessCache.get("BASICPO.SUB_MARKET"));
        customerInfo.setNetwork(businessCache.get("BASICPO.NETWORK"));

        ts.setJsonFileName("addProductOfferingShoppingCart.json");
        resultInfo = Steps.PROVISION.eoc
                .addPoInShoppingCart("Provision : Add Basic PO in Shopping Cart", this, customerInfo, ts, resultInfo)
                .run();

        ts.setBasicPoReferenceId(customerInfo.getPooiId());

        /***************************
         * Updating the Basic PO with the resources IMSI,ISDN and simCardClass
         ***********************************/

        JSONObject jsonobject = JsonHelper.updatePOOi(ts.getResponseBody(), "resourceNumber", customerInfo.getIsdn());

        jsonobject = JsonHelper.updatePOOiResources(jsonobject.toString(), "IMSI", linkedPorts[0], "value");

        jsonobject = JsonHelper.updatePOOiResources(jsonobject.toString(), "simCardClass", serialNumbers[0], "name");

        ts.setResponseBody(jsonobject.toString());

        Steps.PROTOCOLS.businessLogicEOC.updatePooi("Update the Basic  PO with resources(ISDN,IMSI and Serial Number)",
                this, customerInfo, ts, resultInfo).run();

        /****************************
         * Adding the first Optional PO in the Cart
         ***********************************/
        ts.setJsonFileName("AddOptionalPOonBasic.json");
        customerInfo.setMarketType("ISD");

        customerInfo.setMarket(businessCache.get("OPTIONALPO.MARKET"));
        customerInfo.setSubMarket(businessCache.get("OPTIONALPO.SUB_MARKET"));
        customerInfo.setNetwork(businessCache.get("OPTIONALPO.NETWORK"));

        Steps.PROTOCOLS.businessLogicEOC.addOptionalPOonBasicPO("Provision : Add first Optional PO in to Basic PO",
                this, customerInfo, ts, resultInfo).run();

        /******************************
         * Update Optional PO with Mobile Device Characteristics
         ****************/

        addDeviceInOptionalPO();

        Steps.PROTOCOLS.businessLogicEOC
                .updateOptionalProductOffering("Provision : Update Optional PO with Mobile Device Characteristics",
                        this, customerInfo, ts, resultInfo)
                .run();

        /****************************
         * Adding one more Optional PO in the Cart
         ***********************************/

        productOfferings.put("optional", businessCache.get("PO.ID_OPTIONAL_SIM_Device"));
        ts.setProductOfferingIds(productOfferings);

        customerInfo.setMarketType("ISD");

        customerInfo.setMarket(businessCache.get("OPTIONALPO.MARKET"));
        customerInfo.setSubMarket(businessCache.get("OPTIONALPO.SUB_MARKET"));
        customerInfo.setNetwork(businessCache.get("OPTIONALPO.NETWORK"));

        ts.setJsonFileName("AddOptionalPOonBasic.json");
        Steps.PROTOCOLS.businessLogicEOC.addOptionalPOonBasicPO("Provision : Add 2nd Optional PO in to Basic PO", this,
                customerInfo, ts, resultInfo).run();

        /************************
         * Update the Optional PO with resources IMSI,simCardClass
         ************************/

        jsonobject = JsonHelper.updatePOOiResources(ts.getResponseBody(), "IMSI", linkedPorts[1], "value");

        jsonobject = JsonHelper.updatePOOiResources(jsonobject.toString(), "simCardClass", serialNumbers[1], "name");

        ts.setResponseBody(jsonobject.toString());

        Steps.PROTOCOLS.businessLogicEOC.updateOptionalProductOffering(
                "Provision : Update Second Optional PO with resources IMSI and simCardClass", this, customerInfo, ts,
                resultInfo).run();

        /***********************************
         * Submit the Shopping cart
         ***************************************/

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision: Submit Shopping Cart for one Basic PO and two Optional PO ", this,
                        customerInfo, ts, resultInfo)
                .run();

        /***********************************
         * Create the Shopping cart for adding one more simCardClass in the Basic PO
         ***************************************/

        ts.setJsonFileName("createShoppingCart.json");
        Steps.PROVISION.eoc
                .createShoppingCart("Provision : Create Shopping Cart For adding resource simCardClass in the Basic PO",
                        this, customerInfo, ts, resultInfo)
                .run();

        /***********************************
         * Get the Shopping cart for getting Basic POOI
         ***************************************/

        Steps.PROVISION.eoc
                .getShoppingCart("Get shopping cart for getting Basic PO", this, customerInfo, ts, resultInfo).run();

        String pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("basic")), "id");
        customerInfo.setPooiId(pooiId);

        Steps.PROTOCOLS.businessLogicEOC.getPooi("Getting the POOI For adding one more simCardClass Resource", this,
                customerInfo, ts, resultInfo).run();

        /*****************************
         * Extract the resource Specification and parentId from Basic POOI Response
         **********/

        String resourceSpecification = JsonPath.read(ts.getResponseBody(), "$.resources[1].resourceSpecification");

        String parentId = JsonPath.read(ts.getResponseBody(), "$.resources[1].parentId");

        ts.setDeviceId(serialNumbers[2]);
        ts.setResourceParentId(parentId);
        ts.setResourceSpecification(resourceSpecification);

        /********************************************
         * Update the Basic POOI with another simCardClass
         ***************************/
        JSONObject getPOOIRequestBody = searchKeyAndUpdateJson(ts.getResponseBody(), "action",
                businessCache.get("ACTION.CODE_MODIFY"));

        ts.setResponseBody(getPOOIRequestBody.toString());

        Steps.PROTOCOLS.businessLogicEOC.updatePooi("Updation Action Values in the Basic PO for Adding new sim card",
                this, customerInfo, ts, resultInfo).run();
        Steps.PROTOCOLS.businessLogicEOC
                .addResources("Adding the another resource simCard in the Basic PO", this, customerInfo, ts, resultInfo)
                .run();

        /********************************************
         * Getting the Basic POOI and inserting the another IMSI in the Basic POOI
         ***************************/

        Steps.PROVISION.eoc
                .getShoppingCart("Get shopping cart for getting Basic PO ", this, customerInfo, ts, resultInfo).run();

        pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("basic")), "id");
        customerInfo.setPooiId(pooiId);

        resultInfo = Steps.PROTOCOLS.businessLogicEOC
                .getPooi("Getting the Basic POOI For adding one more IMSI", this, customerInfo, ts, resultInfo).run();

        jsonobject = JsonHelper.updatePOOiResources(ts.getResponseBody(), "IMSI", linkedPorts[2], "value");

        ts.setResponseBody(jsonobject.toString());

        Steps.PROTOCOLS.businessLogicEOC
                .updatePooi("Update the Basic with one more IMSI ", this, customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC.submitShoppingCart(
                "Provision Submit Shopping cart for one more IMSI and simCardClass in the Basic PO ", this,
                customerInfo, ts, resultInfo).run();

        /***********************************
         * Offline Voice Call 3 times with different different IMSI
         ********************************/
        offlineVoiceCall(linkedPorts[0]); // offline Voice Call with first IMSI
        offlineVoiceCall(linkedPorts[1]); // offline Voice Call with Second IMSI
        offlineVoiceCall(linkedPorts[2]); // offline Voice Call with Third IMSI

        /*****************************
         * Verification the charges in the Bill
         ******************/

        ts.setInvoiceCharge(businessCache.get("INVOICE.CHARGE"));
        ts.setInvoicePdfPageNo(Integer.parseInt(businessCache.get("INVOICE.PAGENO_1")));
        ts.setRemoveInvoicePdfFile(Boolean.getBoolean(businessCache.get("INVOICE.REMOVE")));
        ts.setJsonFileName("sendBillGenerateRequest.xml");
        Steps.BILL_GENERATION.stepsBilling
                .sendInvoiceRequest("Protocol : Send Bill Process Create Request", this, resultInfo, customerInfo, ts)
                .run();

        resultInfo = Steps.BILL_GENERATION.stepsBilling
                .getInvoice("Validation : Get Invoice PDF and Validate GRAND TOTAL in PDF", this, resultInfo,
                        customerInfo, ts)
                .run();
        String readInvoiceData = (String) resultInfo.getResult();

        if (readInvoiceData.contains("GRAND TOTAL " + ts.getInvoiceCharge())) {
            Jive.log("OK : Invoice PDF Charges : " + ts.getInvoiceCharge() + " are Validated Successfully");
        } else {
           Jive.failAndContinue("FAILED : Invoice PDF Charges : " + ts.getInvoiceCharge() + " are Not Validated Successfully! Please Check the Invoice Content : "
                    + readInvoiceData);
        }
        /*****************************
         * Verification the Resources in the Bill
         ******************/
        if (readInvoiceData.contains(customerInfo.getIsdn()) && (readInvoiceData.contains(serialNumbers[1]))
                && (readInvoiceData.contains(serialNumbers[0])) && readInvoiceData.contains(serialNumbers[2])) {
            Jive.log("OK : " + "Sim Serial Numbers " + serialNumbers[0] + "," + serialNumbers[1] + ","
                    + serialNumbers[2] + " and ISDN " + customerInfo.getIsdn() + " are Validated Successfully");
        } else {
            Jive.fail("FAILED : " + "Sim Serial Numbers " + serialNumbers[0] + "," + serialNumbers[1] + ","
                    + serialNumbers[2] + " and ISDN " + customerInfo.getIsdn() + " are NOT Validated Successfully");
        }
    }

    private void addDeviceInOptionalPO() {
        JSONObject body = null;
        body = updatePOOi(ts.getResponseBody(), "Mobile_Model", Constants.MOBILEMODEL);
        body = updatePOOi(body.toString(), "Mobile_Maker", Constants.MOBILEMAKER);
        body = updatePOOi(body.toString(), "Mobile_Color", Constants.MOBILECOLOR);
        body = updatePOOi(body.toString(), "deviceID", Constants.DEVICEID + getRandomNumberWithinRange(10, 999999));
        ts.setResponseBody(body.toString());
    }

    private String[] getResources(String resource) {
        resource = resource.replace("[", "");
        resource = resource.replace("]", "");
        String[] resources = resource.split(",");
        return resources;

    }

    private void offlineVoiceCall(String imsi) {
        /***************************************
         * OfflineBSCS Voice Call
         ******************************/
        ts.setUdrTextFileName("voice_udr.txt");

        String body = RawMessages.getBodyFromTextFilePlacedInTestStep(this.getClass().getName(),
                "protocols/bscs/udrTemplates", ts.getUdrTextFileName()).toString();

        body = body.replace("{{IMSI}}", imsi);
        body = body.replace("{{MSISDN}}", customerInfo.getIsdn());
        body = body.replace("{{DATE}}", calculateDateTime.getCurrentDateTime());
        body = body.replace("{{PLCODE}}", customerInfo.getPlCodePub());

        body = body.replace("{{DURATION}}", businessCache.get("CALL.DURATION"));
        body = body.replace("{{NETWORK}}", businessCache.get("CALL.NETWORK"));
        body = body.replace("{{TYPE}}", businessCache.get("CALL.TYPE"));

        ts.setResponseBody(body);

        customerInfo.setMsisdn(customerInfo.getIsdn());

        Steps.CHARGING_OFFLINE.stepsChargingOfflineBscs
                .offlineVoice("Protocol : BSCS - Offline Voice Usage", this, resultInfo, customerInfo, ts).run();
    }

}
