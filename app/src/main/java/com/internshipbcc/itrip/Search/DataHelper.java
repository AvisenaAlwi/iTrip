package com.internshipbcc.itrip.Search;

import android.content.Context;
import android.widget.Filter;

import com.internshipbcc.itrip.DbHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Sena on 12/03/2018.
 */

public class DataHelper {

    public static List<ItemSuggestion> dataSuggestion = Arrays.asList(
            new ItemSuggestion("Malang"),
            new ItemSuggestion("Surabaya"),
            new ItemSuggestion("Sidoarjo"),
            new ItemSuggestion("Kepanjen"),
            new ItemSuggestion("Bromo"),
            new ItemSuggestion("Gunung Bromo"),
            new ItemSuggestion("Coban Rondo"),
            new ItemSuggestion("Coban Talun"),
            new ItemSuggestion("Sumber Sira"),
            new ItemSuggestion("Sumber Maron"),
            new ItemSuggestion("Pantai Balekambang"),
            new ItemSuggestion("Pantai Teluk Asmoro"),
            new ItemSuggestion("Pantai 3 Warna"),
            new ItemSuggestion("Jatimpark"),
            new ItemSuggestion("Jatimpark 1"),
            new ItemSuggestion("Jatimpark 2"),
            new ItemSuggestion("Jatimpark 3"),
            new ItemSuggestion("Jawa Timur Park 1"),
            new ItemSuggestion("Jawa Timur Park 2"),
            new ItemSuggestion("Jawa Timur Park 3"),
            new ItemSuggestion("Taman Savari")
    );

    public static List<ItemSuggestion> getHistory(Context context, int count) {
//        List<ItemSuggestion> suggestionList = new ArrayList<>();
//        ItemSuggestion colorSuggestion;
//        for (int i = 0; i < dataSuggestion.size(); i++) {
//            colorSuggestion = dataSuggestion.get(i);
//            colorSuggestion.setHistory(true);
//            suggestionList.add(colorSuggestion);
//            if (suggestionList.size() == count) {
//                break;
//            }
//        }
        return new DbHelper(context).getHistory();
    }

    public static void findSuggestions(Context context, String query, final int limit, final long simulatedDelay,
                                       final OnFindSuggestionsListener listener) {
        new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                try {
                    Thread.sleep(simulatedDelay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                List<ItemSuggestion> suggestionList = new ArrayList<>();
                if (!(constraint == null || constraint.length() == 0)) {
                    for (ItemSuggestion suggestion : dataSuggestion) {
                        if (suggestion.getBody().toLowerCase()
                                .contains(constraint.toString().toLowerCase())) {
                            suggestionList.add(suggestion);
                            if (limit != -1 && suggestionList.size() == limit) {
                                break;
                            }
                        }
                    }
                    for (ItemSuggestion suggestion : new DbHelper(context).getHistory()) {
                        if (suggestion.getBody().toLowerCase()
                                .contains(constraint.toString().toLowerCase())) {
                            if (!suggestionList.contains(suggestion))
                                suggestionList.add(suggestion);
                            if (limit != -1 && suggestionList.size() == limit) {
                                break;
                            }
                        }
                    }
                }

                FilterResults results = new FilterResults();
                Collections.sort(suggestionList, (lhs, rhs) -> lhs.isHistory() ? -1 : 0);
                results.values = suggestionList;
                results.count = suggestionList.size();

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (listener != null) {
                    listener.onResults((List<ItemSuggestion>) results.values);
                }
            }
        }.filter(query);

    }

    public interface OnFindSuggestionsListener {
        void onResults(List<ItemSuggestion> results);
    }
}
