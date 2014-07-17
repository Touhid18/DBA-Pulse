package com.dbaservicesptyltd.dbaservices.model;

import org.json.JSONObject;

public class ServerResponse {

	JSONObject jObj;
	int status;

	public ServerResponse() {
	}

	public ServerResponse(JSONObject jObj, int status) {
		this.jObj = jObj;
		this.status = status;
	}

	public JSONObject getjObj() {
		return jObj;
	}

	public void setjObj(JSONObject jObj) {
		this.jObj = jObj;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

}
