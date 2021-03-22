package com.ericsson.soiv.teststeps.protocols.eoc.businesslogic;

import com.ericsson.jive.core.execution.Jive;
import com.ericsson.jive.core.execution.TestStepBase;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpMethod;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdatePooi extends TestStepBase<TestResultInfo> {
    private static Logger LOG = LoggerFactory.getLogger(UpdatePooi.class);
    private SoivTestBase soivTestBase = null;
    private TestResultInfo resultInfo = null;
    private CustomerInfo customerInfo = null;
    private TransactionSpecification tSpec = null;

    public UpdatePooi(String title, SoivTestBase soivTestBase, CustomerInfo customerInfo, TransactionSpecification tSpec, TestResultInfo resultInfo) {
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

        String body = null;

        if (!tSpec.getResponseBody().equals(null)){
            body = tSpec.getResponseBody();
        }
        else {
            Jive.fail("Set the Requested Body to Update POOI");
        }

        resultInfo = Steps.PROTOCOLS.eoc
                .sendEocRequest(this.getClass().getName(), resultInfo, HttpMethod.PUT, 200,
                        "/eoc/on/v1/sc/" + customerInfo.getShoppingCartId() + "/pooi/" + customerInfo.getPooiId(), body)
                .run();

        return resultInfo;
    }

    public SoivTestBase getSoivTestBase() {
        return soivTestBase;
    }

    public UpdatePooi setSoivTestBase(SoivTestBase soivTestBase) {
        this.soivTestBase = soivTestBase;
        return this;
    }

}
