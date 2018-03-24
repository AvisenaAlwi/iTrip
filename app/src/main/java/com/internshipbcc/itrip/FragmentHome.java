package com.internshipbcc.itrip;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.arlib.floatingsearchview.util.Util;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.internshipbcc.itrip.Adapter.RvAdapterHome;
import com.internshipbcc.itrip.Adapter.RvAdapterSearch;
import com.internshipbcc.itrip.Search.DataHelper;
import com.internshipbcc.itrip.Search.ItemSuggestion;
import com.internshipbcc.itrip.Util.ItemHome;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Created by Sena on 12/03/2018.
 */

public class FragmentHome extends Fragment {
    public static final int FIND_SUGGESTION_SIMULATED_DELAY = 300;
    public static final int VOICE_SEARCH_CODE = 120;
    public String mLastQuery = "";
    private FloatingSearchView fsv;
    private SliderLayout sliderLayout;
    private RecyclerView rvHome, rvSearch;
    private RelativeLayout loadingDimLayout, layoutSearch, loadingDimLayoutSearch;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        fsv = rootView.findViewById(R.id.floating_search_view);
        sliderLayout = rootView.findViewById(R.id.slider);
        rvHome = rootView.findViewById(R.id.rv_home);
        rvSearch = rootView.findViewById(R.id.rv_search);

        loadingDimLayout = rootView.findViewById(R.id.loading_dim_layout);
        layoutSearch = rootView.findViewById(R.id.layout_search);
        loadingDimLayoutSearch = rootView.findViewById(R.id.loading_dim_layout_search);

