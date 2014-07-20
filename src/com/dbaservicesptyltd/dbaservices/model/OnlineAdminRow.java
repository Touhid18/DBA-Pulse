/**
 * 
 */
package com.dbaservicesptyltd.dbaservices.model;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Touhid
 * 
 */
public class OnlineAdminRow {

	private String name;
	private int user_id, actioned, ignored, resolved;
	private boolean is_online;

	public OnlineAdminRow(String adminName, int uId, int active, int pending, int resolved, boolean isOnLine) {
		this.name = adminName;
		this.user_id = uId;
		this.actioned = active;
		this.ignored = pending;
		this.resolved = resolved;
		this.is_online = isOnLine;
	}

	@Override
	public String toString() {
		return "Name: " + name + ", user_id=" + user_id + ", actioned=" + actioned + ", ignored=" + ignored
				+ ", resolved=" + resolved + ", is_online:" + is_online;
	}

	/**
	 * @return the adminName
	 */
	public String getAdminName() {
		return name;
	}

	/**
	 * @param adminName
	 *            the adminName to set
	 */
	public void setAdminName(String adminName) {
		this.name = adminName;
	}

	/**
	 * @return the user_id
	 */
	public int getUserId() {
		return user_id;
	}

	/**
	 * @param userId
	 *            the user_id to set
	 */
	public void setUserId(int userId) {
		this.user_id = userId;
	}

	/**
	 * @return the active
	 */
	public int getActive() {
		return actioned;
	}

	/**
	 * @param active
	 *            the active to set
	 */
	public void setActive(int active) {
		this.actioned = active;
	}

	/**
	 * @return the pending
	 */
	public int getPending() {
		return ignored;
	}

	/**
	 * @param pending
	 *            the pending to set
	 */
	public void setPending(int pending) {
		this.ignored = pending;
	}

	/**
	 * @return the resolved
	 */
	public int getResolved() {
		return resolved;
	}

	/**
	 * @param resolved
	 *            the resolved to set
	 */
	public void setResolved(int resolved) {
		this.resolved = resolved;
	}

	/**
	 * @return the isOnline
	 */
	public boolean isOnline() {
		return is_online;
	}

	/**
	 * @param isOnline
	 *            the isOnline to set
	 */
	public void setOnline(boolean isOnline) {
		this.is_online = isOnline;
	}

	/**
	 * @param adminArray
	 *            as JSONArray
	 * @return parsed adminList
	 */
	public static ArrayList<OnlineAdminRow> parseAdminList(JSONArray adminArray) {
		ArrayList<OnlineAdminRow> adminList = new ArrayList<OnlineAdminRow>();

		GsonBuilder gsonb = new GsonBuilder();
		Gson gson = gsonb.create();

		try {
			for (int i = 0; i < adminArray.length(); i++) {

				JSONObject thisAdmin = adminArray.getJSONObject(i);
				if (thisAdmin != null) {
					String jsonString = thisAdmin.toString();
					OnlineAdminRow admin = gson.fromJson(jsonString, OnlineAdminRow.class);
					Log.d("parseAdminList",admin.toString());
					adminList.add(admin);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return adminList;
	}
}
