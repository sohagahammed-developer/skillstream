package com.upwordly.slillstream.Fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.upwordly.slillstream.Adapters.Course;
import com.upwordly.slillstream.Adapters.CourseAdapter;
import com.upwordly.slillstream.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class CourseFragment extends Fragment {
    RecyclerView recyclerView;
    ArrayList<Course> courseList = new ArrayList<>();
    CourseAdapter adapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.fragment_course, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);

        new Thread(() -> {

            String json = loadJSONFromAsset(requireContext(), "course.json");

            if (json != null) {
                Gson gson = new Gson();
                HomeFragment.CourseResponse response = gson.fromJson(json, HomeFragment.CourseResponse.class);

                if (response != null && response.getCourses() != null) {
                    courseList.clear();
                    courseList.addAll(response.getCourses());
                }
            }

            requireActivity().runOnUiThread(() -> {
                adapter.notifyDataSetChanged();
            });

        }).start();

        adapter = new CourseAdapter(requireContext(), courseList);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        return view;
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



    public class CourseResponse {
        private List<Course> courses;
        public List<Course> getCourses() {
            return courses;
        }
    }
}