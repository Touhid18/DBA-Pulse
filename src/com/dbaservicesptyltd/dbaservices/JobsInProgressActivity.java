package com.dbaservicesptyltd.dbaservices;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ListView;

import com.dbaservicesptyltd.dbaservices.adapter.NotifAdapter;
import com.dbaservicesptyltd.dbaservices.model.NotifItem;
import com.dbaservicesptyltd.dbaservices.model.OnlineAdminRow;
import com.dbaservicesptyltd.dbaservices.model.ServerResponse;
import com.dbaservicesptyltd.dbaservices.parser.JsonParser;
import com.dbaservicesptyltd.dbaservices.utils.Constants;
import com.dbaservicesptyltd.dbaservices.utils.DBAServiceApplication;

public class JobsInProgressActivity extends Activity {

	private static Context tContext;
	@SuppressWarnings("unused")
	private static final String TAG = "JobsInProgressActivity";
	private static ArrayList<NotifItem> jobsList;
	private NotifAdapter jobsAdapter;
	private OnlineAdminRow adminObj;

	private ImageView ivRefresh;
	private boolean isNewRefresh;

	private ProgressDialog pDialog;
	private JsonParser jsonParser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.jobs_progress);
		tContext = JobsInProgressActivity.this;
		formAdminObj();

		pDialog = new ProgressDialog(tContext);
		jsonParser = new JsonParser();

		setRefreshAction();
		ListView lvNotifs = (ListView) findViewById(R.id.lv_jobs);

		jobsList = new ArrayList<NotifItem>();
		jobsAdapter = new NotifAdapter(tContext, R.layout.notif_row, jobsList);
		lvNotifs.setAdapter(jobsAdapter);

		new GetAdminJobs().execute();
	}

	private void formAdminObj() {
		Intent intent = getIntent();
		String adminName = intent.getStringExtra(Constants.U_NAME);
		int userId = intent.getIntExtra(Constants.U_ID, 0);
		int active = intent.getIntExtra(Constants.U_ACTIVE_COUNT, 0);
		int pending = intent.getIntExtra(Constants.U_PENDING_COUNT, 0);
		int resolved = intent.getIntExtra(Constants.U_RESOLVED_COUNT, 0);
		boolean isOnline = intent.getBooleanExtra(Constants.U_IS_ONLINE, false);
		adminObj = new OnlineAdminRow(adminName, userId, active, pending, resolved, isOnline);
	}

	private void setRefreshAction() {
		ivRefresh = (ImageView) findViewById(R.id.iv_refresh_jobs);
		final Animation rotation = AnimationUtils.loadAnimation(tContext, R.anim.rotate_refresh);
		rotation.setRepeatCount(Animation.INFINITE);
		ivRefresh.startAnimation(rotation);
		ivRefresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ivRefresh.startAnimation(rotation);
				isNewRefresh = true;
				new GetAdminJobs().execute();
			}
		});
	}

	private class GetAdminJobs extends AsyncTask<Void, Void, JSONObject> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (!pDialog.isShowing() && !isNewRefresh) {
				pDialog.setMessage("Refreshing job list ...");
				pDialog.setCancelable(false);
				pDialog.setIndeterminate(true);
				pDialog.show();
			}
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			int userId = adminObj.getUserId();
			String url = Constants.URL_PARENT + "in_progress/" + userId;

			ServerResponse response = jsonParser.retrieveServerData(Constants.REQUEST_TYPE_GET, url, null, null,
					DBAServiceApplication.getAppAccessToken(tContext));
			if (response.getStatus() == 200) {
				Log.d(">>>><<<<", "success in retrieving job list for user_id: " + userId);
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
					if (status.equals("OK")) {
						JSONArray jobsArray = responseObj.getJSONArray("jobs_in_progress");
						jobsList = NotifItem.parseNotifList(jobsArray);
						Log.e("???????", "jobs count = " + jobsList.size());

						jobsAdapter.setData(jobsList);
						jobsAdapter.notifyDataSetChanged();
						ivRefresh.clearAnimation();
					} else {
						alert("Invalid token!");
					}
				} catch (JSONException e) {
					alert("Exception.");
					e.printStackTrace();
				}
			}

			if (pDialog.isShowing())
				pDialog.dismiss();
		}
	}

	void alert(String message) {
		AlertDialog.Builder bld = new AlertDialog.Builder(tContext);
		bld.setMessage(message);
		bld.setNeutralButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		bld.create().show();
	}

}
