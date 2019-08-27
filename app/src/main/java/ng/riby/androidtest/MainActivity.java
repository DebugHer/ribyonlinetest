package ng.riby.androidtest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.room.Room;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static final int PERMISSION_LOCATION_REQUEST_CODE = 1000;
    Button start, showDistanceBtn;
    TextView distanceTv;
    ArrayList<Double> locations;
    ArrayList<Double> tempLocations;
    TextView newLatTv;


    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;


    @Override
    protected void onStart(){
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume(){
        super.onResume();
        //Permission check for Android 6.0+
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if(mGoogleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startLocationUpdates();
        start = findViewById(R.id.startBtn);
        distanceTv = findViewById(R.id.distanceTravelled);
        locations = new ArrayList<>();
        showDistanceBtn = findViewById(R.id.showDistance);
        showDistanceBtn.setEnabled(false);
        newLatTv = findViewById(R.id.newLatLng);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        final LocationDao locationDao = AppDatabase.getAppDatabase(MainActivity.this).getLocationDAO();

        final LocationModel locationModel = new LocationModel();

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_LOCATION_REQUEST_CODE);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(start.getText().toString().equalsIgnoreCase("start")){
                    start.setText("Stop");
                    distanceTv.setText("...");

                    FusedLocationProviderClient locationClient = getFusedLocationProviderClient(MainActivity.this);
                    locationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            Double startLatitude  = location.getLatitude();
                            Double startLongitude = location.getLongitude();
                            locations.add(0,startLongitude);
                            locations.add(1,startLatitude);
                            Log.d("LocationLog","Successfully Inserted Start Values temporarily");
                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("MainActivity", "Error trying to get last GPS location");
                                    e.printStackTrace();
                                }
                            });

                }else{
                    start.setText("Start");
                    Toast.makeText(MainActivity.this, "Please wait while calculating distance", Toast.LENGTH_SHORT).show();

                    FusedLocationProviderClient locationClient = getFusedLocationProviderClient(MainActivity.this);

                    locationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            Double stopLatitude  = location.getLatitude();
                            Double stopLongitude = location.getLongitude();
                            locations.add(2,stopLongitude);
                            locations.add(3,stopLatitude);

                            locationModel.setStopLongitude(locations.get(2));
                            locationModel.setStopLatitude(locations.get(3));
                            locationModel.setStartLongitude(locations.get(0));
                            locationModel.setStartLatitude(locations.get(1));

                            locationDao.insert(locationModel);
                            Log.d("LocationLog","Successfully Inserted stop Values");
                            Log.d("LocationLog","Successfully got location"+ locationDao.getLocation().getStopLatitude());
                            showDistanceBtn.setEnabled(true);
                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("MainActivity", "Error trying to get last GPS location");
                                    e.printStackTrace();
                                }
                            });
                }
            }
        });

        showDistanceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDistanceBtn.setEnabled(false);
                //log coordinates
                Log.d("LocationLog LocationCoords",locationDao.getLocation().getStartLatitude()+"");
                Log.d("LocationLog LocationCoords",locationDao.getLocation().getStartLongitude()+"");
                Log.d("LocationLog LocationCoords" , locationDao.getLocation().getStopLatitude()+"");
                Log.d("LocationLog LocationCoords" , locationDao.getLocation().getStopLongitude()+"");

                //also a very good method for calculating distance between coordinates(.distanceBetween)
                //float[] results = new float[1];
//             Location.distanceBetween(
//                        locationDao.getLocation().getStartLatitude(),
//                        locationDao.getLocation().getStartLongitude(),
//                        locationDao.getLocation().getStopLatitude(),
//                        locationDao.getLocation().getStopLongitude(),
//                        results
//
//                );

                LatLng fromLatLong = new LatLng(locationDao.getLocation().getStartLatitude(),
                        locationDao.getLocation().getStartLongitude());
                LatLng toLatLong = new LatLng(locationDao.getLocation().getStopLatitude(),
                        locationDao.getLocation().getStopLongitude());
                Double dist = SphericalUtil.computeDistanceBetween(fromLatLong, toLatLong);

                //Note: GPS Results in a room provide innacurate results
                distanceTv.setText("Distance: "+dist+" meters");
                locations.clear();
            }
        });
    }

    private void startLocationUpdates() {

        mLocationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY) //GPS quality location points
                .setInterval(2000) //At least once every 2 seconds
                .setFastestInterval(1000); //At most once a second
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!!
                } else {
                    // permission denied!
                    Toast.makeText(MainActivity.this, "Permission denied to Access Location", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("Changing Location ",location.getLongitude()+"");
        Log.d("Changing Location ",location.getLatitude()+"");
        newLatTv.setText("New LatLng: "+location.getLatitude()+" , "+location.getLongitude());
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onPause(){
        super.onPause();
        //Permission check for Android 6.0+
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if(mGoogleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            }
        }
    }

    @Override
    protected void onStop(){
        super.onStop();
        mGoogleApiClient.disconnect();
    }
}
