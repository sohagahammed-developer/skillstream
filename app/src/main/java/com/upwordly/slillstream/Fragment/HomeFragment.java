package com.upwordly.slillstream.Fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.gson.Gson;
import com.upwordly.slillstream.Adapters.Course;
import com.upwordly.slillstream.Adapters.CourseAdapter;
import com.upwordly.slillstream.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {

    ImageSlider image_slider;
    RecyclerView recyclerView;
    CourseAdapter adapter;
    ArrayList<SlideModel> slideModels = new ArrayList<>();
    ArrayList<Course> courseList = new ArrayList<>();
    SearchView courseSearch;

    private List<Course> courseListFull;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        image_slider = view.findViewById(R.id.image_slider);
        recyclerView = view.findViewById(R.id.recyclerView);
        courseSearch = view.findViewById(R.id.courseSearch);


        slideModels.add(new SlideModel(R.drawable.img_1,ScaleTypes.CENTER_CROP));
        slideModels.add(new SlideModel(R.drawable.img_2,ScaleTypes.CENTER_CROP));
        slideModels.add(new SlideModel(R.drawable.img_3,ScaleTypes.CENTER_CROP));
        slideModels.add(new SlideModel(R.drawable.img_4,ScaleTypes.CENTER_CROP));

        image_slider.setImageList(slideModels, ScaleTypes.CENTER_CROP);


//        new Thread(() -> {
//
//            String json = loadJSONFromAsset(requireContext(), "course.json");
//
//            if (json != null) {
//                Gson gson = new Gson();
//                CourseResponse response = gson.fromJson(json, CourseResponse.class);
//
//                if (response != null && response.getCourses() != null) {
//                    courseList.clear();
//                    courseList.addAll(response.getCourses());
//                }
//            }
//
//            requireActivity().runOnUiThread(() -> {
//                adapter.notifyDataSetChanged();
//            });
//
//        }).start();

        new Thread(() -> {
            String json = loadJSONFromAsset(requireContext(), "course.json");

            if (json != null) {
                Gson gson = new Gson();
                CourseResponse response = gson.fromJson(json, CourseResponse.class);

                if (response != null && response.getCourses() != null) {
                    List<Course> fetchedCourses = response.getCourses();

                    // UI আপডেট সবসময় runOnUiThread এর ভেতর হবে
                    requireActivity().runOnUiThread(() -> {
                        // সরাসরি অ্যাডাপ্টারের updateList মেথড কল করুন
                        // এটি আপনার arrayList এবং arrayListFull দুইটাই আপডেট করে দেবে
                        adapter.updateList(fetchedCourses);
                    });
                }
            }
        }).start();


        adapter = new CourseAdapter(requireContext(), courseList);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        courseSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Call the filter method from your CourseAdapter
                if (adapter != null) {
                    adapter.filter(newText);
                }
                return true;
            }
        });

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