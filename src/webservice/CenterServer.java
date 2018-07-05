package webservice;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import java.util.ArrayList;


@WebService
public interface CenterServer {

	@WebMethod
	public String createTRecord (String managerId,String firstName,String lastName,String address,String phone,String specialization,String location);
	@WebMethod
	public String createSRecord (String managerId, String firstName, String lastName, String[] courseRegistered, String status, String statusDate);
	@WebMethod
	public String getRecordCounts (String managerId);
	@WebMethod
	public String editRecord ( String managerId,String recordID, String fieldName,String newValue);
	@WebMethod
	public String transferRecord(String managerID, String recordID, String remoteCenterServerName);
	@WebMethod
	public int getLocalRecordCount();
	@WebMethod
	public void shutdown();
}
