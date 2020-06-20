package maverick.ericjoseph.com.maverickessentials;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.squareup.picasso.Picasso;

public class StoreView extends AppCompatActivity {

    public String store_names_array[], store_locality_array[], store_type_array[], store_image_url_array[];
    public int erpos = 0;
    public String email, username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_view);

        // Code to get Store Title (Store Name, Store Locality) from previous activity
        Bundle b = getIntent().getExtras();
        username = b.getString("customer_username");
        email = b.getString("customer_emailID");

        Toast.makeText(getApplicationContext(), "Welcome " + username, Toast.LENGTH_LONG).show();

        FirebaseDatabase database0 = null;
        DatabaseReference myref0 = null;

        // Going through the Firebase Database to count the number of shops
        try {
            database0 = FirebaseDatabase.getInstance("https://maverick-essentials.firebaseio.com/");
            myref0 = database0.getReference("Store Details");

            myref0.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    erpos = 0;
                    Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                    while (children.iterator().hasNext()) {
                        DataSnapshot o = children.iterator().next();
                        ++erpos;
                    }
                    Toast.makeText(getApplicationContext(), "Values Populated and available for viewing. Count = " + erpos, Toast.LENGTH_SHORT).show();

                    store_names_array = new String[erpos];
                    store_locality_array = new String[erpos];
                    store_type_array = new String[erpos];
                    store_image_url_array = new String[erpos];

                    // Getting values from the Firebase database
                    try {
                        FirebaseDatabase database = FirebaseDatabase.getInstance("https://maverick-essentials.firebaseio.com/");
                        DatabaseReference myref = database.getReference("Store Details");

                        myref.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                erpos = 0;
                                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                                while (children.iterator().hasNext()) {
                                    Toast.makeText(getApplicationContext(), "Please wait data values are loading", Toast.LENGTH_SHORT).show();
                                    DataSnapshot o = children.iterator().next();

                                    store_names_array[erpos] = o.child("Store Name").getValue().toString();
                                    store_locality_array[erpos] = o.child("Store Locality").getValue().toString();
                                    store_type_array[erpos] = o.child("Store Type").getValue().toString();
                                    store_image_url_array[erpos] = o.child("Store Image URL").getValue().toString();

                                    ++erpos;
                                }
                                Toast.makeText(getApplicationContext(), "Values Populated and available for viewing", Toast.LENGTH_SHORT).show();

                                CustomList adapter = new CustomList(StoreView.this, store_names_array, store_image_url_array, store_type_array, store_locality_array);
                                ListView store_list = findViewById(R.id.store_listview);
                                store_list.setAdapter(adapter);

                                //fillTextViews(0);
                                //adapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.select_dialog_item,names_array);
                                //search_bar.setAdapter(adapter);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(getApplicationContext(), "Error in populating values", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "Error in populating database due to Database Errors", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(getApplicationContext(), "Error in populating values", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error in populating database due to Database Errors", Toast.LENGTH_SHORT).show();
        } // End of code for counting records
    }

    class CustomList extends ArrayAdapter<String> {

        private final Activity context;
        private String[] store_names = null;
        private String[] store_localities = null;
        private String[] store_types = null;
        private String[] store_image_urls = null;

        public CustomList(Activity context, String[] store_names, String[] store_image_urls, String[] store_types, String[] store_localities) {
            super(context, R.layout.store_list_layout, store_names);
            this.context = context;
            this.store_names = store_names;
            this.store_image_urls = store_image_urls;
            this.store_types = store_types;
            this.store_localities = store_localities;
        }

        @Override
        public View getView(final int position, View view, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();

            View rowView = inflater.inflate(R.layout.store_list_layout, null, true);
            TextView store_name = (TextView) rowView.findViewById(R.id.store_name_list);
            TextView store_type = (TextView) rowView.findViewById(R.id.store_type_list);
            final TextView store_locality = (TextView) rowView.findViewById(R.id.store_locality_list);
            ImageView store_image = (ImageView) rowView.findViewById(R.id.store_img);

            store_name.setText(store_names[position]);
            store_type.setText(store_types[position]);
            store_locality.setText(store_localities[position]);

            Picasso.get().load(store_image_urls[position]).placeholder(R.drawable.loading_icon).into(store_image);

            store_name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(context, ItemViewer.class);
                    String store_title_str = store_names[position] + ", " + store_localities[position];
                    i.putExtra("store_title", store_title_str);
                    i.putExtra("customer_emailID", email);
                    startActivity(i);
                }
            });

            return rowView;
        }
    }
}