package com.ericsson.soiv.teststeps.charging.online.raw;

import com.ericsson.jive.core.protocols.http.messagetypes.HttpMethod;
import com.ericsson.soiv.utils.TestResultInfo;


/**
 * These steps are used for sending Raw messages against BSCS
 * So its up to the test case writer to define messages according
 * to a specific version of a protocol.
 **/
public class StepsChargingRaw {

    public CipIpSimulatorRaw sendRequestTrafficSim(String title, TestResultInfo resultInfo, HttpMethod httpMethod, int statusCode,
                                                   String uri) {
        return new CipIpSimulatorRaw(title,resultInfo,httpMethod,statusCode,uri);
    }



}
