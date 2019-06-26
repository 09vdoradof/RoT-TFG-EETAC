package com.rot.rot;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * App main activity, gestiona la detección de beacons, mostrando un mensaje de los beacons
 * detectados
 *
 * @author Victor Dorado Fernández
 */
public class Gestiondatos extends AppCompatActivity implements View.OnClickListener{

    protected final String TAG = Gestiondatos.this.getClass().getSimpleName();;

    //Creamos las variables necesarias para los permisos de utilizacion del Bluetooth
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final int REQUEST_ENABLE_BLUETOOTH = 1;

    //Declaramos los elementos graficos del layout
    private Button btn_start, btn_stop;
    private TextView textView;
    //Declaramos la variable que recojera los datos del servicio GPS
    private BroadcastReceiver broadcastReceiver;
    int contadordedatosenviados=0;//Variable que almazena el numero de datos enviados

    //Variables necesarias para los metodos de envio "Volley"
    RequestQueue queue;
    JsonObjectRequest request;
    Map<String, String> map = new HashMap<String, String>();
    String Usuario; // Variable usuario para utilizar durante el fichero
    String Nom_Carrera;//Variable que guarda el nombre de la carrera
    String latitud;//Variable que guarda la latitud y que va siendo actualizada
    String longitud;//Variable que guarda la longitud y que va siendo actualizada

    //Variables correspondientes a Blouetooth Manager
    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    BluetoothLeScanner btScanner;
    Button startScanningButton;
    Button stopScanningButton;
    TextView peripheralTextView;
    private final static int REQUEST_ENABLE_BT = 1;
    //private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    TextView Numero;
    //Preuba para el textbox
    //Runnable updater;
    //int pruebatemp;

    ArrayList<String> memoriacorredores = new ArrayList<String>();//Array que guarda los corredores que han envido un dato de manera que un solo voluntario solo pueda enviar un dato de un corredor aunque le lleguen más.
    @Override
    protected void onResume() {
        super.onResume();
        if(broadcastReceiver == null){
            broadcastReceiver = new BroadcastReceiver() {//Recibimos actualización de coordenadas por parte del servicio GPS
                @Override
                public void onReceive(Context context, Intent intent) {

                    //Gestionamos la información recibida por el intent
                    String sIntent = ""+intent.getExtras().get("coordinates");
                    String lat=sIntent.split(" ")[1];
                    String lon=sIntent.split(" ")[0];
                    //Actualizmos el contenido de las textbox con las nuevas coordenadas
                    TextView latit = (TextView)findViewById(R.id.Latitud);
                    latit.setText("Latitud: "+lat);
                    TextView longt = (TextView)findViewById(R.id.Longitud);
                    longt.setText("Longitud: "+lon);
                    //Guardamos las nuevas coordenadas en las variables globales de latitud y longitud
                    latitud=lat;
                    longitud=lon;

                }
            };
        }

        registerReceiver(broadcastReceiver,new IntentFilter("location_update"));
    }
    //No tocar el error del oncreateeeee -----------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestiondatos);

        ArrayList<Identifier> identifiers = new ArrayList<>();


        getSupportActionBar().hide();//<!-- Para que no salga la barra superior en todas las activitis-->

        //Recuperamos la variable usuario que pasamos desde login
        Bundle p = getIntent().getExtras();
        String usu = p.getString("EXTRA_SESSION_USER");
        String car = p.getString("EXTRA_SESSION_USER2");
        Usuario = usu;
        Nom_Carrera = car;
        memoriacorredores.add("inicializacion");//Inicilaizamos con un valor random el vector de memoria corredores
        btn_start = (Button) findViewById(R.id.button);
        btn_stop = (Button) findViewById(R.id.button2);
        Numero = (TextView)findViewById(R.id.numerodeenvios);



