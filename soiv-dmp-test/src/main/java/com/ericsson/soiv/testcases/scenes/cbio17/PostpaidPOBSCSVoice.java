package com.ericsson.soiv.testcases.scenes.cbio17;

import com.ericsson.jive.core.execution.*;
import com.ericsson.soiv.fixtures.SoivFixture;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.CalculateDateTime;
import com.ericsson.soiv.utils.CustomerInfo;
import com.ericsson.soiv.utils.RawMessages;
import com.ericsson.soiv.utils.TestResultInfo;
import com.ericsson.soiv.utils.TransactionSpecification;
import org.junit.Test;

import java.io.IOException;
import java.util.LinkedHashMap;

import static com.ericsson.soiv.utils.http.ReadBusinessData.readBusinessCacheData;

// Created By ZKAPSAR 25-Mar-2019
// Modified By ZMUKMAN 22-April-2019

@Fixture(SoivFixture.class)
public class PostpaidPOBSCSVoice extends SoivTestBase {
    private TestResultInfo resultInfo = new TestResultInfo();
    private CustomerInfo customerInfo = new CustomerInfo();
    private TransactionSpecification ts = new TransactionSpecification();
    private CalculateDateTime calculateDateTime = new CalculateDateTime();

    @Test
    @Id("00000002")
    @Tags("OT-Regression, OT-Postpaid, BSCS-Controlled, OT-CBiO17")
    @Description("TS731_PO_BSCS_Basic_Voice_B1")
    public void postpaidPoBscsBasicVoice() throws IOException {

        LinkedHashMap<String, String> businessCache = new LinkedHashMap<>(
                readBusinessCacheData(this.getTestCase().getId()));
        customerInfo.setCustomerBillCycle(businessCache.get("CUSTOMER.BILLCYCLE"));
        customerInfo.setOrderMode(businessCache.get("ORDER.MODE"));
        customerInfo.setRequester(businessCache.get("ORDER.REQUESTER"));

        LinkedHashMap productOfferings = new LinkedHashMap();
        productOfferings.put("basic", businessCache.get("PO.ID_BASIC"));
        ts.setProductOfferingIds(productOfferings);
        ts.setInvoicePdfPageNo(Integer.parseInt(businessCache.get("INVOICE.PAGENO")));
        ts.setRemoveInvoicePdfFile(Boolean.valueOf(businessCache.get("INVOICE.REMOVE")));

        customerInfo.setNpCodePub(businessCache.get("MSISDN.NPCODEPUB"));
        customerInfo.setPlCodePub(businessCache.get("MSISDN.PLCODEPUB"));
        customerInfo.setSubmIdPub(businessCache.get("MSISDN.SUBMIDPUB"));
        customerInfo.setHlCode(businessCache.get("MSISDN.HLCODE"));
        customerInfo.setRsCode(businessCache.get("MSISDN.RSCODE"));
        customerInfo.setCsControlled(businessCache.get("MSISDN.CSCONTROLLED"));

        Steps.PROVISION.eoc.createCustomerContractBasicPO("Create Customer and Contract with Basic PO", this,
                customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision : Submit Shopping Cart", this, customerInfo, ts, resultInfo).run();

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
                .offlineVoice("Protocol : BSCS - Offline SMS Usage", this, resultInfo, customerInfo, ts).run();

        ts.setInvoiceCharge(businessCache.get("INVOICE.CHARGE_1"));
        verifyBill(ts);

        ts.setTargetState(businessCache.get("TARGET.STATE_SUSPEND"));
        ts.setActionCode(businessCache.get("ACTION.CODE_SUSPEND"));
        ts.setReasonCode(businessCache.get("REASON.CODE_SUSPEND"));
        ts.setIsAdminChargesEnabled(Boolean.valueOf(businessCache.get("ADMINCHARGES.ENABLE_SUSPEND")));

        Steps.PROVISION.eoc.manageContractStatus("Provision : Suspend Contract ", this, customerInfo, ts, resultInfo)
                .run();

        ts.setTargetState(businessCache.get("TARGET.STATE_REACTIVATE"));
        ts.setActionCode(businessCache.get("ACTION.CODE_REACTIVATE"));
        ts.setReasonCode(businessCache.get("REASON.CODE_REACTIVATE"));
        ts.setIsAdminChargesEnabled(Boolean.valueOf(businessCache.get("ADMINCHARGES.ENABLE_REACTIVATE")));

        Steps.PROVISION.eoc
                .manageContractStatus("Provision : ReActivation Contract ", this, customerInfo, ts, resultInfo).run();

        ts.setTargetState(businessCache.get("TARGET.STATE_DEACTIVATE"));
        ts.setActionCode(businessCache.get("ACTION.CODE_DEACTIVATE"));
        ts.setReasonCode(businessCache.get("REASON.CODE_DEACTIVATE"));
        ts.setIsAdminChargesEnabled(Boolean.valueOf(businessCache.get("ADMINCHARGES.ENABLE_DEACTIVATE")));

        Steps.PROVISION.eoc
                .manageContractStatus("Provision : DeActivation Contract ", this, customerInfo, ts, resultInfo).run();

        Steps.PROVISION.eoc.createCustomerContractBasicPO("Create Customer and Contract with Basic PO", this,
                customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision : Submit Shopping Cart", this, customerInfo, ts, resultInfo).run();

        ts.setTargetState(businessCache.get("TARGET.STATE_SUSPEND_PERSONALIZE"));
        ts.setActionCode(businessCache.get("ACTION.CODE_SUSPEND_PERSONALIZE"));
        ts.setReasonCode(businessCache.get("REASON.CODE_SUSPEND_PERSONALIZE"));
        ts.setEventCode(businessCache.get("EVENT.CODE_SUSPEND_PERSONALIZE"));
        ts.setPrice(businessCache.get("PRICE.AMOUNT_SUSPEND_PERSONALIZE"));
        ts.setIsAdminChargesEnabled(Boolean.valueOf(businessCache.get("ADMINCHARGES.ENABLE_PERSONALIZE")));
        ts.setInvoiceCharge(businessCache.get("INVOICE.CHARGE_2"));

        Steps.PROVISION.eoc.manageContractStatus("Provision : Suspend Contract With Personalization", this,
                customerInfo, ts, resultInfo).run();

        ts.setInvoiceCharge(businessCache.get("INVOICE.CHARGE_2"));
        verifyBill(ts);
    }

    private void verifyBill(TransactionSpecification ts) {
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
}
