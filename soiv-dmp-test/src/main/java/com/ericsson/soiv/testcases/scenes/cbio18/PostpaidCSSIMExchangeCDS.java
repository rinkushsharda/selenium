package com.ericsson.soiv.testcases.scenes.cbio18;

import com.ericsson.jive.core.execution.*;
import com.ericsson.soiv.fixtures.SoivFixture;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.*;
import com.ericsson.soiv.utils.Constants;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import static com.ericsson.soiv.utils.JsonHelper.*;
import static com.ericsson.soiv.utils.http.ReadBusinessData.readBusinessCacheData;

//Created By ZKAPSAR 22-July-2019

@Fixture(SoivFixture.class)
public class PostpaidCSSIMExchangeCDS extends SoivTestBase {
    private TestResultInfo resultInfo = new TestResultInfo();
    private CustomerInfo customerInfo = new CustomerInfo();
    private TransactionSpecification ts = new TransactionSpecification();
    private LinkedHashMap<String, String> businessCache = null;

    @Test
    @Id("00000035")
    @Tags("OT-Regression, OT-Postpaid, EC-Controlled, OT-CBiO18")
    @Description("CS Controlled PO SIM Change BUC22B")
    public void postpaidCsSimExchangeCds() throws IOException, InterruptedException {

        businessCache = new LinkedHashMap<>(readBusinessCacheData(this.getTestCase().getId()));
        String[] isdn = new String[Integer.parseInt(businessCache.get("SEARCH.COUNT"))];
        JSONObject body = null;

        customerInfo.setCustomerBillCycle(businessCache.get("CUSTOMER.BILLCYCLE"));
        customerInfo.setOrderMode(businessCache.get("ORDER.MODE"));
        customerInfo.setRequester(businessCache.get("ORDER.REQUESTER"));
        ts.setInvoicePdfPageNo(Integer.parseInt(businessCache.get("INVOICE.PAGENO_1")));
        ts.setRemoveInvoicePdfFile(Boolean.valueOf(businessCache.get("INVOICE.REMOVE")));

        LinkedHashMap<String, String> productOfferings = new LinkedHashMap<String, String>();
        productOfferings.put("basic", businessCache.get("PO.ID_BASIC"));
        productOfferings.put("optional", businessCache.get("PO.ID_OPTIONAL"));

        customerInfo.setMarketType(businessCache.get("MARKET.TYPE"));
        customerInfo.setMarket(businessCache.get("BASICPO.MARKET"));
        customerInfo.setSubMarket(businessCache.get("BASICPO.SUB_MARKET"));

        customerInfo.setNetwork(businessCache.get("BASICPO.NETWORK"));
        customerInfo.setSerialNumberType(businessCache.get("SIM.NUMBERTYPE"));
        customerInfo.setSearchCount(Integer.parseInt(businessCache.get("SEARCH.COUNT")));

        customerInfo.setLogicalResourceType("CDS");
        customerInfo.setNpCodePub(businessCache.get("CDS.NPCODEPUB"));
        customerInfo.setPlCodePub(businessCache.get("CDS.PLCODEPUB"));
        customerInfo.setSubmIdPub(businessCache.get("CDS.SUBMIDPUB"));
        customerInfo.setHlCode(businessCache.get("CDS.HLCODE"));
        customerInfo.setRsCode(businessCache.get("CDS.RSCODE"));
        customerInfo.setCsControlled(businessCache.get("CDS.CSCONTROLLED"));

        // Doing Contracting with Basic PO, Fetching multiple SIM's IMSI's and MSISDN
        // for market CDSor ISD
        ts.setProductOfferingIds(productOfferings);
        resultInfo = Steps.PROVISION.bscs
                .createCustomer("Provision : Create Customer for Basic PO", this, customerInfo, ts, resultInfo).run();

        ts.setJsonFileName("createShoppingCart.json");
        resultInfo = Steps.PROVISION.eoc
                .createShoppingCart("Provision : Create Shopping Cart for Basic PO", this, customerInfo, ts, resultInfo)
                .run();

        ts.setJsonFileName("addProductOfferingShoppingCart.json");
        resultInfo = Steps.PROVISION.eoc.addPoInShoppingCart("Provision : Add PO in Shopping Cart for Basic PO", this,
                customerInfo, ts, resultInfo).run();
        String respBody = ts.getResponseBody();

        ts.setJsonFileName("selectLogicalResource.xml");
        resultInfo = Steps.PROTOCOLS.businessLogicBSCS
                .selectLogicalResource("Protocol : Get 2 Free ISDN Number", this, customerInfo, ts, resultInfo).run();

        // Getting multiple ISDN's
        isdn[0] = getResources(customerInfo.getresource().toString()).get(0).toString();

        isdn[1] = getResources(customerInfo.getresource().toString()).get(1).toString();
        customerInfo.setIsdn(isdn[0]);

        ts.setJsonFileName("selectSerialNumber.xml");
        Steps.PROTOCOLS.businessLogicBSCS.getFreeSerialNumber("Get three Free Serial Number and three Port Number",
                this, customerInfo, ts, resultInfo).run();

        // Getting multiple IMSI and SIM Number
        JSONArray linkedPorts = getResources(customerInfo.getLinkedPortNumber());

        JSONArray serialNumbers = getResources(customerInfo.getSerialNumber());

        customerInfo.setMsisdn(customerInfo.getIsdn());
        customerInfo.setSerialNumber(serialNumbers.get(0).toString());
        customerInfo.setLinkedPortNumber(linkedPorts.get(0).toString());

        ts.setResponseBody(respBody);
        resultInfo = Steps.PROTOCOLS.businessLogicEOC
                .updateProductOffering("Provision : Update Basic PO", this, customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC.submitShoppingCart(
                "Provision : Submit Shopping Cart For Customer with Basic PO : " + customerInfo.getCustomerId(), this,
                customerInfo, ts, resultInfo).run();

        // Adding Optional PO in Basic PO
        ts.setJsonFileName("createShoppingCart.json");
        resultInfo = Steps.PROVISION.eoc.createShoppingCart("Provision : Create Shopping Cart For Optional IP PO", this,
                customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart To get Basic POOI", this, customerInfo, ts, resultInfo)
                .run();
        customerInfo.setIsdn(isdn[1]);
        customerInfo.setMsisdn(customerInfo.getIsdn());
        customerInfo.setSerialNumber(serialNumbers.get(1).toString());
        customerInfo.setLinkedPortNumber(linkedPorts.get(1).toString());

        String pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("basic")), "id");
        ts.setBasicPoReferenceId(pooiId);

        ts.setJsonFileName("AddOptionalPOonBasic.json");
        Steps.PROTOCOLS.businessLogicEOC.addOptionalPOonBasicPO("Provision : Add Optional PO in to Basic PO", this,
                customerInfo, ts, resultInfo).run();
        Steps.PROTOCOLS.businessLogicEOC.getPooi("Get Optional Poi", this, customerInfo, ts, resultInfo).run();

        // Updating optional pO with ISDN, IMSI, SIM Number

        body = JsonHelper.updatePOOiResources(ts.getResponseBody(), "E.164", customerInfo.getMsisdn(), "value");
        body = JsonHelper.updatePOOiResources(body.toString(), "IMSI", customerInfo.getLinkedPortNumber(), "value");
        body = JsonHelper.updatePOOi(body.toString(), "deviceID", customerInfo.getSerialNumber());
        body = JsonHelper.updatePOOi(body.toString(), "mainDirNum", "false");
        ts.setResponseBody(body.toString());

        Steps.PROTOCOLS.businessLogicEOC
                .updatePooi("Provision : Update the Optional PO", this, customerInfo, ts, resultInfo).run();
        Steps.PROTOCOLS.businessLogicEOC.submitShoppingCart("Provision: Submit Shopping Cart for Optional PO ", this,
                customerInfo, ts, resultInfo).run();

        // Verify Bill with Optional PO Serial Number on it
        Jive.log("Verifying Bill Before SIM Change");
        verifyInvoicePDF(customerInfo, "BEFORE");

        // Changing SIM or Serial Number with new one
        ts.setJsonFileName("createShoppingCart.json");
        resultInfo = Steps.PROVISION.eoc.createShoppingCart("Provision : Create Shopping Cart for SIM Exchange", this,
                customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart for SIM Exchange", this, customerInfo, ts, resultInfo)
                .run();

        pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("optional")), "id");
        customerInfo.setPooiId(pooiId);
        Jive.log("OLD SIM IS" + customerInfo.getSerialNumber());
        // Getting new Serial Number
        ts.setJsonFileName("selectSerialNumber.xml");
        customerInfo.setSearchCount(1);
        Steps.PROTOCOLS.businessLogicBSCS.getFreeSerialNumber("Get three Free Serial Number and three Port Number",
                this, customerInfo, ts, resultInfo).run();
        Jive.log("NEW SIM IS" + customerInfo.getSerialNumber());

