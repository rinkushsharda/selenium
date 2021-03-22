package com.ericsson.soiv.teststeps.protocols.cs.buisnesslogic;

import com.ericsson.jive.core.execution.TestStepBase;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpMethod;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.CustomerInfo;
import com.ericsson.soiv.utils.TestResultInfo;
import com.ericsson.soiv.utils.TransactionSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// This Test Step is used to Run Pam Scheduler and Do Action depends on the Service Id
public class RunPamScheduler extends TestStepBase<TestResultInfo> {

private static Logger LOG = LoggerFactory.getLogger(RunPamScheduler.class);
private SoivTestBase soivTestBase = null;
private TestResultInfo resultInfo = null;
private CustomerInfo customerInfo = null;
private TransactionSpecification tSpec = null;

    public RunPamScheduler(String title, SoivTestBase soivTestBase, CustomerInfo customerInfo,
                       TransactionSpecification tSpec, TestResultInfo resultInfo) {
    super(title);
    this.resultInfo = resultInfo;
    this.customerInfo = customerInfo;
    this.soivTestBase = soivTestBase;
    this.tSpec = tSpec;

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

    resultInfo = Steps.PROTOCOLS.cs.sendCsRequest("GetAccountBalanceAndDate Request to Charging System", resultInfo, HttpMethod.GET, 200,
            "/csadmin/request_handler.php?"
                    +"request=PAM%2FRunPAM&subscriberNumber="+customerInfo.getMsisdn()
                    +"&pamServiceID="+customerInfo.getPamServiceId()
                    +"&pamIndicator="+customerInfo.getPamIndicator()
                    +"&action=Send&returnFormat=json").run();

    tSpec.setResponseBody(resultInfo.getResult().toString());
    return resultInfo;
    }

    public SoivTestBase getSoivTestBase() {
    return soivTestBase;
    }

    public RunPamScheduler setSoivTestBase(SoivTestBase soivTestBase) {
        this.soivTestBase = soivTestBase;
        return this;
    }
    }