package com.example.hp.proyectoldb;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class Equipo extends AppCompatActivity {
    Context ctx = this;
    int token;
    ArrayList<String>listaUsuarios = new ArrayList<>();
    ArrayList<String>listaEquipos = new ArrayList<>();
    String nombreCreador, contrasenaEquipo, nombreEquipoNuevo,nick, nickLiderNuevo, equipo, evento;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.equipo);

        //Creamos un bundle con todos los Extras
        Bundle bundle = getIntent().getExtras();
        token = bundle.getInt("token");
        nick = bundle.getString("nick");
        equipo = bundle.getString("equipo");
        evento = bundle.getString("evento");

        final TextView txtEquipo = findViewById(R.id.txtEquipo);
        LinearLayout contenedorVertical = findViewById(R.id.layoutVertical);
        ImageView imagenAjustes = findViewById(R.id.imageNombre);
        imagenAjustes.setVisibility(View.INVISIBLE);

        txtEquipo.setText(equipo);

        try {
            new GetUsuariosEquipo().execute(evento,equipo).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            new GetCreadorEquipo().execute(equipo).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        nick = nick.trim().toLowerCase();

        System.out.println("USU: "+nick+" Creador: "+nombreCreador);
        System.out.println("IGUALES?: "+nick.equals(nombreCreador));

        for(int i = 0; i < listaUsuarios.size();i++) {
            LinearLayout contenedorHorizontal = new LinearLayout(ctx);
            contenedorHorizontal.setOrientation(LinearLayout.HORIZONTAL);

            final TextView txtUsu = new TextView(ctx);
            txtUsu.setId(i);
            txtUsu.setText(listaUsuarios.get(i));
            txtUsu.setTextColor(Color.WHITE);
            txtUsu.setTextSize(20);

            contenedorVertical.addView(contenedorHorizontal);
            contenedorHorizontal.addView(txtUsu);

            if(nick.equals(nombreCreador)) {
                imagenAjustes.setVisibility(View.VISIBLE);
                //Si el nombre de la lista es el mismo que el logeado, no puede borrarlo ni pasar lider

                if (!txtUsu.getText().toString().toLowerCase().trim().equals(nick.trim())) {
                    final ImageView imageBasura = new ImageView(ctx);
                    imageBasura.setImageResource(R.drawable.ic_action_trash);
                    imageBasura.setId(i);

                    final ImageView imageKey = new ImageView(ctx);
                    imageKey.setImageResource(R.drawable.ic_action_key);
                    imageKey.setId(i);

                    imageBasura.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Toast.makeText(ctx, "Imagen: " + imageBasura.getId() + "Usuario: " + txtUsu.getId(), Toast.LENGTH_SHORT).show();

                            AlertDialog.Builder builderAlerta = new AlertDialog.Builder(ctx);
                            builderAlerta.setCancelable(true);
                            builderAlerta.setTitle("Expulsar");
                            builderAlerta.setMessage("¿Desea echar a " + txtUsu.getText() + " del equipo?");
                            builderAlerta.setPositiveButton("Aceptar",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            try {
                                                new EliminarMiembroEquipo().execute(txtUsu.getText().toString(),equipo).get();
                                                new EliminarMiembroParticipacion().execute(txtUsu.getText().toString(),equipo).get();
                                            } catch (ExecutionException e) {
                                                e.printStackTrace();
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }

                                            Toast.makeText(ctx, "Usuario eliminado", Toast.LENGTH_SHORT).show();

                                            Intent intent = new Intent(ctx, Equipo.class);
                                            intent.putExtra("nick",nick);
                                            intent.putExtra("token",token);
                                            intent.putExtra("equipo",equipo);
                                            intent.putExtra("evento",evento);
                                            startActivity(intent);
                                            finish();
                                        }
                                    });
                            builderAlerta.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });

                            AlertDialog dialogAlerta = builderAlerta.create();
                            dialogAlerta.show();
                        }
                    });

                    imageKey.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AlertDialog.Builder builderAlerta = new AlertDialog.Builder(ctx);
                            builderAlerta.setCancelable(true);
                            builderAlerta.setTitle("Ceder lider");
                            builderAlerta.setMessage("¿Desea dejar de ser lider y dar liderazgo a " + txtUsu.getText() + "?");
                            builderAlerta.setPositiveButton("Aceptar",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            nickLiderNuevo = txtUsu.getText().toString();

                                            try {
                                                new CambiarCreador().execute(equipo).get();
                                            } catch (ExecutionException e) {
                                                e.printStackTrace();
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }

                                            Toast.makeText(ctx, "Ahora el lider del equipo es "+txtUsu.getText(), Toast.LENGTH_SHORT).show();

                                            Intent intent = new Intent(ctx, Equipo.class);
                                            intent.putExtra("nick",nick);
                                            intent.putExtra("token",token);
                                            intent.putExtra("equipo",equipo);
                                            intent.putExtra("evento",evento);
                                            startActivity(intent);
                                            finish();
                                        }
                                    });
                            builderAlerta.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });

                            AlertDialog dialogAlerta = builderAlerta.create();
                            dialogAlerta.show();
                        }
                    });

                    contenedorHorizontal.addView(imageBasura);
                    contenedorHorizontal.addView(imageKey);
                }
                imagenAjustes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        AlertDialog.Builder builderAlerta = new AlertDialog.Builder(ctx);
                        builderAlerta.setCancelable(true);
                        builderAlerta.setTitle("Configuración");
                        builderAlerta.setMessage("Configuración del equipo");
                        builderAlerta.setPositiveButton("Cambiar nombre",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        AlertDialog.Builder builderAlerta = new AlertDialog.Builder(ctx);
                                        builderAlerta.setCancelable(true);
                                        builderAlerta.setTitle("Cambio de nombre");
                                        builderAlerta.setMessage("Escriba el nuevo nombre del equipo");
                                        final EditText inputNombre = new EditText(ctx);
                                        builderAlerta.setView(inputNombre);
                                        builderAlerta.setPositiveButton("Aceptar",
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {

                                                        nombreEquipoNuevo = inputNombre.getText().toString();

                                                        try {
                                                            new GetEquipos().execute().get();
                                                        } catch (ExecutionException e) {
                                                            e.printStackTrace();
                                                        } catch (InterruptedException e) {
                                                            e.printStackTrace();
                                                        }

                                                        boolean repetido = false;
                                                        for(int i = 0; i < listaEquipos.size();i++)
                                                        {
                                                            if(listaEquipos.get(i).trim().equals(nombreEquipoNuevo.trim()))
                                                            {
                                                                System.out.println(listaEquipos.get(i).trim()+" - "+nombreEquipoNuevo);
                                                                System.out.println("REPETIDO?: "+listaEquipos.get(i).trim().equals(nombreEquipoNuevo.trim()));
                                                                repetido = true;
                                                                Toast.makeText(Equipo.this, "Este nombre de equipo ya existe", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                        if(!repetido) {
                                                            try {
                                                                new ActualizarNombreEquipo().execute(equipo).get();
                                                            } catch (ExecutionException e) {
                                                                e.printStackTrace();
                                                            } catch (InterruptedException e) {
                                                                e.printStackTrace();
                                                            }

                                                            txtEquipo.setText(nombreEquipoNuevo);

                                                            Toast.makeText(Equipo.this, "Nombre cambiado", Toast.LENGTH_SHORT).show();
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
                                    }
                            });
                        builderAlerta.setNegativeButton("Cambiar contraseña", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AlertDialog.Builder builderAlerta = new AlertDialog.Builder(ctx);
                                builderAlerta.setCancelable(true);
                                builderAlerta.setTitle("Cambio contraseña");
                                builderAlerta.setMessage("Escriba la nueva contraseña del equipo");
                                final EditText inputPass = new EditText(ctx);
                                builderAlerta.setView(inputPass);
                                builderAlerta.setPositiveButton("Aceptar",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                contrasenaEquipo = inputPass.getText().toString().trim();

                                                try {
                                                    new ActualizarContrasena().execute(equipo.trim()).get();
                                                } catch (ExecutionException e) {
                                                    e.printStackTrace();
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                }

                                                Toast.makeText(Equipo.this, "Contraseña cambiada", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                builderAlerta.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                });

                                AlertDialog dialogAlerta = builderAlerta.create();
                                dialogAlerta.show();
                            }
                        });

                        AlertDialog dialogAlerta = builderAlerta.create();
                        dialogAlerta.show();
                    }
                });
            }
        }
    }
    class GetUsuariosEquipo extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            System.out.println("Se esta descargando los USUARIOS del JSON");
        }

        @Override
        protected Void doInBackground(String... arg0) {
            HttpHandler handler = new HttpHandler();

            //IP CLASE
            //String url2 = "http://192.168.20.154/api/v1/usuariosEquipo/"+arg0[0]+"/"+arg0[1];

            //IP CASA
            //String url2 = "http://192.168.1.109/api/v1/usuariosEquipo/"+arg0[0]+"/"+arg0[1];
            //IP TRABAJO
            String url2 = "http://10.245.97.173/api/v1/usuariosEquipo/"+arg0[0]+"/"+arg0[1];

            //HOSTING
            //String url2 = "http://losdeblanco.000webhostapp.com/ldbapi/public/api/v1/usuariosEquipo/"+arg0[0]+"/"+arg0[1];

            String usuarios = handler.makeServiceCall(url2);
            listaUsuarios.clear();
            if(usuarios != null)
            {
                try {
                    JSONObject jsonObject = new JSONObject(usuarios);
                    JSONArray jsonUsuario = jsonObject.getJSONArray("usuarios");

                    for(int i = 0; i < usuarios.length();i++)
                    {
                        JSONObject usuario = jsonUsuario.getJSONObject(i);
                        listaUsuarios.add(usuario.getString("nick"));
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
    class GetEquipos extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            System.out.println("Se esta descargando los USUARIOS del JSON");
        }

        @Override
        protected Void doInBackground(String... arg0) {
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
            listaEquipos.clear();
            if(equipos != null)
            {
                try {
                    JSONObject jsonObject = new JSONObject(equipos);
                    JSONArray jsonEquipo = jsonObject.getJSONArray("equipos");

                    for(int i = 0; i < equipos.length();i++)
                    {
                        JSONObject equipo = jsonEquipo.getJSONObject(i);
                        listaEquipos.add(equipo.getString("nombreEquipo"));
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
    class GetCreadorEquipo extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... arg0) {
            HttpHandler handler = new HttpHandler();

            //IP CLASE
            //String url2 = "http://192.168.20.154/api/v1/creador/"+arg0[0];

            //IP CASA
            //String url2 = "http://192.168.1.109/api/v1/creador/"+arg0[0];

            //IP TRABAJO
            String url2 = "http://10.245.97.173/api/v1/creador/"+arg0[0];

            //HOSTING
            //String url2 = "http://losdeblanco.000webhostapp.com/ldbapi/public/api/v1/creador/"+arg0[0];

            String creador = handler.makeServiceCall(url2);
            if(creador != null)
            {
                nombreCreador = creador.trim().toLowerCase();
            }
            else{
                System.out.println("NO SE HA PODIDO ESTABLECER CONEXION CON LA URL");
            }
            return null;
        }
    }
    class ActualizarContrasena extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... arg) {
            HttpHandler handler = new HttpHandler();

            //IP CLASE
            //String url = "http://192.168.20.154/api/v1/actualizarPassEquipo/"+arg[0];

            //IP CASA
            //String url = "http://192.168.1.109/api/v1/actualizarPassEquipo/"+arg[0];

            //IP TRABAJO
            String url = "http://10.245.97.173/api/v1/actualizarPassEquipo/"+arg[0];

            //HOSTING
            //String url = "http://losdeblanco.000webhostapp.com/ldbapi/public/api/v1/actualizarPassEquipo/"+arg[0];

            //Creamos un JSON con los datos que nos pase el usuario
            final JSONObject data = new JSONObject();
            try {
                data.put("contrasenaEquipo",contrasenaEquipo);

                System.out.println("JSON DATA: "+data.toString()+"\n"+arg[0]);

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

    class ActualizarNombreEquipo extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... arg) {
            HttpHandler handler = new HttpHandler();

            //IP CLASE
            //String url = "http://192.168.20.154/api/v1/actualizarNombreEquipo/"+arg[0];

            //IP CASA
            //String url = "http://192.168.1.109/api/v1/actualizarNombreEquipo/"+arg[0];

            //IP TRABAJO
            String url = "http://10.245.97.173/api/v1/actualizarNombreEquipo/"+arg[0];

            //HOSTING
            //String url = "http://losdeblanco.000webhostapp.com/ldbapi/public/api/v1/actualizarNombreEquipo/"+arg[0];

            //Creamos un JSON con los datos que nos pase el usuario
            final JSONObject data = new JSONObject();
            try {
                data.put("nombreEquipoNuevo",nombreEquipoNuevo);

                System.out.println("JSON DATA: "+data.toString()+"\n"+arg[0]);

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
    class CambiarCreador extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... arg) {
            HttpHandler handler = new HttpHandler();

            //IP CLASE
            //String url = "http://192.168.20.154/api/v1/actualizarCreadorEquipo/"+arg[0];

            //IP CASA
            //String url = "http://192.168.1.109/api/v1/actualizarCreadorEquipo/"+arg[0];

            //IP TRABAJO
            String url = "http://10.245.97.173/api/v1/actualizarCreadorEquipo/"+arg[0];

            //HOSTING
            //String url = "http://losdeblanco.000webhostapp.com/ldbapi/public/api/v1/actualizarCreadorEquipo/"+arg[0];

            //Creamos un JSON con los datos que nos pase el usuario
            final JSONObject data = new JSONObject();
            try {
                data.put("nick",nickLiderNuevo);

                System.out.println("JSON DATA: "+data.toString()+"\n"+arg[0]);

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
    class EliminarMiembroEquipo extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... arg0) {
            HttpHandler handler = new HttpHandler();

            //IP CLASE
            //String url2 = "http://192.168.20.154/api/v1/eliminarMiembroEquipo/"+arg0[0]+"/"+arg0[1];

            //IP CASA
            //String url2 = "http://192.168.1.109//api/v1/eliminarMiembroEquipo/"+arg0[0]+"/"+arg0[1];

            //IP TRABAJO
            String url2 = "http://10.245.97.173/api/v1/eliminarMiembroEquipo/"+arg0[0]+"/"+arg0[1];

            //HOSTING
            //String url2 = "http://losdeblanco.000webhostapp.com/ldbapi/public/api/v1/eliminarMiembroEquipo/"+arg0[0]+"/"+arg0[1];

            System.out.println("SE VA A ELIMINAR");
            handler.eliminarDato(url2);

            return null;
        }

    }
    class EliminarMiembroParticipacion extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... arg0) {
            HttpHandler handler = new HttpHandler();

            //IP CLASE
            //String url2 = "http://192.168.20.154/api/v1/eliminarMiembroParticipacion/"+arg0[0]+"/"+arg0[1];

            //IP CASA
            //String url2 = "http://192.168.1.109//api/v1/eliminarMiembroParticipacion/"+arg0[0]+"/"+arg0[1];

            //IP TRABAJO
            String url2 = "http://10.245.97.173/api/v1/eliminarMiembroParticipacion/"+arg0[0]+"/"+arg0[1];

            //HOSTING
            //String url2 = "http://losdeblanco.000webhostapp.com/ldbapi/public/api/v1/eliminarMiembroParticipacion/"+arg0[0]+"/"+arg0[1];

            System.out.println("SE VA A ELIMINAR");
            handler.eliminarDato(url2);

            return null;
        }
    }
}
