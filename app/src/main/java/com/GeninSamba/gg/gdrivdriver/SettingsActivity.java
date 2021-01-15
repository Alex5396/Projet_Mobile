package com.GeninSamba.gg.gdrivdriver;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG_LOG = "log" ;
    private EditText mNameField, mPrenomField, mPhoneField, mCarMarqueField, mCarColorField;
    private Button mBack, mConfirm;
    private ImageView mProfileImage;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private DatabaseReference mDriverNomDatabase,mDriverCarDatabase,mDriverDatabase;
    private String userID;
    private String mName;
    private String mPrenom;
    private String mPhone;
    private String mMarqueCar;
    private String mColorCar;
    private String mProfileImageUrl;

    private Uri resultUri;

    private RadioGroup mRadioGroup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        mNameField = (EditText) findViewById(R.id.name);
        mPrenomField = (EditText) findViewById(R.id.prenom);
        mPhoneField = (EditText) findViewById(R.id.phone);
        mCarMarqueField = (EditText) findViewById(R.id.car);
        mCarColorField = (EditText) findViewById(R.id.couleur);
        progressBar = findViewById(R.id.progressBar);
        mProfileImage = (ImageView) findViewById(R.id.profileImage);

        mBack = (Button) findViewById(R.id.back);
        mConfirm = (Button) findViewById(R.id.confirm);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        mDriverDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Chauffeurs").child(userID);
        mDriverNomDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Chauffeurs").child(userID).child("nom");
        mDriverCarDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Chauffeurs").child(userID).child("Car");

        getUserInfo();

        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });

        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserInformation();
            }
        });

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                return;
            }
        });
    }
    private void getUserInfo(){
        mDriverDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> mapNom = (Map<String, Object>) dataSnapshot.child("nom").getValue();
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    if(mapNom.get("nom")!=null){
                        mName = mapNom.get("nom").toString();
                        mNameField.setText(mName);
                    }
                    if(mapNom.get("prenom")!=null){
                        mPrenom = mapNom.get("prenom").toString();
                        mPrenomField.setText(mPrenom);
                    }
                    if(map.get("contact")!=null){
                        mPhone = map.get("contact").toString();
                        mPhoneField.setText(mPhone);
                    }

                    if(mapNom.get("profileImageUrl")!=null){
                        mProfileImageUrl = mapNom.get("profileImageUrl").toString();
                        Glide.with(getApplication()).load(mProfileImageUrl).into(mProfileImage);
                    }
                    if(dataSnapshot.child("Car").exists()&& dataSnapshot.getChildrenCount()>0){
                        Map<String, Object> mapCar = (Map<String, Object>) dataSnapshot.child("Car").getValue();
                        if(mapCar.get("marque")!=null){
                            mMarqueCar = mapCar.get("marque").toString();
                            mCarMarqueField.setText(mMarqueCar);
                        }
                        if(mapCar.get("couleur")!=null){
                            mColorCar = mapCar.get("couleur").toString();
                            mCarColorField.setText(mColorCar);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }



    private void saveUserInformation() {
        progressBar.setVisibility(View.VISIBLE);

        mName = mNameField.getText().toString();
        mPrenom = mPrenomField.getText().toString();
        mPhone = mPhoneField.getText().toString();
        mMarqueCar = mCarMarqueField.getText().toString();
        mColorCar = mCarColorField.getText().toString();

        Map<String,Object> userCar = new HashMap<String, Object>();
        userCar.put("marque", mMarqueCar);
        userCar.put("couleur", mColorCar);
        mDriverCarDatabase.updateChildren(userCar);

        Map<String,Object> userInfo = new HashMap<String,Object>();
        userInfo.put("nom", mName);
        userInfo.put("prenom", mPrenom);
        userInfo.put("contact", mPhone);
        mDriverNomDatabase.updateChildren(userInfo);

        if(resultUri != null) {

            StorageReference filePath = FirebaseStorage.getInstance().getReference().child("profile_images").child(userID);
            Bitmap bitmap = null;
            Cursor cursor= getContentResolver().query(resultUri,new String[]{MediaStore.Images.ImageColumns.DATA},null,null,null);
            if (cursor!= null && cursor.getCount()>0){
                cursor.moveToFirst();
                String newPath=cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
                if(newPath!=null){
                    Bitmap bitmapTemp= decodeFile(newPath);
                    if (bitmapTemp!=null){
                        Bitmap user_i = null;
                        user_i = getCircularBitmap(bitmapTemp);
                        user_i = addBorderToCircularBitmap(user_i,15, Color.WHITE);
                        user_i = addShadowToCircularBitmap(user_i,4,Color.LTGRAY);
                        bitmap = user_i;
                    }
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
            byte[] data = baos.toByteArray();
            UploadTask uploadTask = filePath.putBytes(data);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.setVisibility(View.GONE);
                }
            });
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();

                    Map<String,Object> newImage = new HashMap<String,Object>();
                    newImage.put("profileImageUrl", downloadUrl.toString());
                    mDriverNomDatabase.updateChildren(newImage);
                    progressBar.setVisibility(View.GONE);
                    finish();
                    return;
                }
            });
        }else{
            progressBar.setVisibility(View.GONE);
            finish();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == Activity.RESULT_OK){
            final Uri uri = data.getData();
            resultUri = uri;
            Cursor cursor= getContentResolver().query(uri,new String[]{MediaStore.Images.ImageColumns.DATA},null,null,null);
            if (cursor!= null && cursor.getCount()>0){
                cursor.moveToFirst();
                String newPath=cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
                if(newPath!=null){
                    Bitmap bitmap= decodeFile(newPath);
                    if (bitmap!=null){
                        Bitmap user_i = null;
                        user_i = getCircularBitmap(bitmap);
                        user_i = addBorderToCircularBitmap(user_i,15, Color.WHITE);
                        user_i = addShadowToCircularBitmap(user_i,4,Color.LTGRAY);
                        mProfileImage.setImageBitmap(user_i);}
                }
            }
        }
    }

    public static Bitmap decodeFile(String pathName){
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        for (options.inSampleSize = 1; options.inSampleSize <= 32; options.inSampleSize++){
            try {
                bitmap = BitmapFactory.decodeFile(pathName, options);
                Log.d(TAG_LOG, "Decoded successfully for sampleSize "+options.inSampleSize);
                break;
            }catch (OutOfMemoryError outOfMemoryError){
                Log.e(TAG_LOG, "outOfMemoryError while reading file for sampleSize "+options.inSampleSize+" retrying with higher value");
            }
        }
        return bitmap;
    }

    public Bitmap getCircularBitmap(Bitmap bitmap){
        int squareBitmapWidth = Math.min(bitmap.getWidth(),bitmap.getHeight());
        Bitmap output =Bitmap.createBitmap(squareBitmapWidth,squareBitmapWidth, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        Rect rect = new Rect(0,0,squareBitmapWidth,squareBitmapWidth);
        RectF rectF = new RectF(rect);

        canvas.drawOval(rectF,paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        float left = (squareBitmapWidth-bitmap.getWidth())/2;
        float top = (squareBitmapWidth-bitmap.getHeight())/2;

        canvas.drawBitmap(bitmap,left,top,paint);
        bitmap.recycle();

        return output;
    }

    protected Bitmap addBorderToCircularBitmap(Bitmap srcBitmap, int borderWidth, int borderColor){
        int dstBitmapWidth = srcBitmap.getWidth()+borderWidth*2;

        Bitmap dstBitmap = Bitmap.createBitmap(dstBitmapWidth,dstBitmapWidth,Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(dstBitmap);
        canvas.drawBitmap(srcBitmap,borderWidth,borderWidth,null);

        Paint paint = new Paint();
        paint.setColor(borderColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(borderWidth);
        paint.setAntiAlias(true);

        canvas.drawCircle(
                canvas.getWidth()/2,
                canvas.getWidth()/2,
                canvas.getWidth()/2 - borderWidth/2,
                paint
        );

        srcBitmap.recycle();

        return  dstBitmap;
    }

    protected Bitmap addShadowToCircularBitmap(Bitmap srcBitmap,int shadowWidth,int shadowColor){
        int dstBitmapWidth = srcBitmap.getWidth()+shadowWidth*2;
        Bitmap dstBitmap = Bitmap.createBitmap(dstBitmapWidth,dstBitmapWidth,Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(dstBitmap);
        canvas.drawBitmap(srcBitmap,shadowWidth,shadowWidth,null);

        Paint paint = new Paint();
        paint.setColor(shadowColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(shadowWidth);
        paint.setAntiAlias(true);

        canvas.drawCircle(
                dstBitmapWidth/2,
                dstBitmapWidth/2,
                dstBitmapWidth/2 - shadowWidth/2,
                paint
        );

        srcBitmap.recycle();

        return  dstBitmap;
    }

}
