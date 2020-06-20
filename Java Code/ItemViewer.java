package maverick.ericjoseph.com.maverickessentials;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

public class ItemViewer extends AppCompatActivity {

    Bitmap qr_code_bitmap_image = null;
    File qr_code_imageFile;
    String qr_code_filename;

    String purchase_id = null;

    public String[] item_names_array = null, item_brands_array = null, item_types_array = null, item_quantities_array = null,
            item_expiry_dates_array = null, item_prices_array = null, item_units_array = null, item_discounts_array = null, item_image_urls_array = null;
    public int erpos = 0;

    public ArrayList<String> cart_items_list = new ArrayList<>();
    public String store_title_text, customer_emailID;
    public TextView view_items, view_cart, finalize_purchase;
    public AlertDialog.Builder alert;
    public ListView common_list_view;
    public String list_status;
    public float grand_total = 0;
    public ArrayList<CartItem> cartItem;

    public String purchase_text = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_viewer);

        Bundle b = getIntent().getExtras();
        store_title_text = b.getString("store_title");
        customer_emailID = b.getString("customer_emailID");

        TextView store_title = findViewById(R.id.store_title_textview);
        store_title.setText(store_title_text);

        FirebaseDatabase database0 = null;
        DatabaseReference myref0 = null, myref1 = null;

        common_list_view = findViewById(R.id.item_list_view);

        alert = new AlertDialog.Builder(getApplicationContext());

        view_items = findViewById(R.id.view_items_tv);
        view_cart = findViewById(R.id.view_cart_tv);
        finalize_purchase = findViewById(R.id.purchase_cart_tv);

        list_status = "Items_List";

        cartItem = new ArrayList<>();

        // Going through the Firebase Database to count the number of shops
        try {
            database0 = FirebaseDatabase.getInstance("https://maverick-essentials.firebaseio.com/");
            myref0 = database0.getReference("Item Details/" + store_title_text);

            myref0.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    erpos = 0;
                    Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                    while (children.iterator().hasNext()) {
                        DataSnapshot o = children.iterator().next();
                        ++erpos;
                    }

                    item_names_array = new String[erpos];
                    item_brands_array = new String[erpos];
                    item_types_array = new String[erpos];
                    item_quantities_array = new String[erpos];
                    item_expiry_dates_array = new String[erpos];
                    item_prices_array = new String[erpos];
                    item_units_array = new String[erpos];
                    item_discounts_array = new String[erpos];
                    item_image_urls_array = new String[erpos];

                    Toast.makeText(getApplicationContext(), "Values Populated and available for viewing. Count = " + erpos, Toast.LENGTH_SHORT).show();
                    populateCustomItemListView(store_title_text);
                    list_status = "Items_List";
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
        // End of code for counting records

        view_items.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                populateCustomItemListView(store_title_text);
                list_status = "Items_List";
            }
        });

        view_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, cart_items_list);
                common_list_view.setAdapter(adapter);
                Toast.makeText(getApplicationContext(), "Your cart is now visible", Toast.LENGTH_SHORT).show();
                list_status = "Cart_List";
            }
        });

        finalize_purchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String id = store_title_text + "_" + System.currentTimeMillis();
                uploadQR(id);
            }
        });

        common_list_view.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(list_status.equals("Cart_List") == true){

                    cart_items_list.remove(i);
                    grand_total -= cartItem.get(i).total_price;
                    cartItem.remove(i);

                    Toast.makeText(getApplicationContext(), "Selected Item removed from cart", Toast.LENGTH_SHORT).show();

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, cart_items_list);
                    common_list_view.setAdapter(adapter);
                    list_status = "Cart_List";
                }
                return false;
            }
        });
    }

    private void populateCustomItemListView(String store_title_text){
        // Getting values from the Firebase database
        try {
               FirebaseDatabase database = FirebaseDatabase.getInstance("https://maverick-essentials.firebaseio.com/");
               DatabaseReference myref = database.getReference("Item Details/" + store_title_text);

               myref.addValueEventListener(new ValueEventListener() {
                   @Override
                   public void onDataChange(DataSnapshot dataSnapshot) {
                       erpos = 0;
                       Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                       while (children.iterator().hasNext()) {
                            Toast.makeText(getApplicationContext(), "Please wait data values of items are loading", Toast.LENGTH_SHORT).show();
                            DataSnapshot o = children.iterator().next();

                            item_names_array[erpos] = o.child("Item Name").getValue().toString();
                            item_brands_array[erpos] = o.child("Item Brand").getValue().toString();
                            item_types_array[erpos] = o.child("Item Type").getValue().toString();
                            item_quantities_array[erpos] = o.child("Item Quantity").getValue().toString();
                            item_expiry_dates_array[erpos] = o.child("Item Expiry Date").getValue().toString();
                            item_prices_array[erpos] = o.child("Item Price").getValue().toString();
                            item_units_array[erpos] = o.child("Item Quantity Unit").getValue().toString();
                            item_discounts_array[erpos] = o.child("Item Discount Percentage").getValue().toString();
                            item_image_urls_array[erpos] = o.child("Item Image URL").getValue().toString();

                            ++erpos;
                       }

                       Toast.makeText(getApplicationContext(), "Item Values Populated and available for viewing", Toast.LENGTH_LONG).show();


                       CustomItemList adapter = new CustomItemList(ItemViewer.this, item_names_array, item_brands_array, item_types_array, item_quantities_array, item_expiry_dates_array, item_prices_array, item_units_array, item_discounts_array, item_image_urls_array);
                       common_list_view.setAdapter(adapter);
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

    class CustomItemList extends ArrayAdapter<String> {

        private final Activity context;
        private String[] item_names, item_brands, item_types, item_quantities, item_expiry_dates, item_prices,
                item_units, item_discounts, item_image_urls;

        int[] selected_item_numbers;
        float[] discounted_price_individual;

        public CustomItemList(Activity context, String[] item_names, String[] item_brands, String[] item_types, String[] item_quantities, String[] item_expiry_dates, String[] item_prices, String[] item_units, String[] item_discounts, String[] item_image_urls) {
            super(context, R.layout.store_item_list_layout, item_names);
            this.context = context;

            this.item_names = item_names;
            this.item_brands = item_brands;
            this.item_types = item_types;
            this.item_quantities = item_quantities;
            this.item_expiry_dates = item_expiry_dates;
            this.item_prices = item_prices;
            this.item_units = item_units;
            this.item_discounts = item_discounts;
            this.item_image_urls = item_image_urls;

            int len = item_names.length;
            selected_item_numbers = new int[len];
            discounted_price_individual = new float[len];
        }

        @Override
        public View getView(final int position, View view, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();

            View rowView = inflater.inflate(R.layout.store_item_list_layout, null, true);
            final TextView item_name, item_brand, item_type, item_quantity, item_expiry, item_price,
                    item_unit, item_discount, item_selected_quantity, item_resultant_price;

            item_name = rowView.findViewById(R.id.item_name_tv);
            item_brand = rowView.findViewById(R.id.item_brand_tv);
            item_type = rowView.findViewById(R.id.item_type_tv);
            item_quantity = rowView.findViewById(R.id.item_quantity_tv);
            item_expiry = rowView.findViewById(R.id.item_expiry_tv);
            item_price = rowView.findViewById(R.id.item_price_tv);
            item_unit = rowView.findViewById(R.id.item_unit_tv);
            item_discount = rowView.findViewById(R.id.item_discount_tv);
            item_selected_quantity = rowView.findViewById(R.id.selected_quantity_tv);
            item_resultant_price = rowView.findViewById(R.id.resultant_price_tv);
            item_resultant_price.setText("Price: Rs. 0");

            ImageButton increment_qty_btn = rowView.findViewById(R.id.increment_quantity_btn);
            increment_qty_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(selected_item_numbers[position] < Integer.valueOf(item_quantities[position])) {
                        ++selected_item_numbers[position];
                        item_selected_quantity.setText(String.valueOf(selected_item_numbers[position]));
                        float price_val = Float.valueOf(item_prices[position]);
                        float temp = price_val;
                        price_val = price_val - ((temp * Integer.valueOf(item_discounts[position]))/100);
                        discounted_price_individual[position] = price_val;
                        price_val = price_val * selected_item_numbers[position];
                        item_resultant_price.setText("Price after discount - Rs. " + Math.round(price_val));
                    }
                }
            });

            ImageButton decrement_qty_btn = rowView.findViewById(R.id.decrement_quantity_btn);
            decrement_qty_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(selected_item_numbers[position] > 0) {
                        --selected_item_numbers[position];
                        item_selected_quantity.setText(String.valueOf(selected_item_numbers[position]));
                        float price_val = Float.valueOf(item_prices[position]);
                        float temp = price_val;
                        price_val = price_val - ((temp * Integer.valueOf(item_discounts[position]))/100);
                        price_val = price_val * selected_item_numbers[position];
                        item_resultant_price.setText("Price after discount - Rs. " + Math.round(price_val));
                    }
                }
            });

            ImageButton add_to_cart = rowView.findViewById(R.id.add_to_cart_btn);
            add_to_cart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (selected_item_numbers[position] > 0) {

                        float discounted_item_price = Math.round(discounted_price_individual[position]);
                        float total_price = Math.round(Integer.valueOf(item_selected_quantity.getText().toString()) * discounted_item_price);

                        addToCart(item_names[position], item_types[position], item_brands[position],
                                Integer.valueOf(item_selected_quantity.getText().toString()),
                                Integer.valueOf(item_discounts[position]), discounted_item_price, total_price);

                        // Toast.makeText(getApplicationContext(), "Item added to cart", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            ImageView item_image = rowView.findViewById(R.id.item_imageview);

            item_name.setText(item_names[position]);
            item_brand.setText(item_brands[position]);
            item_type.setText(item_types[position]);
            item_quantity.setText("Stock: " + item_quantities[position] + " " + item_units[position]);
            item_expiry.setText("Expiry: " + item_expiry_dates[position]);
            item_price.setText("Rs. " + item_prices[position]);
            item_unit.setText("/" + item_units[position]);
            item_discount.setText(item_discounts[position] + "% off.");

            item_selected_quantity.setText("0");
            item_resultant_price.setText("0");

            Picasso.get().load(item_image_urls[position]).placeholder(R.drawable.loading_icon).into(item_image);

            return rowView;
        }
    }

    public class CartItem {
        public String item_name, item_brand, item_type;
        public int item_quantity, item_discount_percent;
        public float item_price;
        public float total_price;

        void add(String name, String type, String brand, int qty, int discount, float price, float total){
            item_name = name;
            item_brand = brand;
            item_type = type;
            item_discount_percent = discount;
            item_quantity = qty;
            item_price = price;
            total_price = total;
        }
    }

    public void addToCart(String item_name, String item_type, String item_brand, int selected_quantity, int discount_percent, float discounted_price, float total_price) {
        String cart_item_text = item_name + " | Quantity: " + selected_quantity + " | Price: " + discounted_price +  " | Total Price: " + total_price;
        cart_items_list.add(cart_item_text);
        grand_total += total_price;
        Toast.makeText(getApplicationContext(), "Item added to your cart", Toast.LENGTH_SHORT).show();

        CartItem temp = new CartItem();
        temp.add(item_name, item_type, item_brand, selected_quantity, discount_percent, discounted_price, total_price);
        cartItem.add(temp);
    }

    public void upload_Item_Purchase_Details_to_Firebase(String id, String qrcode_url) {

        FirebaseDatabase database;
        DatabaseReference myRef1, myRef2;

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String currentDate = sdf.format(new Date());

        database = FirebaseDatabase.getInstance("https://maverick-essentials.firebaseio.com/");
        myRef1 = database.getReference("Item Purchase Details");
        myRef2 = myRef1.child(id);

        myRef2.child("Purchase ID").setValue(id);
        myRef2.child("Purchase Date").setValue(currentDate);
        myRef2.child("Purchase Store with location").setValue(store_title_text);
        myRef2.child("Purchase QR Code URL").setValue(qrcode_url);
        myRef2.child("Pickup Status").setValue("No");

        int i;

        try {

            for (i = 0; i < cartItem.size(); i++) {
                String item_id = "Item " + (i + 1);
                myRef2.child(item_id).setValue("Item " + (i + 1));
                // myRef2.child(item_id).child("Purchase Date").setValue(currentDate);
                // myRef2.child(item_id).child("Purchase Store").setValue(store_title_text);
                myRef2.child(item_id).child("Item Name").setValue(cartItem.get(i).item_name);
                myRef2.child(item_id).child("Item Brand").setValue(cartItem.get(i).item_brand);
                myRef2.child(item_id).child("Item Type").setValue(cartItem.get(i).item_type);
                myRef2.child(item_id).child("Item Selected Quantity").setValue(cartItem.get(i).item_quantity);
                myRef2.child(item_id).child("Item Discount Percentage").setValue(cartItem.get(i).item_discount_percent);
                myRef2.child(item_id).child("Item Discounted Price").setValue(cartItem.get(i).item_price);
                myRef2.child(item_id).child("Item Total Price").setValue(cartItem.get(i).total_price);

                String link = "https://script.google.com/macros/s/AKfycbyFzn-Olq4WPFIUEro13jwOWGxtna546CWZlwPkE4_jYYjKSe9o/exec?func=addData&store=" + store_title_text
                        + "&item_name=" + cartItem.get(i).item_name
                        + "&type=" + cartItem.get(i).item_type
                        + "&brand=" + cartItem.get(i).item_brand
                        + "&quantity=" + cartItem.get(i).item_quantity
                        + "&discount_percent=" + cartItem.get(i).item_discount_percent
                        + "&discounted_price=" + cartItem.get(i).item_price
                        + "&total_price=" + cartItem.get(i).total_price;

                purchase_text += "Item Name: " + cartItem.get(i).item_name
                        + " | Item Type: " + cartItem.get(i).item_type
                        + " | Item Brand: " + cartItem.get(i).item_brand
                        + " | Item Quantity: " + cartItem.get(i).item_quantity
                        + " | Item Discount Percent: " + cartItem.get(i).item_discount_percent
                        + " | Item Discounted Price: Rs. " + cartItem.get(i).item_price
                        + " | Item Total Price: Rs. " + cartItem.get(i).total_price + "\n";

                // Instantiate the RequestQueue.
                RequestQueue queue = Volley.newRequestQueue(this);

                // Request a string response from the provided URL.
                StringRequest stringRequest = new StringRequest(Request.Method.GET, link,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });

                // Add the request to the RequestQueue.
                queue.add(stringRequest);
            }
            purchase_text += "\n\nGrand Total: Rs. " + grand_total;
        }
        catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error in saving Item Details", Toast.LENGTH_SHORT).show();
        }

        finally {
            Toast.makeText(getApplicationContext(),"Purchase Details have been saved", Toast.LENGTH_SHORT).show();

            //Getting content for email
            String email = customer_emailID;
            String subject = "Maverick Essentials Purchase";
            String message = "Greetings.\n\nYour purchase ID is : " + purchase_id + "\nPlease find attached your purchase bill and QR Code of purchase from Maverick Essentials\n\n" + purchase_text;
            message += "\n\nThe attached QR code is to be shown at the store for receiving your purchase.\n\nThank you for shopping with us.\n\nRegards,\nMaverick Essentials";

            //Creating SendMail object
            SendMail sm = new SendMail(this, email, subject, message);

            //Executing sendmail to send email
            sm.execute();
        }
    }

    public void uploadQR(String str){
        FirebaseStorage storage = FirebaseStorage.getInstance("gs://maverick-essentials.appspot.com");
        // Create a storage reference from our app
        StorageReference storageRef = storage.getReference("Item Purchase QR Codes");
        // Create a reference to "mountains.jpg"
        StorageReference childRef = storageRef.child(str + "_qr_code.jpg");

        Bitmap bitmap = makeBitmap(str);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        File filesDir = getApplicationContext().getFilesDir();
        qr_code_imageFile = new File(filesDir, str + "_qr_code.jpg");

        qr_code_filename = str + "_qr_code";

        OutputStream os;
        try {
            os = new FileOutputStream(qr_code_imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Error writing bitmap", e);
        }

        qr_code_bitmap_image = bitmap;

        byte[] data = baos.toByteArray();

        purchase_id = str;

        final UploadTask uploadTask = childRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // qr_download_link = taskSnapshot.getDownloadUrl();

                Task<Uri> result = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String qr_download_link = uri.toString();
                        Toast.makeText(getApplicationContext(), "QR Code of purchase has been uploaded successful", Toast.LENGTH_LONG).show();
                        upload_Item_Purchase_Details_to_Firebase(purchase_id, qr_download_link);
                    }
                });
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(getApplicationContext(),"Please wait for qr code to upload",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public Bitmap makeBitmap(String STR){
        Bitmap bitmap = null;
        //ImageView imageView = (ImageView) findViewById(R.id.qr_img);
        try {
            bitmap = encodeAsBitmap(STR);
            //  imageView.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    Bitmap encodeAsBitmap(String str) throws WriterException {
        BitMatrix result;
        int WIDTH = 200;
        try {
            result = new MultiFormatWriter().encode(str,
                    BarcodeFormat.QR_CODE, WIDTH, WIDTH, null);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }
        int width = 200;
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, w, h);
        return bitmap;
    }

    public class Config {
        public static final String EMAIL ="demoeric2099@gmail.com";
        public static final String PASSWORD ="DemoEric98#";
    }

    public class SendMail extends AsyncTask<Void,Void,Void> {

        //Declaring Variables
        private Context context;
        private Session session;

        //Information to send email
        private String email;
        private String subject;
        private String message;

        //Progressdialog to show while sending email
        private ProgressDialog progressDialog;

        //Class Constructor
        public SendMail(Context context, String email, String subject, String message){
            //Initializing variables
            this.context = context;
            this.email = email;
            this.subject = subject;
            this.message = message;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Showing progress dialog while sending email
            progressDialog = ProgressDialog.show(context,"Sending copy of purchase details to your registered Email ID","Please wait...",false,false);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //Dismissing the progress dialog
            progressDialog.dismiss();
            //Showing a success message
            Toast.makeText(context,"A copy of your purchase details with a QR code has been sent to your registered email ID",Toast.LENGTH_LONG).show();
            purchase_text = new String();
        }

        @Override
        protected Void doInBackground(Void... params) {
            //Creating properties
            Properties props = new Properties();

            //Configuring properties for gmail
            //If you are not using gmail you may need to change the values
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.socketFactory.port", "465");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.port", "465");

            //Creating a new session
            session = Session.getDefaultInstance(props,
                    new javax.mail.Authenticator() {
                        //Authenticating the password
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(Config.EMAIL, Config.PASSWORD);
                        }
                    });

            try {
                //Creating MimeMessage object
                MimeMessage mm = new MimeMessage(session);

                //Setting sender address
                mm.setFrom(new InternetAddress(Config.EMAIL));
                //Adding receiver
                mm.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
                //Adding subject
                mm.setSubject(subject);

                // Create the message part
                BodyPart messageBodyPart = new MimeBodyPart();

                // Now set the actual message
                messageBodyPart.setText(message);

                // Create a multipar message
                Multipart multipart = new MimeMultipart();

                // Set text message part
                multipart.addBodyPart(messageBodyPart);

                // Part two is attachment
                messageBodyPart = new MimeBodyPart();
                String filename = qr_code_imageFile.getPath();
                DataSource source = new FileDataSource(filename);
                messageBodyPart.setDataHandler(new DataHandler(source));
                messageBodyPart.setFileName(qr_code_filename);
                multipart.addBodyPart(messageBodyPart);

                // Send the complete message parts
                mm.setContent(multipart);

                //Sending email
                Transport.send(mm);

            } catch (MessagingException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}