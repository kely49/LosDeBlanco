package com.example.hp.proyectoldb;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class CreacionEquipo extends AppCompatActivity {


    ArrayList<String> listaEquipos = new ArrayList<>();
    String nick, nombreEdit, contraEquipo;
    int idUsu;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.creacion_equipos);

        //Creamos un bundle con todos los Extras
        Bundle bundle = getIntent().getExtras();
        final int token = bundle.getInt("token");
        nick = bundle.getString("nick");

        final EditText editEquipo = findViewById(R.id.editEquipo);
        Button botonEquipo = findViewById(R.id.botonEquipoCreado);
        final EditText editContraEquipo = findViewById(R.id.editContraEquipo);

        botonEquipo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contraEquipo = editContraEquipo.getText().toString();
                nombreEdit = editEquipo.getText().toString().trim();
                String nombreEditSinEspacios = nombreEdit.replaceAll("\\s","");
                System.out.println("1111");
                try {
                    new GetEquipos().execute().get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int iguales = 0;
                for(int i = 0; i < listaEquipos.size();i++) {
                    String nombreEquipo = listaEquipos.get(i);
                    String trim = nombreEquipo.trim();
                    String sinBlancos = trim.replaceAll("\\s","");
                    iguales = nombreEditSinEspacios.compareToIgnoreCase(sinBlancos);
                }
                //Si el nombre de equipo que meten NO coincide con el nombre de otro equipo se crea
                if(nombreEditSinEspacios.equals(""))
                {
                    Toast.makeText(CreacionEquipo.this, "Debes insertar un nombre de equipo", Toast.LENGTH_SHORT).show();
                }
                else if(iguales != 0) {
                    try {
                        new GetIdUsu().execute().get();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    try {
                        new CrearEquipo().execute(idUsu).get();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(CreacionEquipo.this, "Equipo creado correctamente", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(CreacionEquipo.this, "Equipo existente", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    class GetEquipos extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            System.out.println("Se esta descargando los USUARIOS del JSON");
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler handler = new HttpHandler();

            //IP CLASE
            //String url2 = "http://192.168.20.154/api/v1/equipos";

            //IP CASA
            //String url2 = "http://192.168.1.109/api/v1/equipos";

            //IP TRABAJO
            String url2 = "http://10.245.97.173/api/v1/equipos";

            //HOSTING
            //String url2 = "http://losdeblanco.000webhostapp.com/ldbapi/public/api/v1/equipos";

            String equipos = handler.makeServiceCall(url2);
            if(equipos != null)
            {
                try {
                    JSONObject jsonObject = new JSONObject(equipos);
                    JSONArray arrayEquipo = jsonObject.getJSONArray("equipos");

                    for(int i = 0; i < arrayEquipo.length();i++)
                    {
                        JSONObject evento = arrayEquipo.getJSONObject(i);
                        String nombreEquipo = evento.getString("nombreEquipo");
                        int idCreador =  evento.getInt("idCreador");

                        listaEquipos.add(nombreEquipo);
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
            String url2 = "http://10.245.97.173/api/v1/idUsuario/"+nick;

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
    class CrearEquipo extends AsyncTask<Integer, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Integer... arg0) {
            HttpHandler handler = new HttpHandler();

            //IP CLASE
            //String url2 = "http://192.168.20.154/api/v1/crearEquipo";

            //IP CASA
            //String url2 = "http://192.168.1.109/api/v1/crearEquipo";

            //IP TRABAJO
            String url2 = "http://10.245.97.173/api/v1/crearEquipo";

            //HOSTING
            //String url2 = "http://losdeblanco.000webhostapp.com/ldbapi/public/api/v1/crearEquipo";;

            final JSONObject data = new JSONObject();
            try {
                data.put("nombreEquipo",nombreEdit);
                data.put("contrasenaEquipo",contraEquipo);
                data.put("idCreador",arg0[0]);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            System.out.println("JSON A AÑADIR: "+data.toString());
            handler.crearPOST(url2,data.toString());

            return null;
        }
    }
}
