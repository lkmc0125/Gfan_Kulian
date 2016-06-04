package com.xiaohong.kulian.ui.guide;

import java.util.ArrayList;
import java.util.List;

import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;

public class SinglePage {
    public Drawable mBackground;
    public int mLayoutResId;
    public List<SingleElement> mElementsList = new ArrayList<SingleElement>();
    public Fragment mCustomFragment;
}
