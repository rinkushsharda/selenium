package com.ericsson.soiv.teststeps.charging.online.simulator;

import com.ericsson.jive.core.execution.Jive;
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

/*This method is used to check the Message which comes after Making call,
 that can be an error message or some other Message*/

public class OnlineCipWithValidation extends TestStepBase<TestResultInfo>{
    private static Logger LOG = LoggerFactory.getLogger(OnlineCipWithValidation.class);
    private SoivTestBase soivTestBase = null;
    private TestResultInfo resultInfo = null;
    private CustomerInfo customerInfo = null;
    private TransactionSpecification tSpec = null;

    public OnlineCipWithValidation(String title, SoivTestBase soivTestBase, TestResultInfo resultInfo, CustomerInfo customerInfo, TransactionSpecification tSpec) {
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

        resultInfo = Steps.OnlineChargingRaw.sendRequestTrafficSim("Simulator - Online Voice Usage", resultInfo, HttpMethod.GET, 200,
                "/trafficsim/diameterd.php?ret_format=text&A-Number="
                        +customerInfo.getMsisdn()
                        +"&B-Number="+tSpec.getCalledNumber()
                        +"&CC-Time="+tSpec.getDuration()+"&Other-Party-Id-MNP-Result="
                        +"&Other-Party-Id-MNP-RN=&Other-Party-Id-MNP-IMSI="
                        +"&trafficcase=CIPIP%2Fvoice")
                .run();

        HttpResponse response = (HttpResponse) resultInfo.getResult();
        String respBody = (String)response.getBody().getValue();

        if(!(respBody.contains(tSpec.getCallMessage())))
        {
            Jive.fail("FAILED : Not Getting Expected Message After Voice Call");
        }

        return resultInfo;
    }

    public SoivTestBase getSoivTestBase() {
        return soivTestBase;
    }

    public OnlineCipWithValidation setSoivTestBase(SoivTestBase soivTestBase) {
        this.soivTestBase = soivTestBase;
        return this;
    }

}

