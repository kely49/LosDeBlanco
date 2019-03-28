package com.example.hp.proyectoldb;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.concurrent.ExecutionException;

public class EdicionStats extends AppCompatActivity {
    int idEvento, idUsu, numPruebas, superviviente;
    String nick;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edicion_stats);

        //Recogemos lo que haya en Extra
        Bundle bundle = getIntent().getExtras();
        final String nombre = bundle.getString("nombreEdicion");
        String fecha = bundle.getString("fecha");
        nick = bundle.getString("nick");

        TextView txtEdicionStats = findViewById(R.id.txtEdicionStats);
        final CheckBox checkBoxStats = findViewById(R.id.checkboxStats);
        final EditText editPruebas = findViewById(R.id.editPruebas);
        Button botonEstadistica =findViewById(R.id.botonEstadisctica);

        botonEstadistica.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(checkBoxStats.isChecked())
                {
                    superviviente = 1;
                }
                else{
                    superviviente = 0;
                }

                numPruebas = Integer.parseInt(editPruebas.getText().toString());

                //Cogemos el idEvento del evento que pinchemos
                try {
                    new GetIdEvento().execute(nombre).get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //Sacamos el idUsu del usuario que está en sesion
                try {
                    new GetIdUsu().execute().get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                try {
                    new CrearParticipacionStats().execute(idUsu,idEvento).get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });



        txtEdicionStats.setText(nombre);
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
            System.out.println("NICK: "+nick);

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
    class GetIdEvento extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... arg0) {
            HttpHandler handler = new HttpHandler();

            //IP CLASE
            //String url2 = "http://192.168.20.154/api/v1/idEvento/"+arg0[0];

            //IP CASA
            //String url2 = "http://192.168.1.109//api/v1/idEvento/"+arg0[0];

            //IP TRABAJO
            String url2 = "http://16.19.142.155/api/v1/idEvento/"+arg0[0];

            //HOSTING
            //String url2 = "http://losdeblanco.000webhostapp.com/ldbapi/public/api/v1/idEvento/"+arg0[0];

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
    class CrearParticipacionStats extends AsyncTask<Integer, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Integer... arg0) {
            HttpHandler handler = new HttpHandler();

            //IP CLASE
            //String url2 = "http://192.168.20.154/api/v1/crearParticipacionStats";

            //IP CASA
            //String url2 = "http://192.168.1.109/api/v1/crearParticipacionStats";

            //IP TRABAJO
            String url2 = "http://16.19.142.155/api/v1/crearParticipacionStats";

            //HOSTING
            //String url2 = "http://losdeblanco.000webhostapp.com/ldbapi/public/api/v1/crearParticipacionStats";

            int idUsu = arg0[0];
            int idEvento = arg0[1];

            final JSONObject data = new JSONObject();
            try {
                data.put("idUsu",idUsu);
                data.put("idEvento",idEvento);
                data.put("pruebasRealizadas",numPruebas);
                data.put("superviviente",superviviente);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            System.out.println("JSON A AÑADIR: "+data.toString());
            handler.crearPUT(url2,data.toString());

            return null;
        }

    }
}
