
package com.ericsson.soiv.teststeps.charging.online.simulator;

import com.ericsson.jive.core.execution.TestStepBase;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpMethod;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpResponse;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.CustomerInfo;
import com.ericsson.soiv.utils.TestResultInfo;
import com.ericsson.soiv.utils.TransactionSpecification;
import com.jayway.jsonpath.JsonPath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InteractiveVoice extends TestStepBase<TestResultInfo> {
    private static Logger LOG = LoggerFactory.getLogger(OnlineCip.class);
    private SoivTestBase soivTestBase = null;
    private TestResultInfo resultInfo = null;
    private CustomerInfo customerInfo = null;
    private TransactionSpecification tSpec = null;

    public InteractiveVoice(String title, SoivTestBase soivTestBase, TestResultInfo resultInfo,
            CustomerInfo customerInfo, TransactionSpecification tSpec) {
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
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        resultInfo = Steps.OnlineChargingRaw.sendRequestTrafficSim("Simulator - Start Interactive Voice Usage",
                resultInfo, HttpMethod.GET, 200,
                "/trafficsim/diameterd.php?ret_format=json&trafficcase=CIPIP/REQUEST/VOICE&A-Number="
                        + customerInfo.getMsisdn() + "&B-Number=" + tSpec.getCalledNumber() + "&Service-Identifier="
                        + tSpec.getserviceIdentifier() + "&Start-Time=" + tSpec.getCallStartTime())
                .run();

        HttpResponse response = (HttpResponse) resultInfo.getResult();
        String respBody = (String) response.getBody().getValue();

        String sessionId = JsonPath.read(respBody, "$.Session-Id");
        resultInfo = Steps.OnlineChargingRaw.sendRequestTrafficSim("Simulator - Terminate Interactive Voice Usage",
                resultInfo, HttpMethod.GET, 200,
                "/trafficsim/diameterd.php?ret_format=json&trafficcase=CIPIP/REQUEST/VOICE/TERMINATE&A-Number="
                        + customerInfo.getMsisdn() + "&B-Number=" + tSpec.getCalledNumber() + "&Service-Identifier="
                        + tSpec.getserviceIdentifier() + "&Session-Id=" + sessionId + "&CC-Time=" + tSpec.getDuration())
                .run();

        response = (HttpResponse) resultInfo.getResult();
        respBody = (String) response.getBody().getValue();

        return resultInfo;
    }

    public SoivTestBase getSoivTestBase() {
        return soivTestBase;
    }

    public InteractiveVoice setSoivTestBase(SoivTestBase soivTestBase) {
        this.soivTestBase = soivTestBase;
        return this;
    }

}
