package com.acculoc;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class AmbulanceHome extends AppCompatActivity implements com.google.android.gms.location.LocationListener,GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private AmbulanceSkeleton me;
    private DatabaseReference databaseReference;
    private TextView name,phone,numberplate,locationStatus;
    private Button logout;
    private GoogleApiClient googleApiClient;
    private LocationSettingsRequest locationSettingsRequest;
    private PendingResult<LocationSettingsResult> locationSettingsResultPendingResult;
    private Location myLocation;
    private GoogleMap googleMap;
    private LocationRequest locationRequest;
    LatLng myLatLng;
    private MarkerOptions myMarkerOptions;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ambulance_home);
        initViews();
        initData();



    }
    private void initData(){
        firebaseDatabase=Firebase.getInstance();
        firebaseAuth=FirebaseAuth.getInstance();
        databaseReference=firebaseDatabase.getReference("ambulance");
        databaseReference.child(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                me= dataSnapshot.getValue(AmbulanceSkeleton.class);
                name.setBackgroundColor(Color.TRANSPARENT);
                name.setText(me.getName());
                phone.setBackgroundColor(Color.TRANSPARENT);
                phone.setText(me.getPhone());
                numberplate.setBackgroundColor(Color.TRANSPARENT);
                numberplate.setText(me.getRegistrationNumber());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        googleApiClient= new GoogleApiClient.Builder(this).addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this).addApi(LocationServices.API).build();
        try{myLocation=LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            locationRequest = new LocationRequest();
            locationRequest.setInterval(5000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }catch(SecurityException e){e.printStackTrace();}
    }
    private void initViews(){
        name=findViewById(R.id.name);
        phone=findViewById(R.id.phone);
        numberplate =findViewById(R.id.numberplate);
        locationStatus=findViewById(R.id.locationStatus);
        logout=findViewById(R.id.logmeout);
logout.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        firebaseAuth.signOut();
        startActivity(new Intent(AmbulanceHome.this,SplashScreen.class));
        finish();
    }
});
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try{
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,locationRequest,this);}catch (SecurityException e){e.printStackTrace();}
        locationSettingsRequest = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest).build();
        locationSettingsResultPendingResult = LocationServices.SettingsApi.checkLocationSettings(googleApiClient,locationSettingsRequest);
        locationSettingsResultPendingResult.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                Status status= locationSettingsResult.getStatus();
                LocationSettingsStates locationSettingsStates= locationSettingsResult.getLocationSettingsStates();
                switch(status.getStatusCode()){
                    case LocationSettingsStatusCodes.SUCCESS:
                        Toast.makeText(AmbulanceHome.this, "All Settings OK . We are good to go.", Toast.LENGTH_SHORT).show();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        if(!locationSettingsStates.isGpsPresent()){
                            Toast.makeText(AmbulanceHome.this, "GPS is not Available.", Toast.LENGTH_SHORT).show();
                        }
                        if(locationSettingsStates.isGpsPresent() && !locationSettingsStates.isGpsUsable())
                        {
                            Toast.makeText(AmbulanceHome.this, "GPS not turned on . \n Performance will degrade.", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        myLocation=location;
        myLatLng = new LatLng(myLocation.getLatitude(),myLocation.getLongitude());
        HashMap<String,Object> update= new HashMap<>();
        update.put("lat",myLatLng.latitude);
        update.put("lon",myLatLng.longitude);
        locationStatus.setText(myLatLng.toString());
        databaseReference.child(firebaseAuth.getUid()).updateChildren(update);
    }
}
