package ar.edu.unne.med.geomedv2;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import androidx.appcompat.app.AppCompatActivity;
import ar.edu.unne.med.geomedv2.modelo.SessionManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    //Inicializar variables
    EditText etEmail,etPassword;
    Button btLogin;
    SessionManager sessionManager;

    private RequestQueue mQueue;
    private ProgressDialog progressDialog;
    private static final String URL_WS = "https://simed.med.unne.edu.ar/api";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(getApplicationContext());
        mQueue = Volley.newRequestQueue(this);
        if(sessionManager.getLogin()){
            startActivity(new Intent(getApplicationContext(),InicioActivity.class));
            finish();
        }else{
            btLogin = findViewById(R.id.btn_login);
            btLogin.setOnClickListener(this);
            etEmail = findViewById(R.id.et_email);
            etPassword = findViewById(R.id.et_password);
            if(!sessionManager.getEmail().equals("")){
                etEmail.setText(sessionManager.getEmail());
                etEmail.setEnabled(false);
                etPassword.setFocusableInTouchMode(true);

            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                loadDialog();
                authenticate();
                break;
        }
    }

    private void loadDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Procesando datos...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
    }

    private void authenticate(){
        String url = URL_WS + "/login";
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    if(jsonResponse.getBoolean("status") == true){
                        sessionManager.setLogin(jsonResponse.getBoolean("status"));
                        sessionManager.setEmail(etEmail.getText().toString());
                        sessionManager.setToken(jsonResponse.getString("token"));
                        progressDialog.dismiss();
                        startActivity(new Intent(getApplicationContext(),InicioActivity.class));
                        finish();
                    }else{
                        //Toast.makeText(MainActivity.this, "NOOOOOOOO", Toast.LENGTH_SHORT).show();
                        Toast.makeText(MainActivity.this, "Credencial incorrecta", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }

                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, "Credencial incorrecta", Toast.LENGTH_SHORT).show();
                error.printStackTrace();
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("email",etEmail.getText().toString());
                params.put("password",etPassword.getText().toString());
                //params.put("random",code);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                return params;
            }
        };
        mQueue.add(request);
    }

}