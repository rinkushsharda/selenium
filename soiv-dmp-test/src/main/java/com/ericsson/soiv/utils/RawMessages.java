package com.ericsson.soiv.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import com.ericsson.jive.core.execution.Jive;
import com.ericsson.jive.core.frameworkconfiguration.Level;

public class RawMessages {
    private static String RESOURCE_BASE_PATH = "/com/ericsson/soiv/";
    private static String RESOURCE_BASE_PATH_TESTCASES = RESOURCE_BASE_PATH + "testcases/";
    private static String RESOURCE_BASE_PATH_TESTSTEPS = RESOURCE_BASE_PATH + "teststeps/";

    public static String getBodyFromJsonFilePlacedInTestCase(String className, String testCaseName, String filename) {
        String completePath = RESOURCE_BASE_PATH_TESTCASES + testCaseName + "/" + filename;
        Jive.log("RawMessages, trying to read file under resources placed under testcases: " + completePath);
        return readFile(className, completePath).toString();
    }

    public static String getBodyFromJsonFilePlacedInTestStep(String className, String testStepSubStructure, String filename) {
        String completePath = RESOURCE_BASE_PATH_TESTSTEPS + testStepSubStructure + "/" + filename;
        Jive.log("RawMessages, trying to read file under resources placed under teststeps: " + completePath);
        return readFile(className, completePath).toString();
    }

    public static StringBuilder getBodyFromTextFilePlacedInTestStep(String className, String testStepSubStructure, String filename) {
        String completePath = RESOURCE_BASE_PATH_TESTSTEPS + testStepSubStructure + "/" + filename;
        Jive.log("RawMessages, trying to read file under resources placed under teststeps: " + completePath);
        return readTextFile(className, completePath);
    }

    private static StringBuilder readFile(String className, String completePath) {
        Jive.log(Level.DEBUG, "RawMessages, readfile classname: " + className + ", completePath: " + completePath);
        Class<?> classType = null;
        try {
            classType = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        assert classType != null;
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(classType.getResourceAsStream(completePath)));
        StringBuilder stringBuilder = new StringBuilder();
        String nextLine = readLine(bufferedReader);
        while (nextLine != null) {
            stringBuilder.append(nextLine);
            nextLine = readLine(bufferedReader);
        }
        return stringBuilder;
    }

    private static String readLine(BufferedReader bufferedReader) {
        String line = null;
        try {
            line = bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return line;
    }
    private static StringBuilder readTextFile  (String className, String completePath) {
        Jive.log(Level.DEBUG, "RawMessages, readfile classname: " + className + ", completePath: " + completePath);
        Class<?> classType = null;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            classType = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {

            assert classType != null;
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(classType.getResourceAsStream(completePath)));

            String nextLine = readLine(bufferedReader);
            while (nextLine != null) {
                stringBuilder.append(nextLine);
                stringBuilder.append("\r\n");
                nextLine = readLine(bufferedReader);
            }
        }
        catch (Exception e)
        {
            Jive.fail("Exception caught while Reading a file! Check the file path: " +completePath+
                    " Exception Message : "+ e.getMessage());

        }
        return stringBuilder;
    }
    public static String replaceVal(String json, String replaceKey, String replaceValue) {
        return json.replaceAll(Pattern.quote("{{" + replaceKey + "}}"), replaceValue);
    }
}
