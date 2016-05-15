package com.dashngo.android;

import com.dashngo.android.net.model.Product;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void parseMap() {
        Gson gson = new GsonBuilder().create();
        String json = "{\n" +
                "\"2982734982342\": {\n" +
                "    \"name\": \"Toothbrush\",\n" +
                "    \"price\": \"0.50\",\n" +
                "    \"address\": \"Xas;ldfkjas;dlfkjasdfasdf\"\n" +
                "},\n" +
                "\"2982734982343\": {\n" +
                "    \"name\": \"Toothpaste\",\n" +
                "    \"price\": \"1.00\",\n" +
                "    \"address\": \"Xsdfasdl;fkjasd;lfkjasdf\"\n" +
                "},\n" +
                "\"2982734982344\": {\n" +
                "    \"name\": \"Floss\",\n" +
                "    \"price\": \"1.50\",\n" +
                "    \"address\": \"Xa;sldfkjasasdf;dlfkjasdf\"\n" +
                "}\n" +
                "}";
        TypeToken<Map<String, Product>> mapTypeToken = new TypeToken<Map<String, Product>>() {
        };
        Map<String, Product> productMap = gson.fromJson(json, mapTypeToken.getType());
        System.out.println(productMap);
    }
}