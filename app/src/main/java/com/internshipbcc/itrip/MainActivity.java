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
//    FragmentHome fHome = new FragmentHome();
//    FragmentNearby fNearby = new FragmentNearby();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(android.R.anim.fade_in, 0);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomBar = findViewById(R.id.bottomBar);
        bottomBar.setOnTabSelectListener(tabId -> {
            switch (tabId) {
                case R.id.tab_home:
                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new FragmentHome(), "home")
                            .commit();
                    break;
                case R.id.tab_nearby:
                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new FragmentNearby(), "nearby")
                            .commit();
                    break;
                case R.id.tab_account:
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new FragmentAccount(), "account")
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

    @Override
    public void onBackPressed() {
        try {
            FragmentHome home = (FragmentHome) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (!home.onBackPressed()) {
                super.onBackPressed();
            }
        } catch (Exception e) {
            e.printStackTrace();

            super.onBackPressed();
        }

    }
}
