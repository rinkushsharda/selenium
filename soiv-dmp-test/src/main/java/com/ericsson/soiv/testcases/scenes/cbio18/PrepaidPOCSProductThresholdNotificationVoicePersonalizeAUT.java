package com.ericsson.soiv.testcases.scenes.cbio18;

import com.ericsson.jive.core.execution.*;
import com.ericsson.soiv.fixtures.SoivFixture;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.*;
import com.jayway.jsonpath.JsonPath;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import com.jcraft.jsch.Session;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.LinkedHashMap;

import static com.ericsson.soiv.utils.JsonHelper.*;
import static com.ericsson.soiv.utils.http.ReadBusinessData.readBusinessCacheData;
import static java.io.File.separator;

// ZKAPSAR Created on 15th-June-2019

@Fixture (SoivFixture.class)
public class PrepaidPOCSProductThresholdNotificationVoicePersonalizeAUT extends SoivTestBase {
    private TestResultInfo resultInfo = new TestResultInfo();
    private CustomerInfo customerInfo = new CustomerInfo();
    private TransactionSpecification ts = new TransactionSpecification();
    private LinkedHashMap<String, String> productOfferings = new LinkedHashMap<String, String>();
    private LinkedHashMap<String, String> dbQuery = new LinkedHashMap<String, String>();
    private LinkedHashMap<String, String> businessCache = null;
    private String tmpDirectory = "./tmp"+separator;

    @Test
    @Id("00000044")
    @Tags("OT-Regression, OT-Prepaid, EC-Controlled, OT-CBiO18")
    @Description("PO CS Product Threshold Notification For Data RUT")
    public void prepaidPoCsProductThresholdNotificationvoicePersonalizeAut()  throws IOException,SQLException,InterruptedException{

        businessCache = new LinkedHashMap<>(readBusinessCacheData(this.getTestCase().getId()));

        customerInfo.setCustomerBillCycle(businessCache.get("CUSTOMER.BILLCYCLE"));
        customerInfo.setOrderMode(businessCache.get("ORDER.MODE"));
        customerInfo.setRequester(businessCache.get("ORDER.REQUESTER"));

        productOfferings.put("basic", businessCache.get("PO.ID_BASIC"));
        productOfferings.put("optional", businessCache.get("PO.ID_OPTIONAL"));
        dbQuery.put("query1", businessCache.get("DB_QUERY_1"));
        dbQuery.put("query2", businessCache.get("DB_QUERY_2"));
        dbQuery.put("query3", businessCache.get("DB_QUERY_3"));
        dbQuery.put("query4", businessCache.get("DB_QUERY_4"));
        notificationPersonalizeVoice(productOfferings, dbQuery);
    }

