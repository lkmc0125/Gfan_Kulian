package com.xiaohong.kulian.common.widget;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

import com.xiaohong.kulian.bean.MessageBean;

/**
 * 实现无焦点的跑马灯功能
 * @author ablert
 *
 */
/*public class MarqueeTextView extends TextView {
    public MarqueeTextView(Context con) {
        super(con);
      }

      public MarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
      }
      public MarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
      }
      @Override
      public boolean isFocused() {
          return true;
      }
      @Override
      protected void onFocusChanged(boolean focused, int direction,
         Rect previouslyFocusedRect) {  
      }
      }*/

public class MarqueeTextView extends TextView implements Runnable {
private int currentScrollX=0;// 初始滚动的位置
private int firstScrollX=0;
private boolean isStop = false;
private int textWidth;
private int mWidth=0; //控件宽度
private int speed=10;
private int delayed=200;
private int start_delayed=200;
private int endX; //滚动到哪个位置
private int position=0;
private ArrayList<Integer> messageWidthList=new ArrayList<Integer>();

private ArrayList<MessageBean> messageBeans;
public ArrayList<MessageBean> getMessageBeans() {
    return messageBeans;
}
public void setMessageBeans(ArrayList<MessageBean> messageBeans) {
    this.messageBeans = messageBeans;
}
private boolean isFirstDraw=true; //当首次或文本改变时重置


public MarqueeTextView(Context context) {
        super(context);
        
}
public MarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
}
public MarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
}
@Override
public void setText(CharSequence text, BufferType type) {
    // TODO Auto-generated method stub
//    getTextWidth();
    getTextWidth_First();
//    System.out.println("getTextWidth_First()"+messageWidthList);
    super.setText(text, type);
}
@Override
protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(isFirstDraw){
                getTextWidth();
                firstScrollX=getScrollX();      //起始位置不一定为0,改变内容后会变，需重新赋值       
                currentScrollX=firstScrollX;
                mWidth=this.getWidth();
//                endX=firstScrollX+textWidth-mWidth/2;
                endX=firstScrollX+textWidth-mWidth/4;
                isFirstDraw=false;
               /* if(messageBeans!=null){
                    position=position%messageBeans.size();
                    setText(messageBeans.get(position).getMessageText());
                    position++;
                }*/
        }
}
//每次滚动几点

public void setSpeed(int sp){
        speed=sp;
}

//滚动间隔时间,毫秒

public void setDelayed(int delay){
        delayed=delay;  
}
/**
* 获取文字宽度
*/
private void getTextWidth() {
        Paint paint = this.getPaint();
        String str = this.getText().toString();
        textWidth = (int) paint.measureText(str);
}
/**
 * 获取文字的宽度，同时保存每一条消息的位置信息
 */
private void getTextWidth_First() {
    Paint paint = this.getPaint();
    String Message_value="";
    if(messageBeans!=null){
        for(MessageBean s:messageBeans){
            String str="";
            if(!Message_value.equals("")){
                Message_value+="                                        ";
                str+="                                        ";
            }
            str=s.getMessageText()+"          ";
            textWidth += (int) paint.measureText(str);
            messageWidthList.add(textWidth);
        } 
        if(messageWidthList!=null){
            System.out.println("getTextWidth_First()"+messageWidthList);
        }
    }
}
/**
 * 判断当前消息是那一条信息
 * @return
 */
public int getPosition() {
    System.out.println("getTextWidth_First()"+textWidth);
    System.out.println("getTextWidth_First()"+messageWidthList);
    System.out.println("getTextWidth_First()"+(currentScrollX));
    for(int i=0;i<messageWidthList.size();i++){
        if(currentScrollX<messageWidthList.get(i)){
            position=i;
            break;
        }
    }
    return position;
}
public void setPosition(int position) {
    this.position = position;
}
@Override
public void run() {
        //currentScrollX += 1;// 滚动速度
        currentScrollX += speed;// 滚动速度,每次滚动几点  
        scrollTo(currentScrollX, 0);
        if (isStop) {
                return;
        }
        //从头开始
        if (currentScrollX >= endX) {           
                //scrollTo(0, 0);
                //currentScrollX = 0; //原文重置为0,发现控件所放的位置不同，初始位置不一定为0
                scrollTo(firstScrollX,0);
                currentScrollX=firstScrollX;
                postDelayed(this,start_delayed);
        }else{          
                postDelayed(this, delayed);}
        }

@Override
protected void onTextChanged(CharSequence text, int start, int lengthBefore,int lengthAfter) {
        
        isStop=true;    //停止滚动
        this.removeCallbacks(this); //清空队列
        currentScrollX=firstScrollX; //滚动到初始位置
        this.scrollTo(currentScrollX, 0);
        super.onTextChanged(text, start, lengthBefore, lengthAfter);    
        isFirstDraw=true; //需重新设置参数
        isStop=false;
        postDelayed(this,start_delayed); //头部停的时间
        
}
// 开始滚动
public void startScroll() {     
        isStop = false;
        this.removeCallbacks(this);
        postDelayed(this,start_delayed); 
}
// 停止滚动
public void stopScroll() {
        isStop = true;
}
// 从头开始滚动
public void startFor0() {
        currentScrollX = 0;
        startScroll();
}
}
