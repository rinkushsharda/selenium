package com.ericsson.soiv.testcases.scenes.cbio17;

import com.ericsson.jive.core.execution.*;
import com.ericsson.soiv.fixtures.SoivFixture;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.*;
import com.ericsson.soiv.utils.Constants;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static com.ericsson.soiv.utils.http.ReadBusinessData.readBusinessCacheData;

// Done By ZKAPSAR 17th May 2019

@Fixture(SoivFixture.class)
public class PostpaidPOCSSharedAccountSms extends SoivTestBase {

    // Declaration of Class Variables
    private TestResultInfo resultInfo = new TestResultInfo();
    private CustomerInfo customerInfo = new CustomerInfo();
    private TransactionSpecification ts = new TransactionSpecification();
    private LinkedHashMap<String, String> productOfferings = new LinkedHashMap<String, String>();
    private LinkedHashMap<String, String> businessCache = null;

    @Test
    @Id("00000032")
    @Tags("OT-Regression, OT-Postpaid, EC-Controlled, OT-CBiO17")
    @Description("TS769_Postpaid_PO_CS_Shared_Account_SMS")

    public void postpaidPoCsSharedAccountSms() throws IOException, InterruptedException {

        // Declaring Test Case Level Variables

        List<String> member = new ArrayList<String>();
        LinkedHashMap<String, String> memberHash = new LinkedHashMap<String, String>();
        businessCache = new LinkedHashMap<>(readBusinessCacheData(this.getTestCase().getId()));

        customerInfo.setCustomerBillCycle(businessCache.get("CUSTOMER.BILLCYCLE"));
        customerInfo.setOrderMode(businessCache.get("ORDER.MODE"));
        customerInfo.setRequester(businessCache.get("ORDER.REQUESTER"));

        // Contrating of Mother with Basic PO "PO_CS_Postpaid_Basic_M6"

        productOfferings.put("basic", businessCache.get("PO.ID_BASIC"));
        productOfferings.put("optional", businessCache.get("PO.ID_OPTIONAL"));
        productOfferings.put("memberPO", businessCache.get("PO.ID_MEMBER"));

        ts.setProductOfferingIds(productOfferings);

        customerInfo.setNpCodePub(businessCache.get("MSISDN.NPCODEPUB"));
        customerInfo.setPlCodePub(businessCache.get("MSISDN.PLCODEPUB"));
        customerInfo.setSubmIdPub(businessCache.get("MSISDN.SUBMIDPUB"));
        customerInfo.setHlCode(businessCache.get("MSISDN.HLCODE"));
        customerInfo.setRsCode(businessCache.get("MSISDN.RSCODE"));
        customerInfo.setCsControlled(businessCache.get("MSISDN.CSCONTROLLED"));

        Steps.PROVISION.eoc.createCustomerContractBasicPO("Create Customer Mother and Contract with Basic PO", this,
                customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision : Submit Shopping Cart Mother", this, customerInfo, ts, resultInfo)
                .run();

        Steps.PROVISION.eoc
                .getShoppingCart("Provision : Get Shopping Cart of Mother", this, customerInfo, ts, resultInfo).run();

        memberHash.put("motherNumber", customerInfo.getMsisdn());
        memberHash.put("motherCustomer", customerInfo.getCustomerId());
        memberHash.put("motherContract", customerInfo.getContractId());

        Jive.log("Mother Mobile " + memberHash.get("motherNumber"));
        Jive.log("Mother Customer " + memberHash.get("motherCustomer"));
        Jive.log("Mother Contract " + memberHash.get("motherContract"));

        // Contrating of Child1 with Basic PO "PO_CS_Postpaid_Basic_M6"

        Steps.PROVISION.eoc.createCustomerContractBasicPO("Create Customer Child1 and Contract with Basic PO", this,
                customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision : Submit Shopping Cart Child11", this, customerInfo, ts, resultInfo)
                .run();
        Steps.PROVISION.eoc
                .getShoppingCart("Provision : Get Shopping Cart of Child1", this, customerInfo, ts, resultInfo).run();

        memberHash.put("child1Number", customerInfo.getMsisdn());
        memberHash.put("child1Customer", customerInfo.getCustomerId());
        memberHash.put("child1Contract", customerInfo.getContractId());

        Jive.log("Child1 Mobile " + memberHash.get("child1Number"));
        Jive.log("Child1 Mobile " + memberHash.get("child1Customer"));
        Jive.log("Child1 Mobile " + memberHash.get("child1Contract"));

        // Contracting of Child2 with Basic PO "PO_CS_Postpaid_Basic_M6"

        Steps.PROVISION.eoc.createCustomerContractBasicPO("Create Customer Child2 and Contract with Basic PO", this,
                customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision : Submit Shopping Cart Child2", this, customerInfo, ts, resultInfo)
                .run();
        Steps.PROVISION.eoc
                .getShoppingCart("Provision : Get Shopping Cart of Child2", this, customerInfo, ts, resultInfo).run();

        memberHash.put("child2Number", customerInfo.getMsisdn());
        memberHash.put("child2Customer", customerInfo.getCustomerId());
        memberHash.put("child2Contract", customerInfo.getContractId());

        Jive.log("Child2 Mobile " + memberHash.get("child2Number"));
        Jive.log("Child2 Mobile " + memberHash.get("child2Customer"));
        Jive.log("Child2 Mobile " + memberHash.get("child2Contract"));

        // Contracting of Father (Provider) with Basic PO "PO_CS_Postpaid_Basic_M6 and
        // Provider PO "PO_CS_Provider_Voice_M6"

        Steps.PROVISION.eoc.createCustomerContractBasicPO("Create Customer Father and Contract with Basic PO", this,
                customerInfo, ts, resultInfo).run();

        ts.setJsonFileName("AddOptionalPOonBasic.json");
        Steps.PROTOCOLS.businessLogicEOC.addOptionalPOonBasicPO("Provision : Add Optional PO in to Basic PO", this,
                customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision : Submit Shopping Cart", this, customerInfo, ts, resultInfo).run();
        Steps.PROVISION.eoc
                .getShoppingCart("Provision : Create Shopping Cart of Father", this, customerInfo, ts, resultInfo)
                .run();

        memberHash.put("fatherNumber", customerInfo.getMsisdn());
        memberHash.put("fatherCustomer", customerInfo.getCustomerId());
        memberHash.put("fatherContract", customerInfo.getContractId());

        Jive.log("Father Mobile " + memberHash.get("fatherNumber"));
        Jive.log("Father Mobile " + memberHash.get("fatherCustomer"));
        Jive.log("Father Mobile " + memberHash.get("fatherContract"));

        // Adding Member PO "PO_CS_MEMBER_VOICE_M6" to all Members Created

        customerInfo.setCustomerId(memberHash.get("fatherCustomer"));
        member.add(memberHash.get("fatherContract"));
        member.add(memberHash.get("fatherCustomer"));
        member.add(memberHash.get("motherContract"));
        member.add(memberHash.get("motherCustomer"));
        member.add(memberHash.get("child1Contract"));
        member.add(memberHash.get("child1Customer"));
        member.add(memberHash.get("child2Contract"));
        member.add(memberHash.get("child2Customer"));

        membersProvider("Provision : Add All Members in Provider", member);

        // Checking Whether All members are added with Member PO successfully or not

        customerInfo.setMsisdn(memberHash.get("motherNumber"));
        customerInfo.setSharedNumber(memberHash.get("fatherNumber"));
        checkForAddMembers("Check If Member " + memberHash.get("motherNumber") + " Was Added or Not");
        customerInfo.setMsisdn(memberHash.get("child1Number"));
        checkForAddMembers("Check If Member " + memberHash.get("child1Number") + " Was Added or Not");
        customerInfo.setMsisdn(memberHash.get("child2Number"));
        checkForAddMembers("Check If Member " + memberHash.get("child2Number") + " Was Added or Not");
        customerInfo.setMsisdn(memberHash.get("fatherNumber"));
        checkForAddMembers("Check If Member " + memberHash.get("fatherNumber") + " Was Added or Not");

        // Send SMS from Mother Number
        customerInfo.setMsisdn(memberHash.get("motherNumber"));
        customerInfo.setSharedNumber(memberHash.get("fatherNumber"));
        sendSms("Send SMS from " + memberHash.get("motherNumber"), businessCache.get("MOTHER.SMSCOST"));

        Thread.sleep(Long.valueOf(businessCache.get("THREAD.SLEEP_USAGE")));

        // Send SMS from Child1 Number
        customerInfo.setMsisdn(memberHash.get("child1Number"));
        customerInfo.setSharedNumber(memberHash.get("fatherNumber"));
        sendSms("Send SMS from " + memberHash.get("child1Number"), businessCache.get("CHILD1.SMSCOST"));

        Thread.sleep(Long.valueOf(businessCache.get("THREAD.SLEEP_USAGE")));

        // Send SMS from Chil2 Number
        customerInfo.setMsisdn(memberHash.get("child2Number"));
        customerInfo.setSharedNumber(memberHash.get("fatherNumber"));
        sendSms("Send SMS from " + memberHash.get("child2Number"), businessCache.get("CHILD2.SMSCOST"));

        Thread.sleep(Long.valueOf(businessCache.get("THREAD.SLEEP_USAGE")));

        // Send SMS from Father Number
        customerInfo.setMsisdn(memberHash.get("fatherNumber"));
        customerInfo.setSharedNumber(memberHash.get("fatherNumber"));
        sendSms("Send SMS from " + memberHash.get("fatherNumber"), businessCache.get("FATHER.SMSCOST"));

        Thread.sleep(Long.valueOf(businessCache.get("THREAD.SLEEP_INVOICE")));

        // Generating bill for Father Number
        customerInfo.setCustomerId(memberHash.get("fatherCustomer"));
        verifyBill("Verify Bill of" + memberHash.get("fatherCustomer") + "for One time and Usage Charges");

    }

    // Method to Add or Remove the Members from Provider Account

    public void membersProvider(String title, List<String> member) {
        JSONObject jsonObj = null;
        Jive.log(title);
        StringBuilder memberList = new StringBuilder("[");

        ts.setJsonFileName("createShoppingCart.json");
        Steps.PROVISION.eoc.createShoppingCart("Provision : Create Shopping Cart for Adding Member", this, customerInfo,
                ts, resultInfo).run();

        Steps.PROVISION.eoc.getShoppingCart("Provision : Create Shopping Cart for Adding Member", this, customerInfo,
                ts, resultInfo).run();
        customerInfo.setPooiId(JsonHelper.getRequestedJsonElement(ts, productOfferings.get("optional"), "id"));
        Steps.PROTOCOLS.businessLogicEOC
                .getPooi("Getting the POOI for Adding Member", this, customerInfo, ts, resultInfo).run();
        for (int i = 0; i < member.size(); i++) {
            if (i % 2 == 0) {
                memberList = memberList.append("\"").append(member.get(i)).append(":");
            } else if (i == (member.size() - 1)) {
                memberList = memberList.append(member.get(i)).append("\"\n");

            } else {
                memberList = memberList.append(member.get(i)).append("\",\n");
            }
        }
        memberList = memberList.append("]");

        if (member.size() == 0) {
            memberList.setLength(0);
            memberList = memberList.append("[\"").append("\"]");
        }
        String respBody = ts.getResponseBody().replaceFirst("No_Change", "Modify");
        jsonObj = JsonHelper.insertValues(respBody, "memberList", memberList.toString(), "services",
                "serviceCharacteristics", "name");
        ts.setResponseBody(jsonObj.toString());

        Steps.PROTOCOLS.businessLogicEOC
                .updatePooi("Provision : Update POOI for Adding Member", this, customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC.submitShoppingCart("Provision : Submit Shopping Cart for Adding Member", this,
                customerInfo, ts, resultInfo).run();

    }

    // Check if Member is Added Successfully or Not

    public void checkForAddMembers(String title) {
        Jive.log(title);
        String offerProvider = null;
        boolean testForOffer = false;
        String numOfOffers = null;
        Jive.log(title);

        Steps.PROTOCOLS.businessLogicCS.getAccountDetails("Get Account Details to Verify Addition of Members", this,
                customerInfo, ts, resultInfo).run();

        String respBody = ts.getResponseBody();
        numOfOffers = JsonPath.read(respBody, "$.offerInformationList[*].offerID").toString();

        if (numOfOffers.split(",").length > 1) {

            for (int i = 0; i < numOfOffers.split(",").length; i++) {

                try {
                    offerProvider = JsonPath.read(respBody, "$.offerInformationList[" + i + "].offerProviderID");
                } catch (PathNotFoundException path) {
                    Jive.log("PROVIDER LINK NOT THERE");
                    continue;
                }
                if (offerProvider.equals(customerInfo.getSharedNumber())) {
                    testForOffer = true;
                    break;
                }

            }
        }
        if (testForOffer == false) {
            Jive.failAndContinue("MEMBER " + customerInfo.getMsisdn() + " NOT ADDED SUCCESSFULLY : Please check and Try Gain");

        } else {
            Jive.log("MEMBER " + customerInfo.getMsisdn() + " WAS ADDED SUCCESSFULLY");
        }

    }

    // Perform SMS and check if correct charge is deducted

    public void sendSms(String title, String cost) {

        Jive.log(title);

        Steps.PROTOCOLS.businessLogicCS.getAccountBalanceAndDate("Protocol : Before - Get Account Balance from CS",
                this, customerInfo, ts, resultInfo).run();
        BigDecimal balanceBefore = ts.getAccountBalance();

        ts.setExpectedCost(cost);
        ts.setCharge(businessCache.get("USAGE.CHARGE"));// This is only for the CS Charging Validation
        ts.setCalledNumber(Constants.NATIONAL_NUMBER_1);

        Steps.CHARGING_ONLINE.simulator.onlineSms("Protocol : Usage Online SMS", this, resultInfo, customerInfo, ts)
                .run();

        Steps.PROTOCOLS.businessLogicCS.getAccountBalanceAndDate("Protocol : After Usage - Get Account Balance from CS",
                this, customerInfo, ts, resultInfo).run();

        BigDecimal balanceAfter = ts.getAccountBalance();
        Jive.log("Balance Before : " + balanceBefore + " Balance After : " + balanceAfter);

        Assert.assertEquals(balanceBefore.subtract(new BigDecimal(ts.getCharge())), balanceAfter);
        Jive.log("OK : Expected Charges " + ts.getCharge() + " Deducted Successfully!");
    }

    // Verify Bill for Charges

    public void verifyBill(String title) {

        Jive.log(title);
        ts.setInvoicePdfPageNo(Integer.parseInt(businessCache.get("INVOICE.PAGE_1")));
        ts.setInvoiceCharge(businessCache.get("INVOICE.CHARGE_1"));
        ts.setJsonFileName("sendBillGenerateRequest.xml");

        Steps.BILL_GENERATION.stepsBilling.sendInvoiceRequest("Protocol : Send Bill Process Create Request for Father",
                this, resultInfo, customerInfo, ts).run();

        resultInfo = Steps.BILL_GENERATION.stepsBilling
                .getInvoice("Validation : Get Invoice PDF and Validate Charges(net) in PDF for Father", this,
                        resultInfo, customerInfo, ts)
                .run();

        if (!resultInfo.getResult().toString().contains(ts.getInvoiceCharge())) {
            Jive.failAndContinue("Invoice One Time Charge In Bill Is Not Correct");
        }
        /*
         * Uncomment below block from line 380-385 once PO is chnaged by Testing team
         */
        ts.setInvoiceCharge(businessCache.get("INVOICE.CHARGE_2"));

        if (!resultInfo.getResult().toString().contains(ts.getInvoiceCharge())) {
            Jive.failAndContinue("Invoice Usage Charge In BIll Is Not Correct");
        }

    }

}