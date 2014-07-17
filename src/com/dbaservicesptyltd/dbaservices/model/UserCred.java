package com.dbaservicesptyltd.dbaservices.model;

import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class UserCred {
	private String token;
	private int user_id;
	private String firstname;
	private String lastname;
	private String email;

	public UserCred() {
	}

	public UserCred(String token, int uid, String firstname, String lastname, String email) {
		this.token = token;
		this.user_id = uid;
		this.firstname = firstname;
		this.lastname = lastname;
		this.email = email;
	}

	public static UserCred parseUserCred(JSONObject userObj) {
		UserCred userCred = new UserCred();

		GsonBuilder gsonb = new GsonBuilder();
		Gson gson = gsonb.create();
		if (userObj != null) {
			String jsonString = userObj.toString();
			userCred = gson.fromJson(jsonString, UserCred.class);
		}
		return userCred;
	}

	@Override
	public String toString() {
		return " UserID: " + user_id + "\n First Name: " + firstname + "\n Last Name: " + lastname + "\n Email: "
				+ email + "\n Token: " + token;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	/**
	 * @return the user_id
	 */
	public int getUserId() {
		return user_id;
	}

	/**
	 * @param user_id
	 *            the user_id to set
	 */
	public void setUserId(int user_id) {
		this.user_id = user_id;
	}

	/**
	 * @return the firstname
	 */
	public String getFirstname() {
		return firstname;
	}

	/**
	 * @param firstname
	 *            the firstname to set
	 */
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	/**
	 * @return the lastname
	 */
	public String getLastname() {
		return lastname;
	}

	/**
	 * @param lastname
	 *            the lastname to set
	 */
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email
	 *            the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}
}
