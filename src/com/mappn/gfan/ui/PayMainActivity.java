package com.mappn.gfan.ui;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Html;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager.BadTokenException;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewAnimator;

import com.mappn.gfan.R;
import com.mappn.gfan.common.ApiAsyncTask.ApiRequestListener;
import com.mappn.gfan.common.MarketAPI;
import com.mappn.gfan.common.util.AlixId;
import com.mappn.gfan.common.util.DBUtils;
import com.mappn.gfan.common.util.DialogUtil;
import com.mappn.gfan.common.util.DialogUtil.ProgressDialogListener;
import com.mappn.gfan.common.util.DialogUtil.WarningDialogListener;
import com.mappn.gfan.common.util.MobileSecurePayHelper;
import com.mappn.gfan.common.util.MobileSecurePayer;
import com.mappn.gfan.common.util.TopBar;
import com.mappn.gfan.common.util.Utils;
import com.mappn.gfan.common.vo.CardInfo;
import com.mappn.gfan.common.vo.CardsVerification;
import com.mappn.gfan.common.vo.CardsVerifications;
import com.mappn.gfan.common.widget.BaseActivity;
import com.mappn.gfan.common.widget.TitleSpinner;

public class PayMainActivity extends BaseActivity implements OnClickListener, ApiRequestListener,
		ProgressDialogListener, WarningDialogListener, OnCheckedChangeListener {

	// loading view
	private ViewAnimator mCenterArea;
	private TextView mHintView;
	private Button mRetryButton;
	private ProgressBar mProgressBar;

	// main view
	private TitleSpinner mCardsSpinner;
	private TitleSpinner mDenominationSpinner;
	private EditText mCardNumberEditText;
	private EditText mCardPasswordEditText;

	private static final int DIALOG_PROGRESS_BAR = 0;
	private static final int DIALOG_QUERY_CREDIT = DIALOG_PROGRESS_BAR + 1;

	private static final int DIALOG_CARD_IS_EMPTY = DIALOG_QUERY_CREDIT + 1;
	private static final int DIALOG_PASSWORD_IS_EMPTY = DIALOG_CARD_IS_EMPTY + 1;
	private static final int DIALOG_CHECKBOX_IS_EMPTY = DIALOG_PASSWORD_IS_EMPTY + 1;
	private static final int DIALOG_CONFIRM = DIALOG_CHECKBOX_IS_EMPTY + 1;
	private static final int DIALOG_ERROR_1 = DIALOG_CONFIRM + 1;
	private static final int DIALOG_ERROR_2 = DIALOG_ERROR_1 + 1;
	private static final int DIALOG_ERROR_3 = DIALOG_ERROR_2 + 1;
	private static final int DIALOG_CHARGE_SUCCESS = DIALOG_ERROR_3 + 1;
	private static final int DIALOG_ACCOUNT_NUM_WRONG = DIALOG_CHARGE_SUCCESS + 1;
	private static final int DIALOG_PSD_NUM_WRONG = DIALOG_ACCOUNT_NUM_WRONG + 1;
	private static final int DIALOG_UNKNOWN_ERROR = DIALOG_PSD_NUM_WRONG + 1;
	private static final int DIALOG_OUT_TIME = DIALOG_UNKNOWN_ERROR + 1;
	private static final int DIALOG_CHARGE_FAILED = DIALOG_OUT_TIME + 1;
	private static final int DIALOG_CHARGE_CONNECT_FAILED = DIALOG_CHARGE_FAILED + 1;
	private static final int DIALOG_START_ERROR = DIALOG_CHARGE_CONNECT_FAILED + 1;
	private static final int DIALOG_CHARGE_CARD_ERROR = DIALOG_START_ERROR + 1;
	private static final int DIALOG_CHARGE_CARD_NO_ENOUGH_BALANCE_ERROR = DIALOG_CHARGE_CARD_ERROR + 1;
	private static final int DIALOG_CHARGE_NETWORK_ERROR = DIALOG_CHARGE_CARD_NO_ENOUGH_BALANCE_ERROR + 1;
	private static final int DIALOG_CHARGE_CARD_OR_PWD_FAILED = DIALOG_CHARGE_NETWORK_ERROR + 1;
	private static final int DIALOG_NO_CARD_CHOOSE = DIALOG_CHARGE_CARD_OR_PWD_FAILED + 1;
	private static final int DIALOG_CHARGE_INFO = DIALOG_NO_CARD_CHOOSE + 1;

	private CardsVerifications mCardVerifications;
	private int mCredit;
	private CardInfo mCard;
	private CardsVerification mCardVerification;
	private int[] cardMoney;
	private int checkedId = -1;
	// 判断充值超时的基数
	private long lastTime;
	private String mOrderID;
	// 支付类型
	private String mType;
	// 支付宝UI组件
	private EditText mInputEditText;

	private boolean mIsOnPause;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.market_activity_pay_main);

		mType = getIntent().getStringExtra("type");

		if (mType == null) {
			// 从其他入口进入的情况，跳转到充值方式列表内选择一种方式
			finish();
			startActivity(new Intent(getApplicationContext(), ChargeTypeListActivity.class));
			return;
		}

		if (ChargeTypeListActivity.TYPE_PHONECARD.equals(mType)) {
			initPhoneCardView();
			initData();
		} else if (ChargeTypeListActivity.TYPE_ALIPAY.equals(mType)) {
			initAlipayView();
		}

		CheckBox cbDefault = (CheckBox) findViewById(R.id.cb_make_default_charge_type);
		cbDefault.setChecked(mType.equals(mSession.getDefaultChargeType()));
		cbDefault.setOnCheckedChangeListener(this);
	}

	private void initPhoneCardView() {
		initTopBar(R.layout.market_activity_pay_main, R.string.charge);

		// init loading view
		mCenterArea = (ViewAnimator) findViewById(R.id.va_center_area);
		mHintView = (TextView) findViewById(R.id.tv_hint);
		mRetryButton = (Button) findViewById(R.id.btn_retry);
		mRetryButton.setOnClickListener(this);
		mProgressBar = (ProgressBar) findViewById(R.id.pb_progress);
		// init main view
		mCardsSpinner = (TitleSpinner) findViewById(R.id.ts_cards);
		mDenominationSpinner = (TitleSpinner) findViewById(R.id.ts_denomination);
		mCardNumberEditText = (EditText) findViewById(R.id.et_cardNumber);
		mCardPasswordEditText = (EditText) findViewById(R.id.et_cardPassword);

		((TextView) findViewById(R.id.tv_charge_tip)).setOnClickListener(this);
		((Button) findViewById(R.id.btn_charge)).setOnClickListener(this);
	}

	private void initAlipayView() {
		initTopBar(R.layout.market_activity_pay_main_alipay, R.string.charge_alipay);

		((TextView) findViewById(R.id.tvContent)).setText(getString(R.string.alipay_charge_content,
				mSession.getUserName(), getIntent().getIntExtra("balance", 0)));

		if (!getIntent().hasExtra("balance")) { // 从市场详细页进入时，需要查询余额
			showDialog(DIALOG_PROGRESS_BAR);
			MarketAPI.getBalance(this, this);
		}

		final Button btnOk = ((Button) findViewById(R.id.btn_charge_alipay));
		btnOk.setOnClickListener(this);

		final TextView tvInfo = (TextView) findViewById(R.id.tv_info);

		mInputEditText = (EditText) findViewById(R.id.et_input);
		mInputEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				if (s.length() > 0) {
					btnOk.setEnabled(true);
					tvInfo.setText(getString(R.string.alipay_charge_info) + "  价值"
							+ (Integer.valueOf(s.toString()) * 10) + "机锋券");
				} else {
					btnOk.setEnabled(false);
					tvInfo.setText(R.string.alipay_charge_info);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
		});

		if (getIntent().hasExtra("payment")) {
			int gVolume = getIntent().getIntExtra("payment", 100);
			int money = (int) Math.ceil((double) gVolume / 10.00);
			money = Math.min(money, 999);
			mInputEditText.setText(money + "");
		} else {
			mInputEditText.setText("10");
		}

		Selection.setSelection(mInputEditText.getText(), mInputEditText.length());
	}

	/**
	 * 初始化topbar
	 */
	private void initTopBar(int lay, int str) {
		setContentView(lay);
		TopBar.createTopBar(this, new View[] { findViewById(R.id.top_bar_title) },
				new int[] { View.VISIBLE }, getString(str));

		TextView changeDefaultChargeType = new TextView(this);
		changeDefaultChargeType.setId(100);
		changeDefaultChargeType.setTextColor(Color.WHITE);
		changeDefaultChargeType.setFocusable(true);
		changeDefaultChargeType.setClickable(true);
		changeDefaultChargeType.setOnClickListener(this);
		changeDefaultChargeType.setText(Html
				.fromHtml(getString(R.string.change_default_charge_type)));

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
		params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
		params.rightMargin = 10;
		((RelativeLayout) findViewById(R.id.top_bar)).addView(changeDefaultChargeType, params);
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);

		if (dialog.isShowing()) {
			dialog.dismiss();
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {

		switch (id) {
		case DIALOG_PROGRESS_BAR:
			return DialogUtil.createIndeterminateProgressWhiteTextDialog(this, id,
					getString(R.string.querying), false, this);
		case DIALOG_QUERY_CREDIT:
			return DialogUtil.createShowHintOKDialog(this, id,
					getString(R.string.pay_main_option_3),
					getString(R.string.hint_query_credit, mCredit));
		case DIALOG_CARD_IS_EMPTY:
			return DialogUtil.createOKWarningDialog(this, id,
					getString(R.string.warning_account_is_empty), this);
		case DIALOG_PASSWORD_IS_EMPTY:
			return DialogUtil.createOKWarningDialog(this, id,
					getString(R.string.warning_password_is_empty), this);
		case DIALOG_CHECKBOX_IS_EMPTY:
			return DialogUtil.createOKWarningDialog(this, id,
					getString(R.string.warning_checkbox_is_empty), this);
		case DIALOG_CONFIRM:
			return DialogUtil.createYesNo2TVDialog(this, id,
					String.format(getString(R.string.hint_confirm), cardMoney[checkedId],
							mCardVerification.name), getString(R.string.warning_confirm), this);
		case DIALOG_ERROR_1:
			return DialogUtil
					.createOKWarningDialog(this, id, getString(R.string.pay_error_1), this);
		case DIALOG_ERROR_2:
			return DialogUtil
					.createOKWarningDialog(this, id, getString(R.string.pay_error_2), this);
		case DIALOG_ERROR_3:
			return DialogUtil
					.createOKWarningDialog(this, id, getString(R.string.pay_error_3), this);
		case DIALOG_ACCOUNT_NUM_WRONG:
			return DialogUtil.createOKWarningDialog(
					this,
					id,
					getString(R.string.warning_card_account_num_wrong, mCardVerification.name,
							mCardVerification.accountNum), this);
		case DIALOG_PSD_NUM_WRONG:
			return DialogUtil.createOKWarningDialog(
					this,
					id,
					getString(R.string.warning_card_psd_num_wrong, mCardVerification.name,
							mCardVerification.passwordNum), this);
		case DIALOG_UNKNOWN_ERROR:
			return DialogUtil.createOKWarningDialog(this, id, getString(R.string.unknown_error),
					this);
		case DIALOG_OUT_TIME:
			return DialogUtil.createOKWarningDialog(this, id, getString(R.string.timeout_error),
					this);
		case DIALOG_CHARGE_SUCCESS:
            if (ChargeTypeListActivity.TYPE_PHONECARD.equals(mType)) {
                return DialogUtil.createOKWarningDialog(
                        this,
                        id,
                        getString(R.string.charge_success, cardMoney[checkedId],
                                cardMoney[checkedId] * 10), this);
            } else {
                String paymentStr = mInputEditText.getText().toString();
                int payment = Integer.parseInt(paymentStr);
                return DialogUtil.createOKWarningDialog(this, id,
                        getString(R.string.charge_success, payment, payment * 10), this);
            }
		case DIALOG_CHARGE_FAILED:
			return DialogUtil.createOKWarningDialog(this, id, getString(R.string.unknown_error),
					null);
		case DIALOG_CHARGE_CONNECT_FAILED:
		case DIALOG_CHARGE_NETWORK_ERROR:
			return DialogUtil
					.createOKWarningDialog(this, id, getString(R.string.pay_error_3), null);
		case DIALOG_START_ERROR:
			return DialogUtil.createOKWarningDialog(this, id, getString(R.string.user_error), null);
		case DIALOG_CHARGE_CARD_ERROR:
		case DIALOG_CHARGE_CARD_OR_PWD_FAILED:
			return DialogUtil
					.createOKWarningDialog(this, id, getString(R.string.pay_error_1), null);
		case DIALOG_CHARGE_CARD_NO_ENOUGH_BALANCE_ERROR:
			return DialogUtil.createOKWarningDialog(this, id, getString(R.string.balance_error),
					null);
		case DIALOG_NO_CARD_CHOOSE:
			return DialogUtil.createOKWarningDialog(this, id,
					getString(R.string.no_card_choose_error), null);
		case DIALOG_CHARGE_INFO:
			return new AlertDialog.Builder(this).setMessage(R.string.purchase_directions)
					.setPositiveButton(R.string.ok, null).create();
		}
		return super.onCreateDialog(id);
	}

	private void initData() {
		requestData();
	}

	private void showHint(String error, boolean withRetry) {
		mHintView.setText(error);
		mProgressBar.setVisibility(View.GONE);
		mRetryButton.setVisibility(withRetry ? View.VISIBLE : View.GONE);
		mCenterArea.setDisplayedChild(0);
	}

	private void showLoadingHint() {
		mHintView.setText(R.string.loading);
		mProgressBar.setVisibility(View.VISIBLE);
		mRetryButton.setVisibility(View.GONE);
		mCenterArea.setDisplayedChild(0);
	}

	private void showListView() {
		mProgressBar.setVisibility(View.GONE);
		mCenterArea.setDisplayedChild(1);

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, mCardVerifications.getCardNames());
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mCardsSpinner.setAdapter(adapter);
		mCardsSpinner.setOnClickListener(new android.content.DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				PayMainActivity.this.mCard = new CardInfo();
				PayMainActivity.this.mCardVerification = PayMainActivity.this.mCardVerifications.cards
						.get(which);
				String[] tempCardMoney = PayMainActivity.this.mCardVerification.credit.split(",");
				int len = tempCardMoney.length;
				PayMainActivity.this.cardMoney = new int[len];

				String[] cardMoneyString = new String[len];

				for (int i = 0; i < len; i++) {
					PayMainActivity.this.cardMoney[i] = Integer.parseInt(tempCardMoney[i]);
					cardMoneyString[i] = PayMainActivity.this.getString(R.string.pay_unit,
							PayMainActivity.this.cardMoney[i]);
				}

				PayMainActivity.this.mCard.payType = PayMainActivity.this.mCardVerification.pay_type;

				ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(PayMainActivity.this,
						android.R.layout.simple_spinner_item, cardMoneyString);

				adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				PayMainActivity.this.mDenominationSpinner.setAdapter(adapter2);
				PayMainActivity.this.mDenominationSpinner
						.setOnClickListener(new android.content.DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								PayMainActivity.this.checkedId = which;
							}
						});

				String cardNumHint = "";
				if (PayMainActivity.this.mCardVerification != null)
					cardNumHint = PayMainActivity.this.getString(R.string.input_limit,
							PayMainActivity.this.mCardVerification.accountNum);

				PayMainActivity.this.mCardNumberEditText
						.setHint(getString(R.string.card_number_hint) + cardNumHint);

				String cardPsdHint = "";
				if (PayMainActivity.this.mCardVerification != null)
					cardPsdHint = PayMainActivity.this.getString(R.string.input_limit,
							PayMainActivity.this.mCardVerification.passwordNum);
				;
				PayMainActivity.this.mCardPasswordEditText
						.setHint(getString(R.string.card_password_hint) + cardPsdHint);
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_retry:
			onClickRetry();
			break;
		case R.id.btn_charge:
			onClickOk();
			break;
		case R.id.tv_charge_tip:
			if (!isFinishing()) {
				showDialog(DIALOG_CHARGE_INFO);
			}
			break;
		case R.id.btn_charge_alipay:
			String paymentStr = mInputEditText.getText().toString();
			alipay(paymentStr);
			break;
		case 100:
			// 更改默认充值方式
			startChargeTypeListActivity(false);
			break;
		}
	}

	// 更改默认充值方式
	private void startChargeTypeListActivity(boolean hasError) {
		finish();
		Intent intent = new Intent(this, ChargeTypeListActivity.class);
		// 将余额和支付金额带上
		intent.putExtras(getIntent());
		if (hasError) {
			intent.putExtra("error", mType);
		}
		startActivity(intent);
	}

	private void alipay(String paymentStr) {
		MobileSecurePayHelper mspHelper = new MobileSecurePayHelper(this);
		if (mspHelper.detectMobile_sp()) {
			try {
				showDialog(DIALOG_PROGRESS_BAR);

				MarketAPI.getAliPayOrder(this, this, Integer.parseInt(paymentStr),
						getString(R.string.alipay_product_name),
						getString(R.string.alipay_product_desc));
			} catch (Exception e) {
				Utils.W("alipay", e);
			}
		}
	}

	private void onClickOk() {
		String card = mCardNumberEditText.getText().toString();
		String password = mCardPasswordEditText.getText().toString();
		if (mCard == null) {
			if (!isFinishing()) {
				showDialog(DIALOG_NO_CARD_CHOOSE);
			}
		} else if (checkedId == -1) {
			if (!isFinishing()) {
				showDialog(DIALOG_CHECKBOX_IS_EMPTY);
			}
		} else if (TextUtils.isEmpty(card)) {
			if (!isFinishing()) {
				showDialog(DIALOG_CARD_IS_EMPTY);
			}
		} else if (TextUtils.isEmpty(password)) {
			if (!isFinishing()) {
				showDialog(DIALOG_PASSWORD_IS_EMPTY);
			}
		} else if (card.length() != mCardVerification.accountNum) {
			if (!isFinishing()) {
				showDialog(DIALOG_ACCOUNT_NUM_WRONG);
			}
		} else if (password.length() != mCardVerification.passwordNum) {
			if (!isFinishing()) {
				showDialog(DIALOG_PSD_NUM_WRONG);
			}
		} else {
			mCard.cardAccount = mCardNumberEditText.getText().toString();
			mCard.cardPassword = mCardPasswordEditText.getText().toString();
			mCard.cardCredit = cardMoney[checkedId] * 100;
			if (!isFinishing()) {
				showDialog(DIALOG_CONFIRM);
			}
		}
	}

	private void onClickRetry() {
		showLoadingHint();
		requestData();
	}

	private void requestData() {
		MarketAPI.syncCardInfo(this, this);
	}

	private boolean isOutTime() {
		if (System.currentTimeMillis() - lastTime > 1 * 60 * 1000) {
			return true;
		} else {
			return false;
		}
	}

	private void requestQuery() {
		MarketAPI.queryChargeResult(this, this, mOrderID);
	}

	@Override
	public void onWarningDialogOK(int id) {
		switch (id) {
		case DIALOG_CARD_IS_EMPTY:
			mCardNumberEditText.requestFocus();
			break;
		case DIALOG_PASSWORD_IS_EMPTY:
			mCardPasswordEditText.requestFocus();
			break;
		case DIALOG_CHECKBOX_IS_EMPTY:
			break;
		case DIALOG_CONFIRM:
			requestCharge();
			if (!isFinishing()) {
				showDialog(DIALOG_PROGRESS_BAR);
			}
			break;
		case DIALOG_ERROR_1:
			mCardNumberEditText.requestFocus();
			break;
		case DIALOG_ERROR_2:
			mCardNumberEditText.requestFocus();
			break;
		case DIALOG_ERROR_3:
			mCardNumberEditText.requestFocus();
			break;
		case DIALOG_ACCOUNT_NUM_WRONG:
			mCardNumberEditText.requestFocus();
			break;
		case DIALOG_PSD_NUM_WRONG:
			mCardPasswordEditText.requestFocus();
			break;
		case DIALOG_CHARGE_SUCCESS:
			finish();
			break;
		}
	}

	private void requestCharge() {
		MarketAPI.charge(this, this, null, "GFanClient", mCard);
	}

	@Override
	public void onSuccess(int method, Object obj) {
		switch (method) {
		case MarketAPI.ACTION_CHARGE:

			mOrderID = (String) obj;
			lastTime = System.currentTimeMillis();
			requestQuery();
			break;

		case MarketAPI.ACTION_QUERY_CHARGE_BY_ORDERID:

			removeDialog(DIALOG_PROGRESS_BAR);
			if (!isFinishing()) {
				showDialog(DIALOG_CHARGE_SUCCESS);
			}
			break;

		case MarketAPI.ACTION_SYNC_CARDINFO:

			mCardVerifications = (CardsVerifications) obj;
			if (mCardVerifications == null)
				showHint(getString(R.string.hint_sync_charge_info_error), true);
			else {
				mSession.setCreditCardVersion(mCardVerifications.version);
				DBUtils.updataCardsVerification(this, mCardVerifications.cards);
				showListView();
			}
			break;

		case MarketAPI.ACTION_GET_BALANCE:
			removeDialog(DIALOG_PROGRESS_BAR);
			final int balance = Integer.parseInt((String) obj);
			getIntent().putExtra("balance", balance);

			((TextView) findViewById(R.id.tvContent)).setText(getString(
					R.string.alipay_charge_content, mSession.getUserName(), balance));
			break;

		case MarketAPI.ACTION_GET_ALIPAY_ORDER_INFO:
			String orderParams;

			try {
				JSONObject orderInfo = (JSONObject) obj;
				int result = orderInfo.getInt("resultCode");
				if (1 == result) {
					orderParams = orderInfo.getString("alipayParam");
					mOrderID = orderInfo.getString("orderNo");

					MobileSecurePayer msp = new MobileSecurePayer();

					boolean bRet = msp.pay(orderParams, mHandler, AlixId.RQF_PAY, this);
					if (!bRet) {
						removeDialog(DIALOG_PROGRESS_BAR);
						showDialog(DIALOG_CHARGE_FAILED);
					}
				} else {
					// mErrorStatus = result;
					removeDialog(DIALOG_PROGRESS_BAR);
					showDialog(DIALOG_CHARGE_FAILED);
				}
			} catch (JSONException e) {
				// mErrorStatus = 0;
				try {
					removeDialog(DIALOG_PROGRESS_BAR);
					showDialog(DIALOG_CHARGE_FAILED);
				} catch (BadTokenException e1) {
				}
			} catch (BadTokenException e) {
			}

			break;

		case MarketAPI.ACTION_QUERY_ALIPAY_RESULT:
			JSONObject orderInfo = (JSONObject) obj;

			try {
				int code = orderInfo.getInt("resultCode");

				if (2 == code) {
					if (isOutTime()) {
						// mErrorStatus = code;
						removeDialog(DIALOG_PROGRESS_BAR);
						showDialog(DIALOG_OUT_TIME);
					} else {
						new Thread(new Runnable() {

							@Override
							public void run() {
								try {
									Thread.sleep(3000L);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}

								MarketAPI.queryAliPayResult(PayMainActivity.this,
										PayMainActivity.this, PayMainActivity.this.mOrderID);
							}

						}).start();
					}
				} else if (1 == code) {
					removeDialog(DIALOG_PROGRESS_BAR);
					showDialog(DIALOG_CHARGE_SUCCESS);
				} else {
					// mErrorStatus = code;
					removeDialog(DIALOG_PROGRESS_BAR);
					showDialog(DIALOG_CHARGE_FAILED);
				}
			} catch (JSONException e) {
				// mErrorStatus = 0;
				try {
					removeDialog(DIALOG_PROGRESS_BAR);
					showDialog(DIALOG_CHARGE_FAILED);
				} catch (BadTokenException e1) {
				}
			} catch (BadTokenException e) {
			}

			break;

		default:
			break;
		}
	}

	@Override
	public void onError(int method, int statusCode) {

		if (statusCode == 204) {
			mCardVerifications = DBUtils.getAllCardsVerification(this);
			if (mCardVerifications.getCardNames() == null) {
				if (mSession.getCreditCardVersion() > -1) {
					mSession.setCreditCardVersion(-1);
					requestData();
				} else {
					showHint(getString(R.string.hint_sync_charge_info_error), true);
				}
			} else
				showListView();

			return;
		}

		switch (method) {
		case MarketAPI.ACTION_CHARGE:
			removeDialog(DIALOG_PROGRESS_BAR);

			if (!isFinishing()) {
				showDialog(DIALOG_CHARGE_CONNECT_FAILED);
			}
			break;

		case MarketAPI.ACTION_QUERY_CHARGE_BY_ORDERID:
			if (statusCode == 224) {
				if (!isOutTime()) {
					requestQuery();
				} else {
					removeDialog(DIALOG_PROGRESS_BAR);
					if (!isFinishing() && !mIsOnPause) {
						showDialog(DIALOG_OUT_TIME);
					}
				}
			} else if (statusCode == 221) {
				removeDialog(DIALOG_PROGRESS_BAR);
				if (!isFinishing() && !mIsOnPause) {
					showDialog(DIALOG_CHARGE_FAILED);
				}
			} else if (statusCode == 223) {
				removeDialog(DIALOG_PROGRESS_BAR);
				if (!isFinishing() && !mIsOnPause) {
					showDialog(DIALOG_CHARGE_CARD_OR_PWD_FAILED);
				}
			} else if (statusCode == 220) {
				removeDialog(DIALOG_PROGRESS_BAR);
				if (!isFinishing() && !mIsOnPause) {
					showDialog(DIALOG_CHARGE_CARD_NO_ENOUGH_BALANCE_ERROR);
				}
			} else {
				removeDialog(DIALOG_PROGRESS_BAR);
				if (!isFinishing() && !mIsOnPause) {
					showDialog(DIALOG_UNKNOWN_ERROR);
				}
			}
			break;

		case MarketAPI.ACTION_SYNC_CARDINFO:

			showHint(getString(R.string.hint_sync_charge_info_error), true);
			break;

		case MarketAPI.ACTION_GET_BALANCE:

			removeDialog(DIALOG_PROGRESS_BAR);
			showDialog(DIALOG_CHARGE_FAILED);
			break;

		case MarketAPI.ACTION_QUERY_ALIPAY_RESULT:

			removeDialog(DIALOG_PROGRESS_BAR);
			showDialog(DIALOG_CHARGE_FAILED);
			break;

		case MarketAPI.ACTION_GET_ALIPAY_ORDER_INFO:

			removeDialog(DIALOG_PROGRESS_BAR);
			showDialog(DIALOG_CHARGE_FAILED);
			break;

		default:
			break;
		}
	}

	// 支付宝用
	public static class AlixOnCancelListener implements DialogInterface.OnCancelListener {
		Activity mcontext;

		public AlixOnCancelListener(Activity context) {
			mcontext = context;
		}

		@Override
		public void onCancel(DialogInterface dialog) {
			mcontext.onKeyDown(KeyEvent.KEYCODE_BACK, null);
		}
	}

	// alipay
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			try {
				String strRet = (String) msg.obj;

				switch (msg.what) {
				case AlixId.RQF_PAY:
					try {
						int resultStatus = Integer.valueOf(strRet.split(";")[0].split("=")[1]
								.replace("{", "").replace("}", ""));

						if (6001 == resultStatus || 4000 == resultStatus) {
							// 用户取消 或 格式错误
							try {
								PayMainActivity.this.removeDialog(DIALOG_PROGRESS_BAR);
							} catch (BadTokenException e) {
							}
							break;
						}

						PayMainActivity.this.lastTime = System.currentTimeMillis();
						MarketAPI.queryAliPayResult(PayMainActivity.this, PayMainActivity.this,
								PayMainActivity.this.mOrderID);
					} catch (Exception e) {
						try {
							PayMainActivity.this.removeDialog(DIALOG_PROGRESS_BAR);
							PayMainActivity.this.showDialog(DIALOG_CHARGE_FAILED);
						} catch (BadTokenException e1) {
						}
					}
					break;
				}

				super.handleMessage(msg);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	@Override
	public void onWarningDialogCancel(int id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProgressDialogCancel(int id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked) {
			mSession.setDefaultChargeType(mType);
		} else
			mSession.setDefaultChargeType(null);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mIsOnPause = false;
	}

	@Override
	protected void onPause() {
		super.onPause();
		mIsOnPause = true;
	}
}