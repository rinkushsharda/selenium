package com.ericsson.soiv.utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.ericsson.soiv.testbases.SoivTestBase;

public class TransactionSpecification extends SoivTestBase {
    private String responseBody = null;
    private String requestedUri = null;
    private String expectedValidationMessage = "expectedValidationMessage";
    private String eocOrderId = null;
    private String basicPoReferenceId = null;
    private String optionaloReferenceId = null;
    private int duration = -1;
    private String charge = null;
    private String invoicePath = null;
    private String invoiceCharge = null;
    private Integer invoicePdfPageNo = null;
    private Boolean removeInvoicePdfFile = true;
    private String udrTextFileName = null;
    private BigDecimal expectedCost = null;
    private BigDecimal expectedBalance = null;
    private BigDecimal beforeBalance = null;
    private BigDecimal accountBalance = null;
    private BigDecimal dedicatedAccountBalance = null;
    private String calledNumber = null;
    private String answerTime = null;
    private String reasonCode = null;
    private String targetState = null;
    private String actionCode = null;
    private String eventCode = null;
    private String price = null;
    private Boolean adminChargesEnabled = null;
    private String locationAreaCode = null;
    private String cellId = null;
    private String mccMnc = null; // Mobile country code and mobile network code
    private String recordingEntity = null;
    private String fqdn = null; // Fully Qualified Host Name
    private String sessionId = null;
    private String callStartTime = null;
    private int updateNr = -1;
    private BigDecimal babAdjValue = null;
    private String ratingGroup = null;
    private LinkedHashMap<String,String> productOfferingIds = new LinkedHashMap<>();
    private List<String> requestedPOs = new ArrayList<String>();
    private String jsonFilePath = null;
    private String jsonFileName = null;
    private List<String> calledNumbers;
    private String usageCounterValueNew=null;
    private String dedicatedAccountID=null;
    private int dedicatedAccountValue=0;
    private String callMessage=null;
    private String billEndDate=null;
    private int sdpId=1;
    private LinkedHashMap<String,String> invoiceValidationCharge;
    private String serviceCharacteristicsName = null;
    private String serviceCharacteristicsValue = null;
    private int expectedStatusCode;
    private String resourceId = null;
    private String resourceSpecification = null;
    private String resourceParentId = null;
    private String resourceAction = null;
    private String actionValue = null;
    private String deviceId = null;
    private int resourceCardinality = 1;
    private String profileId=null;
    private String searchLimit=null;
    private String messageTrace=null;
    private String sdpTraceFileName=null;
    private String statusTrace=null;
    private int checkOccurenceOfMessage=0;
    private int expectedResourceStatusCode=201;
    private boolean checkForCardinality=false;
    private int serviceIdentifier;
   

    public TransactionSpecification() {}
    
    public TransactionSpecification setserviceIdentifier(int serviceIdentifier) {
        this.serviceIdentifier = serviceIdentifier;
        return this;
    }

    public int getserviceIdentifier() {
        return serviceIdentifier;
    }

    public String getCalledNumber() {
        return calledNumber;
    }

    public TransactionSpecification setCalledNumber(String calledNumber) {
        this.calledNumber = calledNumber;
        return this;
    }
    public int getExpectedStatusCode() {
        return expectedStatusCode;
    }

    public TransactionSpecification setExpectedStatusCode(int expectedStatusCode) {
        this.expectedStatusCode = expectedStatusCode;
        return this;
    }
   
    public LinkedHashMap<String,String> getInvoiceValidationCharge() {
        return invoiceValidationCharge;
    }

    public TransactionSpecification setInvoiceValidationCharge(LinkedHashMap<String,String> invoiceValidationCharge) {
        this.invoiceValidationCharge = invoiceValidationCharge;
        return this;
    }
    
    
    public String getInvoicePath() {
        return invoicePath;
    }

    public String getBillEndDate() {
        return billEndDate;
    }

    public String getUsageRecordLimit() {
        return searchLimit;
    }

    public TransactionSpecification setUsageRecordLimit(String searchLimit) {
        this.searchLimit = searchLimit;
        return this;
    }

    public TransactionSpecification setBillEndDate(String billEndDate) {
        this.billEndDate = billEndDate;
        return this;
    }
    public TransactionSpecification setInvoicePath(String invoicePath) {
        this.invoicePath = invoicePath;
        return this;
    }

    public String getusageCounterValueNew() {
        return usageCounterValueNew;
    }

