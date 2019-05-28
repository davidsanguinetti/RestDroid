package net.eunainter.r2std2oid;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

public class R2stD2oidNew extends AsyncTask<RequestR2D2, Void, ResponseR2D2> {
	volatile HandleObservers observers;
	Timer mTimeout;

	int mRequestTimeout	= 5000;
	private final int TIMEOUT = 10000; // 10 seconds
	final String TAG = "R2stD2oid";

	private Object mLock = new Object();

	private static DefaultHttpClient httpClient;
	private static CookieStore mCookie = null;
	private static HttpContext localContext;

	private static HandleRequests handlerReq;

	Skyrunner skyDad;


	public R2stD2oidNew(Skyrunner sk) {
		skyDad = sk;
		observers = new HandleObservers();
		handlerReq = new HandleRequests();


/*		mTimeout =new Timer();
		mTimeout.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
//				Log.i("R2D2: TIMER", "Time to timeout: " + mRequestTimeout);
				if (mRequestTimeout > 0) {
					mRequestTimeout -= 1000;
					Log.i("SKYRUNNER TIMER", "time:" + String.valueOf(mRequestTimeout));
				}
				else {

					pauseExecution();
					this.cancel();
				}
			}
		}, 1000, 1000);*/
	}

	public void addObserver(RestObserver rObserver) {
		this.observers.addObserver(rObserver);
	}

	public void pauseExecution() {
		Log.i("R2D2: TIMER", "Timed out!");
		observers.timeoutObservers();
		this.cancel(true);
	}

	@Override
	protected ResponseR2D2 doInBackground(final RequestR2D2... requests) {

		try {
			Log.i("R2D2", "Sending req to: " + requests[0].getUrl());


			ResponseR2D2 resp = null;
			int counter = 0;
			do {
				resp = downloadUrl(requests[0]);

				if (resp.getCode() == ResponseR2D2.UNKNOWNHOST) {
					skyDad.warnLowConnection();
					Thread.sleep(5000);
				} else

				if (resp != null && resp.getCode() != 200) {
					skyDad.errorOcurred(resp);
						skyDad.warnErrorOccurred(resp.getMessage());
					break;
				}

				Log.i("RESTCounter", String.valueOf(counter));

				// Try for 5 times
				if (++counter > 5) {
					break;
				}

			} while (resp.getCode() == ResponseR2D2.UNKNOWNHOST);

			if (resp != null && resp.getMessage()!= null)
				Log.i("R2D2", "Response: " + resp.getId() + "; code:" + resp.getCode());
			return resp;
		} catch (Exception e) {
			Log.e("ERREQUEST", e.getMessage());
			return new ResponseR2D2(ResponseR2D2.STATUS_BAD_REQUEST, "Error processing request");
		}
	}

	// onPostExecute displays the results of the AsyncTask.
	@Override
	protected void onPostExecute(ResponseR2D2 result) {
		//        textView.setText(result);
		//		System.out.println("El result: " + result);

		Log.d("RestDroid", "Received from gwapi" + result.getMessage());

		ResponseR2D2 resp = result;
		if (result.getMessage().contains("Unable to resolve host")) {
			resp = new ResponseR2D2(400, "{\"message\":\"Couldn't contact server. Please contact support.\"}");
			skyDad.warnLowConnection();
		} else if (result == null) {
			resp = new ResponseR2D2(400, "{\"message\":\"Couldn't contact server. Please contact support.\"}");

			skyDad.warnErrorOccurred("Couldn't contact server. Please contact support");
		} else
			if (result.getCode() == 200) {
				this.observers.notifyObservers(resp);
				skyDad.handleRequests();
			}
			else
				skyDad.warnErrorOccurred(result.getMessage());
	}

