package com.qboxus.musictok.Interfaces;

import java.util.ArrayList;

/**
 * Created by qboxus on 3/4/2019.
 */

public interface APICallBack {

    void arrayData(ArrayList arrayList);

    void onSuccess(String responce);

    void onFail(String responce);


}