    public TransactionSpecification setusageCounterValueNew(String usageCounterValueNew) {
        this.usageCounterValueNew = usageCounterValueNew;
        return this;
    }

    public String getTraceMessage() {
        return messageTrace;
    }

   public String getTraceFileName() {
        return sdpTraceFileName;
    }

    public TransactionSpecification setTraceFileName(String sdpTraceFileName) {
        this.sdpTraceFileName = sdpTraceFileName;
        return this;
    }

    public TransactionSpecification setTraceMessage(String messageTrace) {
        this.messageTrace = messageTrace;
        return this;
    }
    public String getExpectedValidationMessage() {
        return expectedValidationMessage;
    }

    public TransactionSpecification setExpectedValidationMessage(String expectedValidationMessage) {
        this.expectedValidationMessage = expectedValidationMessage;
        return this;
    }

   public String getStatusTrace() {
        return statusTrace;
    }

    public TransactionSpecification setTrace(String statusTrace) {
        this.statusTrace = statusTrace;
        return this;
    }
    public Boolean getRemoveInvoicePdfFile() {
        return removeInvoicePdfFile;
    }

    public TransactionSpecification setRemoveInvoicePdfFile(Boolean removeInvoicePdfFile) {
        this.removeInvoicePdfFile = removeInvoicePdfFile;
        return this;
    }

    public String getInvoiceCharge() {
        return invoiceCharge;
    }

    public TransactionSpecification setInvoiceCharge(String invoiceCharge) {
        this.invoiceCharge = invoiceCharge;
        return this;
    }

    public String getCallMessage() {
        return callMessage;
    }

    public TransactionSpecification setCallMessage(String callMessage) {
        this.callMessage = callMessage;
        return this;
    }

    public String getDedicatedAccountID() {
        return dedicatedAccountID;
    }

    public TransactionSpecification setDedicatedAccountID(String dedicatedAccountID) {
        this.dedicatedAccountID = dedicatedAccountID;
        return this;
    }
    public int getDedicatedAcountValue() {
        return dedicatedAccountValue;
    }

    public TransactionSpecification setDedicatedAcountValue(int dedicatedAccountValue) {
        this.dedicatedAccountValue = dedicatedAccountValue;
        return this;
    }

    public int getSdpId() {
        return sdpId;
    }

    public TransactionSpecification setSdpId(int sdpId) {
        this.sdpId = sdpId;
        return this;
    }

    public Integer getInvoicePdfPageNo() {
        return invoicePdfPageNo;
    }

    public TransactionSpecification setInvoicePdfPageNo(Integer invoicePdfPageNo) {
        this.invoicePdfPageNo = invoicePdfPageNo;
        return this;
    }

    public String getOptionalPoReferenceId() {
        return optionaloReferenceId;
    }

    public TransactionSpecification setOptionalPoReferenceId(String optionaloReferenceId) {
        this.optionaloReferenceId = optionaloReferenceId;
        return this;
    }

    public int getOccurenceOfMessage() { return checkOccurenceOfMessage;   }

    public TransactionSpecification setOccurenceOfMessage(int checkOccurenceOfMessage) {
        this.checkOccurenceOfMessage = checkOccurenceOfMessage;
        return this;
    }

    public String getBasicPoReferenceId() {
        return basicPoReferenceId;
    }

    public TransactionSpecification setBasicPoReferenceId(String basicPoReferenceId) {
        this.basicPoReferenceId = basicPoReferenceId;
        return this;
    }

    public String getEocOrderId() {
        return eocOrderId;
    }

