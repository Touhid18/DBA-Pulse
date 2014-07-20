/**
 * 
 */
package com.dbaservicesptyltd.dbaservices.adapter;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.dbaservicesptyltd.dbaservices.R;
import com.dbaservicesptyltd.dbaservices.model.NotifItem;
import com.dbaservicesptyltd.dbaservices.utils.Constants;

/**
 * @author Touhid
 * 
 */
public class NotifAdapter extends ArrayAdapter<NotifItem> {

	@SuppressWarnings("unused")
	private static final String TAG = NotifAdapter.class.getSimpleName();
	private static Context tContext;

	public NotifAdapter(Context context, int resource, ArrayList<NotifItem> notifList) {
		super(context, resource, notifList);
		tContext = context;
	}

	private class ViewHolder {
		TextView tvNotif;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// Log.d(tag, "getView() : position=" + position);
		ViewHolder holder = null;

		LayoutInflater inflater = (LayoutInflater) tContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.notif_row, null);
			holder = new ViewHolder();
			holder.tvNotif = (TextView) convertView.findViewById(R.id.tv_notif);
			convertView.setTag(holder);
		} else
			holder = (ViewHolder) convertView.getTag();

		NotifItem item = getItem(position);
		if (item.getStatus() == Constants.NOTIF_TYPE_UNASSIGNED)
			holder.tvNotif.setText(item.getDatetime() + " " + item.getDescription() + ", " + item.getClientName()
					+ ", " + "Unassigned");
		else
			holder.tvNotif.setText(item.getDatetime() + " " + item.getDescription() + ", " + item.getClientName()
					+ ", [" + item.getUserName() + ", " + item.getUpdated() + "].");
		int severity = item.getSeverity();
		if (severity == Constants.NOTIF_SEVERITY_ALERT) {
			holder.tvNotif.setTextColor(tContext.getResources().getColor(R.color.red));
		} else if (severity == Constants.NOTIF_SEVERITY_WARNING) {
			holder.tvNotif.setTextColor(tContext.getResources().getColor(R.color.yellow));
		} else if (severity == Constants.NOTIF_SEVERITY_SIMPLE) {
			holder.tvNotif.setTextColor(tContext.getResources().getColor(R.color.green_brighter));
		} else {
			holder.tvNotif.setTextColor(tContext.getResources().getColor(R.color.Violet));
		}
		if (item.getStatus() == Constants.NOTIF_TYPE_RESOLVED) {
			holder.tvNotif.setTextColor(tContext.getResources().getColor(R.color.gray));
		}

		return convertView;
	}

	public void setData(List<NotifItem> notifList) {
		clear();
		if (notifList != null) {
			for (int i = 0; i < notifList.size(); i++) {
				add(notifList.get(i));
			}
		}
	}
}
