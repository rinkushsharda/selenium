package com.ericsson.soiv.teststeps.charging.online.simulator;

import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.utils.CustomerInfo;
import com.ericsson.soiv.utils.TestResultInfo;
import com.ericsson.soiv.utils.TransactionSpecification;

/**
 * These steps are used for sending Raw messages against BSCS
 * So its up to the test case writer to define messages according
 * to a specific version of a protocol.
 **/
public class StepsChargingGy
{

    public DccGyData dccGyData(String title,
                               SoivTestBase soivTestBase,
                               TestResultInfo resultInfo,
                               CustomerInfo customerInfo,
                               TransactionSpecification transactionSpecification)
    {
        return new DccGyData(title, soivTestBase, resultInfo, customerInfo, transactionSpecification);
    }

    public OnlineGy onlineGy(String title,
                             SoivTestBase soivTestBase,
                             TestResultInfo resultInfo,
                             CustomerInfo customerInfo,
                             TransactionSpecification transactionSpecification)
    {
        return new OnlineGy(title, soivTestBase, resultInfo, customerInfo, transactionSpecification);
    }

    public OnlineCip onlineCip(String title,
                               SoivTestBase soivTestBase,
                               TestResultInfo resultInfo,
                               CustomerInfo customerInfo,
                               TransactionSpecification transactionSpecification)
    {
        return new OnlineCip(title, soivTestBase, resultInfo, customerInfo, transactionSpecification);
    }

    public OnlineSms onlineSms(String title,
                               SoivTestBase soivTestBase,
                               TestResultInfo resultInfo,
                               CustomerInfo customerInfo,
                               TransactionSpecification transactionSpecification)
    {
        return new OnlineSms(title, soivTestBase, resultInfo, customerInfo, transactionSpecification);
    }

    public OnlineGx onlineGx(String title,
                             SoivTestBase soivTestBase,
                             TestResultInfo resultInfo,
                             CustomerInfo customerInfo,
                             TransactionSpecification transactionSpecification)
    {
        return new OnlineGx(title, soivTestBase, resultInfo, customerInfo, transactionSpecification);
    }

    public OnlineCipWithValidation onlineCipWithValidation(String title,
                                                           SoivTestBase cbioTestBase,
                                                           TestResultInfo resultInfo,
                                                           CustomerInfo customerInfo,
                                                           TransactionSpecification transactionSpecification)
    {
        return new OnlineCipWithValidation(title, cbioTestBase, resultInfo, customerInfo, transactionSpecification);
    }

    public OnlineScapV2SSU onlineScapV2SSU(String title,
                                           SoivTestBase cbioTestBase,
                                           TestResultInfo resultInfo,
                                           CustomerInfo customerInfo,
                                           TransactionSpecification transactionSpecification)
    {
        return new OnlineScapV2SSU(title, cbioTestBase, resultInfo, customerInfo, transactionSpecification);
    }
    
    public InteractiveVoice interactiveVoice(String title,
            SoivTestBase soivTestBase,
            TestResultInfo resultInfo,
            CustomerInfo customerInfo,
            TransactionSpecification transactionSpecification)
{
return new InteractiveVoice(title, soivTestBase, resultInfo, customerInfo, transactionSpecification);
}
}
