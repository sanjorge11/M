package edu.unc.web.mobile.dreamist.memestream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.io.InputStream;

public class ProfileActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    EditText profilename;
    EditText imageUri;
    ImageView profileimage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mAuth = FirebaseAuth.getInstance();
        profilename = findViewById(R.id.profile_name);
        imageUri = findViewById(R.id.image_url);
        profileimage = findViewById(R.id.imageView);
        if (mAuth.getCurrentUser() != null &&
                !mAuth.getCurrentUser().getDisplayName().isEmpty()) {
            profilename.setText(mAuth.getCurrentUser().getDisplayName());

        }
        if (mAuth.getCurrentUser() != null &&
                !mAuth.getCurrentUser().getPhotoUrl().toString().isEmpty()) {
            try {
                imageUri.setText(mAuth.getCurrentUser().getPhotoUrl().toString());
                new DownloadImageTask(profileimage)
                        .execute(mAuth.getCurrentUser().getPhotoUrl().toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void profileUpdate(View v) {
        String name = profilename.getText().toString();
        String location = imageUri.getText().toString();
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .setPhotoUri(Uri.parse(location))
                .build();

        mAuth.getCurrentUser().updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("MEMESTREAM", "Successfully updated profile!");
                        } else {
                            Log.w("MEMESTREAM", "Uh oh, can't update like that!");
                            try {
                                task.getException().printStackTrace();
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }

    //Source:https://stackoverflow.com/questions/2471935/how-to-load-an-imageview-by-url-in-android
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
