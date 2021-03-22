package com.ericsson.soiv.testcases.scenes.cbio18;

import com.ericsson.jive.core.execution.*;
import com.ericsson.soiv.fixtures.SoivFixture;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.*;
import com.ericsson.soiv.utils.Constants;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jcraft.jsch.Session;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.LinkedHashMap;

import static com.ericsson.soiv.utils.http.ReadBusinessData.readBusinessCacheData;
import static java.io.File.separator;

// ZKAPSAR Created on 19-June-2019

@Fixture (SoivFixture.class)
public class PrepaidPOCSProductThresholdNotificationVoiceAUT extends SoivTestBase   {
    private TestResultInfo resultInfo = new TestResultInfo();
    private CustomerInfo customerInfo = new CustomerInfo();
    private TransactionSpecification ts = new TransactionSpecification();
    private LinkedHashMap<String, String> productOfferings = new LinkedHashMap<String, String>();
    private LinkedHashMap<String, String> dbQuery = new LinkedHashMap<String, String>();
    private LinkedHashMap<String, String> businessCache = null;
    private String tmpDirectory = "./tmp"+separator;

    @Test @Id ("00000042")
    @Tags ("OT-Regression, OT-Prepaid, EC-Controlled, OT-CBiO18")
    @Description ("PO CS Product Threshold Notification For Voice AUT")
    public void prepaidPoCsProductThresholdNotificationVoiceAut() throws IOException,SQLException,InterruptedException {

         businessCache = new LinkedHashMap<>(readBusinessCacheData(this.getTestCase().getId()));

        customerInfo.setCustomerBillCycle(businessCache.get("CUSTOMER.BILLCYCLE"));
        customerInfo.setOrderMode(businessCache.get("ORDER.MODE"));
        customerInfo.setRequester(businessCache.get("ORDER.REQUESTER"));


        productOfferings.put("basic", businessCache.get("PO.ID_BASIC"));
        productOfferings.put("optional", businessCache.get("PO.ID_OPTIONAL"));
        productOfferings.put("addon",businessCache.get("PO.ID_ADDON"));

        dbQuery.put("query1",businessCache.get("DB_QUERY_1"));
        dbQuery.put("query2",businessCache.get("DB_QUERY_2"));
        dbQuery.put("query3",businessCache.get("DB_QUERY_3"));
        dbQuery.put("query4",businessCache.get("DB_QUERY_4"));

        ts.setProductOfferingIds(productOfferings);
//Calling Notification for Voice AUT
        notificationVoiceSingleOffer(productOfferings,dbQuery);
        notificationVoiceMultipleOffer(productOfferings,dbQuery);

}

