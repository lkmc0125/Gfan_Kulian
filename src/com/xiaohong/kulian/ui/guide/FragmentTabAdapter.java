package com.xiaohong.kulian.ui.guide;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class FragmentTabAdapter extends FragmentPagerAdapter {

    private List<SinglePage> mGuideContent;
    private Context mCtx;

    public FragmentTabAdapter(FragmentActivity a, List<SinglePage> guideContent) {
        super(a.getSupportFragmentManager());
        mCtx = a;
        mGuideContent = guideContent;
    }

    @Override
    public Fragment getItem(int position) {
        // Get a local reference
        SinglePage sp = mGuideContent.get(position);
        
        if (sp.mCustomFragment != null) {
            // This single page has custom fragment, use it
            return sp.mCustomFragment;
        } else {
            PageFragment pageFragment = (PageFragment) Fragment.instantiate(mCtx, PageFragment.class.getName());
            if(sp.mLayoutResId != 0) {
                pageFragment.setLayoutResId(sp.mLayoutResId);
            }else if (sp.mBackground != null) {
                pageFragment.setBg(sp.mBackground);
            }
            return pageFragment;
        }
    }

    @Override
    public int getCount() {
        return mGuideContent.size();
    }

    /**
     * The page to show info. which is at the background of indicator
     * @author free
     *
     */
    public static final class PageFragment extends Fragment {

        private Drawable mBg;
        private int mLayoutResId = 0;

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            if(mLayoutResId != 0) {
                //inlfate view from xml
                View v = inflater.inflate(mLayoutResId, null);
                return v;
            }
            ImageView iv = new ImageView(getActivity());
            if (mBg != null) {
                iv.setBackground(mBg);
            }
            return iv;
        }

        public void setBg(Drawable mBackground) {
            mBg = mBackground;
        }
        
        public void setLayoutResId(int layoutResId) {
            mLayoutResId = layoutResId;
        }

    }
}