    public TransactionSpecification setEocOrderId(String eocOrderId) {
        this.eocOrderId = eocOrderId;
        return this;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public TransactionSpecification setResponseBody(String responseBody) {
        this.responseBody = responseBody;
        return this;
    }

    public String getRequestedUri() {
        return requestedUri;
    }

    public TransactionSpecification setRequestedUri(String requestedUri) {
        this.requestedUri = requestedUri;
        return this;
    }

    public List<String> getCalledNumbers() {
        return calledNumbers;
    }

    public TransactionSpecification setCalledNumbers(List<String> calledNumbers) {
        this.calledNumbers = calledNumbers;
        return this;
    }

    public String getJsonFileName() {
        return jsonFileName;
    }

    public TransactionSpecification setJsonFileName(String jsonFileName) {
        this.jsonFileName = jsonFileName;
        return this;
    }

    public String getUdrTextFileName() {
        return udrTextFileName;
    }

    public TransactionSpecification setUdrTextFileName(String udrTextFileName) {
        this.udrTextFileName = udrTextFileName;
        return this;
    }
    public String getJsonFilePath() {
        return jsonFilePath;
    }

    public TransactionSpecification setJsonFilePath(String jsonFilePath) {
        this.jsonFilePath = jsonFilePath;
        return this;
    }
    public BigDecimal getExpectedCost() {
        return expectedCost;
    }

    public TransactionSpecification setExpectedCost(String expectedCost) {
        this.expectedCost = new BigDecimal(expectedCost);
        return this;
    }

    public BigDecimal getExpectedBalance() {
        return expectedBalance;
    }

    public TransactionSpecification setExpectedBalance(BigDecimal expectedBalance) {
        this.expectedBalance = expectedBalance;
        return this;
    }

    public TransactionSpecification setExpectedBalance(String expectedBalance) {
        this.expectedBalance = new BigDecimal(expectedBalance);
        return this;
    }

    public TransactionSpecification setExpectedBalance(int expectedBalance) {
        this.expectedBalance = new BigDecimal(expectedBalance);
        return this;
    }

    public BigDecimal getBalanceBefore() {
        return beforeBalance;
    }

    public TransactionSpecification setBalanceBefore(int beforeBalance) {
        this.beforeBalance = new BigDecimal(beforeBalance);
        return this;
    }

    public BigDecimal getAccountBalance() {
        return accountBalance;
    }

    public TransactionSpecification setAccountBalance(int accountBalance) {
        this.accountBalance = new BigDecimal(accountBalance);
        return this;
    }

    public BigDecimal getDedicatedAccountBalance() {
        return dedicatedAccountBalance;
    }

    public TransactionSpecification setDedicatedAccountBalance(int dedicatedAccountBalance) {
        this.dedicatedAccountBalance = new BigDecimal(dedicatedAccountBalance);
        return this;
    }
    public int getDuration() {
        return duration;
    }

    public TransactionSpecification setDuration(int duration) {
        this.duration = duration;
        return this;
    }

    public String getAnswerTime() {
        return answerTime;
    }

    public TransactionSpecification setAnswerTime(String answerTime) {
        this.answerTime = answerTime;
        return this;
    }

    public TransactionSpecification setAnswerTime(int offset, int hour, int minute) {
        this.answerTime = DateTimeFactory.newBuilder().withDayOffset(offset).withHour(hour).withMinute(minute).build()
                .getDateTimeFormatted();
        return this;
    }

    public String getTargetState() {
        return targetState;
    }

    public TransactionSpecification setTargetState(String targetState) {
        this.targetState = targetState;
        return this;
    }

    public String getActionCode() {
        return actionCode;
    }

    public TransactionSpecification setActionCode(String actionCode) {
        this.actionCode = actionCode;
        return this;
    }

    public String getReasonCode() {
        return reasonCode;
    }

    public TransactionSpecification setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
        return this;
    }

    public String getEventCode() {
        return eventCode;
    }

    public TransactionSpecification setEventCode(String eventCode) {
        this.eventCode = eventCode;
        return this;
    }

    public String getPrice() {
        return price;
    }

    public TransactionSpecification setPrice(String price) {
        this.price = price;
        return this;
    }

    public Boolean getIsAdminChargesEnabled() {
        return adminChargesEnabled;
    }

    public TransactionSpecification setIsAdminChargesEnabled(Boolean adminChargesEnabled) {
        this.adminChargesEnabled = adminChargesEnabled;
        return this;
    }

    public String getLocationAreaCode() {
        return locationAreaCode;
    }

    public TransactionSpecification setLocationAreaCode(String locationAreaCode) {
        this.locationAreaCode = locationAreaCode;
        return this;
    }

    public String getCellId() {
        return cellId;
    }

    public TransactionSpecification setCellId(String cellId) {
        this.cellId = cellId;
        return this;
    }

    public String getMccMnc() {
        return mccMnc;
    }

    public TransactionSpecification setMccMnc(String mccMnc) {
        this.mccMnc = mccMnc;
        return this;
    }
    public String getCharge() {
        return charge;
    }

    public TransactionSpecification setCharge(String charge) {
        this.charge = charge;
        return this;
    }

    public String getServiceCharacteristicsName() {
        return serviceCharacteristicsName;
    }

    public TransactionSpecification setServiceCharacteristicsName(String serviceCharacteristicsName) {
        this.serviceCharacteristicsName = serviceCharacteristicsName;
        return this;
    }

