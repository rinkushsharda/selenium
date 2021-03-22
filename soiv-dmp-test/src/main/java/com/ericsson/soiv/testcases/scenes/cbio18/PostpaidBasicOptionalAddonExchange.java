package com.ericsson.soiv.testcases.scenes.cbio18;

import com.ericsson.jive.core.execution.*;
import com.ericsson.soiv.fixtures.SoivFixture;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.CustomerInfo;
import com.ericsson.soiv.utils.TestResultInfo;
import com.ericsson.soiv.utils.TransactionSpecification;
import com.jayway.jsonpath.JsonPath;
import org.junit.Test;

import java.io.IOException;
import java.util.LinkedHashMap;

import static com.ericsson.soiv.utils.JsonHelper.getRequestedJsonElement;
import static com.ericsson.soiv.utils.http.ReadBusinessData.readBusinessCacheData;

//Created By ZMUKMAN 26-June-2019

@Fixture(SoivFixture.class)
public class PostpaidBasicOptionalAddonExchange extends SoivTestBase {
    private TestResultInfo resultInfo = new TestResultInfo();
    private CustomerInfo customerInfo = new CustomerInfo();
    private TransactionSpecification ts = new TransactionSpecification();
    private LinkedHashMap<String, String> businessCache = null;

    @Test
    @Id("00000052")
    @Tags("OT-Regression, OT-Postpaid, EC-Controlled, OT-CBiO18")
    @Description("PostpaidBasicOptionalAddonExchange UC02 BUC8BB")
    public void postpaidBasicOptionalAddonExchange() throws IOException, InterruptedException {

        businessCache = new LinkedHashMap<>(readBusinessCacheData(this.getTestCase().getId()));

        customerInfo.setCustomerBillCycle(businessCache.get("CUSTOMER.BILLCYCLE"));
        customerInfo.setOrderMode(businessCache.get("ORDER.MODE"));
        customerInfo.setRequester(businessCache.get("ORDER.REQUESTER"));
        ts.setInvoicePdfPageNo(Integer.parseInt(businessCache.get("INVOICE.PAGENO")));
        ts.setRemoveInvoicePdfFile(Boolean.valueOf(businessCache.get("INVOICE.REMOVE")));

        LinkedHashMap<String, String> productOfferings = new LinkedHashMap<String, String>();
        productOfferings.put("basic", businessCache.get("PO.ID_BASIC"));
        productOfferings.put("optional", businessCache.get("PO.ID_OPTIONAL"));
        productOfferings.put("addon", businessCache.get("PO.ID_ADDON"));
        productOfferings.put("optionalPOSwap", businessCache.get("PO.ID_OPTIONAL_SWAP"));

        ts.setProductOfferingIds(productOfferings);
        customerInfo.setNpCodePub(businessCache.get("MSISDN.NPCODEPUB"));
        customerInfo.setPlCodePub(businessCache.get("MSISDN.PLCODEPUB"));
        customerInfo.setSubmIdPub(businessCache.get("MSISDN.SUBMIDPUB"));
        customerInfo.setHlCode(businessCache.get("MSISDN.HLCODE"));
        customerInfo.setRsCode(businessCache.get("MSISDN.RSCODE"));
        customerInfo.setCsControlled(businessCache.get("MSISDN.CSCONTROLLED"));

        Steps.PROVISION.eoc.createCustomerContractBasicPO("Create Customer and Contract with Basic PO", this,
                customerInfo, ts, resultInfo).run();

        ts.setJsonFileName("AddOptionalPOonBasic.json");
        Steps.PROTOCOLS.businessLogicEOC.addOptionalPOonBasicPO("Provision : Add Optional PO in to Basic PO", this,
                customerInfo, ts, resultInfo).run();

        /*****************************
         * Add addOnPO and Submit the Cart
         **************************/

        ts.setJsonFileName("AddOnPOonBasic.json");
        Steps.PROTOCOLS.businessLogicEOC
                .addAddOnPOonBasicPO("Provision : Update Add On PO in to Basic PO", this, customerInfo, ts, resultInfo)
                .run();
        Steps.PROTOCOLS.businessLogicEOC.submitShoppingCart("Provision : Submit Shopping Cart Without Exchange", this,
                customerInfo, ts, resultInfo).run();

        /*****************************
         * Bill Generation Before PO Exchange
         **************************/
        ts.setInvoiceCharge(businessCache.get("INVOICE.CHARGE_1"));
        verifyInvoicePDF(ts, "Before PO Exchange");

        ts.setJsonFileName("createShoppingCart.json");
        Steps.PROVISION.eoc.createShoppingCart("Provision : Create Shopping Cart For Exchange Of Optional PO", this,
                customerInfo, ts, resultInfo).run();

        /*****************************
         * Steps for Optional PO Exchange
         **************************/

        Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart for POOI of Basic And Optional PO", this, customerInfo,
                        ts, resultInfo)
                .run();

