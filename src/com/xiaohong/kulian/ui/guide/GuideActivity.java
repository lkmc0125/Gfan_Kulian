package com.xiaohong.kulian.ui.guide;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

import com.xiaohong.kulian.R;
import com.xiaohong.kulian.ui.guide.AbsGuideActivity;
import com.xiaohong.kulian.ui.guide.SingleElement;
import com.xiaohong.kulian.ui.guide.SinglePage;

public class GuideActivity extends AbsGuideActivity {

    @Override
    public List<SinglePage> buildGuideContent() {
        // prepare the information for our guide
        List<SinglePage> guideContent = new ArrayList<SinglePage>();

        SinglePage page01 = new SinglePage();
        page01.mLayoutResId = R.layout.activity_help_module_first_layout;       
        guideContent.add(page01);

        SinglePage page02 = new SinglePage();
        page02.mLayoutResId = R.layout.activity_help_module_second_layout;
        guideContent.add(page02);

        SinglePage page03 = new SinglePage();
        page03.mLayoutResId = R.layout.activity_help_module_third_layout;
        guideContent.add(page03);

        SinglePage page04 = new SinglePage();
        page04.mLayoutResId = R.layout.activity_help_module_fouth_layout;
        guideContent.add(page04);

        SinglePage page05 = new SinglePage();
        page05.mLayoutResId = R.layout.activity_help_module_fifth_layout;
        //page05.mCustomFragment = new EntryFragment();
        guideContent.add(page05);

        return guideContent;
    }

    @Override
    public Bitmap dotDefault() {
//        return BitmapFactory.decodeResource(getResources(), R.drawable.help_shape_circle_dot_white);
        return BitmapFactory.decodeResource(getResources(), R.drawable.ic_dot_default);
    }

    @Override
    public Bitmap dotSelected() {
//        return BitmapFactory.decodeResource(getResources(), R.drawable.help_shape_circle_dot_blue);
        return BitmapFactory.decodeResource(getResources(), R.drawable.ic_dot_selected);
    }

    @Override
    public boolean drawDot() {
        return true;
    }

    public void entryApp() {
        // Time to entry your app! We just finish the activity, replace it with
        // your code.

        finish();
    }

    /**
     * You need provide an id to the pager. You could define an id in
     * values/ids.xml and use it.
     */
    @Override
    public int getPagerId() {
        return R.id.guide_container;
    }
}
