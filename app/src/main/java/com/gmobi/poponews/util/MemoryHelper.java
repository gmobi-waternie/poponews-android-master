package com.gmobi.poponews.util;

import android.graphics.Bitmap;

import com.momock.util.Logger;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 10/29 0029.
 */
public class MemoryHelper {
    public static Map<String, SoftReference<Bitmap>> imageCache = new HashMap();

    public static void addBitmap(String uri, Bitmap bmp)
    {
        imageCache.put(uri,new SoftReference<Bitmap>(bmp));
    }

    public static Bitmap getBitmapFromMem(String uri)
    {
        if (imageCache.containsKey(uri)) {
            SoftReference<Bitmap> softReference = imageCache.get(uri);
            if (softReference.get() != null) {
                Logger.error("Hit Cache!" + uri);
                return softReference.get();
            }
        }
        return null;
    }

}
