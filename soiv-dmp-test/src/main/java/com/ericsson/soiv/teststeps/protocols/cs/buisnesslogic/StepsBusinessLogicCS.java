package com.ericsson.soiv.teststeps.protocols.cs.buisnesslogic;

import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.utils.CustomerInfo;
import com.ericsson.soiv.utils.TestResultInfo;
import com.ericsson.soiv.utils.TransactionSpecification;


/**
 * These steps are used for sending Raw messages against BSCS
 * So its up to the test case writer to define messages according
 * to a specific version of a protocol.
 **/
public class StepsBusinessLogicCS {

    public GetAccountBalanceAndDate getAccountBalanceAndDate(String title , SoivTestBase soivTestBase, CustomerInfo customerInfo, TransactionSpecification transactionSpecification, TestResultInfo resultInfo) {
        return new GetAccountBalanceAndDate(title, soivTestBase, customerInfo,transactionSpecification,resultInfo);
    }

    public GetDaAccountBalance getDaAccountBalance(String title , SoivTestBase soivTestBase, CustomerInfo customerInfo, TransactionSpecification transactionSpecification, TestResultInfo resultInfo) {
        return new GetDaAccountBalance(title, soivTestBase, customerInfo,transactionSpecification,resultInfo);
    }

    public GetAccountDetails getAccountDetails(String title , SoivTestBase soivTestBase, CustomerInfo customerInfo, TransactionSpecification tSpec, TestResultInfo resultInfo) {
        return new GetAccountDetails(title, soivTestBase, customerInfo,tSpec,resultInfo);
    }
    public GetThresholdAndCounters getThreshholdAndCounters(String title , SoivTestBase soivTestBase, CustomerInfo customerInfo, TransactionSpecification tSpec, TestResultInfo resultInfo) {
        return new GetThresholdAndCounters(title, soivTestBase,customerInfo,tSpec,resultInfo);
    }
    public RunUsageCounterAdjustment usageCounterAdjustment(String title , SoivTestBase soivTestBase, CustomerInfo customerInfo, TransactionSpecification tSpec, TestResultInfo resultInfo) {
        return new RunUsageCounterAdjustment(title, soivTestBase,customerInfo,tSpec,resultInfo);
    }

    public RunPamScheduler runPam(String title , SoivTestBase soivTestBase, CustomerInfo customerInfo, TransactionSpecification tSpec, TestResultInfo resultInfo) {
        return new RunPamScheduler(title, soivTestBase,customerInfo,tSpec,resultInfo);
    }
    public UpdateAccountDA updateAccountDA(String title , SoivTestBase soivTestBase, CustomerInfo customerInfo, TransactionSpecification tSpec, TestResultInfo resultInfo) {
        return new UpdateAccountDA(title, soivTestBase, customerInfo, tSpec,resultInfo);
    }
    public GetOffers getOffers(String title , SoivTestBase soivTestBase, CustomerInfo customerInfo, TransactionSpecification tSpec, TestResultInfo resultInfo) {
        return new GetOffers(title, soivTestBase, customerInfo, tSpec,resultInfo);
    }


}
