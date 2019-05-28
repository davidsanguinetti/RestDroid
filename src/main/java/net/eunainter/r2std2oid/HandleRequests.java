package net.eunainter.r2std2oid;

import java.util.ArrayList;
import java.util.Random;


/**
 * Requests Factory
 * 
 * @author dsangui
 *
 */
public class HandleRequests {

	private static ArrayList<RequestR2D2> _requests;

	public HandleRequests() {
		_requests = new ArrayList<RequestR2D2>();


	}

	/**
	 * Stores the request on a map with a key indexing it
	 * 
	 * @param request The request to store
	 */
	private static Integer storeRequest(RequestR2D2 request) {
		boolean equal = false;
		Integer id = 0;
		//		RequestR2D2 req;

		do {
			id = (Integer) new Random().nextInt(Short.MAX_VALUE);
			
			for (RequestR2D2 req : _requests)
				equal = (id == req.getId()); 
		}
		while (equal);
		
		request.setId(id);
		_requests.add(request);
		
		return id;
	}
	
	
	public static RequestR2D2 getRequest() {
		RequestR2D2 request = new RequestR2D2();
		
		storeRequest(request);
		
		return request;
	}
	
	/**
	 * Fabricates a new request method
	 * 
	 * @param uri The url of the call
	 * @param method POST or GET
	 * @return The request
	 */
	public static RequestR2D2 getRequest(String uri, byte method) {
		RequestR2D2 request = new RequestR2D2(uri, null, method);
		
		storeRequest(request);
		
		return request;
	}
	


}
