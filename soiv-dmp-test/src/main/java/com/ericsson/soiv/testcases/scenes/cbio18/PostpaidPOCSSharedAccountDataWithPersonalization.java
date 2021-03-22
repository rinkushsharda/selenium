package com.ericsson.soiv.testcases.scenes.cbio18;

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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static com.ericsson.soiv.utils.http.ReadBusinessData.readBusinessCacheData;

// Done By ZKAPSAR 5th July 2019

@Fixture(SoivFixture.class)
public class PostpaidPOCSSharedAccountDataWithPersonalization extends SoivTestBase {
/*
This TC includes the scenario that was discussed with FNT team(Jaswinder Sir), as per Doc there are
8 UC, but some of them were covered in CBIO17 Shared Account TC, so here this is the
main scenario that need to be Automated
 */
    // Declaration of Class Variables
    private TestResultInfo resultInfo = new TestResultInfo();
    private CustomerInfo customerInfo = new CustomerInfo();
    private TransactionSpecification ts = new TransactionSpecification();
    private long commonCounter = 0;
    private long personalCounter=0;
    private String respBody = null;
    private LinkedHashMap<String, String> productOfferings = new LinkedHashMap<String, String>();
    private LinkedHashMap<String,String> businessCache = null;

    @Test
    @Id("00000060")
    @Tags("OT-Regression, OT-Postpaid, EC-Controlled, OT-CBiO18")
    @Description("BUC27A_D_2 Shared Accounts with personalized limits for shared pool and for member balances")

