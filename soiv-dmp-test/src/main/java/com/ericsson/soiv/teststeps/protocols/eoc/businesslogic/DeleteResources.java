package com.ericsson.soiv.teststeps.protocols.eoc.businesslogic;

import com.ericsson.jive.core.execution.Jive;
import com.ericsson.jive.core.execution.TestStepBase;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpMethod;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpResponse;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.CustomerInfo;
import com.ericsson.soiv.utils.TestResultInfo;
import com.ericsson.soiv.utils.TransactionSpecification;
import com.jayway.jsonpath.JsonPath;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteResources extends TestStepBase<TestResultInfo> {
    private static Logger LOG = LoggerFactory.getLogger(DeleteResources.class);
    private SoivTestBase soivTestBase = null;
    private TestResultInfo resultInfo = null;
    private CustomerInfo customerInfo = null;
    private TransactionSpecification tSpec = null;

    public DeleteResources(String title, SoivTestBase soivTestBase, CustomerInfo customerInfo, TransactionSpecification tSpec, TestResultInfo resultInfo) {
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

        Steps.PROTOCOLS.eoc.sendEocRequest("Delete Resource", resultInfo, HttpMethod.DELETE, 204,
                        "/eoc/on/v1/sc/" + customerInfo.getShoppingCartId() + "/pooi/" + customerInfo.getPooiId()+"/resource/"
                                +tSpec.getResourceId()).run();

        Steps.PROTOCOLS.eoc.sendEocRequest("Verify Deleted Resource", resultInfo, HttpMethod.GET, 200,
                        "/eoc/on/v1/sc/" + customerInfo.getShoppingCartId() + "/pooi/" + customerInfo.getPooiId()+"/resource/"
                                +tSpec.getResourceId()).run();

        HttpResponse response = (HttpResponse) resultInfo.getResult();
        String responseBody = (String) response.getBody().getValue();
        String action  = JsonPath.read(responseBody,"$.action");
        Assert.assertEquals("Delete",action);
        Jive.log("Resource Id : "+tSpec.getResourceId()+" Deleted Successfully! ");
        tSpec.setResponseBody(responseBody);

        return resultInfo;
    }

    public SoivTestBase getSoivTestBase() {
        return soivTestBase;
    }

    public DeleteResources setSoivTestBase(SoivTestBase soivTestBase) {
        this.soivTestBase = soivTestBase;
        return this;
    }

}
