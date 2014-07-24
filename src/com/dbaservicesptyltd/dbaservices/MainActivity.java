package com.dbaservicesptyltd.dbaservices;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import com.dbaservicesptyltd.dbaservices.adapter.ViewPagerAdapter;

public class MainActivity extends FragmentActivity {
	// implements AdminClickListener, OnPageChangeListener {

	private ViewPager pager;
	private ViewPagerAdapter vpAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		pager = (ViewPager) findViewById(R.id.pager);
		vpAdapter = new ViewPagerAdapter(MainActivity.this, getSupportFragmentManager());
		// ArrayList<Fragment> fragList = new ArrayList<>();
		// fragList.add(new SystemNotificationFragment(MainActivity.this));
		// fragList.add(new OnlineAdminFragment(MainActivity.this));// , this));
		// vpAdapter = new DynamicViewPagerAdapter(MainActivity.this,
		// getSupportFragmentManager(), fragList);
		pager.setAdapter(vpAdapter);
		// pager.setOnPageChangeListener(this);

	}

	// @Override
	// public void handleClick(boolean isAdd, OnlineAdminRow admin) {
	// // if (!isAdd) {
	// // vpAdapter.removeJobsPage(pager);
	// // pager.setCurrentItem(1); // TO-DO remove fragment
	// // // vpAdapter.destroyItem(pager, 2, null);
	// // return;
	// // }
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
	// @Override
	// public void onBackPressed() {
	// if (pager.getCurrentItem() == 2) {
	// vpAdapter.removeJobsPage(pager);
	// pager.setCurrentItem(1); // TO-DO remove fragment
	// // vpAdapter.destroyItem(pager, 2, null);
	// } else
	// super.onBackPressed();
	// }
	//
	// @Override
	// public void onPageScrollStateChanged(int arg0) {
	// }
	//
	// @Override
	// public void onPageScrolled(int arg0, float arg1, int arg2) {
	// }
	//
	// @Override
	// public void onPageSelected(int position) {
	// if (position == 1 && vpAdapter.getCount() == 3)
	// ;
	// // vpAdapter.removeJobsPage(pager);
	// }
}