    public String getServiceCharacteristicsValue() {
        return serviceCharacteristicsValue;
    }

    public TransactionSpecification setServiceCharacteristicsValue(String serviceCharacteristicsValue) {
        this.serviceCharacteristicsValue = serviceCharacteristicsValue;
        return this;
    }

    public String getRecordingEntity() {
        return recordingEntity;
    }

    public TransactionSpecification setRecordingEntity(String recordingEntity) {
        this.recordingEntity = recordingEntity;
        return this;
    }

    public String getFqdn() {
        return fqdn;
    }

    public TransactionSpecification setFqdn(String fqdn) {
        this.fqdn = fqdn;
        return this;
    }

    public String getSessionId() {
        return sessionId;
    }

    public TransactionSpecification setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public String getCallStartTime() {
        return callStartTime;
    }

    public TransactionSpecification setCallStartTime(String callStartTime) {
        this.callStartTime = callStartTime;
        return this;
    }

    public int getUpdateNr() {
        return updateNr;
    }

    public TransactionSpecification setUpdateNr(int updateNr) {
        this.updateNr = updateNr;
        return this;
    }

    public BigDecimal getBabAdjValue() {
        return babAdjValue;
    }

    public TransactionSpecification setBabAdjValue(BigDecimal babAdjValue) {
        this.babAdjValue = babAdjValue;
        return this;
    }

    public String getRatingGroup() {
        return ratingGroup;
    }

    public TransactionSpecification setRatingGroup(String ratingGroup) {
        this.ratingGroup = ratingGroup;
        return this;
    }

    public List<String> getRequestedPOs() {
        return new ArrayList<String>(requestedPOs);
    }

    public TransactionSpecification setRequestedPOs(List<String> requestedPOs) {
        this.requestedPOs = new ArrayList<String>(requestedPOs);
        return this;
    }

    public TransactionSpecification setRequestedPOs(String requestedPO) {
        ArrayList<String> list = new ArrayList<String>();
        list.add(requestedPO);
        setRequestedPOs(list);
        return this;
    }

    public LinkedHashMap getProductOfferingIds() {
        return new LinkedHashMap(productOfferingIds);
    }

    public TransactionSpecification setProductOfferingIds(LinkedHashMap productOfferingIds) {
        this.productOfferingIds = new LinkedHashMap<>(productOfferingIds);
        return this;
    }

    public TransactionSpecification setProductOfferingIds(String productOfferingId) {
        ArrayList<String> list = new ArrayList<String>();
        list.add(productOfferingId);
        setProductOfferingIds(String.valueOf(list));
        return this;
    }

    public String getResourceId() {
        return resourceId;
    }

    public TransactionSpecification setResourceId(String resourceId) {
        this.resourceId = resourceId;
        return this;
    }

    public String getResourceSpecification() {
        return resourceSpecification;
    }

    public TransactionSpecification setResourceSpecification(String resourceSpecification) {
        this.resourceSpecification = resourceSpecification;
        return this;
    }

    public String getResourceParentId() {
        return resourceParentId;
    }

    public TransactionSpecification setResourceParentId(String resourceParentId) {
        this.resourceParentId = resourceParentId;
        return this;
    }

    public String getActionValue() {
        return actionValue;
    }

    public TransactionSpecification setActionValue(String actionValue) {
        this.actionValue = actionValue;
        return this;
    }

    public String getResourceAction() {
        return resourceAction;
    }

    public TransactionSpecification setResourceAction(String resourceAction) {
        this.resourceAction = resourceAction;
        return this;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public TransactionSpecification setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        return this;
    }

    public int getResourceCardinality() {
        return resourceCardinality;
    }

    public TransactionSpecification setResourceCardinality(int resourceCardinality) {
        this.resourceCardinality = resourceCardinality;
        return this;
    }
    public String getProfileIdBscs() {
        return profileId;
    }

    public TransactionSpecification setProfileIdBscs(String profileId) {
        this.profileId = profileId;
        return this;
    }

    public int getExpectedResourceStatusCode() {
        return expectedResourceStatusCode;
    }

    public TransactionSpecification setExpectedResourceStatusCode(int expectedResourceStatusCode) {
        this.expectedResourceStatusCode = expectedResourceStatusCode;
        return this;
    }
    public boolean getIfCheckForCardinality() {
        return checkForCardinality;
    }

    public TransactionSpecification setIfCheckForCardinality(boolean checkForCardinality) {
        this.checkForCardinality = checkForCardinality;
        return this;
    }
}
