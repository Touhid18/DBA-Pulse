/**
 * 
 */
package com.dbaservicesptyltd.dbaservices.fragments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.dbaservicesptyltd.dbaservices.R;
import com.dbaservicesptyltd.dbaservices.adapter.NotifAdapter;
import com.dbaservicesptyltd.dbaservices.interfaces.AdminClickListener;
import com.dbaservicesptyltd.dbaservices.model.NotifItem;
import com.dbaservicesptyltd.dbaservices.model.OnlineAdminRow;
import com.dbaservicesptyltd.dbaservices.model.ServerResponse;
import com.dbaservicesptyltd.dbaservices.parser.JsonParser;
import com.dbaservicesptyltd.dbaservices.utils.Constants;
import com.dbaservicesptyltd.dbaservices.utils.DBAServiceApplication;

/**
 * @author Touhid
 * 
 */
public class JobsInProgressFragment extends Fragment {

	private static Context tContext;
	private static final String TAG = "JobsInProgressActivity";
	private static ArrayList<NotifItem> jobsList;
	private NotifAdapter jobsAdapter;
	private OnlineAdminRow adminObj;

	@SuppressWarnings("unused")
	private AdminClickListener adminClickListener;

	private ImageView ivRefresh;
	private boolean isNewRefresh;

	private ProgressDialog pDialog;
	private JsonParser jsonParser;

