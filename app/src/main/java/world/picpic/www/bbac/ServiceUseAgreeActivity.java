package world.picpic.www.bbac;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import world.picpic.www.bbac.common.BaseActivity;

public class ServiceUseAgreeActivity extends BaseActivity {

    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_use_agree);

        btnBack = (Button)findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                overridePendingTransition(R.anim.animation_from_right, R.anim.animation_to_left);
            }
        });
    }
}
