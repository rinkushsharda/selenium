package com.ericsson.soiv.teststeps.protocols.bscs.businesslogic;

import com.ericsson.jive.core.execution.TestStepBase;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpMethod;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpResponse;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.*;
import com.jayway.jsonpath.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetFreeSerialNumber extends TestStepBase<TestResultInfo> {
    private static Logger LOG = LoggerFactory.getLogger(GetFreeSerialNumber.class);
    private SoivTestBase soivTestBase = null;
    private TestResultInfo resultInfo = null;
    private CustomerInfo customerInfo = null;
    private TransactionSpecification tSpec = null;
    private ConvertXMLToJson xmlToJson = new ConvertXMLToJson();
    private String serialNumberJsonPath = "$.SOAP-ENV:Envelope.SOAP-ENV:Body.storageMediumSearchResponse.allStoragemediums.item.stmedno.content";
    private String portNumberJsonPath = "$.SOAP-ENV:Envelope.SOAP-ENV:Body.storageMediumSearchResponse.allStoragemediums.item.linkedPortNum.content";

    public GetFreeSerialNumber(String title, SoivTestBase soivTestBase, CustomerInfo customerInfo,
            TransactionSpecification tSpec, TestResultInfo resultInfo) {
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
        if (tSpec.getJsonFileName() == null) {
            SoivTestBase.fail("FAILED : Set the JSON File Name from the test Case");
            return resultInfo;
        }
        String body = RawMessages.getBodyFromJsonFilePlacedInTestStep(this.getClass().getName(), "protocols/bscs",
                tSpec.getJsonFileName());

        if ("GSM".equals(customerInfo.getSerialNumberType())) {
            body = RawMessages.replaceVal(body, "plcodePub", "EUR01");
            body = RawMessages.replaceVal(body, "submIdPub", "GSM");
            body = RawMessages.replaceVal(body, "hlCode", "1");

        } else {
            body = RawMessages.replaceVal(body, "plcodePub", customerInfo.getPlCodePub());
            body = RawMessages.replaceVal(body, "submIdPub", customerInfo.getSubmIdPub());
            body = RawMessages.replaceVal(body, "hlCode", customerInfo.getHlCode());

        }
        if (customerInfo.getSearchCount() > 1) {
            serialNumberJsonPath = "$.SOAP-ENV:Envelope.SOAP-ENV:Body.storageMediumSearchResponse.allStoragemediums.item[*].stmedno.content";
            portNumberJsonPath = "$.SOAP-ENV:Envelope.SOAP-ENV:Body.storageMediumSearchResponse.allStoragemediums.item[*].linkedPortNum.content";

        }

        body = RawMessages.replaceVal(body, "srchCount", customerInfo.getSearchCount().toString());

        resultInfo = Steps.PROTOCOLS.bscs
                .sendBscsRequest(this.getClass().getName(), resultInfo, HttpMethod.POST, 200, "/wsi/services/", body)
                .run();

        HttpResponse response = (HttpResponse) resultInfo.getResult();
        String respBody = (String) response.getBody().getValue();
        respBody = xmlToJson.convertXMLToJSON(respBody);
        Object serialNumber = JsonPath.read(respBody, serialNumberJsonPath);
        Object linkedPortNumber = JsonPath.read(respBody, portNumberJsonPath);
        customerInfo.setSerialNumber(String.valueOf(serialNumber));
        customerInfo.setLinkedPortNumber(String.valueOf(linkedPortNumber));

        LOG.info("Free Serial Number : " + serialNumber + " And Port number : " + linkedPortNumber);
        return resultInfo;
    }

    public SoivTestBase getSoivTestBase() {
        return soivTestBase;
    }

    public GetFreeSerialNumber setSoivTestBase(SoivTestBase soivTestBase) {
        this.soivTestBase = soivTestBase;
        return this;
    }

}
