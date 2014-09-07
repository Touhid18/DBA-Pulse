package com.dbaservicesptyltd.dbaservices;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;

import com.dbaservicesptyltd.dbaservices.adapter.DynamicViewPagerAdapter;
import com.dbaservicesptyltd.dbaservices.fragments.JobsInProgressFragment;
import com.dbaservicesptyltd.dbaservices.fragments.OnlineAdminFragment;
import com.dbaservicesptyltd.dbaservices.fragments.SystemNotificationFragment;
import com.dbaservicesptyltd.dbaservices.interfaces.AdminClickListener;
import com.dbaservicesptyltd.dbaservices.model.OnlineAdminRow;

public class MainActivity extends FragmentActivity implements AdminClickListener, OnPageChangeListener {

	private ViewPager pager;
	// private ViewPagerAdapter vpAdapter;
	private DynamicViewPagerAdapter dvpAdapter;

	private static final String TAG = "MainActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		pager = (ViewPager) findViewById(R.id.pager);
		// vpAdapter = new ViewPagerAdapter(MainActivity.this,
		// getSupportFragmentManager());

		ArrayList<Fragment> fragList = new ArrayList<>();
		fragList.add(SystemNotificationFragment.newInstance(MainActivity.this));
		fragList.add(OnlineAdminFragment.newInstance(MainActivity.this, this));
		dvpAdapter = new DynamicViewPagerAdapter(MainActivity.this, getSupportFragmentManager(), fragList);
		pager.setAdapter(dvpAdapter);
		pager.setOnPageChangeListener(this);
		// pager.setOnPageChangeListener(this);

	}

	@Override
	public void handleClick(boolean isAdd, OnlineAdminRow admin) {
		Log.d(TAG, "handleClick: " + isAdd + ", admin name=" + admin.getAdminName());
		if (isAdd) {
			dvpAdapter.addJobsPage(JobsInProgressFragment.newInstance(MainActivity.this, admin, this));
			pager.setCurrentItem(2);
		} else {
			dvpAdapter.removeJobsPage(pager);
			pager.setCurrentItem(1); // TO-DO remove fragment
			// vpAdapter.destroyItem(pager, 2, null);
			return;
		}
	}

	// Log.d("MainActivity",
	// "handleClick :: admin name = " + admin.getAdminName() +
	// "\nCurrent page number: "
	// + pager.getCurrentItem());
	// // vpAdapter.setAdminObject(admin);
	// // vpAdapter.addJobPage(pager, admin);
	// vpAdapter.addJobsPage(new JobsInProgressFragment(MainActivity.this,
	// admin, this));
	// pager.setCurrentItem(2);
	//
	// Log.i("MainActivity", "" + pager.getCurrentItem());
	// }
	//
	@Override
	public void onBackPressed() {
		if (pager.getCurrentItem() == 2) {
			dvpAdapter.removeJobsPage(pager);
			pager.setCurrentItem(1); // TO-DO remove fragment
			// vpAdapter.destroyItem(pager, 2, null);
		} else
			super.onBackPressed();
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int position) {
		Log.d(TAG, "onPageSelected: position = " + position);
		if (position == 1 && dvpAdapter.getCount() == 3) {
			dvpAdapter.removeJobsPage(pager);
			pager.setCurrentItem(1);
		}
	}
}
