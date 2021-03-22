package com.ericsson.soiv.teststeps.protocols.bscs.businesslogic;

import com.ericsson.jive.core.execution.TestStepBase;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpMethod;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.CustomerInfo;
import com.ericsson.soiv.utils.RawMessages;
import com.ericsson.soiv.utils.TestResultInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadContract extends TestStepBase<TestResultInfo> {
    private static Logger LOG = LoggerFactory.getLogger(ReadContract.class);
    private SoivTestBase soivTestBase = null;
    private TestResultInfo resultInfo = null;
    private CustomerInfo customerInfo = null;

    public ReadContract(String title, SoivTestBase soivTestBase, CustomerInfo customerInfo, TestResultInfo resultInfo) {
        super(title);
        this.resultInfo = resultInfo;
        this.customerInfo = customerInfo;
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

        String body = RawMessages.getBodyFromJsonFilePlacedInTestStep(this.getClass().getName(), "protocols/bscs",
                "ReadContract.xml");

        body = RawMessages.replaceVal(body, "contractID", customerInfo.getContractId());

        resultInfo = Steps.PROTOCOLS.bscs.sendBscsRequest(this.getClass().getName(), resultInfo, HttpMethod.POST, 200,
                "/wsi/services/",body).run();

        return resultInfo;
    }

    public SoivTestBase getSoivTestBase() {
        return soivTestBase;
    }

    public ReadContract setSoivTestBase(SoivTestBase soivTestBase) {
        this.soivTestBase = soivTestBase;
        return this;
    }

}
