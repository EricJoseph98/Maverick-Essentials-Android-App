package maverick.ericjoseph.com.maverickessentials;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class RegisterStore extends AppCompatActivity {

    EditText store_name, store_owner_name, store_owner_phone, store_full_address, store_city, store_locality, store_timings, store_username, store_password;
    Spinner store_type;
    ImageView store_image, store_owner_image;
    float store_lat = 0, store_long = 0;
    int PICK_IMAGE_REQUEST1 = 1411, PICK_IMAGE_REQUEST2 = 1403;
    Uri filePath1, filePath2;
    String downloadUrl1, downloadUrl2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_store);

        store_name = findViewById(R.id.store_name_txt);
        store_owner_name = findViewById(R.id.store_owner_name_txt);
        store_owner_phone = findViewById(R.id.store_owner_phone_txt);
        store_full_address = findViewById(R.id.store_address_txt);
        store_city = findViewById(R.id.store_city_txt);
        store_locality = findViewById(R.id.store_locality_txt);
        store_timings = findViewById(R.id.store_locality_txt);
        store_username = findViewById(R.id.store_username_txt);
        store_password = findViewById(R.id.store_password_txt);

        TextView store_map_picker = findViewById(R.id.store_loc_clk);

        store_type = findViewById(R.id.store_type_spinner);
        String[] store_types_array = {"Grocery", "Medicines", "Every Day Items", "Pets and Vets"};
        ArrayAdapter<String> store_type_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, store_types_array);
        store_type.setAdapter(store_type_adapter);

        Button register_store = findViewById(R.id.store_register_btn);
        register_store.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                upload_Store_Images_to_Firebase();
            }
        });

        store_map_picker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        store_image = (ImageView) findViewById(R.id.store_img);
        store_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Store Image"), PICK_IMAGE_REQUEST1);
            }
        });

        store_owner_image = findViewById(R.id.store_owner_img);
        store_owner_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Store Owner Image"), PICK_IMAGE_REQUEST2);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath1 = data.getData();

            try {
                //getting image from gallery
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath1);
                //Setting image to ImageView
                store_image.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        else  if (requestCode == PICK_IMAGE_REQUEST2 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath2 = data.getData();

            try {
                //getting image from gallery
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath2);
                //Setting image to ImageView
                store_owner_image.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void upload_Store_Images_to_Firebase(){
        try {
            //Storing Image now
            StorageReference storageRef = FirebaseStorage.getInstance("gs://maverick-essentials.appspot.com").getReference("Store Images");
            StorageReference childRef = storageRef.child(store_name.getText().toString() + "_image");
            //uploading the image
            final UploadTask uploadTask = childRef.putFile(filePath1);

            if (filePath1 != null){
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //downloadUrl1 = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();

                        Task<Uri> result = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                        result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                downloadUrl1 = uri.toString();
                                Toast.makeText(getApplicationContext(), "Store Image Upload successful", Toast.LENGTH_LONG).show();
                                upload_Store_Owner_Images_to_Firebase();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Upload Failed -> Please try again" + e, Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(getApplicationContext(), "Uploading Image and Details...Please Wait and do not exit from page", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else
                Toast.makeText(getApplicationContext(),"Please enter all the details or select Image if not done",Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Doctor Image not Uploaded due to errors", Toast.LENGTH_SHORT).show();
        }
    }

    public void upload_Store_Owner_Images_to_Firebase(){
        try {
            //Storing Image now
            StorageReference storageRef = FirebaseStorage.getInstance("gs://maverick-essentials.appspot.com").getReference("Store Owner Images");
            StorageReference childRef = storageRef.child(store_owner_name.getText().toString() + "_image");
            //uploading the image
            final UploadTask uploadTask = childRef.putFile(filePath2);

            if (filePath2 != null){
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Latest way to get Download URI of image uploaded in Firebase
                        Task<Uri> result = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                        result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                downloadUrl2 = uri.toString();
                                Toast.makeText(getApplicationContext(), "Store Owner Image Upload successful", Toast.LENGTH_LONG).show();
                                upload_Store_Details_to_Firebase(downloadUrl1, downloadUrl2);
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Upload Failed -> Please try again" + e, Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(getApplicationContext(), "Uploading Image and Details...Please Wait and do not exit from page", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else
                Toast.makeText(getApplicationContext(),"Please enter all the details or select Image if not done",Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Doctor Image not Uploaded due to errors", Toast.LENGTH_SHORT).show();
        }
    }

    public void upload_Store_Details_to_Firebase(String url1, String url2) {

        FirebaseDatabase database;
        DatabaseReference myRef;

        database = FirebaseDatabase.getInstance("https://maverick-essentials.firebaseio.com/");
        myRef = database.getReference("Store Details");

        String store_id = store_name.getText().toString() + ", " + store_locality.getText().toString();
        String store_type_str = store_type.getSelectedItem().toString();

        try {
            myRef.child(store_id).setValue(store_username.getText().toString());

            myRef.child(store_id).child("Store Name").setValue(store_name.getText().toString());
            myRef.child(store_id).child("Store Type").setValue(store_type_str);
            myRef.child(store_id).child("Store Owner Name").setValue(store_owner_name.getText().toString());
            myRef.child(store_id).child("Store Owner Phone").setValue(store_owner_phone.getText().toString());
            myRef.child(store_id).child("Store Address").setValue(store_full_address.getText().toString());
            myRef.child(store_id).child("Store City").setValue(store_city.getText().toString());
            myRef.child(store_id).child("Store Locality").setValue(store_locality.getText().toString());
            myRef.child(store_id).child("Store Timings").setValue(store_timings.getText().toString());
            myRef.child(store_id).child("Store Username").setValue(store_username.getText().toString());
            myRef.child(store_id).child("Store Password").setValue(store_password.getText().toString());

            myRef.child(store_id).child("Store Latitude").setValue(store_lat);
            myRef.child(store_id).child("Store Longitude").setValue(store_long);

            myRef.child(store_id).child("Store Image URL").setValue(url1);
            myRef.child(store_id).child("Store Owner Image URL").setValue(url2);

            Toast.makeText(getApplicationContext(),"Data values saved for Store " + store_name.getText().toString(),Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){
            Toast.makeText(getApplicationContext(),"Error in saving Store details",Toast.LENGTH_SHORT).show();
        }
    }
}