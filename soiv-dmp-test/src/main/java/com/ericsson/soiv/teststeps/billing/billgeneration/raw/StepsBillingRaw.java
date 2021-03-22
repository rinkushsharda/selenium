package com.ericsson.soiv.teststeps.billing.billgeneration.raw;

import com.ericsson.jive.core.protocols.http.messagetypes.HttpMethod;
import com.ericsson.soiv.utils.TestResultInfo;


/**
 * These steps are used for sending Raw messages against BSCS
 * So its up to the test case writer to define messages according
 * to a specific version of a protocol.
 **/
public class StepsBillingRaw {

    public BillingRaw sendBillingRequest(String title, TestResultInfo resultInfo, HttpMethod httpMethod, int statusCode,
                                                                                 String uri) {
        return new BillingRaw(title,resultInfo,httpMethod,statusCode,uri);
    }

    public BillingRaw sendBillingRequest(String title, TestResultInfo resultInfo, HttpMethod httpMethod, int statusCode,
                                                                                 String uri, String body) {
        return new BillingRaw(title, resultInfo, httpMethod, statusCode, uri, body);
    }

}
