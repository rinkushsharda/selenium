package com.ericsson.soiv.testcases.scenes.cbio18;

import com.ericsson.jive.core.execution.Fixture;

import static com.ericsson.soiv.utils.HelperClass.getRandomNumberWithinRange;
import static com.ericsson.soiv.utils.JsonHelper.getRequestedJsonElement;
import static com.ericsson.soiv.utils.JsonHelper.updatePOOi;
import static com.ericsson.soiv.utils.http.ReadBusinessData.readBusinessCacheData;
import java.io.IOException;
import java.util.LinkedHashMap;
import org.json.JSONObject;
import org.junit.Test;
import com.ericsson.jive.core.execution.Description;
import com.ericsson.jive.core.execution.Id;
import com.ericsson.jive.core.execution.Jive;
import com.ericsson.jive.core.execution.Tags;
import com.ericsson.soiv.fixtures.SoivFixture;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.CalculateDateTime;
import com.ericsson.soiv.utils.Constants;
import com.ericsson.soiv.utils.ConvertXMLToJson;
import com.ericsson.soiv.utils.CustomerInfo;
import com.ericsson.soiv.utils.TestResultInfo;
import com.ericsson.soiv.utils.TransactionSpecification;

//Created by ZEAHDCE on 11-July-2019

@Fixture(SoivFixture.class)
public class PostpaidPOBSCSCrossTechnologyWithOutCFS extends SoivTestBase {
    private TestResultInfo resultInfo = new TestResultInfo();
    private CustomerInfo customerInfo = new CustomerInfo();
    private TransactionSpecification ts = new TransactionSpecification();
    private ConvertXMLToJson xmlToJson = new ConvertXMLToJson();
    private CalculateDateTime calculateDateTime = new CalculateDateTime();
    private LinkedHashMap<String, String> businessCache = null;

    @Test
    @Id("00000065")
    @Tags("OT-Regression, OT-Postpaid, BSCS-Controlled, OT-CBiO18")
    @Description("Cross Technology Product Offering without CFS")
    public void postpaidPOBSCScrossTechnology() throws IOException {

        businessCache = new LinkedHashMap<>(readBusinessCacheData(this.getTestCase().getId()));
        customerInfo.setCustomerBillCycle(businessCache.get("CUSTOMER.BILLCYCLE"));
        customerInfo.setOrderMode(businessCache.get("ORDER.MODE"));
        customerInfo.setRequester(businessCache.get("ORDER.REQUESTER"));

        LinkedHashMap<String, String> productOfferings = new LinkedHashMap<String, String>();
        productOfferings.put("basic", businessCache.get("PO.ID_BASIC"));

        ts.setProductOfferingIds(productOfferings);

        /*****************************
         * createCustomerContractBasicPO
         ***************************/

        resultInfo = Steps.PROVISION.bscs
                .createCustomer("Provision : Create Customer", this, customerInfo, ts, resultInfo).run();

        ts.setJsonFileName("createShoppingCart.json");
        resultInfo = Steps.PROVISION.eoc
                .createShoppingCart("Provision : Create Shopping Cart", this, customerInfo, ts, resultInfo).run();

        /****************************
         * Adding the Basic PO in the Cart
         ***********************************/
        customerInfo.setMarketType("ISD");

        customerInfo.setMarket(businessCache.get("BASICPO.MARKET"));
        customerInfo.setSubMarket(businessCache.get("BASICPO.SUB_MARKET"));
        customerInfo.setNetwork(businessCache.get("BASICPO.NETWORK"));

        ts.setJsonFileName("addProductOfferingShoppingCart.json");
        resultInfo = Steps.PROVISION.eoc
                .addPoInShoppingCart("Provision : Add PO in Shopping Cart", this, customerInfo, ts, resultInfo).run();

        ts.setBasicPoReferenceId(customerInfo.getPooiId());

        /*****************************
         * Getting the Shopping Cart for getting Basic POOI
         *************************/

        Steps.PROVISION.eoc
                .getShoppingCart("Get shopping cart for getting Basic PO", this, customerInfo, ts, resultInfo).run();

        String pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("basic")), "id");
        customerInfo.setPooiId(pooiId);

        Steps.PROTOCOLS.businessLogicEOC.getPooi("Getting the Basic POOI for updating the SetupBox Characteristics ",
                this, customerInfo, ts, resultInfo).run();

        updateSetupBoxCharacteristics();

        Steps.PROTOCOLS.businessLogicEOC.updatePooi("updating the Basic PO for updating the SetupBox Characteristics  ",
                this, customerInfo, ts, resultInfo).run();

        Steps.PROTOCOLS.businessLogicEOC.submitShoppingCart("submit cart for updating the SetupBox Characteristics ",
                this, customerInfo, ts, resultInfo).run();

        verifySetupBoxCharacteristicsInBill(ts);

    }

    private void updateSetupBoxCharacteristics() {
        JSONObject body = null;
        body = updatePOOi(ts.getResponseBody(), "STB_Model", Constants.SETUPBOXMODEL);
        body = updatePOOi(body.toString(), "STB_Maker", Constants.SETUPBOXMAKER);
        body = updatePOOi(body.toString(), "STB_Color", Constants.SETUPBOXCOLOR);
        body = updatePOOi(body.toString(), "deviceID", Constants.DEVICEID + getRandomNumberWithinRange(10, 999999));
        ts.setResponseBody(body.toString());
    }

    private void verifySetupBoxCharacteristicsInBill(TransactionSpecification ts) {

        ts.setInvoicePdfPageNo(Integer.parseInt(businessCache.get("INVOICE.PAGENO_3")));
        ts.setJsonFileName("sendBillGenerateRequest.xml");
        Steps.BILL_GENERATION.stepsBilling
                .sendInvoiceRequest("Protocol : Send Bill Process Create Request", this, resultInfo, customerInfo, ts)
                .run();

        resultInfo = Steps.BILL_GENERATION.stepsBilling
                .getInvoice("Validation : Get Invoice PDF and Validate GRAND TOTAL in PDF", this, resultInfo,
                        customerInfo, ts)
                .run();
        String readInvoiceData = (String) resultInfo.getResult();

        if ((readInvoiceData.contains(Constants.SETUPBOXMODEL) && (readInvoiceData.contains(Constants.SETUPBOXCOLOR))
                && (readInvoiceData.contains(Constants.SETUPBOXMAKER))
                && (readInvoiceData.contains(Constants.DEVICEID)))) {
            Jive.log("OK : SetupBox Characteristics " + Constants.SETUPBOXMODEL + Constants.SETUPBOXMAKER
                    + Constants.SETUPBOXCOLOR + Constants.DEVICEID + " Found - "
                    + " ! Validation Of Invoice is done Successfully!");
        } else {
            Jive.fail("FAILED : Setup Box Characteristics not Found - " + " Kindly Check Invoice Content!");
        }

    }

}