    public void postpaidPoCsSharedAccountDataWithPersonalization() throws IOException {

        // Declaring Test Case Level Variables
        List<String> member = new ArrayList<String>();
        int pamServiceID = 0;
        LinkedHashMap<String, String> memberHash = new LinkedHashMap<String, String>();
        businessCache = new LinkedHashMap<>(readBusinessCacheData(this.getTestCase().getId()));

        customerInfo.setCustomerBillCycle(businessCache.get("CUSTOMER.BILLCYCLE"));
        customerInfo.setOrderMode(businessCache.get("ORDER.MODE"));
        customerInfo.setRequester(businessCache.get("ORDER.REQUESTER"));

        productOfferings.put("basic", businessCache.get("PO.ID_BASIC"));
        productOfferings.put("optional", businessCache.get("PO.ID_PROVIDER"));
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

 //Creating Contract for Father Provider

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

// Adding Member PO to all Members Created
        customerInfo.setCustomerId(memberHash.get("fatherCustomer"));
        member.add(memberHash.get("fatherContract"));
        member.add(memberHash.get("fatherCustomer"));
        member.add(memberHash.get("fatherNumber"));
        member.add(memberHash.get("motherContract"));
        member.add(memberHash.get("motherCustomer"));
        member.add(memberHash.get("motherNumber"));

        addRemoveMembers("Provision : Add Members to all", member,"ADD");

        member.clear();
        customerInfo.setCustomerId(memberHash.get("fatherCustomer"));
        member.add(memberHash.get("motherContract"));
        member.add(memberHash.get("motherCustomer"));
        member.add(memberHash.get("motherNumber"));
        personalizeMembers("Personalize for "+memberHash.get("motherNumber"),member);

// Checking Whether All members are added with Member PO successfully or not

        customerInfo.setMsisdn(memberHash.get("motherNumber"));
        customerInfo.setSharedNumber(memberHash.get("fatherNumber"));
        checkForAddMembers("Check If Member " + memberHash.get("motherNumber") + " Was Added or Not");
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

// Call from Father Number and Consuming all it's Limit
        customerInfo.setMsisdn(memberHash.get("fatherNumber"));
        customerInfo.setSharedNumber(memberHash.get("fatherNumber"));
        callToLimit("Call to " + memberHash.get("fatherNumber") + " to Consume Limit", businessCache.get("FATHER.DATACOST"), Integer.parseInt(businessCache.get("FATHER.DATADUR")), 0,0);

 // Call to Father Number to check call Should fail
        customerInfo.setMsisdn(memberHash.get("fatherNumber"));
        ts.setExpectedValidationMessage("DIAMETER_END_USER_SERVICE_DENIED (4010)");
        callAndCheck("Call to " + memberHash.get("fatherNumber") + " to Check call Account Balance", businessCache.get("FATHER.DATACHECKCOST"), Integer.parseInt(businessCache.get("FATHER.DATACHECKDUR")));

//Call from Mother NUmber
        customerInfo.setMsisdn(memberHash.get("motherNumber"));
        customerInfo.setSharedNumber(memberHash.get("fatherNumber"));
        callToLimit("Call to " + memberHash.get("motherNumber") + " to Consume Limit", businessCache.get("MOTHER.DATACOST"), Integer.parseInt(businessCache.get("MOTHER.DATADUR")), 1,0);

        customerInfo.setMsisdn(memberHash.get("motherNumber"));
        customerInfo.setSharedNumber(memberHash.get("fatherNumber"));
        callToLimit("Call to " + memberHash.get("motherNumber") + " to Consume Limit", businessCache.get("MOTHER.DATACOST"), Integer.parseInt(businessCache.get("MOTHER.DATADUR")), 0,0);

// Call to Mother Number to check call Should fail
        customerInfo.setMsisdn(memberHash.get("motherNumber"));
        ts.setExpectedValidationMessage("DIAMETER_END_USER_SERVICE_DENIED (4010)");
        callAndCheck("Call to " + memberHash.get("motherNumber") + " to Check call Account Balance", businessCache.get("MOTHER.DATACHECKCOST"), Integer.parseInt(businessCache.get("MOTHER.DATACHECKDUR")));

// Delete Member PO from both Father and Mother
        member.clear();
        customerInfo.setCustomerId(memberHash.get("fatherCustomer"));
        member.add(memberHash.get("fatherContract"));
        member.add(memberHash.get("fatherCustomer"));
        member.add(memberHash.get("fatherNumber"));
        member.add(memberHash.get("motherContract"));
        member.add(memberHash.get("motherCustomer"));
        member.add(memberHash.get("motherNumber"));

        addRemoveMembers("Provision : Remove All Members in Provider", member,"DELETE");

// Check if Father and Mother were Removed Successfully as Member
        customerInfo.setSharedNumber(memberHash.get("fatherNumber"));
        customerInfo.setMsisdn(memberHash.get("fatherNumber"));
        checkForRemoveMembers("Check If Member " + memberHash.get("fatherNumber") + " Was Removed or Not");
        customerInfo.setMsisdn(memberHash.get("motherNumber"));
        checkForRemoveMembers("Check If Member " + memberHash.get("motherNumber") + " Was Removed or Not");

 // Deactivate Provider PO on Father
        customerInfo.setCustomerId(memberHash.get("fatherCustomer"));
        ts.setActionCode("Deactivate");
        ts.setReasonCode("DUNND");
        Steps.PROVISION.eoc.updateOptionalPO("Update Member PO-> Remove Provider PO from Father", this, customerInfo,
                ts, resultInfo).run();

// Reactivate Provider PO on Father
        ts.setActionCode("Reactivate");
        ts.setReasonCode("DUNNA");
        Steps.PROVISION.eoc.updateOptionalPO("Update Member PO-> Remove Provider PO from Father", this, customerInfo,
                ts, resultInfo).run();

// Reset PAM
        customerInfo.setMsisdn(memberHash.get("fatherNumber"));
        Steps.PROTOCOLS.businessLogicCS.runPam("Reset PAM : For Provider", this, customerInfo, ts, resultInfo).run();

// Add Member PO on both Father and Mother
        member.clear();
        customerInfo.setCustomerId(memberHash.get("fatherCustomer"));
        member.add(memberHash.get("fatherContract"));
        member.add(memberHash.get("fatherCustomer"));
        member.add(memberHash.get("fatherNumber"));
        member.add(memberHash.get("motherContract"));
        member.add(memberHash.get("motherCustomer"));
        member.add(memberHash.get("motherNumber"));

        addRemoveMembers("Provision : Add All Members in Provider", member,"ADD");

//Check if Mother and Father Added as Member or Not
//Uncomment line 219 after FIx for TR HX76353 come's
        customerInfo.setMsisdn(memberHash.get("motherNumber"));
        customerInfo.setSharedNumber(memberHash.get("fatherNumber"));
        checkForAddMembers("Check If Member " + memberHash.get("motherNumber") + " Was Added or Not");
        customerInfo.setMsisdn(memberHash.get("fatherNumber"));
        checkForAddMembers("Check If Member " + memberHash.get("fatherNumber") + " Was Added or Not");

// Call from Father Number and Consuming all it's Limit
        customerInfo.setMsisdn(memberHash.get("fatherNumber"));
        customerInfo.setSharedNumber(memberHash.get("fatherNumber"));
        callToLimit("Call to " + memberHash.get("fatherNumber") + " to Consume Limit", businessCache.get("FATHER.DATACOST"), Integer.parseInt(businessCache.get("FATHER.DATADUR")), 1,1);

// Call to Father Number to check call Should fail
        customerInfo.setMsisdn(memberHash.get("fatherNumber"));
        ts.setExpectedValidationMessage("DIAMETER_END_USER_SERVICE_DENIED (4010)");
        callAndCheck("Call to " + memberHash.get("fatherNumber") + " to Check call Account Balance", businessCache.get("FATHER.DATACHECKCOST"), Integer.parseInt(businessCache.get("FATHER.DATACHECKDUR")));

//Call from Mother Number and Consuming all it's Limit
//Uncomment line 238 after FIx when EP for TR HX76353 come's

        customerInfo.setMsisdn(memberHash.get("motherNumber"));
        customerInfo.setSharedNumber(memberHash.get("fatherNumber"));
        callToLimit("Call to " + memberHash.get("motherNumber") + " to Consume Limit", businessCache.get("MOTHER.DATACOST"), Integer.parseInt(businessCache.get("MOTHER.DATADUR")), 1,0);

// Call to Mother Number to check call Should fail
//Uncomment line 246 after EP for TR HX76353 come's

        customerInfo.setMsisdn(memberHash.get("motherNumber"));
        customerInfo.setSharedNumber(memberHash.get("fatherNumber"));
        ts.setExpectedValidationMessage("DIAMETER_END_USER_SERVICE_DENIED (4010)");
        callAndCheck("Call to " + memberHash.get("motherNumber") + " to Check call Account Balance", businessCache.get("FATHER.DATACHECKCOST"), Integer.parseInt(businessCache.get("FATHER.DATACHECKDUR")));

}

