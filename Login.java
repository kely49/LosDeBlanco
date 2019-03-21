package com.example.hp.proyectoldb;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.text.NumberFormat;
import java.text.ParseException;

public class Login extends AppCompatActivity {

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
        final TextView txtRegistrar = findViewById(R.id.txtRegistro);
        final CheckBox cajaRecordar = findViewById(R.id.recordarLogin);
        final SharedPreferences prefs = getSharedPreferences("Preferencias",Context.MODE_PRIVATE);

        //Si hay un nick en preferencias, lo cargamos
        editUsu.setText(prefs.getString("nick",""));

        botonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                botonLogin.setBackgroundColor(Color.parseColor("#08088A"));

                //Recogemos los datos que nos da el usuario
                usuario = editUsu.getText().toString();
                contraseña = editPass.getText().toString();

                //Comprobamos que la checkbox esté marcada para guardar el usuario en preferencias
                if(cajaRecordar.isChecked())
                {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("nick", usuario);
                    editor.commit();
                }

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
        txtRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ctx,Registrar.class);
                startActivity(intent);
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
            /*
            //IP CLASE
            String url2 = "http://192.168.20.44/api/v1/login/"+usuario+"/"+contraseña;*/

            /*//IP CASA
            String url2 = "http://192.168.1.109/api/v1/login/"+usuario+"/"+contraseña;
            */
            //IP TRABAJO
            String url2 = "http://16.19.142.155/api/v1/login/"+usuario+"/"+contraseña;

            //Guardamos el valor del token
            token = handler.makeServiceCall(url2);

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
                //Se lo pasamos a la siguiente actividad
                intent.putExtra("token",tokenValido);
                intent.putExtra("nick",usuario);
                startActivity(intent);
            }

        }
    }
}

