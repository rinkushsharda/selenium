package com.ericsson.soiv.teststeps.protocols.air.businesslogic;

import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.utils.CustomerInfo;
import com.ericsson.soiv.utils.TestResultInfo;

public class StepsBusinessLogicAIR {
	
	public GetOneTimeCharge getOneTimeCharge(String title, SoivTestBase soivTestBase, CustomerInfo customerInfo,
            TestResultInfo resultInfo) {
	return new GetOneTimeCharge(title, soivTestBase, customerInfo,  resultInfo);
}

}
