package com.ericsson.soiv.testcases.scenes.cbio17;

import com.ericsson.jive.core.execution.*;
import com.ericsson.soiv.fixtures.SoivFixture;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.CustomerInfo;
import com.ericsson.soiv.utils.JsonHelper;
import com.ericsson.soiv.utils.TestResultInfo;
import com.ericsson.soiv.utils.TransactionSpecification;
import com.jayway.jsonpath.JsonPath;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.LinkedHashMap;

import static com.ericsson.soiv.utils.JsonHelper.searchPamServiceId;
import static com.ericsson.soiv.utils.http.ReadBusinessData.readBusinessCacheData;

// ZMUKMAN Created on 22-May-2019

@Fixture(SoivFixture.class)
public class PostpaidPOCSBasic extends SoivTestBase {
    private TestResultInfo resultInfo = new TestResultInfo();
    private CustomerInfo customerInfo = new CustomerInfo();
    private TransactionSpecification ts = new TransactionSpecification();
    private LinkedHashMap productOfferings = new LinkedHashMap();
    private LinkedHashMap<String, String> businessCache = null;

    @Test
    @Id("00000031")
    @Tags("OT-Regression, OT-Postpaid, EC-Controlled, OT-CBiO17")
    @Description("TS751_PostpaidPOCSBasic")
    public void postpaidPoCsBasic() throws InterruptedException, IOException {

        businessCache = new LinkedHashMap<>(readBusinessCacheData(this.getTestCase().getId()));

        customerInfo.setCustomerBillCycle(businessCache.get("CUSTOMER.BILLCYCLE"));
        customerInfo.setCustomerNewBillCycle(businessCache.get("CUSTOMER.BILLCYCLE_NEW"));
        customerInfo.setOrderMode(businessCache.get("ORDER.MODE"));
        customerInfo.setRequester(businessCache.get("ORDER.REQUESTER"));
        ts.setRemoveInvoicePdfFile(Boolean.valueOf(businessCache.get("INVOICE.REMOVE")));

        productOfferings.put("basic", businessCache.get("PO.ID_BASIC"));
        productOfferings.put("optional", businessCache.get("PO.ID_OPTIONAL"));
        productOfferings.put("basicBarringService", businessCache.get("PO.ID_BASIC_BARRINGSERVICE"));// This PO is Used
                                                                                                     // only for the
                                                                                                     // Barring Of
                                                                                                     // Subscription TC
        ts.setProductOfferingIds(productOfferings);

        /*****************************
         * Create New Customer and Contract for Sales Channel : SelfCare And Billing
         **************************/
        customerInfo.setNpCodePub(businessCache.get("MSISDN.NPCODEPUB"));
        customerInfo.setPlCodePub(businessCache.get("MSISDN.PLCODEPUB"));
        customerInfo.setSubmIdPub(businessCache.get("MSISDN.SUBMIDPUB"));
        customerInfo.setHlCode(businessCache.get("MSISDN.HLCODE"));
        customerInfo.setRsCode(businessCache.get("MSISDN.RSCODE"));
        customerInfo.setCsControlled(businessCache.get("MSISDN.CSCONTROLLED"));

        ts.setJsonFileName("createShoppingCartSalesChannel.json");
        ts.setInvoiceCharge(businessCache.get("INVOICE.CHARGE_SELFCARE"));
        ts.setInvoicePdfPageNo(Integer.parseInt(businessCache.get("INVOICE.PAGENO")));
        createCustomerContract(businessCache.get("SALES.CHANNEL_SELFCARE"));
        verifyOneTimeChargesInvoice(businessCache.get("SALES.CHANNEL_SELFCARE"));

        /*****************************
         * Create New Customer and Contract for Sales Channel : Retail And Billing
         **************************/

        ts.setJsonFileName("createShoppingCartSalesChannel.json");
        ts.setInvoiceCharge(businessCache.get("INVOICE.CHARGE_RETAIL"));
        ts.setInvoicePdfPageNo(Integer.parseInt(businessCache.get("INVOICE.PAGENO")));
        createCustomerContract(businessCache.get("SALES.CHANNEL_RETAIL"));
        verifyOneTimeChargesInvoice(businessCache.get("SALES.CHANNEL_RETAIL"));

        /*****************************
         * Create New Customer , Contract for Optional PO and Validation Of Contracting
         **************************/
      

        Steps.PROVISION.eoc.createCustomerContractBasicPO("Create Customer and Contract with Basic PO", this,
                customerInfo, ts, resultInfo).run();

        ts.setJsonFileName("AddOptionalPOonBasic.json");
        Steps.PROTOCOLS.businessLogicEOC.addOptionalPOonBasicPO("Provision : Add Optional PO in to Basic PO", this,
                customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision : Submit Shopping Cart For Contract : " + customerInfo.getContractId(),
                        this, customerInfo, ts, resultInfo)
                .run();

        Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart for PO Termination", this, customerInfo, ts, resultInfo)
                .run();

        Steps.PROTOCOLS.businessLogicBSCS
                .getPoStatusBSCS("Protocol : Read PO Status from BSCS : " + customerInfo.getContractId(), this,
                        customerInfo, ts, resultInfo)
                .run();

        int count = JsonPath.read(ts.getResponseBody(),
                "$.SOAP-ENV:Envelope.SOAP-ENV:Body.productCfsReadResponse.productCfsReadOutputDTO.products.productCfsReadOutputProductDTO[*].productId.content")
                .toString().split(",").length;
        for (int i = 0; i < count; i++) {
            if (JsonPath.read(ts.getResponseBody(),
                    "$.SOAP-ENV:Envelope.SOAP-ENV:Body.productCfsReadResponse.productCfsReadOutputDTO.products.productCfsReadOutputProductDTO["
                            + i + "].productOfferingId.content")
                    .equals(productOfferings.get("optional"))) {
                if ("a".equals(JsonPath.read(ts.getResponseBody(),
                        "$.SOAP-ENV:Envelope.SOAP-ENV:Body.productCfsReadResponse.productCfsReadOutputDTO.products.productCfsReadOutputProductDTO["
                                + i + "].productStatus.content"))) {
                    Jive.log("OK : " + productOfferings.get("optional") + " PO Found Successfully on BSCS Contract");
                } else {
                    Jive.failAndContinue("FAILED : " + productOfferings.get("optional") + " PO Not Found on BSCS Contract");
                }
            }
        }

        Steps.PROTOCOLS.businessLogicCS.getOffers("Protocol : Get Offers from CS", this, customerInfo, ts, resultInfo)
                .run();

        if (JsonHelper.checkPoOnOffer(ts, productOfferings.get("optional").toString()) == true) {
            Jive.log("OK : " + productOfferings.get("optional") + " PO Found Successfully on CS Offer");
        } else {
            Jive.failAndContinue("FAILED : " + productOfferings.get("optional") + " PO Not Found in CS Offer");
        }

        /*****************************
         * PAM Execution - PO CS Postpaid Basic Monthly Usage
         **************************/

        Steps.PROTOCOLS.businessLogicCS.getAccountDetails("Get PAM Service ID", this, customerInfo, ts, resultInfo)
                .run();
        int pamServiceID = searchPamServiceId(ts.getResponseBody(),
                Integer.parseInt(businessCache.get("PAM.CLASS_ID")));

        customerInfo.setPamServiceId(pamServiceID);

        Steps.PROTOCOLS.businessLogicCS.runPam("Run PAM and Reset SMS Counter", this, customerInfo, ts, resultInfo)
                .run();

        /*****************************
         * INVOICE - Recurring Charges Validations
         **************************/

        // TODO Recurring Charges Should be There need to verify
        Thread.sleep(Long.parseLong(businessCache.get("THREAD.SLEEP_USAGE")));
        ts.setJsonFileName("sendBillGenerateRequest.xml");

        Steps.BILL_GENERATION.stepsBilling.sendInvoiceRequest(
                "Protocol : Send Bill Process Create Request For Customer  : " + customerInfo.getCustomerId(), this,
                resultInfo, customerInfo, ts).run();

        resultInfo = Steps.BILL_GENERATION.stepsBilling
                .getInvoice("Validation : Get Invoice PDF and Validate OTC in PDF : " + customerInfo.getMsisdn(), this,
                        resultInfo, customerInfo, ts)
                .run();
        // String readInvoiceData = (String) resultInfo.getResult();

        /*****************************
         * Change Customer Billing Cycle
         **************************/

        Steps.PROVISION.bscs.changeCustomerBillingCycle("Provision : Change Customer Bill Cycle", this, customerInfo,
                ts, resultInfo).run();
        Jive.log("BSCS : Verify New Bill Cycle : " + customerInfo.getCustomerBillCycle() + " For Customer : "
                + customerInfo.getCustomerId());
        Assert.assertEquals(customerInfo.getCustomerBillCycle(), customerInfo.getCustomerNewBillCycle());

        /*****************************
         * Barring Of Subscription - PO CS Postpaid Basic
         **************************/

        productOfferings.put("basic", ts.getProductOfferingIds().get("basicBarringService").toString());
        ts.setProductOfferingIds(productOfferings);
        Steps.PROVISION.eoc.createCustomerContractBasicPO("Create Customer and Contract with Basic PO", this,
                customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision : Submit Shopping Cart", this, customerInfo, ts, resultInfo).run();

        ts.setServiceCharacteristicsName(businessCache.get("SERVICE.CHARACTERISTICS_NAME"));
        ts.setServiceCharacteristicsValue(businessCache.get("SERVICE.CHARACTERISTICS_VALUE"));

        Steps.PROVISION.eoc
                .barringOfSubscription("Provision : Personalization Barring Service of the Subscription on Basic PO",
                        this, customerInfo, ts, resultInfo)
                .run();

        Boolean barringOutValue = JsonPath.read(ts.getResponseBody(),
                "$.SOAP-ENV:Envelope.SOAP-ENV:Body.productCfsReadResponse.productCfsReadOutputDTO.products.productCfsReadOutputProductDTO.cfs.productCfsReadCFSOutputDTO[1].characteristics.characteristicValueDTO.value.content");

        Jive.log("Verifying Personalization of BarringOutService : " + barringOutValue);
        assert barringOutValue != false;

        /*****************************
         * Suspend And Terminate Contract
         **************************/

        ts.setTargetState(businessCache.get("TARGET.STATE_SUSPEND"));
        ts.setActionCode(businessCache.get("ACTION.CODE_SUSPEND"));
        ts.setReasonCode(businessCache.get("REASON.CODE_SUSPEND"));
        ts.setIsAdminChargesEnabled(Boolean.valueOf(businessCache.get("ADMINCHARGES.ENABLE_SUSPEND")));

        Steps.PROVISION.eoc.manageContractStatus("Provision : Suspend Contract ", this, customerInfo, ts, resultInfo)
                .run();

        ts.setTargetState(businessCache.get("TARGET.STATE_DEACTIVATE"));
        ts.setActionCode(businessCache.get("ACTION.CODE_DEACTIVATE"));
        ts.setReasonCode(businessCache.get("REASON.CODE_DEACTIVATE"));
        ts.setIsAdminChargesEnabled(Boolean.valueOf(businessCache.get("ADMINCHARGES.ENABLE_DEACTIVATE")));

        Steps.PROVISION.eoc
                .manageContractStatus("Provision : DeActivation Contract ", this, customerInfo, ts, resultInfo).run();
    }

    private void createCustomerContract(String salesChannel) {
        customerInfo.setSalesChannel(salesChannel);

        Steps.PROVISION.bscs.createCustomer("Provision : Create Customer", this, customerInfo, ts, resultInfo).run();

        resultInfo = Steps.PROVISION.eoc.createShoppingCart("Provision : Create Shopping Cart For " + salesChannel,
                this, customerInfo, ts, resultInfo).run();

        ts.setJsonFileName("selectSerialNumber.xml");
        resultInfo = Steps.PROTOCOLS.businessLogicBSCS
                .getFreeSerialNumber("Protocol : Get Free Serial Number", this, customerInfo, ts, resultInfo).run();

        ts.setJsonFileName("selectLogicalResource.xml");
        resultInfo = Steps.PROTOCOLS.businessLogicBSCS
                .selectLogicalResource("Protocol : Get Free MSISDN Number", this, customerInfo, ts, resultInfo).run();

        customerInfo.setMsisdn(customerInfo.getresource().toString());
        ts.setJsonFileName("addProductOfferingShoppingCart.json");
        resultInfo = Steps.PROVISION.eoc.addPoInShoppingCart("Provision : Add PO in Shopping Cart For " + salesChannel,
                this, customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC
                .updateProductOffering("Provision : Update Basic PO", this, customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision : Submit Shopping Cart For Sales Channel : " + salesChannel, this,
                        customerInfo, ts, resultInfo)
                .run();

    }

    private void verifyOneTimeChargesInvoice(String message) throws InterruptedException {
        // Steps for Billing
        Thread.sleep(Long.parseLong(businessCache.get("THREAD.SLEEP_USAGE")));

        ts.setJsonFileName("sendBillGenerateRequest.xml");

        Steps.BILL_GENERATION.stepsBilling.sendInvoiceRequest("Protocol : Send Bill Process Create Request For : "
                + message + " And Customer : " + customerInfo.getCustomerId(), this, resultInfo, customerInfo, ts)
                .run();

        resultInfo = Steps.BILL_GENERATION.stepsBilling
                .getInvoice("Validation : Get Invoice PDF and Validate OTC in PDF : " + customerInfo.getMsisdn(), this,
                        resultInfo, customerInfo, ts)
                .run();
        String readInvoiceData = (String) resultInfo.getResult();

        if (readInvoiceData.contains("GRAND TOTAL " + ts.getInvoiceCharge())) {
            Jive.log("OK : Invoice PDF Charges : " + ts.getInvoiceCharge() + " are Validated Successfully");
        } else {
           Jive.failAndContinue("FAILED : Invoice PDF Charges : " + ts.getInvoiceCharge() + " are Not Validated Successfully!! Please Check the Invoice Content : "
                    + readInvoiceData);
        }
      //As communicated by Jaswinder, recurring charge is not required
       /* Jive.log("Dummy Assertion for Recurring Charge");
        if (readInvoiceData.contains("Recuring Charge is "+ts.getInvoiceCharge())) {
            Jive.log("OK : Recuring Charge : " + ts.getInvoiceCharge() + " are Validated Successfully For " + message);
        } else {
            Jive.fail("FAILED : Recuring Charges are Not Validated Successfully! Please Check the Invoice Content : "
                    + readInvoiceData);
        }*/

    }
}
