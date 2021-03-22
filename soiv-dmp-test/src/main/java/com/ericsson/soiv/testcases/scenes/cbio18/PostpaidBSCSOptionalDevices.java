package com.ericsson.soiv.testcases.scenes.cbio18;

import com.ericsson.jive.core.execution.*;
import com.ericsson.soiv.fixtures.SoivFixture;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.CalculateDateTime;
import com.ericsson.soiv.utils.Constants;
import com.ericsson.soiv.utils.CustomerInfo;
import com.ericsson.soiv.utils.RawMessages;
import com.ericsson.soiv.utils.TestResultInfo;
import com.ericsson.soiv.utils.TransactionSpecification;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.util.LinkedHashMap;

import static com.ericsson.soiv.utils.HelperClass.getRandomNumberWithinRange;
import static com.ericsson.soiv.utils.JsonHelper.updatePOOi;
import static com.ericsson.soiv.utils.http.ReadBusinessData.readBusinessCacheData;

//Created By ZMUKMAN 12-June-2019

@Fixture(SoivFixture.class)
public class PostpaidBSCSOptionalDevices extends SoivTestBase {
    private TestResultInfo resultInfo = new TestResultInfo();
    private CustomerInfo customerInfo = new CustomerInfo();
    private TransactionSpecification ts = new TransactionSpecification();
    private CalculateDateTime calculateDateTime = new CalculateDateTime();
    private LinkedHashMap<String, String> productOfferings = new LinkedHashMap<String, String>();
    private LinkedHashMap<String, String> businessCache = null;

