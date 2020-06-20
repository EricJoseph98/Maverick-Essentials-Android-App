package maverick.ericjoseph.com.maverickessentials;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CustomerSignIn extends AppCompatActivity {

    EditText customer_username, customer_password;
    Button customer_signin;
    ImageButton store_view;
    TextView view_store_label;
    String username, email;

    int erpos = 0, login_pos = 0;

    String customer_usernames[] = new String[100];
    String customer_passwords[] = new String[100];
    String customer_names[] = new String[100];
    String customer_emailIDs[] = new String[100];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_sign_in);

        customer_username = findViewById(R.id.customer_username_txt);
        customer_password = findViewById(R.id.customer_password_txt);
        customer_signin = findViewById(R.id.customer_signin_btn);
        view_store_label = findViewById(R.id.view_stores_lbl);

        store_view = findViewById(R.id.store_view_btn);
        store_view.setEnabled(false);
        store_view.setVisibility(View.INVISIBLE);
        view_store_label.setVisibility(View.INVISIBLE);

        customer_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                get_Customer_Details_from_Firebase();
            }
        });

        store_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), StoreView.class);
                i.putExtra("customer_username", username);
                i.putExtra("customer_emailID", email);
                startActivity(i);
                startActivity(i);
            }
        });
    }

    public void checkCustomerCredentials(int size){
        String us = customer_username.getText().toString();
        String pw = customer_password.getText().toString();
        int i, flag = 0;

        for(i=0; i<size; i++){
            if(us.equals(customer_usernames[i]) == true && pw.equals(customer_passwords[i]) == true){
                username = customer_usernames[i];
                email = customer_emailIDs[i];
                flag = 1;
                break;
            }
        }

        if(flag == 0){
            Toast.makeText(getApplicationContext(), "Customer sign in failed. Please enter correct username/password", Toast.LENGTH_LONG).show();
        }

        else if(flag == 1){
            Toast.makeText(getApplicationContext(), "Customer sign in is successful. Please proceed to shop with us.", Toast.LENGTH_LONG).show();
            store_view.setEnabled(true);
            store_view.setVisibility(View.VISIBLE);
            view_store_label.setVisibility(View.VISIBLE);
            login_pos = i;
        }
        else {
            return;
        }
    }

    public void get_Customer_Details_from_Firebase(){
        try {
            FirebaseDatabase database = FirebaseDatabase.getInstance("https://maverick-essentials.firebaseio.com/");

            DatabaseReference myref = database.getReference("Customer Details");
            myref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    erpos = 0;
                    Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                    while (children.iterator().hasNext()) {
                        DataSnapshot o = children.iterator().next();
                        customer_names[erpos] = o.child("Customer Name").getValue().toString();
                        customer_usernames[erpos] = o.child("Customer Username").getValue().toString();
                        customer_passwords[erpos] = o.child("Customer Password").getValue().toString();
                        customer_emailIDs[erpos] = o.child("Customer Email ID").getValue().toString();

                        ++erpos;
                    }
                    Toast.makeText(getApplicationContext(), "Customer data values populated", Toast.LENGTH_SHORT).show();
                    checkCustomerCredentials(erpos);
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
}