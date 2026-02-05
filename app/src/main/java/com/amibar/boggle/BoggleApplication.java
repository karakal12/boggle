package com.amibar.boggle;

import android.app.Application;
import com.amibar.boggle.data.Dictionary;

public class BoggleApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize the dictionary when the application process starts
        Dictionary.getInstance().init(getResources().openRawResource(R.raw.word_list));
    }
}
