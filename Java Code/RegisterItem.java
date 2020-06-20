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
import android.widget.EditText;
import android.widget.ImageButton;
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

public class RegisterItem extends AppCompatActivity {

    EditText item_name, item_brand, item_quantity, item_price, item_expiry_date, item_discount;
    TextView store_title;
    Spinner item_type, item_unit;
    ImageButton register_item, clear_item_details;
    ImageView item_image;
    int PICK_IMAGE_REQUEST = 2099;
    Uri filePath;
    String downloadUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_item);

        // Code to get Store Title (Store Name, Store Locality) from previous activity
        Bundle b = getIntent().getExtras();
        String store_title_text = b.getString("store_title");

        item_name = findViewById(R.id.item_name_txt);
        item_brand = findViewById(R.id.item_brand_txt);
        item_quantity = findViewById(R.id.item_quantity_txt);
        item_price = findViewById(R.id.item_price_txt);
        item_expiry_date = findViewById(R.id.expiry_date_txt);
        item_discount = findViewById(R.id.item_discount_percent_txt);

        store_title = findViewById(R.id.store_title_tv);
        store_title.setText(store_title_text);

        item_type = findViewById(R.id.item_type_spinner);
        item_unit = findViewById(R.id.item_unit_spinner);

        register_item = findViewById(R.id.register_item_details_btn);
        clear_item_details = findViewById(R.id.clear_item_details_btn);

        item_image = findViewById(R.id.item_img);

        String[] item_types_array = {"Grocery", "Eatables", "Beverages", "Medicines", "Furniture", "Toiletries", "Meals", "Utensils", "Electrical", "Electronics", "Pet Food"};
        ArrayAdapter<String> item_types_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, item_types_array);
        item_type.setAdapter(item_types_adapter);

        String[] item_units_array = {"numbers", "packets", "cm", "m", "inch", "gram", "kg", "lbs", "ml", "litres", "ounce"};
        ArrayAdapter<String> item_units_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, item_units_array);
        item_unit.setAdapter(item_units_adapter);

        item_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Item Image"), PICK_IMAGE_REQUEST);
            }
        });

        clear_item_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                item_name.setText("");
                item_brand.setText("");
                item_quantity.setText("");
                item_price.setText("");
                item_brand.setText("");
                item_quantity.setText("");
                item_expiry_date.setText("");
                item_discount.setText("");

                item_type.setSelection(0);
                item_unit.setSelection(0);

                item_image.setImageResource(0);
            }
        });

        register_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                upload_Item_Images_to_Firebase();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();

            try {
                //getting image from gallery
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                //Setting image to ImageView
                item_image.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void upload_Item_Images_to_Firebase(){
        try {
            String item_id = item_name.getText().toString() + "_" + item_brand.getText().toString() + "_" + item_type.getSelectedItem().toString();

            //Storing Image now
            StorageReference storageRef = FirebaseStorage.getInstance("gs://maverick-essentials.appspot.com").getReference("Store Item Images");
            StorageReference childRef = storageRef.child(store_title.getText().toString()).child(item_name.getText().toString() + "_image");
            //uploading the image
            final UploadTask uploadTask = childRef.putFile(filePath);

            if (filePath != null){
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> result = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                        result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                downloadUrl = uri.toString();
                                Toast.makeText(getApplicationContext(), "Item Image Upload successful", Toast.LENGTH_LONG).show();

                                // Time for Uploading Text Details of Item to be registered
                                upload_Item_Details_to_Firebase(downloadUrl);
                            }
                        });


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Item image upload failed. Please try again" + e, Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(getApplicationContext(), "Uploading Item image. Please Wait and do not exit from page", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            else
                Toast.makeText(getApplicationContext(),"Please enter all the details or select image if not done",Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Item Image was not uploaded due to errors", Toast.LENGTH_SHORT).show();
        }
    }

    public void upload_Item_Details_to_Firebase(String url) {

        FirebaseDatabase database;
        DatabaseReference myRef1, myRef2;

        database = FirebaseDatabase.getInstance("https://maverick-essentials.firebaseio.com/");
        myRef1 = database.getReference("Item Details");
        myRef2 = myRef1.child(store_title.getText().toString());

        // String store_id = store_name.getText().toString() + ", " + store_locality.getText().toString();
        // String store_type_str = store_type.getSelectedItem().toString();

        String item_id = item_name.getText().toString() + "_" + item_brand.getText().toString() + "_" + item_type.getSelectedItem().toString();

        try {
            myRef2.child(item_id).setValue(item_name.getText().toString());

            myRef2.child(item_id).child("Item Name").setValue(item_name.getText().toString());
            myRef2.child(item_id).child("Item Image URL").setValue(url);
            myRef2.child(item_id).child("Item Brand").setValue(item_brand.getText().toString());
            myRef2.child(item_id).child("Item Type").setValue(item_type.getSelectedItem().toString());
            myRef2.child(item_id).child("Item Quantity Unit").setValue(item_unit.getSelectedItem().toString());
            myRef2.child(item_id).child("Item Quantity").setValue(item_quantity.getText().toString());
            myRef2.child(item_id).child("Item Price").setValue(item_price.getText().toString());
            myRef2.child(item_id).child("Item Expiry Date").setValue(item_expiry_date.getText().toString());
            myRef2.child(item_id).child("Item Discount Percentage").setValue(item_discount.getText().toString());

            Toast.makeText(getApplicationContext(),"Data values saved for Item " + item_name.getText().toString(),Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){
            Toast.makeText(getApplicationContext(),"Error in saving Item Details",Toast.LENGTH_SHORT).show();
        }
    }
}