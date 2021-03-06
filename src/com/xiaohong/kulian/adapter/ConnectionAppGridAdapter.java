package com.xiaohong.kulian.adapter;

import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.xiaohong.kulian.R;
import com.xiaohong.kulian.common.util.Utils;
import com.xiaohong.kulian.common.widget.RoundImageView;

/**
 * 
 * @author albert 2016/04/06
 * 连接页面推荐应用的九宫格内容容器
 *
 */
@SuppressLint("ViewHolder")
public class ConnectionAppGridAdapter extends BaseAdapter
{

    private LayoutInflater inflater;
    private List<Map<String, Object>> list_result;
    public ConnectionAppGridAdapter(Context context, List<Map<String, Object>> list)
    {
            this.inflater = LayoutInflater.from(context);
            this.list_result=list;
    }
    public int getCount() {
            return list_result.size();
    }

    public Object getItem(int position) {
            return list_result.get(position);
    }

    public long getItemId(int position) {
            return position;
    }

    public void setList_result(List<Map<String, Object>> list_result) {
            this.list_result = list_result;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.connect_third_part_grid_item, null);
            holder.mRoundImageView = (RoundImageView)
                    convertView.findViewById(R.id.connection_recommend_app_image); 
            holder.mNameTv = (TextView)
                    convertView.findViewById(R.id.connection_recommend_app_name_hint_text); 
            holder.mCoinTv = (TextView)
                    convertView.findViewById(R.id.connection_recommend_app_coin_text); 
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        String path=list_result.get(position).get("logo_url").toString();
        String name=list_result.get(position).get("name").toString();
        String GiveCoin=list_result.get(position).get("GiveCoin").toString();
        ImageLoader.getInstance().displayImage(path, 
                new ImageViewAware(holder.mRoundImageView),
                Utils.sDisplayRoundImageOptions);
        holder.mNameTv.setText(name);
        holder.mCoinTv.setText(GiveCoin);
        return convertView;
    }
    
    private static class ViewHolder {
        RoundImageView mRoundImageView;
        TextView mNameTv;
        TextView mCoinTv;
    }
}
