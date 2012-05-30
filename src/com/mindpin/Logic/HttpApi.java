package com.mindpin.Logic;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import com.mindpin.base.http.MindpinPostRequest;

public class HttpApi {
	
	public static final String SITE = "http://192.168.1.28:9527";
	
	// ����·������
	public static final String �û���¼				= "/login";
	
	public static final String ͬ������ 			    = "/api0/mobile_data_syn";
	
	// LoginActivity
	// �û���¼����
	public static boolean user_authenticate(String email, String password) throws Exception {
		return new MindpinPostRequest<Boolean>(
			�û���¼, 
			new BasicNameValuePair("email", email),
			new BasicNameValuePair("password", password)
		){
			@Override
			public Boolean on_success(String response_text) throws Exception{
				JSONObject json = new JSONObject(response_text);
				AccountManager.login(get_cookies(), json.toString());
				return true;
			}
		}.go();
	}
	
	public static boolean mobile_data_syn() throws Exception {
	  return true;
	}
	
	public static class IntentException extends Exception{
		private static final long serialVersionUID = -4969746083422993611L;
	}
}
