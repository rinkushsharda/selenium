package com.ericsson.soiv.teststeps.protocols.air.raw;

import com.ericsson.jive.core.protocols.http.messagetypes.HttpMethod;
import com.ericsson.soiv.utils.TestResultInfo;

/**
 * These steps are used for sending Raw messages against AIR So its up to the
 * test case writer to define messages according to a specific version of a
 * protocol.
 **/
public class StepsAirRaw {

	public AirRaw sendAirRequest(String title, TestResultInfo resultInfo, HttpMethod httpMethod, int statusCode,
			String uri) {
		return new AirRaw(title, resultInfo, httpMethod, statusCode, uri);
	}

	public AirRaw sendAirRequest(String title, TestResultInfo resultInfo, HttpMethod httpMethod, int statusCode,
			String uri, String body) {
		return new AirRaw(title, resultInfo, httpMethod, statusCode, uri, body);
	}

}
