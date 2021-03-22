package com.ericsson.soiv.testcases.scenes.cbio17;

import com.ericsson.jive.core.execution.*;
import com.ericsson.soiv.fixtures.SoivFixture;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.Constants;
import com.ericsson.soiv.utils.*;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static com.ericsson.soiv.utils.http.ReadBusinessData.readBusinessCacheData;

// Done By ZKAPSAR 13th May 2019

@Fixture(SoivFixture.class)
public class PostpaidPOCSSharedAccountData extends SoivTestBase {

    // Declaration of Class Variables
    private TestResultInfo resultInfo = new TestResultInfo();
    private CustomerInfo customerInfo = new CustomerInfo();
    private TransactionSpecification ts = new TransactionSpecification();
    private long j = 0;
    private String respBody = null;
    private LinkedHashMap<String, String> productOfferings = new LinkedHashMap<String, String>();
    private LinkedHashMap<String, String> businessCache = null;

    @Test
    @Id("00000027")
    @Tags("OT-Regression, OT-Postpaid, EC-Controlled, OT-CBiO17")
    @Description("TS767_Postpaid_PO_CS_Shared_Data")

    public void postpaidPoCsSharedAccountData() throws IOException, InterruptedException {

        // Declaring Test Case Level Variables
        List<String> member = new ArrayList<String>();
        int pamServiceID = 0;
        LinkedHashMap<String, String> memberHash = new LinkedHashMap<String, String>();
        businessCache = new LinkedHashMap<>(readBusinessCacheData(this.getTestCase().getId()));

        customerInfo.setCustomerBillCycle(businessCache.get("CUSTOMER.BILLCYCLE"));
        customerInfo.setOrderMode(businessCache.get("ORDER.MODE"));
        customerInfo.setRequester(businessCache.get("ORDER.REQUESTER"));

        productOfferings.put("basic", businessCache.get("PO.ID_BASIC"));
        productOfferings.put("optional", businessCache.get("PO.ID_OPTIONAL"));
        productOfferings.put("memberPO", businessCache.get("PO.ID_MEMBER"));
        productOfferings.put("providerPO", businessCache.get("PO.ID_PROVIDER"));

        // Contrating of Mother with Basic PO "PO_CS_Postpaid_Basic_M3"

        ts.setProductOfferingIds(productOfferings);

        customerInfo.setNpCodePub(businessCache.get("MSISDN.NPCODEPUB"));
        customerInfo.setPlCodePub(businessCache.get("MSISDN.PLCODEPUB"));
        customerInfo.setSubmIdPub(businessCache.get("MSISDN.SUBMIDPUB"));
        customerInfo.setHlCode(businessCache.get("MSISDN.HLCODE"));
        customerInfo.setRsCode(businessCache.get("MSISDN.RSCODE"));
        customerInfo.setCsControlled(businessCache.get("MSISDN.CSCONTROLLED"));

        Steps.PROVISION.eoc.createCustomerContractBasicPO("Create Customer Mother and Contract with Basic PO", this,
                customerInfo, ts, resultInfo).run();

        ts.setJsonFileName("AddOptionalPOonBasic.json");
        Steps.PROTOCOLS.businessLogicEOC.addOptionalPOonBasicPO("Provision : Add Optional PO in to Basic PO", this,
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

        // Contrating of Child1 with Basic PO "PO_CS_Postpaid_Basic_M3"

        Steps.PROVISION.eoc.createCustomerContractBasicPO("Create Customer Child1 and Contract with Basic PO", this,
                customerInfo, ts, resultInfo).run();

        ts.setJsonFileName("AddOptionalPOonBasic.json");
        Steps.PROTOCOLS.businessLogicEOC.addOptionalPOonBasicPO("Provision : Add Optional PO in to Basic PO", this,
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

        // Contracting of Child2 with Basic PO "PO_CS_Postpaid_Basic_M3"

        Steps.PROVISION.eoc.createCustomerContractBasicPO("Create Customer Child2 and Contract with Basic PO", this,
                customerInfo, ts, resultInfo).run();

        ts.setJsonFileName("AddOptionalPOonBasic.json");
        Steps.PROTOCOLS.businessLogicEOC.addOptionalPOonBasicPO("Provision : Add Optional PO in to Basic PO", this,
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

        // Contracting of Father (Provider) with Basic PO "PO_CS_Postpaid_Basic_M3 and
        // Provider PO "PO_CS_Provider_Data_M3"

        Steps.PROVISION.eoc.createCustomerContractBasicPO("Create Customer Father and Contract with Basic PO", this,
                customerInfo, ts, resultInfo).run();

        ts.setJsonFileName("AddOptionalPOonBasic.json");
        Steps.PROTOCOLS.businessLogicEOC.addOptionalPOonBasicPO("Provision : Add Optional PO in to Basic PO", this,
                customerInfo, ts, resultInfo).run();

        productOfferings.put("optional", productOfferings.get("providerPO"));
        ts.setProductOfferingIds(productOfferings);

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

        // Adding Member PO "PO_CS_MEMBER_VOICE_M4" to all Members Created
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

        // Retreiving the PAM Service ID for Provider (Father)

        customerInfo.setMsisdn(memberHash.get("fatherNumber"));

        Steps.PROTOCOLS.businessLogicCS.getAccountDetails("Get Account Details", this, customerInfo, ts, resultInfo)
                .run();

        respBody = ts.getResponseBody();
        pamServiceID = JsonHelper.searchPamServiceId(respBody, 9);
        customerInfo.setPamServiceId(pamServiceID);

        Jive.log("PAM Service ID Is : " + pamServiceID);
        Jive.log("PAM Class ID Is : 9");

        // Call from Mother Number and Consuming all it's Limit

        customerInfo.setMsisdn(memberHash.get("motherNumber"));
        customerInfo.setSharedNumber(memberHash.get("fatherNumber"));
        callToLimit("Call to " + memberHash.get("motherNumber") + " to Consume Limit",
                businessCache.get("MOTHER.DATACOST"), Integer.parseInt(businessCache.get("MOTHER.DATADUR")), 0);

        Thread.sleep(Long.valueOf(businessCache.get("THREAD.SLEEP_USAGE")));

        // Call from Child1 Number and Consuming all it's Limit
        customerInfo.setMsisdn(memberHash.get("child1Number"));
        customerInfo.setSharedNumber(memberHash.get("fatherNumber"));
        callToLimit("Call to " + memberHash.get("child1Number") + " to Consume Limit",
                businessCache.get("CHILD1.DATACOST"), Integer.parseInt(businessCache.get("CHILD1.DATADUR")), 0);

        Thread.sleep(Long.valueOf(businessCache.get("THREAD.SLEEP_USAGE")));

        // Call from Chil2 Number and Consuming all it's Limit
        customerInfo.setMsisdn(memberHash.get("child2Number"));
        customerInfo.setSharedNumber(memberHash.get("fatherNumber"));
        callToLimit("Call to " + memberHash.get("child2Number") + " to Consume Limit",
                businessCache.get("CHILD2.DATACOST"), Integer.parseInt(businessCache.get("CHILD2.DATADUR")), 0);

        Thread.sleep(Long.valueOf(businessCache.get("THREAD.SLEEP_USAGE")));

        // Call from Father Number and Consuming all it's Limit
        customerInfo.setMsisdn(memberHash.get("fatherNumber"));
        customerInfo.setSharedNumber(memberHash.get("fatherNumber"));
        callToLimit("Call to " + memberHash.get("fatherNumber") + " to Consume Limit",
                businessCache.get("FATHER.DATACOST"), Integer.parseInt(businessCache.get("FATHER.DATADUR")), 0);

        Thread.sleep(Long.valueOf(businessCache.get("THREAD.SLEEP_USAGE")));

        // Call to Mother Number to check call Should fail

        customerInfo.setMsisdn(memberHash.get("motherNumber"));
        callAndCheck("Call to " + memberHash.get("motherNumber") + " to Check call Account Balance",
                businessCache.get("MOTHER.DATACHECKCOST"), Integer.parseInt(businessCache.get("MOTHER.DATACHECKDUR")));

        Thread.sleep(Long.valueOf(businessCache.get("THREAD.SLEEP_USAGE")));
        // Call to Child1 Number to check call Should fail

        customerInfo.setMsisdn(memberHash.get("child1Number"));
        callAndCheck("Call to " + memberHash.get("child1Number") + " to Check call Account Balance",
                businessCache.get("CHILD1.DATACHECKCOST"), Integer.parseInt(businessCache.get("CHILD1.DATACHECKDUR")));

        Thread.sleep(Long.valueOf(businessCache.get("THREAD.SLEEP_USAGE")));
        // Call to Child2 Number to check call Should fail

        customerInfo.setMsisdn(memberHash.get("child2Number"));
        callAndCheck("Call to " + memberHash.get("child2Number") + " to Check call Account Balance",
                businessCache.get("CHILD2.DATACHECKCOST"), Integer.parseInt(businessCache.get("CHILD2.DATACHECKDUR")));

        Thread.sleep(Long.valueOf(businessCache.get("THREAD.SLEEP_USAGE")));
        // Call to Father Number to check call Should fail

        customerInfo.setMsisdn(memberHash.get("fatherNumber"));
        callAndCheck("Call to " + memberHash.get("fatherNumber") + " to Check call Account Balance",
                businessCache.get("FATHER.DATACHECKCOST"), Integer.parseInt(businessCache.get("FATHER.DATACHECKDUR")));

        Thread.sleep(Long.valueOf(businessCache.get("THREAD.SLEEP_INVOICE")));

        customerInfo.setCustomerId(memberHash.get("fatherCustomer"));
        verifyBill("Verify Bill of" + memberHash.get("fatherCustomer"));

        // Reset PAM
        customerInfo.setMsisdn(memberHash.get("fatherNumber"));
        Steps.PROTOCOLS.businessLogicCS.runPam("Reset PAM : For Provider", this, customerInfo, ts, resultInfo).run();

        // same flow after resetPAM

        // Call from Mother Number and Consuming all it's Limit after Reset PAM

        customerInfo.setMsisdn(memberHash.get("motherNumber"));
        customerInfo.setSharedNumber(memberHash.get("fatherNumber"));
        //
        callToLimit("Call to " + memberHash.get("motherNumber") + " to Consume Limit",
                businessCache.get("MOTHER.DATACOST"), Integer.parseInt(businessCache.get("MOTHER.DATADUR")), 1);

        // Call from Child1 Number and Consuming all it's Limit after Reset PAM

        customerInfo.setMsisdn(memberHash.get("child1Number"));
        customerInfo.setSharedNumber(memberHash.get("fatherNumber"));
        callToLimit("Call to " + memberHash.get("child1Number") + " to Consume Limit",
                businessCache.get("CHILD1.DATACOST"), Integer.parseInt(businessCache.get("CHILD1.DATADUR")), 0);

        // Call from Child2 Number and Consuming all it's Limit after Reset PAM

        customerInfo.setMsisdn(memberHash.get("child2Number"));
        customerInfo.setSharedNumber(memberHash.get("fatherNumber"));
        callToLimit("Call to " + memberHash.get("child2Number") + " to Consume Limit",
                businessCache.get("CHILD2.DATACOST"), Integer.parseInt(businessCache.get("CHILD2.DATADUR")), 0);

        // Call from Father Number and Consuming all it's Limit after Reset PAM

        customerInfo.setMsisdn(memberHash.get("fatherNumber"));
        customerInfo.setSharedNumber(memberHash.get("fatherNumber"));
        callToLimit("Call to " + memberHash.get("fatherNumber") + " to Consume Limit",
                businessCache.get("FATHER.DATACOST"), Integer.parseInt(businessCache.get("FATHER.DATADUR")), 0);

        // Call from Mother Number to see if it fails

        customerInfo.setMsisdn(memberHash.get("motherNumber"));
        callAndCheck("Call to " + memberHash.get("motherNumber") + " to Check call",
                businessCache.get("MOTHER.DATACHECKCOST"), Integer.parseInt(businessCache.get("MOTHER.DATACHECKDUR")));

        // Call from Child1 Number to see if it fails

        customerInfo.setMsisdn(memberHash.get("child1Number"));
        callAndCheck("Call to " + memberHash.get("child1Number") + " to Check call",
                businessCache.get("CHILD1.DATACHECKCOST"), Integer.parseInt(businessCache.get("CHILD1.DATACHECKDUR")));

        // Call from Child2 Number to see if it fails

        customerInfo.setMsisdn(memberHash.get("child2Number"));
        callAndCheck("Call to " + memberHash.get("child2Number") + " to Check call",
                businessCache.get("CHILD2.DATACHECKCOST"), Integer.parseInt(businessCache.get("CHILD2.DATACHECKDUR")));

        // Call from Father Number to see if it fails

        customerInfo.setMsisdn(memberHash.get("fatherNumber"));
        callAndCheck("Call to " + memberHash.get("fatherNumber") + " to Check call",
                businessCache.get("FATHER.DATACHECKCOST"), Integer.parseInt(businessCache.get("FATHER.DATACHECKDUR")));

        // Remove CHild2 From Member List

        customerInfo.setCustomerId(memberHash.get("fatherCustomer"));
        member.remove(7);
        member.remove(6);

        membersProvider("Provision : Remove Child2 as Member", member);

        // Check if CHild2 was Removed Successfully

        customerInfo.setSharedNumber(memberHash.get("fatherNumber"));
        customerInfo.setMsisdn(memberHash.get("child2Number"));
        checkForRemoveMembers("Check If Member " + memberHash.get("child2Number") + " Was Removed or Not");

        // Try call from Child2 , it should fail

        customerInfo.setMsisdn(memberHash.get("child2Number"));
        callAndCheck("Call to " + memberHash.get("child2Number") + " to Check call",
                businessCache.get("CHILD2.DATACHECKCOST"), Integer.parseInt(businessCache.get("CHILD2.DATACHECKDUR")));

        // Remove Child1 From Member List through Delete Member PO

        member.remove(5);
        member.remove(4);
        customerInfo.setCustomerId(memberHash.get("child1Customer"));
        productOfferings.put("optional", productOfferings.get("memberPO"));
        ts.setProductOfferingIds(productOfferings);

        ts.setActionCode("Delete");
        ts.setReasonCode("DUNND");
        Steps.PROVISION.eoc.updateOptionalPO("Update Member PO-> Remove Provider PO from Father", this, customerInfo,
                ts, resultInfo).run();

        productOfferings.put("optional", productOfferings.get("providerPO"));
        ts.setProductOfferingIds(productOfferings);

        // Check if Child1 was Removed Successfully

        customerInfo.setSharedNumber(memberHash.get("fatherNumber"));
        customerInfo.setMsisdn(memberHash.get("child1Number"));
        checkForRemoveMembers("Check If Member " + memberHash.get("child1Number") + "Was Removed or Not");

        // Try call from Child1 , it shoudld fail

        customerInfo.setMsisdn(memberHash.get("child1Number"));
        callAndCheck("Call to " + memberHash.get("child1Number") + " to Check call",
                businessCache.get("CHILD1.DATACHECKCOST"), Integer.parseInt(businessCache.get("CHILD1.DATACHECKDUR")));

        // Add Child1 and Child2 Again with Member PO

        customerInfo.setCustomerId(memberHash.get("fatherCustomer"));
        member.add(memberHash.get("child1Contract"));
        member.add(memberHash.get("child1Customer"));
        member.add(memberHash.get("child2Contract"));
        member.add(memberHash.get("child2Customer"));
        membersProvider("Provision : Add Members that were Removed earlier in Provider", member);

        // Check if Child1 and Child2 are Added or not
        customerInfo.setMsisdn(memberHash.get("child1Number"));
        customerInfo.setSharedNumber(memberHash.get("fatherNumber"));
        checkForAddMembers("Check If Member " + memberHash.get("child1Number") + "Was Added or Not");
        customerInfo.setMsisdn(memberHash.get("child2Number"));
        checkForAddMembers("Check If Member " + memberHash.get("child2Number") + " Was Added or Not");

        // Reset PAM

        customerInfo.setMsisdn(memberHash.get("fatherNumber"));
        customerInfo.setSharedNumber(memberHash.get("fatherNumber"));

        Steps.PROTOCOLS.businessLogicCS.runPam("Reset PAM : For Provider", this, customerInfo, ts, resultInfo).run();

        // Call to Limit from Child1

        customerInfo.setMsisdn(memberHash.get("child1Number"));
        customerInfo.setSharedNumber(memberHash.get("fatherNumber"));
        callToLimit("Call to " + memberHash.get("motherNumber") + " to Consume Limit",
                businessCache.get("CHILD1.DATACOST"), Integer.parseInt(businessCache.get("CHILD1.DATADUR")), 1);

        // Call to Limit from Child2

        customerInfo.setMsisdn(memberHash.get("child2Number"));
        customerInfo.setSharedNumber(memberHash.get("fatherNumber"));
        callToLimit("Call to " + memberHash.get("motherNumber") + " to Consume Limit",
                businessCache.get("CHILD2.DATACOST"), Integer.parseInt(businessCache.get("CHILD2.DATADUR")), 0);

        // Remove All Members from List

        customerInfo.setCustomerId(memberHash.get("fatherCustomer"));
        member.clear();
        membersProvider("Provision : Remove All Members from Provider", member);

        // Check if all Members are removed Successfully or not

        customerInfo.setSharedNumber(memberHash.get("fatherNumber"));
        customerInfo.setMsisdn(memberHash.get("fatherNumber"));
        checkForRemoveMembers("Check If Member " + memberHash.get("fatherNumber") + " Was Removed or Not");
        customerInfo.setMsisdn(memberHash.get("motherNumber"));
        checkForRemoveMembers("Check If Member " + memberHash.get("motherNumber") + " Was Removed or Not");
        customerInfo.setMsisdn(memberHash.get("child1Number"));
        checkForRemoveMembers("Check If Member " + memberHash.get("child1Number") + " Was Removed or Not");
        customerInfo.setMsisdn(memberHash.get("child2Number"));
        checkForRemoveMembers("Check If Member " + memberHash.get("child2Number") + " Was Removed or Not");

        // Deactivate Provider PO on Father

        ts.setActionCode("Deactivate");
        ts.setReasonCode("DUNND");
        Steps.PROVISION.eoc.updateOptionalPO("Update Member PO-> Remove Provider PO from Father", this, customerInfo,
                ts, resultInfo).run();

        // Reactivate Provider PO on Father

        ts.setActionCode("Reactivate");
        ts.setReasonCode("DUNNA");
        Steps.PROVISION.eoc.updateOptionalPO("Update Member PO-> Remove Provider PO from Father", this, customerInfo,
                ts, resultInfo).run();

        // Add Member PO on Father

        customerInfo.setCustomerId(memberHash.get("fatherCustomer"));
        member.clear();
        member.add(memberHash.get("fatherContract"));
        member.add(memberHash.get("fatherCustomer"));
        membersProvider("Provision : Add Members in Provider", member);

        // Check if Member Added successfully or not

        customerInfo.setSharedNumber(memberHash.get("fatherNumber"));
        customerInfo.setMsisdn(memberHash.get("fatherNumber"));
        checkForAddMembers("Check If Member " + memberHash.get("fatherNumber") + " Was Added or Not");

        // Reset PAM
        customerInfo.setMsisdn(memberHash.get("fatherNumber"));
        Steps.PROTOCOLS.businessLogicCS.runPam("Reset PAM : For Provider", this, customerInfo, ts, resultInfo).run();

        // Call from Father Number to Limit
        customerInfo.setMsisdn(memberHash.get("fatherNumber"));
        customerInfo.setSharedNumber(memberHash.get("fatherNumber"));
        // Need to check here
        callToLimit("Call to " + memberHash.get("motherNumber") + " to Consume Limit",
                businessCache.get("FATHER.DATACOST"), Integer.parseInt(businessCache.get("FATHER.DATADUR")), 1);

    }

    // Method to Add or Remove the Members from Provider Account

    public void membersProvider(String title, List<String> member) {
        JSONObject jsonObj = null;
        Jive.log(title);
        StringBuilder memberList = new StringBuilder("[");
        ts.setJsonFileName("createShoppingCart.json");
        Steps.PROVISION.eoc
                .createShoppingCart("Provision : Create Shopping Cart LAST", this, customerInfo, ts, resultInfo).run();
        Steps.PROVISION.eoc.getShoppingCart("Provision : Create Shopping Cart LAST", this, customerInfo, ts, resultInfo)
                .run();
        customerInfo.setPooiId(JsonHelper.getRequestedJsonElement(ts, productOfferings.get("optional"), "id"));
        Steps.PROTOCOLS.businessLogicEOC.getPooi("Getting the POOI ", this, customerInfo, ts, resultInfo).run();
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
        respBody = ts.getResponseBody().replaceFirst("No_Change", "Modify");
        jsonObj = JsonHelper.insertValues(respBody, "memberList", memberList.toString(), "services",
                "serviceCharacteristics", "name");
        ts.setResponseBody(jsonObj.toString());

        Steps.PROTOCOLS.businessLogicEOC.updatePooi("Provision : Update POOI", this, customerInfo, ts, resultInfo)
                .run();

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision : Submit Shopping Cart", this, customerInfo, ts, resultInfo).run();

    }

    // Check if Member is Added Successfully or Not

    public void checkForAddMembers(String title) {
        Jive.log(title);
        String offerProvider = null;
        boolean testForOffer = false;
        String respBody = null;
        String numOfOffers = null;
        Jive.log(title);

        Steps.PROTOCOLS.businessLogicCS.getAccountDetails("Get Account Details", this, customerInfo, ts, resultInfo)
                .run();

        respBody = ts.getResponseBody();
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

    // Check if Member is Removed Successfully or Not

    public void checkForRemoveMembers(String title) {
        Jive.log(title);
        String offerProvider = null;
        boolean testForOffer = false;
        String respBody = null;
        String numOfOffers = null;
        Jive.log(title);
        Steps.PROTOCOLS.businessLogicCS.getAccountDetails("Get Account Details", this, customerInfo, ts, resultInfo)
                .run();
        respBody = ts.getResponseBody();

        numOfOffers = JsonPath.read(respBody, "$.offerInformationList[*].offerID").toString();

        if (numOfOffers.split(",").length > 1) {

            for (int i = 0; i < numOfOffers.split(",").length; i++) {

                try {
                    offerProvider = JsonPath.read(respBody, "$.offerInformationList[" + i + "].offerProviderID");
                }

                catch (PathNotFoundException path) {
                    Jive.log("PROVIDER LINK NOT THERE");
                    continue;
                }

                if (offerProvider.equals(customerInfo.getSharedNumber())) {
                    testForOffer = true;
                    break;
                }

            }
        }
        if (testForOffer == true) {
            Jive.fail("MEMBER " + customerInfo.getMsisdn() + " NOT Removed SUCCESSFULLY : Please check and Try Gain");

        } else {
            Jive.log("MEMBER " + customerInfo.getMsisdn() + " WAS REMOVED SUCCESSFULLY");
        }

    }

    // Perform Call to consume it's limit Say 1GB of Data

    public void callToLimit(String title, String cost, int duration, int resetPam) {

        Jive.log(title);
        int count = 0;
        int resetDone = 0;
        long counterConsumed = 0;
        int counterId = 0;
        String sharedNumber = null;
        if ((resetPam != 0) && (resetDone == 0)) {
            resetDone = 1;
            j = 0;
        } else {
            resetDone = 0;
        }

        j = j + duration;

        ts.setDuration(duration);
        ts.setExpectedCost(cost); // This is only for the CS Charging Validation
        ts.setRatingGroup(businessCache.get("RATING.GROUP")); // This Charges will be used to validate Invoice PDF
                                                              // Charges

        Steps.CHARGING_ONLINE.simulator
                .onlineGy("Protocol : National - Data Online Charging CIP IP Simulator for " + customerInfo.getMsisdn(),
                        this, resultInfo, customerInfo, ts)
                .run();

        sharedNumber = customerInfo.getSharedNumber();

        customerInfo.setSharedNumber(customerInfo.getMsisdn());
        customerInfo.setMsisdn(sharedNumber);

        Steps.PROTOCOLS.businessLogicCS.getThreshholdAndCounters(
                "Protocol : After - Get Counters Balance from CS for Member", this, customerInfo, ts, resultInfo).run();
        respBody = ts.getResponseBody();

        count = JsonPath.read(respBody, "$.usageCounterUsageThresholdInformation[*].usageCounterID").toString()
                .split(",").length;
        for (int counter = 0; counter < count; counter++) {
            counterConsumed = Long.parseLong(JsonPath.read(respBody,
                    "$.usageCounterUsageThresholdInformation[" + counter + "].usageCounterValue"));
            counterId = JsonPath.read(respBody,
                    "$.usageCounterUsageThresholdInformation[" + counter + "].usageCounterID");
            if (counterId == Integer.parseInt(businessCache.get("USAGE.COUNTER_1"))) {
                if (counterConsumed == duration) {
                    Jive.log("Counter Consumed Is : " + counterConsumed);
                } else {
                    Jive.failAndContinue("FAILED : Correct Counter values not consumed for CounterID" + counterId);
                }
            }
            if (counterId == Integer.parseInt(businessCache.get("USAGE.COUNTER_2"))) {
                if (counterConsumed == j) {
                    Jive.log("Counter Consumed Is : " + counterConsumed);
                } else {
                    Jive.failAndContinue("FAILED : Correct Counter values not consumed for CounterID " + counterId);
                }
            }
        }

    }

    // Perform Call to check if Call Balance After Call

    public void callAndCheck(String title, String cost, int duration) {

        Jive.log(title);

        ts.setExpectedCost(cost); // This is only for the CS Charging Validation
        ts.setCalledNumber(Constants.NATIONAL_NUMBER_1);
        ts.setDuration(duration);

        Steps.CHARGING_ONLINE.simulator.onlineGy(
                "Protocol : National - Data Usage Online Charging CIP IP Simulator from " + customerInfo.getMsisdn(),
                this, resultInfo, customerInfo, ts).run();

        Steps.PROTOCOLS.businessLogicCS.getAccountBalanceAndDate(
                "Protocol : After Data Usage - Get Account Balance from CS", this, customerInfo, ts, resultInfo).run();

        BigDecimal getBalanceAfter = ts.getAccountBalance();

        if (ts.getAccountBalance().intValue() < Integer.parseInt(businessCache.get("MAINACCOUNT.BALANCE"))) {

            Jive.log("Balance After Call is " + getBalanceAfter + " which is <"
                    + businessCache.get("MAINACCOUNT.BALANCE"));

        } else {
            Jive.failAndContinue("Balance After Call is " + getBalanceAfter + " which is > or ="
                    + businessCache.get("MAINACCOUNT.BALANCE") + " Should be < "
                    + businessCache.get("MAINACCOUNT.BALANCE") + "..so. Failing...");

        }

    }

    // Verify Bill for some messages

    public void verifyBill(String title) {
        Jive.log(title);
        String[] billLines = null;
        int countPersonalLimit = 0;
        int countCommonLimit = 0;
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
            Jive.failAndContinue("Invoie One Time Charge In Bill Is Not Correct");
        }
        ts.setInvoiceCharge(businessCache.get("INVOICE.CHARGE_2"));

        if (!resultInfo.getResult().toString().contains(ts.getInvoiceCharge())) {
            Jive.failAndContinue("Invoie Usage Charge In BIll Is Not Correct");
        }

        ts.setInvoicePdfPageNo(Integer.parseInt(businessCache.get("INVOICE.PAGE_2")));

        resultInfo = Steps.BILL_GENERATION.stepsBilling
                .getInvoice("Validation : Get Invoice PDF and Validate Charges(net) in PDF for Father", this,
                        resultInfo, customerInfo, ts)
                .run();

        billLines = resultInfo.getResult().toString().split("\r\n|\r|\n");

        for (int i = 0; i < billLines.length; i++) {
            if (billLines[i].contains(businessCache.get("INVOICE.READLINE_1"))
                    || billLines[i].contains(businessCache.get("INVOICE.READLINE_2"))) {
                countPersonalLimit++;
            }
            if (billLines[i].contains(businessCache.get("INVOICE.READLINE_3"))) {
                countCommonLimit++;
            }

        }
        if ((countPersonalLimit != 4) || (countCommonLimit != 1)) {
            Jive.failAndContinue("Either Personal or Common Counter Count in Bill Is Not Correct... Please Check");
        }

    }

    // Replace existng Values in JSON

    public JSONObject replaceValues(String json, String searchValue, String insertValue) {

        JSONObject jsonObject = new JSONObject(json);
        jsonObject.remove(searchValue);
        jsonObject.put(searchValue, insertValue);

        return jsonObject;
    }

}