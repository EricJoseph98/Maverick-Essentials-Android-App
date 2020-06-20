package maverick.ericjoseph.com.maverickessentials;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class EditItem extends AppCompatActivity {

    ArrayList<String> item_names_array, item_brands_array, item_types_array, item_quantities_array, item_expiry_dates_array,
            item_prices_array, item_units_array, item_discounts_array, item_image_urls_array;

    EditText quantity_text, expiry_date_text, price_text, discount_text;
    TextView names_text, brand_text, type_text, unit_text;

    ImageButton next_item, prev_item, edit_item, delete_item, save_edits;

    ImageView item_imageview;

    int item_position = 0;
    int total_items_number = 0;

    public String store_title_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);

        Bundle b = getIntent().getExtras();
        store_title_text = b.getString("store_title");
        TextView store_title = findViewById(R.id.store_title_tv_for_edit);
        store_title.setText(store_title_text);

        item_imageview = findViewById(R.id.item_img_for_edit);

        next_item = findViewById(R.id.next_item_button);
        next_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(item_position < total_items_number - 1){
                    textFieldsDisabled();
                    item_position++;
                    populateDataFields(item_position);
                }
                else {
                    Toast.makeText(getApplicationContext(), "You have reached the end of the list", Toast.LENGTH_SHORT).show();
                }
            }
        });

        prev_item = findViewById(R.id.previous_item_button);
        prev_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(item_position > 0){
                    textFieldsDisabled();
                    --item_position;
                    populateDataFields(item_position);
                }
                else {
                    Toast.makeText(getApplicationContext(), "You have reached the beginning of the list", Toast.LENGTH_SHORT).show();
                }
            }
        });

        edit_item = findViewById(R.id.edit_item_btn);
        edit_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textFieldsEnabled();
            }
        });

        save_edits = findViewById(R.id.save_item_edits_btn);
        save_edits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String product_name = names_text.getText().toString() + "_" + brand_text.getText().toString() + "_"
                        + type_text.getText().toString();
                editDataFields(store_title_text, product_name);
            }
        });

        delete_item = findViewById(R.id.delete_item_details_btn_for_edit);
        delete_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String product_name = names_text.getText().toString() + "_" + brand_text.getText().toString() + "_"
                        + type_text.getText().toString();
                deleteDataFields(store_title_text, product_name);
            }
        });

        initializeArrayLists();

        names_text = findViewById(R.id.item_name_edit_tv);
        brand_text = findViewById(R.id.item_brand_edit_tv);
        unit_text = findViewById(R.id.item_quantity_unit_edit_tv);
        type_text = findViewById(R.id.item_type_edit_tv);

        quantity_text = findViewById(R.id.item_quantity_txt_for_edit);
        expiry_date_text = findViewById(R.id.expiry_date_txt_for_edit);
        price_text = findViewById(R.id.item_price_txt_for_edit);
        discount_text = findViewById(R.id.item_discount_percent_txt_for_edit);

        populateDatabaseValues(store_title_text);
    }

    private void populateDataFields(int position){
        names_text.setText(item_names_array.get(position));
        brand_text.setText(item_brands_array.get(position));
        unit_text.setText(item_units_array.get(position));
        type_text.setText(item_types_array.get(position));

        quantity_text.setText(item_quantities_array.get(position));
        expiry_date_text.setText(item_expiry_dates_array.get(position));
        price_text.setText(item_prices_array.get(position));
        discount_text.setText(item_discounts_array.get(position));

        Picasso.get().load(item_image_urls_array.get(position)).placeholder(R.drawable.loading_icon).into(item_imageview);
    }

    void textFieldsEnabled(){
        quantity_text.setEnabled(true);
        price_text.setEnabled(true);
        expiry_date_text.setEnabled(true);
        discount_text.setEnabled(true);
    }

    void textFieldsDisabled(){
        quantity_text.setEnabled(false);
        price_text.setEnabled(false);
        expiry_date_text.setEnabled(false);
        discount_text.setEnabled(false);
    }

    void initializeArrayLists(){
        item_names_array = new ArrayList<>();
        item_brands_array = new ArrayList<>();
        item_types_array = new ArrayList<>();
        item_quantities_array = new ArrayList<>();
        item_expiry_dates_array = new ArrayList<>();
        item_prices_array = new ArrayList<>();
        item_units_array = new ArrayList<>();
        item_discounts_array = new ArrayList<>();
        item_image_urls_array = new ArrayList<>();
    }

    private void populateDatabaseValues(String store_title_text){
        // Getting values from the Firebase database
        total_items_number = 0;

        try {
            FirebaseDatabase database = FirebaseDatabase.getInstance("https://maverick-essentials.firebaseio.com/");
            DatabaseReference myref = database.getReference("Item Details/" + store_title_text);

            myref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                    while (children.iterator().hasNext()) {
                        Toast.makeText(getApplicationContext(), "Please wait data values of items are loading", Toast.LENGTH_SHORT).show();
                        DataSnapshot o = children.iterator().next();

                        item_names_array.add(o.child("Item Name").getValue().toString());
                        item_brands_array.add(o.child("Item Brand").getValue().toString());
                        item_types_array.add(o.child("Item Type").getValue().toString());
                        item_quantities_array.add(o.child("Item Quantity").getValue().toString());
                        item_expiry_dates_array.add(o.child("Item Expiry Date").getValue().toString());
                        item_prices_array.add(o.child("Item Price").getValue().toString());
                        item_units_array.add(o.child("Item Quantity Unit").getValue().toString());
                        item_discounts_array.add(o.child("Item Discount Percentage").getValue().toString());
                        item_image_urls_array.add(o.child("Item Image URL").getValue().toString());

                        total_items_number++;
                    }

                    Toast.makeText(getApplicationContext(), "Item Values Populated and available for viewing", Toast.LENGTH_LONG).show();
                    textFieldsDisabled();
                    populateDataFields(0);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(getApplicationContext(), "Error in populating values", Toast.LENGTH_SHORT).show();
                }
            });
        }

        catch (Exception e){
            Toast.makeText(getApplicationContext(),"Error in populating values due to database errors",Toast.LENGTH_SHORT).show();
        }
    }

    void editDataFields(final String store_name, final String product_name){
        try {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            final DatabaseReference myref0 = database.getReference("Item Details").child(store_name).child(product_name);
            myref0.getRef().child("Item Discount Percentage").setValue(discount_text.getText().toString());
            myref0.getRef().child("Item Expiry Date").setValue(expiry_date_text.getText().toString());
            myref0.getRef().child("Item Price").setValue(price_text.getText().toString());
            myref0.getRef().child("Item Quantity").setValue(quantity_text.getText().toString());
            Toast.makeText(getApplicationContext(),"Item values updated",Toast.LENGTH_SHORT).show();
        }

        catch (Exception e){
            Toast.makeText(getApplicationContext(),"Error in updating Item Details",Toast.LENGTH_SHORT).show();
        }
        initializeArrayLists();
        populateDatabaseValues(store_title_text);
    }

    void deleteDataFields(final String store_name, final String product_name){
        try {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference delete_ref = database.getReference("Item Details").child(store_name).child(product_name);
            delete_ref.setValue(null);
            Toast.makeText(getApplicationContext(),"Item Deleted",Toast.LENGTH_SHORT).show();
        }

        catch (Exception e){
            Toast.makeText(getApplicationContext(),"Error in deleting Item Details",Toast.LENGTH_SHORT).show();
        }
        initializeArrayLists();
        populateDatabaseValues(store_title_text);
    }
}