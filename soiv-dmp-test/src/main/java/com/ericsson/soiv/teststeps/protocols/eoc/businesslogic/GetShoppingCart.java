package com.ericsson.soiv.teststeps.protocols.eoc.businesslogic;

import com.ericsson.jive.core.execution.Jive;
import com.ericsson.jive.core.execution.TestStepBase;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpMethod;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpResponse;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.CustomerInfo;
import com.ericsson.soiv.utils.TestResultInfo;
import com.ericsson.soiv.utils.TransactionSpecification;
import com.jayway.jsonpath.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetShoppingCart extends TestStepBase<TestResultInfo> {
    private static Logger LOG = LoggerFactory.getLogger(GetShoppingCart.class);
    private SoivTestBase soivTestBase = null;
    private CustomerInfo customerInfo = null;
    private TransactionSpecification tSpec = null;
    private TestResultInfo resultInfo = null;

    /**
     * This is a test step.
     * 
     * @param title
     *            The title
     * @param soivTestBase
     *            The TestBase
     * @param customerInfo
     *            The customer information object
     * @param tSpec
     *            The transaction specification object
     * @param resultInfo
     *            The result information object
     */
    public GetShoppingCart(String title, SoivTestBase soivTestBase, CustomerInfo customerInfo,
            TransactionSpecification tSpec, TestResultInfo resultInfo) {
        super(title);
        this.soivTestBase = soivTestBase;
        this.customerInfo = customerInfo;
        this.tSpec = tSpec;
        this.resultInfo = resultInfo;

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
        resultInfo = Steps.PROTOCOLS.eoc.sendEocRequest(this.getClass().getName(), resultInfo, HttpMethod.GET, 200,
                "/eoc/on/v1/sc/" + customerInfo.getShoppingCartId() + "?expand=items").run();

        HttpResponse response = (HttpResponse) resultInfo.getResult();
        String respBody = (String) response.getBody().getValue();
        tSpec.setResponseBody(respBody);
        String s = JsonPath.read(respBody, "$.items[0].item.relatedEntities[0].reference");
        if (s.contains("CUST")) {
            customerInfo.setContractId(JsonPath.read(respBody, "$.items[0].item.relatedEntities[1].reference"));
        } else if (s.contains("CONTR")) {
            customerInfo.setContractId(JsonPath.read(respBody, "$.items[0].item.relatedEntities[0].reference"));
        }

        customerInfo.setProductID((String) JsonPath.read(respBody, "$.items[0].item.product.productId"));

        Jive.log("Get Contract ID : " + customerInfo.getContractId());
        Jive.log("Get Product ID : " + customerInfo.getProductId());

        return resultInfo;
    }

    public SoivTestBase getSoivTestBase() {
        return soivTestBase;
    }

    public GetShoppingCart setSoivTestBase(SoivTestBase soivTestBase) {
        this.soivTestBase = soivTestBase;
        return this;
    }

}