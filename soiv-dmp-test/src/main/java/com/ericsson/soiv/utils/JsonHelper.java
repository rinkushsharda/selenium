package com.ericsson.soiv.utils;

import com.ericsson.jive.core.execution.Jive;
import com.mongodb.util.JSON;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.LinkedHashMap;
import java.util.Set;

// Created by ZMUKMAN
public class JsonHelper {
    public static String getRequestedJsonElement(TransactionSpecification tSpec, String poName, String requestedKey) {
        Jive.log("Requested PO is :" + poName);
        String output = null;
        try {
            JSONObject jsonObject = new JSONObject(tSpec.getResponseBody());
            JSONArray items = jsonObject.getJSONArray("items");
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                JSONObject it = item.getJSONObject("item");
                JSONObject po = it.getJSONObject("productOffering");
                if (po.get("id").equals(poName)) {
                    output = it.get(requestedKey).toString();
                    break;
                }
            }
            Jive.log("OK : Output For Request Key - " + requestedKey + " :  " + output);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output;
    }

    public static String getRequestPooiId(TransactionSpecification tSpec, String poName, String requestedKey) {
        String output = null;
        try {
            JSONObject jsonObject = new JSONObject(tSpec.getResponseBody());
            JSONArray items = jsonObject.getJSONArray("items");
            for (int i = items.length() - 1; i >= 0; i--) {
                JSONObject item = items.getJSONObject(i);
                JSONObject it = item.getJSONObject("item");
                JSONObject po = it.getJSONObject("productOffering");
                JSONObject product = it.getJSONObject("product");
                if (po.get("id").equals(poName)) {
                    output = it.get(requestedKey).toString();
                    Jive.log("Requested PO : " + poName + " And Pooi Id : " + product.get("productId"));
                    break;
                }
            }
            Jive.log("OK : Output For Request Key - " + requestedKey + " :  " + output);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output;
    }

    public static String getRequestProductId(TransactionSpecification tSpec, String poName, String requestedKey) {
        String output = null;
        try {
            JSONObject jsonObject = new JSONObject(tSpec.getResponseBody());
            JSONArray items = jsonObject.getJSONArray("items");
            for (int i = items.length() - 1; i >= 0; i--) {
                JSONObject item = items.getJSONObject(i);
                JSONObject it = item.getJSONObject("item");
                JSONObject po = it.getJSONObject("productOffering");
                JSONObject product = it.getJSONObject("product");
                if (po.get("id").equals(poName)) {
                    output = product.get(requestedKey).toString();
                    Jive.log("Requested PO : " + poName + " And Product Id : " + product.get("productId"));
                    break;
                }
            }
            Jive.log("OK : Output For Request Key - " + requestedKey + " :  " + output);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output;
    }

    public static JSONObject requestForPersonalizedPrice(String requestedJsonBody, String searchValue,
            String insertValue) throws JSONException {
        JSONObject requestedJsonObject = new JSONObject(requestedJsonBody);
        JSONObject product = requestedJsonObject.getJSONObject("product"); // product json
        JSONArray productPriceArray = product.getJSONArray("productPrice"); // product price json
        JSONObject productPriceJsonObject = null;
        JSONArray characteristics = null;
        for (int getAllPrice = 0; getAllPrice < productPriceArray.length(); getAllPrice++) {
            productPriceJsonObject = productPriceArray.getJSONObject(getAllPrice);
            characteristics = productPriceJsonObject.getJSONArray("characteristics");
            for (int getAllCharacteristics = 0; getAllCharacteristics < characteristics
                    .length(); getAllCharacteristics++) {
                if (characteristics.getJSONObject(getAllCharacteristics).get("name").equals(searchValue)) {
                    JSONObject characteristic = characteristics.getJSONObject(getAllCharacteristics);
                    Set<String> allKeys = characteristic.keySet();
                    LinkedHashMap<String, Object> linkedHashMap = new LinkedHashMap<>();
                    for (String a1 : allKeys) {
                        linkedHashMap.put(a1, characteristic.get(a1));
                    }
                    linkedHashMap.put("value", insertValue);
                    characteristics.put(getAllCharacteristics, linkedHashMap);
                }
            }
        }
        if (productPriceJsonObject != null) {
            productPriceJsonObject.put("characteristics", characteristics);
        }
        productPriceArray.put(1, productPriceJsonObject);
        product.put("productPrice", productPriceArray);
        requestedJsonObject.put("product", product);
        return requestedJsonObject;
    }

    public static JSONObject searchKeyAndUpdateJson(String json, String searchKey, String insertValue) {
        JSONObject jsonObject = new JSONObject(json);
        jsonObject.remove(searchKey);
        jsonObject.put(searchKey, insertValue);
        return jsonObject;
    }

    public static Integer searchPamServiceId(String requestedJson, int pamClassID) {
        String output = null;
        try {
            JSONObject jsonObject = new JSONObject(requestedJson);
            JSONArray items = jsonObject.getJSONArray("pamInformationList");
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                if (item.get("pamClassID").equals(pamClassID)) {
                    output = item.get("pamServiceID").toString();
                    break;
                }
            }
            Jive.log("OK : Output From JSON : " + output);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert output != null;
        return Integer.parseInt(output);
    }

    public static JSONObject insertValues(String json, String searchValue, String insertValue, String arrName,
            String characteristics, String property) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        JSONArray jsonArray = jsonObject.getJSONArray(arrName);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonobject = jsonArray.getJSONObject(i);
            JSONArray serviceCharacteristics = jsonobject.getJSONArray(characteristics);
            for (int j = 0; j < serviceCharacteristics.length(); j++) {
                if (serviceCharacteristics.getJSONObject(j).getString(property).equals(searchValue)) {
                    JSONObject obj = serviceCharacteristics.getJSONObject(j);
                    Set<String> allKeys = obj.keySet();
                    LinkedHashMap jsonMap = new LinkedHashMap();
                    for (String a : allKeys) {
                        jsonMap.put(a, obj.get(a));
                    }
                    jsonMap.put("value", JSON.parse(insertValue));
                    serviceCharacteristics.put(j, jsonMap);
                }
                jsonArray.getJSONObject(i).remove(characteristics);
                jsonArray.getJSONObject(i).put(characteristics, serviceCharacteristics);
            }
        }
        jsonObject.remove("services");
        jsonObject.put("services", jsonArray);
        return jsonObject;
    }

