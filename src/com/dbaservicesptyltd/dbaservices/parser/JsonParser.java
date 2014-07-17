package com.dbaservicesptyltd.dbaservices.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.dbaservicesptyltd.dbaservices.model.ServerResponse;
import com.dbaservicesptyltd.dbaservices.utils.Constants;

public class JsonParser {

	static InputStream is = null;
	static JSONObject jObj = null;
	static String json = "";

	private static final String TAG = JsonParser.class.getSimpleName();

	// constructor
	public JsonParser() {

	}

	public ServerResponse retrieveServerData(int reqType, String url, List<NameValuePair> urlParams, String content,
			String appToken) {
		Log.d(TAG, "in retrieveServerData method");

		int status = 0;

		StringBuilder sb = null;

		if (urlParams != null) {
			String paramString = URLEncodedUtils.format(urlParams, "utf-8");
			url += "?" + paramString;
		}
		Log.d(TAG, "url after param added = " + url);
		Log.d(TAG, "content body = " + content);

		// Making HTTP request
		try {
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpResponse httpResponse = null;

			if (reqType == Constants.REQUEST_TYPE_GET) {
				HttpGet httpGet = new HttpGet(url);
				httpGet.setHeader("Content-Type", "application/json");
				httpGet.setHeader("Accept", "application/json");
				if (appToken != null) {
					httpGet.setHeader("x-token", appToken);
				}

				httpResponse = httpClient.execute(httpGet);

			} else if (reqType == Constants.REQUEST_TYPE_POST) {
				HttpPost httpPost = new HttpPost(url);
				httpPost.setHeader("Content-Type", "application/json");
				httpPost.setHeader("Accept", "application/json");
				if (appToken != null) {
					httpPost.setHeader("x-token", appToken);
				}

				StringEntity se = new StringEntity(content);
				se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
				httpPost.setEntity(se);

				httpResponse = httpClient.execute(httpPost);
			} else if (reqType == Constants.REQUEST_TYPE_PUT) {
				HttpPut httpPut = new HttpPut(url);
				httpPut.setHeader("Content-Type", "application/json");
				httpPut.setHeader("Accept", "application/json");
				if (appToken != null) {
					httpPut.setHeader("x-token", appToken);
				}

				if (content != null) {
					StringEntity se = new StringEntity(content);
					se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
					httpPut.setEntity(se);
				}

				httpResponse = httpClient.execute(httpPut);
			} else if (reqType == Constants.REQUEST_TYPE_DELETE) {
				HttpDelete httpDelete = new HttpDelete(url);
				httpDelete.setHeader("Content-Type", "application/json");
				httpDelete.setHeader("Accept", "application/json");
				if (appToken != null) {
					httpDelete.setHeader("x-token", appToken);
				}

				if (content != null) {
					StringEntity se = new StringEntity(content);
					se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
					((HttpResponse) httpDelete).setEntity(se);
				}

				httpResponse = httpClient.execute(httpDelete);
			}

			status = httpResponse.getStatusLine().getStatusCode();
			Log.d(TAG, "STAUS = " + status);

			HttpEntity httpEntity = httpResponse.getEntity();
			is = httpEntity.getContent();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			Log.d(TAG, "trying to read input stream.");
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
			sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			Log.d(TAG, "sb = " + sb.toString());
			json = sb.toString();
		} catch (Exception e) {
			Log.e("Buffer Error", "Error converting result " + e.toString());
		}

		// try parse the string to a JSON object
		try {
			jObj = new JSONObject(json);
		} catch (JSONException e) {
			Log.e("JSON Parser", "Error parsing data " + e.toString());
		}

		// return ServerResponse
		return new ServerResponse(jObj, status);
	}

}
