package com.ninesmp.bansystem.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.ninesmp.bansystem.models.Ban;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ApiClient {
    private String baseUrl;
    private String apiKey;
    private final Gson gson;
    private final Logger logger;

    public ApiClient(String baseUrl, String apiKey, Logger logger) {
        setCredentials(baseUrl, apiKey);
        this.gson = new Gson();
        this.logger = logger;
    }

    public void setCredentials(String baseUrl, String apiKey) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.apiKey = apiKey;
    }

    /**
     * Make HTTP request to API
     */
    private JsonObject request(String endpoint, String method, String body) throws Exception {
        URL url = new URL(baseUrl + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("X-API-Key", apiKey);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        if (body != null) {
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = body.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
        }

        int responseCode = conn.getResponseCode();
        BufferedReader reader;

        if (responseCode >= 200 && responseCode < 300) {
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        if (responseCode < 200 || responseCode >= 300) {
            throw new Exception("API request failed: " + responseCode + " - " + response.toString());
        }

        return gson.fromJson(response.toString(), JsonObject.class);
    }

    /**
     * Create a new ban
     */
    public void createBan(Ban ban) throws Exception {
        String json = gson.toJson(ban);
        request("/bans", "POST", json);
        logger.info("Ban created via API: " + ban.getBanId());
    }

    /**
     * Deactivate a ban (unban)
     */
    public void deactivateBan(String banId) throws Exception {
        String encodedBanId = java.net.URLEncoder.encode(banId, StandardCharsets.UTF_8.toString());
        request("/bans/" + encodedBanId + "/deactivate", "PUT", null);
        logger.info("Ban deactivated via API: " + banId);
    }

    /**
     * Get active ban by UUID
     */
    public Ban getActiveBanByUuid(String uuid) {
        return getActiveBanByUuid(uuid, null);
    }

    public Ban getActiveBanByUuid(String uuid, String type) {
        try {
            String endpoint = "/bans/uuid/" + uuid;
            if (type != null) {
                endpoint += "?type=" + type;
            }
            JsonObject response = request(endpoint, "GET", null);
            if (response.has("data") && !response.get("data").isJsonNull()) {
                return gson.fromJson(response.get("data"), Ban.class);
            }
            return null;
        } catch (Exception e) {
            logger.warning("Error fetching ban by UUID: " + e.getMessage());
            return null;
        }
    }

    /**
     * Get active ban by IP
     */
    public Ban getActiveBanByIp(String ip) {
        return getActiveBanByIp(ip, null);
    }

    public Ban getActiveBanByIp(String ip, String type) {
        try {
            String endpoint = "/bans/ip/" + ip;
            if (type != null) {
                endpoint += "?type=" + type;
            }
            JsonObject response = request(endpoint, "GET", null);
            if (response.has("data") && !response.get("data").isJsonNull()) {
                return gson.fromJson(response.get("data"), Ban.class);
            }
            return null;
        } catch (Exception e) {
            logger.warning("Error fetching ban by IP: " + e.getMessage());
            return null;
        }
    }

    /**
     * Get all active bans
     */
    public List<Ban> getAllActiveBans() {
        try {
            JsonObject response = request("/bans/active", "GET", null);
            List<Ban> bans = new ArrayList<>();

            if (response.has("data")) {
                JsonArray data = response.getAsJsonArray("data");
                for (int i = 0; i < data.size(); i++) {
                    bans.add(gson.fromJson(data.get(i), Ban.class));
                }
            }

            return bans;
        } catch (Exception e) {
            logger.warning("Error fetching active bans: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Check for expired bans
     */
    public void checkExpiredBans() {
        try {
            request("/bans/check-expired", "POST", null);
        } catch (Exception e) {
            logger.warning("Error checking expired bans: " + e.getMessage());
        }
    }

    /**
     * Generate random ban ID
     */
    public String generateBanId() {
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder banId = new StringBuilder("#");

        for (int i = 0; i < 7; i++) {
            banId.append(chars.charAt((int) (Math.random() * chars.length())));
        }

        return banId.toString();
    }
}
