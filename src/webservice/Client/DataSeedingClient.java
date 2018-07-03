package webservice.Client;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import webservice.CenterServer;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
public class DataSeedingClient {
    public static HashMap<String, String> recordForTestMultiThread= new HashMap<String, String>();
    public static final String MTL_URL="http://localhost:8080/MTLServer?wsdl";
    public static final String LVL_URL="http://localhost:8081/LVLServer?wsdl";
    public static final String DDO_URL="http://localhost:8082/DDOServer?wsdl";
    public void run(String[] args) throws Exception {
        new DataSeedingClient().scan(args);
    }

    public void scan(String[] args) throws Exception {
        File file=new File("src/webservice/operation.txt");
        InputStreamReader reader=new InputStreamReader(new FileInputStream(file));
        BufferedReader input=new BufferedReader(reader);
        String line=input.readLine();

        QName qName = new QName("http://webservice/", "CenterServerImplService");

        while (line!=null){
            String[] parameters=line.split("\\|");
            if (!verifyId(parameters[0])){
                line=input.readLine();
                continue;
            }
            String serverName=parameters[0].substring(0,3);
            CenterServer service=createRemoteReference(serverName,qName);
            CompletableFuture<String> result;
            switch (parameters[1]) {
                case "createTRecord": {
                    result=createTRecord(service,parameters);
                    result.thenAccept(s-> {
                                System.out.println("Create Successful, recordId is "+s);
                                recordForTestMultiThread.put(s, serverName);
                            }
                    );
                    break;
                }
                case "createSRecord": {
                    result=createSRecord(service,parameters);
                    result.thenAccept(s-> {
                        System.out.println("Create Successful, recordId is "+s);
                        recordForTestMultiThread.put(s, serverName);
                    });
                    break;
                }
                case "getRecordCounts": {
                    result=getRecordCounts(service,parameters[0]);
                    result.thenAccept(s-> System.out.println("Record number is "+s));
                    break;
                }
//                case "editRecord": {
//                    result=editRecord(service, parameters);
//                    result.thenAccept(System.out::println);
//                    break;
//                }
                case "Exit": {
                    System.out.println("GoodBye.");
                    break;
                }
            }
            line=input.readLine();
        }
    }

    public static CenterServer createRemoteReference(String serverName, QName qName) throws MalformedURLException {
        URL url=new URL(MTL_URL);
        switch (serverName) {
            case "MTL":{
                url = new URL(MTL_URL);
                break;
            }
            case "LVL":{
                url = new URL(LVL_URL);
                break;
            }
            case "DDO":{
                url = new URL(DDO_URL);
            }
        }
        Service service = Service.create(url, qName);	// fetch service (similar to "LocateRegistry.lookup" that returns object of type "Remote")

        return service.getPort(CenterServer.class);
    }

    private CompletableFuture<String> createTRecord(CenterServer stub, String[] parameters)  {
        String firstName = parameters[2];
        String lastName = parameters[3];
        String address = parameters[4];
        String specialiazation=parameters[5];
        String location = parameters[6];
        String phone =  parameters[7];
        CompletableFuture<String> future=new CompletableFuture<>();
        new Thread(()->{
            String recordId=stub.createTRecord(parameters[0], firstName, lastName, address, phone, specialiazation, location);
            future.complete(recordId);
        }).start();
        return future;
    }

    private CompletableFuture<String> createSRecord(CenterServer stub, String[] parameters) {
        String firstName = parameters[2];
        String lastName = parameters[3];
        String status = parameters[4];
        String statusDate = parameters[5];
        String[] coursesRegistered=parameters[6].split(" ");
        CompletableFuture<String> future=new CompletableFuture<>();
        new Thread(()->{
            String recordId=stub.createSRecord(parameters[0], firstName, lastName, coursesRegistered, status, statusDate);
            future.complete(recordId);
        }).start();
        return future;
    }

    public CompletableFuture<String> getRecordCounts(CenterServer stub, String managerId) {
        CompletableFuture<String> future=new CompletableFuture<>();
        new Thread(()->{
            String counts=stub.getRecordCounts(managerId);
            future.complete(counts);
        }).start();
        return future;
    }

//    private CompletableFuture<String> editRecord(CenterServer stub, String[] parameters) throws Exception {
//        String recordId = parameters[2];
//        String fieldName = parameters[3];
//        String newValue = parameters[4];
//        CompletableFuture<String> future=new CompletableFuture<>();
//        new Thread(()->{
//            String result= null;
//            try {
//                result = stub.editRecord(parameters[0], recordId, fieldName, newValue);
//            } catch (CenterServerOrb.CenterServerPackage.except except) {
//                except.printStackTrace();
//            }
//            future.complete(result);
//        }).start();
//        return future;
//    }

    private boolean verifyId(String managerId) {
        String addr = managerId.substring(0, 3);
        return addr.equals("MTL") || addr.equals("LVL") || addr.equals("DDO");
    }
}
