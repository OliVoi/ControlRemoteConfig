package com.viettelpost.remoteconfig.controlremoteconfig;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.Buffer;
import java.util.Arrays;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class MainActivity extends AppCompatActivity {

    private static EditText editEmail, editPass;
    private static Button btnSub;
    private FirebaseAuth mAuth;
    private Context context;

    FileInputStream fin;


    private final static String PROJECT_ID = "newlocation-31a4a";
    private final static String BASE_URL = "https://firebaseremoteconfig.googleapis.com";
    private final static String REMOTE_CONFIG_ENDPOINT = "/v1/projects/" + PROJECT_ID + "/remoteConfig";
    private final static String SCOPES = "https://www.googleapis.com/auth/firebase.remoteconfig";
    private final static String testToken = "ya29.c.EloYBlMvmnTCQsaVKklJqPhlOoy9X1JPymDsBY3PlU37E3gB_6PTEEfx0BoOl5H2iWKoBOcjsj191BMLKx8T0mk6TlNK-G5PB8ryuz1ouLgI9ZL8ABfS_i2drRU";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        btnSub = findViewById(R.id.button);

        btnSub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            InputStream inputStream = getApplicationContext().getResources().getAssets()
                                    .open("serviceAccountkey.json", Context.MODE_WORLD_READABLE);
                            getTemplate();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                });
            }
        });
    }

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

//            File file = new File("config.json");
//            PrintWriter printWriter = new PrintWriter(new FileWriter(file));
//            printWriter.print(jsonStr);
//            printWriter.flush();
//            printWriter.close();
            Log.e("chuooix", jsonStr);

            System.out.println("Template retrieved and has been written to config.json");

            // Print ETag
            String etag = httpURLConnection.getHeaderField("ETag");
            System.out.println("ETag from server: " + etag);
        } else {
            System.out.println(inputstreamToString(httpURLConnection.getErrorStream()));
        }

    }

    public HttpURLConnection getCommonConnection(String endpoint) throws IOException {
        URL url = new URL(endpoint);
        InputStream inputStream = getApplicationContext().getResources().getAssets()
                .open("serviceAccountkey.json", Context.MODE_WORLD_READABLE);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestProperty("Authorization", "Bearer " + getAccessToken(inputStream));
        httpURLConnection.setRequestProperty("Content-Type", "application/json; UTF-8");
        return httpURLConnection;
    }

    public String inputstreamToString(InputStream inputStream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        Scanner scanner = new Scanner(inputStream);
        while (scanner.hasNext()) {
            stringBuilder.append(scanner.nextLine());
        }
        return stringBuilder.toString();
    }


}
