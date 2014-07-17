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
public class NotifItem {
	private String description, datetime, updated, clientName;
	private int id, clientID, userID, status, severity;

	public NotifItem(int id, int clientID, int userID, int status, int severity, String description, String datetime,
			String updated, String clientName) {
		this.id = id;
		this.clientID = clientID;
		this.userID = userID;
		this.status = status;
		this.severity = severity;
		this.description = description;
		this.datetime = datetime;
		this.updated = updated;
		this.clientName = clientName;
	}

	public static ArrayList<NotifItem> parseNotifList(JSONArray notifArray) {
		ArrayList<NotifItem> notifList = new ArrayList<NotifItem>();

		GsonBuilder gsonb = new GsonBuilder();
		Gson gson = gsonb.create();

		try {
			for (int i = 0; i < notifArray.length(); i++) {

				JSONObject thisFav = notifArray.getJSONObject(i);
				if (thisFav != null) {
					String jsonString = thisFav.toString();
					NotifItem fav = gson.fromJson(jsonString, NotifItem.class);
					notifList.add(fav);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return notifList;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the datetime
	 */
	public String getDatetime() {
		return datetime;
	}

	/**
	 * @param datetime
	 *            the datetime to set
	 */
	public void setDatetime(String datetime) {
		this.datetime = datetime;
	}

	/**
	 * @return the updated
	 */
	public String getUpdated() {
		return updated;
	}

	/**
	 * @param updated
	 *            the updated to set
	 */
	public void setUpdated(String updated) {
		this.updated = updated;
	}

	/**
	 * @return the clientName
	 */
	public String getClientName() {
		return clientName;
	}

	/**
	 * @param clientName
	 *            the clientName to set
	 */
	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the clientID
	 */
	public int getClientID() {
		return clientID;
	}

	/**
	 * @param clientID
	 *            the clientID to set
	 */
	public void setClientID(int clientID) {
		this.clientID = clientID;
	}

	/**
	 * @return the userID
	 */
	public int getUserID() {
		return userID;
	}

	/**
	 * @param userID
	 *            the userID to set
	 */
	public void setUserID(int userID) {
		this.userID = userID;
	}

	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * @return the severity
	 */
	public int getSeverity() {
		return severity;
	}

	/**
	 * @param severity
	 *            the severity to set
	 */
	public void setSeverity(int severity) {
		this.severity = severity;
	}
}
