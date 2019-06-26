package com.rot.rot;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;

/**
 * Este servicio realiza la función de detectar de forma periodica las coordenadas GPS que recibe el dispositivo movil
 * Y pasarlas a la activity GestionDatos que es la que crea el servicio.
 * @author Victor Dorado Fernández
 */
public class GPS_Service extends Service {


    private LocationListener listener;  //Listener que detectara la actualización de coordenadas
    private LocationManager locationManager;    //Variable que gestiona la libreria GPS

    @Nullable
    @Override

    public IBinder onBind(Intent intent) {
        /*el sistema llama al método onBind () de su servicio para recuperar el IBinder solo cuando el primer cliente se enlaza.
     El sistema luego entrega el mismo IBinder a cualquier cliente adicional que se enlace, sin volver a llamar a onBind ().
    Puede conectar varios clientes a un servicio simultáneamente.*/
        return null;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        //Creamos el listener de Localizacion
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {//Funcion en la que entra el listener cuando detecta un cambio
                //Se crea el intent, se formatea la informacion necesaria y se envia en forma de broadcast a la activity que ha solicitado el servicio
                Intent i = new Intent("location_update");
                i.putExtra("coordinates",location.getLongitude()+" "+location.getLatitude());//Cojemos la informacion de longitud y latitud
                sendBroadcast(i);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {//En caso de que el provedor de GPS este desabilitado, se solicita que lo active mediante un intent
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        };
        //Se define el LocationManager con el contexto de servicio de localizacion
        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        //noinspection MissingPermission
        //Se realizara una actualización cada vez que haya un cambio de coordenas o cada 3s
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,3000,0,listener);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(locationManager != null){
            //noinspection MissingPermission
            locationManager.removeUpdates(listener);//Una vez se quiere destruir el servicio, se elimina tambien el listener
        }
    }
}
