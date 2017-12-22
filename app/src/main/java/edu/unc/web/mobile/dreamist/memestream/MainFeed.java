package edu.unc.web.mobile.dreamist.memestream;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;

public class MainFeed extends AppCompatActivity {

    private final String TAG = "MEMESTREAM";

    private final int GET_IMG_RESULT = 1;

    private Toolbar toolbar;

    EditText email_input, password_input;

    private FirebaseAuth mAuth;
    private FirebaseStorage mStore;
    private DatabaseReference postdb;
    private RecyclerView social_feed;
    private LinearLayoutCompat login_details;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        social_feed = findViewById(R.id.social_feed);
        email_input = findViewById(R.id.email);
        password_input = findViewById(R.id.password);

        LinearLayoutManager llmanager = new LinearLayoutManager(this);
        social_feed.setHasFixedSize(true);
        llmanager.setStackFromEnd(true);
        social_feed.setLayoutManager(llmanager);

        mStore = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();
        login_details = findViewById(R.id.login_details);
        postdb = FirebaseDatabase.getInstance().getReference("posts");
        MyAdapter adapter = new MyAdapter(postdb, this);
        social_feed.setAdapter(adapter);

    }

    protected void onStart(){
        super.onStart();
        //Check if user is signed in
        if(mAuth.getCurrentUser() == null){
            Snackbar.make(social_feed, "Not logged in!",
                    Snackbar.LENGTH_SHORT).show();
            return;
        }

        Snackbar.make(social_feed,
                "Logged in as:" + mAuth.getCurrentUser().getDisplayName(),
                Snackbar.LENGTH_SHORT)
                .show();

        initFeed(mAuth.getCurrentUser());
    }

    public void createAccount(View v){
        String email_str = email_input.getText().toString();
        String password_str = password_input.getText().toString();

        if (email_str.isEmpty() || password_str.isEmpty()) {
            return;
        }

        mAuth.createUserWithEmailAndPassword(email_str, password_str)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "Login successful.");
                            FirebaseUser user = mAuth.getCurrentUser();
                            initFeed(user);
                        } else {
                            Log.w(TAG, "Login failed.");
                            Toast.makeText(MainFeed.this,
                                    "Login failed.",
                                    Toast.LENGTH_SHORT)
                                    .show();
                            try {
                                task.getException().printStackTrace();
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }

    public void signIn(View v){
        String email_str = email_input.getText().toString();
        String password_str = password_input.getText().toString();

        mAuth.signInWithEmailAndPassword(email_str, password_str)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "Login successful.");
                            FirebaseUser user = mAuth.getCurrentUser();
                            initFeed(user);
                        } else {
                            Log.w(TAG, "Login failure.");
                            Toast.makeText(MainFeed.this,
                                    "Login failure", Toast.LENGTH_SHORT)
                                    .show();
                            try {
                                task.getException().printStackTrace();
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }

    protected void initFeed(FirebaseUser user){
        login_details.setVisibility(View.GONE);
        if (user.getDisplayName().isEmpty()) {
            toolbar.setTitle("Padawan #" + user.getUid().substring(0, 4) + "!");
        } else {
            toolbar.setTitle("Mememaster " + user.getDisplayName());
        }
        Log.d(TAG, "Toolbar set?");
        setSupportActionBar(toolbar);
    }

    protected void logout() {
        mAuth.signOut();
        login_details.setVisibility(View.VISIBLE);
        toolbar.setTitle("Y U NO LOG'N?");
        setSupportActionBar(toolbar);
    }

    public void giveMeme(View v) {
        if (mAuth.getCurrentUser() == null) {
            Log.w(TAG, "User isn't logged in.");
            Toast.makeText(this, "Y U NO LOG'N 1st?", Toast.LENGTH_SHORT).show();
            return;
        }

        //Get a photo and upload
        Intent getPhotoIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        getPhotoIntent.setType("image/*");
        startActivityForResult(getPhotoIntent, GET_IMG_RESULT);

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GET_IMG_RESULT) { //asked for an image
            if (resultCode == RESULT_OK && data != null) {
                //We've got our image.
                Uri imageUri = data.getData();
                Log.d(TAG, "We're all good with getting the URI, "
                        + imageUri);
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),
                            imageUri);
                    Log.d(TAG, "Loaded bitmap into memory.");
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] bitmap_data = baos.toByteArray();

                    StorageReference storageRef = mStore.getReference();
                    StorageReference meme = storageRef.child("meme" + (new Date()).getTime()
                            + ".jpg");
                    Log.d(TAG, "Created StorageReference:" + storageRef.getPath());

                    UploadTask uploadTask = meme.putBytes(bitmap_data);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            Log.d(TAG, "Download it from:" + downloadUrl);
                            Log.d(TAG, "Posting to name:" +
                                    taskSnapshot.getMetadata().getName()
                                            .replaceAll("[^\\w]", ""));
                            postdb.child(taskSnapshot.getMetadata().getName()
                                    .replaceAll("[^\\w]", "")).setValue(downloadUrl.toString());
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        switch (id) {
            case R.id.logout:
                if (mAuth.getCurrentUser() != null) { //Already logged out
                    Log.d(TAG, "Logging out...");
                    logout();
                } else {
                    Log.i(TAG, "User already signed out.");
                }
                return true;
            case R.id.profile:
                Intent i = new Intent(this, ProfileActivity.class);
                startActivity(i);
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
