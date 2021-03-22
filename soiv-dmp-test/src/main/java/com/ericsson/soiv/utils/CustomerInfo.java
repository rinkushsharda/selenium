package com.ericsson.soiv.utils;

import java.util.List;

import com.ericsson.soiv.testbases.SoivTestBase;

public class CustomerInfo extends SoivTestBase {
    private String partyId = null;
    private String customerId = null;
    private String customerBillCycle = null;
    private String customerNewBillCycle = null;
    private String shoppingCartId = null;
    private String contactId = null;
    private String pooiId = null;
    private String msisdn = null;
    private String serialNumber = null;
    private String linkedPortNumber = null;
    private String csid = null;
    private String invoicePath = null;
    private String mode = null;
    private String requester = null;
    private String productId = null;
    private String startDate = null;
    private String endDate = null;
    private List<String> productIds = null;
    private String invoiceId = null;
    private int usageCounterId;
    private int pamServiceId;
    private int pamIndicator = 10;
    private String sharedNumber = null;
    private int serviceIdentifier = 6;
    private String salesChannel = null;
    private String logicalResourceType = "msisdn";
    private String serialNumberType = "GSM";
    private String marketType = "GSM";
    private String npCodePub = null;
    private String plCodePub = null;
    private String submIdPub = null;
    private String hlCode = null;
    private String rsCode = null;
    private Long olidNumber = null;
    private String email = null;
    private String csControlled = null;
    private String isdn = null;
    private String ipv4 = null;
    private String iaidNumber = null;
    private String market = null;
    private String subMarket = null;
    private String network = null;
    private String portNpPub = null;
    private String offerId = null;
    private String offerName = null;
    private String apn = null;
    private Integer searchCount = 1;
    private String callingParty = null;
    private Object resource = null;

    public Object getresource() {
        return resource;
    }

    public void setresource(Object resource) {
        this.resource = resource;
    }

    public CustomerInfo() {
    }

    public CustomerInfo(String customerId, String msisdn) {

        if (customerId != null) {
            this.customerId = customerId;
        }
        if (msisdn != null) {
            this.msisdn = msisdn;
        }

    }

    public Integer getSearchCount() {
        return searchCount;
    }

    public void setSearchCount(Integer searchCount) {
        this.searchCount = searchCount;
    }

    public String getNpCodePub() {
        return npCodePub;
    }

    public CustomerInfo setNpCodePub(String npCodePub) {
        this.npCodePub = npCodePub;
        return this;
    }

    public String getemail() {
        return email;
    }

    public CustomerInfo setemail(String email) {
        this.email = email;
        return this;
    }

    public String getMarketType() {
        return marketType;
    }

    public CustomerInfo setMarketType(String marketType) {
        this.marketType = marketType;
        return this;
    }

    public String getMarket() {
        return market;
    }

    public CustomerInfo setMarket(String market) {
        this.market = market;
        return this;
    }

    public String getSubMarket() {
        return subMarket;
    }

    public CustomerInfo setSubMarket(String subMarket) {
        this.subMarket = subMarket;
        return this;
    }

    public Long getOlidNumber() {
        return olidNumber;
    }

    public CustomerInfo setOlidNumber(Long olidNumber) {
        this.olidNumber = olidNumber;
        return this;
    }

    public String getNetwork() {
        return network;
    }

    public CustomerInfo setNetwork(String network) {
        this.network = network;
        return this;
    }

    public String getOfferId() {
        return offerId;
    }

    public CustomerInfo setOfferId(String offerId) {
        this.offerId = offerId;
        return this;
    }

    public String getOfferName() {
        return offerName;
    }

    public CustomerInfo setOfferName(String offerName) {
        this.offerName = offerName;
        return this;
    }

    public String getPortNpPub() {

        return portNpPub;
    }

    public CustomerInfo setPortNpPub(String portNpPub) {
        this.portNpPub = portNpPub;
        return this;
    }

    public String getIaidNumber() {
        return iaidNumber;
    }

    public CustomerInfo setIaidNumber(String iaidNumber) {
        this.iaidNumber = iaidNumber;
        return this;
    }

    public String getSerialNumberType() {
        return serialNumberType;
    }

    public CustomerInfo setSerialNumberType(String serialNumberType) {
        this.serialNumberType = serialNumberType;
        return this;
    }

    public String getCsControlled() {
        return csControlled;
    }

    public CustomerInfo setCsControlled(String csControlled) {
        this.csControlled = csControlled;
        return this;
    }

    public String getRsCode() {
        return rsCode;
    }

    public CustomerInfo setRsCode(String rsCode) {
        this.rsCode = rsCode;
        return this;
    }

    public String getIpv4() {
        return ipv4;
    }

    public CustomerInfo setIpv4(String ipv4) {
        this.ipv4 = ipv4;
        return this;
    }

    public String getLogicalResourceType() {
        return logicalResourceType;
    }

    public CustomerInfo setLogicalResourceType(String logicalResourceType) {
        this.logicalResourceType = logicalResourceType;
        return this;
    }

    public String getPlCodePub() {
        return plCodePub;
    }

    public CustomerInfo setPlCodePub(String plCodePub) {
        this.plCodePub = plCodePub;
        return this;
    }

