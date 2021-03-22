package com.ericsson.soiv.teststeps.protocols.cs.buisnesslogic;

import com.ericsson.jive.core.protocols.http.messagetypes.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.jive.core.execution.TestStepBase;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpMethod;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.CustomerInfo;
import com.ericsson.soiv.utils.TestResultInfo;
import com.ericsson.soiv.utils.TransactionSpecification;

public class RunUsageCounterAdjustment extends TestStepBase<TestResultInfo> {

    private static Logger LOG = LoggerFactory.getLogger(RunUsageCounterAdjustment.class);
    private SoivTestBase soivTestBase = null;
    private TestResultInfo resultInfo = null;
    private CustomerInfo customerInfo = null;
    private TransactionSpecification tSpec = null;

    public RunUsageCounterAdjustment(String title, SoivTestBase soivTestBase, CustomerInfo customerInfo,
            TransactionSpecification tSpec, TestResultInfo resultInfo) {
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
        if (tSpec.getRequestedUri() == null) {
            tSpec.setRequestedUri(
                    "/csadmin/request_handler.php?request=UsageCounters%2FUpdateUsageCounters&subscriberNumber="
                            + customerInfo.getMsisdn()
                            + "&transactionCurrency=&updateUsageCounterForMultiUser=&usageCounterID="
                            + customerInfo.getUsageCounterId() + "&usageCounterValueNew="
                            + tSpec.getusageCounterValueNew()
                            + "&adjustmentUsageCounterValueRelative=&usageCounterMonetaryValueNew=&adjustmentUsageCounterMonetaryValueRelative=&associatedPartyID=&productID="
                            + customerInfo.getProductId() + "&action=Send&returnFormat=json");
        }

        resultInfo = Steps.PROTOCOLS.cs
                .sendCsRequest("Run Usage Counter Adjustment", resultInfo, HttpMethod.GET, 200, tSpec.getRequestedUri())
                .run();
        HttpResponse response = (HttpResponse) resultInfo.getResult();
        tSpec.setResponseBody((String) response.getBody().getValue());
        tSpec.setRequestedUri(null);
        return resultInfo;
    }

    public SoivTestBase getSoivTestBase() {
        return soivTestBase;
    }

    public RunUsageCounterAdjustment setSoivTestBase(SoivTestBase soivTestBase) {
        this.soivTestBase = soivTestBase;
        return this;
    }
}