package com.viettelpost.remoteconfig.controlremoteconfig;

import android.content.res.AssetManager;
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private static EditText editEmail, editPass;
    private static Button btnSub;
    private FirebaseAuth mAuth;

    private final static String PROJECT_ID = "PROJECT_ID";
    private final static String BASE_URL = "https://firebaseremoteconfig.googleapis.com";
    private final static String REMOTE_CONFIG_ENDPOINT = "/v1/projects/" + PROJECT_ID + "/remoteConfig";
    private final static String SCOPES = "https://www.googleapis.com/auth/firebase.remoteconfig";


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);


    }

    private void updateUI(FirebaseUser currentUser) {
        if (currentUser != null) {
            Toast.makeText(this, "Wellcome: " + currentUser.getEmail().toString(), Toast.LENGTH_SHORT).show();
            final FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
            mUser.getIdToken(true)
                    .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                        public void onComplete(@NonNull Task<GetTokenResult> task) {
                            if (task.isSuccessful()) {
                                String idToken = task.getResult().getToken();
                                Log.e("Token: ", idToken);

                            } else {
                                Toast.makeText(MainActivity.this, "No Token", Toast.LENGTH_SHORT).show();
                                Log.e("token:", mUser.getIdToken(true).toString());
                            }
                        }
                    });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SetId();


        try {

            AssetManager ag = getApplicationContext().getAssets();


            GoogleCredential googleCredential = GoogleCredential
                    .fromStream(new FileInputStream("serviceAccountkey.json"))
                    .createScoped(Arrays.asList(SCOPES));
            googleCredential.refreshToken();
            //return googleCredential.getAccessToken();
            Log.e("uuuuuuu", googleCredential.getAccessToken());


        } catch (IOException e) {
            Log.e("erro------", e.toString());
        }


        btnSub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Login();

            }
        });


// ...
        mAuth = FirebaseAuth.getInstance();


        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        mUser.getIdToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            String idToken = task.getResult().getToken();
                            Log.e("yyyy", idToken);
                        } else {
                        }
                    }
                });

    }

    private void SetId() {
        mAuth = FirebaseAuth.getInstance();
        editEmail = findViewById(R.id.editEmail);
        editPass = findViewById(R.id.editPass);
        btnSub = findViewById(R.id.button);
    }

    private void Login() {
        String email = editEmail.getText().toString();
        String pass = editPass.getText().toString();
        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Đăng nhập thành công",
                                    Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);

                        } else {
                            Log.e("Error: ", "signInWithEmail:failure", task.getException());
                            editEmail.setError("");
                            editPass.setError("");
                            Toast.makeText(MainActivity.this, "Đăng nhập thất bại",
                                    Toast.LENGTH_SHORT).show();
                            Toast.makeText(MainActivity.this, "Email hoặc mật khẩu không đúng",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                    }
                });
    }
}
