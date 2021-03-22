package com.ericsson.soiv.teststeps.provision.eoc;

import com.ericsson.jive.core.execution.Jive;
import com.ericsson.jive.core.execution.TestStepBase;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpResponse;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.*;
import com.jayway.jsonpath.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManageContractStatus extends TestStepBase<TestResultInfo> {
    private static Logger LOG = LoggerFactory.getLogger(ManageContractStatus.class);
    private SoivTestBase soivTestBase = null;
    private CustomerInfo customerInfo = null;
    private TransactionSpecification tSpec = null;
    private TestResultInfo resultInfo = null;
    private ConvertXMLToJson xmlToJson = new ConvertXMLToJson();

    /**
     * This is a test step.
     *  @param title The title
     * @param soivTestBase The TestBase
     * @param customerInfo The customer information object
     * @param tSpec The transaction specification object
     * @param resultInfo The result information object
     */
    public ManageContractStatus(String title, SoivTestBase soivTestBase, CustomerInfo customerInfo,
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

        resultInfo = Steps.PROVISION.eoc.getShoppingCart("Manage Contract : Get Shopping Cart", soivTestBase,customerInfo,tSpec,resultInfo)
                .run();

        tSpec.setJsonFileName("createShoppingCart.json");
        resultInfo = Steps.PROVISION.eoc.createShoppingCart("Manage Contract : Create Shopping Cart", soivTestBase, customerInfo, tSpec, resultInfo).run();

        if(tSpec.getIsAdminChargesEnabled()){
           resultInfo = Steps.PROTOCOLS.businessLogicEOC.createCustomerChangeOrderAdminCharge("Protocol : Change Order Admin Charges", soivTestBase
                    ,customerInfo,tSpec,resultInfo).run();

           resultInfo = Steps.PROTOCOLS.businessLogicEOC.createCustomerChangeOrderAdminAction("Protocol : Change Order Admin Action", soivTestBase
                    ,customerInfo,tSpec,resultInfo).run();
        }

        else {
            resultInfo = Steps.PROTOCOLS.businessLogicEOC.createCustomerChangeOrderCart("Manage Contract : CCOI", soivTestBase,customerInfo,tSpec,resultInfo)
                    .run();
        }

        resultInfo = Steps.PROTOCOLS.businessLogicEOC.submitShoppingCart("Manage Contract : Submit Shopping Cart", soivTestBase,customerInfo,tSpec,resultInfo)
                .run();

        resultInfo = Steps.PROTOCOLS.businessLogicBSCS.readContract("Read Contract Status",soivTestBase,customerInfo,resultInfo)
                .run();

        // Validate Contract Status Code and Reason code after updated

        HttpResponse response = (HttpResponse) resultInfo.getResult();
        String respBody = (String)response.getBody().getValue();
        respBody = xmlToJson.convertXMLToJSON(respBody);

        Integer contractStatusCode = JsonPath.read(respBody,"$.SOAP-ENV:Envelope.SOAP-ENV:Body.contractReadResponse.coStatus.content");
        Integer contractReasonCode = JsonPath.read(respBody,"$.SOAP-ENV:Envelope.SOAP-ENV:Body.contractReadResponse.coLastReason.content");

        if(tSpec.getTargetState().equals(contractStatusCode.toString())
                    && tSpec.getReasonCode().equals(contractReasonCode.toString())){
            Jive.log("OK : Contract Is Updated Successfully!"+
                    " ContractStatusCode : "+contractStatusCode+
                    " & ContractReasonCode : "+contractReasonCode);
        }
        else  {
            Jive.fail("FAILED : Contract Status Code : "+ contractStatusCode
                    +" and Reason Code : "+contractReasonCode +
                    " is not Matched with Expected status Code : "+ tSpec.getTargetState()
                    +" and Reason Code : " +tSpec.getReasonCode());
        }

        return resultInfo;
    }

    public SoivTestBase getSoivTestBase() {
        return soivTestBase;
    }

    public ManageContractStatus setSoivTestBase(SoivTestBase soivTestBase) {
        this.soivTestBase = soivTestBase;
        return this;
    }

    public CustomerInfo getCustomerInfo() {
        return customerInfo;
    }

    public ManageContractStatus setCustomerInfo(CustomerInfo customerInfo) {
        this.customerInfo = customerInfo;
        return this;
    }

    public TransactionSpecification getCallInfo() {
        return tSpec;
    }

    public ManageContractStatus setTransactionSpec(TransactionSpecification tSpec) {
        this.tSpec = tSpec;
        return this;
    }

    public ManageContractStatus setName(String name) {
        setTitle(name);
        return this;
    }

}
