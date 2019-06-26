package com.rot.rot;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.os.*;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.rot.rot.R.layout.activity_login;

public class Login extends AppCompatActivity {

    /*Activity Inical del proyecto, el login a la app donde hacemos consulta al server con el metodo VOlley
    * tambien tenemos acceso a un boton de info de la aplicación.
    * @author Victor Dorado Fernández*/


    //Variables necesarias para los metodos de envio "Volley"
    RequestQueue queue;
    JsonObjectRequest request;
    Map<String, String> map = new HashMap<String, String>();

    //Variable donde guardamos el usuario con el que se hace el login, para despues poder pasarlo a la activity del menu
    String USUARIOdef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(activity_login);
        getSupportActionBar().hide();//<!-- Para que no salga la barra superior en todas las activitis-->
    }
    public void login(final View view) throws IOException { //Funcion donde haremos el login

        // Recogemos los valores de Uuario y Password y los pasamos a una variable String para tratar con ellos.
        EditText USUARIO = (EditText) findViewById(R.id.Usuario);
        String usu = USUARIO.getText().toString();
        EditText Password = (EditText) findViewById(R.id.Pass);
        String pass = Password.getText().toString();

        //Guardamos la variable que contiene el usuario en una variable final para poder tratar con ella entre activities.
        USUARIOdef=usu;


        /*Para hacer el login vamos a utilizar el metodo Volley, el cual nos permite hacerlo sin crear asyntasks
        * y haciendo un simple mapeo de las dos variables que necesitamos para hacer el login con conexion al fichero
        * PHP que esta en el server XAMPP. */

        // Creamos la cola de Peticiones/Solicitudes
        queue = Volley.newRequestQueue(this);

        // Los parametros para el php, hacemos el mapping para poder pasarlo a JSON
        // map.put(KEY, VALUE);
        map.put("uname", usu);
        map.put("psw", pass);
        String url="http://"+getResources().getString(R.string.ip_server_Login)+"/check-login_APP_voluntarios.php"; // Cremos la variable url para que se edite automaticamente desde el fichero Strings
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
                            if(resp.equals("OK")){//Se ha inciado sesion corectamente

                                alertmessageLogin(1);
                                openUserMenu();// Abrimos la activity del menu
                            }
                            if(resp.equals("KO")){//La contrseña que se ha dado es incorrecta
                                alertmessageLogin(2);

                            }
                            if(resp.equals("KO2")){//El usuario que se ha dado no existe

                                alertmessageLogin(0);

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() { // the error listener
                    @Override
                    public void onErrorResponse(VolleyError error) { // En caso de error con la conexon con el server entrariamos en esta parte del codigo

                        alertmessageLogin(4);

                    }
                });

        // Ejecutamos la solicitud para btener la informcion en formato JSON
        queue.add(request);

    }
    public void openUserMenu() { // Funcion que utilizamos para abrir la activity del menu
        Intent intent = new Intent(Login.this, Menu.class);
        intent.putExtra("EXTRA_SESSION_USER", USUARIOdef);// Pasamos la variable del usuario a la activity nuevo
        startActivity(intent);

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
        builder.setTitle("LOG IN:"); // El titulo de todos los alert es el nombre de la ativity

        //Dependiendo del valor de a saltara un intent con un mensaje diferente

        if (a==1){
            builder.setMessage("Se ha inicado sesion correctamente");
        }
        if (a==2){
            builder.setMessage("Contraseña incorrecta");
        }
        if (a==0){
            builder.setMessage("Este usuario no existe, intentelo de nuevo.");
        }

        if (a==4){
            builder.setMessage("ESTA APLICACION REQUIERE CONEXION CON EL SERVIDOR PARA FUNCIONAR. ASEGURESE DE TENERLA.");
        }
        if (a==10){
            builder.setMessage("Esta aplicación forma parte del TFG de Víctor Dorado. Alumno de EETAC - UPC ");
        }
        if (a==11){
            String str = "000001";
            str = removeZero(str);
            builder.setMessage("El res es: " + str);
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

//Si clicamos en el botoon de Mas informacion, salta la siguiente función que simplemente llama a mostrar un AlertBox con información.
public void moreinfo(final View view) throws IOException {
    alertmessageLogin(10);

}
    public static String removeZero(String str)
    {
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