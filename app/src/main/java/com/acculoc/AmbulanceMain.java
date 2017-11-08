package com.acculoc;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class AmbulanceMain extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    EditText email,password;
    Button logmein,signup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ambulance_main);
        initViews();
firebaseAuth=FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(firebaseAuth.getCurrentUser()!=null){
            startActivity(new Intent(AmbulanceMain.this,AmbulanceHome.class));
            finish();
        }
    }

    private void initViews(){
        email=findViewById(R.id.email);
        password=findViewById(R.id.password);
        logmein=findViewById(R.id.login);
        signup=findViewById(R.id.signup);
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AmbulanceMain.this,RegisterForAmbulance.class));
                finish();
            }
        });
        logmein.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailstr,passstr;
                emailstr=email.getEditableText().toString();
                passstr=password.getEditableText().toString();
                if(!emailstr.isEmpty()&& !passstr.isEmpty()){
                    final ProgressDialog progressDialog=new ProgressDialog(AmbulanceMain.this);
                    progressDialog.setMessage("Signing in");
                    progressDialog.setCancelable(false);
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.show();
                firebaseAuth.signInWithEmailAndPassword(emailstr,passstr).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        startActivity(new Intent(AmbulanceMain.this,AmbulanceHome.class));
                        progressDialog.dismiss();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(findViewById(R.id.ambuMain),"Error:"+e.getLocalizedMessage(),Snackbar.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });}

            }
        });

    }
}