    public String getSubmIdPub() {
        return submIdPub;
    }

    public CustomerInfo setSubmIdPub(String submIdPub) {
        this.submIdPub = submIdPub;
        return this;
    }

    public String getHlCode() {
        return hlCode;
    }

    public CustomerInfo setHlCode(String hlCode) {
        this.hlCode = hlCode;
        return this;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public CustomerInfo setMsisdn(String msisdn) {
        this.msisdn = msisdn;
        return this;
    }

    public String getIsdn() {
        return isdn;
    }

    public CustomerInfo setIsdn(String isdn) {
        this.isdn = isdn;
        return this;
    }

    public int getUsageCounterId() {
        return usageCounterId;
    }

    public CustomerInfo setUsageCounterId(int usageCounterId) {
        this.usageCounterId = usageCounterId;
        return this;
    }

    public int getPamServiceId() {
        return pamServiceId;
    }

    public CustomerInfo setPamServiceId(int pamServiceId) {
        this.pamServiceId = pamServiceId;
        return this;
    }

    public String getSharedNumber() {
        return sharedNumber;
    }

    public CustomerInfo setSharedNumber(String sharedNumber) {
        this.sharedNumber = sharedNumber;
        return this;
    }

    public int getServiceIdentifier() {
        return serviceIdentifier;
    }

    public CustomerInfo setServiceIdentifier(int serviceIdentifier) {
        this.serviceIdentifier = serviceIdentifier;
        return this;
    }

    public int getPamIndicator() {
        return pamIndicator;
    }

    public CustomerInfo setPamIndicator(int pamIndicator) {
        this.pamIndicator = pamIndicator;
        return this;
    }

    public String getProductId() {
        return productId;
    }

    public CustomerInfo setProductID(String productId) {
        this.productId = productId;
        return this;
    }

    public CustomerInfo setOfferProductID(Integer productId) {
        this.productId = productId.toString();
        return this;
    }

    public String getOfferProductId() {
        return productId;
    }

    public String getOrderMode() {
        return mode;
    }

    public CustomerInfo setOrderMode(String mode) {
        this.mode = mode;
        return this;
    }

    public String getRequester() {
        return requester;
    }

    public CustomerInfo setRequester(String requester) {
        this.requester = requester;
        return this;
    }

    public String getContractId() {
        return contactId;
    }

    public CustomerInfo setContractId(String contactId) {
        this.contactId = contactId;
        return this;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public CustomerInfo setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
        return this;
    }

    public String getCSID() {
        return csid;
    }

    public CustomerInfo setCSID(String csid) {
        this.csid = csid;
        return this;
    }

    public String getInvoicePath() {
        return invoicePath;
    }

    public CustomerInfo setInvoicePath(String invoicePath) {
        this.invoicePath = invoicePath;
        return this;
    }

    public String getLinkedPortNumber() {
        return linkedPortNumber;
    }

    public CustomerInfo setLinkedPortNumber(String linkedPortNumber) {
        this.linkedPortNumber = linkedPortNumber;
        return this;
    }

    public String getPartyId() {
        return partyId;
    }

    public CustomerInfo setPartyId(String partyId) {
        this.partyId = partyId;
        return this;
    }

    public String getCustomerBillCycle() {
        return customerBillCycle;
    }

    public CustomerInfo setCustomerBillCycle(String customerBillCycle) {
        this.customerBillCycle = customerBillCycle;
        return this;
    }

    public String getCustomerNewBillCycle() {
        return customerNewBillCycle;
    }

    public CustomerInfo setCustomerNewBillCycle(String customerNewBillCycle) {
        this.customerNewBillCycle = customerNewBillCycle;
        return this;
    }

    public String getShoppingCartId() {
        return shoppingCartId;
    }

    public CustomerInfo setShoppingCartId(String shoppingCartId) {
        this.shoppingCartId = shoppingCartId;
        return this;
    }

    public String getCustomerId() {
        return customerId;
    }

    public CustomerInfo setCustomerId(String customerId) {
        this.customerId = customerId;
        return this;
    }

    public String getStartDate() {
        return startDate;
    }

    public CustomerInfo setStartDate(String startDate) {
        this.startDate = startDate;
        return this;
    }

    public String getEndDate() {
        return endDate;
    }

    public CustomerInfo setEndDate(String endDate) {
        this.endDate = endDate;
        return this;
    }

    public String getPooiId() {
        return pooiId;
    }

    public CustomerInfo setPooiId(String pooiId) {
        this.pooiId = pooiId;
        return this;
    }

    public List<String> getProductIds() {
        return productIds;
    }

    public CustomerInfo setProductIds(List<String> products) {
        this.productIds = products;
        return this;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public CustomerInfo setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
        return this;
    }

    public String getSalesChannel() {
        return salesChannel;
    }

    public CustomerInfo setSalesChannel(String salesChannel) {
        this.salesChannel = salesChannel;
        return this;
    }

    public String getAPN() {
        return apn;
    }

    public CustomerInfo setAPN(String apn) {
        this.apn = apn;
        return this;
    }

    public String getCallingParty() {
        return callingParty;
    }

    public CustomerInfo setCallingParty(String callingParty) {
        this.callingParty = callingParty;
        return this;
    }

}