        Steps.PROTOCOLS.businessLogicEOC.getPooi("Get Optional POOI", this, customerInfo, ts, resultInfo).run();

        // Updating new Serial Number to be Changed in Optiona PO
        body = JsonHelper.updatePOOi(ts.getResponseBody(), "deviceIdExchange", customerInfo.getSerialNumber());
        body = searchKeyAndUpdateJson(body.toString(), "action", "Modify");
        ts.setResponseBody(body.toString());
        Steps.PROTOCOLS.businessLogicEOC
                .updatePooi("Update Optional POOI for SIM Change", this, customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC.submitShoppingCart("Provision: Submit Shopping Cart for SIM Exchange", this,
                customerInfo, ts, resultInfo).run();

        // Making Online Call
        customerInfo.setMsisdn(isdn[0]);
        ts.setCharge(businessCache.get("USAGE.DATA_CHARGE"));
        ts.setExpectedCost(businessCache.get("USAGE.DATA_COST")); // This is only for the CS Charging
        ts.setDuration(Integer.parseInt(businessCache.get("USAGE.DATA_DURATION")));
        ts.setCalledNumber(Constants.NATIONAL_NUMBER_1);
        runUsageAndCheckBalance(ts);

        // Check Bill with Usage Charges and New SIM of Optional PO
        Jive.log("Verifying Bill After SIM Change");
        verifyInvoicePDF(customerInfo, "AFTER");
    }

