package com.ericsson.soiv.teststeps.protocols.cs.buisnesslogic;

import com.ericsson.jive.core.execution.TestStepBase;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpMethod;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpResponse;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.CustomerInfo;
import com.ericsson.soiv.utils.TestResultInfo;
import com.ericsson.soiv.utils.TransactionSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateAccountDA extends TestStepBase<TestResultInfo> {
    private static Logger LOG = LoggerFactory.getLogger(UpdateAccountDA.class);
    private SoivTestBase soivTestBase = null;
    private TestResultInfo resultInfo = null;
    private CustomerInfo customerInfo = null;
    private TransactionSpecification tSpec = null;


    public UpdateAccountDA(String title, SoivTestBase soivTestBase, CustomerInfo customerInfo, TransactionSpecification tSpec, TestResultInfo resultInfo) {
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

        if (customerInfo.getProductId() == null) {
            resultInfo = Steps.PROTOCOLS.cs.sendCsRequest("GetAccountDetails Request to Charging System", resultInfo, HttpMethod.GET, 200,
                    "/csadmin/request_handler.php?" +
                            "request=UpdateDA&subscriberNumber="
                            + customerInfo.getMsisdn()
                            + "&transactionCurrency=EUR&dedicatedAccountID=" + tSpec.getDedicatedAccountID()
                            + "&adjustmentAmountRelative=&dedicatedAccountValueNew=" + tSpec.getDedicatedAcountValue()
                            + "&dedicatedAccountUnitType=1&adjustmentDateRelative=&expiryDate="
                            + "&expiryPamPeriodIndicator=&startDate=&adjustmentStartDateRelative="
                            + "&startPamPeriodIndicator=&pamServiceID=&productID="
                            + "&transactionType="
                            + "&transactionCode=&action=Send&returnFormat=json").run();
        }
        else
        {
            resultInfo = Steps.PROTOCOLS.cs.sendCsRequest("GetAccountDetails Request to Charging System", resultInfo, HttpMethod.GET, 200,
                "/csadmin/request_handler.php?" +
                        "request=UpdateDA&subscriberNumber="
                        + customerInfo.getMsisdn()
                        + "&transactionCurrency=EUR&dedicatedAccountID=" + tSpec.getDedicatedAccountID()
                        + "&adjustmentAmountRelative=&dedicatedAccountValueNew=" + tSpec.getDedicatedAcountValue()
                        + "&dedicatedAccountUnitType=1&adjustmentDateRelative=&expiryDate="
                        + "&expiryPamPeriodIndicator=&startDate=&adjustmentStartDateRelative="
                        + "&startPamPeriodIndicator=&pamServiceID=&productID=" + customerInfo.getProductId()
                        + "&transactionType="
                        + "&transactionCode=&action=Send&returnFormat=json").run();

        }
        HttpResponse response = (HttpResponse) resultInfo.getResult();
        String responseBody = (String) response.getBody().getValue();
        tSpec.setResponseBody(responseBody);

        return resultInfo;
    }

    public SoivTestBase getSoivTestBase() {
        return soivTestBase;
    }

    public UpdateAccountDA setSoivTestBase(SoivTestBase soivTestBase) {
        this.soivTestBase = soivTestBase;
        return this;
    }

}
