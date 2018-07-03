package webservice;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

public class Client {

	public static void main(String[] args) throws Exception {
		
		ws_setup();
	}
	
	private static void ws_setup() {
		try {
			URL url = new URL("http://localhost:8080/addService?wsdl");

			QName qName = new QName("http://webservice/", "AddImplmentationService");
			
			Service service = Service.create(url, qName);	// fetch service (similar to "LocateRegistry.lookup" that returns object of type "Remote")
			
			CenterServer remoteInterface = service.getPort(CenterServer.class);		// similar to type-casting into proxy object
			
			System.out.println(remoteInterface);
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
