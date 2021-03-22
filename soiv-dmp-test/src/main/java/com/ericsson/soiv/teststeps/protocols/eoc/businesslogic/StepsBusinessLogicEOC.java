package com.ericsson.soiv.teststeps.protocols.eoc.businesslogic;

import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.utils.CustomerInfo;
import com.ericsson.soiv.utils.TestResultInfo;
import com.ericsson.soiv.utils.TransactionSpecification;

/**
 * These steps are used for sending Raw messages against BSCS
 * So its up to the test case writer to define messages according
 * to a specific version of a protocol.
 **/
public class StepsBusinessLogicEOC
{

    public UpdateProductOffering updateProductOffering(String title,
                                                       SoivTestBase soivTestBase,
                                                       CustomerInfo customerInfo,
                                                       TransactionSpecification tSpec,
                                                       TestResultInfo resultInfo)
    {
        return new UpdateProductOffering(title, soivTestBase, customerInfo, tSpec, resultInfo);
    }

    public AddOptionalPOonBasicPO addOptionalPOonBasicPO(String title,
                                                         SoivTestBase soivTestBase,
                                                         CustomerInfo customerInfo,
                                                         TransactionSpecification tSpec,
                                                         TestResultInfo resultInfo)
    {
        return new AddOptionalPOonBasicPO(title, soivTestBase, customerInfo, tSpec, resultInfo);
    }

    public SubmitShoppingCart submitShoppingCart(String title,
                                                 SoivTestBase soivTestBase,
                                                 CustomerInfo customerInfo,
                                                 TransactionSpecification tSpec,
                                                 TestResultInfo resultInfo)
    {
        return new SubmitShoppingCart(title, soivTestBase, customerInfo, tSpec, resultInfo);
    }

    public PresentShoppingCart presentShoppingCart(String title,
                                                   SoivTestBase soivTestBase,
                                                   CustomerInfo customerInfo,
                                                   TransactionSpecification tSpec,
                                                   TestResultInfo resultInfo)
    {
        return new PresentShoppingCart(title, soivTestBase, customerInfo, tSpec, resultInfo);
    }

    public AcceptShoppingCart
           acceptShoppingCart(String title, SoivTestBase soivTestBase, CustomerInfo customerInfo, TestResultInfo resultInfo)
    {
        return new AcceptShoppingCart(title, soivTestBase, customerInfo, resultInfo);
    }

    public AddAddOnPOonBasicPO addAddOnPOonBasicPO(String title,
                                                   SoivTestBase soivTestBase,
                                                   CustomerInfo customerInfo,
                                                   TransactionSpecification tSpec,
                                                   TestResultInfo resultInfo)
    {
        return new AddAddOnPOonBasicPO(title, soivTestBase, customerInfo, tSpec, resultInfo);
    }

    public CheckEocOrderStatus
           checkEocOrderStatus(String title, SoivTestBase soivTestBase, TransactionSpecification tSpec, TestResultInfo resultInfo)
    {
        return new CheckEocOrderStatus(title, soivTestBase, tSpec, resultInfo);
    }

    public CreateCustomerChangeOrderCart createCustomerChangeOrderCart(String title,
                                                                       SoivTestBase soivTestBase,
                                                                       CustomerInfo customerInfo,
                                                                       TransactionSpecification tSpec,
                                                                       TestResultInfo resultInfo)
    {
        return new CreateCustomerChangeOrderCart(title, soivTestBase, customerInfo, tSpec, resultInfo);
    }

    public CreateCustomerChangeOrderAdminAction createCustomerChangeOrderAdminAction(String title,
                                                                                     SoivTestBase soivTestBase,
                                                                                     CustomerInfo customerInfo,
                                                                                     TransactionSpecification tSpec,
                                                                                     TestResultInfo resultInfo)
    {
        return new CreateCustomerChangeOrderAdminAction(title, soivTestBase, customerInfo, tSpec, resultInfo);
    }

