package com.ericsson.soiv.teststeps.charging.online.simulator;

import com.ericsson.jive.core.execution.Jive;
import com.ericsson.jive.core.execution.TestStepBase;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpMethod;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpResponse;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnlineGy extends TestStepBase<TestResultInfo>
{
    private static Logger LOG = LoggerFactory.getLogger(OnlineGy.class);
    private SoivTestBase soivTestBase = null;
    private TestResultInfo resultInfo = null;
    private CustomerInfo customerInfo = null;
    private TransactionSpecification tSpec = null;

    public OnlineGy(String title,
                    SoivTestBase soivTestBase,
                    TestResultInfo resultInfo,
                    CustomerInfo customerInfo,
                    TransactionSpecification tSpec)
    {
        super(title);
        this.resultInfo = resultInfo;
        this.customerInfo = customerInfo;
        this.tSpec = tSpec;
        this.soivTestBase = soivTestBase;

        if (!resultInfo.getShouldContinue())
        {
            LOG.error(title + " - Not executed due to previous error");
        }
    }

    @Override
    public TestResultInfo execute()
    {
        if (!resultInfo.getShouldContinue())
        {
            SoivTestBase.failAndContinue("Step not executed due to a previous error");
            return resultInfo;
        }
        LOG.info("Step title: " + getTitle());

        if (tSpec.getDuration() == -1)
        {

            resultInfo = Steps.OnlineChargingRaw.sendRequestTrafficSim("Simulator - Online Data Usage", resultInfo, HttpMethod.GET, 200,
                    "/trafficsim/diameterd.php?ret_format=text&A-Number=" + customerInfo.getMsisdn()
                            + "&CC-Total-Octets="
                            + "&Rating-Group=" + tSpec.getRatingGroup()
                            + "&3GPP-RAT-Type=01&User-Equipment-IMEI=&trafficcase=CIPIP%2Fdata")
                    .run();
        }
        else
        {
            resultInfo = Steps.OnlineChargingRaw.sendRequestTrafficSim("Simulator - Online Data Usage", resultInfo, HttpMethod.GET, 200,
                    "/trafficsim/diameterd.php?ret_format=text&A-Number=" + customerInfo.getMsisdn()
                            + "&CC-Total-Octets=" + tSpec.getDuration()
                            + "&Rating-Group=" + tSpec.getRatingGroup()
                            + "&3GPP-RAT-Type=01&User-Equipment-IMEI=&trafficcase=CIPIP%2Fdata")
                    .run();
        }

        HttpResponse response = (HttpResponse) resultInfo.getResult();
        String respBody = (String) response.getBody().getValue();

        if ((respBody.contains("Cost " + tSpec.getExpectedCost())
                && respBody.contains("2001")))
        {
            Jive.log("OK : Expected Cost " + tSpec.getExpectedCost()
                    + "\n Matched Successfully : " + respBody);
        }

        else if (respBody.contains(tSpec.getExpectedValidationMessage()))
        {
            Jive.log("OK : Expected Validation Message " + tSpec.getExpectedValidationMessage()
                    + "\n Matched Successfully : Response " + respBody);
        }

        else
        {
           Jive.failAndContinue("FAILED : Expected Cost " + tSpec.getExpectedCost()
                    + "or Validation Message :" + tSpec.getExpectedValidationMessage()
                    + "\n Check Simulator Response : " + respBody);
        }

        return resultInfo;
    }

    public SoivTestBase getSoivTestBase()
    {
        return soivTestBase;
    }

    public OnlineGy setSoivTestBase(SoivTestBase soivTestBase)
    {
        this.soivTestBase = soivTestBase;
        return this;
    }

}
