package com.GeninSamba.gg.gdrivdriver;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;


public class SignupActivity extends AppCompatActivity {
private static final String TAG = "SignupActivity";
    private EditText nameText = null;
    private EditText emailText = null;
    private EditText passwordText = null;
    private Button signupButton = null;
    private EditText prenomText = null;
    private static final String PREFS_NOM_USER = "NOM_USER";
    private static final String PREFS_PRENOM_USER = "PRENOM_USER";
    SharedPreferences sharedPreferences;
    private static final String PREFS = "PREFS";
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private static final String PREFS_USER_STAT = "PREFS_USER_STAT";
    private static final String USER_STAT = "CONNECTED";




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        nameText = (EditText) findViewById(R.id.input_name);
        prenomText = (EditText) findViewById(R.id.input_prenom);
        emailText = (EditText) findViewById(R.id.input_email);
        passwordText = (EditText) findViewById(R.id.input_password);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        auth = FirebaseAuth.getInstance();

        signupButton = (Button) findViewById(R.id.btn_signup);
        TextView loginLink = (TextView) findViewById(R.id.link_login);

        sharedPreferences = getBaseContext().getSharedPreferences(PREFS, MODE_PRIVATE);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }


    public void signup() {
        Log.d(TAG, "Signup");

        if (!validate()) {
            onSignupFailed();
            return;
        }

        signupButton.setEnabled(false);
        nameText.setEnabled(false);
        prenomText.setEnabled(false);
        emailText.setEnabled(false);
        passwordText.setEnabled(false);

        final String nom = nameText.getText().toString().trim();
        final String prenom = prenomText.getText().toString().trim();
        final String email = emailText.getText().toString().trim();
        final String password = passwordText.getText().toString().trim();

        progressBar.setVisibility(View.VISIBLE);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Toast.makeText(SignupActivity.this, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        if (!task.isSuccessful()) {
                            Toast.makeText(SignupActivity.this, "Authentication failed.",Toast.LENGTH_SHORT).show();
                            signupButton.setEnabled(true);

                        } else {

                            String user_id = auth.getCurrentUser().getUid();
                            DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Chauffeurs").child(user_id).child("nom");
                            current_user_db.setValue(email);
                            saveUserInformation(nom,prenom,current_user_db);
                            progressBar.setVisibility(View.GONE);
                            startActivity(new Intent(SignupActivity.this, EditContactActivity.class));
                        }
                    }
                });

    }


    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Echec de connexion", Toast.LENGTH_LONG).show();
        signupButton.setEnabled(true);
        nameText.setEnabled(true);
        prenomText.setEnabled(true);
        emailText.setEnabled(true);
        passwordText.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String name = nameText.getText().toString();
        String prenom = prenomText.getText().toString();
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            nameText.setError("Entrez plus de 3 caractères");
            valid = false;
        } else {
            nameText.setError(null);
        }

        if (prenom.isEmpty() || name.length() < 3) {
            prenomText.setError("Entrez plus de 3 caractères");
            valid = false;
        } else {
            prenomText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError("Enrez une adresse mail valide");
            valid = false;
        } else {
            emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 ) {
            passwordText.setError("Entrez plus de 3 caractères");
            valid = false;
        } else {
            passwordText.setError(null);
        }

        return valid;
    }

    private void saveUserInformation(String nom,String prenom,DatabaseReference dbr) {

        HashMap<String,Object> userInfo = new HashMap<String,Object>();
        userInfo.put("nom", nom);
        userInfo.put("prenom", prenom);
        dbr.updateChildren(userInfo);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(nom+prenom)
                .build();
        if(user!=null){
            user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (!task.isSuccessful()){
                        Toast.makeText(getBaseContext(), "Une erreur s'est produite", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        sharedPreferences.edit()
                .putString(PREFS_USER_STAT, USER_STAT)
                .putString(PREFS_NOM_USER,nom)
                .putString(PREFS_PRENOM_USER,prenom)
                .apply();

    }

}
