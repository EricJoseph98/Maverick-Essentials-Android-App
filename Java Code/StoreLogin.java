package maverick.ericjoseph.com.maverickessentials;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StoreLogin extends AppCompatActivity {

    EditText store_username, store_password;
    TextView item_add_tv, item_edit_tv, purchase_scan_tv, upload_model_tv;
    Button store_login;
    ImageView add_items, edit_items, purchase_scan, upload_3d_model;
    int erpos = 0, login_pos = 0;

    String store_usernames[] = new String[1000];
    String store_passwords[] = new String[1000];
    String store_names[] = new String[1000];
    String store_localities[] = new String[1000];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_login);

        store_username = findViewById(R.id.store_username_txt);
        store_password = findViewById(R.id.store_password_txt);

        add_items = findViewById(R.id.item_add_img);
        edit_items = findViewById(R.id.item_edit_img);
        purchase_scan = findViewById(R.id.purchase_scanner_img);
        upload_3d_model = findViewById(R.id.upload_3d_model_button);

        add_items.setVisibility(View.INVISIBLE);
        edit_items.setVisibility(View.INVISIBLE);
        purchase_scan.setVisibility(View.INVISIBLE);
        upload_3d_model.setVisibility(View.INVISIBLE);

        item_add_tv = findViewById(R.id.item_add_lbl);
        item_edit_tv = findViewById(R.id.item_edit_lbl);
        purchase_scan_tv = findViewById(R.id.purchase_scanner_lbl);
        upload_model_tv = findViewById(R.id.upload_3d_model_desc_tv);

        item_add_tv.setVisibility(View.INVISIBLE);
        item_edit_tv.setVisibility(View.INVISIBLE);
        purchase_scan_tv.setVisibility(View.INVISIBLE);
        upload_model_tv.setVisibility(View.INVISIBLE);

        add_items.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), RegisterItem.class);
                String store_title = store_names[login_pos] + ", " + store_localities[login_pos];
                i.putExtra("store_title", store_title);
                i.putExtra("store_username", store_usernames[login_pos]);
                startActivity(i);
            }
        });

        edit_items.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), EditItem.class);
                String store_title = store_names[login_pos] + ", " + store_localities[login_pos];
                i.putExtra("store_title", store_title);
                i.putExtra("store_username", store_usernames[login_pos]);
                startActivity(i);
            }
        });

        purchase_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), PurchaseScanner.class);
                String store_title = store_names[login_pos] + ", " + store_localities[login_pos];
                i.putExtra("store_title", store_title);
                i.putExtra("store_username", store_usernames[login_pos]);
                startActivity(i);
            }
        });

        upload_3d_model.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), Item3DModelUpload.class);
                String store_title = store_names[login_pos] + ", " + store_localities[login_pos];
                i.putExtra("store_title", store_title);
                i.putExtra("store_username", store_usernames[login_pos]);
                startActivity(i);
            }
        });

        store_login = findViewById(R.id.store_login_button);
        store_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    FirebaseDatabase database = FirebaseDatabase.getInstance("https://maverick-essentials.firebaseio.com/");

                    DatabaseReference myref = database.getReference("Store Details");
                    myref.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            erpos = 0;
                            Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                            while (children.iterator().hasNext()) {
                                DataSnapshot o = children.iterator().next();
                                store_names[erpos] = o.child("Store Name").getValue().toString();
                                store_localities[erpos] = o.child("Store Locality").getValue().toString();
                                store_usernames[erpos] = o.child("Store Username").getValue().toString();
                                store_passwords[erpos] = o.child("Store Password").getValue().toString();

                                ++erpos;
                            }
                            Toast.makeText(getApplicationContext(), "Store data values populated", Toast.LENGTH_SHORT).show();
                            checkCredentials(erpos);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(getApplicationContext(), "Error in populating values", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                catch (Exception e){
                    Toast.makeText(getApplicationContext(),"Error in populating database due to database errors. Please try later.",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    public void checkCredentials(int size){
        String us = store_username.getText().toString();
        String pw = store_password.getText().toString();
        int i, flag = 0;

        for(i=0; i<size; i++){
            if(us.equals(store_usernames[i]) == true && pw.equals(store_passwords[i]) == true){
                flag = 1;
                break;
            }
        }

        if(flag == 0){
            Toast.makeText(getApplicationContext(), "Store Sign In failed. Please enter correct username/password", Toast.LENGTH_LONG).show();
        }

        else {
            Toast.makeText(getApplicationContext(), "Store Sign In successful", Toast.LENGTH_LONG).show();
            add_items.setVisibility(View.VISIBLE);
            edit_items.setVisibility(View.VISIBLE);
            item_add_tv.setVisibility(View.VISIBLE);
            item_edit_tv.setVisibility(View.VISIBLE);
            purchase_scan.setVisibility(View.VISIBLE);
            purchase_scan_tv.setVisibility(View.VISIBLE);
            upload_3d_model.setVisibility(View.VISIBLE);
            upload_model_tv.setVisibility(View.VISIBLE);
            login_pos = i;
        }
    }
}