        setupFloatSearchView();
        setupSliderLayout();
        setupRvHome();
        setupRvSearch();
        return rootView;
    }

    private void setupFloatSearchView() {
        fsv.setOnQueryChangeListener((oldQuery, newQuery) -> {
            if (!oldQuery.equals("") && newQuery.equals("")) {
                fsv.clearSuggestions();
            } else {
                fsv.showProgress();
                DataHelper.findSuggestions(getActivity(), newQuery, 5,
                        FIND_SUGGESTION_SIMULATED_DELAY, results -> {
                            if (fsv.hasFocus())
                                fsv.swapSuggestions(results);
                            else fsv.clearSuggestions();
                            fsv.hideProgress();
                        });
            }
        });
        fsv.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(final SearchSuggestion searchSuggestion) {
                mLastQuery = searchSuggestion.getBody();
                new DbHelper(getContext()).addToHistory(mLastQuery);
                fsv.setSearchBarTitle(mLastQuery);
                fsv.clearSearchFocus();
                fsv.clearSuggestions();
                performSearch();
            }

            @Override
            public void onSearchAction(String query) {
                mLastQuery = query;
                new DbHelper(getContext()).addToHistory(mLastQuery);
                performSearch();
            }
        });
        fsv.setOnFocusChangeListener(new FloatingSearchView.OnFocusChangeListener() {
            @Override
            public void onFocus() {
                //show suggestions when search bar gains focus (typically history suggestions)
                fsv.swapSuggestions(DataHelper.getHistory(getActivity(), 3));
            }

            @Override
            public void onFocusCleared() {
                fsv.clearSuggestions();
            }
        });

        //handle menu clicks the same way as you would
        //in a regular activity
        fsv.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_item_voice_search:
                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                            "Katakan sesuatu...");
                    try {
                        getActivity().startActivityForResult(intent, VOICE_SEARCH_CODE);
                    } catch (ActivityNotFoundException a) {
                        Toast.makeText(getActivity(),
                                "Maaf, fitur pencarian via suara tidak didukung di perangkat Anda.",
                                Toast.LENGTH_SHORT).show();
                    }
            }
        });
        /*
         * Here you have access to the left icon and the text of a given suggestion
         * item after as it is bound to the suggestion list. You can utilize this
         * callback to change some properties of the left icon and the text. For example, you
         * can load the left icon images using your favorite image loading library, or change text color.
         *
         *
         * Important:
         * Keep in mind that the suggestion list is a RecyclerView, so views are reused for different
         * items in the list.
         */
        fsv.setOnBindSuggestionCallback((suggestionView, leftIcon, textView, item, itemPosition) -> {
            ItemSuggestion itemSuggestion = (ItemSuggestion) item;
            String textColor = "#000000";
            String textLight = "#787878";
            if (itemSuggestion.isHistory() || new DbHelper(getContext()).getHistoryTitle()
                    .contains(itemSuggestion.getBody())) {
                leftIcon.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.ic_history_black_24dp, null));
                Util.setIconColor(leftIcon, Color.parseColor(textColor));
                leftIcon.setAlpha(.36f);
            } else {
                leftIcon.setAlpha(0.0f);
                leftIcon.setImageDrawable(null);
            }

            textView.setTextColor(Color.parseColor(textLight));
            String text = itemSuggestion.getBody()
                    .replaceFirst(fsv.getQuery(),
                            "<font color=\"" + textColor + "\">" + fsv.getQuery() + "</font>");
            textView.setText(Html.fromHtml(text));
        });
    }

    private void setupSliderLayout() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference dbRef = db.getReference("/slideshow");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                sliderLayout.removeAllSliders();
                for (DataSnapshot i : dataSnapshot.getChildren()) {
                    String title = i.child("title").getValue(String.class);
                    String image = i.child("image").getValue(String.class);
                    //dataSlideshow.put(title, image);
                    TextSliderView textSliderView = new TextSliderView(getActivity());
                    textSliderView.description(title)
                            .image(image)
                            .setScaleType(BaseSliderView.ScaleType.CenterCrop);

                    sliderLayout.addSlider(textSliderView);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        sliderLayout.setPresetTransformer(SliderLayout.Transformer.Default);
        sliderLayout.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        sliderLayout.setCustomAnimation(new DescriptionAnimation());
        sliderLayout.setDuration(8000);
        sliderLayout.startAutoCycle();
    }

    private void setupRvHome() {

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference dbRef = db.getReference("/home");
        List<ItemHome> dataHome = new ArrayList<>();
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot homeItem : dataSnapshot.getChildren()) {
                    String id = homeItem.child("id").getValue(String.class);
                    String title = homeItem.child("title").getValue(String.class);
                    String des = homeItem.child("des").getValue(String.class);
                    String image = homeItem.child("image").getValue(String.class);
                    boolean isWisata = homeItem.child("type").getValue(Long.class) == 0;
                    dataHome.add(new ItemHome(id, title, des, image, isWisata));
                }
                Collections.reverse(dataHome);
                RvAdapterHome adapterHome = new RvAdapterHome(getActivity(), dataHome);
                rvHome.setLayoutManager(new LinearLayoutManager(getActivity()));
                rvHome.setAdapter(adapterHome);

                try {
                    Animation fadeOut = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out);
                    fadeOut.setDuration(600);
                    fadeOut.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            loadingDimLayout.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                    });
                    loadingDimLayout.setAnimation(fadeOut);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setupRvSearch() {
        rvSearch.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    @Override
    public void onDestroyView() {
        sliderLayout.stopAutoCycle();
        super.onDestroyView();
    }

    public void setSearchQuery(String teks) {
        fsv.setSearchText(teks);
    }

    protected void performSearch() {
        loadingDimLayoutSearch.setVisibility(View.VISIBLE);
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("/items");
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<ItemHome> data = new ArrayList<>();
                for (DataSnapshot item : dataSnapshot.getChildren()) {
                    String title = item.child("title").getValue(String.class);
                    String location = item.child("location").getValue(String.class);
                    if (title != null && title.toLowerCase().contains(mLastQuery.toLowerCase()) ||
                            location != null && location.toLowerCase().contains(mLastQuery.toLowerCase())) {
                        //Jika item yang dicari ditemukan
                        String desc = item.child("desc").getValue(String.class);
                        String image = item.child("images").getValue(String.class).split(",")[0];
                        boolean isWisata = item.child("images").getValue(String.class).equalsIgnoreCase("0");
                        ItemHome itemCari = new ItemHome(item.getKey(), title, desc, image, isWisata);
                        data.add(itemCari);
                    }
                }
                if (data.size() != 0) {
                    rvSearch.setVisibility(View.VISIBLE);
                    rvSearch.setAdapter(new RvAdapterSearch(getActivity(), data));
                } else {
                    rvSearch.setVisibility(View.GONE);
                    rvSearch.setAdapter(null);
                }
                try {
                    Thread.sleep(400);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                layoutSearch.setVisibility(View.VISIBLE);
                loadingDimLayoutSearch.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public boolean onBackPressed() {
        if (layoutSearch.getVisibility() == View.VISIBLE) {
            rvSearch.setAdapter(null);
            layoutSearch.setVisibility(View.GONE);
            fsv.clearQuery();
            fsv.clearSearchFocus();
            return true;
        }
        return false;
    }

}