	public ResponseR2D2 downloadUrl(RequestR2D2 myRequest) throws IOException {
		observers.progressObservers();

		InputStream is = null;
		// Only display the first 500 characters of the retrieved
		// web page content.
		String result ="";
		int len = 33000;

		int idRequest = myRequest.getId();


		int status = 200;
		String messageSend = "";

		try {
			HttpResponse httpResponse = null;

			/**
			 * Always gives priority to hardcoded string entity
			 */
/*
			JSONObject jsonObj = (myRequest.getJson() == null || myRequest.getJson().length() == 0)
					? myRequest.createJson()
							: myRequest.getJson();
*/



					//					(myRequest.getJson() == null ||
					//					myRequest.getJson().length() == 0) ?  myRequest.createJson() : myRequest.getJson();
					//			String st2send = myRequest.getStringEntity();

					if (myRequest.getPublishMethod() == RequestR2D2.POST || myRequest.getPublishMethod() == RequestR2D2.PUT) {
						URL url = new URL(myRequest.getUrl());
						List<NameValuePair> params = myRequest.getNameValueJson();


						StringBuilder postData = new StringBuilder();
						for (NameValuePair param : params) {
							if (postData.length() != 0) postData.append('&');
							postData.append(param.getName());
							postData.append('=');
						//	postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
							postData.append(String.valueOf(param.getValue()));
						}
						byte[] postDataBytes = postData.toString().getBytes("UTF-8");

						HttpURLConnection conn = (HttpURLConnection)url.openConnection();
						conn.setDoOutput(true);
						conn.setRequestMethod(RequestR2D2.getReqMethod(myRequest.getPublishMethod()));
						conn.setConnectTimeout(TIMEOUT);



						if (myRequest.getPublishMethod() == RequestR2D2.POST || myRequest.getPublishMethod() == RequestR2D2.PUT) {
//							conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
							// TODO : change between the two content types
							conn.setRequestProperty("Content-Type", "application/json");
//							conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));

							if (myRequest.getBearer() != null)
								conn.setRequestProperty("Authorization", "Bearer " + myRequest.getBearer());

							String jsonpars = myRequest.createJson().toString();
							byte[] postDataBytesJson = myRequest.getJson().toString().getBytes("UTF-8");

							OutputStreamWriter out;
							String sttokener;
							try {
								out= new OutputStreamWriter(conn.getOutputStream());
								sttokener =  new JSONTokener(myRequest.createJson().toString()).nextValue().toString();

							} catch (Exception e) {
								Log.e("R2stdroid", "Exception" + e.getMessage());

								ResponseR2D2 response = new ResponseR2D2(400, "Exception " + e.getMessage());
								response.setId(idRequest);
								response.setRequest(myRequest);
								response.setTransportedObject(myRequest.getTransportObject());

								return response;
							}
							String sttosend = myRequest.getJson().toString();
//							String query = object.getString("query");

							if (sttosend.length() > 2) {
								out.write(sttosend);
								Log.d("RESTDroid POST Send: ", sttosend);
							}
							else {
								out.write(sttokener);
								Log.d("RESTDroid TOKENER: ", sttokener);

							}
							out.close();
//							conn.getOutputStream().write(postDataBytesJson);

							status = conn.getResponseCode();

							try {
								InputStream ios = conn.getInputStream();
							}	catch(Exception e) {
								Log.d("SOR", e.getMessage());
							}
						}

						int st = conn.getResponseCode();
						InputStream istr;
						if (conn.getResponseCode() == 200 || conn.getResponseCode() == 201)
							istr = conn.getInputStream();
						else
							istr = conn.getErrorStream();

						Reader in = new BufferedReader(new InputStreamReader(istr, "UTF-8"));
						StringBuilder stb = new StringBuilder("");
						for ( int c = in.read(); c != -1; c = in.read() )
							stb.append((char) c);

 						Log.i("RestDroid", stb.toString());
						messageSend = stb.toString();

						// GET
					} else {
						/*
						 * Builds the url to send
						 */
						Uri.Builder bld = Uri.parse(myRequest.getUrl()).buildUpon();
						List<NameValuePair> params = myRequest.getNameValueJson();
						for (int i=0; i < params.size(); i++)
							bld.appendQueryParameter(params.get(i).getName(), params.get(i).getValue());
						String urltosend = bld.toString();

						if (urltosend!= null && !urltosend.contains("mobwell"))
						Log.d("RESTDroid", urltosend);

						if (urltosend == null)
							return null;

						HttpGet request = new HttpGet(urltosend);

						if (myRequest.getBearer() != null)
							request.addHeader("Authorization", "Bearer " + myRequest.getBearer());



						try {
							httpResponse = R2stD2oidNew.getHttpClient().execute(request, localContext);

							//				HttpResponse
							HttpEntity httpEntity = httpResponse.getEntity();

							if (httpEntity != null) {
								//						InputStream ist = httpEntity.getContent();

								messageSend = EntityUtils.toString(httpEntity, HTTP.UTF_8);

								//						messageSend = readIt(ist);

								//					Log.i(TAG, "Result: " + result);
							}
						} catch (UnknownHostException e ) {
							Log.e(TAG, "Unknownk hostException: " + e.getMessage());
							status = ResponseR2D2.UNKNOWNHOST;
							messageSend = e.getLocalizedMessage();
						} catch (ClientProtocolException e) {
							Log.e(TAG, "ClientProtocolException: " + e);
							status = ResponseR2D2.STATUS_CLIENTPROTOCOL;
							messageSend = e.getLocalizedMessage();
						} catch (IOException e) {
							Log.e(TAG, "IOException: " + e);

							status = ResponseR2D2.STATUS_IOEXCEPTION;
							messageSend = e.getLocalizedMessage();
						}
						catch (Exception e) {
							//				if (status =)
							Log.e(TAG, "General Exception: " + e);

							status = ResponseR2D2.STATUS_GENERAL_ERROR;
							messageSend = e.getLocalizedMessage();
						}
					}

//					if (messageSend != null)
//							if (status == 200 || !messageSend.isEmpty()){
				ResponseR2D2 response = new ResponseR2D2(status, messageSend);
				response.setId(idRequest);
				response.setRequest(myRequest);
				response.setTransportedObject(myRequest.getTransportObject());

				return response;
//			} else {
//
//					}
		} finally {
			if (httpClient != null) {
				ClientConnectionManager cmanager = R2stD2oidNew.getHttpClient().getConnectionManager();

				if (cmanager != null)
					cmanager.closeIdleConnections(3,TimeUnit.SECONDS);
			}
		}

//		return null;
	}

