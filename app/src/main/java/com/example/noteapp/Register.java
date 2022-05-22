package com.example.noteapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.intellij.lang.annotations.Pattern;

import java.util.concurrent.TimeUnit;

public class Register extends AppCompatActivity {

    public static boolean checkEnterOTP;
    public static boolean CHECK_EXIST = false;


    EditText registerName, registerEmail, registerPassword, registerConfirmPassword, registerPhoneNumber;
    Button btnRegister;
    ProgressBar progressBar;
    TextView txt_registerLogin;
    FirebaseAuth fireAuth;

    ConstraintLayout mLayoutRegister;
    String userID, userName, userEmail, userPhoneNumber;
    FirebaseFirestore fStore;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initUi();
        initListener();
    }
    //ok
    private void initUi(){
        mLayoutRegister = findViewById(R.id.layoutRegister);
        registerName = findViewById(R.id.registerName);
        registerEmail = findViewById(R.id.registerEmail);
        registerPassword = findViewById(R.id.registerPassword);
        registerConfirmPassword = findViewById(R.id.registerPassword2);
        registerPhoneNumber = findViewById(R.id.registerPhoneNumber);
        btnRegister = findViewById(R.id.registerBtnRegister);
        progressBar = findViewById(R.id.progress_bar);
        txt_registerLogin = findViewById(R.id.txt_registerLogin);
        fireAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
    }

    private void initListener() {
        //        Switch to Login
        txt_registerLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Login.class));
                finish();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                  Extract the data from the form
                userName = registerName.getText().toString().trim();
                userEmail = registerEmail.getText().toString().trim();
                userPhoneNumber = registerPhoneNumber.getText().toString().trim();
                checkEnterOTP = false;

                String password = registerPassword.getText().toString().trim();
                String confPass = registerConfirmPassword.getText().toString().trim();
                if(userName.isEmpty()){
                    setErr(registerName,"Name is required");
                    return;
                }

                if(userEmail.isEmpty()){
                    setErr(registerEmail,"Email is required");
                    return;
                }
                if(!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()){
                    setErr(registerEmail, "Email  invalid");
                    return;
                }
//                if (checkPhone_Email_Exist()){
////                    setErr(registerEmail, "Email already exists");
//                    setErr(registerPhoneNumber, "Phone number was used");
//                    return;
//                }
                if(userPhoneNumber.isEmpty()){
                    setErr(registerPhoneNumber, "Phone number is required");
                    return;
                }
                if(password.isEmpty()){
                    setErr(registerPassword, "Password is required");
                    return;
                }

                if(confPass.isEmpty()){
                    setErr(registerPassword, "Confirm password is required");
                    return;
                }

                if(!password.equals(confPass)){
                    setErr(registerConfirmPassword, "Password is invalid");
                    return;
                }
                Log.d("CHECK_EXIST", CHECK_EXIST + "");
                //xac thuc otp
//              Data is validated

                fireAuth.createUserWithEmailAndPassword(userEmail,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        addUserToData();
                        onClickVerifyPhoneNumber();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Register.this, "The email address is already in use by another account", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    public void onClickVerifyPhoneNumber(){
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+84" + userPhoneNumber,
                120, TimeUnit.SECONDS,
                Register.this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks(){

                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(Register.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(verificationId, forceResendingToken);
                        progressBar.setVisibility(View.VISIBLE);
                        btnRegister.setVisibility(View.INVISIBLE);
                        Intent intent = new Intent(Register.this, VerifyPhoneNumber.class);
                        intent.putExtra("phoneNumber", userPhoneNumber);
                        intent.putExtra("verificationId", verificationId);
                        intent.putExtra("userID",FirebaseAuth.getInstance().getCurrentUser().getUid());

                        Log.d("onCodeSent", "onCodeSent:" + verificationId);
                        Log.d("userID", "userID " + FirebaseAuth.getInstance().getCurrentUser().getUid());
                        startActivity(intent);
                    }

                    @Override
                    public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
                        super.onCodeAutoRetrievalTimeOut(s);
                    }
                }
        );
    }

    private void setErr(EditText edt, String warn){
        edt.setError(warn);
    }

    private void addUserToData(){
        User user = new User(userName, userEmail, userPhoneNumber, checkEnterOTP);
        userID = fireAuth.getCurrentUser().getUid();

        fStore.collection("users")
                .document(userID)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("add", "DocumentSnapshot written with ID: " + userID);
                    }
                });
    }

    private boolean checkPhone_Email_Exist(){

        CollectionReference userRef = fStore.collection("users");
        Query queryMail_Phone = userRef.whereEqualTo("phoneNumber", userPhoneNumber);
        queryMail_Phone.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot document : task.getResult()){
                        if (document.exists()){
                            Log.d("CHECK_EXIST", "Name and Email already exists");
                            CHECK_EXIST = true ;
                        } else {
                            CHECK_EXIST = false;
                        }
                    }
                } else {
                    Log.d("TAG", "error get data:" + task.getException());
                }
            }
        });
        return CHECK_EXIST;
    }


}