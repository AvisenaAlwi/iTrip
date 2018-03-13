package com.internshipbcc.itrip;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.FragmentActivity;
import android.view.WindowManager;

import com.roughike.bottombar.BottomBar;

import java.util.ArrayList;

public class MainActivity extends FragmentActivity {

    BottomBar bottomBar;
    FragmentHome fHome = new FragmentHome();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomBar = findViewById(R.id.bottomBar);
        bottomBar.setOnTabSelectListener(tabId -> {
            switch (tabId) {
                case R.id.tab_home:
                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, fHome, "home")
                            .commit();
                    break;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FragmentHome.VOICE_SEARCH_CODE) {
            if (resultCode == RESULT_OK) {
                ArrayList<String> result = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                String text = result.get(0);
                ((FragmentHome) getSupportFragmentManager().findFragmentById(R.id.fragment_container))
                        .setSearchQuery(text);
            }
        }
    }
}
