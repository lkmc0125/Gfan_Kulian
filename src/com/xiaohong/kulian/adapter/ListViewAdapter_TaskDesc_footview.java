package com.xiaohong.kulian.adapter;

import java.util.ArrayList;
import java.util.List;

import aga.fdf.grd.os.df.AdExtraTaskStatus;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.xiaohong.kulian.R;
import com.xiaohong.kulian.common.vo.TaskDescObject;

public class ListViewAdapter_TaskDesc_footview extends BaseAdapter{

//    List<TaskDescObject> mList;
//    private final Context context;
    
    
    public final int DEFAULT_SHOW_COUNT;
    private final Context context;
    protected ListView mListView;
    protected LayoutInflater inflater;
    protected LinearLayout headView;
    protected Button btn_loadmore;
    protected List<TaskDescObject> mShowObjects = new ArrayList<TaskDescObject>();
    protected List<TaskDescObject> mAllObjects = null;
    protected boolean shrink = true;

    public ListViewAdapter_TaskDesc_footview( Context context, ListView mListView,int DEFAULT_SHOW_COUNT) {
        this.context = context;
        this.mListView = mListView;
        this.DEFAULT_SHOW_COUNT=DEFAULT_SHOW_COUNT;
        inflater = LayoutInflater.from(context);
        headView = (LinearLayout) inflater.inflate(R.layout.activity_listview_youmi_footer, null);
        btn_loadmore = (Button) headView.findViewById(R.id.btn_loadmore);
        btn_loadmore.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                changeShow();
            }
        });
        mListView.addFooterView(headView, null, false);
    }
    
//    public ListViewAdapter_TaskDesc_footview(Context context, ArrayList<TaskDescObject> list) {
//        this.context = context;
//        mList = list;
//    }

    public void setAdapterData( List<TaskDescObject> mAllObjects ) {
        this.mAllObjects = mAllObjects;
        mShowObjects.clear();
        if( mAllObjects != null ) {
            if( mAllObjects.size() <= DEFAULT_SHOW_COUNT ) {
                headView.setVisibility(View.GONE);
                mShowObjects.addAll(mAllObjects);
            } else {
                headView.setVisibility(View.VISIBLE);
                for (int i = 0; i < DEFAULT_SHOW_COUNT; i++) {
                    mShowObjects.add(mAllObjects.get(i));
                }
            }
        }
        notifyDataSetChanged();
        setListViewHeightBasedOnChildren(mListView);
    }
    
    private void changeShow() {
        if( headView.getVisibility() == View.GONE ) {
            headView.setVisibility(View.VISIBLE);
        }
        mShowObjects.clear();
        if( shrink ) {
            shrink = false;
            mShowObjects.addAll(mAllObjects);
            btn_loadmore.setText("收起任务");
        } else {
            shrink = true;
            for (int i = 0; i < DEFAULT_SHOW_COUNT; i++) {
                mShowObjects.add(mAllObjects.get(i));
            }
            btn_loadmore.setText("更多任务");
        }
        notifyDataSetChanged();
        setListViewHeightBasedOnChildren(mListView);
    }
    
    /**
     * 当ListView外层有ScrollView时，需要动态设置ListView高度
     * @param listView
     */
    protected void setListViewHeightBasedOnChildren(ListView listView) { 
        if(listView == null) return;
        ListAdapter listAdapter = listView.getAdapter(); 
        if (listAdapter == null) { 
            return; 
        } 
        int totalHeight = 0; 
        for (int i = 0; i < listAdapter.getCount(); i++) { 
            View listItem = listAdapter.getView(i, null, listView); 
            listItem.measure(0, 0); 
            totalHeight += listItem.getMeasuredHeight(); 
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams(); 
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1)); 
        listView.setLayoutParams(params); 
    }
    
    public void setData(List<TaskDescObject> list) {
        mShowObjects = list;
    }

    @Override
    public int getCount() {
        return mShowObjects == null ? 0 : mShowObjects.size();
    }

    @Override
    public TaskDescObject getItem(int position) {
        return mShowObjects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.lv_item_task_desc, null);
            viewHolder.tv_id = (TextView) convertView.findViewById(R.id.lvitem_tv_ext_id);
            viewHolder.tv_desc = (TextView) convertView.findViewById(R.id.lvitem_tv_ext_desc);
            viewHolder.tv_status = (TextView) convertView.findViewById(R.id.lvitem_tv_ext_status);
            viewHolder.tv_points = (TextView) convertView.findViewById(R.id.lvitem_tv_ext_points);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        TaskDescObject descObject = getItem(position);

        // 设置左边的数字序号
        viewHolder.tv_id.setText(String.valueOf(position + 1));
        // 设置任务赚取描述
        viewHolder.tv_desc.setText(descObject.getDesc());
        // 设置积分或者已完成
        viewHolder.tv_points.setText("+" + descObject.getPoints());

        switch (descObject.getStatus()) {

        // 任务未开始
        case AdExtraTaskStatus.NOT_START:
            viewHolder.tv_id.setTextColor(context.getResources().getColor(R.color.gray_1));
            // viewHolder.tv_id.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.tv_task_cannotdo_bg));

            viewHolder.tv_desc.setTextColor(context.getResources().getColor(R.color.gray_1));

            viewHolder.tv_points.setTextColor(context.getResources().getColor(R.color.gray_1));
            // viewHolder.tv_points.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.tv_task_cannotdo_bg));

            viewHolder.tv_status.setText("任务未开始（请勿卸载）");
            break;

        // 任务可以做
        case AdExtraTaskStatus.IN_PROGRESS:
            viewHolder.tv_id.setTextColor(context.getResources().getColor(R.color.app_task_select_color));
            // viewHolder.tv_id.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.tv_task_cando_bg));

            viewHolder.tv_desc.setTextColor(Color.BLACK);

            viewHolder.tv_points.setTextColor(context.getResources().getColor(R.color.app_coin_num_color));
            // viewHolder.tv_points.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.tv_task_cando_bg));

            viewHolder.tv_status.setText("任务可进行");
            break;

        // 任务已完成
        case AdExtraTaskStatus.COMPLETE:
            viewHolder.tv_id.setTextColor(context.getResources().getColor(R.color.black));
            // viewHolder.tv_id.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.tv_task_cannotdo_bg));

            viewHolder.tv_desc.setTextColor(context.getResources().getColor(R.color.black));
            viewHolder.tv_desc.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);

            viewHolder.tv_points.setText("完成");
            viewHolder.tv_points.setTextColor(Color.WHITE);
            // viewHolder.tv_points.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.tv_task_complete_bg));

            viewHolder.tv_status.setText("任务已完成（请勿卸载）");
            break;

        // 任务已过时
        case AdExtraTaskStatus.OUT_OF_DATE:
            viewHolder.tv_id.setTextColor(context.getResources().getColor(R.color.black));
            // viewHolder.tv_id.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.tv_task_cannotdo_bg));

            viewHolder.tv_desc.setTextColor(context.getResources().getColor(R.color.black));
            viewHolder.tv_desc.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);

            viewHolder.tv_points.setText("过期");
            viewHolder.tv_points.setTextColor(context.getResources().getColor(R.color.black));
            // viewHolder.tv_points.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.tv_task_cannotdo_bg));

            viewHolder.tv_status.setText("任务已过期");
            break;

        }

        return convertView;
    }

    private static class ViewHolder {
        TextView tv_id;
        TextView tv_desc;
        TextView tv_status;
        TextView tv_points;
    }
}