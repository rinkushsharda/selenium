package com.ericsson.soiv.teststeps.protocols.eoc.businesslogic;

import com.ericsson.jive.core.execution.Jive;
import com.ericsson.jive.core.execution.TestStepBase;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpMethod;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpResponse;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.*;
import com.jayway.jsonpath.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddResources extends TestStepBase<TestResultInfo> {
    private static Logger LOG = LoggerFactory.getLogger(AddResources.class);
    private SoivTestBase soivTestBase = null;
    private TestResultInfo resultInfo = null;
    private CustomerInfo customerInfo = null;
    private TransactionSpecification tSpec = null;

    public AddResources(String title, SoivTestBase soivTestBase, CustomerInfo customerInfo, TransactionSpecification tSpec, TestResultInfo resultInfo) {
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
                "AddResources.json");

        body = RawMessages.replaceVal(body, "deviceID", tSpec.getDeviceId());
        body = RawMessages.replaceVal(body, "parentRFSId", tSpec.getResourceParentId());
        body = RawMessages.replaceVal(body, "PRSName", tSpec.getResourceSpecification());

        resultInfo = Steps.PROTOCOLS.eoc
                .sendEocRequest("Add Resources", resultInfo, HttpMethod.POST, 201,
                        "/eoc/on/v1/sc/" + customerInfo.getShoppingCartId() + "/pooi/" + customerInfo.getPooiId()+"/resource",body)
                .run();

        HttpResponse response = (HttpResponse) resultInfo.getResult();
        String responseBody = (String) response.getBody().getValue();
        String getResourceId = JsonPath.read(responseBody,"$.id");
        Jive.log("Device Resource Id : "+getResourceId);
        tSpec.setResourceId(getResourceId);
        tSpec.setResponseBody(responseBody);

        return resultInfo;
    }

    public SoivTestBase getSoivTestBase() {
        return soivTestBase;
    }

    public AddResources setSoivTestBase(SoivTestBase soivTestBase) {
        this.soivTestBase = soivTestBase;
        return this;
    }

}
