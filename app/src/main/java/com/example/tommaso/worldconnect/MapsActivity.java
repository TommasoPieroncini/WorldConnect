package com.example.tommaso.worldconnect;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MapsActivity extends FragmentActivity {

    final Context context = this;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Button button1;
    private ImageButton button2;
    private EditText edit;
    private Button button3;
    private String provider;
    private static final int TAKE_PICTURE = 1;
    private Uri imageUri;
    private Button photoButton;
    private ImageView infoWindowImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        StrictMode.enableDefaults();
        setUpMapIfNeeded();
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                View contents = getLayoutInflater().inflate(R.layout.infowindow_contents_layout, null);

                return contents;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        provider = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        CheckEnableGPS(provider);
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationChangeListener(myLocationChangeListener);
        edit = (EditText) findViewById(R.id.editText);
        edit.setHint("Customize your snippet!");
    }

    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(final Location location) {
            mMap.setMyLocationEnabled(true);
            ArrayList[] coord = getData();
            ArrayList<Double> xs = coord[0];
            ArrayList<Double> ys = coord[1];
            ArrayList<String> snips = coord[2];
            ArrayList<String> titles = coord[3];
            if (xs.size() > 0) {
                for (int i = 0; i < xs.size(); i++) {
                    Double x = xs.get(i);
                    Double y = ys.get(i);
                    String snip = snips.get(i);
                    String title = titles.get(i);
                    mMap.addMarker(new MarkerOptions().position(new LatLng(x, y)).title(title).snippet(snip));
                }
            }
            button1 = (Button) findViewById(R.id.button1);
            button2 = (ImageButton) findViewById(R.id.button2);
            button3 = (Button) findViewById(R.id.button3);
            photoButton = (Button) findViewById(R.id.button);
            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                LatLng pos;

                @Override
                public boolean onMarkerClick(Marker marker) {
                    marker.showInfoWindow();
                    pos = marker.getPosition();
                    button3.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            float[] results = new float[1];
                            Double x = pos.latitude;
                            Double y = pos.longitude;
                            Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                                    x, y,
                                    results);
                            Integer result = new Integer((int) results[0]);
                            showPopUp(result);
                        }
                    });
                    photoButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            takePhoto();
                        }
                    });
                    return true;
                }
            });

            button1.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v) {
                    Double x = location.getLatitude();
                    Double y = location.getLongitude();
                    Integer id = 1;
                    String title = "Tommy";
                    String locx = Double.toString(x);
                    String locy = Double.toString(y);
                    String ID = Integer.toString(id);
                    Editable text = edit.getText();
                    String snippet = text.toString();
                    edit.setText("");
                    List<NameValuePair> coordLoc = new ArrayList<NameValuePair>();
                    coordLoc.add(new BasicNameValuePair("id",ID));
                    coordLoc.add(new BasicNameValuePair("x",locx));
                    coordLoc.add(new BasicNameValuePair("y", locy));
                    coordLoc.add(new BasicNameValuePair("snippet", snippet));
                    coordLoc.add(new BasicNameValuePair("title",title));
                    sendData(coordLoc);
                    Toast.makeText(getApplicationContext(), "Your location has been adjourned",
                            Toast.LENGTH_LONG).show();
                }
            });
            button2.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v) {
                    mMap.clear();
                }
            });
        }
    };

    public ArrayList[] getData(){
        ArrayList<Double> xs = new ArrayList<Double>();
        ArrayList<Double> ys = new ArrayList<Double>();
        ArrayList<String> snips = new ArrayList<String>();
        ArrayList<String> titles = new ArrayList<>();
        ArrayList[] coord = new ArrayList[]{xs,ys,snips,titles};
        String result = "";
        InputStream input = null;
        try{
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://128.61.104.207:8165/connect.php");
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            input = entity.getContent();
        }
        catch(Exception e){
            Log.e("log_tag1", "Error in http connection " + e.toString());
            Toast.makeText(getApplicationContext(), "Couldn't connect to database",
                    Toast.LENGTH_LONG).show();
        }
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(input,"iso-8859-1"),8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null){
                sb.append(line + "/n");
            }
            input.close();

            result = sb.toString();
        }
        catch(Exception e){
            Log.e("log_tag2", "Error converting result".toString());
        }

        try{
            JSONArray jArray = new JSONArray(result);

            for(int i = 0; i < jArray.length(); i++){
                JSONObject json = jArray.getJSONObject(i);
                xs.add(new Double(json.getString("x")));
                ys.add(new Double(json.getString("y")));
                snips.add(json.getString("snippet"));
                titles.add(json.getString("title"));
            }


        }
        catch(Exception e){
            Log.e("log_tag3", "Error Parsing Data " + e.toString());
        }
        return coord;
    }
    public void sendData(List<NameValuePair> coords){
        mMap.clear();
        String json = "";
        InputStream is = null;
        try{
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://128.61.104.207:8165/update.php");
            httppost.setEntity(new UrlEncodedFormEntity(coords));
            HttpResponse httpResponse = httpclient.execute(httppost);
            HttpEntity entity = httpResponse.getEntity();
            is = entity.getContent();
        }
        catch(Exception e){
            Log.e("log_tag4","Error in output");
        }
    }
    private void showPopUp(Integer result) {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.custom_dialog);
        dialog.setTitle("Distance");
        String Kms = Integer.toString(result / 1000);
        String Mts = Integer.toString(result % 1000);
        TextView info = (TextView) dialog.findViewById(R.id.textView);
        info.setText("You are " + Kms + " kilometers and " + Mts + " meters away from this location.");
        Button button4 = (Button) dialog.findViewById(R.id.button4);
        button4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void CheckEnableGPS(String provider){
        if(provider.contains("gps")){
            //GPS Already Enabled
            Log.e("log_tag12", "GPS Already Enabled");
        }else{
            //GPS not enabled, prompt user
            final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            final AlertDialog.Builder builder =  new AlertDialog.Builder(this);
            final String message = "WorldConnect needs GPS activated to function properly. Do you want to open GPS setting?";
            builder.setMessage(message)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int id) {
                                    startActivity(intent);
                                    d.dismiss();
                                }
                            })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int id) {
                                    d.cancel();
                                }
                            });
            builder.create().show();
        }

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PICTURE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri selectedImage = imageUri;
                    Log.e("URI",selectedImage.toString());
                    getContentResolver().notifyChange(selectedImage, null);
                    ContentResolver cr = getContentResolver();
                    Log.e("contentresolver",cr.toString());
                    Bitmap bitmap;
                    try {
                        bitmap = android.provider.MediaStore.Images.Media
                                .getBitmap(cr, selectedImage);
                        Log.e("bitmap",bitmap.toString());
                        infoWindowImage = (ImageView) findViewById(R.id.imageView2);
                        infoWindowImage.setImageBitmap(bitmap);
                    } catch (Exception e) {
                        Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT)
                                .show();
                        Log.e("Camera", e.toString());
                    }
                }
        }
    }
    public void takePhoto() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        File photo = new File(Environment.getExternalStorageDirectory(),  "Pic.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                Uri.fromFile(photo));
        imageUri = Uri.fromFile(photo);
        startActivityForResult(intent, TAKE_PICTURE);
    }
}
