package maverick.ericjoseph.com.maverickessentials;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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

public class Item3DModelUpload extends AppCompatActivity {

    EditText upload_item_name, upload_item_brand;
    Spinner upload_item_type;
    Button zip_upload;

    int PICK_ZIP_CODE = 2099;

    ProgressBar progressBar;

    //the firebase objects for storage and database
    StorageReference mStorageReference;

    String store_title_text;
    String downloadUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item3_dmodel_upload);

        // Code to get Store Title (Store Name, Store Locality) from previous activity
        Bundle b = getIntent().getExtras();
        store_title_text = b.getString("store_title");
        TextView store_title_tv = findViewById(R.id.upload_store_title_tv);
        store_title_tv.setText(store_title_text);

        upload_item_name = findViewById(R.id.upload_item_name_et);
        upload_item_brand = findViewById(R.id.upload_item_brand_et);
        upload_item_type = findViewById(R.id.upload_item_type_spinner);

        String[] item_types_array = {"Grocery", "Eatables", "Beverages", "Medicines", "Furniture", "Toiletries", "Meals", "Utensils", "Electrical", "Electronics", "Pet Food"};
        ArrayAdapter<String> item_types_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, item_types_array);
        upload_item_type.setAdapter(item_types_adapter);

        zip_upload = findViewById(R.id.upload_zip_btn);
        zip_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //for greater than lolipop versions we need the permissions asked on runtime
                //so if the permission is not available user will go to the screen to allow storage permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                    return;
                }

                //creating an intent for file chooser
                Intent intent = new Intent();
                intent.setType("application/zip");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Zip with 3D model files of Item"), PICK_ZIP_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //when the user choses the file
        if (requestCode == PICK_ZIP_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            //if a file is selected
            if (data.getData() != null) {
                //uploading the file
                uploadFile(data.getData());
            }
            else{
                Toast.makeText(this, "No file chosen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadFile(Uri data) {
        mStorageReference = FirebaseStorage.getInstance().getReference("Item 3D Model Zip Files").child(store_title_text);

        String id = upload_item_name.getText().toString() + "_" + upload_item_brand.getText().toString() + "_" + upload_item_type.getSelectedItem().toString();
        final UploadTask uploadTask = mStorageReference.child(id).putFile(data);
        if (data != null){
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> result = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            downloadUrl = uri.toString();
                            Toast.makeText(getApplicationContext(), "Item 3D Model Zip file upload successful", Toast.LENGTH_LONG).show();

                            // Time for Uploading Text Details of Item to be registered
                            upload_Item_Details_to_Firebase(downloadUrl);
                        }
                    });


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), "Item 3D Model Zip file upload failed. Please try again" + e, Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(getApplicationContext(), "Uploading Item 3D Model Zip file. Please Wait and do not exit from page", Toast.LENGTH_SHORT).show();
                }
            });
        }

        else
            Toast.makeText(getApplicationContext(),"Please enter all the details or select Item 3D Model Zip file if not done",Toast.LENGTH_SHORT).show();

    }

    public void upload_Item_Details_to_Firebase(String url) {

        FirebaseDatabase database;
        DatabaseReference myRef1, myRef2;

        database = FirebaseDatabase.getInstance("https://maverick-essentials.firebaseio.com/");
        myRef1 = database.getReference("Item 3D Model Files Details");
        myRef2 = myRef1.child(store_title_text);

        String item_id = upload_item_name.getText().toString() + "_" + upload_item_brand.getText().toString() + "_" + upload_item_type.getSelectedItem().toString();

        try {
            myRef2.child(item_id).setValue(upload_item_name.getText().toString());

            myRef2.child(item_id).child("Item Name").setValue(upload_item_name.getText().toString());
            myRef2.child(item_id).child("Item 3D Model File URL").setValue(url);
            myRef2.child(item_id).child("Item Brand").setValue(upload_item_brand.getText().toString());
            myRef2.child(item_id).child("Item Type").setValue(upload_item_type.getSelectedItem().toString());

            Toast.makeText(getApplicationContext(),"Data values saved for Item " + upload_item_name.getText().toString(),Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){
            Toast.makeText(getApplicationContext(),"Error in saving Item Details",Toast.LENGTH_SHORT).show();
        }
    }
}