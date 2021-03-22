package com.ericsson.soiv.teststeps.protocols.bscs.businesslogic;

import com.ericsson.jive.core.execution.TestStepBase;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpMethod;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpResponse;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.*;
import com.jayway.jsonpath.JsonPath;

import net.minidev.json.JSONArray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SelectLogicalResource extends TestStepBase<TestResultInfo> {
    private static Logger LOG = LoggerFactory.getLogger(SelectLogicalResource.class);
    private SoivTestBase soivTestBase = null;
    private TestResultInfo resultInfo = null;
    private CustomerInfo customerInfo = null;
    private TransactionSpecification tSpec = null;
    private ConvertXMLToJson xmlToJson = new ConvertXMLToJson();

    public SelectLogicalResource(String title, SoivTestBase soivTestBase, CustomerInfo customerInfo,
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

            body = RawMessages.replaceVal(body, "npcodePub", customerInfo.getNpCodePub());
            body = RawMessages.replaceVal(body, "plcodePub", customerInfo.getPlCodePub());
            body = RawMessages.replaceVal(body, "submIdPub", customerInfo.getSubmIdPub());
            body = RawMessages.replaceVal(body, "hlCode", customerInfo.getHlCode());
            body = RawMessages.replaceVal(body, "rsCode", customerInfo.getRsCode());
            body = RawMessages.replaceVal(body, "csControlled", customerInfo.getCsControlled());
            body = RawMessages.replaceVal(body, "searchResourceCount", String.valueOf(customerInfo.getSearchCount()));
            resultInfo = Steps.PROTOCOLS.bscs.sendBscsRequest(this.getClass().getName(), resultInfo, HttpMethod.POST,
                    200, "/wsi/services/", body).run();
            HttpResponse response = (HttpResponse) resultInfo.getResult();
            String respBody = (String) response.getBody().getValue();
            respBody = xmlToJson.convertXMLToJSON(respBody);
            
            if(customerInfo.getSearchCount()==1)
            {
                Object resource = JsonPath.read(respBody,
                        "$.SOAP-ENV:Envelope.SOAP-ENV:Body.genericDirectoryNumberSearchResponse.directorynumbers.item.dirnum.content"); 
                customerInfo.setresource(resource);
            }
            else
            {
                JSONArray resources = JsonPath.read(respBody,
                        "$.SOAP-ENV:Envelope.SOAP-ENV:Body.genericDirectoryNumberSearchResponse.directorynumbers.item[*].dirnum.content"); 
                customerInfo.setresource(resources);  
            }
            
          
            tSpec.setResponseBody(respBody);
   
        return resultInfo;
    }


    public SoivTestBase getSoivTestBase() {
        return soivTestBase;
    }

    public SelectLogicalResource setSoivTestBase(SoivTestBase soivTestBase) {
        this.soivTestBase = soivTestBase;
        return this;
    }

}
