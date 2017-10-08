package com.msl.utaastu.Utils;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;

/**
 * Created by Malek Shefat on 6/20/2017.
 */

public class ShareImage extends AsyncTask<Void, Void, Integer> {

    private RecyclerView rv;
    private Context context;

    public ShareImage(Context context, RecyclerView recyclerView) {
        this.context = context;
        this.rv = recyclerView;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        ScreenshotHelper.saveBitmapToCache(context, rv);
        if (isCancelled())
            return 0;
        return 1;
    }

    @Override
    protected void onPostExecute(Integer status) {
        super.onPostExecute(status);
        if (status == 1)
            ScreenshotHelper.shareImage(context);
    }
}