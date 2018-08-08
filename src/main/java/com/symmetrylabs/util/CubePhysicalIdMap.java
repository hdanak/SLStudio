package com.symmetrylabs.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.LinkedTreeMap;
import com.symmetrylabs.slstudio.SLStudio;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CubePhysicalIdMap {
    protected final Map<String, String> physicalIds = new HashMap<>();
    protected final Set<String> physicalIdsNotFound = new HashSet<>();
    protected final static String FILENAME = "physid_to_mac.json";

    public CubePhysicalIdMap() {
        byte[] bytes = SLStudio.applet.loadBytes(FILENAME);
        if (bytes != null) {
            Map<String, String> map;
            try {
                map = new Gson().fromJson(new String(bytes), Map.class);
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
                String message = (e.getCause() != null ? e.getCause() : e).getMessage();
                SLStudio.setWarning(FILENAME, "JSON syntax error: " + message);
                return;
            }

            String duplicatedMac = null;
            String invalidMac = null;
            String invalidMacId = null;
            for (String physId : map.keySet()) {
                String mac = map.get(physId).replaceAll(":", "");
                if (physicalIds.containsKey(mac) && duplicatedMac == null) {
                    duplicatedMac = mac;
                }
                physicalIds.put(mac, physId);
                if (mac.length() != 12 && invalidMac == null) {
                    invalidMac = mac;
                    invalidMacId = physId;
                }
            }
            String warning = "";
            if (duplicatedMac != null) warning += "Duplicated MAC: " + duplicatedMac + ".  ";
            if (invalidMac != null) warning += "Invalid MAC: " + invalidMac + " (cube " + invalidMacId + ").  ";
            SLStudio.setWarning(FILENAME, warning.trim());
        }
    }

    public String getPhysicalId(String deviceId) {
        if (!physicalIds.containsKey(deviceId) && !physicalIdsNotFound.contains(deviceId)) {
            physicalIdsNotFound.add(deviceId);
            SLStudio.setWarning("CubePhysicalIdMap", "No physical ID registered for MAC: " + String.join(", ", physicalIdsNotFound));
        }
        return physicalIds.getOrDefault(deviceId, deviceId);
    }
}
