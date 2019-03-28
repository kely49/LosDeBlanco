package com.example.hp.proyectoldb;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;

public class CreacionEquipo extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.creacion_equipos);

        //Creamos un bundle con todos los Extras
        Bundle bundle = getIntent().getExtras();
        final int token = bundle.getInt("token");
        final String nick = bundle.getString("nick");

        Spinner spinner = findViewById(R.id.spinnerCreacionEquipos);

        ArrayList<String> listaEventos = new ArrayList<String>();
        //TODO: Añadir eventos al ArrayList


        ArrayAdapter<String> adaptador = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, listaEventos);
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adaptador);


        }
    }
