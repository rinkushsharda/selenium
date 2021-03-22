package com.ericsson.soiv.teststeps.charging.online.simulator;

import com.ericsson.jive.core.execution.TestStepBase;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpMethod;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpResponse;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.CustomerInfo;
import com.ericsson.soiv.utils.TestResultInfo;
import com.ericsson.soiv.utils.TransactionSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnlineGx extends TestStepBase<TestResultInfo> {
    private static Logger LOG = LoggerFactory.getLogger(OnlineGx.class);
    private SoivTestBase soivTestBase = null;
    private TestResultInfo resultInfo = null;
    private CustomerInfo customerInfo = null;
    private TransactionSpecification tSpec = null;

    public OnlineGx(String title, SoivTestBase soivTestBase, TestResultInfo resultInfo, CustomerInfo customerInfo,
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

        resultInfo = Steps.OnlineChargingRaw
                .sendRequestTrafficSim("Simulator - Online Data Usage", resultInfo, HttpMethod.GET, 200,
                        "/trafficsim/diameterd.php?ret_format=json&trafficcase=CIPIP/REQUEST/GX&A-Number="
                                + customerInfo.getMsisdn() + "&3GPP-RAT-Type=01" + "&Rating-Group="
                                + tSpec.getRatingGroup() + "&policy_type=Gx&unit_type=volume&proto_type=CIPIP")
                .run();
        HttpResponse response = (HttpResponse) resultInfo.getResult();
        String respBody = (String)response.getBody().getValue();
        resultInfo.setResult(respBody);

        return resultInfo;
    }

    public SoivTestBase getSoivTestBase() {
        return soivTestBase;
    }

    public OnlineGx setSoivTestBase(SoivTestBase soivTestBase) {
        this.soivTestBase = soivTestBase;
        return this;
    }

}
