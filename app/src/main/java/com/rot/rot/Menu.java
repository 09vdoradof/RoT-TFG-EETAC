package com.rot.rot;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/* En esta activity Menu, lo que se hace es cargar las carreras en las que el voluntario participa dando información de las mismas.
*  Una vez seleccionado una carrera y en el caso de que esta tenga un estado actual de en curso,
 *  dara paso a la siguiente activity en la cual se podrán obtener datos de los corredores.
 *  @author Victor Dorado Fernández
* */
public class Menu extends AppCompatActivity {
    private TextView mTextViewResult;
    String Usuario; // Variable usuario para utilizar durante el fichero
    String estadocarrera;   //Variable para controlar el estado de la carrera que se ha seleccionado
    String CarreraSel;  //Variable ue guarda a carrera que se ha seleccionado
    //Variables necesarias para los metodos de envio "Volley"
    RequestQueue queue;
    JsonObjectRequest request;
    Map<String, String> map = new HashMap<String, String>();
    ArrayList<String> arraySpinner = new ArrayList<String>();//Array de Strings que se tiliza para rellenar el spinner

    String[] Carreras;// Vector de strings de las carreras en las que el usuario participa
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        getSupportActionBar().hide();//<!-- Para que no salga la barra superior en todas las activitis-->

        //Recuperamos la variable usuario que pasamos desde login
        Bundle p = getIntent().getExtras();
        String usu = p.getString("EXTRA_SESSION_USER");
        Usuario = usu;

        arraySpinner.add("Elige una carrera:");//Añadimos el "Titulo para el spinner"

        cargarcarreras(usu);//Llamamos a la funcion de cargar carreras del usuario que se ha logeado

