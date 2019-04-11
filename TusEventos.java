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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class TusEventos extends AppCompatActivity {

    ArrayList<String> datos = new ArrayList<>();
    ArrayList<Integer> idsEventos = new ArrayList<>();
    Context ctx = this;
    String nick;
    int idEvento;
    int idUsu;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tus_eventos);

        ListView listaTusEventos = findViewById(R.id.listaTusEventos);

        //Recogemos lo que haya en Extra
        Bundle bundle = getIntent().getExtras();
        nick = bundle.getString("nick");

        //Recogemos el idUsu del usuario en sesion
        try {
            new GetIdUsu().execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Sacamos todos los idEvento que tenga ese usuario en la tabla participacion
        //y los guardamos en un ArrayList
        try {
            new GetParticipacion().execute(idUsu).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //recorremos el ArrayList y vamos recogiendo y mostrando los datos de los eventos
        //pertenecientes a dichos ids
        for(int i = 0; i < idsEventos.size();i++) {
            try {
                new GetEvento().execute(idsEventos.get(i)).get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        final ArrayAdapter<String> adaptador = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,datos);
        listaTusEventos.setAdapter(adaptador);

        //notificamos al adaptador cuando haya cambios en el listView
        adaptador.notifyDataSetChanged();

        listaTusEventos.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(final AdapterView<?> parent, View v,
                                           final int index, long arg3) {

                AlertDialog.Builder builderAlerta = new AlertDialog.Builder(ctx);
                builderAlerta.setCancelable(true);
                builderAlerta.setTitle("Eliminar participacion");
                builderAlerta.setMessage("¿Estas seguro de dejar de participar en este evento?");
                builderAlerta.setPositiveButton("Aceptar",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                String nombreSZ = "";
                                String ediocionYFecha = parent.getItemAtPosition(index).toString();
                                char[] caracteres = ediocionYFecha.toCharArray();

                                //Como nos devuelve el texto con el siguiente formato 'nombre: fecha'
                                //lo formateamos, quedandonos solo con el nombre
                                for (int i = 0; i < caracteres.length; i++) {
                                    if(caracteres[i] == ':')
                                    {
                                        break;
                                    }
                                    else{
                                        nombreSZ += caracteres[i];
                                    }
                                }

                                //Cogemos el idEvento del evento que pinchemos
                                try {
                                    new GetIdEvento().execute(nombreSZ).get();
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                try {
                                    System.out.println("IdEvento: "+idEvento+" idUsu: "+idUsu);
                                    new EliminarParticipacionUsuario().execute(idEvento,idUsu).get();
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                Toast.makeText(ctx, "Eliminada participacion en "+nombreSZ, Toast.LENGTH_SHORT).show();

                                //eliminamos el item seleccionado y notificamos al adaptador
                                datos.remove(index);
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

        listaTusEventos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long l) {

                //Sacamos el nombre de la edicion para enviarselo a la clase edicion
                String nombreSZ = "";
                String fechaSZ = "";
                String ediocionYFecha = parent.getItemAtPosition(position).toString();
                char[] caracteres = ediocionYFecha.toCharArray();

                //Como nos devuelve el texto con el siguiente formato 'nombre: fecha'
                //lo formateamos, quedandonos solo con el nombre
                //Guardamos el valor total de la "i" para sacar luego la fecha
                int valorTotalI = 0;
                for (int i = 0; i < caracteres.length; i++) {
                    if(caracteres[i] == ':')
                    {
                        break;
                    }
                    else{
                        nombreSZ += caracteres[i];
                        valorTotalI = i;
                    }
                }
                //sacamos la fecha sabiendo donde acaba el nombre de la SZ y le restamos 1 para que
                //se salte el carcatr ":"
                for(int i = 0; i < caracteres.length;i++)
                {
                    if( i-1 > valorTotalI)
                    {
                        fechaSZ += caracteres[i];
                    }
                }
                //separamos dia, mes y año para compararlo con la fecha actual
                String mesSz = fechaSZ.substring(3,5);
                String anoSz = fechaSZ.substring(6,10);
                String diaSz = fechaSZ.substring(0,2);

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                Date date = new Date();
                String hoy = dateFormat.format(date);

                //separamos dia, mes y año para compararlo con la fecha de la edicion
                String mesHoy = hoy.substring(3,5);
                String anoHoy = hoy.substring(6,10);
                String diaHoy = hoy.substring(0,2);

                //si el dia de la fecha de la SZ es mayor o igual que la de hoy, mostrar la actividad
                //de evento pendiente
                if(diaSz.compareTo(diaHoy) >= 0 && mesSz.compareTo(mesHoy) >= 0 && anoSz.compareTo(anoHoy) >= 0) {
                    //Esta edicion aun no se ha jugado
                    Intent intent = new Intent(ctx,EdicionPendiente.class);
                    //Le pasamos datos a la nueva actividad
                    intent.putExtra("nombreEdicion",nombreSZ);
                    intent.putExtra("fecha",fechaSZ);
                    startActivity(intent);
                }
                else{
                    //Esta es una edicion jugada
                    Intent intent = new Intent(ctx,EdicionStats.class);
                    //Le pasamos datos a la nueva actividad
                    intent.putExtra("nombreEdicion",nombreSZ);
                    intent.putExtra("nick",nick);
                    startActivity(intent);
                }
            }
        });
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
            String url2 = "http://10.245.97.173/api/v1/participacion/"+arg0[0];

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
                        idsEventos.add(evento.getInt("idEvento"));
                        System.out.println("GetParticipacion LLEGUEE");
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
    class GetEvento extends AsyncTask<Integer, Void, Void> {

        String token;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            System.out.println("Se esta descargando los USUARIOS del JSON de GetEvento");
        }

        @Override
        protected Void doInBackground(Integer... arg0) {
            HttpHandler handler = new HttpHandler();

            //IP CLASE
            //String url2 = "http://192.168.20.154/api/v1/eventoConIDOrdenado/"+arg0[0];

            //IP CASA
            //String url2 = "http://192.168.1.109/api/v1/eventoConIDOrdenado/"+arg0[0];

            //IP TRABAJO
            String url2 = "http://10.245.97.173/api/v1/eventoConIDOrdenado/" + arg0[0];

            //HOSTING
            //String url2 = "http://losdeblanco.000webhostapp.com/ldbapi/public/api/v1/eventoConIDOrdenado/"+arg0[0];

            System.out.println("ARG0___: "+arg0[0]);
            String eventos = handler.makeServiceCall(url2);
            System.out.println("EVENTOS___: "+eventos);
            if (eventos != null) {
                try {
                    JSONObject jsonObject = new JSONObject(eventos);
                    JSONArray evento = jsonObject.getJSONArray("evento");

                    for (int i = 0; i < evento.length(); i++) {
                        JSONObject eventoJson = evento.getJSONObject(i);
                        String nombre = eventoJson.getString("nombreEvento");
                        String fecha = eventoJson.getString("fechaEvento");

                        System.out.println("NOMBRE SZ: "+nombre);

                        String mes = fecha.substring(5, 7);
                        String ano = fecha.substring(0, 4);
                        String dia = fecha.substring(8, 10);
                        String fechaFinal = dia + "/" + mes + "/" + ano;

                        datos.add(nombre+":"+fechaFinal);
                        System.out.println("GetEvento LLEGUEE");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("NO SE HA PODIDO ESTABLECER CONEXION CON LA URL");
            }
            return null;
        }

    }

    class EliminarParticipacionUsuario extends AsyncTask<Integer, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Integer... arg0) {
            HttpHandler handler = new HttpHandler();

            //IP CLASE
            //String url2 = "http://192.168.20.154/api/v1/eliminarParticipacionUsuario/"+arg0[0]+"/"+arg0[1];

            //IP CASA
            //String url2 = "http://192.168.1.109//api/v1/eliminarParticipacionUsuario/"+arg0[0]+"/"+arg0[1];

            //IP TRABAJO
            String url2 = "http://10.245.97.173/api/v1/eliminarParticipacionUsuario/"+arg0[0]+"/"+arg0[1];

            //HOSTING
            //String url2 = "http://losdeblanco.000webhostapp.com/ldbapi/public/api/v1/eliminarParticipacionUsuario/"+arg0[0]+"/"+arg0[1];

            System.out.println("SE VA A ELIMINAR");
            handler.eliminarDato(url2);
            System.out.println("Eliminado!!!");

            return null;
        }

    }

}
