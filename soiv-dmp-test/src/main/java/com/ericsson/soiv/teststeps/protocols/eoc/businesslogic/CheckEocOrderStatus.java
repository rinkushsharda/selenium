package com.ericsson.soiv.teststeps.protocols.eoc.businesslogic;

import com.ericsson.jive.core.execution.Jive;
import com.ericsson.jive.core.execution.TestStepBase;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpMethod;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpResponse;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.*;
import com.jayway.jsonpath.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckEocOrderStatus extends TestStepBase<TestResultInfo> {
    private static Logger LOG = LoggerFactory.getLogger(CheckEocOrderStatus.class);
    private SoivTestBase soivTestBase = null;
    private TestResultInfo resultInfo = null;
    private TransactionSpecification tSpec = null;

    public CheckEocOrderStatus(String title, SoivTestBase soivTestBase, TransactionSpecification tSpec, TestResultInfo resultInfo){
        super(title);
        this.resultInfo = resultInfo;
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

        String respBody = null;
        HttpResponse response;
        String orderStatus = null;

        for (int count = 1 ; count<=6 ; count ++) {
            resultInfo = Steps.PROTOCOLS.eoc.sendEocRequest("Get Order Status from EOC Retry Count : "+count, resultInfo, HttpMethod.GET, 200,
                    "/eoc/om/v1/order/" + tSpec.getEocOrderId()).run();

            response = (HttpResponse) resultInfo.getResult();
            respBody = (String) response.getBody().getValue();
            orderStatus = JsonPath.read(respBody, "$.state");
            if ("CLOSED.COMPLETED".equals(orderStatus)) {
                Jive.log("OK : Order Status Now CLOSED.COMPLETED After Retry Count "+ count +" With OrderId : "+tSpec.getEocOrderId());
                break;
            }
            Jive.log("INFO : Check Order Processing on EOC... Current Status is "+orderStatus+" And Retry Count : " + count + " With Sleep of 30 Seconds");
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (!"CLOSED.COMPLETED".equals(orderStatus)) {
            Jive.fail("FAILED : Order Status is not CLOSED.COMPLETED Yet , Kindly Check on EOC GUI For OrderId : "+tSpec.getEocOrderId());
        }
        return resultInfo;
    }

    public SoivTestBase getSoivTestBase() {
        return soivTestBase;
    }

    public CheckEocOrderStatus setSoivTestBase(SoivTestBase soivTestBase) {
        this.soivTestBase = soivTestBase;
        return this;
    }

}
