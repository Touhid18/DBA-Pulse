package com.dbaservicesptyltd.dbaservices.fragments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dbaservicesptyltd.dbaservices.MainActivity;
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

	private Context tContext;
	private static final String TAG = "SystemNotificationFragment";
	private static ArrayList<NotifItem> notifList;
	private NotifAdapter notifAdapter;

	private int refreshCounter = 0;
	// private boolean noMoreData = false;

	private ImageView ivRefresh;
	private boolean isNewRefresh = true;
	private boolean isAsyncTaskRunning = false;
	private boolean isFromRefresh = false;
	private Dialog dialog;
	private static Vibrator vibrator;
	// Set the pattern for vibration
	private static long pattern[] = { 0, 600, 50, 800, 5 * 1000 };

	private ProgressDialog pDialog;
	private JsonParser jsonParser;

	private boolean isADialogOnScreen = false;

	public static SystemNotificationFragment newInstance() {
		return new SystemNotificationFragment();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		tContext = activity;
	}

	// public SystemNotificationFragment(Context context) {
	// tContext = context;
	// }

	// public SystemNotificationFragment() {
	// tContext = (Context) getActivity();
	// }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "inside OncreateView()");
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.sys_notif, container, false);

		pDialog = new ProgressDialog(tContext);
		jsonParser = new JsonParser();
		dialog = new Dialog(tContext, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
		if (vibrator != null) {
			vibrator.cancel();
			vibrator = null;
		}
		vibrator = (Vibrator) tContext.getSystemService(Context.VIBRATOR_SERVICE);
		setRefreshAction(rootView);
		setListView(rootView);

		// Set looper
		scheduledNotifIdSet = new HashSet<Integer>();
		scheduledThreads = new HashMap<>();
		schThPoolExecutor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(50);
		setTwoMinuteNotifRefresher();

		return rootView;
	}

	private void setListView(ViewGroup rootView) {
		ListView lvNotifs = (ListView) rootView.findViewById(R.id.lv_notifs);
		notifList = new ArrayList<NotifItem>();
		notifAdapter = new NotifAdapter(tContext, R.layout.notif_row, notifList);
		lvNotifs.setAdapter(notifAdapter);
		lvNotifs.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				NotifItem notifItem = (NotifItem) parent.getItemAtPosition(position);
				if (NotifAdapter.isNotifUnassigned(notifItem)) {
					isFromRefresh = false;
					showActionDialog(notifItem);
				} else
					Toast.makeText(tContext, "The job is already assigned!", Toast.LENGTH_SHORT).show();
			}
		});
		Button btnLvFooter = new Button(tContext);
		btnLvFooter.setText("Load more ...");
		btnLvFooter.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "Load more clicked");
				// if (noMoreData) {
				// // alert("No more data!");
				// Log.d(TAG, "No more data!");
				// } else {
				refreshCounter++;
				new GetNotifications().execute();
				// }
			}
		});
		lvNotifs.addFooterView(btnLvFooter);
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser && !isAsyncTaskRunning) {
			isNewRefresh = true;
			refreshCounter = 0;
			// noMoreData = false;
			new GetNotifications().execute();
		}
	}

	private void setTwoMinuteNotifRefresher() {
		try {
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
									if (!isAsyncTaskRunning) {
										// noMoreData = false;
										refreshCounter = 0;
										new GetNotifications().execute();
									}
								}
							});
						} catch (Exception e) {
							Log.d(":(", "exception inside 2 min refresher ::\n" + e.getCause() + "\n" + e.getMessage()
									+ "\n" + e.toString());
						}
					}
				}
			});
			refresherThread.start();
		} catch (Exception e) {
			Log.e(TAG, "Exception inside setTwoMinuteNotifRefresher:: " + e.getMessage());
		}
	}

	private void setRelooperForTheIgnoredNotif(final NotifItem notifItem) {
		try {
			final int notifId = notifItem.getId();
			final String notifDesc = notifItem.getDescription();
			if (scheduledNotifIdSet.contains((Integer) notifId) || scheduledThreads.containsKey(notifId))
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
			}, 7, 7, TimeUnit.MINUTES);
			scheduledThreads.put(notifId, t);

		} catch (Exception e) {
			Log.e(TAG, "Exception inside setRelooperForTheIgnoredNotif with notif.:: " + notifItem.getDescription()
					+ "\n" + e.getMessage());
		}
	}

	private boolean isIssueAssigned(int notifId) {
		for (NotifItem ni : notifList) {
			if (ni.getId() == notifId && ni.getStatus() != Constants.NOTIF_TYPE_UNASSIGNED)
				return true;
		}
		return false;
	}

	private void runTheDialog(final NotifItem notifItem) {
		try {
			Log.d("runTheDialog", "Run dialog for " + notifItem.getId() + ", " + notifItem.getDescription());
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Log.d("runOnUiThread", "The dialog for " + notifItem.getId() + ", " + notifItem.getDescription());
					// Toast.makeText(tContext, "Notif Id:" + notifItem.getId(),
					// Toast.LENGTH_SHORT).show();
					isFromRefresh = true;
					showActionDialog(notifItem);
				}
			});

		} catch (Exception e) {
			Log.e(TAG,
					"Exception inside runTheDialog with notif.:: " + notifItem.getDescription() + "\n" + e.getMessage());
		}
	}

	public static boolean isAppInForeground(Context context) {
		try {
			List<RunningTaskInfo> task = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
					.getRunningTasks(1);
			if (task.isEmpty()) {
				return false;
			}
			return task.get(0).topActivity.getPackageName().equalsIgnoreCase(context.getPackageName());
		} catch (Exception e) {
			Log.e(TAG, "Exception inside isAppInForeground ::\n" + e.getMessage());
			return false;
		}
	}

	private void notifyUser(NotifItem notifItem) {
		try {
			if (tContext == null) {
				DBAServiceApplication app = new DBAServiceApplication();
				tContext = app.getAppContext();
			}
			NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(tContext)
					.setSmallIcon(R.drawable.ic_launcher)
					.setContentTitle("DBA Services")
					.setContentText(
							notifItem.getDatetime() + " " + notifItem.getDescription() + ", "
									+ notifItem.getClientName() + ", " + "Unassigned");
			mBuilder.setAutoCancel(true);
			// mBuilder.getNotification().flags |=
			// Notification.FLAG_AUTO_CANCEL;
			Intent resultIntent = new Intent(tContext, MainActivity.class);
			resultIntent.putExtra("stop_vibrator", true);
			resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

			TaskStackBuilder stackBuilder = TaskStackBuilder.create(tContext);
			stackBuilder.addParentStack(MainActivity.class);
			stackBuilder.addNextIntent(resultIntent);
			PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
			mBuilder.setContentIntent(resultPendingIntent);
			NotificationManager mNotificationManager = (NotificationManager) tContext
					.getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.notify(101, mBuilder.build());
			vibratePhone();
		} catch (Exception e) {
			Log.e(TAG,
					"Exception inside notifyUser with notif.:: " + notifItem.getDescription() + "\n" + e.getMessage());
		}

	}

	private void showActionDialog(final NotifItem notifItem) {
		try {
			if (tContext == null)
				return;
			if (!isAppInForeground(tContext)) {
				notifyUser(notifItem);
				return;
			}
			try {
				if (dialog != null && dialog.isShowing()) {
					Log.i(TAG, "Cancelling previous dialog");
					dialog.cancel();
				}
				dialog.setContentView(R.layout.action_dialog);
				dialog.findViewById(R.id.btn_action).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.cancel();
						if (!isAsyncTaskRunning) {
							new DecideNotification().execute(Constants.NOTIF_TYPE_ACTIONED, notifItem.getId());
							// noMoreData = false;
							refreshCounter = 0;
							new GetNotifications().execute();
						}
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
						if (!isAsyncTaskRunning) {
							new DecideNotification().execute(Constants.NOTIF_TYPE_RESOLVED, notifItem.getId());
							// noMoreData = false;
							refreshCounter = 0;
							new GetNotifications().execute();
						}
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
				showVisibleDialog();
				if (isFromRefresh)
					vibratePhone();
			} catch (Exception e) {
				Log.e(TAG, "Exception showing dialog :( \n::\n " + e.toString());
				if (vibrator != null)
					vibrator.cancel();
			}
			dialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					if (vibrator != null)
						vibrator.cancel();
				}
			});
		} catch (Exception e) {
			Log.e(TAG,
					"Exception inside showActionDialog with notif.:: " + notifItem.getDescription() + "\n"
							+ e.getMessage());
		}

	}

	private void showVisibleDialog() {
		try {
			final ScheduledThreadPoolExecutor s = ((ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(2));
			s.scheduleAtFixedRate(new Runnable() {
				@Override
				public void run() {
					if (!isADialogOnScreen) {
						Log.i(TAG, "Showing the pending dialog");
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								dialog.show();
							}
						});
						s.remove(this);
						Log.i(TAG, "Shutting down the Sch.-thread-pool-Executor");
						s.shutdown();
					}
				}
			}, 100, 1 * 1000, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			Log.e(TAG, "Exception inside showVisibleDialog ::\n" + e.getMessage());
		}
	}

	private void setRefreshAction(ViewGroup rootView) {
		try {
			ivRefresh = (ImageView) rootView.findViewById(R.id.iv_refresh);
			final Animation rotation = AnimationUtils.loadAnimation(tContext, R.anim.rotate_refresh);
			rotation.setRepeatCount(Animation.INFINITE);
			ivRefresh.startAnimation(rotation);
			ivRefresh.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					ivRefresh.startAnimation(rotation);
					if (!isAsyncTaskRunning) {
						isNewRefresh = false;
						// noMoreData = false;
						refreshCounter = 0;
						new GetNotifications().execute();
					}
				}
			});
		} catch (Exception e) {
			Log.e(TAG, "Exception inside setRefreshAction :: \n" + e.getMessage());
		}
	}

	private void checkAndShowUnassignedalert() {
		try {
			int st = refreshCounter * 20;
			// If notifList is blank, or refreshCounter is over-increased, then
			// ignore
			if (notifList.size() <= 0 || st >= notifList.size())
				return;
			// Set the start point to 21,41,61 ... etc., while leaving for st=0
			if (refreshCounter > 0)
				++st;
			List<NotifItem> niList = notifList.subList(st, notifList.size());
			for (NotifItem ni : niList) {
				if (NotifAdapter.isNotifUnassigned(ni)
				// ni.getStatus() == Constants.NOTIF_TYPE_UNASSIGNED
						&& ni.getSeverity() == Constants.NOTIF_SEVERITY_ALERT) {
					isFromRefresh = true;
					showActionDialog(ni);
					return;
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "Exception inside checkAndShowUnassignedalert :: \n" + e.getMessage());
		}
	}

	private class GetNotifications extends AsyncTask<Void, Void, JSONObject> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			isAsyncTaskRunning = true;
			try {
				if (vibrator != null)
					vibrator.cancel();
				if (pDialog != null && !pDialog.isShowing() && isNewRefresh) {
					pDialog.setMessage("Refreshing noifictions...");
					pDialog.setCancelable(false);
					pDialog.setIndeterminate(true);
					// pDialog.show();
					final Animation rotation = AnimationUtils.loadAnimation(tContext, R.anim.rotate_refresh);
					rotation.setRepeatCount(Animation.INFINITE);
					ivRefresh.startAnimation(rotation);
				}
			} catch (Exception e) {
			}
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			// String url = Constants.URL_PARENT + "notifications";
			String url = Constants.URL_PARENT + "notifications/" + refreshCounter;

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
			isAsyncTaskRunning = false;
			if (responseObj != null) {
				try {
					String status = responseObj.getString("status");
					if (status.equals("OK")) {
						JSONArray notifArray = responseObj.getJSONArray("notifications");
						ArrayList<NotifItem> notifList2 = NotifItem.parseNotifList(notifArray);
						Log.e("???????", "notifications count = " + notifList2.size());
						// Decide on adding new data to the list
						if (notifList2.size() == 0) {
							ivRefresh.clearAnimation();
							// noMoreData = true;
							// TO_DO
							// alert("No more data!");
							Log.d(TAG, "No more data!");
							if (pDialog.isShowing())
								pDialog.cancel();
							// isAsyncTaskRunning = false;
							return;
						} else if (refreshCounter == 0) {
							notifList = notifList2;
							notifAdapter.setData(notifList);
						} else if (!doesNotifListContain(notifList2.get(notifList2.size() - 1))) {
							notifList.addAll(notifList2);
							notifAdapter.addData(notifList2);
						}
						if (notifList2.size() < 20)
							// noMoreData = true;
							// Notif. list decided, now reload if new found
							sortNotifList();
						notifAdapter.notifyDataSetChanged();
						ivRefresh.clearAnimation();
						checkAndShowUnassignedalert();
					} else {
						// alert("Invalid token or malformed data received!");
						ivRefresh.clearAnimation();
					}
				} catch (JSONException e) {
					// alert("Malformed data received!");
					e.printStackTrace();
				}
			} else {
				if (pDialog.isShowing())
					pDialog.dismiss();
				// noMoreData = false;
				refreshCounter = 0;
				new GetNotifications().execute();
				ivRefresh.clearAnimation();
				// isAsyncTaskRunning = false;
				return;
			}

			if (pDialog.isShowing())
				pDialog.dismiss();
			ivRefresh.clearAnimation();
			// isAsyncTaskRunning = false;
		}
	}

	private void sortNotifList() {
		try {
			Collections.sort(notifList, new Comparator<NotifItem>() {
				@Override
				public int compare(NotifItem lhs, NotifItem rhs) {
					int id1 = lhs.getId();
					int id2 = rhs.getId();
					return id1 < id2 ? 1 : id1 == id2 ? 0 : -1;
				}
			});
		} catch (Exception e) {
			Log.e(TAG, "Exception inside sortNotifList :: " + e.getMessage());
		}
	}

	public boolean doesNotifListContain(NotifItem notifItem) {
		try {
			for (NotifItem ni : notifList) {
				if (ni.getId() == notifItem.getId())
					return true;
			}
			return false;
		} catch (Exception e) {
			Log.e(TAG,
					"Exception inside doesNotifListContain with notif: " + notifItem.getDescription() + "\n"
							+ e.getMessage());
			return false;
		}
	}

	private class DecideNotification extends AsyncTask<Integer, Void, JSONObject> {

		private int actionCode;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			isAsyncTaskRunning = true;
			try {
				if (vibrator != null)
					vibrator.cancel();
				if (!pDialog.isShowing()) {
					pDialog.setMessage("Deciding the issue ...");
					pDialog.setCancelable(false);
					pDialog.setIndeterminate(true);
					// pDialog.show();
					final Animation rotation = AnimationUtils.loadAnimation(tContext, R.anim.rotate_refresh);
					rotation.setRepeatCount(Animation.INFINITE);
					ivRefresh.startAnimation(rotation);
				}
			} catch (Exception e) {
				Log.e(TAG, "Exception inside DecideNotification:onPreExecute :: \n" + e.getMessage());
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
			isAsyncTaskRunning = false;
			if (responseObj != null) {
				try {
					String status = responseObj.getString("status");
					if (status.equals("OK")) {
						ivRefresh.clearAnimation();
						// if (actionCode == Constants.NOTIF_TYPE_ACTIONED)
						// alert("Issue marked as actioned.");
						// else if (actionCode == Constants.NOTIF_TYPE_RESOLVED)
						// alert("Issue marked as resolved.");
						// else if (actionCode == Constants.NOTIF_TYPE_IGNORED)
						// alert("Issue marked as ignored. This will again be prompted within 7 minutes");
					} else {
						ivRefresh.clearAnimation();
						// alert("Invalid token.");
					}
				} catch (JSONException e) {
					// alert("Malformed data received!");
					e.printStackTrace();
				}
			}

			ivRefresh.clearAnimation();
			if (pDialog.isShowing())
				pDialog.dismiss();
			// isAsyncTaskRunning = false;
		}

	}

	// void alert(String message) {
	// try {
	// AlertDialog.Builder bld = new AlertDialog.Builder(tContext);
	// bld.setMessage(message);
	// bld.setNeutralButton("OK", new DialogInterface.OnClickListener() {
	// @Override
	// public void onClick(DialogInterface dialog, int which) {
	// isADialogOnScreen = false;
	// dialog.dismiss();
	// }
	// });
	// bld.create().show();
	// isADialogOnScreen = true;
	// } catch (Exception e) {
	// Log.e(TAG, "Exception inside alert with message: " + message + "\n" +
	// e.getMessage());
	// }
	// }

	private static void vibratePhone() {
		try {
			// Start the vibration
			// start vibration with repeated count, use -1 if you don't want to
			// repeat the vibration
			if (vibrator != null) {
				vibrator.cancel();
				vibrator.vibrate(pattern, 0);
			}
			// else if (tContext != null) {
			// vibrator = (Vibrator)
			// tContext.getSystemService(Context.VIBRATOR_SERVICE);
			// long pattern2[] = { 0, 600, 50, 800, 5 * 1000 };
			// vibrator.vibrate(pattern2, 0);
			// }
			// ((Vibrator)
			// tContext.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(512);
		} catch (Exception e) {
			Log.e(TAG, "Exception inside vibratePhone :: " + e.getMessage());
		}
	}

	@Override
	public void onPause() {
		refreshCounter = 0;
		// noMoreData = false;
		super.onPause();
	}

	@Override
	public void onResume() {
		try {
			refreshCounter = 0;
			// noMoreData = false;
			Log.i(TAG, "onResume : cancelling vibrator ...");
			vibrator.cancel();
		} catch (Exception e) {
			Log.e(TAG, "Exception inside onResume : " + e.getMessage());
		}
		super.onResume();
	}

	@Override
	public void onStop() {
		Log.i(TAG, "onStop : called");
		super.onStop();
	}

	@Override
	public void onDestroyView() {
		Log.i(TAG, "onDestroyView : called");
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "onDestroy : called");
		super.onDestroy();
	}

	@Override
	public void onDetach() {
		Log.i(TAG, "onDetach : called");

		// if (refresherThread.isAlive())
		// refresherThread.interrupt();
		//
		// scheduledNotifIdSet.clear();
		// scheduledNotifIdSet = null;
		// schThPoolExecutor.shutdown();
		// vibrator = null;
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
