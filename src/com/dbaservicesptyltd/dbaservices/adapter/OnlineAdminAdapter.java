/**
 * 
 */
package com.dbaservicesptyltd.dbaservices.adapter;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.dbaservicesptyltd.dbaservices.R;
import com.dbaservicesptyltd.dbaservices.model.OnlineAdminRow;

/**
 * @author Touhid
 * 
 */
public class OnlineAdminAdapter extends ArrayAdapter<OnlineAdminRow> {

	@SuppressWarnings("unused")
	private static final String TAG = OnlineAdminAdapter.class.getSimpleName();
	private static Context tContext;

	public OnlineAdminAdapter(Context context, int resource,
			ArrayList<OnlineAdminRow> adminList) {
		super(context, resource, adminList);
		tContext = context;
	}

	private class ViewHolder {
		TextView tvAdminName, tvActiveCount, tvPendingCount, tvResolvedCount;
		ImageView ivStatus;
		Button btnMessage;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// Log.d(tag, "getView() : position=" + position);
		ViewHolder holder = null;

		LayoutInflater inflater = (LayoutInflater) tContext
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.online_admin_row, null);
			holder = new ViewHolder();
			holder.tvAdminName = (TextView) convertView
					.findViewById(R.id.tv_admin_name);
			holder.tvActiveCount = (TextView) convertView
					.findViewById(R.id.tv_active_count);
			holder.tvPendingCount = (TextView) convertView
					.findViewById(R.id.tv_pending_count);
			holder.tvResolvedCount = (TextView) convertView
					.findViewById(R.id.tv_resolved_count);
			holder.ivStatus = (ImageView) convertView
					.findViewById(R.id.iv_status);
			holder.btnMessage = (Button) convertView
					.findViewById(R.id.btn_msg_admin);
			convertView.setTag(holder);
		} else
			holder = (ViewHolder) convertView.getTag();

		OnlineAdminRow adminItem = getItem(position);
		if (adminItem.isOnline()) {
			holder.ivStatus.setImageBitmap(BitmapFactory.decodeResource(
					tContext.getResources(), R.drawable.status_online));
			// Log.d(TAG, "admin online: " + adminItem.getAdminName());
		} else
			holder.ivStatus.setImageBitmap(BitmapFactory.decodeResource(
					tContext.getResources(), R.drawable.status_offline));
		holder.tvAdminName.setText(adminItem.getAdminName());
		holder.tvActiveCount.setText(adminItem.getActive() + "");
		holder.tvPendingCount.setText(adminItem.getPending() + "");
		holder.tvResolvedCount.setText(adminItem.getResolved() + "");
		holder.btnMessage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO input message & send it
				Intent sendIntent = new Intent(Intent.ACTION_VIEW);
				sendIntent.setData(Uri.parse("sms:"));
				tContext.startActivity(sendIntent);
			}
		});

		return convertView;
	}

	public void setData(List<OnlineAdminRow> notifList) {
		clear();
		if (notifList != null) {
			for (int i = 0; i < notifList.size(); i++) {
				add(notifList.get(i));
			}
		}
	}
}