        String pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("basic")), "id");
        ts.setBasicPoReferenceId(pooiId);

        pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("optional")), "id");
        ts.setOptionalPoReferenceId(pooiId);

        ts.setJsonFileName("SwapProductOffering.json");
        Jive.log("optionalPOSwap" + ts.getProductOfferingIds().get("optionalPOSwap").toString());
        Steps.PROTOCOLS.businessLogicEOC.swapProductOffering("Protocol : Exchange Optional Product Offering", this,
                customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Provision : Submit Shopping Cart For Swap In", this, customerInfo, ts, resultInfo)
                .run();

        // Optional PO Should Be Deleted From the Contract
        verifyPOStatus(productOfferings.get("optional"), "d");

        // AddOn PO Should Be remain Active In the Contract
        verifyPOStatus(productOfferings.get("addon"), "a");

        /*****************************
         * Bill Generation After PO Exchange
         **************************/
        ts.setInvoiceCharge(businessCache.get("INVOICE.CHARGE_2"));
        verifyInvoicePDF(ts, "After PO Exchange");

    }

    private void verifyInvoicePDF(TransactionSpecification ts, String message) throws InterruptedException {
        Thread.sleep(Long.parseLong(businessCache.get("THREAD.SLEEP_USAGE")));
        ts.setJsonFileName("sendBillGenerateRequest.xml");

        Steps.BILL_GENERATION.stepsBilling.sendInvoiceRequest("Protocol : Send Bill Process Create Request " + message,
                this, resultInfo, customerInfo, ts).run();

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

    private void verifyPOStatus(String poName, String poStatus) {

        Steps.PROTOCOLS.businessLogicBSCS
                .getPoStatusBSCS("Read PO Status from BSCS", this, customerInfo, ts, resultInfo).run();

        int count = JsonPath.read(ts.getResponseBody(),
                "$.SOAP-ENV:Envelope.SOAP-ENV:Body.productCfsReadResponse.productCfsReadOutputDTO.products.productCfsReadOutputProductDTO[*].productId.content")
                .toString().split(",").length;
        for (int i = 0; i < count; i++) {
            if (JsonPath.read(ts.getResponseBody(),
                    "$.SOAP-ENV:Envelope.SOAP-ENV:Body.productCfsReadResponse.productCfsReadOutputDTO.products.productCfsReadOutputProductDTO["
                            + i + "].productOfferingId.content")
                    .equals(poName)) {
                if (poStatus.equals(JsonPath.read(ts.getResponseBody(),
                        "$.SOAP-ENV:Envelope.SOAP-ENV:Body.productCfsReadResponse.productCfsReadOutputDTO.products.productCfsReadOutputProductDTO["
                                + i + "].productStatus.content"))) {
                    Jive.log(poName + " : PO is Deleted Successfully");
                } else {
                    Jive.fail(poName + " : PO Not Deleted Successfully. Kindly Check EOC Order Status");
                }
            }

        }
    }

}
