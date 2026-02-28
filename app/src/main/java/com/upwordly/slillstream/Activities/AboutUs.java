package com.upwordly.slillstream.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;
import com.upwordly.slillstream.Adapters.Course;
import com.upwordly.slillstream.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AboutUs extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private List<Course> allCourses = new ArrayList<>();
    private List<Course> verifiedCoursesToShow = new ArrayList<>();
    RecyclerView recyclerView;
    private MyCourseAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_about_us);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        recyclerView = findViewById(R.id.recyclerView);
        adapter = new MyCourseAdapter(verifiedCoursesToShow);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadAllCoursesFromJSON();
    }

    private void loadAllCoursesFromJSON() {
        new Thread(() -> {
            String json = loadJSONFromAsset(AboutUs.this, "course.json");
            if (json != null) {
                Gson gson = new Gson();
                CourseResponse response = gson.fromJson(json, CourseResponse.class);
                if (response != null && response.getCourses() != null) {
                    allCourses.clear();
                    allCourses.addAll(response.getCourses());

                    getVerifiedCourses();
                }
            }
        }).start();
    }

    private void getVerifiedCourses() {
        String currentUid = auth.getUid();
        if (currentUid == null) return;

        db.collection("payment")
                .whereEqualTo("uid", currentUid)
                .whereEqualTo("status", "processing")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Integer> verifiedIDs = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Long cId = document.getLong("courseID");
                        if (cId != null) verifiedIDs.add(cId.intValue());
                        Log.d("DEBUG_FIRESTORE", "courseID: " + cId);
                    }
                    filterAndShowCourses(verifiedIDs);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Error: " + e.getMessage());
                });
    }

    private void filterAndShowCourses(List<Integer> verifiedIDs) {
        verifiedCoursesToShow.clear();

        for (Course course : allCourses) {
            if (verifiedIDs.contains(course.getId())) {
                verifiedCoursesToShow.add(course);
                Log.d("DEBUG_JSON", "json id: " + course.getId());
            }
        }

        // UI থ্রেডে আপডেট নিশ্চিত করা
        runOnUiThread(() -> {
            adapter.notifyDataSetChanged();

            if (verifiedCoursesToShow.isEmpty()) {
                Toast.makeText(AboutUs.this, "কোনো ভেরিফাইড কোর্স পাওয়া যায়নি", Toast.LENGTH_SHORT).show();
            } else {
                Log.d("Filter", "Courses shown: " + verifiedCoursesToShow.size());
            }
        });
    }

    public String loadJSONFromAsset(Context context, String filename) {
        try (InputStream is = context.getAssets().open(filename)) {
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            return new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Inner class for GSON response mapping
    public static class CourseResponse {
        private List<Course> courses;

        public List<Course> getCourses() {
            return courses;
        }
    }

    public class MyCourseAdapter extends RecyclerView.Adapter<MyCourseAdapter.MyViewHolder> {

        private List<Course> courseList;

        public MyCourseAdapter(List<Course> courseList) {
            this.courseList = courseList;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.course_item, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            Course course = courseList.get(position);
            holder.courseName.setText(course.getCourseName());
            holder.courseStartDate.setText(course.getCourseStartDate());
            int imageResId = getResources().getIdentifier(course.getImage(), "drawable", getPackageName());

            holder.ImageView.setImageResource(imageResId);

            holder.coursePrice.setText("View Course");
            holder.coursePrice.setBackgroundColor(getColor(R.color.gradient_end));
            holder.coursePrice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(AboutUs.this, CourseDetails.class)
                            .putExtra("courseID", course.getId()));

                }
            });

        }

        @Override
        public int getItemCount() {
            return courseList.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
             TextView courseName, coursePrice,courseStartDate;
             ImageView ImageView;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                 courseName = itemView.findViewById(R.id.title);
                coursePrice = itemView.findViewById(R.id.coursePrice);
                ImageView = itemView.findViewById(R.id.ImageView);
                courseStartDate = itemView.findViewById(R.id.courseStartDate);
            }
        }
    }

}
