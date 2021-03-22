package com.ericsson.soiv.teststeps.charging.offline.raw;

import com.ericsson.jive.core.execution.Jive;
import com.ericsson.jive.core.execution.TestStepBase;
import com.ericsson.soiv.testbases.SoivTestBase;
import com.ericsson.soiv.utils.*;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import java.io.*;
import java.util.Arrays;

import static com.ericsson.jive.core.ssh.RemoteHostUtils.disconnectChannel;
import static java.io.File.separator;

public class GenerateOfflineUdr extends TestStepBase<TestResultInfo> {
    private SoivTestBase soivTestBase = null;
    private TestResultInfo resultInfo;
    private CustomerInfo customerInfo;
    private TransactionSpecification tSpec;
    private String autoTestDirectory = "/home/bscsadm/bscs_proj/WORK/AUTOTEST";
    private String cmdRunFiot = "/home/bscsadm/bscs_proj/bin/rhel72_x86.x/fiot -MI -NFIH -d" + autoTestDirectory
            + " -f ";
    private String tmpDirectory = "./tmp" + separator;

    public GenerateOfflineUdr(String title, TestResultInfo resultInfo, TransactionSpecification tSpec,
            CustomerInfo customerInfo) {
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

        String body = tSpec.getResponseBody();

        Jive.log("Input UDR Template Read Successfully! " + tSpec.getUdrTextFileName());

        String localUdrFileName = customerInfo.getMsisdn() + "_" + tSpec.getUdrTextFileName();

        try {
            resultInfo = prepareOfflineUdrs(resultInfo, body, localUdrFileName);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        return resultInfo;
    }

    private TestResultInfo prepareOfflineUdrs(TestResultInfo resultInfo, String body, String localUdrFileName)
            throws InterruptedException, IOException {
        File localUdrDirPath = new File(tmpDirectory);
        FileOutputStream udrFilePath = null;
        BufferedWriter bufferedWriter = null;
        OutputStreamWriter outputStreamWriter = null;
        try {
            Jive.log("Updating UDR Template! " + localUdrFileName);
            if (!localUdrDirPath.exists()) {
                boolean isCreated = localUdrDirPath.mkdirs();
                Jive.log("Directory /data/tmp/ Created! : " + isCreated);
            }
            udrFilePath = new FileOutputStream(localUdrDirPath + "/" + localUdrFileName);
            outputStreamWriter = new OutputStreamWriter(udrFilePath);
            bufferedWriter = new BufferedWriter(outputStreamWriter);
            bufferedWriter.write(body);
        } catch (Exception e) {
            Jive.fail("Exception caught while Writing to file" + Arrays.toString(e.getStackTrace()));
        } finally {
            assert bufferedWriter != null;
            bufferedWriter.close();
            assert outputStreamWriter != null;
            outputStreamWriter.close();
            udrFilePath.close();
        }

        Jive.log("UDR Template Modified Successfully , Now Uploading .. UDR File from : " + tmpDirectory + "/"
                + localUdrFileName + " to BSCS HOST Directory : " + autoTestDirectory);

        Session session = RemoteHostUtility.connectToBSCSHost();
        ChannelSftp sftpChannel = null;

        try {
            sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();
            Jive.log("SFTP Channel created To Check Autotest Directory");
            
            RemoteHostUtility.getDirectoryAttributes(sftpChannel,autoTestDirectory,true);
        }
        catch (Exception e)
        {
            Jive.fail("Exception caught while Checking for AUTOTEST Directory" + Arrays.toString(e.getStackTrace()));
        }
        RemoteHostUtility.uploadToRemoteHost(session, localUdrDirPath.toString() + "/" + localUdrFileName,
                autoTestDirectory);

        Jive.log("FIOT Command is Running to Process UDR File . . . :" + cmdRunFiot + localUdrFileName);
        RemoteHostUtility.executeAndKeepAlive(session, "pteh -b -r -u -s -t -g -c");
        Thread.sleep(30000);

        RemoteHostUtility.executeAndKeepAlive(session, cmdRunFiot + localUdrFileName);

        Thread.sleep(10000);

        try {
            sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();
            RemoteHostUtility.getDirectoryAttributes(sftpChannel, autoTestDirectory + "/" + localUdrFileName + ".DONE",
                    false);
        } catch (JSchException | SftpException e) {
            Jive.fail("FIOT Command not executed! Check the Logs on BSCS" + autoTestDirectory + "/" + localUdrFileName
                    + e);
        } finally {
            disconnectChannel(sftpChannel);
            session.disconnect();
            Jive.log("Session is Disconnected now!");
        }

        if (!session.isConnected()) {
            RemoteHostUtility.removeFileFromLocal(localUdrDirPath.toString(), localUdrFileName);
        }

        return resultInfo;
    }

    public SoivTestBase getSoivTestBase() {
        return soivTestBase;
    }

    public GenerateOfflineUdr setSoivTestBase(SoivTestBase soivTestBase) {
        this.soivTestBase = soivTestBase;
        return this;
    }

}