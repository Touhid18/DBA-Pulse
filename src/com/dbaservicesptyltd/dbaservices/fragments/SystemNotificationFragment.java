package com.dbaservicesptyltd.dbaservices.fragments;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ListView;

import com.dbaservicesptyltd.dbaservices.R;
import com.dbaservicesptyltd.dbaservices.adapter.NotifAdapter;
import com.dbaservicesptyltd.dbaservices.model.NotifItem;
import com.dbaservicesptyltd.dbaservices.model.ServerResponse;
import com.dbaservicesptyltd.dbaservices.parser.JsonParser;
import com.dbaservicesptyltd.dbaservices.utils.Constants;
import com.dbaservicesptyltd.dbaservices.utils.DBAServiceApplication;

public class SystemNotificationFragment extends Fragment {

	private static Context tContext;
	private static final String TAG = "SystemNotificationFragment";
	private static ArrayList<NotifItem> notifList;
	private NotifAdapter notifAdapter;

	private ImageView ivRefresh;
	private boolean isNewRefresh=false;

	private ProgressDialog pDialog;
	private JsonParser jsonParser;

	public static Fragment newInstance(Context context) {
		tContext = context;
		return new SystemNotificationFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "inside OncreateView()");
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.sys_notif, container, false);

		pDialog = new ProgressDialog(tContext);
		jsonParser = new JsonParser();

		setRefreshAction(rootView);
		ListView lvNotifs = (ListView) rootView.findViewById(R.id.lv_notifs);

		notifList = new ArrayList<NotifItem>();
		notifAdapter = new NotifAdapter(tContext, R.layout.notif_row, notifList);
		lvNotifs.setAdapter(notifAdapter);

		new GetNotifications().execute();

		return rootView;
	}

	private void setRefreshAction(ViewGroup rootView) {		
		ivRefresh = (ImageView) rootView.findViewById(R.id.iv_refresh);
		final Animation rotation = AnimationUtils.loadAnimation(tContext, R.anim.rotate_refresh);
		rotation.setRepeatCount(Animation.INFINITE);
		ivRefresh.startAnimation(rotation);
		ivRefresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ivRefresh.startAnimation(rotation);
				isNewRefresh = true;
				new GetNotifications().execute();
			}
		});
	}

	private class GetNotifications extends AsyncTask<Void, Void, JSONObject> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (!pDialog.isShowing() && !isNewRefresh) {
				pDialog.setMessage("Refreshing noifictions...");
				pDialog.setCancelable(false);
				pDialog.setIndeterminate(true);
				pDialog.show();
			}
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			String url = Constants.URL_PARENT + "notifications";

			ServerResponse response = jsonParser.retrieveServerData(Constants.REQUEST_TYPE_GET, url, null, null,
					DBAServiceApplication.getAppAccessToken(tContext));
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
						JSONArray notifArray = responseObj.getJSONArray("notifications");
						notifList = NotifItem.parseNotifList(notifArray);
						Log.e("???????", "notifications count = " + notifList.size());

						notifAdapter.setData(notifList);
						notifAdapter.notifyDataSetChanged();
						ivRefresh.clearAnimation();
					} else {
						alert("Invalid token.");
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

/*
 * 
 * notifList = new ArrayList<NotifItem>(); notifList.add(new
 * NotifItem(Constants.NOTIF_TYPE_UNASSIGNED, Constants.NOTIF_SEVERITY_ALERT,
 * "DB Backup Failure on SRV0122", "ClientA", "2014-07-07 4:13pm",
 * "Unassigned")); notifList.add(new NotifItem(Constants.NOTIF_TYPE_UNASSIGNED,
 * Constants.NOTIF_SEVERITY_WARNING, "DB Backup Failure on SRV0122", "ClientA",
 * "2014-07-07 4:13pm", "Unassigned")); notifList.add(new
 * NotifItem(Constants.NOTIF_TYPE_UNASSIGNED, Constants.NOTIF_SEVERITY_WARNING,
 * "DB Backup Failure on SRV0122", "ClientA", "2014-07-07 4:13pm",
 * "Unassigned")); notifList.add(new NotifItem(Constants.NOTIF_TYPE_UNASSIGNED,
 * Constants.NOTIF_SEVERITY_SIMPLE, "DB Backup Failure on SRV0122", "ClientA",
 * "2014-07-07 4:13pm", "Unassigned")); notifList.add(new
 * NotifItem(Constants.NOTIF_TYPE_UNASSIGNED, Constants.NOTIF_SEVERITY_RESOLVED,
 * "DB Backup Failure on SRV0122", "ClientA", "2014-07-07 4:13pm",
 * "Unassigned")); notifList.add(new NotifItem(Constants.NOTIF_TYPE_UNASSIGNED,
 * Constants.NOTIF_SEVERITY_ALERT, "DB Backup Failure on SRV0122", "ClientA",
 * "2014-07-07 4:13pm", "Unassigned")); notifList.add(new
 * NotifItem(Constants.NOTIF_TYPE_UNASSIGNED, Constants.NOTIF_SEVERITY_ALERT,
 * "DB Backup Failure on SRV0122", "ClientA", "2014-07-07 4:13pm",
 * "Unassigned")); notifList.add(new NotifItem(Constants.NOTIF_TYPE_UNASSIGNED,
 * Constants.NOTIF_SEVERITY_SIMPLE, "DB Backup Failure on SRV0122", "ClientA",
 * "2014-07-07 4:13pm", "Unassigned"));
 */
