package com.dbaservicesptyltd.dbaservices;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.dbaservicesptyltd.dbaservices.adapter.ViewPagerAdapter;
import com.dbaservicesptyltd.dbaservices.interfaces.AdminClickListener;
import com.dbaservicesptyltd.dbaservices.model.OnlineAdminRow;

public class MainActivity extends FragmentActivity implements AdminClickListener {

	private ViewPager pager;
	private ViewPagerAdapter vpAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		pager = (ViewPager) findViewById(R.id.pager);
		vpAdapter = new ViewPagerAdapter(MainActivity.this, getSupportFragmentManager(), this);
		pager.setAdapter(vpAdapter);

	}

	@Override
	public void handleClick(boolean isAdd, OnlineAdminRow admin) {
		if (!isAdd) {
			vpAdapter.setPageCount(2); // TODO remove fragment
			// vpAdapter.destroyItem(pager, 2, null);
			return;
		}
		Log.d("MainActivity",
				"handleClick :: admin name = " + admin.getAdminName() + "\nCurrent page number: "
						+ pager.getCurrentItem());
		vpAdapter.setPageCount(3);
		vpAdapter.setAdminObject(admin);
		vpAdapter.notifyDataSetChanged();
		pager.setCurrentItem(3);

		Log.i("MainActivity", "" + pager.getCurrentItem());
	}

	@Override
	public void onBackPressed() {
		if (pager.getCurrentItem() == 2) {
			vpAdapter.setPageCount(2);
			pager.setCurrentItem(1); // TODO remove fragment
			// vpAdapter.destroyItem(pager, 2, null);
		} else
			super.onBackPressed();
	}
}
