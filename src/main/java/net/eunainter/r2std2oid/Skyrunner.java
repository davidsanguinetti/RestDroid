package net.eunainter.r2std2oid;

import android.app.Activity;
import android.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

public class Skyrunner implements RestObserver {

	private R2stD2oidNew mR2d2;
	private RestObserver mrObserver;
	private int mTimeout = 10000;

	private final static int TIMEOUT = 10;

	HashMap<RequestR2D2, Integer> mRequestsTag;
	HashMap<RequestR2D2, ResponseR2D2> mRequestResponse;

	// Map of requests
	private HashMap<Integer, Integer> mRequestIds;

	public void setmRequestIds(HashMap<Integer, Integer> mRequestIds) {
		this.mRequestIds = mRequestIds;
	}

	private Stack<RequestR2D2> priorityRequests;
	private Queue<RequestR2D2> secondaryRequests;

	private boolean warnError = true;


	/*
	 * Five tag for requests.
	 * Enums are not easy to implement in switch in Java: traditional method used
	 */
	public static final class RequestTag {
		public static final int KPOSONE		= 1;
		public static final int KPOSTWO		= 2;
		public static final int KPOSTHREE	= 3;
		public static final int KPOSFOUR	= 4;
		public static final int KPOSFIVE	= 5;
		public static final int SHAREPARAMS	= 0;
	}

	public Skyrunner(int timeout) {
		mR2d2 = new R2stD2oidNew(this);
		this.mTimeout = timeout;
		mR2d2.setRequestTimeout(timeout);

		mRequestIds = new HashMap<Integer, Integer>();

		mRequestsTag = new HashMap<>();
		mRequestResponse = new HashMap<>();

		priorityRequests = new Stack<>();
		secondaryRequests = new LinkedList<RequestR2D2>();
	}

	public Skyrunner(RestObserver robserver) {
		this(TIMEOUT);
		this.mrObserver = robserver;
	}

	private void createConnection(){
		mR2d2 = new R2stD2oidNew(this);
		mR2d2.setRequestTimeout(mTimeout);
		mR2d2.addObserver(mrObserver);
		mR2d2.addObserver(this);
	}


	public void addObserver(RestObserver respobs) {
		this.mrObserver = respobs;
		mR2d2.addObserver(respobs);
	}

	public void sendRequest(RequestR2D2 request, int tag) {
		if (request == null ||
				(request.getBearer() != null && request.getBearer().compareTo("exp") == 0))
			return;

		createConnection();
		request.setId(tag);
		mR2d2.execute(request);
		mRequestIds.put(request.getId(), tag);

		//	long tempo = (Calendar.getInstance().getTimeInMillis());

		//	mRequestsTag.put(request, tag);
	}

	public void addPriorityRequest(RequestR2D2 req, int tag) {
		mRequestsTag.put(req, tag);

		priorityRequests.push(req);


	}

	public void addSecondaryRequest(RequestR2D2 req, int tag) {
		mRequestsTag.put(req, tag);

		secondaryRequests.add(req);
	}

	public void handleRequests() {
		if (!priorityRequests.empty()) {
		RequestR2D2 req = priorityRequests.pop();
			sendRequest(req, mRequestsTag.get(req));
		} else {
			if (!secondaryRequests.isEmpty()) {
				RequestR2D2 req = secondaryRequests.remove();
				sendRequest(req, mRequestsTag.get(req));
			}
		}
	}

	public HashMap<Integer, Integer> getRequestIds() {

		return mRequestIds;
	}

	public void warnErrorOccurred(final String error) {

		Activity av = null;
		Fragment fr = null;

		try {

			if (mrObserver instanceof Activity)
				av = (Activity) mrObserver;
			else if (mrObserver instanceof Fragment) {
				fr = (Fragment) mrObserver;

				if (fr != null)
					av = fr.getActivity();
			} else {
				mrObserver.requestTimeout();
				return;
			}

		} catch(ClassCastException e) {
			return;
		}

		Log.i("RESTDROID TIMEOUT", "Activity: " + av + "\nFragment fr: " + fr);

		av.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				JSONObject jobj= null;

				String errormsg = error;

//				mrObserver.errorOcurred();

				try {
					jobj = new JSONObject(error);

					errormsg = jobj.getString("message");

					errormsg = jobj.getJSONArray("validationErrors").getJSONObject(0).getString("message");

				} catch (JSONException e) {
					errormsg = error;
				}

				if (!warnError)
					return;

				Toast.makeText(((Activity) mrObserver), "An error occurred: " + errormsg, Toast.LENGTH_SHORT).show();

				mrObserver.endConnecting();
			}
		});
	}

	public void warnLowConnection() {
		Activity av = null;
		Fragment fr = null;

		try {


			if (mrObserver instanceof Activity)
				av = (Activity) mrObserver;
			else if (mrObserver instanceof Fragment) {
				fr = (Fragment) mrObserver;

				if (fr != null)
					av = fr.getActivity();
			} else {
				mrObserver.requestTimeout();
				return;
			}

		} catch(ClassCastException e) {
			return;
		}

		Log.i("RESTDROID TIMEOUT", "Activity: " + av + "\nFragment fr: " + fr);
		final Activity a =  av;

			av.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(a, "An error occurred;\nPlease check your Internet connection or contact support", Toast.LENGTH_SHORT).show();
				}
			});

	}

	@Override
	public void receivedResponse(ResponseR2D2 response) {
		handleRequests();

		mRequestResponse.put(response.getRequest(), response);
	}

	@Override
	public void startConnecting() {

	}

	@Override
	public void endConnecting() {

	}

	@Override
	public void requestTimeout() {

	}

	@Override
	public void errorOcurred(ResponseR2D2 response) {
		mrObserver.errorOcurred(response);
	}

    public void setWarnError(boolean warnError) {
        this.warnError = warnError;
    }
}
