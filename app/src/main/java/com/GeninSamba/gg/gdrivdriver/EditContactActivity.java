package com.GeninSamba.gg.gdrivdriver;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class EditContactActivity extends AppCompatActivity {
    private AppCompatButton confirmePhoneButton = null;
    private static final String PREFS = "PREFS";
    private static final String PREFS_CONTACT_USER = "CONTACT_USER";
    SharedPreferences sharedPreferences = null;
    DatabaseReference mClientDatabase;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_contact);

        sharedPreferences = getBaseContext().getSharedPreferences(PREFS, MODE_PRIVATE);

        auth = FirebaseAuth.getInstance();
        String userID = auth.getCurrentUser().getUid();
        mClientDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Chauffeurs").child(userID);

        confirmePhoneButton = (AppCompatButton) findViewById(R.id.phone_continuer);
        confirmePhoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPhone();
            }
        });
    }


    public void onPhoneSuccess() {
        confirmePhoneButton.setEnabled(true);
        setResult(RESULT_OK);
        Intent intent = new Intent(EditContactActivity.this, EditCarActivity.class);
        startActivity(intent);
    }

    public void onPhoneFailed() {
        Toast.makeText(getBaseContext(), "Echec de connexion", Toast.LENGTH_LONG).show();
        confirmePhoneButton.setEnabled(true);
    }

    public void setPhone(){
        confirmePhoneButton.setEnabled(false);

        EditText numeroText = (EditText) findViewById(R.id.input_phoneNumber);
        String numero = numeroText.getText().toString();

        final ProgressDialog progressDialog = new ProgressDialog(EditContactActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Enregistrement...");
        progressDialog.show();

        sharedPreferences.edit().putString(PREFS_CONTACT_USER, numero).apply();
        // TODO: Enregistrer le contact sur Firebase
        HashMap<String,Object> userContact = new HashMap<String, Object>();
        userContact.put("contact", numero);
        mClientDatabase.updateChildren(userContact);

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        progressDialog.dismiss();
                        onPhoneSuccess();
                    }
                }, 3000);
    }

}
