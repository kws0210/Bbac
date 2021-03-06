package world.picpic.www.bbac;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import world.picpic.www.bbac.common.BaseActivity;
import world.picpic.www.bbac.util.CommonUtil;

public class GuideActivity extends BaseActivity implements View.OnClickListener {
    private final static int GUIDE_COUNT = 6;
    private int[] idImageGuides = {R.drawable.guide0, R.drawable.guide1, R.drawable.guide2, R.drawable.guide3, R.drawable.guide4, R.drawable.guide5};
    private LinearLayout layoutIndicator;
    private View[] pagerIndicators;
    private ViewPager mPager;
    private WalkthroughAdapter mAdapter;
    private Button btnNext;
    private TextView tvServiceAgree;
    private boolean isGuideChecked, doubleBackToExitPressedOnce = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_guide);
        super.onCreate(savedInstanceState);

        CommonUtil.setHasShownGuide(GuideActivity.this, false);
        setContentView(R.layout.activity_guide);

        isGuideChecked = false;
        initUI();
        mAdapter = new WalkthroughAdapter(this);

        mPager.setOffscreenPageLimit(GUIDE_COUNT);
        mPager.setAdapter(mAdapter);
        mPager.setOnPageChangeListener(mListener);

    }

    private int mCurrentPage;

    private ViewPager.OnPageChangeListener mListener = new ViewPager.OnPageChangeListener() {
        public void onPageSelected(int position) {
            mPager.setCurrentItem(position);
            mCurrentPage = position;

            if(position == (GUIDE_COUNT-1)) {
                for(int i = 0; i< GUIDE_COUNT; i++) {
                    pagerIndicators[i].setBackground(getResources().getDrawable(R.drawable.circle_mint));
                }
                btnNext.setVisibility(View.INVISIBLE);
            }
             else {
                for(int i = 0; i < GUIDE_COUNT; i++) {
                    if( i == position) {
                        pagerIndicators[i].setBackground(getResources().getDrawable(R.drawable.circle_white));
                    } else {
                        pagerIndicators[i].setBackground(getResources().getDrawable(R.drawable.circle_background));
                    }
                }
                btnNext.setVisibility(View.VISIBLE);
            }
        }
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
        public void onPageScrollStateChanged(int position) {}
    };

    @Override
    protected void onResume() {
        super.onResume();
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    private void initUI() {
        mPager = (ViewPager) findViewById(R.id.walkthrough_pager);

        btnNext = (Button) findViewById(R.id.btnNext);
        btnNext.setOnClickListener(this);
        layoutIndicator     = (LinearLayout) findViewById(R.id.layoutIndicator);
        pagerIndicators = new View[GUIDE_COUNT];
        pagerIndicators[0]  = (View) layoutIndicator.findViewById(R.id.pagerIndicator1);
        pagerIndicators[1]  = (View) layoutIndicator.findViewById(R.id.pagerIndicator2);
        pagerIndicators[2]  = (View) layoutIndicator.findViewById(R.id.pagerIndicator3);
        pagerIndicators[3]  = (View) layoutIndicator.findViewById(R.id.pagerIndicator4);
        pagerIndicators[4]  = (View) layoutIndicator.findViewById(R.id.pagerIndicator5);
        pagerIndicators[5]  = (View) layoutIndicator.findViewById(R.id.pagerIndicator6);

        mCurrentPage = 0;
    }

    @Override
    public void onClick(View v) {
        if (v.equals(btnNext)) {
            mPager.setCurrentItem(mCurrentPage + 1, true);
        }
    }


    private class WalkthroughAdapter extends PagerAdapter {
        private LayoutInflater mInflater;

        public WalkthroughAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ViewGroup layoutGuide = (ViewGroup) mInflater.inflate(R.layout.guide_item, container, false);
            ImageView guideImage = (ImageView) layoutGuide.findViewById(R.id.guideImage);

            try {
                Uri path = Uri.parse("android.resource://" + getPackageName() + "/" + idImageGuides[position]);
                guideImage.setImageURI(path);
                guideImage.setVisibility(View.VISIBLE);

            }catch (Exception e) {
                e.printStackTrace();
            }

            LinearLayout layoutGuideText    = (LinearLayout) layoutGuide.findViewById(R.id.layoutGuideText);
            final TextView tvServiceAgree     = (TextView)layoutGuide.findViewById(R.id.tvServiceAgree);
            final LinearLayout llCloseGuide   = (LinearLayout) layoutGuide.findViewById(R.id.llCloseGuide);
            final Button btnCheckGuide   = (Button) layoutGuide.findViewById(R.id.btnCheckGuide);

            //가이드 다시 보지 않기
            btnCheckGuide.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isGuideChecked) {
                        btnCheckGuide.setBackground(getResources().getDrawable(R.drawable.select_nor));
                        isGuideChecked = false;
                    } else {
                        btnCheckGuide.setBackground(getResources().getDrawable(R.drawable.select_pre));
                        isGuideChecked = true;
                    }
                }
            });
            llCloseGuide.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isGuideChecked)
                        CommonUtil.setHasShownGuide(GuideActivity.this, true);
                    Intent intent = new Intent(GuideActivity.this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    overridePendingTransition(R.anim.animation_from_bottom, R.anim.animation_to_top);
                }
            });

            //개인정보취급방침 및 서비스이용약관 동의 버튼 처리
            SpannableString ss = new SpannableString(getString(R.string.splash_agree));
            ClickableSpan clickableSpanServiceUseAgree = new ClickableSpan() {
                @Override
                public void onClick(View textView) {
                    startActivity(new Intent(GuideActivity.this, ServiceUseAgreeActivity.class));
                    overridePendingTransition(R.anim.animation_from_right, R.anim.animation_to_left);
                }
                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                }
            };
            ClickableSpan clickableSpanPersonnelInfoAgree = new ClickableSpan() {
                @Override
                public void onClick(View textView) {
                    startActivity(new Intent(GuideActivity.this, PersonnelInfoAgreeActivity.class));
                    overridePendingTransition(R.anim.animation_from_right, R.anim.animation_to_left);
                }
                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                }
            };
            ss.setSpan(clickableSpanServiceUseAgree, 15, 22, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ss.setSpan(new ForegroundColorSpan(ContextCompat.getColor(GuideActivity.this, R.color.colorHighlight)), 15, 22, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ss.setSpan(clickableSpanPersonnelInfoAgree, 24, 32, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ss.setSpan(new ForegroundColorSpan(ContextCompat.getColor(GuideActivity.this, R.color.colorHighlight)), 24, 32, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            tvServiceAgree.setText(ss);
            tvServiceAgree.setMovementMethod(LinkMovementMethod.getInstance());
            tvServiceAgree.setHighlightColor(Color.TRANSPARENT);



            if (position == (GUIDE_COUNT-1)){
                layoutGuideText.setVisibility(View.VISIBLE);
            } else {
                layoutGuideText.setVisibility(View.GONE);
            }
            layoutGuide.setTag(position);

            ((ViewPager) container).addView(layoutGuide);

            return layoutGuide;
        }

        @Override
        public void destroyItem(View container, int position, Object view) {
            ((ViewPager) container).removeView((View) view);
        }

        @Override
        public int getCount() {
            return GUIDE_COUNT;
        }

        @Override
        public boolean isViewFromObject(View v, Object object) {
            return v == object;
        }
    }

    @Override
    public void onBackPressed(){
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
    }
}