	/**
	 * Reads an InputStream and converts it to a String.
	 * (Deprecated)
	 * @param stream
	 * @return
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	public String readIt(InputStream stream) throws IOException, UnsupportedEncodingException {
		Reader reader = null;
		reader = new InputStreamReader(stream, "UTF-8");
		char[] buffer = new char[33000];
		reader.read(buffer);
		return new String(buffer);
	}

	public static DefaultHttpClient getHttpClient() {
		/*
		if (httpClient == null) {
			httpClient = new DefaultHttpClient();
			if (mCookie == null) {

				// Create local HTTP context
				localContext = new BasicHttpContext();
				// Bind custom cookie store to the local context
				localContext.setAttribute(ClientContext.COOKIE_STORE, mCookie);


				mCookie = httpClient.getCookieStore();
				httpClient.setCookieStore(mCookie);
			}
//			httpClient.setCookieStore(mCookie);

		}*/
		//		if (httpClient == null) {
		httpClient = new DefaultHttpClient();
		httpClient.getParams().setParameter(CoreProtocolPNames.HTTP_ELEMENT_CHARSET,
				"utf-8");

		if (mCookie == null) {

			// Create local HTTP context
			localContext = new BasicHttpContext();
			// Bind custom cookie store to the local context
			localContext.setAttribute(ClientContext.COOKIE_STORE, mCookie);


			mCookie = httpClient.getCookieStore();
		}
		httpClient.setCookieStore(mCookie);

		//		}
		return httpClient;
	}

	public int getRequestTimeout() {
		return mRequestTimeout;
	}

	public void setRequestTimeout(int mRequestTimeout) {
		this.mRequestTimeout = mRequestTimeout;
	}


}