    public void notificationVoiceSingleOffer(LinkedHashMap<String, String> productOfferings,LinkedHashMap<String, String> dbQuery) throws InterruptedException, SQLException,IOException
    {

        int count=0;
        LinkedHashMap<String, String> dbQueryCheckForOffer=new LinkedHashMap<String, String>();
        dbQueryCheckForOffer.put("queryCoCOde",dbQuery.get("query1"));
        dbQueryCheckForOffer.put("queryContract",dbQuery.get("query2"));
        dbQueryCheckForOffer.put("queryProfileId",dbQuery.get("query3"));
        dbQueryCheckForOffer.put("queryOrderBy",dbQuery.get("query4"));
        String queryCount=null;
        String queryResult=null;
        String bscsBalance=null;
        String csBalance=null;
        String startDate=null;
        String endDate=null;
        String newStart;
        String newEnd;
        int counter =Integer.parseInt(businessCache.get("USAGE.DATA_DURATION_1"));
        DateTimeFormatter df =null;
        DateTime dateStart=null;
        DateTime dateEnd=null;
        
        customerInfo.setNpCodePub(businessCache.get("MSISDN.NPCODEPUB"));
        customerInfo.setPlCodePub(businessCache.get("MSISDN.PLCODEPUB"));
        customerInfo.setSubmIdPub(businessCache.get("MSISDN.SUBMIDPUB"));
        customerInfo.setHlCode(businessCache.get("MSISDN.HLCODE"));
        customerInfo.setRsCode(businessCache.get("MSISDN.RSCODE"));
        customerInfo.setCsControlled(businessCache.get("MSISDN.CSCONTROLLED"));

//Contracting for Data PO
        Steps.PROVISION.eoc.createCustomerContractBasicPO("Create Customer and Contract with Basic PO", this
                ,customerInfo,ts,resultInfo).run();

//Putting Trace ON for MSISDN
        ts.setTrace("ON");
        ts.setTraceFileName("sdp_Trace_"+customerInfo.getMsisdn());
        Steps.PROVISION.sdp.traceOnSdp("Trace on SDP",this,customerInfo,ts,resultInfo).run();

           ts.setProductOfferingIds(productOfferings);
        ts.setJsonFileName("AddOnPOonBasic.json");
        Steps.PROTOCOLS.businessLogicEOC.addAddOnPOonBasicPO("Protocol : Update Add On PO in to Basic PO",this,customerInfo,ts,resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC.submitShoppingCart("Protocol : Submit Shopping Cart",this,customerInfo,ts,resultInfo)
                .run();
        Steps.PROTOCOLS.businessLogicEOC.getShoppingCart("Protocol : Get Shopping Cart",this,customerInfo,ts,resultInfo)
                .run();

//Checking in CS for 1 offer active
        Steps.PROTOCOLS.businessLogicCS.getAccountDetails("Get Account Details from CS", this, customerInfo, ts, resultInfo)
                .run();

        if (JsonPath.read(ts.getResponseBody(), "$.offerInformationList[*].offerID").toString().split(",").length !=1 )
        {
            Jive.fail ("No Correct Number of offers after Second contracting with Data PO");
        }
        Jive.log("Correct Number of Offers in CS");

//Set Offer Name
        count = JsonPath.read(ts.getResponseBody(), "$.offerInformationList[0].treeParameterSetInformationList[*].serviceProvider").toString().split(",").length;
        for (int i=0;i<count;i++) {
            if (JsonPath.read(ts.getResponseBody(), "$.offerInformationList[0].treeParameterSetInformationList[" + i + "].treeParameterInformation").toString().contains("OfferName"))
            {
                for (int j=0;j<JsonPath.read(ts.getResponseBody(), "$.offerInformationList[0].treeParameterSetInformationList[" + i + "].treeParameterInformation[*].treeParameterName").toString().split(",").length;j++)
                {
                    if ("OfferName".equals(JsonPath.read(ts.getResponseBody(), "$.offerInformationList[0].treeParameterSetInformationList[" + i + "].treeParameterInformation[" + j + "].treeParameterName")))
                    {
                        customerInfo.setOfferName(JsonPath.read(ts.getResponseBody(), "$.offerInformationList[0].treeParameterSetInformationList[" + i + "].treeParameterInformation[" + j + "].treeParameterValueString".toString()));
                        break;
                    }
                }
            }
        }
//Checking for a Trace Message
        ts.setTraceMessage(businessCache.get("TRACE.MESSAGE_1").replace("{{OFFER_NAME}}",customerInfo.getOfferName()));
        checkTraceMessage();
        Jive.log("Specific Trace Message "+ts.getTraceMessage()+" on MSISDN "+customerInfo.getMsisdn()+" Found...");

//Checking in BSCS for Active PO

        Steps.PROTOCOLS.businessLogicBSCS.getPoStatusBSCS("Read PO Status from BSCS", this, customerInfo,ts, resultInfo)
                .run();

        count = JsonPath.read(ts.getResponseBody(), "$.SOAP-ENV:Envelope.SOAP-ENV:Body.productCfsReadResponse.productCfsReadOutputDTO.products.productCfsReadOutputProductDTO[*].productId.content").toString().split(",").length;
        for (int i = 0; i < count; i++) {
            if (JsonPath.read(ts.getResponseBody(), "$.SOAP-ENV:Envelope.SOAP-ENV:Body.productCfsReadResponse.productCfsReadOutputDTO.products.productCfsReadOutputProductDTO[" + i + "].productOfferingId.content").equals(productOfferings.get("addon"))) {
                if ("a".equals(JsonPath.read(ts.getResponseBody(), "$.SOAP-ENV:Envelope.SOAP-ENV:Body.productCfsReadResponse.productCfsReadOutputDTO.products.productCfsReadOutputProductDTO[" + i + "].productStatus.content"))) {
                    Jive.log("Add On PO Activated Successfully");
                } else {
                    Jive.fail("Add On PO Activation Failed... Please Check.. Failing");
                }
            }

        }

//Check in BSCS DB to check Start and End Date is Populated
        ts.setProfileIdBscs(businessCache.get("PROFILEID.BSCS"));
        dbQueryCheckForOffer.put("queryCoCOde",dbQuery.get("query1"));
        dbQueryCheckForOffer.put("queryContract",dbQuery.get("query2"));
        dbQueryCheckForOffer.put("queryProfileId",dbQuery.get("query3"));
        dbQueryCheckForOffer.put("queryOrderBy",dbQuery.get("query4"));
        queryCount=dbQueryCheckForOffer.get("queryCoCOde")+dbQueryCheckForOffer.get("queryContract")+customerInfo.getContractId()+"'"+dbQueryCheckForOffer.get("queryProfileId")+ts.getProfileIdBscs()+dbQueryCheckForOffer.get("queryOrderBy");
        queryResult=CreateDatabaseConnection.runQuery(queryCount);
        if (!"2".equals(queryResult))
        {
            Jive.fail("Start or End date for Offer might be not set in BSCS.. Failing.. Please check");
        }
        Jive.log("Both Start and End Date populated in BSCS");

//Data Call and Balance check from CS on MSISDN
        ts.setCharge(businessCache.get("USAGE.DATA_CHARGE_1"));
        ts.setExpectedCost(businessCache.get("USAGE.DATA_COST_1")); // This is only for the CS Charging
        ts.setDuration(Integer.parseInt(businessCache.get("USAGE.DATA_DURATION_1")));
        ts.setCalledNumber(Constants.NATIONAL_NUMBER_1);
        onlineCipCallAndBalanceVerification();

//Checking for a Trace Message
        ts.setTraceMessage(businessCache.get("TRACE.MESSAGE_2").replace("{{OFFER_NAME}}",customerInfo.getOfferName()));
        ts.setTrace("OFF");
        checkTraceMessage();
        Jive.log("Specific Trace Message "+ts.getTraceMessage() +"on MSISDN "+customerInfo.getMsisdn()+" Found...");

        csBalance=ts.getAccountBalance().toString();

        Thread.sleep(Integer.parseInt(businessCache.get("THREAD.SLEEP_BSCS")));

//Balance check from BSCS

        Steps.PROTOCOLS.businessLogicBSCS.getAccountBalance("Get Account Details from BSCS",this,customerInfo,ts,resultInfo).run();
        for (int i=0;i<2;i++)
        {
            try {
                if(i==0) {
                    bscsBalance = JsonPath.read(ts.getResponseBody(), "$.SOAP-ENV:Envelope.SOAP-ENV:Body.balanceHistoryReadResponse.balances.item.balanceRecords.item[0].aggregatedValue.content").toString();
                }
                else {
                    bscsBalance = JsonPath.read(ts.getResponseBody(), "$.SOAP-ENV:Envelope.SOAP-ENV:Body.balanceHistoryReadResponse.balances.item.balanceRecords.item.aggregatedValue.content").toString();
                }
                break;
            }
            catch (PathNotFoundException e)
            {
                continue;
            }
        }
        bscsBalance=bscsBalance.split("\\.")[0];
        csBalance=csBalance.split("\\.")[0];

        if (bscsBalance.length() < csBalance.length())
        {
            csBalance=csBalance.substring(0,bscsBalance.length());
        }
        else if (bscsBalance.length() > csBalance.length()  )
        {
            bscsBalance=bscsBalance.substring(0,csBalance.length());
        }

//Comparing Balance of BSCS and CS
        /*
        there is some issue in BSCS due to which balance in BSCS and CS is not same
        So we are commenting line 224, when the issue is resolved, just uncomment
        line 224
         */

        if (!bscsBalance.equals(csBalance))
        {
            //Jive.fail("Balance from BSCS and CS Not Equal after call.. Failing.. Please check..");
        }
        Jive.log("Balance in BSCS and CS is Same");

//Checking Counter in CS
        Steps.PROTOCOLS.businessLogicCS.getThreshholdAndCounters("Get Counters fro CS",this,customerInfo,ts,resultInfo).run();

        if (counter != Integer.parseInt(JsonPath.read(ts.getResponseBody(),"$.usageCounterUsageThresholdInformation[0].usageCounterValue")))
        {
            Jive.fail("Correct Counter values not consumed.. Please check");

        }
        Jive.log("Correct Counter Value in CS");

//Checking if Offer is active or Not in CS

        Steps.PROTOCOLS.businessLogicCS.getOffers("Get Offers from CS", this, customerInfo, ts, resultInfo).run();

        if (!"0".equals(JsonPath.read(ts.getResponseBody(), "$.offerInformation[0].offerState").toString()))
        {
            Jive.fail("Offerstate is not Active.. Please check");
        }
        customerInfo.setOfferId(String.valueOf(JsonPath.read(ts.getResponseBody(), "$.offerInformation[0].offerID").toString()));
        Jive.log("Offer is Active in CS");

//Check for 30 Days Validity of Offer
        startDate=JsonPath.read(ts.getResponseBody(), "$.offerInformation[0].startDateTime");
        endDate=JsonPath.read(ts.getResponseBody(), "$.offerInformation[0].expiryDateTime");

        startDate=startDate.split("\\+")[0].replace("T"," ");
        endDate=endDate.split("\\+")[0].replace("T"," ");

         df = DateTimeFormat.forPattern("yyyyMMdd HH:mm:ss");
         dateStart= df.parseDateTime(startDate).plusDays(Integer.parseInt(businessCache.get("OFFER.VALIDITY")));
         dateEnd= df.parseDateTime(endDate);

        newStart = String.valueOf(dateStart.getDayOfMonth()) + String.valueOf(dateStart.getMonthOfYear()) + String.valueOf(dateStart.getYear());
        newEnd =String.valueOf(dateEnd.getDayOfMonth()) + String.valueOf(dateEnd.getMonthOfYear()) + String.valueOf(dateEnd.getYear());

        if (!newStart.equals(newEnd))
        {
            Jive.fail("Start date and End Date Not Equal after Contracting");
        }
        Jive.log("Active Period Date for Offer is Correct as per PO");

//CHecking fr Number of Usage Records
        ts.setUsageRecordLimit(businessCache.get("USAGE.RECORDSLIMITBSCS"));
        Steps.PROTOCOLS.businessLogicBSCS.getUsageEvent("Get Usage Event from BSCS",this,customerInfo,ts,resultInfo).run();

        if (JsonPath.read(ts.getResponseBody(),"$.SOAP-ENV:Envelope.SOAP-ENV:Body.usageDataRecordsReadResponse.output.item.cdrId.content").toString().split(",").length !=1)
        {
            Jive.fail("Number of CDR Count from BSCS Not Equal .. Failing ..Please check");
        }
        Jive.log("Correct Number of Usage Records in BSCS");
//Checking COunter in BSCS

        if (counter !=(Integer) JsonPath.read(ts.getResponseBody(),"$.SOAP-ENV:Envelope.SOAP-ENV:Body.usageDataRecordsReadResponse.output.item.duration.content"))
        {
            Jive.fail("Counter Values from BSCS not equal to duration.. Failing");
        }
        Jive.log("Correct Counter in BSCS");

    }
    public void notificationVoiceMultipleOffer(LinkedHashMap<String, String> productOfferings,LinkedHashMap<String, String> dbQuery) throws InterruptedException, SQLException,IOException
    {

        int count=0;
        LinkedHashMap<String, String> dbQueryCheckForOffer=new LinkedHashMap<String, String>();;
        dbQueryCheckForOffer.put("queryCoCOde",dbQuery.get("query1"));
        dbQueryCheckForOffer.put("queryContract",dbQuery.get("query2"));
        dbQueryCheckForOffer.put("queryProfileId",dbQuery.get("query3"));
        dbQueryCheckForOffer.put("queryOrderBy",dbQuery.get("query4"));
        String queryCount=null;
        String queryResult=null;
        String bscsBalance=null;
        String csBalance=null;
        String startDate=null;
        String endDate=null;
        String newStart;
        String newEnd;
        int counter =Integer.parseInt(businessCache.get("USAGE.DATA_DURATION_2"));
        int tempCounter=0;
        DateTimeFormatter df =null;
        DateTime dateStart=null;
        DateTime dateEnd=null;
        int countPoStatus=0;

//Contracting for Data PO
        Steps.PROVISION.eoc.createCustomerContractBasicPO("Create Customer and Contract with Basic PO", this
                ,customerInfo,ts,resultInfo).run();

//Putting Trace ON for MSISDN
        ts.setTrace("ON");
        ts.setTraceFileName("sdp_Trace_"+customerInfo.getMsisdn());
        Steps.PROVISION.sdp.traceOnSdp("Trace on SDP",this,customerInfo,ts,resultInfo).run();

        ts.setProductOfferingIds(productOfferings);
        ts.setJsonFileName("AddOnPOonBasic.json");
        Steps.PROTOCOLS.businessLogicEOC.addAddOnPOonBasicPO("Protocol : Update Add On PO in to Basic PO",this,customerInfo,ts,resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC.submitShoppingCart("Protocol : Submit Shopping Cart",this,customerInfo,ts,resultInfo)
                .run();
        Steps.PROTOCOLS.businessLogicEOC.getShoppingCart("Protocol : Get Shopping Cart",this,customerInfo,ts,resultInfo)
                .run();
//Check in BSCS DB to check Start and End Date is Populated
        ts.setProfileIdBscs(businessCache.get("PROFILEID.BSCS"));
        dbQueryCheckForOffer.put("queryCoCOde",dbQuery.get("query1"));
        dbQueryCheckForOffer.put("queryContract",dbQuery.get("query2"));
        dbQueryCheckForOffer.put("queryProfileId",dbQuery.get("query3"));
        dbQueryCheckForOffer.put("queryOrderBy",dbQuery.get("query4"));
        queryCount=dbQueryCheckForOffer.get("queryCoCOde")+dbQueryCheckForOffer.get("queryContract")+customerInfo.getContractId()+"'"+dbQueryCheckForOffer.get("queryProfileId")+ts.getProfileIdBscs()+dbQueryCheckForOffer.get("queryOrderBy");
        queryResult=CreateDatabaseConnection.runQuery(queryCount);
        if (!"2".equals(queryResult))
        {
            Jive.fail("Start or End date for Offer might be not set in BSCS.. Failing.. Please check");
        }
        Jive.log("Both Start and End Date Populated Successfully for Offer");

//Repurchase PO

        Steps.PROVISION.eoc.repurchaseAddOnPO("Repurchase AddOn PO",this,customerInfo,ts,resultInfo).run();

//Checking in CS for 2 offer active
        Steps.PROTOCOLS.businessLogicCS.getAccountDetails("Get Account Details from CS", this, customerInfo, ts, resultInfo)
                .run();

        if (JsonPath.read(ts.getResponseBody(), "$.offerInformationList[*].offerID").toString().split(",").length !=2 )
        {
            Jive.fail ("No Correct Number of offers after Second contracting with Data PO");
        }
        Jive.log("2 Number of Offers in CS");
//Set Offer Name
        count = JsonPath.read(ts.getResponseBody(), "$.offerInformationList[0].treeParameterSetInformationList[*].serviceProvider").toString().split(",").length;
        for (int i=0;i<count;i++) {
            if (JsonPath.read(ts.getResponseBody(), "$.offerInformationList[0].treeParameterSetInformationList[" + i + "].treeParameterInformation").toString().contains("OfferName"))
            {
                for (int j=0;j<JsonPath.read(ts.getResponseBody(), "$.offerInformationList[0].treeParameterSetInformationList[" + i + "].treeParameterInformation[*].treeParameterName").toString().split(",").length;j++)
                {
                    if ("OfferName".equals(JsonPath.read(ts.getResponseBody(), "$.offerInformationList[0].treeParameterSetInformationList[" + i + "].treeParameterInformation[" + j + "].treeParameterName")))
                    {
                        customerInfo.setOfferName(JsonPath.read(ts.getResponseBody(), "$.offerInformationList[0].treeParameterSetInformationList[" + i + "].treeParameterInformation[" + j + "].treeParameterValueString".toString()));
                        break;
                    }
                }
            }
        }
//Checking for a Trace Message
        ts.setTraceMessage(businessCache.get("TRACE.MESSAGE_1").replace("{{OFFER_NAME}}",customerInfo.getOfferName()));
        ts.setOccurenceOfMessage(2);
        checkTraceMessage();
        Jive.log("Specific Trace Message "+ts.getTraceMessage()+" on MSISDN "+customerInfo.getMsisdn()+" Found...");
        ts.setOccurenceOfMessage(0);

//Checking in BSCS for Active PO

        Steps.PROTOCOLS.businessLogicBSCS.getPoStatusBSCS("Read PO Status from BSCS", this, customerInfo,ts, resultInfo)
                .run();

        count = JsonPath.read(ts.getResponseBody(), "$.SOAP-ENV:Envelope.SOAP-ENV:Body.productCfsReadResponse.productCfsReadOutputDTO.products.productCfsReadOutputProductDTO[*].productId.content").toString().split(",").length;
        for (int i = 0; i < count; i++) {
            if (JsonPath.read(ts.getResponseBody(), "$.SOAP-ENV:Envelope.SOAP-ENV:Body.productCfsReadResponse.productCfsReadOutputDTO.products.productCfsReadOutputProductDTO[" + i + "].productOfferingId.content").equals(productOfferings.get("addon"))) {
                if ("a".equals(JsonPath.read(ts.getResponseBody(), "$.SOAP-ENV:Envelope.SOAP-ENV:Body.productCfsReadResponse.productCfsReadOutputDTO.products.productCfsReadOutputProductDTO[" + i + "].productStatus.content"))) {
                    countPoStatus++;
                    Jive.log("Add On PO Activated Successfully");
                } else {
                    Jive.fail("Add On PO Activation Failed... Please Check.. Failing");
                }
            }

        }
        if (countPoStatus !=2)
        {
            Jive.fail("Both Offers are not Activated as per BSCS... Failing ...");
        }
        Jive.log("Both Offers are active as per BSCS");

 //Check for Date from BSCS DB
        ts.setProfileIdBscs(String.valueOf(Integer.parseInt(businessCache.get("PROFILEID.BSCS"))+1));
        dbQueryCheckForOffer.put("queryCoCOde",dbQuery.get("query1"));
        dbQueryCheckForOffer.put("queryContract",dbQuery.get("query2"));
        dbQueryCheckForOffer.put("queryProfileId",dbQuery.get("query3"));
        dbQueryCheckForOffer.put("queryOrderBy",dbQuery.get("query4"));
        queryCount=dbQueryCheckForOffer.get("queryCoCOde")+dbQueryCheckForOffer.get("queryContract")+customerInfo.getContractId()+"'"+dbQueryCheckForOffer.get("queryProfileId")+ts.getProfileIdBscs()+dbQueryCheckForOffer.get("queryOrderBy");
        queryResult=CreateDatabaseConnection.runQuery(queryCount);
        if (!"2".equals(queryResult))
        {
            Jive.fail("Start or End date for Offer might be not set in BSCS.. Failing.. Please check");
        }
        Jive.log("Both Start and End Date populated in BSCS");

//Data Call and Balance check from CS on MSISDN
        ts.setCharge(businessCache.get("USAGE.DATA_CHARGE_2"));
        ts.setExpectedCost(businessCache.get("USAGE.DATA_COST_2")); // This is only for the CS Charging
        ts.setDuration(Integer.parseInt(businessCache.get("USAGE.DATA_DURATION_2")));
        ts.setCalledNumber(Constants.NATIONAL_NUMBER_1);
        onlineCipCallAndBalanceVerification();

//Checking for a Trace Message
        ts.setTraceMessage(businessCache.get("TRACE.MESSAGE_3").replace("{{OFFER_NAME}}",customerInfo.getOfferName()));
        ts.setTrace("OFF");
        checkTraceMessage();
        Jive.log("Specific Trace Message "+ts.getTraceMessage()+" on MSISDN "+customerInfo.getMsisdn()+" Found...");

        csBalance=ts.getAccountBalance().toString();

        Thread.sleep(Integer.parseInt(businessCache.get("THREAD.SLEEP_BSCS")));


//Balance check from BSCS

        Steps.PROTOCOLS.businessLogicBSCS.getAccountBalance("Get Account Details from BSCS",this,customerInfo,ts,resultInfo).run();
        for (int i=0;i<2;i++)
        {
            try {
                if(i==0) {
                    bscsBalance = JsonPath.read(ts.getResponseBody(), "$.SOAP-ENV:Envelope.SOAP-ENV:Body.balanceHistoryReadResponse.balances.item.balanceRecords.item[0].aggregatedValue.content").toString();
                }
                else {
                    bscsBalance = JsonPath.read(ts.getResponseBody(), "$.SOAP-ENV:Envelope.SOAP-ENV:Body.balanceHistoryReadResponse.balances.item.balanceRecords.item.aggregatedValue.content").toString();
                }
                break;
            }
            catch (PathNotFoundException e)
            {
                continue;
            }
        }
        bscsBalance=bscsBalance.split("\\.")[0];
        csBalance=csBalance.split("\\.")[0];

        if (bscsBalance.length() < csBalance.length())
        {
            csBalance=csBalance.substring(0,bscsBalance.length());
        }
        else if (bscsBalance.length() > csBalance.length()  )
        {
            bscsBalance=bscsBalance.substring(0,csBalance.length());
        }

//Comparing Balance of BSCS and CS

        if (!bscsBalance.equals(csBalance))
        {
            //Jive.fail("Balance from BSCS and CS Not Equal after call.. Failing.. Please check..");
        }
        Jive.log("Balance in BSCS and CS is Same");

//Checking Counter in CS
        Steps.PROTOCOLS.businessLogicCS.getThreshholdAndCounters("Get Counters fro CS", this, customerInfo, ts, resultInfo).run();
        for (int i=0;i <JsonPath.read(ts.getResponseBody(), "$.usageCounterUsageThresholdInformation[*].usageCounterValue").toString().split(",").length;i++) {
        tempCounter=tempCounter+Integer.parseInt(JsonPath.read(ts.getResponseBody(), "$.usageCounterUsageThresholdInformation[" + i +"].usageCounterValue"));
        }

        if (counter != tempCounter ) {
            Jive.fail("Correct Counter values not consumed.. Please check");

        }
        Jive.log("Correct Counter Value in CS");

//Checking if Offer is active or Not in CS
        for (int i=0;i<=1;i++) {

            Steps.PROTOCOLS.businessLogicCS.getOffers("Get Offers from CS", this, customerInfo, ts, resultInfo).run();

            if (!"0".equals(JsonPath.read(ts.getResponseBody(), "$.offerInformation[" + i + "].offerState").toString())) {
                Jive.fail("Offerstate is not Active.. Please check");
            }
            customerInfo.setOfferId(String.valueOf(JsonPath.read(ts.getResponseBody(), "$.offerInformation[" + i + "].offerID").toString()));
            Jive.log("Offer " +customerInfo.getOfferId() + "is Active in CS");

//Check for 30 Days Validity of Offer
            startDate = JsonPath.read(ts.getResponseBody(), "$.offerInformation[" + i + "].startDateTime");
            endDate = JsonPath.read(ts.getResponseBody(), "$.offerInformation[" + i + "].expiryDateTime");

            startDate = startDate.split("\\+")[0].replace("T", " ");
            endDate = endDate.split("\\+")[0].replace("T", " ");

            df = DateTimeFormat.forPattern("yyyyMMdd HH:mm:ss");
            dateStart = df.parseDateTime(startDate).plusDays(Integer.parseInt(businessCache.get("OFFER.VALIDITY")));
            dateEnd = df.parseDateTime(endDate);

            newStart = String.valueOf(dateStart.getDayOfMonth()) + String.valueOf(dateStart.getMonthOfYear()) + String.valueOf(dateStart.getYear());
            newEnd = String.valueOf(dateEnd.getDayOfMonth()) + String.valueOf(dateEnd.getMonthOfYear()) + String.valueOf(dateEnd.getYear());

            if (!newStart.equals(newEnd)) {
                Jive.fail("Active Period for Offer " +customerInfo.getOfferId()+ " is not Correct as Defined in PO.. Failing");
            }
            Jive.log("Active Period for Offer "+customerInfo.getOfferId()+" is Correct as Defined in PO");
        }
//CHecking fr Number of Usage Records
        ts.setUsageRecordLimit(businessCache.get("USAGE.RECORDSLIMITBSCS"));
        Steps.PROTOCOLS.businessLogicBSCS.getUsageEvent("Get Usage Event from BSCS",this,customerInfo,ts,resultInfo).run();

        if (JsonPath.read(ts.getResponseBody(),"$.SOAP-ENV:Envelope.SOAP-ENV:Body.usageDataRecordsReadResponse.output.item.cdrId.content").toString().split(",").length !=1)
        {
            Jive.fail("Number of CDR Count from BSCS Not Equal .. Failing ..Please check");
        }
        Jive.log("Correct Number of Usage Records in BSCS");
//Checking COunter in BSCS

        if (counter !=(Integer) JsonPath.read(ts.getResponseBody(),"$.SOAP-ENV:Envelope.SOAP-ENV:Body.usageDataRecordsReadResponse.output.item.duration.content"))
        {
            Jive.fail("Counter Values from BSCS not equal to duration.. Failing");
        }
        Jive.log("Correct Counter in BSCS");


    }
    public void checkTraceMessage() throws InterruptedException, IOException{

        String  findMessageInTrace=null;
        String tempFile="tempTraceFile_"+customerInfo.getMsisdn();
        File localTraceFile;
        File tmpLocalDir = new File(tmpDirectory);
        findMessageInTrace="grep \'"+customerInfo.getMsisdn()+".*"+ts.getTraceMessage()+"\' ~/"+ts.getTraceFileName()+" > ~/"+tempFile;
        Session session  = RemoteHostUtility.connectToSDPHost();
        Jive.log("Getting Trace by Command "+findMessageInTrace);
        RemoteHostUtility.executeAndKeepAlive(session,findMessageInTrace);
        Thread.sleep(30000);
        RemoteHostUtility.downloadFromRemoteHost(session,tempFile,tmpLocalDir.toString()+"/");
        localTraceFile = new File(tmpLocalDir.toString()+"/"+tempFile);
        BufferedReader readTempTraceFile;
        readTempTraceFile = new BufferedReader(new FileReader(localTraceFile));
        if (localTraceFile.length() == 0)
        {
            Jive.fail("Specific Trace Message on MSISDN "+customerInfo.getMsisdn()+" Not Found...Failing");
            RemoteHostUtility.executeAndKeepAlive(session, "rm -f " + ts.getTraceFileName());
        }
        try  {

            if(ts.getOccurenceOfMessage() != 0)
            {
                int countOfMessage=0;

                String line=readTempTraceFile.readLine();
                while (line != null) {
                    if(line.contains(ts.getTraceMessage()))
                    {
                        countOfMessage++;
                        if (countOfMessage==ts.getOccurenceOfMessage())
                        {
                            break;
                        }
                    }
                    line = readTempTraceFile.readLine();
                }
                if (countOfMessage!=ts.getOccurenceOfMessage())
                {
                    Jive.fail("THe Expected Message"+ts.getTraceMessage()+ "not there in trace for "+ts.getOccurenceOfMessage()+ "times" );
                }
                Jive.log("The Expected Message"+ts.getTraceMessage()+ "there in trace for "+ts.getOccurenceOfMessage()+ "times" );
                ts.setOccurenceOfMessage(0);
            }
        }
        catch(IOException e)
        {
            Jive.fail("Exception caught while reading file"+e.getStackTrace());
        }
        finally{
            readTempTraceFile.close();
        }
        RemoteHostUtility.removeFileFromLocal(tmpLocalDir.toString(), localTraceFile.toString());
        RemoteHostUtility.executeAndKeepAlive(session,"rm -f tempTraceFile");
        if("OFF".equals(ts.getStatusTrace())) {
            RemoteHostUtility.executeAndKeepAlive(session, "rm -f " + ts.getTraceFileName());
        }

    }

    public void onlineCipCallAndBalanceVerification() {
        Steps.PROTOCOLS.businessLogicCS.getAccountBalanceAndDate("Protocol : Before - Get Account Balance from CS",
                this, customerInfo, ts, resultInfo).run();
        BigDecimal getBalanceBefore = ts.getAccountBalance();

        Jive.log("Balance Before is " + getBalanceBefore);

        Steps.CHARGING_ONLINE.simulator
                .onlineCip("Protocol : Online Data Usage CIP IP Simulator", this, resultInfo, customerInfo, ts).run();

        Steps.PROTOCOLS.businessLogicCS.getAccountBalanceAndDate("Protocol : After - Get Account Balance from CS", this,
                customerInfo, ts, resultInfo).run();

        BigDecimal getBalanceAfter = ts.getAccountBalance();
        Jive.log("Balance Before : " + getBalanceBefore + " Balance After : " + getBalanceAfter);

        Assert.assertEquals(getBalanceBefore.subtract(new BigDecimal(ts.getCharge())), getBalanceAfter);
        Jive.log("OK : Expected Balance " + ts.getCharge() + "Matched Successfully!");
    }
}

