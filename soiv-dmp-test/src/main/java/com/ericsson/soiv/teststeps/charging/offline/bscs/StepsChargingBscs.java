package com.ericsson.soiv.teststeps.charging.offline.bscs;

import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.utils.CustomerInfo;
import com.ericsson.soiv.utils.TestResultInfo;
import com.ericsson.soiv.utils.TransactionSpecification;


/**
 * These steps are used for sending Raw messages against BSCS
 * So its up to the test case writer to define messages according
 * to a specific version of a protocol.
 **/
public class StepsChargingBscs {

    public OfflineGy offlineGy(String title, SoivTestBase soivTestBase, TestResultInfo resultInfo, CustomerInfo customerInfo, TransactionSpecification transactionSpecification) {
        return new OfflineGy(title, soivTestBase, resultInfo, customerInfo,transactionSpecification);
    }

    public OfflineVoice offlineVoice(String title, SoivTestBase soivTestBase, TestResultInfo resultInfo, CustomerInfo customerInfo, TransactionSpecification transactionSpecification) {
        return new OfflineVoice(title, soivTestBase, resultInfo, customerInfo,transactionSpecification);
    }

    public OfflineSms offlineSms(String title, SoivTestBase soivTestBase, TestResultInfo resultInfo, CustomerInfo customerInfo, TransactionSpecification transactionSpecification) {
        return new OfflineSms(title, soivTestBase, resultInfo, customerInfo,transactionSpecification);
    }

}
