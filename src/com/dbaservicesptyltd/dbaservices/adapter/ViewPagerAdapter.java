package com.dbaservicesptyltd.dbaservices.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.dbaservicesptyltd.dbaservices.fragments.OnlineAdminFragment;
import com.dbaservicesptyltd.dbaservices.fragments.SystemNotificationFragment;

public class ViewPagerAdapter extends FragmentPagerAdapter {

	private static Context tContext;
	private static final int NUM_PAGES = 2;

	public ViewPagerAdapter(Context context, FragmentManager fm) {
		super(fm);
		tContext = context;
	}

	@Override
	public int getCount() {
		return NUM_PAGES;
	}

	@Override
	public Fragment getItem(int pageId) {
		if(pageId==0)
			return SystemNotificationFragment.newInstance(tContext);
		else 
			return OnlineAdminFragment.newInstance(tContext);
	}

}
