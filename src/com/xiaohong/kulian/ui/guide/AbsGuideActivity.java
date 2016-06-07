package com.xiaohong.kulian.ui.guide;

import java.util.List;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.xiaohong.kulian.R;

/**
 * The user must extends this class to implements guide requirement
 * @author free
 *
 */
@SuppressLint("NewApi")
public abstract class AbsGuideActivity extends FragmentActivity{

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        List<SinglePage> guideContent = buildGuideContent();

        if (guideContent == null) {
            // nothing to show
            return;
        }

        // prepare views
//        FrameLayout container = new FrameLayout(this);
        setContentView(R.layout.activity_help_moudule_main);
        RelativeLayout container = (RelativeLayout)findViewById
                (R.id.help_module_main_relativelayout);
//        RelativeLayout container=new RelativeLayout(this);
        ViewPager pager = new ViewPager(this);
        pager.setId(getPagerId());

        container.addView(pager, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

//        setContentView(container);
//        container.setBackgroundColor(Color.parseColor("#00ffffff"));
//        container.setAlpha(0.9f);

        FragmentPagerAdapter adapter = new FragmentTabAdapter(this, guideContent);
        pager.setAdapter(adapter);

        GuideView guideView = new GuideView(this, guideContent, drawDot(), dotDefault(), dotSelected());
        pager.setOnPageChangeListener(guideView);
        container.addView(guideView, new LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT));
//        ImageView imageView=new ImageView(this);
//        imageView.setBackgroundResource(R.drawable.closeicon);
//        RelativeLayout.LayoutParams params=
//                new RelativeLayout.LayoutParams(120,120);
//        params.rightMargin=48;
//        params.topMargin=45;
//        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//        container.addView(imageView, params);
        ImageView imageView=(ImageView)AbsGuideActivity.this.findViewById
                (R.id.ImageView_Cancel_help);
//        imageView.performClick();
//        System.out.println("ImageView_Cancel_help"+container.removeView(view));
        System.out.println("ImageView_Cancel_help"+imageView.isFocusable());
        System.out.println("ImageView_Cancel_help"+container.isFocusable());
        
        imageView.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                System.out.println("ImageView_Cancel_help001");
                AbsGuideActivity.this.finish();
                
            }
        });
//        container.setOnClickListener(new OnClickListener() {
//            
//            @Override
//            public void onClick(View v) {
//                System.out.println("ImageView_Cancel_help001");
//                AbsGuideActivity.this.finish();
//                
//            }
//        });
//        imageView.setOnTouchListener(new OnTouchListener()  
//        {  
//            @Override  
//            public boolean onTouch(View arg0, MotionEvent arg1)   
//            {   
//                System.out.println("ImageView_Cancel_help002");
//                return true;  
//            }             
//        });  
//        System.out.println("ImageView_Cancel_help"+imageView.isClickable());
    }

    abstract public List<SinglePage> buildGuideContent();

    abstract public boolean drawDot();

    abstract public Bitmap dotDefault();

    abstract public Bitmap dotSelected();

    abstract public int getPagerId();
    public void myonclick(View v){
        System.out.println("ImageView_Cancel_help001");
        AbsGuideActivity.this.finish();
    }
}
