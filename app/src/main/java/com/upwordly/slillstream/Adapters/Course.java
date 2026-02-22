package com.upwordly.slillstream.Adapters;

import java.io.Serializable;
import java.util.List;

public class Course implements Serializable {
    private int id;
    private String image;
    private String course_name;
    private String course_title;
    private int course_price;
    private String course_start_date;
    private List<String> learning_technology;
    private List<String> guidelines;


    public int getId() { return id; }
    public String getImage() { return image; }
    public String getCourseName() { return course_name; }
    public String getCourseTitle() { return course_title; }
    public int getCoursePrice() { return course_price; }
    public String getCourseStartDate() { return course_start_date; }
    public List<String> getLearningTechnology() { return learning_technology; }
    public List<String> getGuidelines() { return guidelines; }


    public void setId(int id) { this.id = id; }
    public void setImage(String image) { this.image = image; }
    public void setCourseName(String course_name) { this.course_name = course_name; }
    public void setCourseTitle(String course_title) { this.course_title = course_title; }
    public void setCoursePrice(int course_price) { this.course_price = course_price; }
    public void setCourseStartDate(String course_start_date) { this.course_start_date = course_start_date; }
    public void setLearningTechnology(List<String> learning_technology) { this.learning_technology = learning_technology; }
    public void setGuidelines(List<String> guidelines) { this.guidelines = guidelines; }
}
