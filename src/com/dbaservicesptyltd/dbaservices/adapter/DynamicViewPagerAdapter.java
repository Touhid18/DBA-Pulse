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

	@SuppressWarnings("unused")
	private static Context tContext;
	private static ArrayList<Fragment> fragList;

	public DynamicViewPagerAdapter(Context context, FragmentManager fm, ArrayList<Fragment> fragList) {
		super(fm);
		tContext = context;
		DynamicViewPagerAdapter.fragList = fragList;
	}

	@Override
	public Fragment getItem(int position) {
		return fragList.get(position);
	}
	
	public void addJobsPage(JobsInProgressFragment jobsFrag){
		fragList.add(jobsFrag);
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return fragList.size();
	}

//	public void removeJobsPage() {
//		fragList.remove(2);
//		notifyDataSetChanged();
//	}

	public void removeJobsPage(ViewPager pager) {
		Log.d("removeJobsPage","deleting jobs fragment");
		fragList.remove(2);
		try {
			Object objectobject = this.instantiateItem(pager, 2);
			if (objectobject != null)
				destroyItem(pager, 2, objectobject);
		} catch (Exception e) {
			Log.i("DELLA", "no more Fragment in FragmentPagerAdapter");
		}
		notifyDataSetChanged();
	}

}
