package com.example.hp.proyectoldb;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class UnirseEquipo extends AppCompatActivity {
    Context ctx = this;
    ArrayList<String> listaEventos = new ArrayList<>();
    ArrayList<Integer> idsEventos = new ArrayList<>();
    ArrayList<String> arrayEquipos = new ArrayList<>();
    int idUsu, idEvento, idEquipo, passExitosa;
    String nick;
    boolean duplicado;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.unirse_equipo);

        //Creamos un bundle con todos los Extras
        Bundle bundle = getIntent().getExtras();
        final int token = bundle.getInt("token");
        nick = bundle.getString("nick");

        Spinner spinner = findViewById(R.id.spinnerUnirseEquipos);
        final ListView listaEquipos = findViewById(R.id.listViewEquipos);
        Button botonUnirseEquipos = findViewById(R.id.botonBuscar);
        final EditText editBuscar = findViewById(R.id.editBuscar);

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

        //recorremos el ArrayList, vamos recogiendo los datos de los eventos y guardandolos en un ArrayList
        for(int i = 0; i < idsEventos.size();i++) {
            try {
                //Añadimos solo los eventos que no hayan pasado
                new GetEvento().execute(idsEventos.get(i)).get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        ArrayAdapter<String> adaptador = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, listaEventos);
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adaptador);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                String nombreYFecha = parent.getItemAtPosition(position).toString();

                char[] caracteres = nombreYFecha.toCharArray();

                //Como nos devuelve el texto con el siguiente formato 'nombre: fecha'
                //lo formateamos, quedandonos solo con el nombre
                //Guardamos el valor total de la "i" para sacar luego la fecha
                String nombreSZ = "";
                for (int i = 0; i < caracteres.length; i++) {
                    if (caracteres[i] == ':') {
                        break;
                    }
                    else {
                        nombreSZ += caracteres[i];
                    }
                }

                try {
                    new GetIdEvento().execute(nombreSZ).get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        final ArrayAdapter<String> adaptadorList = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,arrayEquipos);
        listaEquipos.setAdapter(adaptadorList);

        listaEquipos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
           @Override
           public void onItemClick(AdapterView<?> parent, View view, int position, long l) {
               //Creamos el primer pop up con 2 botones
               final String nombreEquipo = parent.getItemAtPosition(position).toString();
               AlertDialog.Builder builderAlerta = new AlertDialog.Builder(ctx);
               builderAlerta.setCancelable(true);
               builderAlerta.setTitle("Confirmacion");
               builderAlerta.setMessage("¿Desea unirse a "+nombreEquipo+"?");
               builderAlerta.setPositiveButton("Aceptar",
                       new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialog, int which) {
                               try {
                                   new GetIdEquipo().execute(nombreEquipo).get();
                               } catch (ExecutionException e) {
                                   e.printStackTrace();
                               } catch (InterruptedException e) {
                                   e.printStackTrace();
                               }
                               duplicado = false;
                               //Comprobamos si ya esta participando en ese evento con ese equipo u otro equipo
                               try {
                                   new GetDuplicadoMiembro().execute(idEvento,idEquipo,idUsu).get();
                               } catch (ExecutionException e) {
                                   e.printStackTrace();
                               } catch (InterruptedException e) {
                                   e.printStackTrace();
                               }
                               System.out.println("idUsu:"+idUsu+" idEvento:"+idEvento+" idEquipo:"+idEquipo);
                               if(!duplicado) {
                                   //Si aceptan creamos el segundo popup que contiene el editText para insertar la password
                                   AlertDialog.Builder builderPass = new AlertDialog.Builder(ctx);
                                   builderPass.setCancelable(true);
                                   builderPass.setTitle("Acceso a equipo");
                                   builderPass.setMessage("Inserte contraseña");
                                   final EditText inputPass = new EditText(ctx);
                                   builderPass.setView(inputPass);
                                   builderPass.setPositiveButton("Aceptar",
                                           new DialogInterface.OnClickListener() {
                                               @Override
                                               public void onClick(DialogInterface dialog, int which) {

                                                   try {
                                                       new GetPassEquipoExitoso().execute(nombreEquipo, inputPass.getText().toString()).get();
                                                   } catch (ExecutionException e) {
                                                       e.printStackTrace();
                                                   } catch (InterruptedException e) {
                                                       e.printStackTrace();
                                                   }
                                                   //Si la contraseña que mete el usuario coincide con la del equipo
                                                   if (passExitosa == 1) {

                                                       //Creamos la relacion en la tabla miembros
                                                       try {
                                                           new CrearMiembro().execute(idEvento, idEquipo, idUsu).get();
                                                       } catch (ExecutionException e) {
                                                           e.printStackTrace();
                                                       } catch (InterruptedException e) {
                                                           e.printStackTrace();
                                                       }
                                                       Toast.makeText(UnirseEquipo.this, "Te has unido a " + nombreEquipo, Toast.LENGTH_SHORT).show();

                                                       //Actualizar tabla participacion, para meter el idEquipo
                                                       try {
                                                           new ActualizarParticipacion().execute(idEvento,idUsu,idEquipo).get();
                                                       } catch (ExecutionException e) {
                                                           e.printStackTrace();
                                                       } catch (InterruptedException e) {
                                                           e.printStackTrace();
                                                       }
                                                   } else {
                                                       Toast.makeText(UnirseEquipo.this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show();
                                                   }
                                               }
                                           });
                                   builderPass.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                       @Override
                                       public void onClick(DialogInterface dialog, int which) {
                                       }
                                   });

                                   AlertDialog dialogPass = builderPass.create();
                                   dialogPass.show();
                               }
                               else {
                                   Toast.makeText(ctx, "Ya estas apuntado en este evento", Toast.LENGTH_SHORT).show();
                               }

                           }

                       });
               builderAlerta.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                   }
               });

               AlertDialog dialogAlerta = builderAlerta.create();
               dialogAlerta.show();

           }});

        botonUnirseEquipos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Borramos el array cada vez que demos al boton
                arrayEquipos.clear();

                //Mostramos los equipos que haya buscado el usuario
                try {
                    new GetBuscarEquipos().execute(editBuscar.getText().toString()).get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //notificamos al adaptador cuando haya cambios en el listView
                adaptadorList.notifyDataSetChanged();
            }
        });
    }
    class ActualizarParticipacion extends AsyncTask<Integer, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Integer... arg) {
            HttpHandler handler = new HttpHandler();

            //Recogemos los datos que se pasan al AsyncTask
            int idEvento =arg[0];
            int idUsu=arg[1];
            int idEquipo = arg[2];

            //IP CLASE
            //String url = "http://192.168.20.154/api/v1/actualizarParticipacion/"+idUsu+"/"+idEvento;

            //IP CASA
            //String url = "http://192.168.1.109/api/v1/actualizarParticipacion/"+idUsu+"/"+idEvento;

            //IP TRABAJO
            String url = "http://10.245.97.173/api/v1/actualizarParticipacion/"+idUsu+"/"+idEvento;

            //HOSTING
            //String url = "http://losdeblanco.000webhostapp.com/ldbapi/public/api/v1/actualizarParticipacion/"+idUsu+"/"+idEvento;

            //Creamos un JSON con los datos que nos pase el usuario
            final JSONObject data = new JSONObject();
            try {
                data.put("idEvento",idEvento);
                data.put("idUsu",idUsu);
                data.put("idEquipo",idEquipo);

                System.out.println("JSON DATA: "+data.toString());

            } catch (JSONException e) {
                e.printStackTrace();
            }

            if(url != null)
            {
                //Mandamos el JSON a la URL
                handler.crearPUT(url,data.toString());
            }
            else{
                System.out.println("NO SE HA PODIDO ESTABLECER CONEXION CON LA URL");
            }
            return null;
        }
    }
    class CrearMiembro extends AsyncTask<Integer, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Integer... arg) {
            HttpHandler handler = new HttpHandler();

            //Recogemos los datos que se pasan al AsyncTask
            int idEvento =arg[0];
            int idEquipo=arg[1];
            int idUsu=arg[2];

            //IP CLASE
            //String url = "http://192.168.20.154/api/v1/crearMiembro";

            //IP CASA
            //String url = "http://192.168.1.109/api/v1/crearMiembro";

            //IP TRABAJO
            String url = "http://10.245.97.173/api/v1/crearMiembro";

            //HOSTING
            //String url = "http://losdeblanco.000webhostapp.com/ldbapi/public/api/v1/crearMiembro";

            //Creamos un JSON con los datos que nos pase el usuario
            final JSONObject data = new JSONObject();
            try {
                data.put("idEvento",idEvento);
                data.put("idEquipo",idEquipo);
                data.put("idUsu",idUsu);

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
    class GetDuplicadoMiembro extends AsyncTask<Integer, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Integer... arg0) {
            HttpHandler handler = new HttpHandler();

            //Recogemos los datos que se pasan al AsyncTask
            int idEvento =arg0[0];
            int idEquipo=arg0[1];
            int idUsu=arg0[2];

            //IP CLASE
            //String url2 = "http://192.168.20.154/api/v1/miembro/"+idUsu+"/"+idEvento+"/"+idEquipo;

            //IP CASA
            //String url2 = "http://192.168.1.109/api/v1/miembro/"+idUsu+"/"+idEvento+"/"+idEquipo;

            //IP TRABAJO
            String url2 = "http://10.245.97.173/api/v1/miembro/"+idUsu+"/"+idEvento+"/"+idEquipo;

            //HOSTING
            //String url2 = "http://losdeblanco.000webhostapp.com/ldbapi/public/api/v1/miembro/"+idUsu+"/"+idEvento+"/"+idEquipo;

            String evento = handler.makeServiceCall(url2);
            if(evento != null)
            {
                int token = 0;
                try {
                    token = NumberFormat.getInstance().parse(evento).intValue();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if(token == 1)
                {
                    duplicado = true;
                    System.out.println("duplicado!!");
                }
                else{
                    duplicado = false;
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
            //String url2 = "http://192.168.1.109/api/v1/idEvento/"+arg0[0];

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
    class GetPassEquipoExitoso extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... arg0) {
            HttpHandler handler = new HttpHandler();

            //IP CLASE
            //String url2 = "http://192.168.20.154/api/v1/contrasenaEquipo/"+arg0[0]+"/"+arg0[1];

            //IP CASA
            //String url2 = "http://192.168.1.109/api/v1/contrasenaEquipo/"+arg0[0]+"/"+arg0[1];

            //IP TRABAJO
            String url2 = "http://10.245.97.173/api/v1/contrasenaEquipo/"+arg0[0]+"/"+arg0[1];

            //HOSTING
            //String url2 = "http://losdeblanco.000webhostapp.com/ldbapi/public/api/v1/contrasenaEquipo/"+arg0[0]+"/"+arg0[1];

            String usuario = handler.makeServiceCall(url2);
            if(usuario != null)
            {
                try {
                    passExitosa = NumberFormat.getInstance().parse(usuario).intValue();

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
    class GetIdEquipo extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            System.out.println("Se esta descargando los USUARIOS del JSON");
        }

        @Override
        protected Void doInBackground(String... arg0) {
            HttpHandler handler = new HttpHandler();

            //IP CLASE
            //String url2 = "http://192.168.20.154/api/v1/IdEquipo/"+arg0[0];

            //IP CASA
            //String url2 = "http://192.168.1.109/api/v1/IdEquipo/"+arg0[0];

            //IP TRABAJO
            String url2 = "http://10.245.97.173/api/v1/IdEquipo/"+arg0[0];

            //HOSTING
            //String url2 = "http://losdeblanco.000webhostapp.com/ldbapi/public/api/v1/IdEquipo/"+arg0[0];

            String equipos = handler.makeServiceCall(url2);
            if(equipos != null)
            {
                try {
                    JSONObject jsonObject = new JSONObject(equipos);
                    JSONArray idEquipos = jsonObject.getJSONArray("equipo");

                    for(int i = 0; i < idEquipos.length();i++)
                    {
                        JSONObject equipo = idEquipos.getJSONObject(i);
                        idEquipo = equipo.getInt("idEquipo");
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
            //String url2 = "http://192.168.20.154/api/v1/eventoConID/"+arg0[0];

            //IP CASA
            //String url2 = "http://192.168.1.109/api/v1/eventoConID/"+arg0[0];

            //IP TRABAJO
            String url2 = "http://10.245.97.173/api/v1/eventoConID/" + arg0[0];

            //HOSTING
            //String url2 = "http://losdeblanco.000webhostapp.com/ldbapi/public/api/v1/eventoConID/"+arg0[0];

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

                        listaEventos.add(nombre+":"+fechaFinal);
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
    class GetBuscarEquipos extends AsyncTask<String, Void, Void> {

        String token;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            System.out.println("Se esta descargando los USUARIOS del JSON de GetEvento");
        }

        @Override
        protected Void doInBackground(String... arg0) {
            HttpHandler handler = new HttpHandler();

            //IP CLASE
            //String url2 = "http://192.168.20.154/api/v1/buscarEquipos/"+arg0[0];

            //IP CASA
            //String url2 = "http://192.168.1.109/api/v1/buscarEquipos/"+arg0[0];

            //IP TRABAJO
            String url2 = "http://10.245.97.173/api/v1/buscarEquipos/" + arg0[0];

            //HOSTING
            //String url2 = "http://losdeblanco.000webhostapp.com/ldbapi/public/api/v1/buscarEquipos/"+arg0[0];

            System.out.println("ARG0___: "+arg0[0]);
            String equipos = handler.makeServiceCall(url2);
            if (equipos != null) {
                try {
                    JSONObject jsonObject = new JSONObject(equipos);
                    JSONArray equipo = jsonObject.getJSONArray("equipos");

                    for (int i = 0; i < equipo.length(); i++) {
                        JSONObject eventoJson = equipo.getJSONObject(i);
                        String nombre = eventoJson.getString("nombreEquipo");

                        arrayEquipos.add(nombre);
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
}
