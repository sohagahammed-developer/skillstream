package com.upwordly.slillstream.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.upwordly.slillstream.Adapters.Course;
import com.upwordly.slillstream.R;

import java.util.HashMap;
import java.util.Map;

public class CourseDetailsActivity extends AppCompatActivity {


    ImageView courseImage;
    TextView title, name, date, price;
    LinearLayout techContainer, guidelineContainer;
    Button enrollBtn;
    RatingBar user_rating;
    FirebaseAuth auth;
    FirebaseFirestore db;
    Course course;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_course_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        courseImage = findViewById(R.id.courseImage);
        title = findViewById(R.id.courseTitle);
        name = findViewById(R.id.courseName);
        date = findViewById(R.id.startDate);
        price = findViewById(R.id.price);
        techContainer = findViewById(R.id.techContainer);
        guidelineContainer = findViewById(R.id.guidelineContainer);
        enrollBtn = findViewById(R.id.enrollBtn);
        user_rating = findViewById(R.id.user_rating);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        course = (Course) getIntent().getSerializableExtra("course_data");


        if (course != null) {

            title.setText(course.getCourseTitle());
            name.setText(course.getCourseName());
            date.setText("Start Date: " + course.getCourseStartDate());
            price.setText("BDT " + course.getCoursePrice());

            int imgId = getResources().getIdentifier(
                    course.getImage(),
                    "drawable",
                    getPackageName()
            );
            courseImage.setImageResource(imgId);

            for (String tech : course.getLearningTechnology()) {
                TextView tv = new TextView(this);
                tv.setText("• " + tech);
                tv.setTextSize(15);
                tv.setTextColor(getColor(R.color.colorControlHighlight));
                tv.setPadding(0, 8, 0, 8);
                techContainer.addView(tv);
            }

            for (String rule : course.getGuidelines()) {
                TextView tv = new TextView(this);
                tv.setText("✓ " + rule);
                tv.setTextSize(14);
                tv.setTextColor(getColor(R.color.colorControlHighlight));
                tv.setPadding(0, 8, 0, 8);
                guidelineContainer.addView(tv);
            }
        }

        enrollBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CourseDetailsActivity.this, paymentActivity.class);
                intent.putExtra("course_price", course.getCoursePrice());
                intent.putExtra("courseID", course.getId());
                startActivity(intent);
            }
        });
        user_rating.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    showReviewDialog();
                    return true;
                }
                return true;
            }
        });
    }
    public void showReviewDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.review, null);
        builder.setView(dialogView);

        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
        EditText editReview = dialogView.findViewById(R.id.editReview);
        Button btnSubmit = dialogView.findViewById(R.id.btnSubmit);

        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float rating = ratingBar.getRating();
                String reviewText = editReview.getText().toString().trim();
                String userId = auth.getUid();

                if (userId == null) {
                    Toast.makeText(CourseDetailsActivity.this, "Please Login First", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (rating == 0) {
                    Toast.makeText(CourseDetailsActivity.this, "Please select at least 1 star", Toast.LENGTH_SHORT).show();
                } else if (reviewText.isEmpty()) {
                    Toast.makeText(CourseDetailsActivity.this, "Please write a comment", Toast.LENGTH_SHORT).show();
                } else {

                    // DATA PREPARATION
                    Map<String, Object> reviewData = new HashMap<>();
                    reviewData.put("rating", rating);
                    reviewData.put("comment", reviewText);
                    reviewData.put("userId", userId);
                    reviewData.put("courseId", course.getId());
                    reviewData.put("timestamp", System.currentTimeMillis());

                    // UNIQUE ID: This prevents multiple reviews for the SAME course by the SAME user
                    String uniqueDocId = userId + "_" + course.getId();

                    db.collection("Reviews").document(uniqueDocId)
                            .set(reviewData)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(CourseDetailsActivity.this, "Review submitted successfully!", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(CourseDetailsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

        dialog.show();
    }
}