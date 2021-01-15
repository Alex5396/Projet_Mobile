package com.GeninSamba.gg.gdrivdriver;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class EditCarActivity extends AppCompatActivity {
    SharedPreferences sharedPreferences = null;
    DatabaseReference mDriverDatabase;
    FirebaseAuth auth;
    private AppCompatButton confirmeCarButton = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_car);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        auth = FirebaseAuth.getInstance();
        String userID = auth.getCurrentUser().getUid();
        mDriverDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Chauffeurs").child(userID).child("Car");

        confirmeCarButton = (AppCompatButton) findViewById(R.id.btn_confirme_car);
        confirmeCarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCar();
            }
        });


    }

    private void setCar() {
        confirmeCarButton.setEnabled(false);

        EditText marque = (EditText) findViewById(R.id.input_marque);
        String mMarque = marque.getText().toString();

        EditText couleur = (EditText) findViewById(R.id.input_couleur);
        String mCouleur = couleur.getText().toString();

        EditText immatriculation = (EditText) findViewById(R.id.input_immatriculation);
        String mImmatriculation = immatriculation.getText().toString();


        final ProgressDialog progressDialog = new ProgressDialog(EditCarActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Enregistrement...");
        progressDialog.show();

        // TODO: Enregistrer le contact sur Firebase
        HashMap<String,Object> driverCar = new HashMap<String, Object>();
        driverCar.put("marque", mMarque);
        driverCar.put("couleur", mCouleur);
        driverCar.put("immatriculation", mImmatriculation);
        mDriverDatabase.updateChildren(driverCar).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                progressDialog.dismiss();
                                onSetCarSuccess();
                            }
                        }, 3000);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                progressDialog.dismiss();
                                onSetCarFailed();
                            }
                        }, 3000);
            }
        });

    }

    public void onSetCarSuccess() {
        confirmeCarButton.setEnabled(true);
        setResult(RESULT_OK);
        Intent intent = new Intent(EditCarActivity.this, MapsActivity.class);
        startActivity(intent);
    }

    public void onSetCarFailed() {
        Toast.makeText(getBaseContext(), "Echec de connexion", Toast.LENGTH_LONG).show();
        confirmeCarButton.setEnabled(true);
    }
}
