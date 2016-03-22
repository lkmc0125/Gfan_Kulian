package com.xiaohong.kulian.ui;

import org.json.JSONObject;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.tencent.mm.sdk.constants.Build;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.xiaohong.kulian.Constants;
import com.xiaohong.kulian.R;
import com.xiaohong.kulian.common.util.Utils;
import com.xiaohong.kulian.common.widget.BaseActivity;

public class PayMainActivity extends BaseActivity {
    private IWXAPI mWxApi;
    private static final String TAG = "PayMainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_main);
        initData();
    }

    private void initData() {
        mWxApi = WXAPIFactory.createWXAPI(this, Constants.APP_ID, false);
        mWxApi.registerApp(Constants.APP_ID);

        boolean isPaySupported = mWxApi.getWXAppSupportAPI() >= Build.PAY_SUPPORTED_SDK_INT;
        Toast.makeText(PayMainActivity.this, String.valueOf(isPaySupported), Toast.LENGTH_SHORT).show();

        Button appayBtn = (Button) findViewById(R.id.appay_btn);
        appayBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String url = "http://wxpay.weixin.qq.com/pub_v2/app/app_pay.php?plat=android";
                Button payBtn = (Button) findViewById(R.id.appay_btn);
                payBtn.setEnabled(false);
                Toast.makeText(PayMainActivity.this, "请稍候...", Toast.LENGTH_SHORT).show();
                try {
                    byte[] buf = Utils.httpGet(url);
                    if (buf != null && buf.length > 0) {
                        String content = new String(buf);
                        Log.e("get server pay params:",content);
                        JSONObject json = new JSONObject(content); 
                        if (null != json && !json.has("retcode") ){
                            PayReq req = new PayReq();
                            req.appId           = json.getString("appid");
                            req.partnerId       = json.getString("partnerid");
                            req.prepayId        = json.getString("prepayid");
                            req.nonceStr        = json.getString("noncestr");
                            req.timeStamp       = json.getString("timestamp");
                            req.packageValue    = json.getString("package");
                            req.sign            = json.getString("sign");
                            req.extData         = "app data"; // optional
                            Toast.makeText(PayMainActivity.this, "正在跳转到微信", Toast.LENGTH_SHORT).show();
                            mWxApi.sendReq(req);
                        }else{
                            Log.d("PAY_GET", "获取支付信息失败："+json.getString("retmsg"));
                            Toast.makeText(PayMainActivity.this, "错误信息："+json.getString("retmsg"), Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Log.d("PAY_GET", "服务器连接失败");
                        Toast.makeText(PayMainActivity.this, "无法连接到服务器，请稍后再试", Toast.LENGTH_SHORT).show();
                    }
                }catch(Exception e){
                    Log.e("PAY_GET", "异常信息："+e.getMessage());
                    Toast.makeText(PayMainActivity.this, "异常："+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                payBtn.setEnabled(true);
            }
        });
    
    }

}
