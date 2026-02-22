package com.upwordly.slillstream.Adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.upwordly.slillstream.Activities.CourseDetailsActivity;
import com.upwordly.slillstream.Activities.paymentActivity;
import com.upwordly.slillstream.R;

import java.util.ArrayList;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewholder> {

    Context context;
    ArrayList<Course> arrayList;

    // Constructor
    public CourseAdapter(Context context, ArrayList<Course> arrayList){
        this.context = context;
        this.arrayList = arrayList;
    }

    // Inflate item layout
    @NonNull
    @Override
    public CourseViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.course_item, parent, false);
        return new CourseViewholder(view);
    }

    // Bind data
    @Override
    public void onBindViewHolder(@NonNull CourseViewholder holder, int position) {
        Course model = arrayList.get(position);

        holder.title.setText(model.getCourseTitle());
        holder.price.setText("BDT " + model.getCoursePrice());
        holder.startDate.setText("Start: " + model.getCourseStartDate());
        int imageResId = context.getResources().getIdentifier(model.getImage(), "drawable", context.getPackageName());

        holder.imageView.setImageResource(imageResId);

        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                Intent intent = new Intent(context, CourseDetailsActivity.class);
                intent.putExtra("course_data", arrayList.get(pos));
                context.startActivity(intent);
            }
        });
        holder.price.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, paymentActivity.class);
                intent.putExtra("course_price", model.getCoursePrice());
                intent.putExtra("courseID", model.getId());
                context.startActivity(intent);
            }
        });




    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    // ViewHolder
    public static class CourseViewholder extends RecyclerView.ViewHolder {
        TextView  title, price, startDate;
        ImageView imageView;

        public CourseViewholder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.ImageView);
            title = itemView.findViewById(R.id.title);
            price = itemView.findViewById(R.id.coursePrice);
            startDate = itemView.findViewById(R.id.courseStartDate);
        }
    }


}
