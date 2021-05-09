package com.example.assignment1mc;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.example.assignment1mc.utils.VideoToHeartRateProcessing;

/**
 * @author Wenzhe Zheng
 * @version 1.0.0
 * @date 2021/2/19
 * @description
 */
public class MyAsyncTask extends AsyncTask<Void, Void, Float> {

    private static final String TAG = "MyAsyncTask";

    @SuppressLint("StaticFieldLeak")
    private final Context context;

    private ProgressDialog progressDialog;

    private RequestResultCallback requestResultCallback;

    public MyAsyncTask(Context context) {
        this.context = context;
    }

    public void setRequestResultCallback(RequestResultCallback requestResultCallback) {
        this.requestResultCallback = requestResultCallback;
    }

    interface RequestResultCallback {
        void onCallback(Float result);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.d(TAG, "onPreExecute: ");

        if (progressDialog == null) {
            progressDialog = new ProgressDialog(context);
        }
        progressDialog.setMessage("Calculating");
        progressDialog.show();
    }

    @Override
    protected Float doInBackground(Void... voids) {
        float hr = 0;

        try {
            hr = VideoToHeartRateProcessing.processing();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Calaulate fail：" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        return hr;
    }

    @Override
    protected void onPostExecute(Float s) {
        super.onPostExecute(s);
        Log.d(TAG, "onPostExecute: result -> " + s);

        progressDialog.dismiss();

        if (requestResultCallback != null) {
            requestResultCallback.onCallback(s);
        }
    }

    @Override
    protected void onCancelled(Float s) {
        super.onCancelled(s);
        Log.d(TAG, "onCancelled: result -> " + s);

        progressDialog.dismiss();
    }

}
