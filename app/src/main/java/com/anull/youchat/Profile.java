package com.anull.youchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Objects;
import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;


public class Profile extends AppCompatActivity {

    private DatabaseReference userDatabase;
    private FirebaseUser currentUser;
    private Button change_image, change_status;
    private TextView profile_status, profile_name;
    private CircleImageView profile_avatar;
    private StorageReference iStorageRef;
    ProgressDialog pProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        iStorageRef = FirebaseStorage.getInstance().getReference();

        profile_avatar = (CircleImageView) findViewById(R.id.profile_avatar);

        change_image = (Button) findViewById(R.id.change_image);
        change_status = (Button) findViewById(R.id.change_status);

        profile_name = (TextView) findViewById(R.id.profile_name);
        profile_status = (TextView) findViewById(R.id.profile_status);

        change_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String temp = profile_status.getText().toString().trim();

                Intent stIntent = new Intent(Profile.this, ChangeStatus.class);
                stIntent.putExtra("temp", temp);
                startActivity(stIntent);
                finish();

            }
        });

        change_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .setMinCropWindowSize(500,500)
                        .start(Profile.this);

            }
        });

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String cUid = currentUser.getUid();

        userDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(cUid);
        userDatabase.keepSynced(true);

        userDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();

                profile_name.setText(name);
                profile_status.setText(status);

                if(!image.equals("default"))
                {
                    Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_avatar).into(profile_avatar, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {

                            Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(profile_avatar);

                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {



            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                Uri resultUri = result.getUri();
                File thumb_filePath = new File(resultUri.getPath());

                pProgress = new ProgressDialog(this);
                pProgress.setTitle("Uploading Picture");
                pProgress.setMessage("Please wait while the image is being uploaded.");
                pProgress.setCanceledOnTouchOutside(false);
                pProgress.show();

                final String currentID = currentUser.getUid();

                Bitmap thumb_bitmap = new Compressor(this).setMaxWidth(200).setMaxHeight(200).setQuality(75).compressToBitmap(thumb_filePath);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumb_byte = baos.toByteArray();


                final StorageReference filePath = iStorageRef.child("profile_images").child(currentID + ".jpg");
                final StorageReference bitmap_filePath = iStorageRef.child("profile_images").child("thumb_images").child(currentID + ".jpg");

                filePath.putFile(resultUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {

                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                        if (!task.isSuccessful()) {
                            throw Objects.requireNonNull(task.getException());
                        }

                        return filePath.getDownloadUrl();
                    }

                }).addOnCompleteListener(new OnCompleteListener<Uri>() {

                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {

                        if (task.isSuccessful()) {

                            String currentImageURL = task.getResult().toString();

                            userDatabase.child("image").setValue(currentImageURL).addOnCompleteListener(new OnCompleteListener<Void>() {

                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {

                                        bitmap_filePath.putBytes(thumb_byte).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {

                                            @Override
                                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                                                if (!task.isSuccessful()) {
                                                    throw Objects.requireNonNull(task.getException());
                                                }

                                                return bitmap_filePath.getDownloadUrl();
                                            }

                                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {

                                            @Override
                                            public void onComplete(@NonNull Task<Uri> task) {

                                                pProgress.dismiss();

                                                if (task.isSuccessful()) {

                                                    String thumbURL = task.getResult().toString();

                                                    userDatabase.child("thumb_image").setValue(thumbURL).addOnCompleteListener(new OnCompleteListener<Void>() {

                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {

                                                            if (task.isSuccessful()) {

                                                                Toast toast = Toast.makeText(getApplicationContext(), "Profile picture updated successfully..", Toast.LENGTH_SHORT);
                                                                toast.setGravity(Gravity.CENTER, 0, 0);
                                                                toast.show();

                                                            } else {

                                                                Toast toast = Toast.makeText(getApplicationContext(), "There was some error.", Toast.LENGTH_SHORT);
                                                                toast.setGravity(Gravity.CENTER, 0, 0);
                                                                toast.show();

                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

                try {
                    throw error;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