        //Inicializamos el spinner con los nombres de carreras obtenidos de la carga y metidos guardados en "arrayspinner"
        final Spinner s = (Spinner) findViewById(R.id.Spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, arraySpinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(adapter);

        //Se crea el listener para que cuando se selecciona una opcion del spinner se realizen las funcione sindicadas
        s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override

            //En el caso de que se selecciona un item
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                //Recojemos el nombre de la carrera selecionado y llamamos a la funcionnde actualizar dando el nombre como parametro
                String eleccion=s.getSelectedItem().toString();
                ActualizarScreen(eleccion);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // En caso de que no se selecciona nada, no se realiza ninguna ación.
            }

        });
    }

    public void cargarcarreras(String usuario){//Funcion donde se cargan las carreras en las que el volutario participa


        // Creamos la cola de Peticiones/Solicitudes
        queue = Volley.newRequestQueue(this);

        // Los parametros para el php, hacemos el mapping para poder pasarlo a JSON
        // map.put(KEY, VALUE);
        map.put("uname", Usuario);
        String url="http://"+getResources().getString(R.string.ip_server_Login)+"/Consulta_Carreras_UnVoluntarioEspecifico_APP.php"; // Cremos la variable url para que se edite automaticamente desde el fichero Strings
        // La solicitud JSON
        // JsonObjectRequest(METHOD, URL, JSONOBJECT(PARAMETERS), OK_LISTENER, ERROR_LISTENER);

        request = new JsonObjectRequest(
                Request.Method.POST, // the request method
                //"http://192.168.43.84:8080/Consulta_Carreras_UnVoluntarioEspecifico_APP.php", // the URL
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
                            resp.substring(0, resp.length() - 1);// Quitamos el ultimo / de la respuesta para poder dividir bien la respuesta

                            // Cada carrera va seprada por un / y cada dato de la carrera por un -

                            String[] separated = resp.split("/");// Separamos todos las carreras en un vector.

                            Carreras = separated;//Guaradamos la variable para tenr tdas las carreras
                            for(int i =0;i<separated.length;i++){//Añadimos opciones al spinner para elegi carrera
                                String[] DatosCarreras=separated[i].split("-");
                                String opt=DatosCarreras[0] + " - "+ DatosCarreras[2];
                                arraySpinner.add(opt);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() { // the error listener
                    @Override
                    public void onErrorResponse(VolleyError error) { // En caso de error con la conexon con el server entrariamos en esta parte del codigo

                        alertmessageMenu(4);

                    }
                });

        // Ejecutamos la solicitud para btener la informcion en formato JSON
        queue.add(request);



    }
    public void ActualizarScreen(String x){//Dependiendo del valor que s haya escojido, se actualiza los datos de pantalla
        try{
            String[] nombre = x.split("-");// Dividimos el string para quitar el año
            //nombre[0].substring(0, nombre[0].length() - 1);//Quitamos es ultimo espacio de este string

            if(nombre[0].equals("Elige una carrera:")){// NO hace nada si esta en esta opcion
                return;
            }
            else {
                for(int i=0;i<Carreras.length;i++){//En caso de elegir una comprovamos sus datos
                    String nom=nombre[0].substring(0, nombre[0].length() - 1);
                    CarreraSel=nom; //Guardamos el nombre de la carrera para despues pasarlo a la otra activity
                    if(nom.equals(Carreras[i].split("-")[0])){//Nos quedamos con los datos de la carrera que coincida el nombre
                        //Recordamos que los datos que nos pasa desde server son: Nombre, ciudad, anyo y estado de carrera
                        //Tratamos los datos que recojemos desde el servidor
                        String Ciudad=Carreras[i].split("-")[1];
                        String Anyo=Carreras[i].split("-")[2];
                        String Estado=Carreras[i].split("-")[3];
                        //Comprovamos estado de carrera
                        if(Estado.equals("0")){
                            Estado ="La carrera no ha iniciado";
                        }
                        if(Estado.equals("1")){
                            Estado ="La carrera esta en curso";
                        }
                        if(Estado.equals("2")){
                            Estado ="La carrera ha finalizado";
                        }
                        //Guardamos el estado de carrera como variable global para poder detectar despues si la carrera seleccionada esta en curso y en ese caso abrir la activity de recolección de datos
                        estadocarrera = Estado;

                        //Escribimos en los TextViews los datos importantes de la carrera
                        TextView city = (TextView)findViewById(R.id.textView7);
                        city.setText(Ciudad);
                        TextView year = (TextView)findViewById(R.id.textView5);
                        year.setText(Anyo);
                        TextView state = (TextView)findViewById(R.id.textView4);
                        state.setText(Estado);
                    }


                }
            }

        }catch(Exception e){
            String efw="";//En caso de excepción ya hay otros controladores que la detectaran
        }


    }

    public void enviardato(final View view) throws IOException {
        //Comprovamos si la carrera seleccionada se pueden enviar datos
        if (estadocarrera.equals("La carrera no ha iniciado")){
            alertmessageMenu(0);
        }
        if (estadocarrera.equals("La carrera esta en curso")){
            alertmessageMenu(1);
            openProcesardatos();//Abrimos la pantalla d eenvio de datos
        }
        if (estadocarrera.equals("La carrera ha finalizado")){
            alertmessageMenu(2);
        }

    }
    public void openProcesardatos() { // Funcion que utilizamos para abrir la activity del menu
        //Intent intent = new Intent(Menu.this, BLE_Service.class);
        Intent intent = new Intent(Menu.this, Gestiondatos.class);
        intent.putExtra("EXTRA_SESSION_USER", Usuario);// Pasamos la variable del usuario a la activity nuevo
        intent.putExtra("EXTRA_SESSION_USER2", CarreraSel);// Pasamos la variable del usuario a la activity nuevo
        startActivity(intent);

    }
    public void alertmessageMenu(int a){ // Función donde creamos todos los alert necesarios
        // make a handler that throws a runtime exception when a message is received
        final Handler handler = new Handler() {

            public void handleMessage(Message mesg) {
                throw new RuntimeException();
            }
        };

        // make a text input dialog and show it
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Menu:"); // El titulo de todos los alert es el nombre de la ativity

        //Dependiendo del valor de a saltara un activity diferente

        if (a==1){
            builder.setMessage("La carrera está en curso, se te da accesso a una nueva pantalla para registrar datos");
        }
        if (a==2){
            builder.setMessage("Esta carrera ya ha finalizado y por lo tanto no puedes enviar datos");
        }
        if (a==0){
            builder.setMessage("Esta carrera todavía no ha empezado y por lo tanto no puedes enviar datos");
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

}
