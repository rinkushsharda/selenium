package com.ericsson.soiv.teststeps.protocols.eoc.businesslogic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.jive.core.execution.TestStepBase;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpMethod;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpResponse;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.CustomerInfo;
import com.ericsson.soiv.utils.RawMessages;
import com.ericsson.soiv.utils.TestResultInfo;
import com.ericsson.soiv.utils.TransactionSpecification;

public class AddIpv4Resources extends TestStepBase<TestResultInfo>
{
    private static Logger LOG = LoggerFactory.getLogger(AddIpv4Resources.class);
    private SoivTestBase soivTestBase = null;
    private TestResultInfo resultInfo = null;
    private CustomerInfo customerInfo = null;
    private TransactionSpecification tSpec = null;

    public AddIpv4Resources(String title,
                            SoivTestBase soivTestBase,
                            CustomerInfo customerInfo,
                            TransactionSpecification tSpec,
                            TestResultInfo resultInfo)
    {
        super(title);
        this.resultInfo = resultInfo;
        this.customerInfo = customerInfo;
        this.tSpec = tSpec;
        this.soivTestBase = soivTestBase;

        if (!resultInfo.getShouldContinue())
        {
            LOG.error(title + " - Not executed due to previous error");
        }
    }

    @Override
    public TestResultInfo execute()
    {
        if (!resultInfo.getShouldContinue())
        {
            SoivTestBase.failAndContinue("Step not executed due to a previous error");
            return resultInfo;
        }
        LOG.info("Step title: " + getTitle());

        String body = RawMessages.getBodyFromJsonFilePlacedInTestStep(this.getClass().getName(), "protocols/eoc",
                "AddIpv4Resources.json");

        body = RawMessages.replaceVal(body, "ExtraIPv4", customerInfo.getIpv4());
        body = RawMessages.replaceVal(body, "rfsID", tSpec.getResourceParentId());
        body = RawMessages.replaceVal(body, "resourceSpecification", tSpec.getResourceSpecification());

        resultInfo = Steps.PROTOCOLS.eoc
                .sendEocRequest("Add Resources", resultInfo, HttpMethod.POST, tSpec.getExpectedResourceStatusCode(),
                        "/eoc/on/v1/sc/" + customerInfo.getShoppingCartId() + "/pooi/" + customerInfo.getPooiId() + "/resource", body)
                .run();

        HttpResponse response = (HttpResponse) resultInfo.getResult();
        String responseBody = (String) response.getBody().getValue();
        tSpec.setResponseBody(responseBody);

        return resultInfo;
    }

    public SoivTestBase getSoivTestBase()
    {
        return soivTestBase;
    }

    public AddIpv4Resources setSoivTestBase(SoivTestBase soivTestBase)
    {
        this.soivTestBase = soivTestBase;
        return this;
    }

}