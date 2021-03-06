package com.jesus.myapplication.servicios;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.jesus.myapplication.MainActivity;

public class MiServicio extends Service implements LocationListener {

    private final Context ctx;

    double latitud, longitud;
    Location location;
    boolean gpsActivo;
    TextView texto;
    LocationManager locationManager;
    String provider = "";

    public MiServicio(){
        super();
        this.ctx = this.getApplicationContext();
    }

    public MiServicio(Context c){
        super();
        this.ctx = c;
        getLocation();
    }

    /*public void setView(View v){
        texto = (TextView)v;
        texto.setText("Coordenadas: "+latitud+","+longitud);
    }*/

    public void getLocation(){
        try {
            locationManager = (LocationManager)this.ctx.getSystemService(LOCATION_SERVICE);
            gpsActivo = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if(!gpsActivo) {
                gpsActivo = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                provider="network";
            }
        }catch (Exception e){}

        if (gpsActivo) {
            if (provider == "network"){
                locationManager.requestLocationUpdates(locationManager.NETWORK_PROVIDER
                        , 1000 * 6
                        , 10
                        , this);
                location = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);
            }else{
                locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER
                        ,1000*6
                        ,10
                        ,this);
                location = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
            }
            latitud = location.getLatitude();
            longitud = location.getLongitude();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (gpsActivo){
            if (provider == "network"){
                locationManager.requestLocationUpdates(locationManager.NETWORK_PROVIDER
                        , 1000 * 6
                        , 10
                        , this);
                location = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);
            }else{
                locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER
                        ,1000*6
                        ,10
                        ,this);
                location = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
            }
            latitud = location.getLatitude();
            longitud = location.getLongitude();
        }
        if((double)((int)(latitud*10000)/10000.0)==28.5967 && (double)((int)(longitud*10000)/10000.0) ==-106.1169)
            Toast.makeText(ctx, "Se ha encontrado ubicacion conocida", Toast.LENGTH_SHORT).show();
        //llamar web service para conmparar ubicaciones conocidas en base de datos
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}