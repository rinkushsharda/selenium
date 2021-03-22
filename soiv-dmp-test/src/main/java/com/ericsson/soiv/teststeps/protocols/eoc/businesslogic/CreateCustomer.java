package com.ericsson.soiv.teststeps.protocols.eoc.businesslogic;

import com.ericsson.jive.core.execution.TestStepBase;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpMethod;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.RawMessages;
import com.ericsson.soiv.utils.TestResultInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateCustomer extends TestStepBase<TestResultInfo> {
    private static Logger LOG = LoggerFactory.getLogger(CreateCustomer.class);
    private SoivTestBase soivTestBase = null;
    private TestResultInfo resultInfo = null;

    public CreateCustomer(String title, SoivTestBase soivTestBase, TestResultInfo resultInfo) {
        super(title);
        this.resultInfo = resultInfo;
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
        String body = RawMessages.getBodyFromJsonFilePlacedInTestStep(this.getClass().getName(), "protocols/",
                "executeBillRunVer2.json");
        body = RawMessages.replaceVal(body, "customerBillCycle", "01");

        resultInfo = Steps.PROTOCOLS.bscs.sendBscsRequest(this.getClass().getName(), resultInfo, HttpMethod.GET, 200,
                "/wsi/services/ws_CIL_6_CustomerCreateService.wsdl/",body).run();

        return resultInfo;
    }

    public SoivTestBase getSoivTestBase() {
        return soivTestBase;
    }

    public CreateCustomer setSoivTestBase(SoivTestBase soivTestBase) {
        this.soivTestBase = soivTestBase;
        return this;
    }

}
