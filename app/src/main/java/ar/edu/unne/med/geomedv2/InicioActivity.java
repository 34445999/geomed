package ar.edu.unne.med.geomedv2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import ar.edu.unne.med.geomedv2.modelo.SessionManager;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class InicioActivity extends AppCompatActivity {
    private RequestQueue mQueue;
    private ProgressDialog progressDialog;
    private String geoubicacion;
    private String geodireccion;
    private String geoestado;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private static final String URL_WS = "https://simed.med.unne.edu.ar/api";

    SessionManager sessionManager;
    TextView ubicacion, horario, estado, marcar_leyenda;
    CardView marcar;
    ImageView marcar_icono;
    Button covid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        if( ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){

            }else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }

        sessionManager = new SessionManager(getApplicationContext());
        mQueue = Volley.newRequestQueue(this);

        covid = findViewById(R.id.covid);
        marcar = findViewById(R.id.marcar);
        marcar_icono = findViewById(R.id.marcar_icono);
        marcar_leyenda = findViewById(R.id.marcar_leyenda);

        ubicacion = findViewById(R.id.ubicacion);
        horario = findViewById(R.id.horario);
        estado = findViewById(R.id.estado);

        ubicacion.setText("Ubicación: \n" + sessionManager.getUbicacion());
        estado.setText("Estado: \n" + sessionManager.getEstado());
        horario.setText("Hora: \n" + sessionManager.getHora());

        cambiar_boton();

        //final LocationListener locationListener = new LocationListener() {
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //Log.d("Location Changes", location.toString());
                if (location.getLatitude() != 0.0 && location.getLongitude() != 0.0) {
                    geoubicacion = location.getLatitude() + "" + location.getLongitude();
                    geodireccion = "No registrado";
                    storeUbicacion();
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d("Status Changed", String.valueOf(status));
                switch (status) {
                    case LocationProvider.AVAILABLE:
                        Log.d("debug", "LocationProvider.AVAILABLE");
                        break;
                    case LocationProvider.OUT_OF_SERVICE:
                        Log.d("debug", "LocationProvider.OUT_OF_SERVICE");
                        break;
                    case LocationProvider.TEMPORARILY_UNAVAILABLE:
                        Log.d("debug", "LocationProvider.TEMPORARILY_UNAVAILABLE");
                        break;
                }
            }

            @Override
            public void onProviderEnabled(String provider) {
                Toast.makeText(InicioActivity.this, "GPS Activado", Toast.LENGTH_SHORT).show();
                Log.d("Provider Enabled", provider);
            }

            @Override
            public void onProviderDisabled(String provider) {
                Toast.makeText(InicioActivity.this, "GPS Desactivado", Toast.LENGTH_SHORT).show();
                Log.d("Provider Disabled", provider);
            }
        };

        final Looper looper = null;
        //final LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        final Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(true);
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);

        marcar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mensaje;
                if(sessionManager.getEstado().equals("Salida")){
                    mensaje = "Se registrará la hora y ubicación de la entrada?";
                }else{
                    mensaje = "Se registrará la hora y ubicación de la salida?";
                }
                AlertDialog.Builder b = new AlertDialog.Builder(InicioActivity.this);
                b.setMessage(mensaje)
                        .setCancelable(true)
                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(sessionManager.getEstado().equals("Salida")){
                                    geoestado = "Entrada";
                                }
                                if(sessionManager.getEstado().equals("No especificado")){
                                    geoestado = "Entrada";
                                }
                                if(sessionManager.getEstado().equals("Entrada")){
                                    geoestado = "Salida";
                                }
                                if(sessionManager.getEstado().equals("Intermedia")){
                                    geoestado = "Salida";
                                }
                                final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                                if (!gpsEnabled) {
                                    Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    startActivity(settingsIntent);
                                }
                                if (ActivityCompat.checkSelfPermission(InicioActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(InicioActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(InicioActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
                                    return;
                                }

                                loadDialog();

                                locationManager.requestSingleUpdate(criteria, locationListener, looper);
                                //locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, looper);

                            }
                        })
                        .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        })
                        .show();
            }
        });

        covid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(InicioActivity.this, "COVID", Toast.LENGTH_SHORT).show();
                AlertDialog.Builder b = new AlertDialog.Builder(InicioActivity.this);
                b.setMessage("Confirma que se contagio de COVID ?")
                        .setCancelable(true)
                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Toast.makeText(InicioActivity.this, "COVIDD", Toast.LENGTH_SHORT).show();
                                loadDialog();
                                storeCovid();
                            }
                        })
                        .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        })
                        .show();
            }
        });
    }

    @Override
    public void onStart() {
        //el sistema llama a onStart () tanto cuando crea su actividad como cuando reinicia la actividad desde el estado detenido.
        super.onStart();
        loadDialog();
        check();
    }

    @Override
    protected void onStop() {
        super.onStop();
        locationManager.removeUpdates(locationListener);
    }

    private void cambiar_boton() {
        if(!sessionManager.getEstado().equals("Salida")){
            if(sessionManager.getEstado().equals("No especificado")){
                marcar.setBackgroundColor(Color.parseColor("#1B5E20"));
                marcar_icono.setImageResource(R.drawable.ic_location);
                marcar_leyenda.setText("Marcar Entrada");
            }else{
                marcar.setBackgroundColor(Color.parseColor("#B71C1C"));
                marcar_icono.setImageResource(R.drawable.ic_location_verde);
                marcar_leyenda.setText("Marcar Salida");
            }
        }
        if(sessionManager.getEstado().equals("Salida")){
            marcar.setBackgroundColor(Color.parseColor("#1B5E20"));
            marcar_icono.setImageResource(R.drawable.ic_location);
            marcar_leyenda.setText("Marcar Entrada");
        }
    }

    private void loadDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Cargando datos...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
    }

    private void storeCovid(){
        String url = URL_WS + "/covid/store";
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    progressDialog.dismiss();
                    if(jsonResponse.getBoolean("status") == false){
                        Toast.makeText(InicioActivity.this, "Notifique su constagio nuevamente", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(InicioActivity.this, "Notificación exitosa", Toast.LENGTH_SHORT).show();
                    }
                }catch (JSONException e){
                    progressDialog.dismiss();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(InicioActivity.this, "Ocurrio un error", Toast.LENGTH_SHORT).show();
                //error.printStackTrace();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                params.put("Authorization","Bearer " + sessionManager.getToken());
                return params;
            }
        };
        mQueue.add(request);
    }

    private void storeUbicacion(){
        Log.d("storeUbicacion", "Ejecuto");
        String url = URL_WS + "/ubicacion/store";
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    //Toast.makeText(InicioActivity.this, jsonResponse.toString(), Toast.LENGTH_SHORT).show();
                    //Log.d("estado json", jsonResponse.getString("estado"));
                    if(jsonResponse.getBoolean("status") == true){
                        sessionManager.setUbicacion(geoubicacion);
                        sessionManager.setDireccion(geodireccion);
                        sessionManager.setEstado(jsonResponse.getString("estado"));
                        sessionManager.setHora(jsonResponse.getString("horario"));
                        ubicacion.setText("Ubicación: \n" + sessionManager.getUbicacion());
                        estado.setText("Estado: \n" + sessionManager.getEstado());
                        horario.setText("Hora: \n" + sessionManager.getHora());
                        cambiar_boton();
                        if(sessionManager.getEstado().equals("Entrada")){
                            geoestado = "Intermedia";
                        }
                        Log.d("storeUbicacion", geoestado);
                        Log.d("storeUbicacion", sessionManager.getEstado());
                        progressDialog.dismiss();
                    }
                    progressDialog.dismiss();
                }catch (JSONException e){
                    progressDialog.dismiss();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(InicioActivity.this, "Ocurrio un error", Toast.LENGTH_SHORT).show();
                error.printStackTrace();
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                //Log.d("estado sesion", sessionManager.getEstado());
                String p_ubicacion = geoubicacion;
                String p_direccion = geodireccion;
                String p_estado = geoestado;

                Map<String,String> params = new HashMap<String, String>();
                params.put("ubicacion", p_ubicacion);
                params.put("direccion", p_direccion);
                params.put("estado", p_estado);
                //params.put("random",code);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                params.put("Authorization","Bearer " + sessionManager.getToken());
                return params;
            }
        };
        mQueue.add(request);
    }

    private void check(){
        String url = URL_WS + "/check";
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    //Toast.makeText(InicioActivity.this, jsonResponse.toString(), Toast.LENGTH_SHORT).show();
                    //Log.d("augusto", sessionManager.getToken());
                    if(jsonResponse.getBoolean("status") == false){
                        sessionManager.setLogin(jsonResponse.getBoolean("status"));
                        sessionManager.setToken("");
                        progressDialog.dismiss();
                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        finish();
                    }
                    progressDialog.dismiss();
                }catch (JSONException e){
                    progressDialog.dismiss();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(InicioActivity.this, "Ocurrio un error", Toast.LENGTH_SHORT).show();
                //error.printStackTrace();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                params.put("Authorization","Bearer " + sessionManager.getToken());
                return params;
            }
        };
        mQueue.add(request);
    }
}