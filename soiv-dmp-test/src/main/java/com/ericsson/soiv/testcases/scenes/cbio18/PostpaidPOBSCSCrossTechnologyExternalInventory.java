package com.ericsson.soiv.testcases.scenes.cbio18;

import static com.ericsson.soiv.utils.http.ReadBusinessData.readBusinessCacheData;

import java.io.IOException;
import java.math.BigInteger;
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
import com.ericsson.soiv.utils.Constants;
import com.ericsson.soiv.utils.CustomerInfo;
import com.ericsson.soiv.utils.HelperClass;

import static com.ericsson.soiv.utils.HelperClass.*;
import com.ericsson.soiv.utils.JsonHelper;
import com.ericsson.soiv.utils.RawMessages;
import com.ericsson.soiv.utils.TestResultInfo;
import com.ericsson.soiv.utils.TransactionSpecification;
import com.jayway.jsonpath.JsonPath;

// Created by ZEAHDCE on 4-July-2019

@Fixture(SoivFixture.class)
public class PostpaidPOBSCSCrossTechnologyExternalInventory extends SoivTestBase {

    private TestResultInfo resultInfo = new TestResultInfo();
    private CustomerInfo customerInfo = new CustomerInfo();
    private TransactionSpecification ts = new TransactionSpecification();
    private CalculateDateTime calculateDateTime = new CalculateDateTime();
    BigInteger min = null;
    BigInteger max = null;

