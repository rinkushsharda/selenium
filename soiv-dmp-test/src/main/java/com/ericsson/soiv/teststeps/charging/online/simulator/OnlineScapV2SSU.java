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

public class OnlineScapV2SSU extends TestStepBase<TestResultInfo>{
    private static Logger LOG = LoggerFactory.getLogger(OnlineScapV2SSU.class);
    private SoivTestBase soivTestBase = null;
    private TestResultInfo resultInfo = null;
    private CustomerInfo customerInfo = null;
    private TransactionSpecification tSpec = null;

    public OnlineScapV2SSU(String title, SoivTestBase soivTestBase, TestResultInfo resultInfo, CustomerInfo customerInfo, TransactionSpecification tSpec) {
        super(title);
        this.resultInfo = resultInfo;
        this.customerInfo = customerInfo;
        this.tSpec = tSpec;
        this.soivTestBase =soivTestBase;

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

        resultInfo = Steps.OnlineChargingRaw.sendRequestTrafficSim("Simulator - Online MMS Usage Over ScapV2SSU", resultInfo, HttpMethod.GET, 200,
        "/trafficsim/diameterd.php?ret_format=text&"
                +"A-Number=" +customerInfo.getMsisdn()
                +"&B-Number=" + tSpec.getCalledNumber()
                +"&Service-Identifier=" +customerInfo.getServiceIdentifier()
                +"&CC-Service-Specific-Units="+tSpec.getDuration()
                +"&A-Location=&Service-Provider-Id=&trafficcase=CIPIP/req/event&"
                +"Charging-Context-Id=SCAP_V.2.0@ericsson.com&Requested-Action=0")
                .run();

        HttpResponse response = (HttpResponse) resultInfo.getResult();
        String respBody = (String)response.getBody().getValue();

        if((respBody.contains("Cost "+tSpec.getExpectedCost())
                && respBody.contains("2001"))) {
            Jive.log("OK : Expected Cost " + tSpec.getExpectedCost()
                    + "\n Matched Successfully : " + respBody);
        }

        else if(respBody.contains(tSpec.getExpectedValidationMessage())) {
            Jive.log("OK : Expected Validation Message " + tSpec.getExpectedValidationMessage()
                    + "\n Matched Successfully : Response " + respBody);
        }

        else {
           Jive.failAndContinue("FAILED : Expected Cost " + tSpec.getExpectedCost()
                    +"or Validation Message :"+tSpec.getExpectedValidationMessage()
                    + "\n Check Simulator Response : " + respBody);
        }
        return resultInfo;
    }

    public SoivTestBase getSoivTestBase() {
        return soivTestBase;
    }

    public OnlineScapV2SSU setSoivTestBase(SoivTestBase soivTestBase) {
        this.soivTestBase = soivTestBase;
        return this;
    }

}

