package com.ericsson.soiv.teststeps.provision.sdp;

import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.utils.CustomerInfo;
import com.ericsson.soiv.utils.TestResultInfo;
import com.ericsson.soiv.utils.TransactionSpecification;

public class StepsSDP {
    /**
     * This is a test step.
     * 
     * @param title The title
     * @param soivTestBase The TestBase
     * @param customerInfo The customer information object
     * @param resultInfo The result object
     * @param tSpec Transaction Specification
     * @return CreateShoppingCart
     */

  public TraceOnSdp traceOnSdp(String title, SoivTestBase soivTestBase, CustomerInfo customerInfo, TransactionSpecification tSpec,
                                                     TestResultInfo resultInfo) {
        return new TraceOnSdp(title,resultInfo,tSpec,customerInfo);
    }

}