    @Test
    @Id("00000039")
    @Tags("OT-Regression, OT-Postpaid, BSCS-Controlled, OT-CBiO18")
    @Description("PostpaidBSCSOptionalDevices BUC22A_F")
    public void postpaidBscsOptionalDevices() throws IOException, InterruptedException {

        businessCache = new LinkedHashMap<>(readBusinessCacheData(this.getTestCase().getId()));

        customerInfo.setCustomerBillCycle(businessCache.get("CUSTOMER.BILLCYCLE"));
        customerInfo.setOrderMode(businessCache.get("ORDER.MODE"));
        customerInfo.setRequester(businessCache.get("ORDER.REQUESTER"));

        ts.setRemoveInvoicePdfFile(Boolean.getBoolean(businessCache.get("INVOICE.REMOVE")));

        productOfferings.put("basicDevice", businessCache.get("PO.ID_BASIC_DEVICE"));
        productOfferings.put("basicDeviceCFSS", businessCache.get("PO.ID_BASIC_DEVICE_CFSS"));
        productOfferings.put("basicVoice", businessCache.get("PO.ID_BASIC_VOICE"));
        productOfferings.put("basicDeviceCFSSAddon", businessCache.get("PO.ID_BASIC_DEVICE_CFSS_ADDON"));
        productOfferings.put("optionalDevice", businessCache.get("PO.ID_OPTIONAL_DEVICE"));
        productOfferings.put("optionalDeviceCFSS", businessCache.get("PO.ID_OPTIONAL_DEVICE_CFSS"));
        productOfferings.put("addonDevice", businessCache.get("PO.ID_ADDON_DEVICE"));
        ts.setProductOfferingIds(productOfferings);

        /******************************
         * createCustomerContractBasicPO With Device and Without CFSS
         ***************************/
        productOfferings.put("basic", (String) ts.getProductOfferingIds().get("basicDevice"));
        ts.setProductOfferingIds(productOfferings);
        customerInfo.setNpCodePub(businessCache.get("MSISDN.NPCODEPUB"));
        customerInfo.setPlCodePub(businessCache.get("MSISDN.PLCODEPUB"));
        customerInfo.setSubmIdPub(businessCache.get("MSISDN.SUBMIDPUB"));
        customerInfo.setHlCode(businessCache.get("MSISDN.HLCODE"));
        customerInfo.setRsCode(businessCache.get("MSISDN.RSCODE"));
        customerInfo.setCsControlled(businessCache.get("MSISDN.CSCONTROLLED"));

        /******************************
         * createCustomerContractBasicPO With Device and With CFSS
         ***************************/

        productOfferings.put("basic", (String) ts.getProductOfferingIds().get("basicDeviceCFSS"));
        ts.setProductOfferingIds(productOfferings);
        Steps.PROVISION.eoc.createCustomerContractBasicPO("Create Customer and Contract with Device & CFSS On Basic PO",
                this, customerInfo, ts, resultInfo).run();

        addDeviceInOptionalPO();

        Steps.PROTOCOLS.businessLogicEOC.updateOptionalProductOffering("Provision : Update Basic PO to Add Devices",
                this, customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision : Submit Shopping Cart For Customer B : " + customerInfo.getCustomerId(),
                        this, customerInfo, ts, resultInfo)
                .run();

        /*****************************
         * Consume Offline National Voice from Basic PO
         **************************/

        ts.setInvoiceCharge(businessCache.get("INVOICE.CHARGE_2"));
        runUsageAndCheckBill(ts);
        verifyDeviceInBill(ts, Constants.MOBILEMAKER, Integer.parseInt(businessCache.get("INVOICE.PAGE_2")));

        /******************************
         * createCustomerContractBasicPO With Device On Basic and CFSS On Optional
         ***************************/

        productOfferings.put("basic", (String) ts.getProductOfferingIds().get("basicVoice"));
        ts.setProductOfferingIds(productOfferings);
        Steps.PROVISION.eoc.createCustomerContractBasicPO("Create Customer and Contract with Device On Basic PO", this,
                customerInfo, ts, resultInfo).run();

        /******************************
         * Add optional PO With CFSS and Submit the Cart
         **************************/

        productOfferings.put("optional", (String) ts.getProductOfferingIds().get("optionalDeviceCFSS"));
        ts.setProductOfferingIds(productOfferings);
        ts.setJsonFileName("AddOptionalPOonBasic.json");
        Steps.PROTOCOLS.businessLogicEOC.addOptionalPOonBasicPO("Provision : Add Optional PO Wth CFSS in to Basic PO",
                this, customerInfo, ts, resultInfo).run();

        addDeviceInOptionalPO();

        Steps.PROTOCOLS.businessLogicEOC.updateOptionalProductOffering("Provision : Update Optional PO to Add Devices",
                this, customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision : Submit Shopping Cart For Customer C : " + customerInfo.getCustomerId(),
                        this, customerInfo, ts, resultInfo)
                .run();

        Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart To Set Contract ID", this, customerInfo, ts, resultInfo)
                .run();

        String setContractId = customerInfo.getContractId();
        customerInfo.setCustomerId(customerInfo.getCustomerId());
        ts.setActionCode(businessCache.get("PERSONALIZE.KEY_ACTION"));
        ts.setActionValue(businessCache.get("PERSONALIZE.VALUE_MODIFY"));
        ts.setResourceAction("Add");
        ts.setResourceCardinality(Integer.parseInt(businessCache.get("RESOURCE.CARDINALITY")));

        Steps.PROVISION.eoc.manageResourcesInContract("Add " + ts.getResourceCardinality() + " Resource In Contract",
                this, customerInfo, ts, resultInfo).run();

        ts.setActionCode(businessCache.get("PERSONALIZE.KEY_ACTION"));
        ts.setActionValue(businessCache.get("PERSONALIZE.VALUE_MODIFY"));
        ts.setResourceAction("Delete");

        Steps.PROVISION.eoc
                .manageResourcesInContract("Delete 1st Resource From Contract", this, customerInfo, ts, resultInfo)
                .run();

        Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart To Set Contract ID", this, customerInfo, ts, resultInfo)
                .run();

        /**** DeActivate the PO and Validated the PO Status ******/

        ts.setActionCode(businessCache.get("ACTION.CODE_DEACTIVATE"));
        ts.setReasonCode(businessCache.get("REASON.CODE_DEACTIVATE"));
        productOfferings.put("optional", productOfferings.get("optionalDeviceCFSS"));
        ts.setProductOfferingIds(productOfferings);
        Steps.PROVISION.eoc.updateOptionalPO("DeActivate Optional PO on Contract C", this, customerInfo, ts, resultInfo)
                .run();

        /**** Reactivated the PO and Validated the PO Status ******/
        ts.setActionCode(businessCache.get("ACTION.CODE_REACTIVATE"));
        ts.setReasonCode(businessCache.get("REASON.CODE_REACTIVATE"));
        productOfferings.put("optional", productOfferings.get("optionalDeviceCFSS"));
        ts.setProductOfferingIds(productOfferings);

        Steps.PROVISION.eoc.updateOptionalPO("Reactivate Optional Po on Contract C", this, customerInfo, ts, resultInfo)
                .run();

        /**** DeActivate And Repurchase Optional PO and Validated the PO Status ******/

        ts.setActionCode(businessCache.get("ACTION.CODE_DEACTIVATE"));
        ts.setReasonCode(businessCache.get("REASON.CODE_DEACTIVATE"));
        productOfferings.put("optional", productOfferings.get("optionalDeviceCFSS"));
        ts.setProductOfferingIds(productOfferings);
        Steps.PROVISION.eoc.updateOptionalPO("DeActivate Again Optional PO on Contract C For Repurchase", this,
                customerInfo, ts, resultInfo).run();

        customerInfo.setContractId(setContractId);
        productOfferings.put("optional", productOfferings.get("optionalDeviceCFSS"));
        ts.setProductOfferingIds(productOfferings);
        Steps.PROVISION.eoc.repurchaseOptionalPO("Repurchase Optional PO on Existing Contract C", this, customerInfo,
                ts, resultInfo).run();

        /*****************************
         * Consume Offline National Voice from Basic PO
         **************************/

        ts.setInvoiceCharge(businessCache.get("INVOICE.CHARGE_3"));
        runUsageAndCheckBill(ts);
        verifyDeviceInBill(ts, Constants.MOBILEMAKER, Integer.parseInt(businessCache.get("INVOICE.PAGE_3")));

        /******************************
         * createCustomerContractBasicPO With Device On Addon and CFSS On BasicPO
         ***************************/

        productOfferings.put("basic", (String) ts.getProductOfferingIds().get("basicDeviceCFSSAddon"));
        ts.setProductOfferingIds(productOfferings);
        Steps.PROVISION.eoc.createCustomerContractBasicPO("Create Customer and Contract with CFSS On Basic PO", this,
                customerInfo, ts, resultInfo).run();

        /******************************
         * Add Addon PO With Device and Submit the Cart
         **************************/

        productOfferings.put("addon", (String) ts.getProductOfferingIds().get("addonDevice"));
        ts.setProductOfferingIds(productOfferings);
        ts.setJsonFileName("AddOnPOonBasic.json");
        Steps.PROTOCOLS.businessLogicEOC.addAddOnPOonBasicPO("Provision : Update Add On PO With Devices in to Basic PO",
                this, customerInfo, ts, resultInfo).run();

        addDeviceInOptionalPO();

        Steps.PROTOCOLS.businessLogicEOC.updateOptionalProductOffering("Provision : Update AddOn PO and Add Devices",
                this, customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision : Submit Shopping Cart For Customer D : " + customerInfo.getCustomerId(),
                        this, customerInfo, ts, resultInfo)
                .run();

        /*****************************
         * Consume Offline National Voice from Basic PO
         **************************/

        ts.setInvoiceCharge(businessCache.get("INVOICE.CHARGE_4"));
        runUsageAndCheckBill(ts);
        verifyDeviceInBill(ts, Constants.MOBILEMAKER, Integer.parseInt(businessCache.get("INVOICE.PAGE_4")));

    }

