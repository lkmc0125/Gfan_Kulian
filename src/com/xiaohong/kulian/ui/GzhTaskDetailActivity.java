package com.xiaohong.kulian.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import com.xiaohong.kulian.R;

public class GzhTaskDetailActivity extends Activity implements OnClickListener {
    private static final String TAG = "GzhTaskDetailActivity";
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gzh_task_detail);
        
    }

    @SuppressLint("NewApi")
    private void initViews() {
       
    }
    
    @Override
    protected void onDestroy() {
       
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        
    }

   

}
