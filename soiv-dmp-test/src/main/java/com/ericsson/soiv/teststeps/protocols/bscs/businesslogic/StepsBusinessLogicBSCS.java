package com.ericsson.soiv.teststeps.protocols.bscs.businesslogic;

import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.utils.CustomerInfo;
import com.ericsson.soiv.utils.TestResultInfo;
import com.ericsson.soiv.utils.TransactionSpecification;

/**
 * These steps are used for sending Raw messages against BSCS So its up to the
 * test case writer to define messages according to a specific version of a
 * protocol.
 **/
public class StepsBusinessLogicBSCS
{

    public GetFreeSerialNumber getFreeSerialNumber(String title,
                                                   SoivTestBase soivTestBase,
                                                   CustomerInfo customerInfo,
                                                   TransactionSpecification tSpec,
                                                   TestResultInfo resultInfo)
    {
        return new GetFreeSerialNumber(title, soivTestBase, customerInfo, tSpec, resultInfo);
    }

    public SelectLogicalResource selectLogicalResource(String title,
                                                       SoivTestBase soivTestBase,
                                                       CustomerInfo customerInfo,
                                                       TransactionSpecification tSpec,
                                                       TestResultInfo resultInfo)
    {
        return new SelectLogicalResource(title, soivTestBase, customerInfo, tSpec, resultInfo);
    }

    public ReadContract readContract(String title,
                                     SoivTestBase soivTestBase,
                                     CustomerInfo customerInfo,
                                     TestResultInfo resultInfo)
    {
        return new ReadContract(title, soivTestBase, customerInfo, resultInfo);
    }

    public GetPoStatusBSCS getPoStatusBSCS(String title,
                                           SoivTestBase soivTestBase,
                                           CustomerInfo customerInfo,
                                           TransactionSpecification tSpec,
                                           TestResultInfo resultInfo)
    {
        return new GetPoStatusBSCS(title, soivTestBase, customerInfo, tSpec, resultInfo);
    }

    public GetContractHistory getContractHistory(String title,
                                                 SoivTestBase soivTestBase,
                                                 CustomerInfo customerInfo,
                                                 TransactionSpecification tSpec,
                                                 TestResultInfo resultInfo)
    {
        return new GetContractHistory(title, soivTestBase, customerInfo, tSpec, resultInfo);
    }

    public ReadCustomer readCustomer(String title,
                                     SoivTestBase soivTestBase,
                                     CustomerInfo customerInfo,
                                     TransactionSpecification tSpec,
                                     TestResultInfo resultInfo)
    {
        return new ReadCustomer(title, soivTestBase, customerInfo, tSpec, resultInfo);
    }

    public PortSearch portSearch(String title,
                                 SoivTestBase soivTestBase,
                                 CustomerInfo customerInfo,
                                 TransactionSpecification tSpec,
                                 TestResultInfo resultInfo)
    {
        return new PortSearch(title, soivTestBase, customerInfo, tSpec, resultInfo);
    }

    public GetAccountBalance getAccountBalance(String title,
                                               SoivTestBase soivTestBase,
                                               CustomerInfo customerInfo,
                                               TransactionSpecification tSpec,
                                               TestResultInfo resultInfo)
    {
        return new GetAccountBalance(title, soivTestBase, customerInfo, tSpec, resultInfo);
    }

    public GetUsageEvent getUsageEvent(String title,
                                       SoivTestBase soivTestBase,
                                       CustomerInfo customerInfo,
                                       TransactionSpecification tSpec,
                                       TestResultInfo resultInfo)
    {
        return new GetUsageEvent(title, soivTestBase, customerInfo, tSpec, resultInfo);
    }

}
