package ar.edu.unne.med.geomedv2.modelo;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public SessionManager(Context context){
        sharedPreferences = context.getSharedPreferences("AppKey",0);
        editor = sharedPreferences.edit();
        editor.apply();
    }

    public void setLogin(boolean login){
        editor.putBoolean("KEY_LOGIN",login);
        editor.commit();
    }

    public boolean getLogin(){
        return sharedPreferences.getBoolean("KEY_LOGIN",false);
    }

    public void setEmail(String email){
        editor.putString("KEY_EMAIL",email);
        editor.commit();
    }

    public String getEmail(){
        return sharedPreferences.getString("KEY_EMAIL","");
    }

    public void setToken(String token){
        editor.putString("KEY_TOKEN",token);
        editor.commit();
    }

    public String getToken(){
        return sharedPreferences.getString("KEY_TOKEN","");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    public void setUbicacion(String ubicacion){
        editor.putString("UBICACION",ubicacion);
        editor.commit();
    }

    public String getUbicacion(){
        return sharedPreferences.getString("UBICACION","No especificado");
    }

    public void setDireccion(String direccion){
        editor.putString("DIRECCION",direccion);
        editor.commit();
    }

    public String getDireccion(){
        return sharedPreferences.getString("DIRECCION","No especificado");
    }

    public void setEstado(String estado){
        editor.putString("ESTADO",estado);
        editor.commit();
    }

    public String getEstado(){
        return sharedPreferences.getString("ESTADO","No especificado");
    }

    public void setHora(String hora){
        editor.putString("HORA",hora);
        editor.commit();
    }

    public String getHora(){
        return sharedPreferences.getString("HORA","No especificado");
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void logout(){
        editor.putString("KEY_TOKEN","");
        editor.putBoolean("KEY_LOGIN",false);
        editor.commit();
    }
}