    private void addDeviceInOptionalPO() {
        JSONObject body = null;
        body = updatePOOi(ts.getResponseBody(), "Mobile_Model", Constants.MOBILEMODEL);
        body = updatePOOi(body.toString(), "Mobile_Maker", Constants.MOBILEMAKER);
        body = updatePOOi(body.toString(), "Mobile_Color", Constants.MOBILECOLOR);
        body = updatePOOi(body.toString(), "deviceID", Constants.DEVICEID + getRandomNumberWithinRange(10, 999999));
        ts.setResponseBody(body.toString());
    }

    private void runUsageAndCheckBill(TransactionSpecification ts) throws InterruptedException {

        Thread.sleep(Long.valueOf(businessCache.get("THREAD.SLEEP_INVOICE")));
        ts.setInvoicePdfPageNo(Integer.parseInt(businessCache.get("INVOICE.PAGE")));

        /***************************************
         * OfflineBSCS Voice Call
         ******************************/
        ts.setUdrTextFileName("voice_udr.txt");

        String body = RawMessages.getBodyFromTextFilePlacedInTestStep(this.getClass().getName(),
                "protocols/bscs/udrTemplates", ts.getUdrTextFileName()).toString();

        body = body.replace("{{IMSI}}", customerInfo.getLinkedPortNumber());
        body = body.replace("{{MSISDN}}", customerInfo.getMsisdn());
        body = body.replace("{{DATE}}", calculateDateTime.getCurrentDateTime());
        body = body.replace("{{PLCODE}}", "EUR01");
        body = body.replace("{{DURATION}}", "3600");
        body = body.replace("{{NETWORK}}", "GSM");
        body = body.replace("{{TYPE}}", "TEL");

        ts.setResponseBody(body);
        Steps.CHARGING_OFFLINE.stepsChargingOfflineBscs
                .offlineVoice("Protocol : BSCS - Offline Voice Usage", this, resultInfo, customerInfo, ts).run();

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

    }

    private void verifyDeviceInBill(TransactionSpecification ts, String searchString, int invoicePageNo) {

        ts.setInvoicePdfPageNo(invoicePageNo);
        resultInfo = Steps.BILL_GENERATION.stepsBilling
                .getInvoice("Validation : Get Invoice PDF and Validate Device", this, resultInfo, customerInfo, ts)
                .run();
        String readInvoiceData = (String) resultInfo.getResult();

        if (readInvoiceData.contains(searchString)) {
            Jive.log("OK : Matching String Found - " + searchString + " ! Validation Of Invoice is done Successfully!");
        } else {
            Jive.failAndContinue("FAILED : Matching String ot Found - " + searchString + " Kindly Check Invoice Content!");
        }

    }

}
