package world.picpic.www.bbac;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONObject;

import world.picpic.www.bbac.common.BaseActivity;
import world.picpic.www.bbac.common.Url;
import world.picpic.www.bbac.fragments.HomeFragment;
import world.picpic.www.bbac.fragments.MsgListFragment;
import world.picpic.www.bbac.util.CommonUtil;
import world.picpic.www.bbac.util.NetworkThreadTask;

public class HomeActivity extends BaseActivity implements NetworkThreadTask.OnCompleteListener {
    private Button btnMy;
    public TextView tvFragmentTitle, tvMessageCount;
    private ImageView ivFragmentTitle;
    private HomeFragment homeFragment;
    private MsgListFragment msgListFragment;

    private final int REQ_PHONE_BOOK = 101;
    private final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 10;
    private final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 11;
    private final int REQ_CODE_REGISTER = 12;
    private final int REQ_CODE_GET_MESSAGE_COUNT = 13;
    private final int REQ_CODE_GET_GOOGLE_PLAY_ADDRESS = 14;
    private String fromSeq = null;
    private String msgHint = null;
    private String googlePlayAddress = "";
    private boolean doubleBackToExitPressedOnce = false;
    MyBroadcastReceiver mReceiver = null;
    boolean mIsReceiverRegistered = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);
        homeFragment = new HomeFragment();
        msgListFragment = new MsgListFragment();

        btnMy = (Button) findViewById(R.id.btnMy);
        tvFragmentTitle = (TextView) findViewById(R.id.tvFragmentTitle);
        tvMessageCount = (TextView) findViewById(R.id.tvMessageCount);
        ivFragmentTitle = (ImageView) findViewById(R.id.ivFragmentTitle);

        initFragment();
        initTitle();

        checkRegistUserInfo();
        requestMsgCount();
        requestGooglePlayAddress();

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey("isFromNoti")) {
            if (extras.getBoolean("isFromNoti")) {
                transitFragment(msgListFragment, true);
            }
        }
        View.OnClickListener onTitleClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fragmentTag = getSupportFragmentManager().findFragmentById(R.id.mainFragment).toString();
                if (fragmentTag.startsWith(getResources().getString(R.string.tag_msg_list))) {
                    ((MsgListFragment) getSupportFragmentManager().findFragmentById(R.id.mainFragment)).setMsgType();
                }
            }
        };
        tvFragmentTitle.setOnClickListener(onTitleClickListener);
        ivFragmentTitle.setOnClickListener(onTitleClickListener);
    }

    @Override
    public void onNewIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.containsKey("isFromNoti")) {
                if (extras.getBoolean("isFromNoti")) {
                    String fragmentTag = getSupportFragmentManager().findFragmentById(R.id.mainFragment).toString();
                    if (fragmentTag.startsWith(getResources().getString(R.string.tag_msg_list))) {
//                        msgListFragment.getMsgList();
                    } else if (fragmentTag.startsWith(getResources().getString(R.string.tag_home))) {
                        transitFragment(msgListFragment, true);
                    }
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mIsReceiverRegistered) {
            if (mReceiver == null)
                mReceiver = new MyBroadcastReceiver();
            registerReceiver(mReceiver, new IntentFilter("fromPush"));
            mIsReceiverRegistered = true;
        }
    }

    @Override
    protected void onPause() {
        if (mIsReceiverRegistered) {
            super.onPause();
            unregisterReceiver(mReceiver);
            mReceiver = null;
            mIsReceiverRegistered = false;
        }
    }

    public void initTitle() {
        btnMy.setBackground(getResources().getDrawable(R.drawable.icon_message_list));

        findViewById(R.id.layoutTitle).setOnClickListener(onOutsideClickListnener);
        findViewById(R.id.layoutTitle).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    hideSoftKeyboard(HomeActivity.this);
            }
        });


    }

    public void initFragment() {
        btnMy.setBackground(getResources().getDrawable(R.drawable.icon_message_list));
        tvFragmentTitle.setText(getString(R.string.title_btn_home));
        ivFragmentTitle.setVisibility(View.GONE);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.mainFragment, homeFragment);
        fragmentTransaction.commit();
    }

    public void transitFragment(Fragment fragment, boolean isHome) {
        Bundle bundle = new Bundle();


        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (isHome) {
            btnMy.setBackground(getResources().getDrawable(R.drawable.btn_setting));
            tvFragmentTitle.setText(getString(R.string.title_msg_list));
            ivFragmentTitle.setVisibility(View.VISIBLE);
            tvMessageCount.setVisibility(View.GONE);
            fragmentTransaction.setCustomAnimations(R.anim.animation_from_right, R.anim.animation_to_left);
            fromSeq = null;
            msgHint = null;

            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "화면 전환 : " + getString(R.string.title_msg_list));

        } else {
            btnMy.setBackground(getResources().getDrawable(R.drawable.icon_message_list));
            tvFragmentTitle.setText(getString(R.string.title_btn_home));
            ivFragmentTitle.setVisibility(View.GONE);
            requestMsgCount();
            fragmentTransaction.setCustomAnimations(R.anim.animation_from_left, R.anim.animation_to_right);

            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "화면 전환 : " + getString(R.string.title_btn_home));
        }

        fragmentTransaction.replace(R.id.mainFragment, fragment);
        fragmentTransaction.commitAllowingStateLoss();

        hideSoftKeyboard(this);

        getFirebaseAnalytics().logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    public void transitFragmentByReply(Fragment fragment, String seq, String msg) {
        btnMy.setBackground(getResources().getDrawable(R.drawable.icon_message_list));
        tvFragmentTitle.setText(getString(R.string.title_btn_home));
        ivFragmentTitle.setVisibility(View.GONE);
        requestMsgCount();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.setCustomAnimations(R.anim.animation_from_left, R.anim.animation_to_right);
        fragmentTransaction.replace(R.id.mainFragment, fragment);
        fragmentTransaction.commitAllowingStateLoss();

        fromSeq = seq;
        msgHint = msg;

        hideSoftKeyboard(this);

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "답장으로 인한 화면 전환 : " + getString(R.string.title_btn_home));
        getFirebaseAnalytics().logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_PHONE_BOOK) {
            super.onActivityResult(requestCode, resultCode, data);

            if (resultCode == RESULT_OK) {
                Uri contactData = data.getData();
                Cursor cursor = getContentResolver().query(contactData, null, null, null, null);
                cursor.moveToFirst();

                String id =cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                String hasPhone =cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String number = "";
                if (hasPhone.equalsIgnoreCase("1")) {
                    Cursor phones = getContentResolver().query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ id,
                            null, null);
                    phones.moveToFirst();
                    number = phones.getString(phones.getColumnIndex("data1"));
                }

                ((HomeFragment) getSupportFragmentManager().findFragmentById(R.id.mainFragment)).getEditPhoneNo().setText(CommonUtil.formatMessageTargetNameAndNumber(name, number));
                ((HomeFragment) getSupportFragmentManager().findFragmentById(R.id.mainFragment)).getEditPhoneNo().setTag(number);
            }
        }
    }

    View.OnClickListener onOutsideClickListnener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String fragmentTag = getSupportFragmentManager().findFragmentById(R.id.mainFragment).toString();
            if (fragmentTag.startsWith(getResources().getString(R.string.tag_home))) {
                ((HomeFragment) getSupportFragmentManager().findFragmentById(R.id.mainFragment)).getEditPhoneNo().clearFocus();
                ((HomeFragment) getSupportFragmentManager().findFragmentById(R.id.mainFragment)).getEditMessage().clearFocus();
            }
        }
    };



    @Override
    public void onBackPressed() {
        String fragmentTag = getSupportFragmentManager().findFragmentById(R.id.mainFragment).toString();
        if (fragmentTag.startsWith(getResources().getString(R.string.tag_msg_list))) {
            ((MsgListFragment) getSupportFragmentManager().findFragmentById(R.id.mainFragment)).cancelDelete();
            transitFragment(homeFragment, false);
        } else if (fragmentTag.startsWith(getResources().getString(R.string.tag_home))) {
            if("".equals(getFromSeq())) {
                if (doubleBackToExitPressedOnce) {
                    super.onBackPressed();
                    return;
                }
                doubleBackToExitPressedOnce = true;
                Toast.makeText(this, getString(R.string.toast_back_again), Toast.LENGTH_LONG).show();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce = false;
                    }
                }, 2000);
            } else {
                transitFragment(msgListFragment, true);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_PHONE_STATE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    registerUserInfo();
                }
                break;

            case MY_PERMISSIONS_REQUEST_READ_CONTACTS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ((HomeFragment) getSupportFragmentManager().findFragmentById(R.id.mainFragment)).phoneBook();
                }
                break;
        }
    }

    @Override
    public void onSuccess(int requestCode, String responseText) {

        if (requestCode == REQ_CODE_REGISTER) {
            Log.v("KWS", "Data inserted successfully. Send successfull.");
            String fragmentTag = getSupportFragmentManager().findFragmentById(R.id.mainFragment).toString();
            if (fragmentTag.startsWith(getResources().getString(R.string.tag_msg_list))) {
                ((MsgListFragment) getSupportFragmentManager().findFragmentById(R.id.mainFragment)).getMsgList();
            } else {
                requestMsgCount();
            }
        } else if (requestCode == REQ_CODE_GET_GOOGLE_PLAY_ADDRESS) {
            googlePlayAddress = responseText;
        } else if (requestCode == REQ_CODE_GET_MESSAGE_COUNT) {

            try {
                JSONArray jsonArray = new JSONArray(responseText);
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                String totalMessageCount = jsonObject.getString("total");

                CommonUtil.setBadgeCount(getApplicationContext(),  totalMessageCount);
                setBadgeCount(totalMessageCount);

                String fragmentTag = getSupportFragmentManager().findFragmentById(R.id.mainFragment).toString();
                if (fragmentTag.startsWith(getResources().getString(R.string.tag_home))) {
                    if (Integer.parseInt(totalMessageCount) == 0) {
                        tvMessageCount.setVisibility(View.GONE);
                    } else {
                        tvMessageCount.setVisibility(View.VISIBLE);
                        if(Integer.parseInt(totalMessageCount) < 100)
                            tvMessageCount.setText(totalMessageCount);
                        else
                            tvMessageCount.setText("99");

                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                onFailure(requestCode, responseText);
            }
        } else {
            Log.v("KWS", "SUCCESS but Data inserted unsuccessfully.");
            transitFragment(homeFragment, false);
        }
    }

    @Override
    public void onFailure(int requestCode, String responseText) {
        Log.v("KWS", "Data inserted fail");
        Toast.makeText(this, getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
    }

    private void checkRegistUserInfo() {
        if ("".equals(CommonUtil.getUserPhoneNo(this))) {
            showDialog(R.string.alert_msg_register, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ContextCompat.checkSelfPermission(HomeActivity.this, android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                        registerUserInfo();
                    } else {
                        ActivityCompat.requestPermissions(HomeActivity.this, new String[]{android.Manifest.permission.READ_PHONE_STATE}, MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
                    }
                }
            }, null);
        }
    }

    public void registerUserInfo() {
        final TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        final String phoneNo = telephonyManager.getLine1Number();
        String editedPhoneNo = phoneNo;
        if (phoneNo.contains("+")) {
            editedPhoneNo = phoneNo.substring(3);
            editedPhoneNo = "0" + editedPhoneNo;
        }
        CommonUtil.setUserPhoneNo(this, editedPhoneNo);

        Bundle param = new Bundle();
        param.putInt("reqCode", REQ_CODE_REGISTER);
        param.putString("url", Url.REGISTER);
        param.putString("phoneNo", editedPhoneNo);
        param.putString("token", CommonUtil.getUserFCMToken(this));

        NetworkThreadTask mTask = new NetworkThreadTask(HomeActivity.this, true);
        mTask.setOnCompleteListener(HomeActivity.this);
        mTask.execute(param);
    }

    public String getFromSeq() {
        if (fromSeq != null)
            return fromSeq;
        else
            return "";
    }

    public String getMsgHint() {
        if (msgHint != null)
            return msgHint;
        else
            return "";
    }

    private void setBadgeCount(String strCount) {
        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
        intent.putExtra("badge_count_package_name", getComponentName().getPackageName());
        intent.putExtra("badge_count_class_name", Splash.class.getName());
        intent.putExtra("badge_count", Integer.parseInt(strCount));
        sendBroadcast(intent);
    }

    private void requestMsgCount() {
        Bundle bundle = new Bundle();
        bundle.putString("url", Url.GET_MESSAGE_COUNT);
        bundle.putInt("reqCode", REQ_CODE_GET_MESSAGE_COUNT);
        bundle.putString("phoneNo", CommonUtil.getUserPhoneNo(this));

        NetworkThreadTask mTask = new NetworkThreadTask(this, false);
        mTask.setOnCompleteListener(this);
        mTask.execute(bundle);
    }

    private void requestGooglePlayAddress() {
        Bundle bundle = new Bundle();
        bundle.putString("url", Url.GET_GOOGLE_PLAY_ADDRESS);
        bundle.putInt("reqCode", REQ_CODE_GET_GOOGLE_PLAY_ADDRESS);

        NetworkThreadTask mTask = new NetworkThreadTask(this, false);
        mTask.setOnCompleteListener(this);
        mTask.execute(bundle);
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            requestMsgCount();
            String fragmentTag = getSupportFragmentManager().findFragmentById(R.id.mainFragment).toString();
            if (fragmentTag.startsWith(getResources().getString(R.string.tag_msg_list))) {
                msgListFragment.getMsgList();
            }
        }
    }

    public String getGooglePlayAddress() {
        return googlePlayAddress;
    }
}
