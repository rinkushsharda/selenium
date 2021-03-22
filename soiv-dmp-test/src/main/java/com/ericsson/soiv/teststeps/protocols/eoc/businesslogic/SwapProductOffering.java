package com.ericsson.soiv.teststeps.protocols.eoc.businesslogic;

import com.ericsson.jive.core.execution.TestStepBase;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpMethod;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpResponse;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.*;
import com.jayway.jsonpath.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SwapProductOffering extends TestStepBase<TestResultInfo> {
    private static Logger LOG = LoggerFactory.getLogger(SwapProductOffering.class);
    private SoivTestBase soivTestBase = null;
    private TestResultInfo resultInfo = null;
    private CustomerInfo customerInfo = null;
    private TransactionSpecification tSpec = null;
    private CalculateDateTime getTimeInMillis = new CalculateDateTime();

    public SwapProductOffering(String title, SoivTestBase soivTestBase, CustomerInfo customerInfo, TransactionSpecification tSpec, TestResultInfo resultInfo) {
        super(title);
        this.resultInfo = resultInfo;
        this.customerInfo = customerInfo;
        this.tSpec = tSpec;
        this.soivTestBase = soivTestBase;

        if (!resultInfo.getShouldContinue()) {
            LOG.error(title + " - Not executed due to previous error");
        }
    }

    @Override
    public TestResultInfo execute() {
        if (!resultInfo.getShouldContinue()) {
            SoivTestBase.failAndContinue("Step not executed due to a previous error");
            return resultInfo;
        }
        LOG.info("Step title: " + getTitle());
        String body = RawMessages.getBodyFromJsonFilePlacedInTestStep(this.getClass().getName(), "protocols/eoc",
                tSpec.getJsonFileName());

        body = RawMessages.replaceVal(body, "scID", customerInfo.getShoppingCartId());
        body = RawMessages.replaceVal(body, "customerID", customerInfo.getCustomerId());
        body = RawMessages.replaceVal(body, "sdpID", String.valueOf(tSpec.getSdpId()));
        body = RawMessages.replaceVal(body, "coDate", String.valueOf(getTimeInMillis.getCurrentDate()));
        body = RawMessages.replaceVal(body, "contractID", customerInfo.getContractId());
        body = RawMessages.replaceVal(body, "basicPoID", tSpec.getBasicPoReferenceId());
        body = RawMessages.replaceVal(body, "SwapPO_POOI", tSpec.getOptionalPoReferenceId());
        body = RawMessages.replaceVal(body, "suppressBSCSOneTimeCharges", "false");
        body = RawMessages.replaceVal(body, "OptionalPOIDSwapTo", tSpec.getProductOfferingIds().get("optionalPOSwap").toString());

        resultInfo = Steps.PROTOCOLS.eoc.sendEocRequest(this.getClass().getName(), resultInfo, HttpMethod.POST, 201,
                "/eoc/on/v1/sc/"+customerInfo.getShoppingCartId()+"/pooi",body).run();

        HttpResponse response = (HttpResponse) resultInfo.getResult();
        String respBody = (String)response.getBody().getValue();
        tSpec.setResponseBody(respBody);

        return resultInfo;
    }

    public SoivTestBase getSoivTestBase() {
        return soivTestBase;
    }

    public SwapProductOffering setSoivTestBase(SoivTestBase soivTestBase) {
        this.soivTestBase = soivTestBase;
        return this;
    }

}
