package com.example.hp.proyectoldb;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

public class Agenda extends AppCompatActivity {
    Context ctx = this;
    ArrayList<String> datos =  new ArrayList<>();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.agenda);

        //AQUI EMPIEZA MENU SLIDE

        //AQUI TERMINA
        //Creamos un bundle con todos los Extras
        Bundle bundle = getIntent().getExtras();
        //Recuperamos el token y nick que nos dio el login
        final int token = bundle.getInt("token");
        Button botonAñadirEvent = findViewById(R.id.botonAñadirEvent);
        botonAñadirEvent.setVisibility(View.INVISIBLE);
        ListView listaEdiciones = findViewById(R.id.listView);

        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Ejecutamos AsyncTask
        new GetEventos().execute();

        ArrayAdapter<String> adaptador = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,datos);

        listaEdiciones.setAdapter(adaptador);
        //notificamos al adaptador cuando haya cambios en el listView
        adaptador.notifyDataSetChanged();

        //Si el usuario es Admin, le mostramos el boton de añadir evento
        if(token == -10)
        {
            botonAñadirEvent.setVisibility(View.VISIBLE);
        }

        //TODO: Recoger el nombre de la SZ pinchada y recuperar los datos de la db de dicha SZ

        //TODO: Accedemos a la actividad donde se muestra la edicion
        //Intent intent = new Intent(ctx,MostrarEdicion.class);
        //startActivity(intent);

        botonAñadirEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ctx,CrearEdicion.class);
                intent.putExtra("token",token);
                startActivity(intent);
                finish();
            }
        });
    }
    //Clase que se encarga de sacar el JSON de la URL
    class GetEventos extends AsyncTask<Void, Void, Void> {

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
            String url2 = "http://192.168.20.44/api/v1/eventos";*/

            /*//IP CASA
            String url2 = "http://192.168.1.109/api/v1/eventos";
            */
            //IP TRABAJO
            String url2 = "http://16.19.142.155/api/v1/eventos";

            //Guardamos el valor del token
            String eventos = handler.makeServiceCall(url2);
            if(eventos != null)
            {
                try {
                    JSONObject jsonObject = new JSONObject(eventos);
                    JSONArray usuarios = jsonObject.getJSONArray("eventos");

                    for(int i = 0; i < usuarios.length();i++)
                    {
                        JSONObject evento = usuarios.getJSONObject(i);
                        String nombre = evento.getString("nombreEvento");
                        String fecha =  evento.getString("fechaEvento");

                        //sacamos el dia, mes, año para ordenarlo y que lo vea mejor el usuario
                        String mes = fecha.substring(5,7);
                        String ano = fecha.substring(0,4);
                        String dia = fecha.substring(8,10);
                        String fechaFinal = dia+"/"+mes+"/"+ano;

                        datos.add(nombre+" : "+fechaFinal);
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
