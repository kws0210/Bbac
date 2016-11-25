package world.picpic.www.bbac.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;

import org.apache.http.NameValuePair;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.util.List;

import world.picpic.www.bbac.R;
import world.picpic.www.bbac.common.ResultCd;

/**
 * Created by Wonseob on 2016. 8. 15..
 */
public class NetworkThreadTask extends AsyncTask<Bundle, Bundle, Void> {
    private Context context;
    private ServerUtil serverUtil;
    private OnCompleteListener onCompleteListener = null;
    private ProgressDialog pd;
    private Typeface tf;

    public NetworkThreadTask() {}
    public NetworkThreadTask(Context context, boolean useProgress) {
        this.context = context;

        if(useProgress) {
            pd = new ProgressDialog(context, R.style.ProgressDialogTheme);
            pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pd.setIndeterminate(true);
            pd.setMessage(Html.fromHtml("<font color='white'>"
                    + context.getString(R.string.loding) + "</font>"));
            pd.setCancelable(false);
            pd.setCanceledOnTouchOutside(false);
            pd.hide();
        } else {
            pd = null;
        }


    }

    public interface OnCompleteListener {
        public void onSuccess(int requestCode, String responseText);

        public void onFailure(int requestCode, String responseText);
    }

    public void setOnCompleteListener(OnCompleteListener listener) {
        this.onCompleteListener = listener;
    }

    protected void onPreExecute() {
        if(context != null && pd != null)
            pd.show();
    }

    @Override
    protected Void doInBackground(Bundle... params) {
        int reqCode = 0;
        JSONObject result = null;
        String url = "";
        String response = "";
        BufferedReader bufferedReader;
        List<NameValuePair> paramList = null;

        reqCode = params[0].getInt("reqCode");
        params[0].remove("reqCode");

        try {
            serverUtil = new ServerUtil();
            response = serverUtil.serverInterface( (Bundle) params[0].clone() );

        } catch (Exception e) {
            e.printStackTrace();
        }

        Bundle res = new Bundle();
        res.putInt("reqCode", reqCode);
        res.putString("response", response);

        publishProgress(res);
        return null;
    }

    @Override
    protected void onProgressUpdate(Bundle... response) {
        int reqCode = response[0].getInt("reqCode");
        String responseStr = response[0].getString("response");
        JSONObject jsonObject = null;
        String resultCd = "";

        if (context != null && pd != null)
            pd.dismiss();

        try {
            jsonObject = new JSONObject(responseStr);
            resultCd = jsonObject.getString("resultCd");

            if (ResultCd.FAILURE.equals(resultCd)) {
                onCompleteListener.onFailure(reqCode, responseStr);
            } else {
                onCompleteListener.onSuccess(reqCode, responseStr);
            }
        } catch (Exception e) {
            e.printStackTrace();
            onCompleteListener.onFailure(reqCode, responseStr);
        }
    }
}
