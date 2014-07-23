package com.dbaservicesptyltd.dbaservices.fragments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
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
import android.widget.Toast;

import com.dbaservicesptyltd.dbaservices.R;
import com.dbaservicesptyltd.dbaservices.adapter.NotifAdapter;
import com.dbaservicesptyltd.dbaservices.model.NotifItem;
import com.dbaservicesptyltd.dbaservices.model.ServerResponse;
import com.dbaservicesptyltd.dbaservices.parser.JsonParser;
import com.dbaservicesptyltd.dbaservices.utils.Constants;
import com.dbaservicesptyltd.dbaservices.utils.DBAServiceApplication;

public class SystemNotificationFragment extends Fragment {

	private Handler refresherHandler;
	private Thread refresherThread;

	private ScheduledThreadPoolExecutor schThPoolExecutor;
	private Set<Integer> scheduledNotifIdSet;
	private HashMap<Integer, ScheduledFuture<?>> scheduledThreads;

	private static Context tContext;
	private static final String TAG = "SystemNotificationFragment";
	private static ArrayList<NotifItem> notifList;
	private NotifAdapter notifAdapter;

	private ImageView ivRefresh;
	private boolean isNewRefresh = true;
	private Dialog dialog;

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
		lvNotifs.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				NotifItem notifItem = (NotifItem) parent.getItemAtPosition(position);
				if (notifItem.getStatus() == Constants.NOTIF_TYPE_UNASSIGNED)
					showActionDialog(notifItem);
				else
					Toast.makeText(tContext, "The job is already assigned!", Toast.LENGTH_SHORT).show();
			}
		});

		// Set looper
		scheduledNotifIdSet = new HashSet<Integer>();
		scheduledThreads = new HashMap<>();
		schThPoolExecutor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(50);
		setTwoMinuteNotifRefresher();

		return rootView;
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		isNewRefresh = true;
		if (isVisibleToUser)
			new GetNotifications().execute();
	}

	private void setTwoMinuteNotifRefresher() {
		refresherHandler = new Handler();
		refresherThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					if (Thread.interrupted()) {
						Log.i("-_-", "2min refresher thread interrupted, so exiting the loop.");
						break;
					}
					try {
						Thread.sleep(2 * 60 * 1000);
						if (Thread.interrupted()) {
							Log.i("-_-", "2min refresher thread interrupted, so exiting the loop.");
							break;
						}
						refresherHandler.post(new Runnable() {
							@Override
							public void run() {
								Log.d(":D", "2min refresher being run...");
								if (dialog.isShowing())
									dialog.cancel();
								new GetNotifications().execute();
							}
						});
					} catch (Exception e) {
						Log.d(":(", "exception inside 2 min refresher ::\n" + e.getCause() + "\n" + e.getMessage());
					}
				}
			}
		});
		refresherThread.start();
	}

	private void setRelooperForTheIgnoredNotif(final NotifItem notifItem) {
		final int notifId = notifItem.getId();
		final String notifDesc = notifItem.getDescription();
		if (scheduledNotifIdSet.contains((Integer) notifId))
			return;
		if (scheduledThreads.containsKey(notifId))
			return;
		Log.d("setRelooperForTheIgnoredNotif", "Scheduling for " + notifId + ", " + notifDesc);
		scheduledNotifIdSet.add((Integer) notifId);
		ScheduledFuture<?> t = schThPoolExecutor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				Log.d("setRelooperForTheIgnoredNotif", "Dialog for " + notifId + ", " + notifDesc);
				if (isIssueAssigned(notifId)) {
					scheduledThreads.get(notifId).cancel(false);
					return;
				}
				runTheDialog(notifItem);
			}
		}, 7, 7, TimeUnit.MINUTES); // TODO Minute
		scheduledThreads.put(notifId, t);
	}

	private boolean isIssueAssigned(int notifId) {
		for (NotifItem ni : notifList) {
			if (ni.getId() == notifId && ni.getStatus() != Constants.NOTIF_TYPE_UNASSIGNED)
				return true;
		}
		return false;
	}

	private void runTheDialog(final NotifItem notifItem) {
		Log.d("runTheDialog", "Run dialog for " + notifItem.getId() + ", " + notifItem.getDescription());
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Log.d("runOnUiThread", "The dialog for " + notifItem.getId() + ", " + notifItem.getDescription());
				// Toast.makeText(tContext, "Notif Id:" + notifItem.getId(),
				// Toast.LENGTH_SHORT).show();
				showActionDialog(notifItem);
			}
		});
	}

	private void showActionDialog(final NotifItem notifItem) {
		try {
			if (dialog != null && dialog.isShowing()) {
				Log.i(TAG, "Cancelling previous dialog");
				dialog.cancel();
			}
			dialog = new Dialog(tContext, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
			dialog.setContentView(R.layout.action_dialog);
			dialog.findViewById(R.id.btn_action).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.cancel();
					new DecideNotification().execute(Constants.NOTIF_TYPE_ACTIONED, notifItem.getId());
				}
			});
			dialog.findViewById(R.id.btn_ignore).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.cancel();
					setRelooperForTheIgnoredNotif(notifItem);
				}
			});
			dialog.findViewById(R.id.btn_resolved).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.cancel();
					new DecideNotification().execute(Constants.NOTIF_TYPE_RESOLVED, notifItem.getId());
					new GetNotifications().execute();
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
			tvBody.setText(notifItem.getDatetime() + " " + notifItem.getDescription() + ", "
					+ notifItem.getClientName() + ", " + notifItem.getUpdated());
			// Center-focus the dialog
			Window window = dialog.getWindow();
			window.setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			window.setGravity(Gravity.CENTER);
			dialog.show();
			vibratePhone();
		} catch (Exception e) {
			Log.e(TAG, "Exception showing dialog :( \n::\n " + e.toString());
		}
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
				isNewRefresh = false;
				new GetNotifications().execute();
			}
		});
	}

	private void checkAndShowUnassignedalert() {
		for (NotifItem ni : notifList) {
			if (ni.getStatus() == Constants.NOTIF_TYPE_UNASSIGNED && ni.getSeverity() == Constants.NOTIF_SEVERITY_ALERT) {
				showActionDialog(ni);
				return;
			}
		}
	}

	private class GetNotifications extends AsyncTask<Void, Void, JSONObject> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			try {
				if (!pDialog.isShowing() && isNewRefresh) {
					pDialog.setMessage("Refreshing noifictions...");
					pDialog.setCancelable(false);
					pDialog.setIndeterminate(true);
					pDialog.show();
				}
			} catch (Exception e) {
			}
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			String url = Constants.URL_PARENT + "notifications";

			try {
				ServerResponse response = jsonParser.retrieveServerData(Constants.REQUEST_TYPE_GET, url, null, null,
						DBAServiceApplication.getAppAccessToken(tContext));
				if (response.getStatus() == 200) {
					Log.d(">>>><<<<", "success in retrieving notifications.");
					JSONObject responseObj = response.getjObj();
					return responseObj;
				} else
					return null;
			} catch (Exception e) {
				Log.e("JSONParser", "Exception in retrieveServerData" + e.toString());
			}
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
						sortNotifList();

						notifAdapter.setData(notifList);
						notifAdapter.notifyDataSetChanged();
						ivRefresh.clearAnimation();
						checkAndShowUnassignedalert();
					} else {
						alert("Invalid token or malformed data received!");
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

	private void sortNotifList() {
		Collections.sort(notifList, new Comparator<NotifItem>() {
			@Override
			public int compare(NotifItem lhs, NotifItem rhs) {
				int id1 = lhs.getId();
				int id2 = rhs.getId();
				return id1 < id2 ? 1 : id1 == id2 ? 0 : -1;
			}
		});
	}

	private class DecideNotification extends AsyncTask<Integer, Void, JSONObject> {

		private int actionCode;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (!pDialog.isShowing()) {
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

	private void vibratePhone() {
		// Set the pattern for vibration
		// long pattern[] = { 0, 200, 100, 300, 400 };
		// // Start the vibration
		// Vibrator vibrator = (Vibrator)
		// tContext.getSystemService(Context.VIBRATOR_SERVICE);
		// // start vibration with repeated count, use -1 if you don't want to
		// // repeat the vibration
		// vibrator.vibrate(pattern, 0);
		((Vibrator) tContext.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(800);
	}

	@Override
	public void onStop() {
		Log.i("onStop", "called");
		super.onStop();
	}

	@Override
	public void onDestroyView() {
		Log.i("onDestroyView", "called");

		if (refresherThread.isAlive())
			refresherThread.interrupt();

		scheduledNotifIdSet.clear();
		scheduledNotifIdSet = null;
		schThPoolExecutor.shutdown();
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		Log.i("onDestroy", "called");
		super.onDestroy();
	}

	@Override
	public void onDetach() {
		Log.i("onDetach", "called");
		super.onDetach();
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
