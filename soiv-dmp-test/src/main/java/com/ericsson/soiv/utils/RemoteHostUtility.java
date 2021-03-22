package com.ericsson.soiv.utils;


import com.ericsson.jive.core.execution.Jive;
import com.ericsson.jive.core.ssh.ExecutionResult;
import com.ericsson.jive.core.ssh.JiveSSHException;
import com.ericsson.jive.core.utils.Sftp;
import com.ericsson.soiv.fixtures.SoivFixture;
import com.jcraft.jsch.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Properties;

import static com.ericsson.jive.core.execution.Jive.getFixture;
import static com.ericsson.jive.core.ssh.RemoteHostUtils.disconnectChannel;

// Created by ZMUKMAN

public class RemoteHostUtility {
    private static Session session = null;
    private static Logger LOG = LoggerFactory.getLogger(RemoteHostUtility.class);

    public static Session connectToBSCSHost() {
        session = initSession(Integer.parseInt((((SoivFixture) getFixture()).getConfigurationPropertyFailIfNotSet("bscs1-ssh_port"))),
                (((SoivFixture) getFixture()).getConfigurationPropertyFailIfNotSet("bscs1-host"))
                , (((SoivFixture) getFixture()).getConfigurationPropertyFailIfNotSet("bscs1-username"))
                , (((SoivFixture) getFixture()).getConfigurationPropertyFailIfNotSet("bscs1-password")));
        if (!session.isConnected()) {
            Jive.fail("SFTP Session is not created , Check the configurations");
        }
        return session;
    }

    public static Session connectToSDPHost() {
        session = initSession(Integer.parseInt((((SoivFixture) getFixture()).getConfigurationPropertyFailIfNotSet("sdp-ssh_port"))),
                (((SoivFixture) getFixture()).getConfigurationPropertyFailIfNotSet("sdp-host"))
                , (((SoivFixture) getFixture()).getConfigurationPropertyFailIfNotSet("sdp-username"))
                , (((SoivFixture) getFixture()).getConfigurationPropertyFailIfNotSet("sdp-password")));
        if (!session.isConnected()) {
            Jive.fail("SFTP Session is not created , Check the configurations");
        }
        return session;
    }
    private static Session initSession(int port, String host, String username, String password) {
        try {
            JSch jSch = new JSch();
            jSch.removeAllIdentity();
            Session session = jSch.getSession(username, host, port);
            Properties config = new Properties();
            config.setProperty("StrictHostKeyChecking", "no");
            config.setProperty("PreferredAuthentications", "publickey,password,keyboard-interactive");
            session.setPassword(password);
            session.setConfig(config);
            session.connect(60000);
            return session;
        } catch (JSchException var9) {
            throw new JiveSSHException("Failed to initialize session for " + username + '@' + host + ':' + port, var9);
        }
    }

    public static Sftp connectToHostSftp(String hostname, String username, String password) {
        return new com.ericsson.jive.core.utils.Sftp(22, (
                (SoivFixture) getFixture()).getConfigurationPropertyFailIfNotSet(hostname),
                ((SoivFixture) getFixture()).getConfigurationPropertyFailIfNotSet(username),
                ((SoivFixture) getFixture()).getConfigurationPropertyFailIfNotSet(password));
    }

    public static com.ericsson.jive.core.ssh.ExecutionResult execute(Sftp client, String cmd) {
        ExecutionResult result;
        result = client.execute(cmd);
        LOG.info("Executing command .. : " + cmd + ", output: \n" + result.getOutput());
        return result;
    }

    public static ChannelShell executeAndKeepAlive(Session session, String command) {
        try {
            ChannelShell channelShell = (ChannelShell) com.ericsson.jive.core.ssh.RemoteHostUtils.connectChannel(session, "shell");
            writeCommandToChannel(channelShell, command);
          //  getCommandOutput(channelShell);
            return channelShell;
        } catch (IOException | JSchException var4) {
            throw JiveSSHException.newExecutionException(command, var4);
        }

    }