    public CreateCustomerChangeOrderAdminCharge createCustomerChangeOrderAdminCharge(String title,
                                                                                     SoivTestBase soivTestBase,
                                                                                     CustomerInfo customerInfo,
                                                                                     TransactionSpecification tSpec,
                                                                                     TestResultInfo resultInfo)
    {
        return new CreateCustomerChangeOrderAdminCharge(title, soivTestBase, customerInfo, tSpec, resultInfo);
    }

    public GetPooi getPooi(String title,
                           SoivTestBase soivTestBase,
                           CustomerInfo customerInfo,
                           TransactionSpecification tSpec,
                           TestResultInfo resultInfo)
    {
        return new GetPooi(title, soivTestBase, customerInfo, tSpec, resultInfo);
    }

    public UpdatePooi updatePooi(String title,
                                 SoivTestBase soivTestBase,
                                 CustomerInfo customerInfo,
                                 TransactionSpecification tSpec,
                                 TestResultInfo resultInfo)
    {
        return new UpdatePooi(title, soivTestBase, customerInfo, tSpec, resultInfo);
    }

    public GetShoppingCart getShoppingCart(String title,
                                           SoivTestBase soivTestBase,
                                           CustomerInfo customerInfo,
                                           TransactionSpecification tSpec,
                                           TestResultInfo resultInfo)
    {
        return new GetShoppingCart(title, soivTestBase, customerInfo, tSpec, resultInfo);
    }

    public CreateBillCycleChangeOrderRequest createBillCycleChangeOrderRequest(String title,
                                                                               SoivTestBase soivTestBase,
                                                                               CustomerInfo customerInfo,
                                                                               TransactionSpecification tSpec,
                                                                               TestResultInfo resultInfo)
    {
        return new CreateBillCycleChangeOrderRequest(title, soivTestBase, customerInfo, tSpec, resultInfo);
    }

    public UpdateOptionalProductOffering updateOptionalProductOffering(String title,
                                                                       SoivTestBase soivTestBase,
                                                                       CustomerInfo customerInfo,
                                                                       TransactionSpecification tSpec,
                                                                       TestResultInfo resultInfo)
    {
        return new UpdateOptionalProductOffering(title, soivTestBase, customerInfo, tSpec, resultInfo);
    }

    public GetResources getResources(String title,
                                     SoivTestBase soivTestBase,
                                     CustomerInfo customerInfo,
                                     TransactionSpecification tSpec,
                                     TestResultInfo resultInfo)
    {
        return new GetResources(title, soivTestBase, customerInfo, tSpec, resultInfo);
    }

    public DeleteResources deleteResources(String title,
                                           SoivTestBase soivTestBase,
                                           CustomerInfo customerInfo,
                                           TransactionSpecification tSpec,
                                           TestResultInfo resultInfo)
    {
        return new DeleteResources(title, soivTestBase, customerInfo, tSpec, resultInfo);
    }

    public AddResources addResources(String title,
                                     SoivTestBase soivTestBase,
                                     CustomerInfo customerInfo,
                                     TransactionSpecification tSpec,
                                     TestResultInfo resultInfo)
    {
        return new AddResources(title, soivTestBase, customerInfo, tSpec, resultInfo);
    }

    public AddIpv4Resources addIpv4Resources(String title,
                                             SoivTestBase soivTestBase,
                                             CustomerInfo customerInfo,
                                             TransactionSpecification tSpec,
                                             TestResultInfo resultInfo)
    {
        return new AddIpv4Resources(title, soivTestBase, customerInfo, tSpec, resultInfo);
    }

    public SwapProductOffering swapProductOffering(String title,
                                                   SoivTestBase soivTestBase,
                                                   CustomerInfo customerInfo,
                                                   TransactionSpecification tSpec,
                                                   TestResultInfo resultInfo)
    {
        return new SwapProductOffering(title, soivTestBase, customerInfo, tSpec, resultInfo);
    }

}
