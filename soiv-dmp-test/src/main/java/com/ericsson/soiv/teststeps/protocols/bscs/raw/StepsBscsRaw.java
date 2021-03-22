package com.ericsson.soiv.teststeps.protocols.bscs.raw;

import com.ericsson.jive.core.protocols.http.messagetypes.HttpMethod;
import com.ericsson.soiv.utils.TestResultInfo;


/**
 * These steps are used for sending Raw messages against BSCS
 * So its up to the test case writer to define messages according
 * to a specific version of a protocol.
 **/
public class StepsBscsRaw {

    public BscsRaw sendBscsRequest(String title, TestResultInfo resultInfo, HttpMethod httpMethod, int statusCode,
                                   String uri) {
        return new BscsRaw (title, resultInfo, httpMethod, statusCode, uri);
    }

    public BscsRaw sendBscsRequest(String title, TestResultInfo resultInfo, HttpMethod httpMethod, int statusCode,
                                   String uri, String body) {
        return new BscsRaw(title, resultInfo, httpMethod, statusCode, uri, body);
    }

}
