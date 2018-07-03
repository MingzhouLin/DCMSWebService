package webservice.UDP;

import webservice.ByteUtility;
import webservice.CenterServer;
import webservice.CenterServerImpl;
import webservice.Record.Records;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;

public class UDPServer implements Runnable {
    private int portNumber;
    private CenterServerImpl centerServer;
    private boolean stop = true;
    private DatagramSocket datagramSocket = null;

    public UDPServer(int portNumber, CenterServerImpl centerServer) {
        this.portNumber = portNumber;
        this.centerServer = centerServer;
    }

    public CenterServerImpl getCenterServer() {
        return centerServer;
    }

    public void setCenterServer(CenterServerImpl centerServer) {
        this.centerServer = centerServer;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }


    @Override
    public void run() {
        try {
            datagramSocket = new DatagramSocket(portNumber);
            byte[] buffer = new byte[1024];
            byte[] sendBuffer = new byte[1024];
            while (stop){
                DatagramPacket request = new DatagramPacket(buffer,buffer.length);
                try {
                    datagramSocket.receive(request);
                    System.out.printf("some data was recevied via udp");
                    Object object = null;
                    try {
                        object = ByteUtility.toObject(request.getData());

                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (object instanceof String) {
                        String receiveData =  (String)object;
                        System.out.printf(receiveData);
                        if (receiveData.equals("getCount")) {
                            String reply = centerServer.getLocalRecordCount() + "";
                            sendBuffer = reply.getBytes();
                        }
                    }else if (object instanceof Records){
                        Records record = (Records)object;
                        HashMap<Character,ArrayList<Records>> centerdata = centerServer.database;
                        synchronized (centerdata) {
                            if (centerdata.get(record.getLastName().charAt(0)) != null) {
                                centerdata.get(record.getLastName().charAt(0)).add(record);

                            } else {
                                ArrayList<Records> newArray = new ArrayList<>();
                                newArray.add(record);
                                centerdata.put(record.getLastName().charAt(0), newArray);
                                System.out.println(record.getRecordID());
                            }
                        }
                        String reply =  record.getRecordID() + " is stored in the " + centerServer.getCenterName() + " | ";
                        sendBuffer = reply.getBytes();
                    }

                    DatagramPacket send = new DatagramPacket(sendBuffer,sendBuffer.length,request.getAddress(),request.getPort());
                    datagramSocket.send(send);

                } catch (IOException e) {
                    System.out.println("UDP MTLServer socket is closed!");
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        System.out.println("UDP MTLServer is closed!");

    }

    public void stopServer(){
        datagramSocket.close();
        stop = false;

    }
}

