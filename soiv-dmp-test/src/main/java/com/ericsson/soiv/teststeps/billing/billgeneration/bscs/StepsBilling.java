package com.ericsson.soiv.teststeps.billing.billgeneration.bscs;

import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.utils.CustomerInfo;
import com.ericsson.soiv.utils.TestResultInfo;
import com.ericsson.soiv.utils.TransactionSpecification;


/**
 * These steps are used for sending Raw messages against BSCS
 * So its up to the test case writer to define messages according
 * to a specific version of a protocol.
 **/
public class StepsBilling {

    public SendInvoiceRequest sendInvoiceRequest(String title, SoivTestBase soivTestBase, TestResultInfo resultInfo, CustomerInfo customerInfo, TransactionSpecification tSpec) {
        return new SendInvoiceRequest(title, soivTestBase, resultInfo, customerInfo, tSpec);
    }

    public GetInvoice getInvoice(String title, SoivTestBase soivTestBase, TestResultInfo resultInfo, CustomerInfo customerInfo, TransactionSpecification tSpec) {
        return new GetInvoice(title, soivTestBase, resultInfo, customerInfo, tSpec);
    }
}