    @Test
    @Id("00000059")
    @Tags("OT-Regression, OT-Postpaid, BSCS-Controlled, OT-CBiO18")
    @Description("Cross Technology Product Offering External Inventory")
    public void postpaidPOBSCSCrossTechnologyExternalInventory() throws IOException {

        LinkedHashMap<String, String> businessCache = new LinkedHashMap<>(
                readBusinessCacheData(this.getTestCase().getId()));
        customerInfo.setCustomerBillCycle(businessCache.get("CUSTOMER.BILLCYCLE"));
        customerInfo.setOrderMode(businessCache.get("ORDER.MODE"));
        customerInfo.setRequester(businessCache.get("ORDER.REQUESTER"));
        min = new BigInteger(businessCache.get("RANDOM.MIN"));
        max = new BigInteger(businessCache.get("RANDOM.MAX"));

        LinkedHashMap<String, String> productOfferings = new LinkedHashMap<String, String>();
        productOfferings.put("basic", businessCache.get("PO.ID_BASIC"));
        productOfferings.put("optional", businessCache.get("PO.ID_OPTIONAL"));

        ts.setProductOfferingIds(productOfferings);

        /*****************************
         * createCustomerContractBasicPO
         ***************************/

        resultInfo = Steps.PROVISION.bscs
                .createCustomer("Provision : Create Customer", this, customerInfo, ts, resultInfo).run();

        ts.setJsonFileName("createShoppingCart.json");
        resultInfo = Steps.PROVISION.eoc
                .createShoppingCart("Provision : Create Shopping Cart", this, customerInfo, ts, resultInfo).run();

        /****************************************
         * Handling for the resource IPV4
         **************************/

        customerInfo.setLogicalResourceType("IPV4");
        customerInfo.setNpCodePub(businessCache.get("IPV4.NPCODEPUB"));
        customerInfo.setPlCodePub(businessCache.get("IPV4.PLCODEPUB"));
        customerInfo.setSubmIdPub(businessCache.get("IPV4.SUBMIDPUB"));
        customerInfo.setHlCode(businessCache.get("IPV4.HLCODE"));
        customerInfo.setRsCode(businessCache.get("IPV4.RSCODE"));
        customerInfo.setCsControlled(businessCache.get("IPV4.CSCONTROLLED"));

        ts.setJsonFileName("selectLogicalResource.xml");
        resultInfo = Steps.PROTOCOLS.businessLogicBSCS
                .selectLogicalResource("Protocol : Get Free IPV4 ", this, customerInfo, ts, resultInfo).run();

        customerInfo.setIpv4(getRandomIP(customerInfo.getresource().toString()));
        /***************************portport
         * Handling for the resource Serial Number and LinkedPort Number
         ***********************************/

        customerInfo.setSerialNumberType("ISD");

        customerInfo.setPlCodePub(businessCache.get("SERIALNo.PLCODEPUB"));
        customerInfo.setSubmIdPub(businessCache.get("SERIALNo.SUBMIDPUB"));
        customerInfo.setHlCode(businessCache.get("SERIALNo.HLCODE"));

        ts.setJsonFileName("selectSerialNumber.xml");
        Steps.PROTOCOLS.businessLogicBSCS
                .getFreeSerialNumber("Get Free Serial Number and Port Number", this, customerInfo, ts, resultInfo)
                .run();

        /***************************
         * Handling for the resource IAID
         ***********************************/

        ts.setJsonFileName("portSearch.xml");

        customerInfo.setSubmIdPub(businessCache.get("PORT.SUBMIDPUB"));
        customerInfo.setPortNpPub(businessCache.get("PORT.PORTNPPUB"));
        customerInfo.setPlCodePub(businessCache.get("PORT.PLCODEPUB"));
        customerInfo.setHlCode(businessCache.get("PORT.HLCODE"));

        Steps.PROTOCOLS.businessLogicBSCS.portSearch("Search Port Number for IAI ", this, customerInfo, ts, resultInfo)
                .run();

        String iaid = JsonPath.read(ts.getResponseBody(),
                "$.SOAP-ENV:Envelope.SOAP-ENV:Body.portsSearchResponse.returnedPorts.item.portnum.content");

        customerInfo.setIaidNumber(generateNumber(iaid).toString());

        min = BigInteger.valueOf(Integer.parseInt(businessCache.get("RANDOM.MIN")));
        max = BigInteger.valueOf(Integer.parseInt(businessCache.get("RANDOM.MAX")));

        customerInfo.setSerialNumber(generateNumber(customerInfo.getSerialNumber()).toString());
        min = BigInteger.valueOf(Integer.parseInt(businessCache.get("RANDOM.MIN")));
        max = BigInteger.valueOf(Integer.parseInt(businessCache.get("RANDOM.MAX")));

        customerInfo.setLinkedPortNumber(generateNumber(customerInfo.getLinkedPortNumber()).toString());

        /*****************************************
         * Handling for the resource ISDN
         *********************/

        min = BigInteger.valueOf(Integer.parseInt(businessCache.get("RANDOM.MIN")));
        max = BigInteger.valueOf(Integer.parseInt(businessCache.get("RANDOM.MAX")));

        /***************************
         * Handling for the resource ISDN
         ***********************************/

        BigInteger isdn = generateNumber(Constants.ISDN_NUMBER);

        customerInfo.setIsdn(isdn.toString());

        /**************************************
         * Adding Basic PO in the Cart
         *****************************/

        customerInfo.setMarketType("ISD");

        customerInfo.setMarket(businessCache.get("BASICPO.MARKET"));
        customerInfo.setSubMarket(businessCache.get("BASICPO.SUB_MARKET"));
        customerInfo.setNetwork(businessCache.get("BASICPO.NETWORK"));

        ts.setJsonFileName("addProductOfferingShoppingCart.json");
        resultInfo = Steps.PROVISION.eoc
                .addPoInShoppingCart("Provision : Add PO in Shopping Cart", this, customerInfo, ts, resultInfo).run();

        ts.setBasicPoReferenceId(customerInfo.getPooiId());
        /********************************
         * Updating the resources IAID,IPV4 and device ID in the Basic PO
         ******/

        JSONObject jsonObject = JsonHelper.updatePOOiResources(ts.getResponseBody(), "IAI",
                customerInfo.getIaidNumber(), "value");

        jsonObject = JsonHelper.updatePOOiResources(jsonObject.toString(), "IPV4", customerInfo.getIpv4(), "value");

        jsonObject = JsonHelper.updatePOOi(jsonObject.toString(), "deviceID", "SetupBox");

        jsonObject = JsonHelper.updatePOOi(jsonObject.toString(), "hlrcode", businessCache.get("BASICPO.HLRCODE"));

        ts.setResponseBody(jsonObject.toString());

        Steps.PROTOCOLS.businessLogicEOC.updatePooi("Update the basic PO with resources IAID,IPV4,deviceID and hlrcode",
                this, customerInfo, ts, resultInfo).run();

        /***************************
         * Adding the Optional PO in the Cart
         ***********************************/

        customerInfo.setMarketType("ISD");

        customerInfo.setMarket(businessCache.get("OPTIONALPO.MARKET"));
        customerInfo.setSubMarket(businessCache.get("OPTIONALPO.SUB_MARKET"));
        customerInfo.setNetwork(businessCache.get("OPTIONALPO.NETWORK"));

        ts.setJsonFileName("AddOptionalPOonBasic.json");
        Steps.PROTOCOLS.businessLogicEOC.addOptionalPOonBasicPO("Provision : Add Optional PO in to Basic PO", this,
                customerInfo, ts, resultInfo).run();

        /***************************
         * Updating the Optional PO with the resources IMSI,ISDN and simCardClass and
         * Submit the cart
         ***********************************/

        JSONObject js = JsonHelper.updatePOOi(ts.getResponseBody(), "resourceNumber", customerInfo.getIsdn());

        js = JsonHelper.updatePOOiResources(js.toString(), "IMSI", customerInfo.getLinkedPortNumber(), "value");

        js = JsonHelper.updatePOOi(js.toString(), "deviceID", customerInfo.getSerialNumber());

        js = JsonHelper.updatePOOi(js.toString(), "hlrcode", businessCache.get("OPTIONALPO.HLRCODE"));

        js = JsonHelper.updatePOOi(js.toString(), "prefix", businessCache.get("OPTIONALPO.PREFIX"));

        ts.setResponseBody(js.toString());
        Steps.PROTOCOLS.businessLogicEOC
                .updatePooi("Update the optional PO with resources ISDN,IMSI,SerialNumber and hlrcode,prefix", this,
                        customerInfo, ts, resultInfo)
                .run();

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("submit cart for basic PO and Optional PO", this, customerInfo, ts, resultInfo)
                .run();

        /***************************************
         * OfflineBSCS Voice Call
         ******************************/
        ts.setUdrTextFileName("voice_udr.txt");

        String body = RawMessages.getBodyFromTextFilePlacedInTestStep(this.getClass().getName(),
                "protocols/bscs/udrTemplates", ts.getUdrTextFileName()).toString();

        body = body.replace("{{IMSI}}", customerInfo.getLinkedPortNumber());
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

        /***************************************
         * OfflineBSCS Gy Call
         ******************************/

        ts.setUdrTextFileName("ip_udr.txt");
        body = RawMessages.getBodyFromTextFilePlacedInTestStep(this.getClass().getName(), "protocols/bscs/udrTemplates",
                ts.getUdrTextFileName()).toString();

        body = body.replace("{{IPV4}}", HelperClass.iptoHexaDecimal(customerInfo.getIpv4()));
        body = body.replace("{{PORT}}", customerInfo.getIaidNumber());
        body = body.replace("{{DATE}}", calculateDateTime.getCurrentDateTime());
        ts.setResponseBody(body);

        Steps.CHARGING_OFFLINE.stepsChargingOfflineBscs
                .offlineGy("Protocol : BSCS - Offline Data Usage", this, resultInfo, customerInfo, ts).run();

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
           Jive.failAndContinue("FAILED : Invoice PDF Charges : " + ts.getInvoiceCharge() + " are Not Validated Successfully!! Please Check the Invoice Content : "
                    + readInvoiceData);
        }

        /*****************************
         * Verification the resources in the Bill
         ******************/

        if ((readInvoiceData.contains(HelperClass.iptoHexaDecimal(customerInfo.getIpv4())))
                && (readInvoiceData.contains(customerInfo.getIsdn()))
                && (readInvoiceData.contains(customerInfo.getSerialNumber())))

        {
            Jive.log("OK : Invoice PDF Resources IP- " + customerInfo.getIpv4() + " ISDN- " + customerInfo.getIsdn()
                    + " SimNumber- " + customerInfo.getSerialNumber() + "Validated Successfully");

        } else {
           Jive.failAndContinue("FAILED : Invoice PDF Resources IP- " + customerInfo.getIpv4() + " ISDN- " + customerInfo.getIsdn()
                    + " SimNumber- " + customerInfo.getSerialNumber() + "Not Validated Successfully");
        }
    }

    private BigInteger generateNumber(String originalResource) {
        BigInteger original = new BigInteger(originalResource);
        min = min.add(original);
        max = max.add(original);
        return HelperClass.generateRandomBigIntegerFromRange(min.toString(), max.toString());
    }
}
