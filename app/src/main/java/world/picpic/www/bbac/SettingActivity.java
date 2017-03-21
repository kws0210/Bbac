package world.picpic.www.bbac;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import world.picpic.www.bbac.common.ResultCd;
import world.picpic.www.bbac.common.Url;
import world.picpic.www.bbac.util.CommonUtil;
import world.picpic.www.bbac.util.NetworkThreadTask;

public class SettingActivity extends Activity implements View.OnClickListener, NetworkThreadTask.OnCompleteListener{

    private final int REQ_CODE_GET_RECEIVE_SMS = 23;
    private final int REQ_CODE_UDDATE_RECEIVE_SMS = 24;
    private TextView tvReceiveSmsSub;
    private ImageView ivPushNoti, ivPushSound, ivReceiveSms;
    private boolean getSms = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorMint));
        }

        initUI();
        getReceiveSms();
    }

    private void initUI() {
        ivPushNoti      = (ImageView)findViewById(R.id.ivPushNoti);
        ivPushSound     = (ImageView)findViewById(R.id.ivPushSound);
        ivReceiveSms    = (ImageView)findViewById(R.id.ivReceiveSms);
        tvReceiveSmsSub = (TextView)findViewById(R.id.tvReceiveSmsSub);

        if(CommonUtil.getReceiveNotification(SettingActivity.this)){
            ivPushNoti.setImageDrawable(getResources().getDrawable(R.drawable.switch_on));
        } else {
            ivPushNoti.setImageDrawable(getResources().getDrawable(R.drawable.switch_off));
        }

        if(CommonUtil.getUseNotificationSound(SettingActivity.this)){
            ivPushSound.setImageDrawable(getResources().getDrawable(R.drawable.switch_on));
        } else {
            ivPushSound.setImageDrawable(getResources().getDrawable(R.drawable.switch_off));
        }
    }

    private void getReceiveSms() {
        Bundle bundle = new Bundle();
        bundle.putString("url", Url.GET_RECEIVE_SMS);
        bundle.putInt("reqCode", REQ_CODE_GET_RECEIVE_SMS);
        bundle.putString("phoneNo", CommonUtil.getUserPhoneNo(this));

        NetworkThreadTask mTask = new NetworkThreadTask(this, false);
        mTask.setOnCompleteListener(this);
        mTask.execute(bundle);
    }

    private void updateReceiveSms(String getSms) {
        Bundle bundle = new Bundle();
        bundle.putString("url", Url.UPDATE_RECEIVE_SMS);
        bundle.putInt("reqCode", REQ_CODE_UDDATE_RECEIVE_SMS);
        bundle.putString("phoneNo", CommonUtil.getUserPhoneNo(this));
        bundle.putString("getSms", getSms);

        NetworkThreadTask mTask = new NetworkThreadTask(this, false);
        mTask.setOnCompleteListener(this);
        mTask.execute(bundle);
    }


    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btnBack){
            onBackPressed();
        } else if(v.getId() == R.id.ivPushNoti) {
            if(CommonUtil.getReceiveNotification(SettingActivity.this)){
                CommonUtil.setReceiveNotification(SettingActivity.this, false);
                ivPushNoti.setImageDrawable(getResources().getDrawable(R.drawable.switch_off));
            } else {
                CommonUtil.setReceiveNotification(SettingActivity.this, true);
                ivPushNoti.setImageDrawable(getResources().getDrawable(R.drawable.switch_on));
            }
        } else if(v.getId() == R.id.ivPushSound) {
            if(CommonUtil.getUseNotificationSound(SettingActivity.this)){
                CommonUtil.setUseNotificationSound(SettingActivity.this, false);
                ivPushSound.setImageDrawable(getResources().getDrawable(R.drawable.switch_off));
            } else {
                CommonUtil.setUseNotificationSound(SettingActivity.this, true);
                ivPushSound.setImageDrawable(getResources().getDrawable(R.drawable.switch_on));
            }
        } else if(v.getId() == R.id.ivReceiveSms) {
            if(getSms)
                updateReceiveSms("0");
            else
                updateReceiveSms("1");
        } else if(v.getId() == R.id.llReceiveBlockedList) {
            Intent intent = new Intent(SettingActivity.this, BlockedMessageListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            overridePendingTransition(R.anim.animation_from_right, R.anim.animation_to_left);
        } else if(v.getId() == R.id.llServiceUseAgree) {
            Intent intent = new Intent(SettingActivity.this, ServiceUseAgreeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            overridePendingTransition(R.anim.animation_from_right, R.anim.animation_to_left);
        } else if(v.getId() == R.id.llPersonnelInfoAgree) {
            Intent intent = new Intent(SettingActivity.this, PersonnelInfoAgreeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            overridePendingTransition(R.anim.animation_from_right, R.anim.animation_to_left);
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.animation_from_left, R.anim.animation_to_right);
    }

    @Override
    public void onSuccess(int requestCd, String responseText) {
        JSONObject jsonObject = null;
        String resultCd;
        try {
            jsonObject = new JSONObject(responseText);
            resultCd = jsonObject.getString("resultCd");

            if (ResultCd.SUCCESS.equals(resultCd)) {
                if (requestCd == REQ_CODE_GET_RECEIVE_SMS || requestCd == REQ_CODE_UDDATE_RECEIVE_SMS) {
                    if(jsonObject.getInt("getSms") == 1){
                        ivReceiveSms.setImageDrawable(getResources().getDrawable(R.drawable.switch_on));
                        tvReceiveSmsSub.setText(R.string.setting_receive_sms_sub_on);
                        getSms = true;
                    } else {
                        ivReceiveSms.setImageDrawable(getResources().getDrawable(R.drawable.switch_off));
                        tvReceiveSmsSub.setText(R.string.setting_receive_sms_sub_off);
                        getSms = false;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            onFailure(requestCd, responseText);
        }
    }

    @Override
    public void onFailure(int requestCode, String responseText) {
        Toast.makeText(this, getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
    }
}
