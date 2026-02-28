package com.upwordly.slillstream.Activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.upwordly.slillstream.R;

import java.util.HashMap;
import java.util.Map;

public class paymentActivity extends AppCompatActivity {

    private TextView tvPhoneNumber, tvPrice;
    private ImageView btnCopy;
    private EditText etTrxId;
    private Button btnConfirm;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private MaterialCardView methodBkash, methodNagad, methodBank;

    private String selectedMethodType = "";
    private String bkashNumber = "";
    private String nagadNumber = "";
    private String bankNumber = "";

    String name = "";
    String gender = "";
    String dob = "";


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

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        int coursePrice = getIntent().getIntExtra("course_price", 0);
        int courseID = getIntent().getIntExtra("courseID", 0);

        initViews();
        LoadDialog();

        tvPrice.setText("BDT : " + coursePrice);

        // Load account numbers from Firestore
        loadPaymentSettings();

        // Copy Button Click
        btnCopy.setOnClickListener(v -> {
            String number = tvPhoneNumber.getText().toString();
            if (!number.isEmpty()) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Copied Number", number);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "নাম্বার কপি হয়েছে!", Toast.LENGTH_SHORT).show();
            }
        });

        // Confirm Button Click
        btnConfirm.setOnClickListener(v -> {
            String trxId = etTrxId.getText().toString().trim();

            if (name.isEmpty()){
                Toast.makeText(this, "আগে পূরণ করুন", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedMethodType.isEmpty()) {
                Toast.makeText(this, "আগে একটি পেমেন্ট মেথড সিলেক্ট করুন", Toast.LENGTH_SHORT).show();
                return;
            }

            if (trxId.isEmpty()) {
                etTrxId.setError("Transaction ID অবশ্যই দিতে হবে!");
                return;
            }

            verifyAndSubmitPayment(trxId, courseID);
        });

        setupMethodSelection();
    }

    private void initViews() {
        tvPhoneNumber = findViewById(R.id.tvPhoneNumber);
        btnCopy = findViewById(R.id.btnCopy);
        etTrxId = findViewById(R.id.etTrxId);
        btnConfirm = findViewById(R.id.btnConfirm);
        tvPrice = findViewById(R.id.price);
        methodBkash = findViewById(R.id.methodBkash);
        methodNagad = findViewById(R.id.methodNagad);
        methodBank = findViewById(R.id.methodBank);
    }

    private void setupMethodSelection() {
        MaterialCardView[] cards = {methodBkash, methodNagad, methodBank};

        for (MaterialCardView card : cards) {
            card.setOnClickListener(v -> {
                // Reset all cards UI
                for (MaterialCardView c : cards) {
                    c.setStrokeWidth(0);
                    LinearLayout layout = (LinearLayout) c.getChildAt(0);
                    TextView tv = (TextView) layout.getChildAt(1);
                    tv.setTypeface(null, Typeface.NORMAL);
                }

                // Highlight selected card
                card.setStrokeWidth(dpToPx(2));
                card.setStrokeColor(Color.parseColor("#D12082"));
                LinearLayout selectedLayout = (LinearLayout) card.getChildAt(0);
                TextView selectedTv = (TextView) selectedLayout.getChildAt(1);
                selectedTv.setTypeface(null, Typeface.BOLD);

                // Update selected method and number
                if (card == methodBkash) {
                    selectedMethodType = "bkash";
                    tvPhoneNumber.setText(bkashNumber);
                } else if (card == methodNagad) {
                    selectedMethodType = "nagad";
                    tvPhoneNumber.setText(nagadNumber);
                } else if (card == methodBank) {
                    selectedMethodType = "bank";
                    tvPhoneNumber.setText(bankNumber);
                }
            });
        }
    }

    private void verifyAndSubmitPayment(String trxId, int courseID) {
        btnConfirm.setEnabled(false); // Disable to prevent double submission

        db.collection("payment").document(trxId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            Toast.makeText(this, "এই Transaction ID টি ইতিমধ্যে ব্যবহার করা হয়েছে!", Toast.LENGTH_LONG).show();
                            btnConfirm.setEnabled(true);
                        } else {
                            savePaymentData(trxId, auth.getUid(), courseID);
                        }
                    } else {
                        btnConfirm.setEnabled(true);
                        Log.e("Firestore", "Error checking trxId", task.getException());
                    }
                });
    }

    private void savePaymentData(String trxId, String uid, int courseID) {
        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("name", name);
        paymentData.put("gender", gender);
        paymentData.put("dob", dob);
        paymentData.put("trxId", trxId);
        paymentData.put("uid", uid);
        paymentData.put("status", "processing");
        paymentData.put("courseID", courseID);
        paymentData.put("method", selectedMethodType);
        paymentData.put("timestamp", FieldValue.serverTimestamp());

        db.collection("payment")
                .document(trxId)
                .set(paymentData)
                .addOnSuccessListener(aVoid -> {
                    startActivity(new Intent(paymentActivity.this, AboutUs.class));

                })
                .addOnFailureListener(e -> {
                    btnConfirm.setEnabled(true);
                    Log.e("Firestore", "Save error: " + e.getMessage());
                    Toast.makeText(this, "পেমেন্ট সেভ করতে সমস্যা হয়েছে। আবার চেষ্টা করুন।", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadPaymentSettings() {
        db.collection("paymentSetting")
                .document("methods")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        bkashNumber = documentSnapshot.getString("bkashNumber");
                        nagadNumber = documentSnapshot.getString("nagadNumber");
                        bankNumber = documentSnapshot.getString("bankNumber");
                    } else {
                        Toast.makeText(this, "পেমেন্ট সেটিংস পাওয়া যায়নি!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Load error: " + e.getMessage()));
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    void LoadDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(paymentActivity.this);
        builder.setTitle("Payment Details");

        LinearLayout layout = new LinearLayout(paymentActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText etName = new EditText(paymentActivity.this);
        etName.setHint("Name");
        layout.addView(etName);

        final EditText etGender = new EditText(paymentActivity.this);
        etGender.setHint("Gender");
        layout.addView(etGender);

        final EditText etDob = new EditText(paymentActivity.this);
        etDob.setHint("Date of birth");
        layout.addView(etDob);

        builder.setView(layout);

        // ⚠️ এখানে null দিবা (important)
        builder.setPositiveButton("Confirm", null);
        builder.setNegativeButton("Cancel", (dialogInterface, i) -> {
            getOnBackPressedDispatcher().onBackPressed();
        });
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();

        dialog.show();


        // ✅ override button click
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {

            String namee = etName.getText().toString().trim();
            String genderStr = etGender.getText().toString().trim();
            String dobStr = etDob.getText().toString().trim();

            boolean isValid = true;

            if (namee.isEmpty()) {
                etName.setError("আগে পূরণ করুন");
                isValid = false;
            }

            if (genderStr.isEmpty()) {
                etGender.setError("আগে পূরণ করুন");
                isValid = false;
            }

            if (dobStr.isEmpty()) {
                etDob.setError("আগে পূরণ করুন");
                isValid = false;
            }

            // ✅ সব ঠিক হলে তবেই dismiss
            if (isValid) {
                name = namee;
                gender = genderStr;
                dob = dobStr;
                dialog.dismiss();
            }
        });

    }


}