    public void addRemoveMembers(String title, List<String> member,String addRemove) {
        JSONObject jsonObj = null;
        String body=null;
        StringBuilder bodyUpdatePooi=new StringBuilder("");
        String tempBodyUpdatePooi=null;
        Jive.log(title);
        ts.setJsonFileName("createShoppingCart.json");
        Steps.PROVISION.eoc
                .createShoppingCart("Provision : Create Shopping Cart LAST", this, customerInfo, ts, resultInfo).run();
        Steps.PROVISION.eoc.getShoppingCart("Provision : Create Shopping Cart LAST", this, customerInfo, ts, resultInfo)
                .run();
        customerInfo.setPooiId(JsonHelper.getRequestedJsonElement(ts, productOfferings.get("optional"), "id"));
        Steps.PROTOCOLS.businessLogicEOC.getPooi("Getting the POOI ", this, customerInfo, ts, resultInfo).run();

        for (int i=3;i<=member.size();) {
            if (i % 3 == 0) {
                body = RawMessages.getBodyFromTextFilePlacedInTestStep(this.getClass().getName(),"protocols/eoc",
                        "ModifyMemberStatus.json").toString();
                for (int j=i;j>=(j-3);j--) {

                    body = body.replace("{{MSISDN}}", member.get(j-1));
                    body = body.replace("{{CUSTOMERID}}", member.get(j-2));
                    body = body.replace("{{CONTRACTID}}", member.get(j-3));
                    body = body.replace("{{ACTION}}", addRemove);
                    body = body.replace("{{BALANCE}}", "");
                if ("DELETE".equals(addRemove))
                {
                    body=body.substring(0,body.lastIndexOf(","))+"}\"]";
                }
                    break;
                }

                 if (member.size() > 3){
                    if (i==member.size()) {
                        bodyUpdatePooi=bodyUpdatePooi.append(body);
                    }
                    else {
                        bodyUpdatePooi = bodyUpdatePooi.append(body).append(",");
                    }
                }
                else
                {
                    bodyUpdatePooi=bodyUpdatePooi.append(body);
                }
                i=i+3;

            }
        }

         tempBodyUpdatePooi=bodyUpdatePooi.toString().replace("]","").replace("[","");
         bodyUpdatePooi=new StringBuilder(tempBodyUpdatePooi);
         bodyUpdatePooi= bodyUpdatePooi.insert(0,"[");
        bodyUpdatePooi=bodyUpdatePooi.append("]");

        respBody = ts.getResponseBody().replaceFirst("No_Change", "Modify");
        jsonObj = JsonHelper.insertValues(respBody, "deltaMemberList", bodyUpdatePooi.toString(), "services",
                "serviceCharacteristics", "name");
        ts.setResponseBody(jsonObj.toString());

        Steps.PROTOCOLS.businessLogicEOC.updatePooi("Provision : Update POOI", this, customerInfo, ts, resultInfo)
                .run();

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision : Submit Shopping Cart", this, customerInfo, ts, resultInfo).run();

    }

