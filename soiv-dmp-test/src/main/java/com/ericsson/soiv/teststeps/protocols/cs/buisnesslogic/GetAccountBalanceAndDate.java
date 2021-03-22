package com.ericsson.soiv.teststeps.protocols.cs.buisnesslogic;

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

public class GetAccountBalanceAndDate extends TestStepBase<TestResultInfo> {
    private static Logger LOG = LoggerFactory.getLogger(GetAccountBalanceAndDate.class);
    private SoivTestBase soivTestBase = null;
    private TestResultInfo resultInfo = null;
    private CustomerInfo customerInfo = null;
    private TransactionSpecification tSpec = null;

    public GetAccountBalanceAndDate(String title, SoivTestBase soivTestBase, CustomerInfo customerInfo, TransactionSpecification tSpec, TestResultInfo resultInfo) {
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

        resultInfo = Steps.PROTOCOLS.cs.sendCsRequest("GetAccountBalanceAndDate Request to Charging System", resultInfo, HttpMethod.GET, 200,
                "/csadmin/request_handler.php?"
                        +"request=GetBalanceAndDate&subscriberNumber="+customerInfo.getMsisdn()
                        +"&requestSubDedicatedAccountDetailsFlag=1&requestAttributesFlag=&requestTreeParameterSetsFlag=1"
                        +"&action=Send&returnFormat=json").run();

        HttpResponse response = (HttpResponse) resultInfo.getResult();
        String respBody = (String)response.getBody().getValue();
        tSpec.setResponseBody(respBody);
        String getAccountBalance = JsonPath.read(respBody,"$.accountValue1");
        Jive.log("Account Balance : "+getAccountBalance);
        tSpec.setAccountBalance(Integer.parseInt(getAccountBalance));
        return resultInfo;
    }

    public SoivTestBase getSoivTestBase() {
        return soivTestBase;
    }

    public GetAccountBalanceAndDate setSoivTestBase(SoivTestBase soivTestBase) {
        this.soivTestBase = soivTestBase;
        return this;
    }

}
