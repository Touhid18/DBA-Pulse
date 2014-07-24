package com.dbaservicesptyltd.dbaservices.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.dbaservicesptyltd.dbaservices.fragments.OnlineAdminFragment;
import com.dbaservicesptyltd.dbaservices.fragments.SystemNotificationFragment;

public class ViewPagerAdapter extends FragmentPagerAdapter {
	// implements OnPageChangeListener {

	private static Context tContext;
	private static int NUM_PAGES = 2;

	// private OnlineAdminRow admin;

	// private AdminClickListener adminClickListener;

	public ViewPagerAdapter(Context context, FragmentManager fm) {
		// , AdminClickListener adminClickListener) {
		super(fm);
		tContext = context;
		// this.adminClickListener = adminClickListener;
	}

	@Override
	public int getCount() {
		return NUM_PAGES;
	}

	@Override
	public Fragment getItem(int pageId) {
		if (pageId == 0)
			return new SystemNotificationFragment(tContext);
		else
			// if (pageId == 1)
			return new OnlineAdminFragment(tContext);// , adminClickListener);
		// else if (NUM_PAGES > 2 && pageId == 2)
		// return new JobsInProgressFragment(tContext, admin,
		// adminClickListener);// TODO
		// else
		// return null;
	}

	// @Override
	// public void destroyItem(ViewGroup container, int position, Object object)
	// {
	// super.destroyItem(container, position, object);
	// if (position <= getCount()) {
	// FragmentManager manager = ((Fragment) object).getFragmentManager();
	// FragmentTransaction trans = manager.beginTransaction();
	// trans.remove((Fragment) object);
	// trans.commit();
	// }
	// }

	// @Override
	// public boolean isViewFromObject(View view, Object object) {
	// return view == object;
	// }

	// public void removeJobsPage(ViewPager pager) {
	// Log.d("removeJobsPage", "deleting jobs fragment");
	// NUM_PAGES = 2;
	// try {
	// Object objectobject = this.instantiateItem(pager, 2);
	// if (objectobject != null)
	// destroyItem(pager, 2, objectobject);
	// } catch (Exception e) {
	// Log.i("DELLA", "no more Fragment in FragmentPagerAdapter");
	// }
	// notifyDataSetChanged();
	// }

	// @Override
	// public Object instantiateItem(ViewGroup container, int position) {
	// if (position == 3)
	// return new JobsInProgressFragment(tContext, admin);//,
	// adminClickListener);
	// return super.instantiateItem(container, position);
	// }
	//
	// public void addJobPage(ViewPager pager, OnlineAdminRow admin) {
	// NUM_PAGES = 3;
	// // Fragment jobsFrag = (Fragment) instantiateItem(pager, 2) ;
	// Fragment jobsFrag = new JobsInProgressFragment(tContext, admin);//,
	// adminClickListener);
	// // String fTag = jobsFrag.getTag();
	// // if (fTag.equals(null) || fTag.equals(""))
	// String fTag = "jobs_in_progress_admin" + admin.getUserId();
	// FragmentManager manager = jobsFrag.getFragmentManager();
	// if (manager != null) {
	// Log.d("......", "manager got");
	// FragmentTransaction trans = manager.beginTransaction();
	// trans.add(jobsFrag, fTag).commit();
	// pager.addView(jobsFrag.getView());
	// } else {
	// Log.d("......", "manager null");
	// FragmentTransaction trans = ((FragmentActivity)
	// tContext).getSupportFragmentManager().beginTransaction();
	// trans.add(jobsFrag, fTag).commit();
	// pager.addView(jobsFrag.onCreateView(
	// (LayoutInflater)
	// tContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE), pager,
	// null));
	// }
	//
	// notifyDataSetChanged();
	// }
	//
	// public void setAdminObject(OnlineAdminRow admin) {
	// Log.d("**VPAdapter: setAdminObject**", "New admin being set: " +
	// admin.getAdminName());
	// this.admin = admin;
	// }
}
