package com.example.hp.proyectoldb;

import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Api_Json extends AppCompatActivity {

    ArrayList<String> listaUsuarios = new ArrayList<>();


    public Api_Json()
    {
        new GetUsuarios().execute();
        System.out.println("TAMAÑO LISTA DESPUES DE EXECUTE: "+listaUsuarios.size());
    }

    public class GetUsuarios extends AsyncTask<Void, Void, Void>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            System.out.println("Se esta descargando los USUARIOS del JSON");
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler handler = new HttpHandler();
            String url = "http://192.168.1.109/api/v1/usuarios";
            System.out.println("Accediendo a la url");
            //Hacemos peticion a la url y recivimos respuesta
            String jsonStr = handler.makeServiceCall(url);
            System.out.println("URL CORRECTA: "+jsonStr);

            if(jsonStr != null)
            {
                try {
                    JSONObject jsonObject = new JSONObject(jsonStr);
                    JSONArray usuarios = jsonObject.getJSONArray("usuarios");
                    System.out.println("TAMAÑO LISTA: "+usuarios.length());
                    for(int i = 0; i < usuarios.length();i++)
                    {
                        JSONObject usuario = usuarios.getJSONObject(i);
                        String nombre = usuario.getString("nick");
                        System.out.println(nombre);
                        listaUsuarios.add(nombre);
                        System.out.println("TAMAÑO LISTA onPostExecute: "+listaUsuarios.size());

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

    public ArrayList<String> getListaUsuarios() {

        return listaUsuarios;
    }
}
