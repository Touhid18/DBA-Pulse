/**
 * 
 */
package com.dbaservicesptyltd.dbaservices.model;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Touhid
 * 
 */
public class OnlineAdminRow {

	private String name;
	private int user_id, active, pending, resolved;
	private boolean isOnline;

	public OnlineAdminRow(String adminName, int uId, int active, int pending, int resolved, boolean isOnLine) {
		this.name = adminName;
		this.user_id = uId;
		this.active = active;
		this.pending = pending;
		this.resolved = resolved;
		this.isOnline = isOnLine;
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
		return active;
	}

	/**
	 * @param active
	 *            the active to set
	 */
	public void setActive(int active) {
		this.active = active;
	}

	/**
	 * @return the pending
	 */
	public int getPending() {
		return pending;
	}

	/**
	 * @param pending
	 *            the pending to set
	 */
	public void setPending(int pending) {
		this.pending = pending;
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
		return isOnline;
	}

	/**
	 * @param isOnline
	 *            the isOnline to set
	 */
	public void setOnline(boolean isOnline) {
		this.isOnline = isOnline;
	}

	/**
	 * @param adminArray
	 *            as JSONArray
	 * @return parsed adminList
	 */
	public static ArrayList<OnlineAdminRow> parseNotifList(JSONArray adminArray) {
		ArrayList<OnlineAdminRow> adminList = new ArrayList<OnlineAdminRow>();

		GsonBuilder gsonb = new GsonBuilder();
		Gson gson = gsonb.create();

		try {
			for (int i = 0; i < adminArray.length(); i++) {

				JSONObject thisAdmin = adminArray.getJSONObject(i);
				if (thisAdmin != null) {
					String jsonString = thisAdmin.toString();
					OnlineAdminRow fav = gson.fromJson(jsonString, OnlineAdminRow.class);
					adminList.add(fav);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return adminList;
	}
}
