package com.example.hp.proyectoldb;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class EdicionStats extends AppCompatActivity {
    int idEvento, idEquipo, numComp,numCompInfec, idUsu, numPruebas, superviviente;
    String nick, stringSuperviviente, nombre, nombreEquipo;
    boolean estoyEnLista, heSobrevivido = false;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edicion_stats);

        //Recogemos lo que haya en Extra
        Bundle bundle = getIntent().getExtras();
        nombre = bundle.getString("nombreEdicion");
        String fecha = bundle.getString("fecha");
        nick = bundle.getString("nick");

        TextView txtEdicionStats = findViewById(R.id.txtEdicionStats);
        final CheckBox checkBoxStats = findViewById(R.id.checkboxStats);
        final EditText editPruebas = findViewById(R.id.editPruebas);
        Button botonEstadistica =findViewById(R.id.botonEstadisctica);

        TextView txtHasSobrevivido = findViewById(R.id.txtHasSobrevivido);
        final TextView txtPruebasRealizadas = findViewById(R.id.txtPruebasRealizadas);
        TextView txtNumComp = findViewById(R.id.txtCompaneros);
        TextView txtNumCompInfectados = findViewById(R.id.txtInfectados);
        TextView txtPorcentaje = findViewById(R.id.txtPorcentaje);
        TextView txtequipoParticipado = findViewById(R.id.txtEquipoParticipado);

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
        //Sacamos si ha sobrevivido y añadimos al texTview
        try {
            new GetSobrevivido().execute(idUsu,idEvento).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        txtHasSobrevivido.setText(stringSuperviviente);

        //Sacamos nombre del equipo
        try {
            new GetEquipo().execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        txtequipoParticipado.setText(nombreEquipo);

        //Sacamos numero de compañeros
        try {
            new GetCompaneros().execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Si estamos en el equipo, restamos 1
        try {
            new GetUsuariosEquipo().execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        txtNumComp.setText(""+numComp);

        //Sacamos el numero de persona del equipo infectadas
        try {
            new GetCompanerosInfectados().execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        txtNumCompInfectados.setText(""+numCompInfec);

        //Agregamos numero de pruebas
        try {
            new GetPruebas().execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        txtPruebasRealizadas.setText(""+numPruebas);

        //Calculamos porcentaje de infeccion
        int porcentajeInfectado = 0;
        if(!heSobrevivido && estoyEnLista)
        {
            porcentajeInfectado = ((numCompInfec+1)*100)/(numComp+1);
        }
        else if(heSobrevivido && estoyEnLista)
        {
            porcentajeInfectado = (numCompInfec*100)/(numComp+1);
        }
        else
        {
            porcentajeInfectado = (numCompInfec*100)/numComp;
        }

        txtPorcentaje.setText(porcentajeInfectado+"%");

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

                try {
                    new CrearParticipacionStats().execute(idUsu,idEvento).get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Toast.makeText(EdicionStats.this, "Estadisticas guardadas!", Toast.LENGTH_SHORT).show();

            }
        });

        txtEdicionStats.setText(nombre);
    }
    class GetSobrevivido extends AsyncTask<Integer, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Integer... arg0) {
            HttpHandler handler = new HttpHandler();

            //IP CLASE
            //String url2 = "http://192.168.20.154/api/v1/isuperviviente/"+arg0[0]+"/"+arg0[1];

            //IP CASA
            //String url2 = "http://192.168.1.109//api/v1/superviviente/"+arg0[0]+"/"+arg0[1];

            //IP TRABAJO
            String url2 = "http://10.245.97.173/api/v1/superviviente/"+arg0[0]+"/"+arg0[1];

            //HOSTING
            //String url2 = "http://losdeblanco.000webhostapp.com/ldbapi/public/api/v1/superviviente/"+arg0[0]+"/"+arg0[1];

            String sobrevivido = handler.makeServiceCall(url2);
            if(sobrevivido != null)
            {
                stringSuperviviente = sobrevivido;
                if(stringSuperviviente.equals("si"))
                {
                    heSobrevivido = true;
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
    class GetEquipo extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler handler = new HttpHandler();

            //IP CLASE
            //String url2 = "http://192.168.20.154/api/v1/equipo/"+idUsu+"/"+idEvento;

            //IP CASA
            //String url2 = "http://192.168.1.109/api/v1/equipo/"+idUsu+"/"+idEvento;

            //IP TRABAJO
            String url2 = "http://10.245.97.173/api/v1/equipo/"+idUsu+"/"+idEvento;

            //HOSTING
            //String url2 = "http://losdeblanco.000webhostapp.com/ldbapi/public/api/v1/equipo/"+idUsu+"/"+idEvento;

            String equipo = handler.makeServiceCall(url2);
            if(equipo != null)
            {
                nombreEquipo = equipo;
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
            String url2 = "http://10.245.97.173/api/v1/idEvento/"+arg0[0];

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
    class GetCompaneros extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            System.out.println("Se esta descargando los USUARIOS del JSON");
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler handler = new HttpHandler();

            //IP CLASE
            //String url2 = "http://192.168.20.154/api/v1/numComp/"+nombreEquipo+"/"+idEvento;

            //IP CASA
            //String url2 = "http://192.168.1.109/api/v1/numComp/"+nombreEquipo+"/"+idEvento;

            //IP TRABAJO
            String url2 = "http://10.245.97.173/api/v1/numComp/"+nombreEquipo+"/"+idEvento;

            //HOSTING
            //String url2 = "http://losdeblanco.000webhostapp.com/ldbapi/public/api/v1/numComp/"+nombreEquipo+"/"+idEvento;

            String participacion = handler.makeServiceCall(url2);
            if(participacion != null)
            {
                try {
                    JSONObject jsonObject = new JSONObject(participacion);
                    JSONArray compa = jsonObject.getJSONArray("companeros");

                    for(int i = 0; i < compa.length();i++)
                    {
                        JSONObject jsonComp = compa.getJSONObject(i);
                        numComp = jsonComp.getInt("numComp");
                        idEquipo = jsonComp.getInt("idEquipo");
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
    class GetPruebas extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            System.out.println("Se esta descargando los USUARIOS del JSON");
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler handler = new HttpHandler();

            //IP CLASE
            //String url2 = "http://192.168.20.154/api/v1/numPruebas/"+idUsu+"/"+idEvento;

            //IP CASA
            //String url2 = "http://192.168.1.109/api/v1/numPruebas/"+idUsu+"/"+idEvento;

            //IP TRABAJO
            String url2 = "http://10.245.97.173/api/v1/numPruebas/"+idUsu+"/"+idEvento;
            //HOSTING
            //String url2 = "http://losdeblanco.000webhostapp.com/ldbapi/public/api/v1/numPruebas/"+idUsu+"/"+idEvento;

            String participacion = handler.makeServiceCall(url2);
            if(participacion != null)
            {
                try {
                    JSONObject jsonObject = new JSONObject(participacion);
                    JSONArray compa = jsonObject.getJSONArray("pruebas");

                    for(int i = 0; i < compa.length();i++)
                    {
                        JSONObject jsonComp = compa.getJSONObject(i);
                        numPruebas = jsonComp.getInt("pruebasRealizadas");
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
    class GetCompanerosInfectados extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            System.out.println("Se esta descargando los USUARIOS del JSON");
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler handler = new HttpHandler();

            //IP CLASE
            //String url2 = "http://192.168.20.154/api/v1/numCompInfectados/"+idEquipo+"/"+idEvento;

            //IP CASA
            //String url2 = "http://192.168.1.109/api/v1/numCompInfectados/"+idEquipo+"/"+idEvento;

            //IP TRABAJO
            String url2 = "http://10.245.97.173/api/v1/numCompInfectados/"+idEquipo+"/"+idEvento;

            //HOSTING
            //String url2 = "http://losdeblanco.000webhostapp.com/ldbapi/public/api/v1/numCompInfectados/"+idEquipo+"/"+idEvento;

            String participacion = handler.makeServiceCall(url2);
            if(participacion != null)
            {
                try {
                    JSONObject jsonObject = new JSONObject(participacion);
                    JSONArray compa = jsonObject.getJSONArray("infectados");

                    for(int i = 0; i < compa.length();i++)
                    {
                        JSONObject jsonComp = compa.getJSONObject(i);
                        numCompInfec = jsonComp.getInt("numCompInfectados");
                    }
                    if(!heSobrevivido)
                    {
                        numCompInfec--;
                    }
                    if(numCompInfec<0)
                    {
                        numCompInfec = 0;
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
    class GetUsuariosEquipo extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            System.out.println("Se esta descargando los USUARIOS del JSON");
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler handler = new HttpHandler();

            //IP CLASE
            //String url2 = "http://192.168.20.154/api/v1/usuariosEquipo/"+nombre+"/"+nombreEquipo;

            //IP CASA
            //String url2 = "http://192.168.1.109/api/v1/usuariosEquipo/"+nombre+"/"+nombreEquipo;

            //IP TRABAJO
            String url2 = "http://10.245.97.173/api/v1/usuariosEquipo/"+nombre+"/"+nombreEquipo;

            //HOSTING
            //String url2 = "http://losdeblanco.000webhostapp.com/ldbapi/public/api/v1/usuariosEquipo/"+nombre+"/"+nombreEquipo;

            String participacion = handler.makeServiceCall(url2);
            if(participacion != null)
            {
                try {
                    JSONObject jsonObject = new JSONObject(participacion);
                    JSONArray compa = jsonObject.getJSONArray("usuarios");
                    ArrayList<String> listaUsu = new ArrayList<>();
                    for(int i = 0; i < compa.length();i++)
                    {
                        JSONObject jsonComp = compa.getJSONObject(i);
                        listaUsu.add(jsonComp.getString("nick"));
                    }
                    for(int i = 0; i < listaUsu.size();i++)
                    {
                        if(listaUsu.get(i).trim().equals(nick.trim()))
                        {
                            numComp--;
                            estoyEnLista = true;
                        }
                    }
                    if(numComp < 0)
                    {
                        numComp = 0;
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
            String url2 = "http://10.245.97.173/api/v1/crearParticipacionStats";

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
