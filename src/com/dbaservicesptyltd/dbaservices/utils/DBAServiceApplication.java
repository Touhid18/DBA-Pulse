package com.dbaservicesptyltd.dbaservices.utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

import com.bugsense.trace.BugSenseHandler;
import com.dbaservicesptyltd.dbaservices.model.UserCred;

/**
 * Singleton class to return the application, which handles the preference
 * values
 */
public class DBAServiceApplication extends Application{

	private static Context mContext;
	protected static SharedPreferences prefs;

	public DBAServiceApplication(Context context) {
		mContext = context;
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		BugSenseHandler.initAndStartSession(mContext, "5cd599a4");
	}
	public DBAServiceApplication() {
		DBAServiceApplication.mContext = getApplicationContext();
		prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
	}

	// /** Returns a singleton instance of the application */
	// public static DBAServiceApplication getAppInstance() {
	// if (appInstance == null)
	// appInstance = new DBAServiceApplication();
	// return appInstance;
	// }

	public Context getAppContext() {
		if(mContext==null)
			mContext= getApplicationContext();
		return mContext;
	}

	// public void setFirstTime(Boolean firstTimeFlag) {
	// Editor editor = User.edit();
	// editor.putBoolean(Constants.FIRST_TIME, firstTimeFlag);
	// editor.commit();
	// }

	// public void setCredentials(String username, String password){
	// Editor editor = User.edit();
	// editor.putString(Constants.USER_NAME, username);
	// editor.putString(Constants.PASSWORD, password);
	// editor.commit();
	// }

	// public void setRememberMe(Boolean rememberMeFlag) {
	// Editor editor = User.edit();
	// editor.putBoolean(Constants.REMEMBER_ME, rememberMeFlag);
	// editor.commit();
	// }

	// public void setAccessToken(String token){
	// Editor editor = User.edit();
	// editor.putString(Constants.ACCESS_TOKEN, token);
	// editor.commit();
	// }

	// public void setProfileImageUrl(String imageUrl){
	// Editor editor = User.edit();
	// editor.putString(Constants.PROFILE_PIC_URL, imageUrl);
	// editor.commit();
	// }

	public static void setUserCred(UserCred userCred, Context context) {
		if (prefs == null)
			prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = prefs.edit();
		editor.putString(Constants.ACCESS_TOKEN, userCred.getToken());
		editor.putInt(Constants.USER_ID, userCred.getUserId());
		editor.putString(Constants.USER_FIRST_NAME, userCred.getFirstname());
		editor.putString(Constants.USER_LAST_NAME, userCred.getLastname());
		editor.commit();
		Log.d("DBAServiceApplication", "setUserCred: Saved as ::\n " + userCred.toString());
	}

	public static UserCred getUserCred(Context context) {
		if (prefs == null)
			prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String token = prefs.getString(Constants.ACCESS_TOKEN, null);
		int uid = prefs.getInt(Constants.USER_ID, -1);
		String userFirstName = prefs.getString(Constants.USER_FIRST_NAME, null);
		String userLastName = prefs.getString(Constants.USER_LAST_NAME, null);
		String userEmail = prefs.getString(Constants.USER_EMAIL, null);
		UserCred userCred = new UserCred(token, uid, userFirstName, userLastName, userEmail);
		return userCred;
	}

	// public String getAccessToken(Context context) {
	// if (prefs == null)
	// prefs = PreferenceManager.getDefaultSharedPreferences(context);
	// String token = prefs.getString(Constants.ACCESS_TOKEN, null);
	// return token;
	// }

	public static String getAppAccessToken(Context context) {
		if (prefs == null)
			prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String token = prefs.getString(Constants.ACCESS_TOKEN, "none");
		return token;
	}

	public static int getUserId(Context context) {
		if (prefs == null)
			prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getInt(Constants.USER_ID, 0);
	}

	// public boolean isFirstTime() {
	// Boolean firstTimeFlag = User.getBoolean(Constants.FIRST_TIME, true);
	// return firstTimeFlag;
	// }
	// public String getUserName(){
	// String userName = User.getString(Constants.USER_NAME, null);
	// return userName;
	// }
	//
	// public String getPassword(){
	// String pass = User.getString(Constants.PASSWORD, null);
	// return pass;
	// }
	//
	// public boolean isRememberMe() {
	// Boolean rememberMeFlag = User.getBoolean(Constants.REMEMBER_ME, false);
	// return rememberMeFlag;
	// }
	//
	//
	// public String getProfileImageUrl(){
	// String imageUrl = User.getString(Constants.PROFILE_PIC_URL, null);
	// return imageUrl;
	// }
}
