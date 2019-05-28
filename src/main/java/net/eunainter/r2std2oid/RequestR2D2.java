package net.eunainter.r2std2oid;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class RequestR2D2 {
	
	private Integer		_id;

	private String 		url;
	private byte		publishMethod;
	private ArrayList<NameValuePair> params;
	private JSONObject 	_json;
	private String	mStringEntity;
	private String bearer = null;

	/**
	 * Object related to the request itself
	 */
	private Object transportObject;


	public static final byte POST	= 0;
	public static final byte GET 	= 1;
	public static final byte PUT 	= 2;

	public RequestR2D2(String uri, JSONObject jobject, byte pMethod) {
		this.url = uri.trim();
		
		params = new ArrayList<NameValuePair>();
		
		if (jobject == null)
			_json = new JSONObject();
		else
			_json = jobject;
		
		this.publishMethod = pMethod;
	}
	
	public RequestR2D2() {
	}

	public boolean addParValue(String parameter, String value) {
		return params.add(new BasicNameValuePair(parameter.trim(), value.trim()));
	}


	public boolean addParValue(String parameter, int value) {
		return params.add(new BasicNameValuePair(parameter.trim(), String.valueOf(value)));
	}

	public boolean addParValue(String parameter, JSONObject value) {
		return params.add(new BasicNameValuePair(parameter.trim(), value.toString()));
/*		try {
		    this._json.put(parameter.trim(), value);
		} catch (JSONException e) {
		    Log.e("R2STD2OID", "JSONException: " + e);
		    return false;
		}*/
		
		// return true;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public JSONObject getJson() {
		return this._json;
	}

	public JSONObject createJson() {
		List<NameValuePair> nvp = getNameValueJson();
		JSONObject 	json = new JSONObject();
		try {
			for (NameValuePair namevalue : nvp)
				json.put(namevalue.getName(), namevalue.getValue());
		} catch (JSONException e) {
		    Log.e("R2STD2OID", "JSONException: " + e);
		    return null;
		}
		
		return json;
	}
	
	public List<NameValuePair> getNameValueJson() {
		ArrayList<NameValuePair> parameters = new ArrayList<>();

		try {
			for (NameValuePair nvp : params) {
//				json.put(nvp.getName(), nvp.getValue());
				parameters.add(nvp);

			}
			if (this._json != null) {
				Iterator<String> it = this._json.keys();
				String key = null;
				while (it.hasNext()) {
					key = it.next();
					parameters.add(new BasicNameValuePair(key, _json.getString(key) ));
				}
			}
		} catch (JSONException e) {
			Log.e("R2STD2OID", "JSONException: " + e);
			return null;
		}


		
		return parameters;
	}

	public void setJson(JSONObject json) {
		this._json = json;
	}

	public byte getPublishMethod() {
		return publishMethod;
	}

	public void setPublishMethod(byte publishMethod) {
		this.publishMethod = publishMethod;
	}

	public Integer getId() {
		return _id;
	}

	public void setId(Integer _id) {
		this._id = _id;
	}

	public String getStringEntity() {
		return mStringEntity;
	}

	public void setStringEntity(String mStringEntity) {
		this.mStringEntity = mStringEntity;
	}

	public static String getReqMethod(int reqmeth) {
		switch (reqmeth) {
			case POST	: return "POST";
			case GET 	: return "GET";
			case PUT 	: return "PUT";
			default		: return "";
		}
	}

	public String getBearer() {
		return bearer;
	}

	public void setBearer(String bearer) {
		this.bearer = bearer;
	}

	public Object getTransportObject() {
		return transportObject;
	}

	public void setTransportObject(Object transportObject) {
		this.transportObject = transportObject;
	}

	public ArrayList<NameValuePair> getParams() {
		return params;
	}

	public String getParamValue(String par) {
		for (NameValuePair np : params) {
			if (np.getName().compareToIgnoreCase(par) == 0)
				return np.getValue();
		}

		return null;
	}
}