        if (!runtime_permissions()){//Llamamos a la función de pedir permisos
            enable_buttons();}//Se llama a la función de activar botones que es la que tambien activa el servicioGPS
        //Inicializamos Textview donde se veran los dspositivs escaneados
        peripheralTextView = (TextView) findViewById(R.id.PeripheralTextView);
        peripheralTextView.setMovementMethod(new ScrollingMovementMethod());
        //Inicializamos los botons que dan acceso al inicio del scanner
        startScanningButton = (Button) findViewById(R.id.StartScanButton);
        startScanningButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startScanning();
            }
        });//Creamos el lister para este boton y llamamos a la funcion StartScnning en el caso que spulse el boton.

        //Inicializamos los botons que dan acceso al fin del scanner
        stopScanningButton = (Button) findViewById(R.id.StopScanButton);
        stopScanningButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopScanning();//Creamos el lister para este boton y llamamos a la funcion StopScnning en el caso que spulse el boton.
            }
        });
        stopScanningButton.setVisibility(View.INVISIBLE);//De primeras el boton stop esta invisble ya que no puede parar algo que no ha inciado
        //inicilaizamos las variables necesarias para utiizar y gestionar el bluetooth
        btManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        btScanner = btAdapter.getBluetoothLeScanner();


        if (btAdapter != null && !btAdapter.isEnabled()) {//Pedimos permisos para acceder al bluetooth
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent,REQUEST_ENABLE_BT);
        }
