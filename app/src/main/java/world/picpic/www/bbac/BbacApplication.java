package world.picpic.www.bbac;

import android.app.Application;

import world.picpic.www.bbac.util.CommonUtil;

/**
 * Created by Wonseob on 2016. 7. 25..
 */
public class BbacApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
//        CommonUtil.overrideFont(getApplicationContext(), "SERIF", "fonts/BMHANNA_11yrs.ttf");
        CommonUtil.overrideFont(getApplicationContext(), "SERIF", "fonts/NanumGothic.otf");
    }
}
