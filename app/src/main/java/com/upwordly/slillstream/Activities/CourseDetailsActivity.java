package com.upwordly.slillstream.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.upwordly.slillstream.Adapters.Course;
import com.upwordly.slillstream.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CourseDetailsActivity extends AppCompatActivity {


    ImageView courseImage;
    TextView title, name, date, price;
    LinearLayout techContainer, guidelineContainer;
    Button enrollBtn;
    FirebaseAuth auth;
    FirebaseFirestore db;
    Course course;
    RecyclerView courseReview;
    ReviewAdapter adapter;

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
        courseReview = findViewById(R.id.courseReview);
        courseReview.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReviewAdapter();
        courseReview.setAdapter(adapter);

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
        loadReviews(course.getId());

    }

    private void loadReviews(int courseId) {
        db.collection("Reviews")
                .whereEqualTo("courseId", courseId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Toast.makeText(CourseDetailsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        List<Map<String, Object>> reviewList = new ArrayList<>();

                        if (value != null) {
                            for (DocumentSnapshot doc : value.getDocuments()) {
                                Map<String, Object> data = doc.getData();
                                if (data != null) {
                                    reviewList.add(data);
                                }
                            }
                            adapter.setReviews(reviewList);

                        }
                    }
                });
    }

    public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewHolder> {
        private List<Map<String, Object>> reviewList = new ArrayList<>();

        // নতুন ডেটা সেট করার জন্য মেথড
        public void setReviews(List<Map<String, Object>> list) {
            this.reviewList = list;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ReviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // এখানে আপনার রিভিউ আইটেম লেআউট (R.layout.item_review) ইনফ্লেট করুন
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
            return new ReviewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ReviewHolder holder, int position) {
            Map<String, Object> data = reviewList.get(position);

            // Map থেকে ডাটা বের করা
            String comment = String.valueOf(data.get("comment"));
            // Firestore থেকে আসা নম্বরগুলো সাধারণত Double বা Long হয়, তাই ফ্লোটে কনভার্ট করা
            float rating = Float.parseFloat(String.valueOf(data.get("rating")));

            holder.commentText.setText(comment);
            holder.ratingBar.setRating(rating);
        }

        @Override
        public int getItemCount() {
            return reviewList.size();
        }

        public class ReviewHolder extends RecyclerView.ViewHolder {
            TextView commentText;
            RatingBar ratingBar;

            public ReviewHolder(@NonNull View itemView) {
                super(itemView);
                // আপনার item_review.xml এর আইডি অনুযায়ী সেট করুন
                commentText = itemView.findViewById(R.id.rev_comment);
                ratingBar = itemView.findViewById(R.id.rev_rating);
            }
        }
    }

}