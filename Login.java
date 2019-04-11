package com.example.hp.proyectoldb;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.concurrent.ExecutionException;

public class Login extends AppCompatActivity {

    final Context ctx = this;
    String usuario, nickRecu, contraseña, emailRecu, passBD;
    boolean coinciden;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final Button botonLogin = findViewById(R.id.botonLogin);
        final EditText editUsu = findViewById(R.id.loginUsu);
        final EditText editPass = findViewById(R.id.passUsu);
        final TextView txtRegistrar = findViewById(R.id.txtRegistro);
        final CheckBox cajaRecordar = findViewById(R.id.recordarLogin);
        final TextView txtRecuperarPass = findViewById(R.id.recuperarPassword);
        final SharedPreferences prefs = getSharedPreferences("Preferencias",Context.MODE_PRIVATE);

        //Si hay un nick en preferencias, lo cargamos
        editUsu.setText(prefs.getString("nick",""));

        botonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                botonLogin.setBackgroundColor(Color.parseColor("#08088A"));

                //Recogemos los datos que nos da el usuario
                usuario = editUsu.getText().toString();
                contraseña = editPass.getText().toString();

                //Comprobamos que la checkbox esté marcada para guardar el usuario en preferencias
                if(cajaRecordar.isChecked())
                {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("nick", usuario);
                    editor.commit();
                }

                //El Thread principal espera a que el AsyncTask termine
                try {
                    new GetUsuarios().execute().get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });
        txtRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ctx,Registrar.class);
                startActivity(intent);
            }
        });
        txtRecuperarPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builderAlerta = new AlertDialog.Builder(ctx);
                builderAlerta.setCancelable(true);
                builderAlerta.setTitle("Recuperacion de contraseña");
                builderAlerta.setMessage("¿Cual es tu email?");
                final EditText editEmail = new EditText(ctx);
                builderAlerta.setView(editEmail);
                builderAlerta.setPositiveButton("Aceptar",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                emailRecu = editEmail.getText().toString();

                                AlertDialog.Builder builderAlerta = new AlertDialog.Builder(ctx);
                                builderAlerta.setCancelable(true);
                                builderAlerta.setTitle("Recuperacion de contraseña");
                                builderAlerta.setMessage("¿Cual es tu nick?");
                                final EditText editNick = new EditText(ctx);
                                builderAlerta.setView(editNick);
                                builderAlerta.setPositiveButton("Aceptar",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                nickRecu = editNick.getText().toString();

                                                try {
                                                    new RecuperarContrasena().execute(nickRecu,emailRecu).get();
                                                } catch (ExecutionException e) {
                                                    e.printStackTrace();
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                }

                                                if(coinciden)
                                                {
                                                    try {
                                                        new EnviarMail().execute(emailRecu,passBD).get();
                                                    } catch (ExecutionException e) {
                                                        e.printStackTrace();
                                                    } catch (InterruptedException e) {
                                                        e.printStackTrace();
                                                    }

                                                    Toast.makeText(Login.this, "Email enviado, revisa la badeja de entrada y la de SPAM!!", Toast.LENGTH_LONG).show();
                                                }
                                                else if(!coinciden)
                                                {
                                                    Toast.makeText(Login.this, "El email o el nick es incorrecto", Toast.LENGTH_SHORT).show();
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
                builderAlerta.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                AlertDialog dialogAlerta = builderAlerta.create();
                dialogAlerta.show();
            }
        });
    }
    class EnviarMail extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... arg0) {
            HttpHandler handler = new HttpHandler();

            //IP CLASE
            //String url2 = "http://192.168.20.154/api/v1/enviarMail/"+arg0[0]+"/"+arg0[1];

            //IP CASA
            //String url2 = "http://192.168.1.109/api/v1/enviarMail/"+arg0[0]+"/"+arg0[1];

            //IP TRABAJO
            String url2 = "http://10.245.97.173/api/v1/enviarMail/"+arg0[0]+"/"+arg0[1];

            //HOSTING
            //String url2 = "http://losdeblanco.000webhostapp.com/ldbapi/public/api/v1/enviarMail/"+arg0[0]+"/"+arg0[1];

            handler.makeServiceCall(url2);

            return null;
        }
    }

    class RecuperarContrasena extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... arg0) {
            HttpHandler handler = new HttpHandler();

            if(arg0[0].equals(""))
            {
                arg0[0] = "q";
            }
            if(arg0[1].equals(""))
            {
                arg0[1] = "q";
            }

            //IP CLASE
            //String url2 = "http://192.168.20.154/api/v1/recuperarContrasena/"+arg0[0]+"/"+arg0[1];

            //IP CASA
            //String url2 = "http://192.168.1.109/api/v1/recuperarContrasena/"+arg0[0]+"/"+arg0[1];

            //IP TRABAJO
            String url2 = "http://10.245.97.173/api/v1/recuperarContrasena/"+arg0[0]+"/"+arg0[1];

            //HOSTING
            //String url2 = "http://losdeblanco.000webhostapp.com/ldbapi/public/api/v1/recuperarContrasena/"+arg0[0]+"/"+arg0[1];

            String pass = handler.makeServiceCall(url2);
            if(pass != null)
            {
                if(!pass.trim().equals("-1"))
                {
                    passBD = pass.trim();
                    coinciden = true;
                }
                else{coinciden = false;}
            }
            else{
                System.out.println("NO SE HA PODIDO ESTABLECER CONEXION CON LA URL");
            }
            return null;
        }
    }

    class GetUsuarios extends AsyncTask<Void, Void, Void> {

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
            //String url2 = "http://192.168.20.154/api/v1/login/"+usuario+"/"+contraseña;

            //IP CASA
            //String url2 = "http://192.168.1.109/api/v1/login/"+usuario+"/"+contraseña;

            //IP TRABAJO
            String url2 = "http://10.245.97.173/api/v1/login/"+usuario+"/"+contraseña;

            //HOSTING
            //ring url2 = "http://losdeblanco.000webhostapp.com/ldbapi/public/api/v1/login/"+usuario+"/"+contraseña;
            //Guardamos el valor del token
            token = handler.makeServiceCall(url2);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Intent intent = new Intent(ctx, Agenda.class);

            //Convertimos el token a entero, valor por defecto -1 para que no haga login
            int tokenValido = -1;
            try {
                tokenValido = NumberFormat.getInstance().parse(token).intValue();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            //Comprobamos que el token recibido no sea -1
            //INFO: valor de token --> -1(Login invalido) -10(Admin) cualquier otro(Login valido)
            if(tokenValido == -1)
            {
                Toast.makeText(ctx, "Usuario/contraseña Incorrecto", Toast.LENGTH_SHORT).show();
            }
            else{
                //Se lo pasamos a la siguiente actividad
                intent.putExtra("token",tokenValido);
                intent.putExtra("nick",usuario);
                startActivity(intent);
                contraseña="";

            }
        }
    }
}

