package com.example.hp.proyectoldb;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

public class Registrar extends AppCompatActivity {

    Context ctx = this;
    ArrayList<String> listaUsuarios = new ArrayList<>();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registrar);

        final Button botonRegistro = findViewById(R.id.botonRegistrar);
        final EditText editNick = findViewById(R.id.editUsu);
        final EditText editPass = findViewById(R.id.editPass);
        final EditText editRePass = findViewById(R.id.editRepPass);
        final EditText editNombre = findViewById(R.id.editNombre);
        final EditText editApellido = findViewById(R.id.editApellido);
        final EditText editEmail = findViewById(R.id.editEmail);
        final EditText editFecha = findViewById(R.id.editFechNac);

        final Calendar myCalendar = Calendar.getInstance();

        //Recogemos la fecha elegida por el usuario en el calendario y se la metemos al editText de fecha
        final DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,int dayOfMonth) {

                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                String myFormat = "yyyy-MM-dd"; //In which you need put here
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat);

                editFecha.setText(sdf.format(myCalendar.getTime()));
            }

        };

        //Al hacer click sobre el editText de fecha, se nos abre un calendario
        editFecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(ctx, datePickerListener, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        //Listener para el boton. Lanza el AsyncTask
        botonRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nick = editNick.getText().toString();
                String contrasena= editPass.getText().toString();
                String email= editEmail.getText().toString();
                String nombre= editNombre.getText().toString();
                String apellido= editApellido.getText().toString();
                String fecha= editFecha.getText().toString();
                boolean usuarioRepe = false;

                try {
                    new GetUsuarios().execute().get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                for(int i =0; i < listaUsuarios.size();i++)
                {
                    if(listaUsuarios.get(i).equals(nick.trim()))
                    {
                        usuarioRepe = true;
                    }
                }

                //Comprobar que ambas contraseñas coinciden, que no dejan contraseña en blanco y que
                //todos los campos quedan rellenos
                if(!editPass.getText().toString().equals(editRePass.getText().toString()))
                {
                    Toast.makeText(ctx, "Las contraseñas NO coinciden", Toast.LENGTH_SHORT).show();
                }
                else if(editPass.getText().toString().equals("") || editRePass.getText().toString().equals(""))
                {
                    Toast.makeText(ctx, "Debes insertar una contraseña", Toast.LENGTH_SHORT).show();
                }
                else if(editNick.getText().toString().equals("") || editEmail.getText().toString().equals("") ||
                        editNombre.getText().toString().equals("") || editApellido.getText().toString().equals("")
                        || editFecha.getText().toString().equals(""))
                {
                    Toast.makeText(ctx, "Debes Rellenar todos los campos", Toast.LENGTH_SHORT).show();
                }
                else
                {   if(!usuarioRepe)
                    {
                        try {
                            new RegistrarUsuarios().execute(nick, contrasena, email, nombre, apellido, fecha).get();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        Toast.makeText(ctx, "Registro Exitoso", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ctx, Login.class);
                        startActivity(intent);
                    }
                    else
                    {
                        Toast.makeText(ctx, "Usuario ya registrado", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
    //Clase que se encarga de sacar el JSON de la URL
    class RegistrarUsuarios extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... arg) {
            HttpHandler handler = new HttpHandler();

            //Recogemos los datos que se pasan al AsyncTask
            String nick =arg[0];
            String contrasena=arg[1];
            String email=arg[2];
            String nombre=arg[3];
            String apellido=arg[4];
            String fecha=arg[5];

            //IP CLASE
            //String url = "http://192.168.20.154/api/v1/crearUsuario";

            //IP CASA
            //String url = "http://192.168.1.109/api/v1/crearUsuario";

            //IP TRABAJO
            String url = "http://16.19.142.155/api/v1/crearUsuario";

            //HOSTING
            //String url = "http://losdeblanco.000webhostapp.com/ldbapi/public/api/v1/crearUsuario";

            //Creamos un JSON con los datos que nos pase el usuario
            final JSONObject data = new JSONObject();
            try {
                data.put("nick",nick);
                data.put("contrasena",contrasena);
                data.put("email",email);
                data.put("nombreUsu",nombre);
                data.put("apellidoUsu",apellido);
                data.put("fechaNac",fecha);
                data.put("imagenPerfil","");
                System.out.println("JSON: "+data.toString());

            } catch (JSONException e) {
                e.printStackTrace();
            }

            if(url != null)
            {
                //Mandamos el JSON a la URL
                handler.crearPOST(url,data.toString());
            }
            else{
                System.out.println("NO SE HA PODIDO ESTABLECER CONEXION CON LA URL");
            }
            return null;
        }
    }
    class GetUsuarios extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            System.out.println("Se esta descargando los USUARIOS del JSON");
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler handler = new HttpHandler();

            //IP CLASE
            //String url = "http://192.168.20.154/api/v1/usuarios";

            //IP CASA
            //String url = "http://192.168.1.109/api/v1/usuarios";

            //IP TRABAJO
            String url = "http://16.19.142.155/api/v1/usuarios";

            //HOSTING
            //String url = "http://losdeblanco.000webhostapp.com/ldbapi/public/api/v1/usuarios";

            //Hacemos peticion a la url y recivimos respuesta
            String jsonStr = handler.makeServiceCall(url);

            if(jsonStr != null)
            {
                try {
                    JSONObject jsonObject = new JSONObject(jsonStr);
                    JSONArray usuarios = jsonObject.getJSONArray("usuarios");

                    for(int i = 0; i < usuarios.length();i++)
                    {
                        JSONObject usuario = usuarios.getJSONObject(i);
                        String nombre = usuario.getString("nick");
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
    }
}



