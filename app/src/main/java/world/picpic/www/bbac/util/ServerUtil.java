package world.picpic.www.bbac.util;

import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.util.Log;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wonseob on 2016. 8. 16..
 */
public class ServerUtil {

    private static boolean e2eEncryptEnabled = true;

    public static HttpClient httpClient = null;
    public static HttpContext httpContext = new BasicHttpContext();
    public static CookieStore cookieStore = new BasicCookieStore();

    static {
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
    }

    private static Base64 base64 = new Base64();

    public ServerUtil(){}


    public static HttpClient getClientInstance() {
        if (httpClient == null) {
            httpClient = AndroidHttpClient.newInstance("ANDROID");
        }
        return httpClient;
    }

    public synchronized String serverInterface(Bundle params) {
        String url = params.getString("url");

        Log.d("ServerUtil", "request[" + params.toString() + "]");


        List<NameValuePair> paramList = null;

        e2eEncryptEnabled = true;

        //base64 encode
        base64Encoding(params);

        paramList = bundleToArrayList(params);


        HttpResponse response;

        try {
            //request
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(new UrlEncodedFormEntity(paramList, "UTF-8"));

            response = getClientInstance().execute(httpPost, httpContext);

            if (response.getEntity() != null) {
                String rslt = EntityUtils.toString(response.getEntity());
                Log.d("ServerUtil", "response[" + rslt + "]");


                return rslt;
            }

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    private void base64Encoding(Bundle params) {
        if (e2eEncryptEnabled) {
            for (String key : params.keySet()) {
                try {
//                    params.putString(key, new String(base64.encode(params.getString(key).getBytes()), "UTF-8"));
                    params.putString( key, params.getString(key) );
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private ArrayList<NameValuePair> bundleToArrayList(Bundle params) {
        ArrayList<NameValuePair> rtn = new ArrayList<NameValuePair>();

        for (String key : params.keySet()) {
            try {
                rtn.add(new BasicNameValuePair(key, params.getString(key)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return rtn;
    }

    private String bundleToString(Bundle params) {
        String strRtn = "";
        int index = 0;
        for (String key : params.keySet()) {
            if (index != 0) strRtn += "&";
            strRtn += key + "=" + params.getString(key);
            index++;
        }
        return strRtn;
    }
}
