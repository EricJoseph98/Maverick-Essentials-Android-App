package maverick.ericjoseph.com.maverickessentials;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    Intent portal;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView store_register_img, customer_register_img, store_login_img, customer_login_img, ar_viewer_img;

        store_register_img = findViewById(R.id.store_signup_img);
        store_register_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                portal = new Intent(getApplicationContext(), RegisterStore.class);
                startActivity(portal);
            }
        });

        store_login_img = findViewById(R.id.store_signin_img);
        store_login_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                portal = new Intent(getApplicationContext(), StoreLogin.class);
                startActivity(portal);
            }
        });

        customer_register_img = findViewById(R.id.customer_signup_img);
        customer_register_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                portal = new Intent(getApplicationContext(), CustomerSignUp.class);
                startActivity(portal);
            }
        });

        customer_login_img = findViewById(R.id.customer_signin_img);
        customer_login_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                portal = new Intent(getApplicationContext(), CustomerSignIn.class);
                startActivity(portal);
            }
        });

        ar_viewer_img = findViewById(R.id.ar_model_viewer_img);
        ar_viewer_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "https://console.echoAR.xyz/arjs?key=fancy-credit-0200";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
    }
}