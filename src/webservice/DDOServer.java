package webservice;

import javax.jws.WebService;
import javax.xml.ws.Endpoint;
import java.util.Scanner;

public class DDOServer extends CenterServerImpl{

	public static void main(String[] args) throws Exception{
		
		ws_setup();
	}

	private static void ws_setup() throws Exception {
		String centerRegistryHost = "localhost";
		int centerRegistryUDPPort = 8190;
		String serverName = "DDO";
		CenterServerImpl centerServer = new CenterServerImpl(serverName, 8182, centerRegistryHost, centerRegistryUDPPort);		// server implementation class

		Endpoint e = Endpoint.publish("http://localhost:8082/DDOServer", centerServer);		// binding it to service registry --> WSDL file is created
		
		System.out.println("Is Published : " + e.isPublished());
		System.out.println("press stop to shut down!");
		Scanner scanner = new Scanner(System.in);
		if (scanner.nextLine().equals("stop")){
			e.stop();
			centerServer.shutdown();
		}// check
	}

}
