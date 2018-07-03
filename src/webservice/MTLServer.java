package webservice;

import javax.jws.WebService;
import javax.xml.ws.Endpoint;


public class MTLServer{

	public static void main(String[] args) throws Exception{
		
		ws_setup();
	}
		
	private static void ws_setup() throws Exception {
		String centerRegistryHost = "localhost";
		int centerRegistryUDPPort = 8190;
		String serverName = "MTL";
		CenterServerImpl centerServer = new CenterServerImpl(serverName, 8180,centerRegistryHost, centerRegistryUDPPort);		// server implementation class

		Endpoint e = Endpoint.publish("http://localhost:8080/MTLServer", centerServer);		// binding it to service registry --> WSDL file is created
		
		System.out.println("Is Published : " + e.isPublished());		// check
	}

}
