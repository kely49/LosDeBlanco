package com.example.hp.proyectoldb;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class Contacto extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contacto);

        //Creamos un bundle con todos los Extras
        Bundle bundle = getIntent().getExtras();
        final int token = bundle.getInt("token");
        final String nick = bundle.getString("nick");

        final EditText editNombre = findViewById(R.id.txtContactoNombre);
        final EditText editAsunto = findViewById(R.id.txtContactoAsunto);
        final EditText editMensaje = findViewById(R.id.txtContactoMensaje);
        Button botonEnviarMensaje = findViewById(R.id.botonEnviarMensaje);

        botonEnviarMensaje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent itSend = new Intent(android.content.Intent.ACTION_SEND);
                // vamos a enviar texto plano
                itSend.setType("plain/text");
                // colocamos los datos para el envío
                itSend.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{ "losdeblanco.contacto@gmail.com"});
                itSend.putExtra(android.content.Intent.EXTRA_SUBJECT, editAsunto.getText().toString());
                itSend.putExtra(android.content.Intent.EXTRA_TEXT, "[Mensaje enviado por: "+editNombre.getText().toString()
                        +", nick: "+nick+"]\n\n"+editMensaje.getText().toString());
                startActivity(itSend);

                editNombre.setText("");
                editMensaje.setText("");
                editAsunto.setText("");
            }
        });
    }
}