	public JobsInProgressFragment(Context context, OnlineAdminRow admin, AdminClickListener adminClicker) {
		tContext = context;
		adminObj = admin;
		adminClickListener = adminClicker;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "inside OncreateView()");
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.jobs_progress, container, false);
		// formAdminObj();
		setAdminViews(rootView);

		pDialog = new ProgressDialog(tContext);
		jsonParser = new JsonParser();

		setRefreshAction(rootView);
		ListView lvNotifs = (ListView) rootView.findViewById(R.id.lv_jobs);

		jobsList = new ArrayList<NotifItem>();
		jobsAdapter = new NotifAdapter(tContext, R.layout.notif_row, jobsList);
		lvNotifs.setAdapter(jobsAdapter);
		lvNotifs.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				showActionDialog((NotifItem) parent.getItemAtPosition(position));
			}
		});
		new GetAdminJobs().execute();
		getTag();
		return rootView;
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		// super.setUserVisibleHint(isVisibleToUser);
		// isNewRefresh = true;
		// if (!isVisibleToUser)
		// adminClickListener.handleClick(false, adminObj); //TODO remove this
		// page by interface calling
	}

	private void setAdminViews(View rootView) {
		ImageView ivStatus = (ImageView) rootView.findViewById(R.id.iv_status);
		if (adminObj != null && adminObj.isOnline())
			ivStatus.setImageBitmap(BitmapFactory.decodeResource(tContext.getResources(), R.drawable.status_online));
		((TextView) rootView.findViewById(R.id.tv_admin_name)).setText(adminObj.getAdminName());
		((TextView) rootView.findViewById(R.id.tv_active_count)).setText(adminObj.getActive() + "");
		((TextView) rootView.findViewById(R.id.tv_pending_count)).setText(adminObj.getPending() + "");
		((TextView) rootView.findViewById(R.id.tv_resolved_count)).setText(adminObj.getResolved() + "");
	}

	// private void formAdminObj() {
	// Intent intent = getIntent();
	// String adminName = intent.getStringExtra(Constants.U_NAME);
	// int userId = intent.getIntExtra(Constants.U_ID, 0);
	// int active = intent.getIntExtra(Constants.U_ACTIVE_COUNT, 0);
	// int pending = intent.getIntExtra(Constants.U_PENDING_COUNT, 0);
	// int resolved = intent.getIntExtra(Constants.U_RESOLVED_COUNT, 0);
	// boolean isOnline = intent.getBooleanExtra(Constants.U_IS_ONLINE, false);
	// Log.d(TAG, "Admin values: " + adminName + ", " + userId + ", " + active +
	// ", " + pending + ", " + resolved
	// + ", " + isOnline);
	// adminObj = new OnlineAdminRow(adminName, userId, active, pending,
	// resolved, isOnline);
	// }

	private void setRefreshAction(ViewGroup rootView) {
		ivRefresh = (ImageView) rootView.findViewById(R.id.iv_refresh_jobs);
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

	private void showActionDialog(final NotifItem notifItem) {
		final Dialog dialog = new Dialog(tContext, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
		dialog.setContentView(R.layout.action_dialog);
		dialog.findViewById(R.id.btn_action).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.cancel();
				new DecideNotification().execute(Constants.NOTIF_TYPE_ACTIONED, notifItem.getId());
				new GetAdminJobs().execute();
			}
		});
		dialog.findViewById(R.id.btn_ignore).setVisibility(View.GONE);
		dialog.findViewById(R.id.btn_resolved).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.cancel();
				new DecideNotification().execute(Constants.NOTIF_TYPE_RESOLVED, notifItem.getId());
				new GetAdminJobs().execute();
			}
		});
		String head = "";
		if (notifItem.getSeverity() == Constants.NOTIF_SEVERITY_ALERT)
			head = "CRITICAL ALERT RECEIVED";
		else if (notifItem.getSeverity() == Constants.NOTIF_SEVERITY_WARNING)
			head = "WARNING RECEIVED";
		else
			head = "Notification Received";
		TextView tvHead = (TextView) dialog.findViewById(R.id.tv_dialog_head);
		tvHead.setText(Html.fromHtml("<u>" + head + "</u>"));
		TextView tvBody = (TextView) dialog.findViewById(R.id.tv_dialog_body);
		tvBody.setText(notifItem.getDatetime() + " " + notifItem.getDescription() + ", " + notifItem.getClientName()
				+ ", " + notifItem.getUpdated());
		// Center-focus the dialog
		Window window = dialog.getWindow();
		window.setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		window.setGravity(Gravity.CENTER);
		dialog.show();
	}

	private class GetAdminJobs extends AsyncTask<Void, Void, JSONObject> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (!pDialog.isShowing() && !isNewRefresh) {
				pDialog.setMessage("Refreshing job list ...");
				pDialog.setCancelable(false);
				pDialog.setIndeterminate(true);
				// pDialog.show();
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
						sortJobsList();

						jobsAdapter.setData(jobsList);
						jobsAdapter.notifyDataSetChanged();
						ivRefresh.clearAnimation();
					} else {
						alert("Invalid token!");
					}
				} catch (JSONException e) {
					alert("Malformed data received!");
					e.printStackTrace();
				}
			}

			if (pDialog.isShowing())
				pDialog.dismiss();
		}
	}

	private class DecideNotification extends AsyncTask<Integer, Void, JSONObject> {

		private int actionCode;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (!pDialog.isShowing() && !isNewRefresh) {
				pDialog.setMessage("Deciding the issue ...");
				pDialog.setCancelable(false);
				pDialog.setIndeterminate(true);
				pDialog.show();
			}
		}

		@Override
		protected JSONObject doInBackground(Integer... params) {
			String url = Constants.URL_PARENT + "notifications";
			actionCode = (int) params[0];
			int notifId = (int) params[1];

			JSONObject jObj = new JSONObject();
			try {
				jObj.put("id", notifId);
				jObj.put("status", actionCode);
			} catch (JSONException e) {
				e.printStackTrace();
			}

			ServerResponse response = jsonParser.retrieveServerData(Constants.REQUEST_TYPE_POST, url, null,
					jObj.toString(), DBAServiceApplication.getAppAccessToken(tContext));
			if (response.getStatus() == 200) {
				Log.d(">>>><<<<", "success in retrieving notifications.");
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
						if (actionCode == Constants.NOTIF_TYPE_ACTIONED)
							alert("Issue marked as actioned.");
						else if (actionCode == Constants.NOTIF_TYPE_RESOLVED)
							alert("Issue marked as resolved.");
						else if (actionCode == Constants.NOTIF_TYPE_IGNORED)
							alert("Issue marked as ignored. This will again be prompted within 7 minutes");
						ivRefresh.clearAnimation();
					} else {
						alert("Invalid token.");
						ivRefresh.clearAnimation();
					}
				} catch (JSONException e) {
					alert("Malformed data received!");
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

	private void sortJobsList() {
		Collections.sort(jobsList, new Comparator<NotifItem>() {
			@Override
			public int compare(NotifItem lhs, NotifItem rhs) {
				int id1 = lhs.getId();
				int id2 = rhs.getId();
				return id1 < id2 ? 1 : id1 == id2 ? 0 : -1;
			}
		});
	}

}
