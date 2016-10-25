package com.poponews.global.wxapi;


import com.gmobi.poponews.R;
import com.gmobi.poponews.cases.main.MainActivity;
import com.momock.util.Logger;
import com.tencent.connect.common.Constants;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.ShowMessageFromWX;
import com.tencent.mm.sdk.modelmsg.WXAppExtendObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {
	
	private static final int TIMELINE_SUPPORTED_VERSION = 0x21020001;

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_blank);
        MainActivity.wxApi.handleIntent(getIntent(), this);
    }

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		setIntent(intent);
		MainActivity.wxApi.handleIntent(intent, this);
	}


	@Override
	public void onReq(BaseReq baseReq) {

	}

	@Override
	public void onResp(BaseResp resp) {
		int result = 0;

		switch (resp.errCode) {
			case BaseResp.ErrCode.ERR_OK:
				result = R.string.share_success;
				break;
			case BaseResp.ErrCode.ERR_USER_CANCEL:
				result = R.string.share_fail;
				break;
			case BaseResp.ErrCode.ERR_AUTH_DENIED:
				result = R.string.share_fail;
				break;
			default:
				result = R.string.share_fail;
				break;
		}

		Toast.makeText(this, result, Toast.LENGTH_LONG).show();
		finish();
	}


}