    public void notificationPersonalizeVoice(LinkedHashMap<String, String> productOfferings, LinkedHashMap<String, String> dbQuery) throws InterruptedException,IOException{

        int count = 0;
        String startDate = null;
        String endDate = null;
        String newStart;
        String newEnd;
        int counter = Integer.parseInt(businessCache.get("USAGE.DATA_DURATION"));
        DateTimeFormatter df = null;
        DateTime dateStart = null;
        DateTime dateEnd = null;
        ts.setProductOfferingIds(productOfferings);

//Contracting for Data PO
        customerInfo.setNpCodePub(businessCache.get("MSISDN.NPCODEPUB"));
        customerInfo.setPlCodePub(businessCache.get("MSISDN.PLCODEPUB"));
        customerInfo.setSubmIdPub(businessCache.get("MSISDN.SUBMIDPUB"));
        customerInfo.setHlCode(businessCache.get("MSISDN.HLCODE"));
        customerInfo.setRsCode(businessCache.get("MSISDN.RSCODE"));
        customerInfo.setCsControlled(businessCache.get("MSISDN.CSCONTROLLED"));
        Steps.PROVISION.eoc.createCustomerContractBasicPO("Create Customer and Contract with Basic PO", this
                , customerInfo, ts, resultInfo).run();

//Putting Trace ON for MSISDN
        ts.setTrace("ON");
        ts.setTraceFileName("sdp_Trace_"+customerInfo.getMsisdn());
        Steps.PROVISION.sdp.traceOnSdp("Trace on SDP",this,customerInfo,ts,resultInfo).run();

        ts.setJsonFileName("AddOptionalPOonBasic.json");
        Steps.PROTOCOLS.businessLogicEOC.addOptionalPOonBasicPO("Provision : Add Optional Bundled PO in to Basic PO",
                this, customerInfo, ts, resultInfo).run();
        Steps.PROTOCOLS.businessLogicEOC.submitShoppingCart("Protocol : Submit Shopping Cart", this, customerInfo, ts, resultInfo)
                .run();
        Steps.PROTOCOLS.businessLogicEOC.getShoppingCart("Protocol : Get Shopping Cart", this, customerInfo, ts, resultInfo)
                .run();

//Checking in CS for 1 offer active
        Steps.PROTOCOLS.businessLogicCS.getAccountDetails("Get Account Details from CS", this, customerInfo, ts, resultInfo)
                .run();

        if (JsonPath.read(ts.getResponseBody(), "$.offerInformationList[*].offerID").toString().split(",").length != 1) {
            Jive.fail("No Correct Number of offers after Second contracting with Data PO");
        }
        Jive.log("1 Offer Activated Successfully");

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

        Steps.PROTOCOLS.businessLogicBSCS.getPoStatusBSCS("Read PO Status from BSCS", this, customerInfo, ts, resultInfo)
                .run();

        count = JsonPath.read(ts.getResponseBody(), "$.SOAP-ENV:Envelope.SOAP-ENV:Body.productCfsReadResponse.productCfsReadOutputDTO.products.productCfsReadOutputProductDTO[*].productId.content").toString().split(",").length;
        for (int i = 0; i < count; i++) {
            if (JsonPath.read(ts.getResponseBody(), "$.SOAP-ENV:Envelope.SOAP-ENV:Body.productCfsReadResponse.productCfsReadOutputDTO.products.productCfsReadOutputProductDTO[" + i + "].productOfferingId.content").equals(productOfferings.get("optional"))) {
                if ("a".equals(JsonPath.read(ts.getResponseBody(), "$.SOAP-ENV:Envelope.SOAP-ENV:Body.productCfsReadResponse.productCfsReadOutputDTO.products.productCfsReadOutputProductDTO[" + i + "].productStatus.content"))) {
                    Jive.log("Add On PO Activated Successfully");
                } else {
                    Jive.fail("Add On PO Activation Failed... Please Check.. Failing");
                }
            }

        }

        personalizeThreshold(customerInfo,ts,resultInfo);

//Data Call and Balance check from CS on MSISDN
        ts.setRatingGroup(businessCache.get("RATING.GROUP"));
        ts.setCharge(businessCache.get("USAGE.DATA_CHARGE"));
        ts.setExpectedCost(businessCache.get("USAGE.DATA_COST")); // This is only for the CS Charging
        ts.setDuration(Integer.parseInt(businessCache.get("USAGE.DATA_DURATION")));

        onlineCipCallAndBalanceVerification();

//Check for Trace Message after Voice Call
        ts.setTraceMessage(businessCache.get("TRACE.MESSAGE_2").replace("{{OFFER_NAME}}",customerInfo.getOfferName()));
        ts.setTrace("OFF");
        checkTraceMessage();
        Jive.log("Specific Trace Message "+ts.getTraceMessage()+" on MSISDN "+customerInfo.getMsisdn()+" Found...");
        Thread.sleep(Integer.parseInt(businessCache.get("THREAD.SLEEP_BSCS")));

//Checking COunter in CS
        Steps.PROTOCOLS.businessLogicCS.getThreshholdAndCounters("Get Counters from CS", this, customerInfo, ts, resultInfo).run();

        if (counter != Integer.parseInt(JsonPath.read(ts.getResponseBody(), "$.usageCounterUsageThresholdInformation[0].usageCounterValue"))) {
            Jive.fail("Correct Counter values not consumed.. Please check");

        }
        Jive.log("Counter Value Correctly Populated in CS");

//Checking if Offer is active or Not in CS

        Steps.PROTOCOLS.businessLogicCS.getOffers("Get Offers from CS", this, customerInfo, ts, resultInfo).run();

        if (!"0".equals(JsonPath.read(ts.getResponseBody(), "$.offerInformation[0].offerState").toString())) {
            Jive.fail("Offerstate is not Active.. Please check");
        }
        Jive.log("Offer State is Active in CS");

//Check of Period of Validity of Offer
        startDate=JsonPath.read(ts.getResponseBody(), "$.offerInformation[0].startDateTime");
        endDate=JsonPath.read(ts.getResponseBody(), "$.offerInformation[0].expiryDateTime");

        startDate=startDate.split("\\+")[0].replace("T"," ");
        endDate=endDate.split("\\+")[0].replace("T"," ");

        df = DateTimeFormat.forPattern("yyyyMMdd HH:mm:ss");
        dateStart= df.parseDateTime(startDate);
        dateEnd= df.parseDateTime(endDate);

        newStart = String.valueOf(dateStart.getDayOfMonth()) + String.valueOf(dateStart.getMonthOfYear()) + String.valueOf(dateStart.getYear());
        newEnd =String.valueOf(dateEnd.getDayOfMonth()) + String.valueOf(dateEnd.getMonthOfYear()) + String.valueOf(dateEnd.getYear());

        if ((newStart ==null) || (newEnd == null))
        {
            Jive.fail("Either Start or End Date is Null.. Failing");
        }
        Jive.log("Both Start and End date's are set");

//Checking fr Number of Usage Records
        ts.setUsageRecordLimit(businessCache.get("USAGE.RECORDSLIMITBSCS"));
        Steps.PROTOCOLS.businessLogicBSCS.getUsageEvent("Get Usage Event from BSCS", this, customerInfo, ts, resultInfo).run();

        if (JsonPath.read(ts.getResponseBody(), "$.SOAP-ENV:Envelope.SOAP-ENV:Body.usageDataRecordsReadResponse.output.item.cdrId.content").toString().split(",").length != 1) {
            Jive.fail("Number of CDR Count from BSCS Not Equal .. Failing ..Please check");
        }
        Jive.log("Correct Number of Usage Records in BSCS");

//Checking Counter Values in BSCS
        if (counter != (Integer)JsonPath.read(ts.getResponseBody(), "$.SOAP-ENV:Envelope.SOAP-ENV:Body.usageDataRecordsReadResponse.output.item.duration.content")) {
            Jive.fail("Correct Counter values not consumed from BSCS.. Please check");

        }
        Jive.log("Correct Counter Consumes as per BSCS");

//Check in BSCS if Correct Charges deducted or not
        if (ts.getExpectedCost().intValue() != (Integer)JsonPath.read(ts.getResponseBody(),"$.SOAP-ENV:Envelope.SOAP-ENV:Body.usageDataRecordsReadResponse.output.item.ratedFlatAmount.amount.content"))
        {
            Jive.fail("Not Correct Cost Deducted after Call");
        }
        Jive.log("Correct Charges Deducted as per BSCS");


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

    public void personalizeThreshold(CustomerInfo customerInfo,TransactionSpecification ts,TestResultInfo resultInfo)
    {
        ts.setJsonFileName("createShoppingCart.json");
        Steps.PROVISION.eoc
                .createShoppingCart("Provision : Create Shopping Cart For Personalized usage",this,customerInfo,ts,resultInfo).run();

    resultInfo =Steps.PROTOCOLS.businessLogicEOC
            .getShoppingCart("Protocol : Get Shopping Cart For Personalized usage",this,customerInfo,ts,resultInfo).run();

    String pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("optional")), "id");
        customerInfo.setPooiId(pooiId);

    resultInfo =Steps.PROTOCOLS.businessLogicEOC.getPooi("Getting the POOI For Personalized usage",this,customerInfo,ts,resultInfo).run();

    /* updating the values in POOI */

    JSONObject getPOOIRequestBody = searchKeyAndUpdateJson(ts.getResponseBody(), businessCache.get("PERSONALIZE.KEY_ACTION"),businessCache.get("PERSONALIZE.VALUE_MODIFY"));
    getPOOIRequestBody =requestForPersonalizedPrice(getPOOIRequestBody.toString(),businessCache.get("PERSONALIZE.ELEMENTKEY_1"), businessCache.get("PERSONALIZE.ELEMENTVALUE_1"));
        ts.setResponseBody(getPOOIRequestBody.toString());

    /* Update POOI Request with submitting the shopping cart */

        Steps.PROTOCOLS.businessLogicEOC
                .updatePooi("Updation of Price Values for Personalization",this,customerInfo,ts,resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision : Submit Shopping Cart For Personalized usage",this,customerInfo,ts,resultInfo).run();

}
}

