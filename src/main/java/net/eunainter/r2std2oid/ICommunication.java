package net.eunainter.r2std2oid;


public interface ICommunication {

	public RequestR2D2 convertToRequest(IDataOriginal data);
	
	public ResponseR2D2 sendRequest(RequestR2D2 theRequest);
}
