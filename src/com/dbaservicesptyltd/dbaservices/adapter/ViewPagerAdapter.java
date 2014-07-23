package com.dbaservicesptyltd.dbaservices.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import com.dbaservicesptyltd.dbaservices.fragments.JobsInProgressFragment;
import com.dbaservicesptyltd.dbaservices.fragments.OnlineAdminFragment;
import com.dbaservicesptyltd.dbaservices.fragments.SystemNotificationFragment;
import com.dbaservicesptyltd.dbaservices.interfaces.AdminClickListener;
import com.dbaservicesptyltd.dbaservices.model.OnlineAdminRow;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {// implements
																	// OnPageChangeListener
																	// {

	private static Context tContext;
	private static int NUM_PAGES = 2;
	private OnlineAdminRow admin;

	private AdminClickListener adminClickListener;

	public ViewPagerAdapter(Context context, FragmentManager fm, AdminClickListener adminClickListener) {
		super(fm);
		tContext = context;
		this.adminClickListener = adminClickListener;
	}

	@Override
	public int getCount() {
		return NUM_PAGES;
	}

	@Override
	public int getItemPosition(Object object) {
		Log.w("VPA_getItemPosition", "called");
		return POSITION_NONE;
	}

	@Override
	public Fragment getItem(int pageId) {
		if (pageId == 0)
			return SystemNotificationFragment.newInstance(tContext);
		else if (pageId == 1)
			return new OnlineAdminFragment(tContext, adminClickListener);
		else if (NUM_PAGES > 2 && pageId == 2)
			return new JobsInProgressFragment(tContext, admin, adminClickListener);// TODO
		else
			return null;
	}

	// @Override
	// public void destroyItem(ViewGroup container, int position, Object object)
	// {
	// // TODO Auto-generated method stub
	// // super.destroyItem(container, position, object);
	// container.removeViewAt(position);
	// }

	public void setPageCount(int nPage) {
		NUM_PAGES = nPage;
	}

	public void setAdminObject(OnlineAdminRow admin) {
		Log.d("**VPAdapter: setAdminObject**", "New admin being set: " + admin.getAdminName());
		this.admin = admin;
	}

	// @Override
	// public void onPageScrollStateChanged(int arg0) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void onPageScrolled(int arg0, float arg1, int arg2) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void onPageSelected(int position) {
	// }

}
