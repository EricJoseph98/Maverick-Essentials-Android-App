package maverick.ericjoseph.com.maverickessentials;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.Result;

import java.util.ArrayList;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class PurchaseScanner extends AppCompatActivity implements ZXingScannerView.ResultHandler{

    private ZXingScannerView mScannerView;
    public ArrayList<String> purchase_id_list;
    public int flag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_scanner);

        purchase_id_list = new ArrayList<>();
        populatevalues();

        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);                       // Set the scanner view as the content view
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    public void populatevalues(){
        try {
            FirebaseDatabase database = FirebaseDatabase.getInstance("https://maverick-essentials.firebaseio.com/");

            DatabaseReference myref = database.getReference("Item Purchase Details");
            myref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                    while (children.iterator().hasNext()) {
                        DataSnapshot o = children.iterator().next();
                        purchase_id_list.add(o.child("Purchase ID").getValue().toString());
                    }
                   // Toast.makeText(getApplicationContext(), "Purchase ID values populated", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                   // Toast.makeText(getApplicationContext(), "Error in populating Purchase ID values", Toast.LENGTH_SHORT).show();
                }
            });
        }

        catch (Exception e){
           // Toast.makeText(getApplicationContext(),"Error in populating Purchase ID values due to database errors. Please try later.",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void handleResult(Result rawResult) {
        String eventScanString = rawResult.toString();
        // Toast.makeText(getApplicationContext(),"Detected Text: " + eventScanString,Toast.LENGTH_SHORT).show();

        int j;

        int flag = 0;
        for(j = 0; j < purchase_id_list.size(); j++){
            String raw_string = purchase_id_list.get(j);
            if(raw_string.equals(eventScanString) == true){
                // Toast.makeText(getApplicationContext(),"Purchase ID match detected",Toast.LENGTH_SHORT).show();
                flag = 1;
                registerCheckIn(eventScanString);
                break;
            }
        }

        if(flag == 0)
            // Toast.makeText(getApplicationContext(),"No such Purchase ID in database", Toast.LENGTH_LONG).show();

        return;
        // onBackPressed();
    }

    void registerCheckIn(final String x){
        try {
            FirebaseDatabase database = FirebaseDatabase.getInstance();

            final DatabaseReference myref = database.getReference("Item Purchase Details");
            flag = 0;
            myref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                    while (children.iterator().hasNext()) {
                        DataSnapshot o = children.iterator().next();
                        String id = o.child("Purchase ID").getValue().toString();
                        String pickup_status = o.child("Pickup Status").getValue().toString();

                        if(id.equals(x) == true && pickup_status.equals("No") == true) {
                            Toast.makeText(getApplicationContext(), "Pickup Status is in No", Toast.LENGTH_LONG).show();
                            myref.child(id).child("Pickup Status").setValue("Yes");
                            Toast.makeText(getApplicationContext(), "Pickup Status updated to Yes", Toast.LENGTH_SHORT).show();
                            flag = 1;
                            break;
                        }
                    }
                    // Toast.makeText(getApplicationContext(), "Values Populated and available for viewing", Toast.LENGTH_SHORT).show();

                    if(flag == 0){
                        Toast.makeText(getApplicationContext(), "Purchase is either unavailable or has been picked up already", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(getApplicationContext(), "Error in populating values", Toast.LENGTH_SHORT).show();
                }
            });
        }

        catch (Exception e){
            Toast.makeText(getApplicationContext(),"Error in populating database due to Database Errors",Toast.LENGTH_SHORT).show();
        }
    }
}