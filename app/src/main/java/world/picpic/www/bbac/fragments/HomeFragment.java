package world.picpic.www.bbac.fragments;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.StringTokenizer;
import world.picpic.www.bbac.HomeActivity;
import world.picpic.www.bbac.R;
import world.picpic.www.bbac.common.BaseActivity;
import world.picpic.www.bbac.common.ResultCd;
import world.picpic.www.bbac.common.Url;
import world.picpic.www.bbac.util.BackPressEditText;
import world.picpic.www.bbac.util.CommonUtil;
import world.picpic.www.bbac.util.NetworkThreadTask;

public class HomeFragment extends Fragment implements View.OnClickListener, NetworkThreadTask.OnCompleteListener {
    private final int REQ_PHONE_BOOK = 101;
    private Activity context;
    private View fragmentView;
    private TextView tvReciever;
    private BackPressEditText editPhoneNo, editMessage;
    private LinearLayout llSendOptions;
    private ImageButton btnHome, btnMy;
    private Button btnSendMessage, btnPhoneNo, spinnerSendType;
    private String editedPhoneNo = "";
    private final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 10;
    private final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 11;
    private final int REQ_CODE_REGISTER         = 12;
    private final int REQ_CODE_SEND_MESSAGE     = 13;
    private final int REQ_CODE_REPLY_MESSAGE    = 14;
    private final int REQ_CODE_SEND_SMS         = 15;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_home, container, false);

        btnMy = (ImageButton) context.findViewById(R.id.btnMy);
        tvReciever = (TextView) fragmentView.findViewById(R.id.tvReciever);
        editPhoneNo = (BackPressEditText) fragmentView.findViewById(R.id.editPhoneNo);
        editMessage = (BackPressEditText) fragmentView.findViewById(R.id.editMessage);
        btnHome = (ImageButton) context.findViewById(R.id.btnHome);
        btnPhoneNo = (Button) fragmentView.findViewById(R.id.btnPhoneNo);
        btnSendMessage = (Button) context.findViewById(R.id.btnSendMessage);
        spinnerSendType = (Button) context.findViewById(R.id.spinnerSendType);
        llSendOptions = (LinearLayout) context.findViewById(R.id.llSendOptions);

        Typeface myTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/NanumGothic.otf");
        editPhoneNo.setText("");
        editMessage.setText("");
        editPhoneNo.setTypeface(myTypeface);
        editMessage.setTypeface(myTypeface);

        llSendOptions.setVisibility(View.VISIBLE);

        context.findViewById(R.id.layoutTitle).setBackgroundResource(R.color.colorMint);
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = context.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(context, R.color.colorMint));
        }

        if("".equals( ((HomeActivity)context).getFromSeq()))  {
            editPhoneNo.setEnabled(true);
            tvReciever.setVisibility(View.VISIBLE);
            editPhoneNo.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    String phoneNo = editPhoneNo.getText().toString();
                    if (hasFocus) {
                        if (phoneNo.contains("-")) {
                            if (editPhoneNo.getTag() != null && phoneNo.contains("<"))
                                phoneNo = editPhoneNo.getTag().toString();
                            StringTokenizer tokenizer = new StringTokenizer(phoneNo, "-");
                            String editedPhoneNo = "";
                            while (tokenizer.hasMoreTokens()) {
                                String temp = tokenizer.nextToken();
                                editedPhoneNo += temp;
                            }
                            editPhoneNo.setText(editedPhoneNo);
                        } else if (editPhoneNo.getTag() != null && phoneNo.contains("<")) {
                            phoneNo = editPhoneNo.getTag().toString();
                            editPhoneNo.setText(phoneNo);
                        }
                    } else {
                        setEditPhoneNo(phoneNo);

                    }

                }
            });


            editPhoneNo.setOnBackPressListener(onBackPressListener);

            btnPhoneNo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((BaseActivity) context).hideSoftKeyboard(context);
                    if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                        phoneBook();
                    } else {
                        ActivityCompat.requestPermissions(context, new String[]{android.Manifest.permission.READ_CONTACTS}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                    }

                }
            });
        } else {
            editPhoneNo.setText(getResources().getString(R.string.phone_no_reply));
            editPhoneNo.setGravity(Gravity.CENTER);
            editPhoneNo.setEnabled(false);
            tvReciever.setVisibility(View.GONE);
            editMessage.setHint(((HomeActivity)context).getMsgHint());
            btnPhoneNo.setVisibility(View.GONE);
        }

        editMessage.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    editMessage.setMaxLines(7);
                } else {
                    editMessage.setMaxLines(300);
                }
            }
        });
        editMessage.setOnBackPressListener(onBackPressListener);

        View.OnClickListener sendOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if ("".equals( ((HomeActivity)context).getFromSeq())
                && "".equals(editPhoneNo.getText().toString().trim())) {
                    ((BaseActivity) context).showAlert(R.string.alert_msg_to_whom);
                } else if ("".equals( ((HomeActivity)context).getFromSeq())
                &&PhoneNumberUtils.isEmergencyNumber(editPhoneNo.getText().toString().trim())) {
                    ((BaseActivity) context).showAlert(R.string.alert_msg_to_emergency);
                } else if ("".equals(editMessage.getText().toString())) {
                    ((BaseActivity) context).showAlert(R.string.alert_msg_no_message);
                } else {
                    String phoneNo = editPhoneNo.getText().toString().trim();


                    if("".equals(CommonUtil.getUserPhoneNo(context))) {
                        ((BaseActivity)context).showDialog(R.string.alert_msg_register, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                                    registerUserInfo();
                                } else {
                                    ActivityCompat.requestPermissions(context, new String[]{android.Manifest.permission.READ_PHONE_STATE}, MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
                                }
                            }
                        }, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                return;
                            }
                        });
                    } else {
                        if("".equals( ((HomeActivity)context).getFromSeq())) {
                            if (phoneNo.contains("<")) {
                                if(editPhoneNo.getTag() != null && !"".equals(editPhoneNo.getTag().toString()))
                                    phoneNo = editPhoneNo.getTag().toString();
                                else
                                    phoneNo = phoneNo.substring(phoneNo.indexOf('<') + 1, phoneNo.indexOf('>'));
                            }
                            if (phoneNo.contains("-")) {
                                StringTokenizer tokenizer = new StringTokenizer(phoneNo, "-");
                                editedPhoneNo = "";
                                while (tokenizer.hasMoreTokens()) {
                                    String temp = tokenizer.nextToken();
                                    editedPhoneNo += temp;
                                }
                            } else {
                                editedPhoneNo = phoneNo;
                            }
                        } else {
                            editedPhoneNo = ((HomeActivity)context).getFromSeq();
                        }

                        Bundle param = new Bundle();
                        if("".equals( ((HomeActivity)context).getFromSeq())) {
                            param.putInt("reqCode", REQ_CODE_SEND_MESSAGE);
                            param.putString("url", Url.SEND_MESSAGE);
                        } else {
                            param.putInt("reqCode", REQ_CODE_REPLY_MESSAGE);
                            param.putString("url", Url.REPLY_MESSAGE);
                            param.putString("isReply", ((HomeActivity)context).getReplySeq());
                        }
                        param.putString("fromWhom", CommonUtil.getUserPhoneNo(context));
                        param.putString("phoneNo", editedPhoneNo);
                        param.putString("message", editMessage.getText().toString());

                        NetworkThreadTask mTask = new NetworkThreadTask(context, true, getResources().getString(R.string.sending));
                        mTask.setOnCompleteListener(HomeFragment.this);
                        mTask.execute(param);
                    }


                }
            }
        };
        btnSendMessage.setOnClickListener(sendOnClickListener);


        View.OnClickListener onOutsideClickListnener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editMessage.clearFocus();
                editPhoneNo.clearFocus();
            }
        };
        spinnerSendType.setOnClickListener(this);
        if(CommonUtil.getIsWithSms(context))
            spinnerSendType.setText(getResources().getString(R.string.home_spinner_type_app_with_sms));
        else
            spinnerSendType.setText(getResources().getString(R.string.home_spinner_type_app));

        fragmentView.findViewById(R.id.layoutHome).setOnClickListener(onOutsideClickListnener);
        fragmentView.findViewById(R.id.layoutHome).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus)
                    ((BaseActivity) context).hideSoftKeyboard(context);
            }
        });


        btnMy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((HomeActivity)context).transitFragment(new MsgListFragment(), true);
            }
        });

        btnHome.setVisibility(View.GONE);

        return fragmentView;
    }

    @Override
    public void onAttach(final Context activity) {
        super.onAttach(activity);
        context = (Activity) activity;
    }

    public void phoneBook() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        context.startActivityForResult(intent, REQ_PHONE_BOOK);
    }

    @Override
    public void onClick(View v) {
        editMessage.clearFocus();
        editPhoneNo.clearFocus();
        if (R.id.spinnerSendType == v.getId()) {
            ((BaseActivity) context).showSelectDialogWithRadioButton(R.string.home_spinner_type_app_with_sms, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    spinnerSendType.setText(getResources().getString(R.string.home_spinner_type_app_with_sms));
                    CommonUtil.setIsWithSms(context, true);
                }
            }, R.string.home_spinner_type_app, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    spinnerSendType.setText(getResources().getString(R.string.home_spinner_type_app));
                    CommonUtil.setIsWithSms(context, false);
                }
            }, CommonUtil.getIsWithSms(context));
        }
    }

    public static String getContactName(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) {
            return null;
        }
        String contactName = "";
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        return contactName;
    }

    public EditText getEditPhoneNo() {
        return editPhoneNo;
    }

    public EditText getEditMessage() {
        return editMessage;
    }

    public void registerUserInfo() {
        final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        final String phoneNo = telephonyManager.getLine1Number();
        String editedPhoneNo = phoneNo;
        if(phoneNo.contains("+")) {
            editedPhoneNo = phoneNo.substring(3);
            editedPhoneNo = "0" + editedPhoneNo;
        }
        CommonUtil.setUserPhoneNo(context, editedPhoneNo);

        Bundle param = new Bundle();
        param.putInt("reqCode", REQ_CODE_REGISTER);
        param.putString("url", Url.REGISTER);
        param.putString("phoneNo", editedPhoneNo);
        param.putString("token", CommonUtil.getUserFCMToken(context));

        NetworkThreadTask mTask = new NetworkThreadTask(context, true);
        mTask.setOnCompleteListener(this);
        mTask.execute(param);
    }

    private void setEditPhoneNo(String phoneNo) {
        String name = "";
        if (!"".equals(phoneNo) && !phoneNo.contains("<")) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                name = getContactName(context, phoneNo);
            }
        }

        if ("".equals(name)) {
            if(!editPhoneNo.getText().toString().contains("-"))
                editPhoneNo.setText(PhoneNumberUtils.formatNumber(phoneNo));
        }
        else {
            if (!phoneNo.contains("<"))
                editPhoneNo.setText(CommonUtil.formatMessageTargetNameAndNumber(name, PhoneNumberUtils.formatNumber(phoneNo)));
            else
                editPhoneNo.setText(PhoneNumberUtils.formatNumber(phoneNo));
            editPhoneNo.setTag(PhoneNumberUtils.formatNumber(phoneNo));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch(requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_PHONE_STATE :
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    registerUserInfo();
                }
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        String phoneNo = editPhoneNo.getText().toString();
        if (phoneNo.contains("<")) {
            phoneNo = phoneNo.substring(phoneNo.indexOf('<') + 1, phoneNo.indexOf('>'));
        }
        StringTokenizer tokenizer = new StringTokenizer(phoneNo, "-");
        String editedPhoneNo = "";
        while (tokenizer.hasMoreTokens()) {
            String temp = tokenizer.nextToken();
            editedPhoneNo += temp;
        }

        editPhoneNo.setTag(editedPhoneNo);

    }

    @Override
    public void onSuccess(int requestCd, String responseText) {

        JSONObject jsonObject = null;
        String resultCd = "";

        try {
            jsonObject = new JSONObject(responseText);
            resultCd = jsonObject.getString("resultCd");

            if(ResultCd.SUCCESS.equals(resultCd)) {
                if (requestCd == REQ_CODE_SEND_MESSAGE) {
                    if (CommonUtil.getIsWithSms(context)) {
                        sendSms();
                    } else {
                        showMessageAlert(getResources().getString(R.string.alert_msg_send_complete));
                    }
                } else if (requestCd == REQ_CODE_REPLY_MESSAGE) {
                    if (CommonUtil.getIsWithSms(context)) {
                        replySms();
                    } else {
                        showMessageAlert(getResources().getString(R.string.alert_msg_send_complete));
                    }
                }
            } else if(ResultCd.SUCCESS_WITH_ALERT.equals(resultCd)) {
                if (requestCd == REQ_CODE_SEND_SMS) {
                    String alertMsg = jsonObject.getString("alertMsg");
                    showMessageAlert(alertMsg);
                }
            }
        } catch (Exception e) {
            onFailure(requestCd, responseText);
        }
    }

    @Override
    public void onFailure(int requestCd, String responseText) {
        Toast.makeText(context, getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
    }

    private BackPressEditText.OnBackPressListener onBackPressListener = new BackPressEditText.OnBackPressListener()
    {
        @Override
        public void onBackPress()
        {
            editMessage.setMaxLines(300);
            editMessage.clearFocus();
        }
    };

    private String getSmsMsg() {
        String msg = editMessage.getText().toString();
        if(msg.length() > 25) {
            msg = msg.substring(0, 23) + "...";
        }
        msg += getString(R.string.postfix_sms);
        msg += ((HomeActivity)context).getGooglePlayAddress();

        return msg;
    }

    private void sendSms() {
        Bundle param = new Bundle();
        param.putInt("reqCode", REQ_CODE_SEND_SMS);
        param.putString("url", Url.SEND_SMS);
        param.putString("fromWhom", CommonUtil.getUserPhoneNo(context));
        param.putString("phoneNo", editedPhoneNo);
        param.putString("msg", getSmsMsg());


        NetworkThreadTask mTask = new NetworkThreadTask(context, true, getResources().getString(R.string.sending_sms));
        mTask.setOnCompleteListener(this);
        mTask.execute(param);
    }

    private void replySms() {
        Bundle param = new Bundle();
        param.putInt("reqCode", REQ_CODE_SEND_SMS);
        param.putString("url", Url.REPLY_SMS);
        param.putString("fromWhom", CommonUtil.getUserPhoneNo(context));
        param.putString("phoneNo", editedPhoneNo);
        param.putString("msg", getSmsMsg());


        NetworkThreadTask mTask = new NetworkThreadTask(context, true, getResources().getString(R.string.sending_sms));
        mTask.setOnCompleteListener(this);
        mTask.execute(param);
    }

    private void showMessageAlert(String message) {
        ((BaseActivity) context).showAlert(message,
                R.string.alert_btn_ok,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if("".equals(((HomeActivity)context).getFromSeq())) {
                            editPhoneNo.setText("");
                            editMessage.setText("");
                        } else {
                            ((HomeActivity) context).transitFragment(new MsgListFragment(), true);
                        }
                    }
                });
    }
}
