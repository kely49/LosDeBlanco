package com.example.hp.proyectoldb;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

public class CrearEdicion extends AppCompatActivity {
    Context ctx = this;
    ArrayList<String> listaEventos = new ArrayList<>();
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crear_edicion);

        final EditText editEdicion = findViewById(R.id.editNombreCrearEdicion);
        final EditText editFecha = findViewById(R.id.editFechaCrearEdicion);
        final EditText editProvincia = findViewById(R.id.provinciaCrearEdicion);
        final EditText editLocalidad = findViewById(R.id.localidadCrearEdicion);
        Button botonAnadirEvento = findViewById(R.id.botonCrearEdicion);

        //Cargamos todos los Extra
        Bundle bundle = getIntent().getExtras();
        final int token = bundle.getInt("token");
        final String nick = bundle.getString("nick");

        final Calendar myCalendar = Calendar.getInstance();

        //Recogemos la fecha elegida por el usuario en el calendario y se la metemos al editText de fecha
        final DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                String myFormat = "yyyy-MM-dd";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat);
                editFecha.setText(sdf.format(myCalendar.getTime()));
            }

        };
        //Al hacer click sobre el editText de fecha, se nos abre un calendario
        editFecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(ctx, datePickerListener, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();


            }
        });

        botonAnadirEvento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nombreEvento = editEdicion.getText().toString();
                String provincia = editProvincia.getText().toString();
                String localidad = editLocalidad.getText().toString();
                String fechaEvento= editFecha.getText().toString();

                //Hacemos comprobaciones de que no nos meta nada en blanco a la base de datos
                if(editEdicion.getText().toString().equals(""))
                {
                    Toast.makeText(ctx, "El nombre de la edicion no puede estar vacio", Toast.LENGTH_SHORT).show();
                }
                else if(editFecha.getText().toString().equals(""))
                {
                    Toast.makeText(ctx, "La fecha de la edicion no puede estar vacia", Toast.LENGTH_SHORT).show();
                }
                else if(editLocalidad.getText().toString().equals(""))
                {
                    Toast.makeText(ctx, "La localidad de la edicion no puede estar vacia", Toast.LENGTH_SHORT).show();
                }
                else if(editProvincia.getText().toString().equals(""))
                {
                    Toast.makeText(ctx, "La provincia de la edicion no puede estar vacia", Toast.LENGTH_SHORT).show();
                }
                else{
                    Intent intent = new Intent(ctx,Agenda.class);
                    //Volvemos a la agenda pasandole el Token y el nick, para no perderlo de
                    //el menu lateral al volver
                    intent.putExtra("token",token);
                    intent.putExtra("nick",nick);
                    try {
                        new RegistrarEventos().execute(nombreEvento,provincia,localidad,fechaEvento).get();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    startActivity(intent);
                    finish();
                }
            }
        });
    }
    class RegistrarEventos extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... arg) {
            HttpHandler handler = new HttpHandler();

            //Recogemos los datos que se pasan al AsyncTask
            String nombreEvento =arg[0];
            String provincia=arg[1];
            String localidad=arg[2];
            String fechaEvento=arg[3];

            //IP CLASE
            //String url = "http://192.168.20.154/api/v1/crearEvento";

            //IP CASA
            //String url = "http://192.168.1.109/api/v1/crearEvento";

            //IP TRABAJO
            String url = "http://16.19.142.155/api/v1/crearEvento";

            //HOSTING
            //String url = "http://losdeblanco.000webhostapp.com/ldbapi/public/api/v1/crearEvento";

            //Creamos un JSON con los datos que nos pase el usuario
            final JSONObject data = new JSONObject();
            try {
                data.put("nombreEvento",nombreEvento);
                data.put("provincia",provincia);
                data.put("localidad",localidad);
                data.put("fechaEvento",fechaEvento);

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
}
