package showlocation.eurecom.fr.showlocation;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements LocationListener,OnMapReadyCallback {
    protected LocationManager locationmanager = null ;
    private String provider;
    Location location;
    TextView latitudeField;
    TextView longitudeField;
    public  static  final int MY_PERMISSIONS_LOCATION=0 ;
    private GoogleMap googleMap;
    static final LatLng NICE= new LatLng(43.7031,7.02661);
    static final LatLng EURECOM = new LatLng(43.614376,7.070450);
    PendingIntent pendingIntent;
    public SharedPreferences sharedPreferences;
    private static final String PROX_ALERT_INTENT = "fr.eurecom.locationservices.android.lbs.ProximityAlert";
    private static final String POINT_LATITUDE_KEY = "POINT_LATITUDE_KEY";
    private static final String POINT_LONGITUDE_KEY = "POINT_LONGITUDE_KEY";
    private int count = 0;
    Marker m;

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        updateLocationView();
        this.googleMap = googleMap ;
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(EURECOM)
                .zoom(17)
                .bearing(90)
                .tilt(0)
                .build();

        this.googleMap.addMarker(new MarkerOptions().position(NICE).title("Nice").snippet("Enjoy French Riviera"));

        this.googleMap.addMarker(new MarkerOptions().position(EURECOM).title("EURECOM").snippet("Enjoy MobServ")) ;


        MarkerOptions a = new MarkerOptions()
                .position(new LatLng(50,6))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        m = googleMap.addMarker(a);



        this.googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        this.googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {

                Intent intent = new Intent(PROX_ALERT_INTENT);
                PendingIntent proximityIntent =
                        PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
                if (ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(getApplicationContext(),
                                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Log.i("Permission: ", "To be checked");
                    return;
                } else {
                    Log.i("Permission: ", "GRANTED");
                }

                saveCoordinatesInPreferences((float) point.latitude,
                        (float) point.longitude);
                MarkerOptions a = new MarkerOptions()
                        .position(point)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                googleMap.addMarker(a);

                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                locationManager.addProximityAlert(point.latitude, point.longitude, 5, -1,
                        proximityIntent);
                IntentFilter filter = new IntentFilter(PROX_ALERT_INTENT);
                registerReceiver(new ProximityIntentReceiver(), filter);
                Log.i("Registred", "proximity");
                Toast.makeText(getBaseContext(), "Added a proximity Alert at :\nLong = "+point.longitude+"\nLat = "+point.latitude,
                        Toast.LENGTH_LONG).show();
                ++count;
            }
    });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Criteria criteria = new Criteria();
        locationmanager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationmanager.getBestProvider(criteria,false);

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            Log.i("Permisison","To be Checked");
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},MY_PERMISSIONS_LOCATION);
            return;
        } else {
            Log.i("Permission","Granted");
        }

        longitudeField = (TextView) findViewById(R.id.textView02);
        latitudeField = (TextView) findViewById(R.id.textView04);

        location = locationmanager.getLastKnownLocation(provider);
        if (locationmanager==null){
            locationmanager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }

        provider = locationmanager.getBestProvider(criteria,false );
        location = locationmanager.getLastKnownLocation(provider);

        locationmanager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) this);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        sharedPreferences = getSharedPreferences("location",0);



    }

    @Override
    public void onLocationChanged(Location location) {
        updateLocationView();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {
        Toast.makeText(this,"Enabled new provider",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onProviderDisabled(String s) {
        Toast.makeText(this,"Disabled provider",Toast.LENGTH_LONG).show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case MY_PERMISSIONS_LOCATION: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.i("Access:", "Now Permissions are granted");
                } else {
                    Log.i("Access: ","Permissions are denied");
                }
                break;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        locationmanager= (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = locationmanager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(!gpsEnabled){
            enableLocationSettings();
        } else {

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationmanager.removeUpdates(this);
    }

    public void showLocation(View view){
       switch (view.getId()){
           case R.id.button01: {
               if (this.googleMap.getMapType() == GoogleMap.MAP_TYPE_HYBRID) {
                   this.googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
               } else {
                   this.googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
               }
               break ;
           }
       }
    }

    public void updateLocationView() {
        Criteria criteria = new Criteria() ;
        locationmanager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationmanager.getBestProvider(criteria, false );

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            Log.i("Permisison","To be Checked");
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},MY_PERMISSIONS_LOCATION);
            return;
        } else {
            Log.i("Permission","Granted");
        }

        location = locationmanager.getLastKnownLocation(provider);
         if(location != null){
                 double lat = location.getLatitude();
                 double lng = location.getLongitude();
                 latitudeField.setText(String.valueOf(lat));
                 longitudeField.setText(String.valueOf(lng));
                    if (m != null) {
                        m.setPosition(new LatLng(lat, lng));
                    }

         } else{
             Log.i("showLocation" ,"NULL") ;

         }
    }
    private void enableLocationSettings() {
        Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(settingsIntent);
    }

    private void saveCoordinatesInPreferences(float latitude, float longitude) {
        SharedPreferences prefs =
                this.getSharedPreferences(getClass().getSimpleName(),
                        Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putFloat(POINT_LATITUDE_KEY, latitude);
        prefsEditor.putFloat(POINT_LONGITUDE_KEY, longitude);
        prefsEditor.commit();
    }
}
