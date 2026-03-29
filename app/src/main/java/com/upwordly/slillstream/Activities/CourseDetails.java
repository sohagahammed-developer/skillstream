package com.upwordly.slillstream.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.upwordly.slillstream.R;

import java.util.HashMap;
import java.util.Map;

public class CourseDetails extends AppCompatActivity {
    private String vid1, vid2, vid3;
    RatingBar user_rating;
    FirebaseAuth auth;
    FirebaseFirestore db;
    int courseId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_course_details2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        user_rating = findViewById(R.id.user_rating);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        courseId = getIntent().getIntExtra("courseID", 1);

        // ২. কন্ডিশন অনুযায়ী ভিডিও আইডি সেট করা
        setVideoIds(courseId);

        // ৩. প্লেয়ার খুঁজে বের করা এবং ভিডিও লোড করা
        ImageView thumb1 = findViewById(R.id.img_thumbnail1);
        ImageView thumb2 = findViewById(R.id.img_thumbnail2);
        ImageView thumb3 = findViewById(R.id.img_thumbnail3);

        // ৪. থাম্বনেইল ইমেজ লোড করা
        loadThumbnail(thumb1, vid1);
        loadThumbnail(thumb2, vid2);
        loadThumbnail(thumb3, vid3);

        // ৫. ক্লিকে ইউটিউব ওপেন করা
        findViewById(R.id.layout_video1).setOnClickListener(v -> openYouTube(vid1));
        findViewById(R.id.layout_video2).setOnClickListener(v -> openYouTube(vid2));
        findViewById(R.id.layout_video3).setOnClickListener(v -> openYouTube(vid3));

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
                    Toast.makeText(CourseDetails.this, "Please Login First", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (rating == 0) {
                    Toast.makeText(CourseDetails.this, "Please select at least 1 star", Toast.LENGTH_SHORT).show();
                } else if (reviewText.isEmpty()) {
                    Toast.makeText(CourseDetails.this, "Please write a comment", Toast.LENGTH_SHORT).show();
                } else {

                    Map<String, Object> reviewData = new HashMap<>();
                    reviewData.put("rating", rating);
                    reviewData.put("comment", reviewText);
                    reviewData.put("userId", userId);
                    reviewData.put("courseId", courseId);
                    reviewData.put("timestamp", System.currentTimeMillis());
                    String uniqueDocId = userId + "_" + courseId;

                    db.collection("Reviews").document(uniqueDocId)
                            .set(reviewData)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(CourseDetails.this, "Review submitted successfully!", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(CourseDetails.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

        dialog.show();
    }

    private void setVideoIds(int id) {
        switch (id) {
            case 1: // Flutter (Latest Tutorials)
                vid1 = "VPvVD8t02U8";
                vid2 = "I05uHRzuTxU";
                vid3 = "1ukSR1GRtMU";
                break;
            case 2: // Web Development (HTML/CSS/JS)
                vid1 = "3VcmZ3anN1I";
                vid2 = "qz0aGYMCzl0";
                vid3 = "lI1ae4REbBM";
                break;
            case 3: // Digital Marketing (Beginner to Pro)
                vid1 = "nU-IIXBWlns";
                vid2 = "Z_Ksp6J6964";
                vid3 = "mXG7YV6OAXM";
                break;
            case 4: // Graphic Design (Photoshop/Illustrator)
                vid1 = "un50Bs4BvZ8";
                vid2 = "9EigH6V_RNo";
                vid3 = "V75fT_pI7T0";
                break;
            case 5: // UI/UX Design (Figma)
                vid1 = "c9Wg66H8D0";
                vid2 = "68w2VwalD5w";
                vid3 = "vWk_fGfIlyE";
                break;
            case 6: // Cyber Security (Ethical Hacking)
                vid1 = "3Kq1MIfTWCE";
                vid2 = "z5nc96rlq7Y";
                vid3 = "PlHnamdwGmk";
                break;
            default:
                vid1 = "dQw4w9WgXcQ";
                vid2 = "dQw4w9WgXcQ";
                vid3 = "dQw4w9WgXcQ";
        }
    }

    private void loadThumbnail(ImageView imageView, String videoId) {
        // ইউটিউব থাম্বনেইল পাওয়ার লিঙ্ক (hqdefault ভালো রেজুলেশন দেয়)
        String thumbnailUrl = "https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg";

        Glide.with(this)
                .load(thumbnailUrl)
                .placeholder(com.denzcoskun.imageslider.R.drawable.default_loading) // ছবি লোড হওয়ার সময় যা দেখাবে
                .into(imageView);
    }

    private void openYouTube(String videoId) {
        // প্রথমে ইউটিউব অ্যাপ দিয়ে ট্রাই করবে
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoId));

        // যদি অ্যাপ না থাকে তবে ব্রাউজারে খুলবে
        if (intent.resolveActivity(getPackageManager()) == null) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=" + videoId));
        }
        startActivity(intent);
    }
}