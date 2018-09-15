package com.viettelpost.remoteconfig.controlremoteconfig;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.session.MediaSession;
import android.util.Log;
import android.widget.Toast;


import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class auth {
    private final static String PROJECT_ID = "newlocation-31a4a";
    private final static String BASE_URL = "https://firebaseremoteconfig.googleapis.com";
    private final static String REMOTE_CONFIG_ENDPOINT = "/v1/projects/" + PROJECT_ID + "/remoteConfig";
    private final static String SCOPES = "https://www.googleapis.com/auth/firebase.remoteconfig";
    private String token = "";

    // [START retrieve_access_token]
    private static String getAccessToken(InputStream inputStream) throws IOException {
        GoogleCredential googleCredential = GoogleCredential
                .fromStream(inputStream)
                .createScoped(Arrays.asList(SCOPES));
        googleCredential.refreshToken();
        return googleCredential.getAccessToken();
    }

    public void getTemplate() throws IOException {

        HttpURLConnection httpURLConnection = getCommonConnection(BASE_URL + REMOTE_CONFIG_ENDPOINT);
        httpURLConnection.setRequestMethod("GET");
        httpURLConnection.setRequestProperty("Accept-Encoding", "gzip");
        int code = httpURLConnection.getResponseCode();
        if (code == 200) {
            InputStream inputStream = new GZIPInputStream(httpURLConnection.getInputStream());
            String response = inputstreamToString(inputStream);

            JsonParser jsonParser = new JsonParser();
            JsonElement jsonElement = jsonParser.parse(response);

            Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
            String jsonStr = gson.toJson(jsonElement);

            File file = new File("config.json");
            PrintWriter printWriter = new PrintWriter(new FileWriter(file));
            printWriter.print(jsonStr);
            printWriter.flush();
            printWriter.close();

            System.out.println("Template retrieved and has been written to config.json");

            // Print ETag
            String etag = httpURLConnection.getHeaderField("ETag");
            System.out.println("ETag from server: " + etag);
        } else {
            System.out.println(inputstreamToString(httpURLConnection.getErrorStream()));
        }

    }

//    public void getVersions() throws IOException {
//        HttpURLConnection httpURLConnection = getCommonConnection(BASE_URL + REMOTE_CONFIG_ENDPOINT
//                + ":listVersions?pageSize=5");
//        httpURLConnection.setRequestMethod("GET");
//
//        int code = httpURLConnection.getResponseCode();
//        if (code == 200) {
//            String versions = inputstreamToPrettyString(httpURLConnection.getInputStream());
//
//            System.out.println("Versions:");
//            System.out.println(versions);
//        } else {
//            System.out.println(inputstreamToString(httpURLConnection.getErrorStream()));
//        }
//    }
//
//    public void rollback(int version) throws IOException {
//        HttpURLConnection httpURLConnection = getCommonConnection(BASE_URL + REMOTE_CONFIG_ENDPOINT
//                + ":rollback");
//        httpURLConnection.setDoOutput(true);
//        httpURLConnection.setRequestMethod("POST");
//        httpURLConnection.setRequestProperty("Accept-Encoding", "gzip");
//
//        JsonObject jsonObject = new JsonObject();
//        jsonObject.addProperty("version_number", version);
//
//        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(httpURLConnection.getOutputStream());
//        outputStreamWriter.write(jsonObject.toString());
//        outputStreamWriter.flush();
//        outputStreamWriter.close();
//
//        int code = httpURLConnection.getResponseCode();
//        if (code == 200) {
//            System.out.println("Rolled back to: "  + version);
//            InputStream inputStream = new GZIPInputStream(httpURLConnection.getInputStream());
//            System.out.println(inputstreamToPrettyString(inputStream));
//
//            // Print ETag
//            String etag = httpURLConnection.getHeaderField("ETag");
//            System.out.println("ETag from server: " + etag);
//        } else {
//            System.out.println("Error:");
//            InputStream inputStream = new GZIPInputStream(httpURLConnection.getErrorStream());
//            System.out.println(inputstreamToString(inputStream));
//        }
//    }

//    public void publishTemplate(String etag) throws IOException {
//        if (etag.equals("*")) {
//            Scanner scanner = new Scanner(System.in);
//            System.out.println("Are you sure you would like to force replace the template? Yes (y), No (n)");
//            String answer = scanner.nextLine();
//            if (!answer.equalsIgnoreCase("y")) {
//                System.out.println("Publish canceled.");
//                return;
//            }
//        }
//
//        System.out.println("Publishing template...");
//        HttpURLConnection httpURLConnection = getCommonConnection(BASE_URL + REMOTE_CONFIG_ENDPOINT);
//        httpURLConnection.setDoOutput(true);
//        httpURLConnection.setRequestMethod("PUT");
//        httpURLConnection.setRequestProperty("If-Match", etag);
//        httpURLConnection.setRequestProperty("Content-Encoding", "gzip");
//
//        String configStr = readConfig();
//
//        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(httpURLConnection.getOutputStream());
//        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(gzipOutputStream);
//        outputStreamWriter.write(configStr);
//        outputStreamWriter.flush();
//        outputStreamWriter.close();
//
//        int code = httpURLConnection.getResponseCode();
//        if (code == 200) {
//            System.out.println("Template has been published.");
//        } else {
//            System.out.println(inputstreamToString(httpURLConnection.getErrorStream()));
//        }
//
//    }

     public String readConfig() throws FileNotFoundException {
        File file = new File("config.json");
        Scanner scanner = new Scanner(file);

        StringBuilder stringBuilder = new StringBuilder();
        while (scanner.hasNext()) {
            stringBuilder.append(scanner.nextLine());
        }
        return stringBuilder.toString();
    }

    public String inputstreamToPrettyString(InputStream inputStream) throws IOException {
        String response = inputstreamToString(inputStream);

        JsonParser jsonParser = new JsonParser();
        JsonElement jsonElement = jsonParser.parse(response);

        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        String jsonStr = gson.toJson(jsonElement);

        return jsonStr;
    }

    public String inputstreamToString(InputStream inputStream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        Scanner scanner = new Scanner(inputStream);
        while (scanner.hasNext()) {
            stringBuilder.append(scanner.nextLine());
        }
        return stringBuilder.toString();
    }

    public HttpURLConnection getCommonConnection(String endpoint) throws IOException {
        URL url = new URL(endpoint);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestProperty("Authorization", "Bearer " );
        httpURLConnection.setRequestProperty("Content-Type", "application/json; UTF-8");
        return httpURLConnection;
    }
}