//------ DEJAR ESTE FALLOOOOO no afectaaaaaaa//
        // Make sure we have access coarse location enabled, if not, prompt the user to enable it
        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle("This app needs location access");
            builder.setMessage("Please grant location access so this app can detect peripherals.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                }
            });
            builder.show();
        }
        //updateTime(); Era una prueba para el timer
    }

    @Override
    public void onClick(View view) {



            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                // Si los permisos de localización todavía no se han concedido, solicitarlos
                if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {

                    askForLocationPermissions();

                } else { // Permisos de localización concedidos
                }
            } else { // Versiones de Android < 6
            }


    }

   /*
   Esta funcion al final no hace falta porque ya se ha solucionado lo del textview
   public void updateTime() {

        final Handler timerHandler = new Handler();

        updater = new Runnable() {
            @Override
            public void run() {
                Numero.setText(String.valueOf(pruebatemp));
                timerHandler.postDelayed(updater,10);
                pruebatemp++;
            }
        };
        timerHandler.post(updater);
    }*/
    private ScanCallback leScanCallback = new ScanCallback() {// Dispositivo scaneado callback
        @Override
        public void onScanResult(int callbackType, ScanResult result) {//El objeto result contiene toda la info del disppsotivo escaneado
            peripheralTextView.append("Device Name: " + result.getDevice().getName() + " rssi: " + result.getRssi() + "\n");//Obtenemos el nmbr ey la potencia del dspositvo escaneado
            try {
                //Los beacons de las carrera tienen este format: CorredorID-000001
                //Comprovamos que la primera parte de la string se corresponde para en ese cso enviar el dato
                String idcor=String.valueOf(result.getDevice().getName());
                String[] idcorseparated = idcor.split("-");


                if (idcorseparated[0].equals("CorredorID")){
                    enviardato(String.valueOf(result.getDevice().getName()));//llamamos a la función para enviar datos al server dando como parametro el nombre dle dispositivo
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            //showToastMessage("Device Name: " + result.getDevice().getName());

            // auto scroll for text view
            final int scrollAmount = peripheralTextView.getLayout().getLineTop(peripheralTextView.getLineCount()) - peripheralTextView.getHeight();
            // if there is no need to scroll, scrollAmount will be <=0
            if (scrollAmount > 0)
                peripheralTextView.scrollTo(0, scrollAmount);
        }
    };

    public void startScanning() {//Funcion donde se incia scaner Bluetooth
        System.out.println("start scanning");
        peripheralTextView.setText("");
        startScanningButton.setVisibility(View.INVISIBLE);//Ponemos el boton inicar scaner en invisible
        stopScanningButton.setVisibility(View.VISIBLE);//Ponemos el boton stop scaner en visible
        AsyncTask.execute(new Runnable() {//Ejecutmos el scan en una asyntask para que coora en otro thread
            @Override
            public void run() {
                btScanner.startScan(leScanCallback);
            }
        });
    }

    public void stopScanning() {//Funcion que realiza el stop del scanner de dispositvos BLE
        actualizartextview();
        System.out.println("stopping scanning");
        peripheralTextView.append("Stopped Scanning");
        startScanningButton.setVisibility(View.VISIBLE);//Ponemos el boton iniciar scaner en visible
        stopScanningButton.setVisibility(View.INVISIBLE);//Ponemos el boton stop scaner en invisible
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.stopScan(leScanCallback);//Detenemos el scan
            }
        });
    }


    private void askForLocationPermissions() {//Pedimos los permisos para tilizar BL

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.location_access_needed);
        builder.setMessage(R.string.grant_location_access);
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            public void onDismiss(DialogInterface dialog) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSION_REQUEST_COARSE_LOCATION);
            }
        });
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {//Pedimos permisos para utilizar GPS
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.funcionality_limited);
                    builder.setMessage(getString(R.string.location_not_granted) +
                            getString(R.string.cannot_discover_beacons));
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {

                        }
                    });
                    builder.show();
                }
                return;
            }
        }
    }

    private boolean isLocationEnabled() {

        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        boolean networkLocationEnabled = false;

        boolean gpsLocationEnabled = false;

        try {
            networkLocationEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            gpsLocationEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

        } catch (Exception ex) {
            Log.d(TAG, "Excepción al obtener información de localización");
        }

        return networkLocationEnabled || gpsLocationEnabled;
    }

    private void showToastMessage (String message) {//Funcion para mostrar mensaje en pantalla sin alertbox
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public void enviardato(String identificador) throws IOException {
        //Actualizamos numeor de datos enviados



//Comprovamos que no ha enviado un dato con ese mismo id, ya que un mismo voluntario solo debe cojer le dato de un mismo corroedor. Es una forma de tener una memeoria
        /*
        * Como aun no s eha definido un standard para los dispositvos BLE se busca dos ejemplos de beacons de los que disponemos
        * */

        //--------------------------------Formateado Coorecto con la tipologia de CorredorID-0001--------
        //Los beacons de las carrera tienen este format: CorredorID-000001
        //Comprovamos que la primera parte de la string se corresponde para en ese cso enviar el dato
        String IDcorredor=identificador;
        String[] idcorseparated = IDcorredor.split("-");
        String idC=idcorseparated[1];//Cojemos la segund parte del string que contien el numeor de identificador
        String corredorid = removeZero(idC);//Esta es la variable resultante que enviaremos para guardar el dato
        int c;
        int res=0;
        int resneg=0;
        //Algoritmo para comprobar si ya se ha enviado ese dato
        for(c=0;c<memoriacorredores.size();c++){
            if(memoriacorredores.get(c).equals(IDcorredor)){
                res++;
            }
            else{
                resneg++;
            }
        }
        if (res!=0){//Ya se ha enviado este id
            //Ya no se muestra el mensaje en el caso de que sea dos veces el mismo identificador
            //alertmessageLogin(8);
            return;
        }
        //Actualizamos numeoro de datos enviados
        contadordedatosenviados=contadordedatosenviados+1;
        memoriacorredores.add(IDcorredor);
        actualizartextview();
        showToastMessage(String.valueOf(contadordedatosenviados));

        //timer.schedule(new SmallDelay(), 10);

        // Creamos la cola de Peticiones/Solicitudes
        queue = Volley.newRequestQueue(this);

        // Los parametros para el php, hacemos el mapping para poder pasarlo a JSON
        // map.put(KEY, VALUE);
        map.put("unameV", Usuario);
        map.put("latitud", latitud);
        map.put("longitud", longitud);
        map.put("nomcarrera", Nom_Carrera);
        map.put("IDCorredor", corredorid); //Lo recibimos por BLE
        String url="http://"+getResources().getString(R.string.ip_server_Login)+"/InsertarDato_APP.php"; // Cremos la variable url para que se edite automaticamente desde el fichero Strings
        // La solicitud JSON
        // JsonObjectRequest(METHOD, URL, JSONOBJECT(PARAMETERS), OK_LISTENER, ERROR_LISTENER);

        request = new JsonObjectRequest(
                Request.Method.POST, // the request method
                //getResources().getString(R.string.meatShootingMessage, numPoundsMeat);
                //"http://192.168.43.84:8080/check-login_APP_voluntarios.php", // the URL
                url,
                new JSONObject(map), // the parameters for the php
                new Response.Listener<JSONObject>() { // the response listener
                    @Override
                    public void onResponse(JSONObject response){
                        // Aquí parseamos la respuesta
                        Log.d("Response", String.valueOf(response));
                        // Aquí parseamos la respuesta
                        JSONObject myJson = response;

                        String resp = null;
                        try { //Comprovamos la respuesta recivida del server
                            resp = myJson.getString("res");
                            if(resp.equals("OK")) {//Se ha inciado sesion corectamente
                                //alertmessageLogin(1);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() { // the error listener
                    @Override
                    public void onErrorResponse(VolleyError error) { // En caso de error con la conexon con el server entrariamos en esta parte del codigo

                        //ya no se muestra el mensaje de error porue sino la aplicación peta de la de veces que escanea por segundo
                        //alertmessageLogin(4);
                    }
                });
        // Ejecutamos la solicitud para btener la informcion en formato JSON
        queue.add(request);

    }

    @Override
    protected void onDestroy() {//Cuando salimos de la activty cerramos tambien el Broadcast que utiliza el servicio GPS
        super.onDestroy();
        if(broadcastReceiver != null){
            unregisterReceiver(broadcastReceiver);
        }
    }
    private void enable_buttons() {//Funcion que activa y desactiva el servicioGPS
        //Utilizamos los listener de los botones de start stop gps
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Creamos el intent en la clase del servicio y lo iniciamos
                Intent i =new Intent(getApplicationContext(),GPS_Service.class);
                startService(i);

            }
        });

        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Paramos el intent de la clase GPS qe esta cursando el servicio GPS
                Intent i = new Intent(getApplicationContext(),GPS_Service.class);
                stopService(i);


            }
        });

    }

    private boolean runtime_permissions() {//Pedimos permisos para la localizacion GPS
        if(Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},100);

            return true;
        }
        return false;
    }

    private void actualizartextview (){
        TextView Numero = (TextView)findViewById(R.id.numerodeenvios);
        Numero.setText(String.valueOf(contadordedatosenviados));
    }
    public void alertmessageLogin(int a){ // Función donde creamos todos los alert necesarios
        // make a handler that throws a runtime exception when a message is received
        final Handler handler = new Handler() {

            public void handleMessage(Message mesg) {
                throw new RuntimeException();
            }
        };

        // make a text input dialog and show it
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Datos:"); // El titulo de todos los alert es el nombre de la ativity

        //Dependiendo del valor de a saltara un activity diferente

        if (a==1){
            builder.setMessage("Dato insertado correctamente");
        }
        if (a==70){
            builder.setMessage("un detectado");
        }

        if (a==4){
            builder.setMessage("ESTA APLICACION REQUIERE CONEXION CON EL SERVIDOR PARA FUNCIONAR. ASEGURESE DE TENERLA.");
        }
        if (a==8){
            builder.setMessage("Este identificador ya ha registrado un dato.");
        }

        builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                handler.sendMessage(handler.obtainMessage());
            }
        });

        builder.show(); // Mostramos el alert

        // loop till a runtime exception is triggered.
        try { Looper.loop(); }
        catch(RuntimeException e2) {}
    }

    public static String removeZero(String str)
    {//Funcion para quitar ceros inicales dentro de un string de numeros
        // Count leading zeros
        int i = 0;
        while (i < str.length() && str.charAt(i) == '0')
            i++;

        // Convert str into StringBuffer as Strings
        // are immutable.
        StringBuffer sb = new StringBuffer(str);

        // The  StringBuffer replace function removes
        // i characters from given index (0 here)
        sb.replace(0, i, "");

        return sb.toString();  // return in String
    }

}