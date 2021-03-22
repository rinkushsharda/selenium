package com.ericsson.soiv.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

import com.ericsson.soiv.methods.ReadConfigFile;

public class JiveSetupProperties {
    private static String PROJECT_EXPANSION_FILE_PATH = "tmp/Jivesetup";

    public static String getPropertyValue(String propertyName) {

        Properties prop = new Properties();
        String propertyValue;
        String fileContent = ReadConfigFile.readConfigFileWithoutPWToString(PROJECT_EXPANSION_FILE_PATH);
        InputStream in = IOUtils.toInputStream(fileContent);
        try {
            prop.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        propertyValue = prop.getProperty(propertyName);
        return propertyValue;
    }
}
