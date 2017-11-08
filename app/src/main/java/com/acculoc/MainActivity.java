package com.acculoc;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.HashSet;


public class MainActivity extends AppCompatActivity implements ChildEventListener,OnMapReadyCallback,LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{
    SupportMapFragment googleMapFragment;
    GoogleApiClient googleApiClient;
    LocationSettingsRequest locationSettingsRequest;
    PendingResult<LocationSettingsResult> locationSettingsResultPendingResult;
    Location myLocation;
    GoogleMap googleMap;
    LocationRequest locationRequest;
    Marker myMarker;
    LatLng myLatLng;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    HashMap<String,AmbulanceSkeleton> ambulanceMap;
    HashMap<String,Marker> markerMap;
    boolean flag=false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        googleMapFragment=(SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.mapView);
        googleMapFragment.getMapAsync(this);
        initData();




    }
    private void initData(){
        ambulanceMap= new HashMap<>();
        firebaseDatabase=Firebase.getInstance();
     markerMap= new HashMap<>();
     databaseReference=firebaseDatabase.getReference();
     googleApiClient= new GoogleApiClient.Builder(this)
             .addApi(LocationServices.API)
             .addOnConnectionFailedListener(this)
             .addConnectionCallbacks(this).build();
     try{myLocation=LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
         locationRequest = new LocationRequest();
         locationRequest.setInterval(5000);
         locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
     }catch(SecurityException e){e.printStackTrace();}

 }
    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap=googleMap;
       try{ googleMap.setMyLocationEnabled(true);}catch (SecurityException e){e.printStackTrace();}
        if(myLocation!=null){
            myLatLng = new LatLng(myLocation.getLatitude(),myLocation.getLongitude());
            myMarker=googleMap.addMarker(new MarkerOptions().position(myLatLng));
            myMarker.setTitle("Its me");
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng,15));
            getAmbulanceMarked();
        }
    }

    private void getAmbulanceMarked(){
        if(!flag){
            databaseReference.child("ambulance").orderByChild("lat").startAt(myLatLng.latitude-0.01).endAt(myLatLng.latitude +0.01).addChildEventListener(this);
        Toast.makeText(this, myLatLng.toString(), Toast.LENGTH_SHORT).show();
        flag=true;
    }}
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.v("Google API","connected");
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
                        Toast.makeText(MainActivity.this, "All Settings OK . We are good to go.", Toast.LENGTH_SHORT).show();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        if(!locationSettingsStates.isGpsPresent()){
                            Toast.makeText(MainActivity.this, "GPS is not Available.", Toast.LENGTH_SHORT).show();
                        }
                        if(locationSettingsStates.isGpsPresent() && !locationSettingsStates.isGpsUsable())
                        {
                            Toast.makeText(MainActivity.this, "GPS not turned on . \n Performance will degrade.", Toast.LENGTH_SHORT).show();
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
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.v("Google API Client","Connection Failed");
    }

    @Override
    public void onLocationChanged(Location location) {
        myLocation=location;
    updateMyMarker();
        getAmbulanceMarked();
    }
    private void updateMyMarker(){
            myLatLng= new LatLng(myLocation.getLatitude(),myLocation.getLongitude());
        if(myMarker!=null){
            myMarker.setPosition(myLatLng);
        }else{
            myMarker=googleMap.addMarker(new MarkerOptions().position(myLatLng).title("Its me"));
        }

    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
       AmbulanceSkeleton ambu=dataSnapshot.getValue(AmbulanceSkeleton.class);
       Log.v("Ambulance",dataSnapshot.toString());
        ambulanceMap.put(dataSnapshot.getKey(),ambu);
        updateMap(1,ambu);
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        AmbulanceSkeleton ambu=dataSnapshot.getValue(AmbulanceSkeleton.class);
        Log.v("Amulance",dataSnapshot.toString());
        ambulanceMap.put(dataSnapshot.getKey(),ambu);
        updateMap(2,ambu);

    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        AmbulanceSkeleton ambu=dataSnapshot.getValue(AmbulanceSkeleton.class);
        ambulanceMap.remove(dataSnapshot.getKey());
        updateMap(3,ambu);
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
    private void updateMap(int x,AmbulanceSkeleton ambu){
        switch (x){
            case 1:
              markerMap.put(ambu.getUid(),googleMap.addMarker(new MarkerOptions().position(new LatLng(ambu.lat,ambu.lon))));
              markerMap.get(ambu.getUid()).setTitle(
                      "Phone Number:"+ambu.getPhone()
                      );
              markerMap.get(ambu.getUid()).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ambulanceicon));
                break;
            case 2:
                markerMap.get(ambu.getUid()).setPosition(new LatLng(ambu.lat,ambu.lon));
                break;
            case 3:
                markerMap.get(ambu.getUid()).remove();
                markerMap.remove(ambu.getUid());
                break;
        }

    }

}
