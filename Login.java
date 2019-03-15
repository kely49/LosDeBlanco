package com.example.hp.proyectoldb;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;

public class Login extends AppCompatActivity {
    ArrayList<String> listaUsuarios = new ArrayList<>();

    final Context ctx = this;
    String usuario;
    String contraseña;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final Button botonLogin = findViewById(R.id.botonLogin);
        final EditText editUsu = findViewById(R.id.loginUsu);
        final EditText editPass = findViewById(R.id.passUsu);

        botonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ctx, Agenda.class);
                botonLogin.setBackgroundColor(Color.parseColor("#08088A"));

                //Recogemos los datos que nos da el usuario
                usuario = editUsu.getText().toString();
                contraseña = editPass.getText().toString();

                //Evitamos que se ejecute AsynTask para que le de tiempo a recoger los datos del usuario
                //Antes de que se ejecute AsyncTask
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                new GetUsuarios().execute();
            }
        });
    }
    //Clase que se encarga de sacar el JSON de la URL
    class GetUsuarios extends AsyncTask<Void, Void, Void> {

        String token;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            System.out.println("Se esta descargando los USUARIOS del JSON");
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler handler = new HttpHandler();
            String url = "http://192.168.1.109/api/v1/usuarios";
            String url2 = "http://192.168.1.109/api/v1/login/"+usuario+"/"+contraseña;
            System.out.println("Accediendo a la url:"+url2);

            //Hacemos peticion a la url y recivimos respuesta
            String jsonStr = handler.makeServiceCall(url);
            token = handler.makeServiceCall(url2);


            if(token != null)
            {
                try {
                    JSONObject jsonObject = new JSONObject(jsonStr);
                    JSONArray usuarios = jsonObject.getJSONArray("usuarios");

                    for(int i = 0; i < usuarios.length();i++)
                    {
                        JSONObject usuario = usuarios.getJSONObject(i);
                        String nombre = usuario.getString("nick");
                        System.out.println(nombre);
                        listaUsuarios.add(nombre);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else{
                System.out.println("NO SE HA PODIDO ESTABLECER CONEXION CON LA URL");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Intent intent = new Intent(ctx, Agenda.class);

            //Convertimos el token a entero, valor por defecto -1 para que no haga login
            int tokenValido = -1;
            try {
                tokenValido = NumberFormat.getInstance().parse(token).intValue();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            //Comprobamos que el token recibido no sea -1
            //INFO: valor de token --> -1(Login invalido) -10(Admin) cualquier otro(Login valido)
            if(tokenValido == -1)
            {
                Toast.makeText(ctx, "Usuario/contraseña Incorrecto", Toast.LENGTH_SHORT).show();

            }
            else{
                Toast.makeText(ctx, "Token: "+token, Toast.LENGTH_SHORT).show();
                intent.putExtra("token",token);
                startActivity(intent);
            }

        }
    }
}

