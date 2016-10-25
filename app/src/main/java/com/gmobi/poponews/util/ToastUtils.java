package com.gmobi.poponews.util;

import android.widget.Toast;

import com.momock.app.App;

/**
 * User: vivian .
 * Date: 2016-07-29
 * Time: 13:47
 */
public class ToastUtils {
    public static void showShortToast(String toast){
        Toast.makeText(App.get().getCurrentContext(),toast,Toast.LENGTH_SHORT).show();
    }

    public static void showLongToast(String toast){
        Toast.makeText(App.get().getCurrentContext(),toast,Toast.LENGTH_LONG).show();
    }
}
