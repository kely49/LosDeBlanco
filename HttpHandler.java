package com.example.hp.proyectoldb;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class HttpHandler {

    public HttpHandler()
    {

    }

    public String makeServiceCall(String reqUrl){
        String response = "";
        try {
            URL url = new URL(reqUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            //Leer la respuesta
            InputStream in = new BufferedInputStream(conn.getInputStream());
            response = convertStreamToString(in);

            in.close();
            conn.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    public void crearPOST(String reqUrl,String json){
        try {
            URL url = new URL(reqUrl);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.connect();

            //Mandamos los datos
            OutputStream os = new BufferedOutputStream(conn.getOutputStream());
            os.write(json.getBytes());

            //Limpiamos
            os.flush();

            //Escribimos la respuesta
            InputStream is = conn.getInputStream();

            is.close();
            os.close();
            conn.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void crearPUT(String reqUrl,String json){
        try {
            URL url = new URL(reqUrl);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("PUT");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.connect();

            //Mandamos los datos
            OutputStream os = new BufferedOutputStream(conn.getOutputStream());
            os.write(json.getBytes());

            //Limpiamos
            os.flush();

            //Escribimos la respuesta
            InputStream is = conn.getInputStream();

            is.close();
            os.close();
            conn.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void eliminarDato(String reqUrl) {
        try {
            URL url = new URL(reqUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "utf8_spanish_ci");
            conn.connect();

            //Enviamos la peticion
            InputStream is = conn.getInputStream();

            is.close();
            conn.disconnect();

        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String convertStreamToString(InputStream is){
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
