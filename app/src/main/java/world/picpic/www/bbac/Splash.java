package world.picpic.www.bbac;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import world.picpic.www.bbac.util.CommonUtil;

public class Splash extends Activity {
    private Handler handler;
    private Runnable runnable;

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
        handler.postDelayed(runnable, 500);
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
}
