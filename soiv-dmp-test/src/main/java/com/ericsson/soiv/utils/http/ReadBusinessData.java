package com.ericsson.soiv.utils.http;

import com.ericsson.jive.core.execution.Jive;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static com.ericsson.soiv.utils.Constants.BUSINESSCACHE;
import static com.ericsson.soiv.utils.Constants.TEMPBUSINESSCACHE;

// Created by ZMUKMAN 16/05/2019

public class ReadBusinessData {

    public static HashMap<String,String> readBusinessCacheData(String useCaseId) throws IOException {
        String replaceUseCase = "_USECASE_"+useCaseId;
        LinkedHashMap<String,String> map = new LinkedHashMap<>();
        BufferedReader br = null;
        try {
            Jive.log("Reading ... "+BUSINESSCACHE +" File for the Use Case : "+useCaseId);
            if(fileExist(BUSINESSCACHE)) {
                br = Files.newBufferedReader(Paths.get(BUSINESSCACHE));
            }
            else {
                br = Files.newBufferedReader(Paths.get(TEMPBUSINESSCACHE));
            }
            String data;
            while ((data = br.readLine()) != null) {
                if(data.contains("=") && data.contains(replaceUseCase)) {
                    String[] array = data.split("=",2);
                    for (int i = 0; i < array.length; i++) {
                        String key = array[i].replace(replaceUseCase,"");
                        map.put(key, array[i + 1]);
                        break;
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            assert br != null;
            br.close();
        }
        return map;
    }

    private static boolean fileExist(String filename) {
        File file = new File(filename);
        return (file.exists() && file.isFile());
    }
}





