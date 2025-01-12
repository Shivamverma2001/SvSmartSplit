package com.sv.svsplitsmart.ui.activity;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ActivityViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public ActivityViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is slideshow fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}