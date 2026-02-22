package com.upwordly.slillstream.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.upwordly.slillstream.Adapters.Course;
import com.upwordly.slillstream.R;

public class CourseDetailsActivity extends AppCompatActivity {


    ImageView courseImage;
    TextView title, name, date, price;
    LinearLayout techContainer, guidelineContainer;
    Button enrollBtn;

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

        Course course = (Course) getIntent().getSerializableExtra("course_data");


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
    }
}