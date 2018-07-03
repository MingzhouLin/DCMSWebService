package webservice.Client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import webservice.CenterServer;

import javax.xml.namespace.QName;

public class InteractionClient {

    public InteractionClient() {
    }

    public static void main(String[] args) throws Exception {
        (new InteractionClient()).scan(args);
    }

    public void scan(String[] args) throws Exception {
        new DataSeedingClient().run(args);
        Thread.sleep(50);
        String managerId = "";
        boolean ifContinue = true;
        QName qName = new QName("http://webservice/", "CenterServerImplService");


        do {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Please input your manager ID:");
            managerId = scanner.nextLine();
            if (!this.verifyId(managerId)) {
                System.out.println("ManagerId error. Please input again");
            } else {
                CenterServer server=DataSeedingClient.createRemoteReference(managerId.substring(0,3),qName);
                ifContinue=processOperation(server, managerId);
            }
        } while(ifContinue);

    }

    public boolean processOperation(CenterServer stub, String managerId) throws Exception {
        Scanner scanner = new Scanner(System.in);
        int option = 0;
        System.out.println("Please select your operation:");
        System.out.println("1> Create Teacher Record.");
        System.out.println("2> Create Student Record.");
        System.out.println("3> Get Record Counts.");
        System.out.println("4> Edit Record.");
        System.out.println("5> Transfer Record.");
        System.out.println("6> Test concurrently edit and transfer the same record.");
        System.out.println("7> Exit.");
        option = scanner.nextInt();
        switch(option) {
            case 1:
                this.createTRecord(stub, managerId);
                break;
            case 2:
                this.createSRecord(stub, managerId);
                break;
            case 3:
                this.getRecordCounts(stub, managerId);
                break;
            case 4:
                this.editRecord(stub, managerId);
                break;
            case 5:
                this.transferRecord(stub, managerId);
                break;
            case 6:
                this.testMultiThread(stub, managerId);
                break;
            case 7:
                System.out.println("GoodBye.");
        }

        return option != 7;
    }

    public void testMultiThread(CenterServer stub, String managerId) throws InterruptedException {
        String serverName=managerId.substring(0,3);
        System.out.println("The record id list below are belongs to your server. Please input the record id as your test case.");
        for (Map.Entry<String, String> entry: DataSeedingClient.recordForTestMultiThread.entrySet()
             ) {
            if(entry.getValue().equals(serverName)){
                System.out.println(entry.getKey());
            }
        }
        Scanner scanner=new Scanner(System.in);
        String recordId=scanner.nextLine().trim();
        System.out.println("Please input the field name you want to change:");
        String fieldName = scanner.nextLine().trim();
        System.out.println("Please input new value:");
        String newValue = scanner.nextLine().trim();
        System.out.println("Please input the destination to transfer:");
        String centerName = scanner.nextLine().trim();
        CompletableFuture<String> edit=new CompletableFuture<>();
        new Thread(()->{
            String result=stub.editRecord(managerId,recordId,fieldName,newValue);
            edit.complete(result);
        }).start();

        CompletableFuture<String> transfer=new CompletableFuture<>();
        new Thread(()->{
            String result = stub.transferRecord(managerId, recordId,centerName);
                transfer.complete(result);
        }).start();

        edit.thenAccept(s-> System.out.println(s));
        transfer.thenAccept(s-> System.out.println(s));
    }

    public void createTRecord(CenterServer stub, String managerId) throws RemoteException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please input teacher's first name:");
        String firstName = scanner.nextLine().trim();
        System.out.println("Please input teacher's last name:");
        String lastName = scanner.nextLine().trim();
        System.out.println("Please input teacher's address");
        String address = scanner.nextLine().trim();
        System.out.println("Please input your teacher's specialization:");
        String specialiazation = scanner.nextLine().trim();
        System.out.println("Please input teacher's location:");
        String location = scanner.nextLine().trim();
        System.out.println("Please input teacher's phone:");
        String phone = scanner.nextLine();
        String result = stub.createTRecord(managerId, firstName, lastName, address, phone, specialiazation, location);
        System.out.println("Teacher record with id: " + result + " was created");
    }

    public void createSRecord(CenterServer stub, String managerId) throws RemoteException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please input student's first name:");
        String firstName = scanner.nextLine().trim();
        System.out.println("Please input student's last name:");
        String lastName = scanner.nextLine().trim();
        System.out.println("Please input student's status:");
        String status = scanner.nextLine().trim();
        System.out.println("Please input your student's statusDate:");
        String statusDate = scanner.nextLine().trim();
        System.out.println("Please input student's courses(split with space):");
        String[] coursesRegistered = scanner.nextLine().split(" ");
        String result = stub.createSRecord(managerId, firstName, lastName, coursesRegistered, status, statusDate);
        System.out.println("Student record with id: " + result + " was created");
    }

    public void getRecordCounts(CenterServer stub, String managerId) throws RemoteException, NotBoundException {
        System.out.println(stub.getRecordCounts(managerId));
    }

    public void editRecord(CenterServer stub, String managerId) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please input your record id:");
        String recordId = scanner.nextLine().trim();
        System.out.println("Please input the field name you want to change:");
        String fieldName = scanner.nextLine().trim();
        System.out.println("Please input new value:");
        String newValue = scanner.nextLine().trim();
        String result = stub.editRecord(managerId, recordId, fieldName, newValue);
        System.out.printf(result + "\n");
    }
    public void transferRecord(CenterServer stub, String managerId) throws InvalidName, CannotProceed {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please input the transfer record id:");
        String recordId = scanner.nextLine().trim();
        System.out.println("Please input the destination to transfer:");
        String centerName = scanner.nextLine().trim();
        String result = stub.transferRecord(managerId,recordId,centerName);
        DataSeedingClient.recordForTestMultiThread.put(recordId, centerName);
        System.out.println(result);
    }

    public boolean verifyId(String managerId) throws Exception {
        if (managerId.length()!=7){
            return false;
        }else {
            String addr = managerId.substring(0, 3);
            return addr.equals("MTL") || addr.equals("LVL") || addr.equals("DDO");
        }
    }
}
