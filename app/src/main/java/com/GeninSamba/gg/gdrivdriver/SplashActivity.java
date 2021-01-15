package com.GeninSamba.gg.gdrivdriver;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class SplashActivity extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    protected static final String PREFS_USER_STAT = "PREFS_USER_STAT";
    public static final String USER_PROFIL_PATH = "user_profil_path";
    private static final String PREFS = "PREFS";
    protected static final String USER_STAT = "CONNECTED";
    private Intent i = null;
    TextView tv = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        tv = findViewById(R.id.tv);
        ImageView iv = findViewById(R.id.iv);
        Animation myanim = AnimationUtils.loadAnimation(this, R.anim.matransition);
        tv.startAnimation(myanim);
        iv.startAnimation(myanim);

        sharedPreferences = getBaseContext().getSharedPreferences(PREFS, MODE_PRIVATE);
        String statUser = sharedPreferences.getString(PREFS_USER_STAT,"");

        if(sharedPreferences.contains(PREFS_USER_STAT)&& statUser.equals(USER_STAT)) {
            i = new Intent(this, MapsActivity.class);
            if(sharedPreferences.contains(USER_PROFIL_PATH)&&sharedPreferences.getString(USER_PROFIL_PATH,null)!=null){
                checkTempFile(sharedPreferences.getString(USER_PROFIL_PATH,null));
            }else {
                tv.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(i);
                        finish();
                    }
                },2000);
            }
        }else{
            i = new Intent(this, LoginActivity.class);
            tv.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(i);
                    finish();
                }
            },2000);
        }

    }

    protected void checkTempFile(String sPath){
        boolean fileExist = new File(sPath).isFile();
        if(!fileExist){
            FirebaseAuth auth = FirebaseAuth.getInstance();
            String iduser = auth.getCurrentUser().getUid();
            StorageReference mClientStorage = FirebaseStorage.getInstance().getReference().child("profile_images").child(iduser);

            try {
                final File localFile = File.createTempFile("images", "jpg");

                mClientStorage.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        sharedPreferences.edit().putString(USER_PROFIL_PATH,localFile.getAbsolutePath()).apply();
                        tv.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(i);
                                finish();
                            }
                        },2000);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SplashActivity.this, "Problème lors de la récupération de la photo de profil", Toast.LENGTH_LONG).show();

                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

        }else{
            tv.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(i);
                    finish();
                }
            },2000);
        }
    }
}