    private void runUsageAndCheckBalance(TransactionSpecification ts) {

        Steps.PROTOCOLS.businessLogicCS.getAccountBalanceAndDate(
                "Protocol : Before Usage - Get Account Balance from CS", this, customerInfo, ts, resultInfo).run();

        BigDecimal getBalanceBefore = ts.getAccountBalance();

        Steps.CHARGING_ONLINE.simulator
                .onlineCip("Protocol : Online Voice Usage CIP IP Simulator", this, resultInfo, customerInfo, ts).run();

        Steps.PROTOCOLS.businessLogicCS.getAccountBalanceAndDate("Protocol : After Usage - Get Account Balance from CS",
                this, customerInfo, ts, resultInfo).run();

        BigDecimal getBalanceAfter = ts.getAccountBalance();
        Jive.log("Balance Before : " + getBalanceBefore + " Balance After : " + getBalanceAfter);

        Assert.assertEquals(getBalanceBefore.subtract(new BigDecimal(ts.getCharge())), getBalanceAfter);
        Jive.log("OK : Expected Charges " + ts.getCharge() + " Deducted Successfully!");
    }

    private void verifyInvoicePDF(CustomerInfo customerInfo, String message) throws InterruptedException {
        String verifyString = null;
        verifyString = businessCache.get("VERIFY.STRING").replace("SIMNO", customerInfo.getSerialNumber());

        Thread.sleep(Long.parseLong(businessCache.get("THREAD.SLEEP_USAGE")));
        ts.setJsonFileName("sendBillGenerateRequest.xml");

        Steps.BILL_GENERATION.stepsBilling
                .sendInvoiceRequest("Protocol : Send Bill Process Create Request ", this, resultInfo, customerInfo, ts)
                .run();

        resultInfo = Steps.BILL_GENERATION.stepsBilling
                .getInvoice("Validation : Get Invoice PDF and Validate GRAND TOTAL in PDF", this, resultInfo,
                        customerInfo, ts)
                .run();
        String readInvoiceData = (String) resultInfo.getResult();

        if (readInvoiceData.contains(verifyString)) {
            Jive.log("SIM Number Verified on Bill");
        } else {
            Jive.fail("SIM Number Not Verified on BIll.. Failing..");
        }
        if ("AFTER".equals(message)) {
            if (readInvoiceData.contains(businessCache.get("INVOICE.CHARGE"))) {
                Jive.log("INVOICE USAGE CHARGES VALIDATED SUCCESSFULL");
            } else {
                Jive.fail("INVOICE USAGE CHARGES NOT CORRECT ON BILL.. FAILING.. PLEASE CHECK");
            }
        }
    }

    private JSONArray getResources(String resource) {
        JSONArray resources = new JSONArray(resource);
        return resources;

    }
}
