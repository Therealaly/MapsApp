package com.example.mapsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.mapsapp.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.PointOfInterest;

import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, SensorEventListener {

    private Sensor mSensorLight;
    private SensorManager mSensorManager;
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    String Root_Frag = "root_fagment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        // default name fragment
        loadFrag(new NormalFragment(), 0);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Yogyakarta and move the camera
        LatLng Yogyakarta = new LatLng(-7.774982843746279, 110.37472737666943);
        mMap.addMarker(new MarkerOptions().position(Yogyakarta).title("Marker in Yogyakarta"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(Yogyakarta));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Yogyakarta,10));
        setMapLongclick(mMap);

        setPoiClick(mMap);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.aubergine_map:
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json));
                loadFrag(new AubergineFragment(), 1);
                return true;
            case R.id.normal_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                loadFrag(new NormalFragment(), 0);
                return true;
            case R.id.sattelite_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                loadFrag(new SatelliteFragment(), 1);
                return true;
            case R.id.terrain_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                loadFrag(new TerrainFragment(), 1);
                return true;
            case R.id.hybrid_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                loadFrag(new HybridFragment(), 1);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
    private void setMapLongclick(final GoogleMap map){
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(@NonNull LatLng latLng) {
                String text = String.format(Locale.getDefault(),
                        "lat: %2$.5f,long: %2$.5f",
                        latLng.latitude,
                        latLng.longitude);
                map.addMarker(new MarkerOptions().position(latLng).title("Dropped pin").snippet(text));
            }
        });
    }
    private void setPoiClick(final GoogleMap map){
        map.setOnPoiClickListener(new GoogleMap.OnPoiClickListener() {
            @Override
            public void onPoiClick(@NonNull PointOfInterest pointOfInterest) {
                Marker poiMarker = mMap.addMarker(new MarkerOptions().position(pointOfInterest.latLng).title(pointOfInterest.name));
                poiMarker.showInfoWindow();
            }
        });
    }
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
        else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    enableMyLocation();
                    break;
                };
        }
    }

    // SENSOR LIGHT

    @Override
    protected void onStart() {
        super.onStart();
        if (mSensorLight == null) return;
        mSensorManager.registerListener(this, mSensorLight, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float currentValue = event.values[0];
        int sensorType = event.sensor.getType();
        switch (sensorType) {
            case Sensor.TYPE_LIGHT:
                if (currentValue < 1500) {
                    mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json));
                }
                if (currentValue >= 1500) {
                    mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.normal_style));
                }
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    // FRAGMENT

    public void loadFrag(Fragment fragment_name, int flag)
    {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        if (flag == 0) {
            ft.add(R.id.FL, fragment_name);

            fm.popBackStack(Root_Frag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            ft.addToBackStack(Root_Frag);
        }
        else {
            ft.replace(R.id.FL, fragment_name);
            ft.addToBackStack(null);
        }

        ft.commit();
    }


}