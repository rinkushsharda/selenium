package com.ericsson.soiv.teststeps.charging.offline.bscs;

import com.ericsson.jive.core.execution.TestStepBase;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.CustomerInfo;
import com.ericsson.soiv.utils.TestResultInfo;
import com.ericsson.soiv.utils.TransactionSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OfflineVoice extends TestStepBase<TestResultInfo> {
    private static Logger LOG = LoggerFactory.getLogger(OfflineSms.class);
    private SoivTestBase soivTestBase = null;
    private TestResultInfo resultInfo = null;
    private CustomerInfo customerInfo = null;
    private TransactionSpecification tSpec = null;

    public OfflineVoice(String title, SoivTestBase soivTestBase, TestResultInfo resultInfo, CustomerInfo customerInfo,
            TransactionSpecification tSpec) {
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

        resultInfo = Steps.OfflineChargingBscsRaw
                .createOfflineUdr(this.getClass().getName(), resultInfo, tSpec, customerInfo).run();

        return resultInfo;
    }

    public SoivTestBase getSoivTestBase() {
        return soivTestBase;
    }

    public OfflineVoice setSoivTestBase(SoivTestBase soivTestBase) {
        this.soivTestBase = soivTestBase;
        return this;
    }

}
