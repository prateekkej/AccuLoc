package com.acculoc;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterForAmbulance extends AppCompatActivity {
private EditText email,password,name,numberplate,phone;
private Button register;
private AmbulanceSkeleton me;
private FirebaseAuth firebaseAuth;
private DatabaseReference databaseReference;
private FirebaseDatabase firebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_for_ambulance);
        firebaseAuth= FirebaseAuth.getInstance();
        firebaseDatabase=Firebase.getInstance();
        databaseReference=firebaseDatabase.getReference("ambulance");
initViews();

    }
    private void initViews(){
        email=findViewById(R.id.email_register);
        password=findViewById(R.id.password_register);
        name=findViewById(R.id.hospital_register);
        numberplate=findViewById(R.id.numberplate_register);
        phone=findViewById(R.id.phone_register);
        register=findViewById(R.id.signmeup);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               checkAndRegister();
            }
        });
    }
    void checkAndRegister(){
        boolean valid=true;
        final String emailstr,passstr,namestr,numberstr,phonestr;
        emailstr=email.getEditableText().toString();
        namestr=name.getEditableText().toString();
        passstr=password.getEditableText().toString();
        numberstr=numberplate.getEditableText().toString();
        phonestr=phone.getEditableText().toString();
        if(!emailstr.matches("(.*)@(.*)")){
            email.setError("Please Check");valid=false;
        }
        if(phonestr.length()>10){
            phone.setError("Please Check");valid=false;
        }
        if(passstr.length()<5 || passstr.length()>20){password.setError("Min-5, Max- 20");valid=false;}
        if(valid){
        me= new AmbulanceSkeleton();
        me.setName(namestr);
        me.setPhone(phonestr);
        me.setRegistrationNumber(numberstr);
            final ProgressDialog progressDialog=new ProgressDialog(RegisterForAmbulance.this);
            progressDialog.setMessage("Signing up");
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();
        firebaseAuth.createUserWithEmailAndPassword(emailstr,passstr).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                me.setUid(firebaseAuth.getUid());
                me.email=emailstr;
           databaseReference.child(firebaseAuth.getUid()).setValue(me).addOnSuccessListener(new OnSuccessListener<Void>() {
               @Override
               public void onSuccess(Void aVoid) {
                   progressDialog.dismiss();
                   startActivity(new Intent(RegisterForAmbulance.this,AmbulanceHome.class));
                   finish();
               }
           });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Snackbar.make(findViewById(R.id.scroll),"Error:"+e.getMessage(),Snackbar.LENGTH_SHORT).show();
                Log.v("Error",e.toString());
                progressDialog.dismiss();
            }
        });
        }

    }
}
