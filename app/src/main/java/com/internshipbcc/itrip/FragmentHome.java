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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.SearchSuggestionsAdapter;
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
import com.internshipbcc.itrip.Search.DataHelper;
import com.internshipbcc.itrip.Search.ItemSuggestion;
import com.internshipbcc.itrip.Util.ItemHome;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Sena on 12/03/2018.
 */

public class FragmentHome extends Fragment {
    public static final int FIND_SUGGESTION_SIMULATED_DELAY = 0;
    public static final int VOICE_SEARCH_CODE = 120;
    public String mLastQuery = "";
    private FloatingSearchView fsv;
    private SliderLayout sliderLayout;
    private RecyclerView rvHome;
    private RelativeLayout loadingDimLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        fsv = rootView.findViewById(R.id.floating_search_view);
        sliderLayout = rootView.findViewById(R.id.slider);
        rvHome = rootView.findViewById(R.id.rv_home);

        loadingDimLayout = rootView.findViewById(R.id.loading_dim_layout);

        setupFloatSearchView();
        setupSliderLayout();
        setupRvHome();
        return rootView;
    }

    private void setupFloatSearchView() {
        fsv.setOnQueryChangeListener((oldQuery, newQuery) -> {
            if (!oldQuery.equals("") && newQuery.equals("")) {
                fsv.clearSuggestions();
            } else {
                //this shows the top left circular progress
                //you can call it where ever you want, but
                //it makes sense to do it when loading something in
                //the background.
                fsv.showProgress();

                //simulates a query call to a data source
                //with a new query.
                DataHelper.findSuggestions(getActivity(), newQuery, 5,
                        FIND_SUGGESTION_SIMULATED_DELAY, new DataHelper.OnFindSuggestionsListener() {
                            @Override
                            public void onResults(List<ItemSuggestion> results) {
                                fsv.swapSuggestions(results);
                                fsv.hideProgress();
                            }
                        });
            }
        });

        fsv.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(final SearchSuggestion searchSuggestion) {
                ItemSuggestion colorSuggestion = (ItemSuggestion) searchSuggestion;
                mLastQuery = searchSuggestion.getBody();
                fsv.setSearchText(mLastQuery);
                fsv.clearSearchFocus();
                fsv.clearSuggestions();
            }

            @Override
            public void onSearchAction(String query) {
                mLastQuery = query;

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
        fsv.setOnMenuItemClickListener(new FloatingSearchView.OnMenuItemClickListener() {
            @Override
            public void onActionMenuItemSelected(MenuItem item) {
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
        fsv.setOnBindSuggestionCallback(new SearchSuggestionsAdapter.OnBindSuggestionCallback() {
            @Override
            public void onBindSuggestion(View suggestionView, ImageView leftIcon,
                                         TextView textView, SearchSuggestion item, int itemPosition) {
                ItemSuggestion itemSuggestion = (ItemSuggestion) item;

                String textColor = "#000000";
                String textLight = "#787878";

                if (itemSuggestion.isHistory()) {
                    leftIcon.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                            R.drawable.ic_history_black_24dp, null));

                    Util.setIconColor(leftIcon, Color.parseColor(textColor));
                    leftIcon.setAlpha(.36f);
                } else {
                    leftIcon.setAlpha(0.0f);
                    leftIcon.setImageDrawable(null);
                }

                textView.setTextColor(Color.parseColor(textColor));
                String text = itemSuggestion.getBody()
                        .replaceFirst(fsv.getQuery(),
                                "<font color=\"" + textLight + "\">" + fsv.getQuery() + "</font>");
                textView.setText(Html.fromHtml(text));
            }

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
        sliderLayout.setDuration(4000);
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

                RvAdapterHome adapterHome = new RvAdapterHome(getActivity(), dataHome);
                rvHome.setLayoutManager(new LinearLayoutManager(getActivity()));
                rvHome.setAdapter(adapterHome);

                loadingDimLayout.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onDestroyView() {
        sliderLayout.stopAutoCycle();
        super.onDestroyView();
    }

    public void setSearchQuery(String teks) {
        fsv.setSearchText(teks);
    }
}
