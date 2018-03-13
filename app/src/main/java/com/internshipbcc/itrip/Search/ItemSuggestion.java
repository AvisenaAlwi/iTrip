package com.internshipbcc.itrip.Search;

import android.os.Parcel;

import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

/**
 * Created by Sena on 12/03/2018.
 */

public class ItemSuggestion implements SearchSuggestion {

    public static final Creator<ItemSuggestion> CREATOR = new Creator<ItemSuggestion>() {
        @Override
        public ItemSuggestion createFromParcel(Parcel in) {
            return new ItemSuggestion(in);
        }

        @Override
        public ItemSuggestion[] newArray(int size) {
            return new ItemSuggestion[size];
        }
    };
    String body;
    boolean isHistory = false;

    public ItemSuggestion(String suggestion) {
        this.body = suggestion.toLowerCase();
    }

    public ItemSuggestion(Parcel source) {
        this.body = source.readString();
        this.isHistory = source.readInt() != 0;
    }

    public boolean isHistory() {
        return isHistory;
    }

    public void setHistory(boolean history) {
        isHistory = history;
    }

    @Override
    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(body);
        dest.writeInt(isHistory ? 1 : 0);
    }
}
