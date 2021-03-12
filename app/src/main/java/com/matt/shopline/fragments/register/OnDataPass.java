package com.matt.shopline.fragments.register;

import android.net.Uri;

public interface OnDataPass {

    void DataStep1(String data, Uri uri);

    void DataStep2(String[] data);

    void DataStep3(String[] data);

    void DataStep4(String[] data);
}
