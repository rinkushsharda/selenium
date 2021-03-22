package com.ericsson.soiv.testcases.scenes.cbio17;

import static com.ericsson.soiv.utils.JsonHelper.getRequestedJsonElement;
import static com.ericsson.soiv.utils.http.ReadBusinessData.readBusinessCacheData;

import java.io.IOException;
import java.util.LinkedHashMap;

import org.junit.Test;

import com.ericsson.jive.core.execution.Description;
import com.ericsson.jive.core.execution.Fixture;
import com.ericsson.jive.core.execution.Id;
import com.ericsson.jive.core.execution.Tags;
import com.ericsson.soiv.fixtures.SoivFixture;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.CustomerInfo;
import com.ericsson.soiv.utils.TestResultInfo;
import com.ericsson.soiv.utils.TransactionSpecification;

// Created By ZEAHDCE on 27-May-2019
@Fixture(SoivFixture.class)
public class SolutionValidationCSPostpaidBundlingOfBundle extends SoivTestBase {
    private TestResultInfo resultInfo = new TestResultInfo();
    private CustomerInfo customerInfo = new CustomerInfo();
    private TransactionSpecification ts = new TransactionSpecification();
    private LinkedHashMap<String, String> productOfferings = new LinkedHashMap<String, String>();
    LinkedHashMap<String, String> businessCache = null;

    @Test
    @Id("00000048")
    @Tags("OT-Regression, OT-Prepaid, EC-Controlled, OT-CBiO17")
    @Description("TS799 - Solution Validation Bundling of Bundled PO")
    public void bundlingofexistingOptionalAndAddonBundle() throws IOException {

        businessCache = new LinkedHashMap<>(readBusinessCacheData(this.getTestCase().getId()));
        customerInfo.setCustomerBillCycle(businessCache.get("CUSTOMER.BILLCYCLE"));
        customerInfo.setOrderMode(businessCache.get("ORDER.MODE"));
        customerInfo.setRequester(businessCache.get("ORDER.REQUESTER"));

        productOfferings.put("basic", businessCache.get("PO.ID_BASIC_BUNDLING_OF_BUNDLE"));
        productOfferings.put("addon", businessCache.get("PO.ID_ADDON_BUNDLING_OF_BUNDLE"));
        productOfferings.put("optional", businessCache.get("PO.ID_OPTIONAL_BUNDLING_OF_BUNDLE"));

        ts.setProductOfferingIds(productOfferings);

        /******************************
         * Create Contract with Basic PO and Submit the cart
         **************************/

        customerInfo.setNpCodePub(businessCache.get("MSISDN.NPCODEPUB"));
        customerInfo.setPlCodePub(businessCache.get("MSISDN.PLCODEPUB"));
        customerInfo.setSubmIdPub(businessCache.get("MSISDN.SUBMIDPUB"));
        customerInfo.setHlCode(businessCache.get("MSISDN.HLCODE"));
        customerInfo.setRsCode(businessCache.get("MSISDN.RSCODE"));
        customerInfo.setCsControlled(businessCache.get("MSISDN.CSCONTROLLED"));

        Steps.PROVISION.eoc.createCustomerContractBasicPO("Create Customer and Contract with Basic PO", this,
                customerInfo, ts, resultInfo).run();
        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Submit shopping cart for Add on PO ", this, customerInfo, ts, resultInfo).run();

        /******************************
         * Adding the Optional And AddOn Bundle to the Basic PO and Submit the cart
         **************************/

        ts.setJsonFileName("createShoppingCart.json");
        Steps.PROVISION.eoc
                .createShoppingCart("Provision : Create Shopping Cart For Optional Bundled PO and Add on Bundled PO",
                        this, customerInfo, ts, resultInfo)
                .run();

        // Set Basic PO Relies On
        Steps.PROTOCOLS.businessLogicEOC
                .getShoppingCart("Protocol : Get Shopping Cart To get Basic PO", this, customerInfo, ts, resultInfo)
                .run();

        String pooiId = getRequestedJsonElement(ts, String.valueOf(ts.getProductOfferingIds().get("basic")), "id");
        ts.setBasicPoReferenceId(pooiId);

        ts.setJsonFileName("AddOnPOonBasic.json");
        Steps.PROTOCOLS.businessLogicEOC.addAddOnPOonBasicPO("Provision : Update Add On Bundled PO in to Basic PO",
                this, customerInfo, ts, resultInfo).run();

        ts.setJsonFileName("AddOptionalPOonBasic.json");
        Steps.PROTOCOLS.businessLogicEOC.addOptionalPOonBasicPO("Provision : Add Optional Bundled PO in to Basic PO",
                this, customerInfo, ts, resultInfo).run();
        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Submit shopping cart for Add on Bundled PO And AddOn Bundled PO ", this,
                        customerInfo, ts, resultInfo)
                .run();

        /******************************
         * Deletion of Optional Bundled PO and repurchase the same Optional Bundled PO
         **************************/
        ts.setActionCode(businessCache.get("ACTION.CODE_DELETE"));

        ts.setProductOfferingIds(productOfferings);
        Steps.PROVISION.eoc.updateOptionalPO("Update POOI for Delete of Optional Bundled PO for repurchase again", this,
                customerInfo, ts, resultInfo).run();

        Steps.PROVISION.eoc.repurchaseOptionalPO("Repurchase the same Bundled optional PO to the Basic PO", this,
                customerInfo, ts, resultInfo).run();
    }

}