    private static void writeCommandToChannel(Channel channel, String command) throws IOException {
        OutputStream outputStream = channel.getOutputStream();
        Throwable var3 = null;

        try {
            outputStream.write((command + '\n').getBytes(Charset.defaultCharset()));
            outputStream.flush();
        } catch (Throwable var12) {
            var3 = var12;
            throw var12;
        } finally {
            if (outputStream != null) {
                if (var3 != null) {
                    try {
                        outputStream.close();
                    } catch (Throwable var11) {
                        var3.addSuppressed(var11);
                    }
                } else {
                    outputStream.close();
                }
            }

        }

    }
    private static void getCommandOutput(Channel channel) throws IOException {
        byte[] buffer = new byte[1024];

        try {
            //InputStream in = channel.getInputStream();
            InputStream in = channel.getInputStream();
            String line = "";
            while (true) {
                //while (in.available() > 0) {
                while (in.read() > 0) {
                    int i = in.read(buffer, 0, 1024);
                    if (i < 0) {
                        break;
                    }
                    line = new String(buffer, 0, i);
                }
            }
        }
        catch  (IOException i){


        }
    }
    public static void getDirectoryAttributes(ChannelSftp channelSftp
            ,String directoryPath, Boolean createDirIfNotExits) throws SftpException {

        SftpATTRS stat = null;

        try {
            stat = channelSftp.stat(directoryPath);
        } catch (Exception e) {
            if (!createDirIfNotExits) {
                Jive.fail(directoryPath + " not found");
            }
        }

        if (stat != null) {
            Jive.log("Directory exist" + directoryPath);
        }
        else if(createDirIfNotExits) {
            channelSftp.mkdir(directoryPath);
            Jive.log("Directory Created!");
        }
    }

    public static void uploadToRemoteHost(Session session, String sourceDir, String destinationDir ) {
        ChannelSftp sftpChannel = null;

        try {
            sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();
            Jive.log("SFTP Channel created To Upload File on Remote Host");

            getDirectoryAttributes(sftpChannel, destinationDir, true);

            sftpChannel.put(sourceDir, destinationDir);

            File f = new File(sourceDir);
            Jive.log("File Successfully Copied From : " + sourceDir + " to Destination :" + destinationDir + " FileName : " + f.getName());

        } catch (Exception sftpException) {
            Jive.fail("FAILED : File is not Copied : " + sourceDir + " to Destination :"
                    + destinationDir, sftpException);
        }
        finally {
            disconnectChannel(sftpChannel);
        }

    }

    public static String downloadFromRemoteHost(Session session, String sourceDir, String destinationDir) {
        ChannelSftp sftpChannel = null;

        try {
            sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();
            Jive.log("SFTP Channel created To Download File on Remote Host");

            getDirectoryAttributes(sftpChannel, sourceDir, false);

            sftpChannel.get(sourceDir, destinationDir);

            File f = new File(sourceDir);
            Jive.log("File Successfully Copied From : " + sourceDir + " to Destination :" + destinationDir + " FileName : " + f.getName());
            return destinationDir + f.getName();
        } catch (Exception sftpException) {
            Jive.fail("FAILED : File is not Copied : " + sourceDir + " to Destination :"
                    + destinationDir, sftpException);
        }
        finally {
            disconnectChannel(sftpChannel);
        }
        return null;
    }

    public static void executeCmd(Session session, String command) {
        ChannelExec channel = null;
        int exitCode;
        try {
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            exitCode = channel.getExitStatus();
            Jive.log("Exit Code " + exitCode);
        } catch (JSchException var10) {
            throw JiveSSHException.newExecutionException(command, var10);
        } finally {
            disconnectChannel(channel);
        }

    }
    public static void removeFileFromLocal(String filePath,String sourceFileName)
    {
        try {

            File source = new File(sourceFileName);
            File targetFile = new File(filePath + "/" +source.getName());
            if (targetFile.delete()) {
                Jive.log("Okay : File Successfully Removed From : " + targetFile + "to Destination :" + " FileName : " + source.getName());
            }
        }catch (Exception e){
            Jive.log("Info : File is not removed from the /tmp directory: " +e);
        }
    }
}







