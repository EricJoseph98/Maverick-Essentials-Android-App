package maverick.ericjoseph.com.maverickessentials;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CustomerSignUp extends AppCompatActivity {

    EditText customer_name, customer_email, customer_phone, customer_drop_off_address, customer_username, customer_password;
    ImageButton customer_registration, customer_clear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_sign_up);

        customer_name = findViewById(R.id.customer_name_txt);
        customer_email = findViewById(R.id.customer_email_txt);
        customer_phone = findViewById(R.id.customer_phone_txt);
        customer_drop_off_address = findViewById(R.id.customer_address_txt);
        customer_username = findViewById(R.id.customer_username_txt);
        customer_password = findViewById(R.id.customer_password_txt);

        customer_registration = findViewById(R.id.customer_register_btn);
        customer_clear = findViewById(R.id.customer_clear_btn);

        customer_registration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                upload_Customer_Details_to_Firebase();
            }
        });

        customer_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customer_name.setText("");
                customer_email.setText("");
                customer_phone.setText("");
                customer_drop_off_address.setText("");
                customer_username.setText("");
                customer_password.setText("");
            }
        });

    }

    public void upload_Customer_Details_to_Firebase() {

        FirebaseDatabase database;
        DatabaseReference myRef;

        database = FirebaseDatabase.getInstance("https://maverick-essentials.firebaseio.com/");
        myRef = database.getReference("Customer Details");

        String customer_id = customer_username.getText().toString();

        try {
            myRef.child(customer_id).setValue(customer_username.getText().toString());

            myRef.child(customer_id).child("Customer Name").setValue(customer_name.getText().toString());
            myRef.child(customer_id).child("Customer Email ID").setValue(customer_email.getText().toString());
            myRef.child(customer_id).child("Customer Phone").setValue(customer_phone.getText().toString());
            myRef.child(customer_id).child("Customer Address").setValue(customer_drop_off_address.getText().toString());
            myRef.child(customer_id).child("Customer Username").setValue(customer_username.getText().toString());
            myRef.child(customer_id).child("Customer Password").setValue(customer_password.getText().toString());

            Toast.makeText(getApplicationContext(),"Customer sign up was successful",Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){
            Toast.makeText(getApplicationContext(),"Error in customer sign up. Please try again",Toast.LENGTH_SHORT).show();
        }
    }
}