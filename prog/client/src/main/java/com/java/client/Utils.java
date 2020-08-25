package com.java.client;

import com.google.gson.Gson;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;


public class Utils {



    public static HttpEntity<String> getHttpEntityFromDTO(Object obj) {
        return getHttpEntity(new Gson().toJson(obj));
    }

    public static HttpEntity<String> getHttpEntity(String string) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(string, headers);
    }
}
