package com.sv.svsplitsmart.ui.groups;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class GroupViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public GroupViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is gallery fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}