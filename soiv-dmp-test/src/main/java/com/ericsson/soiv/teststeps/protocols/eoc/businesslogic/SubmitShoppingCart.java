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

public class SubmitShoppingCart extends TestStepBase<TestResultInfo> {
    private static Logger LOG = LoggerFactory.getLogger(SubmitShoppingCart.class);
    private SoivTestBase soivTestBase = null;
    private TestResultInfo resultInfo = null;
    private CustomerInfo customerInfo = null;
    private TransactionSpecification tSpec = null;

    public SubmitShoppingCart(String title, SoivTestBase soivTestBase, CustomerInfo customerInfo, TransactionSpecification tSpec, TestResultInfo resultInfo) {
        super(title);
        this.resultInfo = resultInfo;
        this.customerInfo = customerInfo;
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

        if("INTERACTIVE".equals(customerInfo.getOrderMode())) {
            Steps.PROTOCOLS.businessLogicEOC.presentShoppingCart("Provision : Present Shopping Cart", soivTestBase, customerInfo, tSpec, resultInfo)
                    .run();
        }

        Steps.PROTOCOLS.businessLogicEOC.acceptShoppingCart("Provision : Accept Shopping Cart", soivTestBase, customerInfo, resultInfo)
                .run();

        resultInfo = Steps.PROTOCOLS.eoc.sendEocRequest("Provision : Submit Shopping Cart", resultInfo, HttpMethod.POST, 200,
                    "/eoc/on/v1/sc/" + customerInfo.getShoppingCartId() + "/submit").run();

        HttpResponse response = (HttpResponse) resultInfo.getResult();
        String respBody = (String)response.getBody().getValue();
        String orderId = JsonPath.read(respBody,"$.quoteID");
        Jive.log("EOC Order Id : "+orderId);

        tSpec.setEocOrderId(orderId);
        resultInfo = Steps.PROTOCOLS.businessLogicEOC.checkEocOrderStatus("Check Order Status", soivTestBase,tSpec,resultInfo).run();
        return resultInfo;
    }

    public SoivTestBase getSoivTestBase() {
        return soivTestBase;
    }

    public SubmitShoppingCart setSoivTestBase(SoivTestBase soivTestBase) {
        this.soivTestBase = soivTestBase;
        return this;
    }

}
