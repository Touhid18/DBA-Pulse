/**
 * 
 */
package com.dbaservicesptyltd.dbaservices.adapter;

import java.util.ArrayList;

import com.dbaservicesptyltd.dbaservices.fragments.JobsInProgressFragment;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

/**
 * @author Touhid
 * 
 */
public class DynamicViewPagerAdapter extends FragmentPagerAdapter {

	private final String TAG = "DynamicViewPagerAdapter";
	@SuppressWarnings("unused")
	private Context tContext;
	private ArrayList<Fragment> fragList;

	public DynamicViewPagerAdapter(Context context, FragmentManager fm, ArrayList<Fragment> fragList) {
		super(fm);
		tContext = context;
		this.fragList = fragList;
	}

	@Override
	public Fragment getItem(int position) {
		return fragList.get(position);
	}

	public void addJobsPage(JobsInProgressFragment jobsFrag) {
		Log.d(TAG, "addJobsPage : frag. list size = " + fragList.size());
		if (fragList.size() > 2) {
			for (int i = 2; fragList.size() > 2; i++)
				fragList.remove(i);
		}
		Log.d(TAG, "addJobsPage : frag. list size = " + fragList.size());
		// Log.d(TAG, "Admin of jobsFrag : " +
		// jobsFrag.getAdmin().getAdminName());
		fragList.add(jobsFrag);
		Log.d(TAG, "addJobsPage : frag. list size = " + fragList.size() + " after adding jobsFrag with admin: "
				+ JobsInProgressFragment.adminObj.getAdminName());
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return fragList.size();
	}

	@Override
	public int getItemPosition(Object object) {
		int i = fragList.indexOf(object);
		if (i <= -1)
			return POSITION_NONE;
		else
			return i;
	}

	// public void removeJobsPage() {
	// fragList.remove(2);
	// notifyDataSetChanged();
	// }

	public void removeJobsPage(ViewPager pager) {
		Log.d("removeJobsPage", "deleting jobs fragment, fragList.size()=" + fragList.size());
		if (fragList.size() > 2) {
			for (int i = 2; fragList.size() > 2; i++)
				fragList.remove(i);
		}
		try {
			Object obj = this.instantiateItem(pager, 2);
			if (obj != null)
				destroyItem(pager, 2, obj);
		} catch (Exception e) {
			Log.i("DELLA", "no more Fragment in FragmentPagerAdapter");
		}
		notifyDataSetChanged();
	}

}
