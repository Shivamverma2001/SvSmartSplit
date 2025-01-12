package com.sv.svsplitsmart.ui.friends;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FriendViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public FriendViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}