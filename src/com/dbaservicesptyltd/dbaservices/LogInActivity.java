/**
 * 
 */
package com.dbaservicesptyltd.dbaservices;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.dbaservicesptyltd.dbaservices.model.ServerResponse;
import com.dbaservicesptyltd.dbaservices.model.UserCred;
import com.dbaservicesptyltd.dbaservices.parser.JsonParser;
import com.dbaservicesptyltd.dbaservices.utils.Constants;
import com.dbaservicesptyltd.dbaservices.utils.DBAServiceApplication;

/**
 * @author Touhid
 * 
 */
public class LogInActivity extends Activity {

	private ProgressDialog pDialog;
	private JsonParser jsonParser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.log_in);

		new DBAServiceApplication(LogInActivity.this);
		String token = DBAServiceApplication.getAppAccessToken(LogInActivity.this);
		if (!(token.equals(null) || token.equals("none"))) {
			Intent intent = new Intent(LogInActivity.this, MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			finish();
		}

		pDialog = new ProgressDialog(LogInActivity.this);
		jsonParser = new JsonParser();

		Button btnLogIn = (Button) findViewById(R.id.btn_log_in);
		btnLogIn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				logIn();
			}
		});

	}

	private void logIn() {
		String email = ((EditText) findViewById(R.id.et_user_email)).getText().toString();
		String pwd = ((EditText) findViewById(R.id.et_pwd)).getText().toString();
		new LogInAsyncTask().execute(email, pwd);
	}

	private class LogInAsyncTask extends AsyncTask<String, Void, JSONObject> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (!pDialog.isShowing()) {
				pDialog.setMessage("Logging in ...");
				pDialog.setCancelable(false);
				pDialog.setIndeterminate(true);
				pDialog.show();
			}
		}

		@Override
		protected JSONObject doInBackground(String... params) {
			String url = Constants.URL_PARENT + "login";

			String email = params[0];
			String password = params[1];

			JSONObject loginObj = new JSONObject();
			try {
				loginObj.put("email", email);
				loginObj.put("password", password);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			String loginData = loginObj.toString();

			ServerResponse response = jsonParser.retrieveServerData(Constants.REQUEST_TYPE_POST, url, null, loginData,
					null);
			if (response.getStatus() == 200) {
				Log.d(">>>><<<<", "log in successful");
				JSONObject responseObj = response.getjObj();
				return responseObj;
			} else
				return null;
		}

		@Override
		protected void onPostExecute(JSONObject responseObj) {
			super.onPostExecute(responseObj);
			if (responseObj != null) {
				try {
					String status = responseObj.getString("status");
					Log.i("LogIn_Response", responseObj.toString());
					if (status.equals("OK")) {
						// Log.i("LogIn_ResponseOK",
						// responseObj.get("token").toString());
						completeLogin((JSONObject) responseObj.get("user"));
					} else {
						Log.e("Login_ERROR", responseObj.toString());
						// alert("Invalid log in data!");
					}
				} catch (JSONException e) {
					// alert("Exception occured during parsing the server response.");
					e.printStackTrace();
				}
			}
			if (pDialog.isShowing())
				pDialog.dismiss();
		}

	}

	private void completeLogin(JSONObject responseJson) {
		DBAServiceApplication.setUserCred(UserCred.parseUserCred(responseJson), LogInActivity.this);
		Intent intent = new Intent(LogInActivity.this, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		finish();
	}

	// void alert(String message) {
	// AlertDialog.Builder bld = new AlertDialog.Builder(LogInActivity.this);
	// bld.setMessage(message);
	// bld.setNeutralButton("OK", new DialogInterface.OnClickListener() {
	//
	// @Override
	// public void onClick(DialogInterface dialog, int which) {
	// dialog.dismiss();
	//
	// }
	// });
	// bld.create().show();
	// }

}
