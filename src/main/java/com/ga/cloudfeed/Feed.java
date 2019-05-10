package com.ga.cloudfeed;


import com.google.firebase.internal.NonNull;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.gson.Gson;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Feed {
    @NonNull
    private String encodedUrl;
    @NonNull
    private String title;
    @NonNull
    private String url;
    private Date lastModified;
    private String websub;
    private Map<String, String> cloud;
    private int subscribers;

    public Feed() {
    }

    public Feed(@NonNull String encodedUrl, @NonNull String title, @NonNull Date lastModified, @NonNull String url, String websub, Map<String, String> cloud, int subscribers) {
        this.encodedUrl = encodedUrl;
        this.title = title;
        this.lastModified = lastModified;
        this.url = url;
        this.websub = websub;
        this.cloud = cloud;
        this.subscribers = subscribers;
    }

    public Feed(String encoded) {
        String decoded = new String(Base64.getDecoder().decode(encoded));
        Map<String, String> map = (Map) (new Gson()).fromJson(decoded, Map.class);
        this.encodedUrl = map.get("encodedUrl");
        this.title = map.get("title");
        this.url = map.get("url");
    }

    public String getEncodedUrl() {
        return this.encodedUrl;
    }

    public String getTitle() {
        return this.title;
    }

    public Date getLastModified() {
        return this.lastModified;
    }

    public String getWebsub() {
        return this.websub;
    }

    public Map<String, String> getCloud() {
        return this.cloud;
    }

    public String getUrl() {
        return this.url;
    }

    public int getSubscribers() {
        return this.subscribers;
    }

    public void sendToTopic() {
        try {
            String response = FirebaseMessaging.getInstance().send(Message.builder().putData("url", this.url).putData("title", this.title).setTopic(this.encodedUrl).build());
            Servlet.getUrl.append("topic " + response + "\n");
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }

    }

    public void subscribe() {
        this.sendRequest("subscribe");
    }

    public void unsubscribe() {
        this.sendRequest("unsubscribe");
    }

    private void sendRequest(String method) {

        if (this.websub != null) {
            try {
                String call = "hub.mode=" + method + "&hub.verify=sync&hub.topic=" + this.url + "&hub.callback=" + Servlet.url + "websub?encoded=" + this.getEncodedGson();
                System.out.println(call);
                byte[] data = call.getBytes(StandardCharsets.UTF_8);
                URL url = new URL(this.websub);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setDoOutput(true);
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                con.setRequestProperty("charset", "utf-8");
                con.getOutputStream().write(data);
                int responseCode = con.getResponseCode();
                if (responseCode < 300) {
                    this.printStream(con.getInputStream(), method, responseCode);
                } else {
                    this.printStream(con.getErrorStream(), method, responseCode);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void printStream(InputStream inputStream, String method, int responseCode) {
    }


    String getEncodedGson() {
        HashMap<String, String> map = new HashMap();
        map.put("url", this.url);
        map.put("title", this.title);
        map.put("encodedUrl", this.encodedUrl);
        String json = (new Gson()).toJson(map);
        return Base64.getEncoder().withoutPadding().encodeToString(json.getBytes());
    }
}
