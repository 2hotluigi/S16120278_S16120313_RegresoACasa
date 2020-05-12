package com.example.regresoacasa;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private GeoApiContext geoApiContext = null;
    public LatLng miUbicacion = null;
    public LatLng miDestinoo = null;
    TextView tvmi_Ubi=null;
    TextView tvmi_Dest=null;
    SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        tvmi_Ubi = findViewById(R.id.etbUbiActual);
        tvmi_Dest = findViewById(R.id.etbUbiDestino);
        Button btnAct = findViewById(R.id.btnIr);
        miDestinoo = new LatLng(20.130256, -101.190733);
        tvmi_Dest.setText("20.130256, -101.190733");
        btnAct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dest =tvmi_Dest.getText()+"";
                dest = dest.trim();
                String []lat_long =dest.split(",");
                miDestinoo = new LatLng(Double.parseDouble(lat_long[0]),Double.parseDouble(lat_long[1]));
                init();
            }
        });
        init();
    }
    public void init(){
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.clear();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null){
                    miUbicacion = new LatLng(location.getLatitude(), location.getLongitude());
                    tvmi_Ubi.setText(location.getLatitude()+", "+ location.getLongitude());
                    tvmi_Ubi.setEnabled(false);
                    final Marker miDestino = mMap.addMarker(new MarkerOptions().position(miDestinoo).title("Mi Destino"));
                    final Marker miOrigen = mMap.addMarker(new MarkerOptions().position(miUbicacion).title("Mi Ubicacion"));
                    calculateDirections(miDestino, miOrigen);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(miUbicacion,15));
                }else {
                    Toast.makeText(null,"No se encontro una ubicacion" ,Toast.LENGTH_LONG).show();
                    Log.e("MAP","No hay map");
                }
            }
        });
        if(geoApiContext == null){
            geoApiContext = new GeoApiContext.Builder()
                    .apiKey("AIzaSyCzsLS8zFEb5D0vpDtdJ_Gk17DooR7Mnzo")
                    .build();
        }
    }

    private void calculateDirections(Marker destino, Marker origen){
        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                destino.getPosition().latitude,
                destino.getPosition().longitude
        );
        DirectionsApiRequest directions = new DirectionsApiRequest(geoApiContext);
        directions.alternatives(false);
        directions.origin(
                new com.google.maps.model.LatLng(
                        origen.getPosition().latitude,
                        origen.getPosition().longitude
                )
        );
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                addPolylinesToMap(result);
            }
            @Override
            public void onFailure(Throwable e) {
                Log.e("TAG", "calculateDirections: Failed to get directions: " + e.getMessage() );
            }
        });
    }

    private void addPolylinesToMap(final DirectionsResult result){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                for(DirectionsRoute route: result.routes){
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());
                    List<LatLng> newDecodedPath = new ArrayList<>();
                    for(com.google.maps.model.LatLng latLng: decodedPath){
                        newDecodedPath.add(new LatLng(
                                latLng.lat,
                                latLng.lng
                        ));
                    }
                    Polyline polyline = mMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setColor(ContextCompat.getColor(getApplicationContext(), R.color.colormapa));
                    polyline.setClickable(true);
                }
            }
        });
    }
}