    public static boolean checkPoOnOffer(TransactionSpecification ts, String poName) {
        boolean found = false;
        try {
            JSONObject jsonObject = new JSONObject(ts.getResponseBody());
            JSONArray offerInformationJsonArray = jsonObject.getJSONArray("offerInformation");
            for (int i = 0; i < offerInformationJsonArray.length(); i++) {
                JSONObject offerInformationJsonObject = offerInformationJsonArray.getJSONObject(i);
                JSONArray attributeInformationList = offerInformationJsonObject
                        .getJSONArray("attributeInformationList");
                for (int j = 0; j < attributeInformationList.length(); j++) {
                    JSONObject attributeInformationJsonObject = attributeInformationList.getJSONObject(j);
                    if (attributeInformationJsonObject.toString().contains("attributeValueString")) {
                        if (attributeInformationJsonObject.get("attributeValueString").toString().equals(poName)) {
                            Jive.log("CS OfferId : " + offerInformationJsonObject.get("offerID") + " For PO : "
                                    + poName);
                            found = true;
                            break;
                        }
                    } else {
                        continue;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return found;
    }

    public static JSONObject updatePOOi(String json, String searchValue, String insertValue) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        JSONArray jsonArray = jsonObject.getJSONArray("resources");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonobject = jsonArray.getJSONObject(i);
            JSONArray resourceCharacteristics = jsonobject.getJSONArray("resourceCharacteristics");
            for (int j = 0; j < resourceCharacteristics.length(); j++) {
                if (resourceCharacteristics.getJSONObject(j).getString("name").equals(searchValue)) {
                    JSONObject obj = resourceCharacteristics.getJSONObject(j);
                    Set<String> allKeys = obj.keySet();
                    LinkedHashMap hm1 = new LinkedHashMap();
                    for (String a : allKeys) {
                        hm1.put(a, obj.get(a));
                    }
                    hm1.put("value", insertValue);
                    resourceCharacteristics.put(j, hm1);
                    break;
                }
                jsonArray.getJSONObject(i).remove("resourceCharacteristics");
                jsonArray.getJSONObject(i).put("resourceCharacteristics", resourceCharacteristics);
            }
        }
        jsonObject.remove("resources");
        jsonObject.put("resources", jsonArray);
        return jsonObject;
    }

    public static JSONObject updatePOOiResources(String json, String searchValue, String insertValue, String key)
            throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        JSONArray jsonArray = jsonObject.getJSONArray("resources");
        JSONArray resourceCharacteristics = null;
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonobject = jsonArray.getJSONObject(i);
            resourceCharacteristics = jsonobject.getJSONArray("resourceCharacteristics");
            for (int j = 0; j < resourceCharacteristics.length(); j++) {
                if (resourceCharacteristics.getJSONObject(j).get(key).equals(searchValue)) {
                    JSONObject obj = resourceCharacteristics.getJSONObject(j + 1);
                    Set<String> allKeys = obj.keySet();
                    LinkedHashMap hm1 = new LinkedHashMap();
                    for (String a : allKeys) {
                        hm1.put(a, obj.get(a));
                    }
                    hm1.put("value", insertValue);
                    resourceCharacteristics.put(j + 1, hm1);
                    jsonArray.getJSONObject(i).remove("resourceCharacteristics");
                    jsonArray.getJSONObject(i).put("resourceCharacteristics", resourceCharacteristics);
                    break;
                }
            }
        }
        jsonObject.remove("resources");
        jsonObject.put("resources", jsonArray);
        return jsonObject;
    }

    public static JSONObject updatePOOiResourcesByIndex(String json, String searchValue, String insertValue, String key,
            int index) throws JSONException {

        JSONObject jsonObject = new JSONObject(json);
        JSONArray jsonArray = jsonObject.getJSONArray("resources");
        JSONArray resourceCharacteristics = null;
        JSONObject jsonobject = jsonArray.getJSONObject(index);
        resourceCharacteristics = jsonobject.getJSONArray("resourceCharacteristics");

        for (int j = 0; j < resourceCharacteristics.length(); j++) {
            if (resourceCharacteristics.getJSONObject(j).get(key).equals(searchValue)) {
                JSONObject obj = resourceCharacteristics.getJSONObject(j);

                Set<String> allKeys = obj.keySet();
                LinkedHashMap hm1 = new LinkedHashMap();
                for (String a : allKeys) {
                    hm1.put(a, obj.get(a));
                }
                hm1.put("value", insertValue);
                resourceCharacteristics.put(j, hm1);
                jsonArray.getJSONObject(index).remove("resourceCharacteristics");
                jsonArray.getJSONObject(index).put("resourceCharacteristics", resourceCharacteristics);
                break;
            }
        }

        jsonObject.remove("resources");
        jsonObject.put("resources", jsonArray);
        return jsonObject;
    }
}
