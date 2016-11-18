/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package world.picpic.www.bbac;

import android.content.Context;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import world.picpic.www.bbac.common.Url;
import world.picpic.www.bbac.fragments.MsgListFragment;
import world.picpic.www.bbac.util.CommonUtil;
import world.picpic.www.bbac.util.NetworkThreadTask;


public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService implements NetworkThreadTask.OnCompleteListener{
    private final int REQ_CODE_REGISTER = 12;
    private static final String TAG = "MyFirebaseIIDService";

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        saveToken(refreshedToken);
    }
    // [END refresh_token]

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void saveToken(String token) {
        //update token
        CommonUtil.setUserFCMToken(this, token);
        registerUserInfo();
    }

    public void registerUserInfo() {
        final TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        final String phoneNo = telephonyManager.getLine1Number();
        String editedPhoneNo = phoneNo;
        if(phoneNo.contains("+")) {
            editedPhoneNo = phoneNo.substring(3);
            editedPhoneNo = "0" + editedPhoneNo;
        }
        CommonUtil.setUserPhoneNo(this, editedPhoneNo);

        Bundle param = new Bundle();
        param.putInt("reqCode", REQ_CODE_REGISTER);
        param.putString("url", Url.REGISTER);
        param.putString("phoneNo", editedPhoneNo);
        param.putString("token", CommonUtil.getUserFCMToken(this));

        NetworkThreadTask mTask = new NetworkThreadTask();
        mTask.setOnCompleteListener(this);
        mTask.execute(param);
    }

    @Override
    public void onSuccess(int requestCode, String responseText) {

        if (requestCode == REQ_CODE_REGISTER) {
            Log.v("KWS", "Data inserted successfully. Send successfull.");
        } else {
            Log.v("KWS", "SUCCESS but Data inserted unsuccessfully.");
        }
    }

    @Override
    public void onFailure(int requestCode, String responseText) {
        Log.v("KWS", "Data inserted fail");
        Toast.makeText (this, getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
    }
}