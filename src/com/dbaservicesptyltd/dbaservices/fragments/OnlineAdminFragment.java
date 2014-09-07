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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;

import com.dbaservicesptyltd.dbaservices.R;
import com.dbaservicesptyltd.dbaservices.adapter.OnlineAdminAdapter;
import com.dbaservicesptyltd.dbaservices.interfaces.AdminClickListener;
import com.dbaservicesptyltd.dbaservices.model.OnlineAdminRow;
import com.dbaservicesptyltd.dbaservices.model.ServerResponse;
import com.dbaservicesptyltd.dbaservices.parser.JsonParser;
import com.dbaservicesptyltd.dbaservices.utils.Constants;
import com.dbaservicesptyltd.dbaservices.utils.DBAServiceApplication;

public class OnlineAdminFragment extends Fragment {

	private static Context tContext;
	private static final String TAG = "OnlineAdminFragment";
	private static ArrayList<OnlineAdminRow> adminList;
	private OnlineAdminAdapter onlineAdminAdapter;

	private AdminClickListener adminClickListener;

	private ImageView ivRefresh;
	private boolean isNewRefresh = true;

	private ProgressDialog pDialog;
	private JsonParser jsonParser;

	public static Fragment newInstance(Context context, AdminClickListener adminClickListener) {
		return new OnlineAdminFragment(context, adminClickListener);
	}

	public OnlineAdminFragment(Context context, AdminClickListener adminClickListener) {
		tContext = context;
		this.adminClickListener = adminClickListener;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "inside OncreateView()");
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.online_admin, container, false);

		pDialog = new ProgressDialog(tContext);
		jsonParser = new JsonParser();

		setRefreshAction(rootView);
		ListView lvNotifs = (ListView) rootView.findViewById(R.id.lv_admins);

		adminList = new ArrayList<OnlineAdminRow>();
		onlineAdminAdapter = new OnlineAdminAdapter(tContext, R.layout.online_admin_row, adminList);
		lvNotifs.setAdapter(onlineAdminAdapter);
		lvNotifs.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				OnlineAdminRow admin = (OnlineAdminRow) parent.getItemAtPosition(position);
				Log.d(TAG, "New admin jobs showing: " + admin.getAdminName());
				adminClickListener.handleClick(true, admin);
				// Intent intent = new Intent(tContext,
				// JobsInProgressActivity.class);
				// intent.putExtra(Constants.U_ID, admin.getUserId());
				// intent.putExtra(Constants.U_NAME, admin.getAdminName());
				// intent.putExtra(Constants.U_ACTIVE_COUNT, admin.getActive());
				// intent.putExtra(Constants.U_PENDING_COUNT,
				// admin.getPending());
				// intent.putExtra(Constants.U_RESOLVED_COUNT,
				// admin.getResolved());
				// intent.putExtra(Constants.U_IS_ONLINE, admin.isOnline());
				// intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				// startActivity(intent);
			}
		});

		return rootView;
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		try {
			isNewRefresh = true;
			if (isVisibleToUser)
				new GetOnlineAdmins().execute();
		} catch (Exception e) {
			Log.e(TAG,
					"Exception in setUserVisibleHint as isVisibleToUser=" + isVisibleToUser + " ::\n" + e.getMessage());
		}
	}

	// @Override
	// public void onResume() {
	// super.onResume();
	// new GetOnlineAdmins().execute();
	// }

	private void setRefreshAction(ViewGroup rootView) {
		try {
			ivRefresh = (ImageView) rootView.findViewById(R.id.iv_refresh_admin);
			final Animation rotation = AnimationUtils.loadAnimation(tContext, R.anim.rotate_refresh);
			rotation.setRepeatCount(Animation.INFINITE);
			ivRefresh.startAnimation(rotation);
			ivRefresh.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					ivRefresh.startAnimation(rotation);
					isNewRefresh = false;
					new GetOnlineAdmins().execute();
				}
			});
		} catch (Exception e) {
			Log.e(TAG, "Exception in setRefreshAction : " + e.getMessage());
		}
	}

	private class GetOnlineAdmins extends AsyncTask<Void, Void, JSONObject> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			try {
				if (pDialog != null && !pDialog.isShowing() && isNewRefresh) {
					pDialog.setMessage("Refreshing admin list ...");
					pDialog.setCancelable(false);
					pDialog.setIndeterminate(true);
					// pDialog.show();
					final Animation rotation = AnimationUtils.loadAnimation(tContext, R.anim.rotate_refresh);
					rotation.setRepeatCount(Animation.INFINITE);
					ivRefresh.startAnimation(rotation);
				}
			} catch (Exception e) {
				Log.e(TAG, "Exception in GetOnlineAdmins:onPreExecute :: " + e.getMessage());
			}
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			String url = Constants.URL_PARENT + "online_admin";

			try {
				ServerResponse response = jsonParser.retrieveServerData(Constants.REQUEST_TYPE_GET, url, null, null,
						DBAServiceApplication.getAppAccessToken(tContext));
				if (response.getStatus() == 200) {
					Log.d(">>>><<<<", "success in retrieving admin list.");
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
						JSONArray adminArray = responseObj.getJSONArray("active_users");
						adminList = OnlineAdminRow.parseAdminList(adminArray);
						Log.e("???????", "admin count = " + adminList.size());

						onlineAdminAdapter.setData(adminList);
						onlineAdminAdapter.notifyDataSetChanged();
						ivRefresh.clearAnimation();
					} else {
						alert("Invalid token!");
						ivRefresh.clearAnimation();
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
		try {
			AlertDialog.Builder bld = new AlertDialog.Builder(tContext);
			bld.setMessage(message);
			bld.setNeutralButton("OK", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			bld.create().show();
		} catch (Exception e) {
			Log.e(TAG, "Exception inside alert() with message : " + message + "\n" + e.getMessage());
		}
	}

}
