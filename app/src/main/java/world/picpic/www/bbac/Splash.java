package world.picpic.www.bbac;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONObject;

import world.picpic.www.bbac.common.BaseActivity;
import world.picpic.www.bbac.common.ResultCd;
import world.picpic.www.bbac.common.Url;
import world.picpic.www.bbac.util.CommonUtil;
import world.picpic.www.bbac.util.NetworkThreadTask;

public class Splash extends BaseActivity implements NetworkThreadTask.OnCompleteListener{
    private Handler handler;
    private Runnable runnable;
    private final int REQUEST_VERSION_INFO = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        runnable = new Runnable() {
            @Override
            public void run() {
                if (!CommonUtil.getHasShownGuide(Splash.this)) {
                    showGuide();
                } else {
                    goHome();
                }
                finish();
            }
        };

        handler = new Handler();
        requestVersionInfo();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(runnable);
        super.onDestroy();
    }

    private void showGuide() {
        Intent intent = new Intent(this, GuideActivity.class);
        startActivity(intent);
    }

    private void goHome(){
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.animation_from_bottom, R.anim.animation_to_top);
    }

    private void requestVersionInfo() {
        int currentVersion = 0;
        try {
            currentVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Bundle bundle = new Bundle();
        bundle.putString("url", Url.GET_VERSION_INFO);
        bundle.putInt("reqCode", REQUEST_VERSION_INFO);
        bundle.putString("currentVersion", String.valueOf(currentVersion));

        NetworkThreadTask mTask = new NetworkThreadTask(this, false);
        mTask.setOnCompleteListener(this);
        mTask.execute(bundle);
    }

    @Override
    public void onSuccess(int requestCd, String responseText) {
        JSONObject jsonObject = null;
        String resultCd = "";

        try {
            jsonObject = new JSONObject(responseText);
            resultCd = jsonObject.getString("resultCd");

            if (requestCd == REQUEST_VERSION_INFO) {
                if(ResultCd.SUCCESS_WITH_DIALOG.equals(resultCd)) {
                    showDialog(jsonObject.getString("dialogMsg"), R.string.alert_btn_ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                            try {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                            } catch (android.content.ActivityNotFoundException anfe) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                            }

                            finish();
                        }
                    }, R.string.btn_delete_nor, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    });

                } else {
                    handler.postDelayed(runnable, 500);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            onFailure(requestCd, responseText);
        }
    }

    @Override
    public void onFailure(int requestCd, String responseText) {
        Toast.makeText(this, getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
    }
}
