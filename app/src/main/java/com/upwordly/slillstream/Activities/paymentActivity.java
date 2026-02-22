package com.upwordly.slillstream.Activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.upwordly.slillstream.R;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class paymentActivity extends AppCompatActivity {

    private TextView tvPhoneNumber, price;
    private ImageView btnCopy;
    private EditText etTrxId;
    private Button btnConfirm;
    FirebaseFirestore db;
    FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        int coursePrice = getIntent().getIntExtra("course_price",0);
        int courseID = getIntent().getIntExtra("courseID",0);


        tvPhoneNumber = findViewById(R.id.tvPhoneNumber);
        btnCopy = findViewById(R.id.btnCopy);
        etTrxId = findViewById(R.id.etTrxId);
        btnConfirm = findViewById(R.id.btnConfirm);
        price = findViewById(R.id.price);
        price.setText("BDT : " + coursePrice);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();




        btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String number = tvPhoneNumber.getText().toString();
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Copied Number", number);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(paymentActivity.this, "নাম্বার কপি হয়েছে!", Toast.LENGTH_SHORT).show();

            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String trxId = etTrxId.getText().toString().trim();

                if (trxId.isEmpty()) {
                    etTrxId.setError("TrxID অবশ্যই পূরণ করতে হবে!");

                } else {
                   paymentVerified(trxId,courseID);
                   etTrxId.setText("");
                    Toast.makeText(paymentActivity.this, "Your Payment is Recorded, as soon as possible it will be verified", Toast.LENGTH_LONG).show();
                    new android.os.Handler().postDelayed(() -> {
                        finish();
                    }, 3000);}
            }
        });
    }

    void paymentVerified(String trxId, int CourseID) {
        String uid = auth.getUid();

        // প্রথমে চেক করছি এই trxId অলরেডি ডাটাবেজে আছে কি না
        db.collection("payment").document(trxId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();

                        if (document.exists()) {
                            // যদি trxId আগে থেকেই থাকে
                            Toast.makeText(paymentActivity.this, "এই Transaction ID টি ইতিমধ্যে ব্যবহার করা হয়েছে!", Toast.LENGTH_LONG).show();
                        } else {
                            // যদি trxId নতুন হয়, তবেই ডাটা সেভ হবে
                            savePaymentData(trxId, uid, CourseID);
                        }
                    } else {
                        Log.e("Firestore", "Error checking trxId: ", task.getException());
                    }
                });
    }

    // ডাটা সেভ করার জন্য আলাদা মেথড
    private void savePaymentData(String trxId, String uid, int CourseID) {
        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("trxId", trxId);
        paymentData.put("uid", uid);
        paymentData.put("status", "processing");
        paymentData.put("courseID", CourseID);
        paymentData.put("timestamp", FieldValue.serverTimestamp());

        db.collection("payment")
                .document(trxId)
                .set(paymentData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(paymentActivity.this, "পেমেন্ট রিকোয়েস্ট সফল হয়েছে।", Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error: " + e.getMessage());
                });
    }






}
