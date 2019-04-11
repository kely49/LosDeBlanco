package com.example.hp.proyectoldb;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class EdicionPendiente extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edicion_pendiente);

        //Recogemos lo que haya en Extra
        Bundle bundle = getIntent().getExtras();
        String nombre = bundle.getString("nombreEdicion");
        String fecha = bundle.getString("fecha");

        TextView txtPendiente = findViewById(R.id.txtPendiente);
        TextView txtFechaPendiente = findViewById(R.id.txtFechaPendiente);

        txtPendiente.setText(nombre);
        txtFechaPendiente.setText(fecha);
    }
}
