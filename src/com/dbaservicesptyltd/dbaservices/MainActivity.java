package com.dbaservicesptyltd.dbaservices;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;

import com.dbaservicesptyltd.dbaservices.adapter.ViewPagerAdapter;
import com.dbaservicesptyltd.dbaservices.interfaces.AdminClickListener;
import com.dbaservicesptyltd.dbaservices.model.OnlineAdminRow;

public class MainActivity extends FragmentActivity implements AdminClickListener,OnPageChangeListener {

	private ViewPager pager;
	private ViewPagerAdapter vpAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		pager = (ViewPager) findViewById(R.id.pager);
		vpAdapter = new ViewPagerAdapter(MainActivity.this, getSupportFragmentManager(), this);
		pager.setAdapter(vpAdapter);
		pager.setOnPageChangeListener(this);

	}

	@Override
	public void handleClick(boolean isAdd, OnlineAdminRow admin) {
		// if (!isAdd) {
		// vpAdapter.removeJobsPage(pager);
		// pager.setCurrentItem(1); // TODO remove fragment
		// // vpAdapter.destroyItem(pager, 2, null);
		// return;
		// }
		Log.d("MainActivity",
				"handleClick :: admin name = " + admin.getAdminName() + "\nCurrent page number: "
						+ pager.getCurrentItem());
		vpAdapter.setAdminObject(admin);
		vpAdapter.addJobPage(pager, admin);
		pager.setCurrentItem(2);

		Log.i("MainActivity", "" + pager.getCurrentItem());
	}

	@Override
	public void onBackPressed() {
		if (pager.getCurrentItem() == 2) {
			vpAdapter.removeJobsPage(pager);
			pager.setCurrentItem(1); // TODO remove fragment
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
		if(position==1 && vpAdapter.getCount()==3)
			vpAdapter.removeJobsPage(pager);
	}
}
