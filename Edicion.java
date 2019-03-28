package com.example.hp.proyectoldb;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.concurrent.ExecutionException;


public class Edicion extends AppCompatActivity {
    ImageView imagen;
    Context ctx = this;
    String nombreEdicion, foto, nick;
    TextView fechaEdicion;
    TextView txtProvincia;
    TextView txtLocalidad;
    String fechaFinal;
    boolean duplicado = false;
    int idUsu, idEvento;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edicion);

        TextView txtNombreEdicion = findViewById(R.id.nombreEdicion);
        fechaEdicion = findViewById(R.id.fechaEdicion);
        imagen = findViewById(R.id.imagenEdicion);
        txtProvincia = findViewById(R.id.provinciaEdicion);
        txtLocalidad = findViewById(R.id.localidadEdicion);
        final CheckBox checkBox = findViewById(R.id.checkboxEdicion);
        Button botonGuardar = findViewById(R.id.botonEdicion);

        //Sacamos todos los Extra
        Bundle bundle = getIntent().getExtras();
        nombreEdicion = bundle.getString("nombreEdicion");
        nick = bundle.getString("nick");
        final int token = bundle.getInt("token");

        //ponemos el nombre de edicion que sacamos de Extra
        txtNombreEdicion.setText(nombreEdicion);

        //sacamos y mostramos toda la info de la edicion
        try {
            new GetEvento().execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        fechaEdicion.setText(fechaFinal);
        //si el campo foto no esta vacio, cargamos la foto que nos pasen
        //si no queda cargada la que está por defecto
        if(!foto.equals("")) {
            //Cargar la imagen
            Picasso.get()
                    .load(foto)
                    .into(imagen);
        }


        botonGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Verificamos que el checkbox está activado
                if(checkBox.isChecked())
                {
                    //Sacamos el idUsu del usuario que está en sesion
                    try {
                        new GetIdUsu().execute().get();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //sacamos el idEvento del evento en el que estemos
                    try {
                        new GetIdEvento().execute().get();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //recorremos los idEventos que tiene ese usuario en la tabla participacion para
                    //ver si va a haber duplicado de datos o no
                    try {
                        new GetParticipacion().execute(idUsu).get();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //si no va a haber un dupliado de datos
                    if(!duplicado) {
                        //Creamos la relacion en la tabla participacion
                        try {
                            new CrearParticipacion().execute(idUsu, idEvento).get();
                            System.out.println("JSON AÑADIDO!");
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        Toast.makeText(Edicion.this, "Se ha guardado correctamente!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ctx, Agenda.class);
                        intent.putExtra("token", token);
                        intent.putExtra("nick", nick);

                        startActivity(intent);
                    }
                    else
                    {
                        Toast.makeText(ctx, "Ya estas participando!", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });

    }
    class CrearParticipacion extends AsyncTask<Integer, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Integer... arg0) {
            HttpHandler handler = new HttpHandler();

            //IP CLASE
            //String url2 = "http://192.168.20.154/api/v1/crearParticipacion";

            //IP CASA
            //String url2 = "http://192.168.1.109/api/v1/crearParticipacion";

            //IP TRABAJO
            String url2 = "http://16.19.142.155/api/v1/crearParticipacion";

            //HOSTING
            //String url2 = "http://losdeblanco.000webhostapp.com/ldbapi/public/api/v1/crearParticipacion";

            int idUsu = arg0[0];
            int idEvento = arg0[1];

            final JSONObject data = new JSONObject();
            try {
                data.put("idUsu",idUsu);
                data.put("idEvento",idEvento);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            System.out.println("JSON A AÑADIR: "+data.toString());
            handler.crearPOST(url2,data.toString());

            return null;
        }

    }
    class GetIdEvento extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler handler = new HttpHandler();

            //IP CLASE
            //String url2 = "http://192.168.20.154/api/v1/idEvento/"+nombreEdicion;

            //IP CASA
            //String url2 = "http://192.168.1.109/api/v1/idEvento/"+nombreEdicion;

            //IP TRABAJO
            String url2 = "http://16.19.142.155/api/v1/idEvento/"+nombreEdicion;

            //HOSTING
            //String url2 = "http://losdeblanco.000webhostapp.com/ldbapi/public/api/v1/idEvento/"+nombreEdicion;

            String evento = handler.makeServiceCall(url2);
            if(evento != null)
            {
                try {
                    idEvento = NumberFormat.getInstance().parse(evento).intValue();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            else{
                System.out.println("NO SE HA PODIDO ESTABLECER CONEXION CON LA URL");
            }
            return null;
        }

    }
    class GetIdUsu extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler handler = new HttpHandler();

            //IP CLASE
            //String url2 = "http://192.168.20.154/api/v1/idUsuario/"+nick;

            //IP CASA
            //String url2 = "http://192.168.1.109/api/v1/idUsuario/"+nick;

            //IP TRABAJO
            String url2 = "http://16.19.142.155/api/v1/idUsuario/"+nick;

            //HOSTING
            //String url2 = "http://losdeblanco.000webhostapp.com/ldbapi/public/api/v1/idUsuario/"+nick;

            String usuario = handler.makeServiceCall(url2);
            if(usuario != null)
            {
                try {
                    idUsu = NumberFormat.getInstance().parse(usuario).intValue();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            else{
                System.out.println("NO SE HA PODIDO ESTABLECER CONEXION CON LA URL");
            }
            return null;
        }

    }
    class GetParticipacion extends AsyncTask<Integer, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            System.out.println("Se esta descargando los USUARIOS del JSON");
        }

        @Override
        protected Void doInBackground(Integer... arg0) {
            HttpHandler handler = new HttpHandler();

            //IP CLASE
            //String url2 = "http://192.168.20.154/api/v1/participacion/"+arg0[0];

            //IP CASA
            //String url2 = "http://192.168.1.109/api/v1/participacion/"+arg0[0];

            //IP TRABAJO
            String url2 = "http://16.19.142.155/api/v1/participacion/"+arg0[0];

            //HOSTING
            //String url2 = "http://losdeblanco.000webhostapp.com/ldbapi/public/api/v1/participacion/"+arg0[0];

            String participacion = handler.makeServiceCall(url2);
            if(participacion != null)
            {
                try {
                    JSONObject jsonObject = new JSONObject(participacion);
                    JSONArray usuarios = jsonObject.getJSONArray("participacion");

                    for(int i = 0; i < usuarios.length();i++)
                    {
                        JSONObject evento = usuarios.getJSONObject(i);
                        int idEventoDuplicado =  evento.getInt("idEvento");
                        if(idEvento == idEventoDuplicado)
                        {
                            duplicado = true;
                        }
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

    class GetEvento extends AsyncTask<Void, Void, Void> {

        String token;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            System.out.println("Se esta descargando los USUARIOS del JSON");
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler handler = new HttpHandler();

            //IP CLASE
            //String url2 = "http://192.168.20.154/api/v1/evento/"+nombreEdicion;

            //IP CASA
            //String url2 = "http://192.168.1.109/api/v1/evento/"+nombreEdicion;

            //IP TRABAJO
            String url2 = "http://16.19.142.155/api/v1/evento/"+nombreEdicion;

            //HOSTING
            //String url2 = "http://losdeblanco.000webhostapp.com/ldbapi/public/api/v1/evento/"+nombreEdicion;

            String eventos = handler.makeServiceCall(url2);
            if(eventos != null)
            {
                try {
                    JSONObject jsonObject = new JSONObject(eventos);
                    JSONArray usuarios = jsonObject.getJSONArray("evento");

                    for(int i = 0; i < usuarios.length();i++)
                    {
                        JSONObject evento = usuarios.getJSONObject(i);
                        String fecha =  evento.getString("fechaEvento");
                        String localidad = evento.getString("localidad");
                        String provincia = evento.getString("provincia");
                        String imagen = evento.getString("foto");

                        String mes = fecha.substring(5,7);
                        String ano = fecha.substring(0,4);
                        String dia = fecha.substring(8,10);
                        fechaFinal = dia+"/"+mes+"/"+ano;

                        foto = imagen;
                        txtLocalidad.setText(localidad);
                        txtProvincia.setText(provincia);
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
