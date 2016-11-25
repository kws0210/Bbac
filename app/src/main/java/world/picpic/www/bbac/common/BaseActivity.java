package world.picpic.www.bbac.common;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import world.picpic.www.bbac.R;

public class BaseActivity extends FragmentActivity{

    private FirebaseAnalytics mFirebaseAnalytics;

    public FirebaseAnalytics getFirebaseAnalytics() {
        if(mFirebaseAnalytics == null)
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        return mFirebaseAnalytics;
    }

    @Override
    protected void onResume() {
        super.onResume();

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, this.getClass().getSimpleName());
        getFirebaseAnalytics().logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    public void showAlert(int msgId){
        final Dialog dialog = new Dialog(this, R.style.noTitleDialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.alertdialog);

        TextView tv = (TextView) dialog.findViewById(R.id.tv_alert_dialog);
        tv.setText(getResources().getString(msgId));
        Button btn = (Button) dialog.findViewById(R.id.btn_alert_dialog_positive);
        ((Button)dialog.findViewById(R.id.btn_alert_dialog_negative)).setVisibility(View.GONE);
        btn.setText("확인");
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        dialog.show();
    }

    public void showAlert(int msgId, int btnNmId, final View.OnClickListener confirmListener){
        final Dialog dialog = new Dialog(this, R.style.noTitleDialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.alertdialog);

        TextView tv = (TextView) dialog.findViewById(R.id.tv_alert_dialog);
        tv.setText(getResources().getString(msgId));
        Button btn = (Button) dialog.findViewById(R.id.btn_alert_dialog_positive);
        ((Button)dialog.findViewById(R.id.btn_alert_dialog_negative)).setVisibility(View.GONE);
        btn.setText(getResources().getString(btnNmId));
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(confirmListener != null)
                    confirmListener.onClick(v);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void showAlert(String msg, int btnNmId, final View.OnClickListener confirmListener){
        final Dialog dialog = new Dialog(this, R.style.noTitleDialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.alertdialog);

        TextView tv = (TextView) dialog.findViewById(R.id.tv_alert_dialog);
        tv.setText(msg);
        Button btn = (Button) dialog.findViewById(R.id.btn_alert_dialog_positive);
        ((Button)dialog.findViewById(R.id.btn_alert_dialog_negative)).setVisibility(View.GONE);
        btn.setText(getResources().getString(btnNmId));
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(confirmListener != null)
                    confirmListener.onClick(v);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void showDialog(int msgId, final View.OnClickListener positiveListener, final View.OnClickListener negativeListener){
        final Dialog dialog = new Dialog(this, R.style.noTitleDialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.alertdialog);

        TextView tv = (TextView) dialog.findViewById(R.id.tv_alert_dialog);
        tv.setText(getResources().getString(msgId));
        Button btnPositive = (Button) dialog.findViewById(R.id.btn_alert_dialog_positive);
        Button btnNegative = (Button) dialog.findViewById(R.id.btn_alert_dialog_negative);
        btnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(positiveListener != null)
                    positiveListener.onClick(v);
                dialog.dismiss();
            }
        });
        btnNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(negativeListener != null)
                    negativeListener.onClick(v);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void showDialog(int msgId, int positiveMsgId, final View.OnClickListener positiveListener, int negativeMsgId, final View.OnClickListener negativeListener) {
        final Dialog dialog = new Dialog(this, R.style.noTitleDialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.alertdialog);

        TextView tv = (TextView) dialog.findViewById(R.id.tv_alert_dialog);
        tv.setText(msgId);
        Button btnPositive = (Button) dialog.findViewById(R.id.btn_alert_dialog_positive);
        Button btnNegative = (Button) dialog.findViewById(R.id.btn_alert_dialog_negative);
        btnPositive.setText(positiveMsgId);
        btnNegative.setText(negativeMsgId);
        btnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (positiveListener != null)
                    positiveListener.onClick(v);
                dialog.dismiss();
            }
        });
        btnNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (negativeListener != null)
                    negativeListener.onClick(v);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void showDialog(String msg, int positiveMsgId, final View.OnClickListener positiveListener, int negativeMsgId, final View.OnClickListener negativeListener) {
        final Dialog dialog = new Dialog(this, R.style.noTitleDialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.alertdialog);

        TextView tv = (TextView) dialog.findViewById(R.id.tv_alert_dialog);
        tv.setText(msg);
        Button btnPositive = (Button) dialog.findViewById(R.id.btn_alert_dialog_positive);
        Button btnNegative = (Button) dialog.findViewById(R.id.btn_alert_dialog_negative);
        btnPositive.setText(positiveMsgId);
        btnNegative.setText(negativeMsgId);
        btnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (positiveListener != null)
                    positiveListener.onClick(v);
                dialog.dismiss();
            }
        });
        btnNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (negativeListener != null)
                    negativeListener.onClick(v);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void showSelectDialog(int positiveMsgId, final View.OnClickListener positiveListener, int negativeMsgId, final View.OnClickListener negativeListener) {
        final Dialog dialog = new Dialog(this, R.style.noTitleDialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.select_dialog);

        Button btnPositive = (Button) dialog.findViewById(R.id.btn_alert_dialog_positive);
        Button btnNegative = (Button) dialog.findViewById(R.id.btn_alert_dialog_negative);
        btnPositive.setText(positiveMsgId);
        btnNegative.setText(negativeMsgId);
        btnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (positiveListener != null)
                    positiveListener.onClick(v);
                dialog.dismiss();
            }
        });
        btnNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (negativeListener != null)
                    negativeListener.onClick(v);
                dialog.dismiss();
            }
        });
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();

        wlp.width = getWindowManager().getDefaultDisplay().getWidth() * 7 / 8;
        window.setAttributes(wlp);

        dialog.show();
    }

    public void showSelectDialogWithRadioButton(int positiveMsgId, final View.OnClickListener positiveListener, int negativeMsgId, final View.OnClickListener negativeListener, boolean isPositive) {
        final Dialog dialog = new Dialog(this, R.style.noTitleDialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.select_dialog_with_radio_button);

        RelativeLayout rlPositive = (RelativeLayout) dialog.findViewById(R.id.rl_alert_dialog_positive);
        RelativeLayout rlNegative = (RelativeLayout) dialog.findViewById(R.id.rl_alert_dialog_negative);
        TextView tvPositive = (TextView) dialog.findViewById(R.id.tv_alert_dialog_positive);
        TextView tvNegative = (TextView) dialog.findViewById(R.id.tv_alert_dialog_negative);
        ImageView ivPositive = (ImageView) dialog.findViewById(R.id.iv_alert_dialog_positive);
        ImageView ivNegative = (ImageView) dialog.findViewById(R.id.iv_alert_dialog_negative);

        tvPositive.setText(positiveMsgId);
        tvNegative.setText(negativeMsgId);
        rlPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (positiveListener != null)
                    positiveListener.onClick(v);
                dialog.dismiss();
            }
        });
        rlNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (negativeListener != null)
                    negativeListener.onClick(v);
                dialog.dismiss();
            }
        });
        if(isPositive) {
            ivPositive.setBackground(getResources().getDrawable(R.drawable.select_pre));
            ivNegative.setBackground(getResources().getDrawable(R.drawable.select_nor));
        } else {
            ivPositive.setBackground(getResources().getDrawable(R.drawable.select_nor));
            ivNegative.setBackground(getResources().getDrawable(R.drawable.select_pre));
        }

        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();

        wlp.gravity = Gravity.BOTTOM;
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(wlp);

        dialog.show();
    }

    public void showSelectDialogOneOption(int positiveMsgId, int negativeMsgId, final View.OnClickListener negativeListener) {
        final Dialog dialog = new Dialog(this, R.style.noTitleDialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.select_dialog);

        Button btnPositive = (Button) dialog.findViewById(R.id.btn_alert_dialog_positive);
        Button btnNegative = (Button) dialog.findViewById(R.id.btn_alert_dialog_negative);
        btnPositive.setText(positiveMsgId);
        btnPositive.setTextColor(getResources().getColor(R.color.colorChacol));
        btnNegative.setText(negativeMsgId);
        btnPositive.setEnabled(false);
        btnNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (negativeListener != null)
                    negativeListener.onClick(v);
                dialog.dismiss();
            }
        });
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();

        wlp.width = getWindowManager().getDefaultDisplay().getWidth() * 7 / 8;
        window.setAttributes(wlp);

        dialog.show();
    }

    public void showSelectDialogTwoOptions(int positiveMsgId, int negativeMsgId, final View.OnClickListener positiveListener, final View.OnClickListener negativeListener) {
        final Dialog dialog = new Dialog(this, R.style.noTitleDialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.select_dialog_two_options);

        Button btnPositive = (Button) dialog.findViewById(R.id.btn_alert_dialog_positive);
        Button btnNegative = (Button) dialog.findViewById(R.id.btn_alert_dialog_negative);
        btnPositive.setText(positiveMsgId);
        btnNegative.setText(negativeMsgId);
        btnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (positiveListener != null)
                    positiveListener.onClick(v);
                dialog.dismiss();
            }
        });
        btnNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (negativeListener != null)
                    negativeListener.onClick(v);
                dialog.dismiss();
            }
        });
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();

        wlp.width = getWindowManager().getDefaultDisplay().getWidth() * 7 / 8;
        window.setAttributes(wlp);

        dialog.show();
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if(activity.getCurrentFocus()!=null){
            imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }
}
