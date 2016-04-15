/**
 * 
 */
package com.xiaohong.kulian.adapter;

import java.text.DecimalFormat;
import java.util.ArrayList;

import com.xiaohong.kulian.R;
import com.xiaohong.kulian.bean.GoodsBean;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * @author free
 *
 */
public class BuyItemGridViewAdapter extends BaseAdapter {
    ArrayList<GoodsBean> mData;
    private Context mContext;
    private LayoutInflater mInfalter;
    private int mSelectedPos = 0;
    
    public void setSelectedPos(int pos) {
        mSelectedPos = pos;
    }
    
    public int getSelectedGoodsId() {
        return mData.get(mSelectedPos).getGoodsId();
    }

    public String getSelectedGoodsName() {
        return mData.get(mSelectedPos).getName();
    }
    
    public BuyItemGridViewAdapter(Context context, ArrayList<GoodsBean> data) {
        mContext = context;
        mData = data;
        mInfalter = LayoutInflater.from(mContext);
        
    }

    /* (non-Javadoc)
     * @see android.widget.Adapter#getCount()
     */
    @Override
    public int getCount() {
        return mData.size();
    }

    /* (non-Javadoc)
     * @see android.widget.Adapter#getItem(int)
     */
    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    /* (non-Javadoc)
     * @see android.widget.Adapter#getItemId(int)
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /* (non-Javadoc)
     * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView == null) {
            convertView = mInfalter.inflate(R.layout.buy_item, null);
            
            holder = new ViewHolder();
            holder.mCoinLayout = (RelativeLayout) convertView.findViewById(R.id.coinlayout);
            holder.mCoinTv = (TextView) convertView.findViewById(R.id.cointv);
            holder.mMoneyTv = (TextView) convertView.findViewById(R.id.moneytv);
            holder.mMoneyUnitTv = (TextView) convertView.findViewById(R.id.moneyunittv);
            convertView.setTag(holder);
            
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        if(mSelectedPos == position) {
            holder.mCoinLayout
            .setBackgroundResource(R.drawable.coincheckedbg);
            holder.mCoinTv.setTextColor(mContext.getResources().getColor(R.color.buy_coin_item_selected_color));
            holder.mMoneyTv.setTextColor(mContext.getResources().getColor(R.color.buy_coin_item_selected_color));
            holder.mMoneyUnitTv.setTextColor(mContext.getResources().getColor(R.color.buy_coin_item_selected_color));
        }else {
            holder.mCoinLayout
            .setBackgroundResource(R.drawable.coindefaultbg);
            holder.mCoinTv.setTextColor(mContext.getResources().getColor(R.color.buy_coin_item_un_selected_black_color));
            holder.mMoneyTv.setTextColor(mContext.getResources().getColor(R.color.buy_coin_item_un_selected_blue_color));
            holder.mMoneyUnitTv.setTextColor(mContext.getResources().getColor(R.color.buy_coin_item_un_selected_black_color));
        }
        GoodsBean item = mData.get(position);
        holder.mCoinTv.setText(item.getName());
        if (item.getPrice() % 100 == 0) {
            holder.mMoneyTv.setText(item.getPrice()/100+"");
        } else {
            float price = (float)item.getPrice()/100.f;
            holder.mMoneyTv.setText(new DecimalFormat("#0.00").format(price));
        }
        return convertView;
    }
    
    private static class ViewHolder {
        private RelativeLayout mCoinLayout;
        private TextView mCoinTv;
        private TextView mMoneyTv;
        private TextView mMoneyUnitTv;
    }

}
