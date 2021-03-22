package com.ericsson.soiv.testcases.scenes.cbio17;

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
import org.junit.Test;

import java.io.IOException;
import java.util.LinkedHashMap;

import static com.ericsson.soiv.utils.http.ReadBusinessData.readBusinessCacheData;

// ZMUKMAN Created on 09-April-2019

@Fixture(SoivFixture.class)
public class PrepaidPOCSBasic extends SoivTestBase {
    private TestResultInfo resultInfo = new TestResultInfo();
    private CustomerInfo customerInfo = new CustomerInfo();
    private TransactionSpecification ts = new TransactionSpecification();

    @Test
    @Id("00000001")
    @Tags("OT-Regression, OT-Prepaid, EC-Controlled, OT-CBiO17")
    @Description("TS702_PrepaidPOCSBasic")
    public void prepaidPoCsBasic() throws IOException {

        LinkedHashMap<String, String> businessCache = new LinkedHashMap<>(
                readBusinessCacheData(this.getTestCase().getId()));

        // Set All the Customer Properties
        customerInfo.setCustomerBillCycle(businessCache.get("CUSTOMER.BILLCYCLE"));
        customerInfo.setOrderMode(businessCache.get("ORDER.MODE"));
        customerInfo.setRequester(businessCache.get("ORDER.REQUESTER"));

        LinkedHashMap<String, String> productOfferings = new LinkedHashMap<String, String>();
        productOfferings.put("basic", businessCache.get("PO.ID_BASIC"));
        productOfferings.put("optional", businessCache.get("PO.ID_OPTIONAL"));
        productOfferings.put("addon", businessCache.get("PO.ID_ADDON"));
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

        ts.setJsonFileName("AddOnPOonBasic.json");
        Steps.PROTOCOLS.businessLogicEOC
                .addAddOnPOonBasicPO("Protocol : Update Add On PO in to Basic PO", this, customerInfo, ts, resultInfo)
                .run();

        Steps.PROTOCOLS.businessLogicEOC
                .submitShoppingCart("Protocol : Submit Shopping Cart", this, customerInfo, ts, resultInfo).run();

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
    }
}
