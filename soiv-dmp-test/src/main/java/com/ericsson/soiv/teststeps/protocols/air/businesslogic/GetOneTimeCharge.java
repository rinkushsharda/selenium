package com.ericsson.soiv.teststeps.protocols.air.businesslogic;

import com.ericsson.jive.core.execution.TestStepBase;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpMethod;
import com.ericsson.jive.core.protocols.http.messagetypes.HttpResponse;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.teststeps.Steps;
import com.ericsson.soiv.utils.ConvertXMLToJson;
import com.ericsson.soiv.utils.CustomerInfo;
import com.ericsson.soiv.utils.RawMessages;
import com.ericsson.soiv.utils.TestResultInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetOneTimeCharge extends TestStepBase<TestResultInfo> {

	private static Logger LOG = LoggerFactory.getLogger(GetOneTimeCharge.class);
	private SoivTestBase soivTestBase = null;
	private TestResultInfo resultInfo = null;
	private CustomerInfo customerInfo = null;
	private ConvertXMLToJson xmlToJson = new ConvertXMLToJson();

	public GetOneTimeCharge(String title, SoivTestBase soivTestBase, CustomerInfo customerInfo
			,TestResultInfo resultInfo) {
		super(title);
		this.resultInfo = resultInfo;
		this.customerInfo = customerInfo;
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

		String body = RawMessages.getBodyFromJsonFilePlacedInTestCase(this.getClass().getName(), "provision",
				"oneTimeCharge.xml");

		body = RawMessages.replaceVal(body, "subscriberNumber", customerInfo.getMsisdn());
		resultInfo = Steps.PROTOCOLS.air
				.sendAirRequest(this.getClass().getName(), resultInfo, HttpMethod.POST, 200, "/Air", body).run();

		HttpResponse response = (HttpResponse) resultInfo.getResult();
		String respBody = (String) response.getBody().getValue();
		respBody = xmlToJson.convertXMLToJSON(respBody);

		resultInfo.setResult(respBody);
		return resultInfo;
	}

	public SoivTestBase getCbioTestBase() {
		return soivTestBase;
	}

	public GetOneTimeCharge setSoivTestBase(SoivTestBase soivTestBase) {
		this.soivTestBase = soivTestBase;
		return this;
	}
}
