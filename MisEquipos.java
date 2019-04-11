package com.example.hp.proyectoldb;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MisEquipos extends AppCompatActivity {
    ArrayList<String>arrayEquipos = new ArrayList<>();
    ArrayList<String>arrayEventos = new ArrayList<>();
    ArrayList<String>equiposMostrar = new ArrayList<>();
    String nick;
    int idUsu;
    Context ctx = this;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mis_equipos);

        //Creamos un bundle con todos los Extras
        Bundle bundle = getIntent().getExtras();
        final int token = bundle.getInt("token");
        nick = bundle.getString("nick");

        ListView listaEquipos = findViewById(R.id.listaMisEquipos);

        try {
            new GetIdUsu().execute().get();
            new GetMisEquipos().execute(idUsu).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final ArrayAdapter<String> adaptador = new ArrayAdapter<>(ctx,android.R.layout.simple_list_item_1,equiposMostrar);
        listaEquipos.setAdapter(adaptador);
        //notificamos al adaptador cuando haya cambios en el listView
        adaptador.notifyDataSetChanged();

        listaEquipos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long l) {

                Intent intent = new Intent(ctx,Equipo.class);
                //Le pasamos datos a la nueva actividad
                intent.putExtra("nick",nick);
                intent.putExtra("token",token);
                intent.putExtra("equipo",arrayEquipos.get(position));
                intent.putExtra("evento",arrayEventos.get(position));
                startActivity(intent);

            }
        });

        listaEquipos.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> parent, View v,
                                           final int index, long arg3) {

                final String nombreEquipo = arrayEquipos.get(index);
                final String nombreSZ = arrayEventos.get(index);

                AlertDialog.Builder builderAlerta = new AlertDialog.Builder(ctx);
                builderAlerta.setCancelable(true);
                builderAlerta.setTitle("Eliminar registro");
                builderAlerta.setMessage("Se va a eliminar el equipo "+nombreEquipo+", que participa en "+nombreSZ);
                builderAlerta.setPositiveButton("Aceptar",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                try {
                                    new EliminarEquipoListaMiembro().execute(nick,nombreEquipo,nombreSZ).get();
                                    new EliminarEquipoListaPart().execute(nick,nombreEquipo,nombreSZ).get();
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                Toast.makeText(ctx, "Eliminado", Toast.LENGTH_SHORT).show();
                                equiposMostrar.remove(index);
                                adaptador.notifyDataSetChanged();
                            }
                        });
                builderAlerta.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                AlertDialog dialogAlerta = builderAlerta.create();
                dialogAlerta.show();

                return true;
            }
        });


    }
    class EliminarEquipoListaMiembro extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... arg0) {
            HttpHandler handler = new HttpHandler();

            //IP CLASE
            //String url2 = "http://192.168.20.154/api/v1/eliminarEquipoListaMiembro/"+arg0[0]+"/"+arg0[1]+"/"+arg0[2];

            //IP CASA
            //String url2 = "http://192.168.1.109//api/v1/eliminarEquipoListaMiembro/"+arg0[0]+"/"+arg0[1]+"/"+arg0[2];

            //IP TRABAJO
            String url2 = "http://10.245.97.173/api/v1/eliminarEquipoListaMiembro/"+arg0[0]+"/"+arg0[1]+"/"+arg0[2];

            //HOSTING
            //String url2 = "http://losdeblanco.000webhostapp.com/ldbapi/public/api/v1/eliminarEquipoListaMiembro/"+arg0[0]+"/"+arg0[1]+"/"+arg0[2];

            System.out.println("SE VA A ELIMINAR");
            handler.eliminarDato(url2);

            return null;
        }

    }
    class EliminarEquipoListaPart extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... arg0) {
            HttpHandler handler = new HttpHandler();

            //IP CLASE
            //String url2 = "http://192.168.20.154/api/v1/eliminarEquipoListaPart/"+arg0[0]+"/"+arg0[1]+"/"+arg0[2];

            //IP CASA
            //String url2 = "http://192.168.1.109//api/v1/eliminarEquipoListaPart/"+arg0[0]+"/"+arg0[1]+"/"+arg0[2];

            //IP TRABAJO
            String url2 = "http://10.245.97.173/api/v1/eliminarEquipoListaPart/"+arg0[0]+"/"+arg0[1]+"/"+arg0[2];

            //HOSTING
            //String url2 = "http://losdeblanco.000webhostapp.com/ldbapi/public/api/v1/eliminarEquipoListaPart/"+arg0[0]+"/"+arg0[1]+"/"+arg0[2];

            System.out.println("SE VA A ELIMINAR");
            handler.eliminarDato(url2);

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
                    System.out.println("GetIdUsu LLEGUEE");
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
    class GetMisEquipos extends AsyncTask<Integer, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            System.out.println("Se esta descargando los USUARIOS del JSON");
        }

        @Override
        protected Void doInBackground(Integer... arg0) {
            HttpHandler handler = new HttpHandler();

            int idUsu = arg0[0];

            //IP CLASE
            //String url2 = "http://192.168.20.154/api/v1/equiposConIDOrdenado/"+idUsu;

            //IP CASA
            //String url2 = "http://192.168.1.109/api/v1/equiposConIDOrdenado/"+idUsu;

            //IP TRABAJO
            String url2 = "http://10.245.97.173/api/v1/equiposConIDOrdenado/"+idUsu;

            //HOSTING
            //String url2 = "http://losdeblanco.000webhostapp.com/ldbapi/public/api/v1/equiposConIDOrdenado/"+idUsu;

            String misEquipos = handler.makeServiceCall(url2);
            if(misEquipos != null)
            {
                try {
                    JSONObject jsonObject = new JSONObject(misEquipos);
                    JSONArray equipos = jsonObject.getJSONArray("equipos");
                    for(int i = 0; i < equipos.length();i++)
                    {
                        JSONObject equipo = equipos.getJSONObject(i);
                        String evento = equipo.getString("nombreEvento");
                        String fecha = equipo.getString("fechaEvento");
                        String equipoString = equipo.getString("nombreEquipo");

                        String ano = fecha.substring(0,4);

                        arrayEventos.add(evento);
                        arrayEquipos.add(equipoString);

                        String stringEquipoMostrar =equipoString.toUpperCase()+"\n"+evento+" - "+ano;
                        equiposMostrar.add(stringEquipoMostrar);
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
