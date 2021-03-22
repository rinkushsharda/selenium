package com.ericsson.soiv.teststeps.charging.offline.raw;

import com.ericsson.soiv.utils.CustomerInfo;
import com.ericsson.soiv.utils.TestResultInfo;
import com.ericsson.soiv.utils.TransactionSpecification;


/**
 * These steps are used for sending Raw messages against BSCS
 * So its up to the test case writer to define messages according
 * to a specific version of a protocol.
 **/

public class StepsChargingRawOffline {

    public GenerateOfflineUdr createOfflineUdr(String title, TestResultInfo resultInfo, TransactionSpecification transactionSpecification, CustomerInfo customerInfo)
    {
         return new GenerateOfflineUdr(title,resultInfo,transactionSpecification,customerInfo);
    }
}
