package com.example.noteapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class VerifyPhoneNumber extends AppCompatActivity {
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    Button verifyBtn, verifySkip;
    TextView resendBtn;
    String verificationId, userID;
    PhoneAuthCredential phoneAuthProvider;
    CountDownTimer countDownTimer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone_number);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        verifyBtn = findViewById(R.id.verifyBtn);
        resendBtn = findViewById(R.id.resend_otp);
        verifySkip = findViewById(R.id.verifySkip);
        verificationId = getIntent().getStringExtra("verificationId");


        EditText editTextOTP =findViewById(R.id.edtOTP);

        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = editTextOTP.getText().toString();
                if(code.isEmpty()){
                    Toast.makeText(VerifyPhoneNumber.this, "Please Enter Code", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (verificationId != null){
                    phoneAuthProvider = PhoneAuthProvider.getCredential(verificationId, code);
                    FirebaseAuth.getInstance().signInWithCredential(phoneAuthProvider).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                updateCheckedOTP();
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            }else {
                                Toast.makeText(VerifyPhoneNumber.this, "Invalid code", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        verifySkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(VerifyPhoneNumber.this, MainActivity.class));
                finish();
            }
        });
        resendBtn.setEnabled(false);
        resendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickVerifyPhoneNumber();
            }
        });
    }
    private void updateCheckedOTP(){
        Intent intent = getIntent();
        userID = intent.getStringExtra("userID");

        Log.d("userID_RECIEVE", userID);
        DocumentReference documentReference = fStore.collection("users").document(userID);
        documentReference.update("otpchecked" ,true).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.e("update", "updated");
            }
        });
    }
    public void onClickVerifyPhoneNumber(){
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+84" + getIntent().getStringExtra("phoneNumber"),
                120, TimeUnit.SECONDS,
                VerifyPhoneNumber.this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks(){

                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(VerifyPhoneNumber.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationIdResend, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(verificationIdResend, forceResendingToken);
                        Toast.makeText(VerifyPhoneNumber.this, "OTP send", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
                        super.onCodeAutoRetrievalTimeOut(s);
//                        startTimer();
                        Log.d("TIME", s.toString());
                    }
                }
        );
    }

//    private void startTimer(){
//        countDownTimer = new CountDownTimer(360000,1000){
//            @Override
//            public void onTick(long millisUntilFinished) {
//                resendBtn.setText("Resend OTP in " + String.valueOf(millisUntilFinished/1000));
//            }
//
//            @Override
//            public void onFinish() {
//                resendBtn.setText("Resend OTP");
//                resendBtn.setEnabled(true);
//            }
//        };
//        countDownTimer.start();
//    }
}