package com.ericsson.soiv.teststeps.billing.billgeneration.bscs;

import com.ericsson.jive.core.execution.Jive;
import com.ericsson.jive.core.execution.TestStepBase;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.utils.*;
import com.jcraft.jsch.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetInvoice extends TestStepBase<TestResultInfo>{
    private static Logger LOG = LoggerFactory.getLogger(GetInvoice.class);
    private SoivTestBase soivTestBase = null;
    private TestResultInfo resultInfo = null;
    private CustomerInfo customerInfo = null;
    private TransactionSpecification tSpec = null;
    private String queryGetCsidPub = "SELECT BBIF.BI_REFERENCE FROM BGH_BILL_IMAGE_REF BBIF WHERE BBIF.CUST_ID=";
    private String queryGetCsidPubOrderBy = " ORDER BY ROWNUM DESC";
    private String readInvoice = null;


    public GetInvoice(String title,SoivTestBase soivTestBase, TestResultInfo resultInfo, CustomerInfo customerInfo, TransactionSpecification tSpec) {
        super(title);
        this.resultInfo = resultInfo;
        this.customerInfo = customerInfo;
        this.tSpec= tSpec;
        this.soivTestBase = soivTestBase;

        if (!resultInfo.getShouldContinue()) {
            LOG.error(title + " - Not executed due to previous error");
            soivTestBase.failAndContinue("Step not executed due to a previous error");
        }
    }

    @Override
    public TestResultInfo execute() {
        if (!resultInfo.getShouldContinue()) {
            SoivTestBase.failAndContinue("Step not executed due to a previous error");
            return resultInfo;
        }
        LOG.info("Step title: " + getTitle());

        try {
            Thread.sleep(1000);
            String query = queryGetCsidPub + customerInfo.getCSID() + queryGetCsidPubOrderBy;
            String getInvoicePath = CreateDatabaseConnection.runQuery(query);

            Jive.log("Invoice Path from BSCS Database : "+getInvoicePath);
            tSpec.setInvoicePath(getInvoicePath);

            if(getInvoicePath != null) {
                Thread.sleep(1000);
                Session getCurrentSession = RemoteHostUtility.connectToBSCSHost();
                readInvoice = ReadAndValidateInvoicePdf.readAndValidateInvoicePdf(getCurrentSession,tSpec);
                resultInfo.setResult(readInvoice);
            }
            else {
                Jive.fail("FAILED : Problem in Invoice PDF , Please check BSCS Database with Query : "+query);
            }

        } catch (Exception e){
            LOG.info(e.getMessage());
        }

        return resultInfo;
    }

    public SoivTestBase getSoivTestBase() {
        return soivTestBase;
    }

    public GetInvoice getInvoice(SoivTestBase soivTestBase) {
        this.soivTestBase = soivTestBase;
        return this;
    }

}

