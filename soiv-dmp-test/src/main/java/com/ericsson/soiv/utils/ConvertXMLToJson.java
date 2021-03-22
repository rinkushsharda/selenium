package com.ericsson.soiv.utils;
import com.ericsson.jive.core.execution.Jive;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import java.io. *;

// Created by ZMUKMAN

public class ConvertXMLToJson {
    private static final int PRETTY_PRINT_INDENT_FACTOR = 4;
    private static String jsonPrettyPrintString = "";

    public static String convertXMLFile(String xmlFilepath) throws IOException {
        InputStream inputStream = null;
        try {
            File file = new File(xmlFilepath);
            inputStream = new FileInputStream(file);
            StringBuilder builder = new StringBuilder();
            int ptr;
            while ((ptr = inputStream.read()) != -1) {
                builder.append((char) ptr);
            }

            String xml = builder.toString();
            JSONObject jsonObj = XML.toJSONObject(xml);
            jsonPrettyPrintString = jsonObj.toString(PRETTY_PRINT_INDENT_FACTOR);
            Jive.log(jsonPrettyPrintString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            assert inputStream != null;
            inputStream.close();
        }
        return jsonPrettyPrintString;
    }

    public static String convertXMLToJSON(String xmlString){
        String jsonPrettyPrintString = "";
        try {
            JSONObject xmlJSONObj = XML.toJSONObject(xmlString);
            jsonPrettyPrintString = xmlJSONObj.toString(PRETTY_PRINT_INDENT_FACTOR);
            Jive.log(jsonPrettyPrintString);
        } catch (JSONException je) {
            Jive.log(je.toString());
        }
        return jsonPrettyPrintString;
    }
}







