package com.example.hp.proyectoldb;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.Toolbar;;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class Agenda extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    Context ctx = this;
    ArrayList<String> datos =  new ArrayList<>();
    String nick,imagen_str, fotoString, nombreSZ = "";
    int token, idEvento, idUsu;
    SharedPreferences prefs;
    ImageView imagenPerfil;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agenda);

        //Preferencias
        prefs = getSharedPreferences("uri",Context.MODE_PRIVATE);

        //Creamos un bundle con todos los Extras
        Bundle bundle = getIntent().getExtras();
        token = bundle.getInt("token");
        nick = bundle.getString("nick");

        Button botonAñadirEvent = findViewById(R.id.botonAñadirEvent);
        botonAñadirEvent.setVisibility(View.INVISIBLE);
        final ListView listaEdiciones = findViewById(R.id.listView);
        Toolbar toolbar = findViewById(R.id.toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        setSupportActionBar(toolbar);

        //Añadimos el menú
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        //Cambiamos el txt de la cabecera
        View header = navigationView.getHeaderView(0);
        TextView txtCabecera = header.findViewById(R.id.headerNombre);
        TextView txtCerrarSesion = header.findViewById(R.id.headerCerrar);
        imagenPerfil = header.findViewById(R.id.imageViewCabecera);
        txtCabecera.setText(nick);

        //Recuperamos la foto que se haya guardado en preferencias

        try {
            new GetIdUsu().execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            new GetImagenPerfil().execute(idUsu).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(!fotoString.equals(""))
        {
            String base = fotoString;
            byte[] imageAsBytes = Base64.decode(base.getBytes(),Base64.DEFAULT);
            imagenPerfil.setImageBitmap(BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length));
        }

        //Al hacer click sobre la imagen, nos da opcion de cambiarla
        imagenPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cargarImagen();
            }

        });

        //Si hacemos click en cerrar sesion, volvemos al Login
        txtCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ctx, Login.class);
                startActivity(intent);
            }
        });

        //Recogemos todos los eventos de la base de datos
        try {
            new GetEventos().execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final ArrayAdapter<String> adaptador = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,datos);
        listaEdiciones.setAdapter(adaptador);

        //notificamos al adaptador cuando haya cambios en el listView
        adaptador.notifyDataSetChanged();

        listaEdiciones.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(ctx, Edicion.class);

                //Sacamos el nombre de la edicion para enviarselo a la clase edicion
                nombreSZ = "";
                String ediocionYFecha = parent.getItemAtPosition(position).toString();
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
                //Le pasamos datos a la nueva actividad
                intent.putExtra("nombreEdicion",nombreSZ);
                intent.putExtra("token",token);
                intent.putExtra("nick",nick);

                startActivity(intent);
            }
        });

        //Si el usuario es Admin, le mostramos el boton de añadir evento
        //y le permitimos borrar items mediante una pulsacion larga
        if(token == -10)
        {
            botonAñadirEvent.setVisibility(View.VISIBLE);

            listaEdiciones.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

                public boolean onItemLongClick(final AdapterView<?> parent, View v,
                                               final int index, long arg3) {

                    AlertDialog.Builder builderAlerta = new AlertDialog.Builder(ctx);
                    builderAlerta.setCancelable(true);
                    builderAlerta.setTitle("Eliminar Edicion");
                    builderAlerta.setMessage("¿Estas seguro de eliminar la edicion?");
                    builderAlerta.setPositiveButton("Aceptar",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    nombreSZ = "";
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

                                    //Eliminamos de la tabla participacion la/s fila/s con dicho idEvento
                                    //Eliminamos de la tabla evento la/s fila/s con el idEvento obtenido
                                    //Eliminamos de la tabla miembros la/s fila/s con el idEvento obtenido
                                    try {
                                        new EliminarParticipacion().execute(idEvento).get();
                                        new EliminarEvento().execute(idEvento).get();
                                        new EliminarEventoMiembro().execute(idEvento).get();
                                    } catch (ExecutionException e) {
                                        e.printStackTrace();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                    Toast.makeText(ctx, "Eliminada "+nombreSZ, Toast.LENGTH_SHORT).show();

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
        }

        botonAñadirEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ctx,CrearEdicion.class);
                //enviamos el token para que al volver a esta actividad podamos recibirlo de nuevo
                intent.putExtra("token",token);
                intent.putExtra("nick",nick);
                startActivity(intent);
                //finish();
            }
        });
    }
    public void cargarImagen(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/");
        startActivityForResult(intent.createChooser(intent,"Seleccione la aplicacion"),10);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            Uri path = data.getData();
            imagenPerfil.setImageURI(path);

            BitmapDrawable drawable = (BitmapDrawable)imagenPerfil.getDrawable();
            Bitmap bitmap = drawable.getBitmap();

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
            byte[] imagen = stream.toByteArray();

            imagen_str = Base64.encodeToString(imagen,0);

            try {
                new GetIdUsu().execute().get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                new CrearImagenPerfil().execute(idUsu).get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            /*SharedPreferences.Editor editor = prefs.edit();
            editor.putString("imagen", imagen_str);
            editor.commit();*/
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        //Aqui configuramos lo que hace al hacer click sobre los items
        //Del menu

        if (id == R.id.nav_agenda) {
            Intent intent = new Intent(this, Agenda.class);
            intent.putExtra("token",token);
            intent.putExtra("nick",nick);
            startActivity(intent);

        } else if (id == R.id.nav_tuseventos) {
            Intent intent = new Intent(this, TusEventos.class);
            intent.putExtra("token",token);
            intent.putExtra("nick",nick);
            startActivity(intent);

        }else if (id == R.id.nav_gestionEquipos) {
            Intent intent = new Intent(this, GestionEquipos.class);
            intent.putExtra("token",token);
            intent.putExtra("nick",nick);
            startActivity(intent);
        }else if (id == R.id.nav_manage) {
            Intent intent = new Intent(this, Contacto.class);
            intent.putExtra("token",token);
            intent.putExtra("nick",nick);
            startActivity(intent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
    class CrearImagenPerfil extends AsyncTask<Integer, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Integer... arg0) {
            HttpHandler handler = new HttpHandler();

            //IP CLASE
            //String url2 = "http://192.168.20.154/api/v1/actualizarImagenPerfil/"+arg0[0];

            //IP CASA
            //String url2 = "http://192.168.1.109/api/v1/actualizarImagenPerfil/"+arg0[0];

            //IP TRABAJO
            String url2 = "http://10.245.97.173/api/v1/actualizarImagenPerfil/"+arg0[0];

            //HOSTING
            //String url2 = "http://losdeblanco.000webhostapp.com/ldbapi/public/api/v1/actualizarImagenPerfil/"+arg0[0];

            final JSONObject data = new JSONObject();
            try {
                data.put("imagenPerfil",imagen_str);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            System.out.println("JSON A AÑADIR: "+data.toString());
            handler.crearPUT(url2,data.toString());

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
    class EliminarEvento extends AsyncTask<Integer, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Integer... arg0) {
            HttpHandler handler = new HttpHandler();

            //IP CLASE
            //String url2 = "http://192.168.20.154/api/v1/eliminarEvento/"+arg0[0];

            //IP CASA
            //String url2 = "http://192.168.1.109//api/v1/eliminarEvento/"+arg0[0];

            //IP TRABAJO
            String url2 = "http://10.245.97.173/api/v1/eliminarEvento/"+arg0[0];

            //HOSTING
            //String url2 = "http://losdeblanco.000webhostapp.com/ldbapi/public/api/v1/eliminarEvento/"+arg0[0];

            System.out.println("SE VA A ELIMINAR");
            handler.eliminarDato(url2);

            return null;
        }

    }
    class EliminarParticipacion extends AsyncTask<Integer, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Integer... arg0) {
            HttpHandler handler = new HttpHandler();

            //IP CLASE
            //String url2 = "http://192.168.20.154/api/v1/eliminarParticipacion/"+arg0[0];

            //IP CASA
            //String url2 = "http://192.168.1.109//api/v1/eliminarParticipacion/"+arg0[0];

            //IP TRABAJO
            String url2 = "http://10.245.97.173/api/v1/eliminarParticipacion/"+arg0[0];

            //HOSTING
            //String url2 = "http://losdeblanco.000webhostapp.com/ldbapi/public/api/v1/eliminarParticipacion/"+arg0[0];

            System.out.println("SE VA A ELIMINAR");
            handler.eliminarDato(url2);

            return null;
        }

    }
    class EliminarEventoMiembro extends AsyncTask<Integer, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Integer... arg0) {
            HttpHandler handler = new HttpHandler();

            //IP CLASE
            //String url2 = "http://192.168.20.154/api/v1/eliminarEventoMiembro"+arg0[0];

            //IP CASA
            //String url2 = "http://192.168.1.109//api/v1/eliminarEventoMiembro"+arg0[0];

            //IP TRABAJO
            String url2 = "http://10.245.97.173/api/v1/eliminarEventoMiembro"+arg0[0];

            //HOSTING
            //String url2 = "http://losdeblanco.000webhostapp.com/ldbapi/public/api/v1/eliminarEventoMiembro"+arg0[0];;

            System.out.println("SE VA A ELIMINAR");
            handler.eliminarDato(url2);

            return null;
        }

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

            //IP CLASE
            //String url2 = "http://192.168.20.154/api/v1/eventos";

            //IP CASA
            //String url2 = "http://192.168.1.109/api/v1/eventos";

            //IP TRABAJO
            String url2 = "http://10.245.97.173/api/v1/eventos";

            //HOSTING
            //String url2 = "http://losdeblanco.000webhostapp.com/ldbapi/public/api/v1/eventos";

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

                        datos.add(nombre+": "+fechaFinal);
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
    class GetImagenPerfil extends AsyncTask<Integer, Void, Void> {

        String token;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            System.out.println("Se esta descargando los USUARIOS del JSON");
        }

        @Override
        protected Void doInBackground(Integer... arg0) {
            HttpHandler handler = new HttpHandler();

            //IP CLASE
            //String url2 = "http://192.168.20.154/api/v1/imagenPerfil/"+arg0[0];

            //IP CASA
            //String url2 = "http://192.168.1.109/api/v1/imagenPerfil/"+arg0[0];

            //IP TRABAJO
            String url2 = "http://10.245.97.173/api/v1/imagenPerfil/"+arg0[0];

            //HOSTING
            //String url2 = "http://losdeblanco.000webhostapp.com/ldbapi/public/api/v1/imagenPerfil/"+arg0[0];

            String imagenes = handler.makeServiceCall(url2);
            if(imagenes != null)
            {
                try {
                    JSONObject jsonObject = new JSONObject(imagenes);
                    JSONArray imagen = jsonObject.getJSONArray("imagen");

                    for(int i = 0; i < imagen.length();i++)
                    {
                        JSONObject picture = imagen.getJSONObject(i);
                        String imagenPerfil = picture.getString("imagenPerfil");

                        fotoString = imagenPerfil;
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
