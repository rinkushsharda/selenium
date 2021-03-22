package com.ericsson.soiv.teststeps.provision.sdp;

import com.ericsson.jive.core.execution.Jive;
import com.ericsson.jive.core.execution.TestStepBase;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.utils.*;
import com.jcraft.jsch.Session;

import java.io.*;
import java.util.Arrays;

import static java.io.File.separator;


public class TraceOnSdp extends TestStepBase<TestResultInfo> {
    private SoivTestBase soivTestBase = null;
    private TestResultInfo resultInfo;
    private CustomerInfo customerInfo;
    private TransactionSpecification tSpec;
    private String cmdRunTrace = "FDSRequestSender -u user -p user ";
    private  String tmpDirectory = "./tmp"+separator;
    private String sdpDir="/home/charles/";
    private String cmdTraceCheck=null;
    private String traceFileSdp="/var/opt/fds/config/plugin/FSC-TraceEventLog/Config.cfg";

    public TraceOnSdp(String title, TestResultInfo resultInfo, TransactionSpecification tSpec, CustomerInfo customerInfo) {
        super(title);
        this.resultInfo = resultInfo;
        this.customerInfo = customerInfo;
        this.tSpec = tSpec;
    }

    @Override
    public TestResultInfo execute() {
        if (!resultInfo.getShouldContinue()) {
            SoivTestBase.failAndContinue("Step not executed due to a previous error");
            return resultInfo;
        }

        Jive.log("Step title: " + getTitle());

        String body = RawMessages.getBodyFromTextFilePlacedInTestStep(this.getClass().getName(),"protocols/sdp/tracetemplates",
                "sdp_trace.txt").toString();

        Jive.log("Input SDP Trace Template Read Successfully! " );

        String localTraceFileName = customerInfo.getMsisdn() + "_trace.txt";

        try {
            resultInfo = prepareTraceFile(resultInfo, customerInfo,body, localTraceFileName);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        return resultInfo;
    }

    private TestResultInfo prepareTraceFile (TestResultInfo resultInfo, CustomerInfo customerInfo,String body, String localTraceFileName) throws InterruptedException, IOException {
        File localTraceDirPath = new File(tmpDirectory);
        FileOutputStream traceFilePath = null;
        BufferedWriter bufferedWriter = null;
        OutputStreamWriter outputStreamWriter = null;
        String fileTraceLog="/var/opt/fds/logs/TraceEventLogFile.txt.0";
        try{
            Jive.log("Updating Trace Template! " + localTraceFileName);
            if(!localTraceDirPath.exists()){
                boolean isCreated = localTraceDirPath.mkdirs();
                Jive.log("Directory /tmp/ Created! : "+isCreated);
            }
            traceFilePath = new FileOutputStream(localTraceDirPath + "/"+ localTraceFileName);
            outputStreamWriter = new OutputStreamWriter(traceFilePath);
            bufferedWriter = new BufferedWriter(outputStreamWriter);
            body = body.replace("{{MSISDN}}", customerInfo.getMsisdn());
            bufferedWriter.write(body);
        }
        catch (Exception e)
        {
            Jive.fail("Exception caught while Writing to file" + Arrays.toString(e.getStackTrace()));
        }
        finally {
            assert bufferedWriter != null;
            bufferedWriter.close();
            assert outputStreamWriter !=null;
            outputStreamWriter.close();
            traceFilePath.close();
        }

        Jive.log("Trace Template Modified Successfully , Now Uploading .. Trace File from : "
                +localTraceDirPath.toString() + " to SDP HOST Directory : "+ sdpDir.toString());

        Session session  = RemoteHostUtility.connectToSDPHost();

        RemoteHostUtility.uploadToRemoteHost(session, localTraceDirPath.toString() + "/"
                + localTraceFileName, sdpDir);

        String localSdpTraceFileName=sdpDir+localTraceFileName;

        Jive.log("Trace Command is Running to Process put Trace . . . :" + cmdRunTrace + localSdpTraceFileName);

        RemoteHostUtility.executeAndKeepAlive(session,cmdRunTrace + localSdpTraceFileName);
        Thread.sleep(30000);
        cmdTraceCheck="grep "+customerInfo.getMsisdn()+ " "+ traceFileSdp+ " > ~/"+localTraceFileName+"_bk";
        RemoteHostUtility.executeAndKeepAlive(session,cmdTraceCheck);
        RemoteHostUtility.executeAndKeepAlive(session,"mv "+localTraceFileName+"_bk " +localTraceFileName);
        Thread.sleep(30000);
        File tmpLocalDir = new File(tmpDirectory);
        RemoteHostUtility.downloadFromRemoteHost(session,localTraceFileName,tmpLocalDir.toString()+"/");
        File tmpLocalDirTraceFile = new File(tmpLocalDir.toString()+"/"+localTraceFileName);
        if (tmpLocalDirTraceFile.length() == 0)
        {
            Jive.fail("Trace Not Successfull on MSISDN "+customerInfo.getMsisdn());
        }
        Jive.log("Trace Successfully Implemented");
        RemoteHostUtility.executeAndKeepAlive(session,"rm -f "+localTraceFileName);
        if(!session.isConnected()){
            RemoteHostUtility.removeFileFromLocal(localTraceDirPath.toString(), localTraceFileName);
        }
        RemoteHostUtility.removeFileFromLocal(localTraceDirPath.toString(), localTraceFileName);
        RemoteHostUtility.executeAndKeepAlive(session,"tail -1cf " +fileTraceLog+" >"+ tSpec.getTraceFileName() +" 2>&1 3>&1 &");
        return resultInfo;
    }

    public SoivTestBase getSoivTestBase() {
        return soivTestBase;
    }

    public TraceOnSdp setSoivTestBase(SoivTestBase soivTestBase) {
        this.soivTestBase = soivTestBase;
        return this;
    }

}
