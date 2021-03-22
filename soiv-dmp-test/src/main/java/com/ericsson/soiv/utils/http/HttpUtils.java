package com.ericsson.soiv.utils.http;

import com.ericsson.jive.core.exceptions.JiveException;

import java.io.IOException;
import java.net.ServerSocket;

public class HttpUtils {

    public static int getOpenPort() {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        } catch (IOException e) {
            throw new JiveException("Failed to get port dynamically", e);
        }
    }
}
