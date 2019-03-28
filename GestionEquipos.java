package com.example.hp.proyectoldb;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class GestionEquipos extends AppCompatActivity {
    Context ctx = this;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gestion_equipos);

        //Creamos un bundle con todos los Extras
        Bundle bundle = getIntent().getExtras();
        final int token = bundle.getInt("token");
        final String nick = bundle.getString("nick");

        Button botonCrearEquipo = findViewById(R.id.botonCrearEquipo);
        Button botonMisEquipos = findViewById(R.id.botonMisEquipos);

        botonCrearEquipo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ctx,CreacionEquipo.class);
                intent.putExtra("token",token);
                intent.putExtra("nick",nick);
                startActivity(intent);
            }
        });

        botonMisEquipos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        }
    }
