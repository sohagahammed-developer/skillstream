package com.upwordly.slillstream.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.upwordly.slillstream.Adapters.CourseAdapter;
import com.upwordly.slillstream.Fragment.ContuctFtagment;
import com.upwordly.slillstream.Fragment.CourseFragment;
import com.upwordly.slillstream.Fragment.HomeFragment;
import com.upwordly.slillstream.Fragment.MyCourseFragment;
import com.upwordly.slillstream.R;

public class HomeActivity extends AppCompatActivity {

    BottomNavigationView navigationView;
    MaterialToolbar toolbar;
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);
        loadFragment(new HomeFragment());
        mAuth = FirebaseAuth.getInstance();
        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                if (menuItem.getItemId() == R.id.menu_home)
                {
                    loadFragment(new HomeFragment());
                }else if (menuItem.getItemId() == R.id.menu_course)
                {
                    loadFragment(new CourseFragment());
                }else if (menuItem.getItemId() == R.id.menu_my_course)
                {
                    loadFragment(new MyCourseFragment());
                }else if (menuItem.getItemId() == R.id.menu_contact)
                {
                    loadFragment(new ContuctFtagment());
                }

                return true;
            }
        });
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                if (item.getItemId() == R.id.aboutUs) {
                    loadFragment(new MyCourseFragment());
                    selectBottomNavItem(R.id.menu_my_course);

                }
                else if (item.getItemId() == R.id.menu_course) {
                    loadFragment(new CourseFragment());
                    selectBottomNavItem(R.id.menu_course);

                }
                else if (item.getItemId() == R.id.menu_my_course) {
                    startActivity(new Intent(HomeActivity.this, AboutUs.class));

                }
                else if (item.getItemId() == R.id.menu_contact) {
                    loadFragment(new ContuctFtagment());
                    selectBottomNavItem(R.id.menu_contact);

                }
                else if (item.getItemId() == R.id.logout) {
                    Logout();
                }

                return true;
            }
        });



    }

    private void Logout() {
        new MaterialAlertDialogBuilder(this).setTitle("Are you sure?")
                .setMessage("Do you really want to log out from your account?")
                .setIcon(R.drawable.baseline_logout_24)
                .setCancelable(false)
                .setPositiveButton("Yes, Logout", (dialog, which) -> {
                    mAuth.signOut();
                    Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }).setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss()).show();
    }
    public void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }
    private void selectBottomNavItem(int menuId) {
        navigationView.setSelectedItemId(menuId);
    }





}