    public void personalizeMembers(String title, List<String> member) {
        JSONObject jsonObj = null;
        String body=null;
        StringBuilder bodyUpdatePooi= new StringBuilder("");
        Jive.log(title);
        ts.setJsonFileName("createShoppingCart.json");
        Steps.PROVISION.eoc
                .createShoppingCart("Provision : Create Shopping Cart for Personalize", this, customerInfo, ts, resultInfo).run();
        Steps.PROVISION.eoc.getShoppingCart("Provision : Create Shopping Cart for Personalize", this, customerInfo, ts, resultInfo)
                .run();
        customerInfo.setPooiId(JsonHelper.getRequestedJsonElement(ts, productOfferings.get("optional"), "id"));
        Steps.PROTOCOLS.businessLogicEOC.getPooi("Getting the POOI ", this, customerInfo, ts, resultInfo).run();

        for (int i=3;i<=member.size();) {
            if (i % 3 == 0) {
                body = RawMessages.getBodyFromTextFilePlacedInTestStep(this.getClass().getName(),"protocols/eoc",
                        "ModifyMemberPersonalization.json").toString();
                for (int j=i;j>=(j-3);j--) {
                    body = body.replace("{{MSISDN}}", member.get(j-1));
                    body = body.replace("{{CUSTOMERID}}", member.get(j-2));
                    body = body.replace("{{CONTRACTID}}", member.get(j-3));
                    body = body.replace("{{ACTION}}", "CHANGE");
                    body = body.replace("{{LRSPERSONALLIMIT}}", businessCache.get("LRSPERSONALLIMIT.NAME"));
                    body = body.replace("{{PERSONALLIMITCHARACTERISTICSNAME}}", businessCache.get("PERSONALLIMITCHARACTERISTICS.NAME"));
                    body = body.replace("{{PERSONALLIMITCHARACTERISTICSVALUE}}", businessCache.get("PERSONALLIMITCHARACTERISTICS.VALUE"));

                    break;
                }

                if (member.size() > 3){
                    if (i==member.size()) {
                        bodyUpdatePooi=bodyUpdatePooi.append(body);
                    }
                    else {
                        bodyUpdatePooi = bodyUpdatePooi.append(body).append(",");
                    }
                }
                else
                {
                    bodyUpdatePooi=bodyUpdatePooi.append(body);
                }
                i=i+3;

            }
        }

        respBody = ts.getResponseBody().replaceFirst("No_Change", "Modify");
        jsonObj = JsonHelper.insertValues(respBody, "deltaMemberList", bodyUpdatePooi.toString(), "services",
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
            Jive.fail("MEMBER " + customerInfo.getMsisdn() + " NOT ADDED SUCCESSFULLY : Please check and Try Gain");

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

// Perform Call to consume it's limit

    public void callToLimit(String title, String cost, int duration, int resetPersonalCounter,int resetCommonCounter) {

        Jive.log(title);
        int count = 0;
        long counterConsumed = 0;
        int counterId = 0;
        String sharedNumber = null;

       if (resetPersonalCounter != 0)
       {
           personalCounter=0;
       }
        if (resetCommonCounter != 0)
        {
            commonCounter=0;
        }

        commonCounter = commonCounter + duration;
        personalCounter=personalCounter+duration;

        ts.setDuration(duration);
        ts.setExpectedCost(cost); // This is only for the CS Charging Validation
        ts.setRatingGroup(businessCache.get("RATING.GROUP")); // This Charges will be used to validate Invoice PDF Charges

        Steps.CHARGING_ONLINE.simulator.onlineGy("Protocol : National - Data Online Charging CIP IP Simulator for "+customerInfo.getMsisdn(),
                this, resultInfo, customerInfo, ts).run();

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
                if (counterConsumed == personalCounter) {
                    Jive.log("Counter Consumed Is : " + counterConsumed);
                } else {
                    Jive.fail("FAILED : Correct Counter values not consumed for CounterID" + counterId);
                }
            }
            if (counterId == Integer.parseInt(businessCache.get("USAGE.COUNTER_2"))) {
                if (counterConsumed == commonCounter) {
                    Jive.log("Counter Consumed Is : " + counterConsumed);
                } else {
                    Jive.fail("FAILED : Correct Counter values not consumed for CounterID " + counterId);
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

        Steps.CHARGING_ONLINE.simulator
                .onlineGy("Protocol : National - Data Usage Online Charging CIP IP Simulator for "+customerInfo.getMsisdn(), this, resultInfo,
                        customerInfo, ts)
                .run();
    }

}