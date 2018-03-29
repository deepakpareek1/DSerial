package com.plancktech.dserial_android.Activity;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DiscoverFromServer implements Runnable {

    private MainActivity activity;
    String msg = "";

    public static DiscoverFromServer getInstance() {

        return DiscoveryThreadHolder.INSTANCE;
    }

    public void setActivity(MainActivity activity) {
        this.activity = activity;
    }

    private static class DiscoveryThreadHolder {

        private static final DiscoverFromServer INSTANCE = new DiscoverFromServer();
    }

    DatagramSocket socket;

    @Override
    public void run() {
        try {
            //Keep a socket open to listen to all the UDP trafic that is destined for this port
            socket = new DatagramSocket(8888, InetAddress.getByName("0.0.0.0"));
            socket.setBroadcast(true);

            while (true) {
                System.out.println(getClass().getName() + ">>>Ready to receive broadcast packets!");
                msg += ">>>Ready to receive broadcast packets!\n";
                SetMessage(msg);

                //Receive a packet
                byte[] recvBuf = new byte[15000];
                DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                socket.receive(packet);

                //Packet received
                System.out.println(getClass().getName() + ">>>Discovery packet received from: " + packet.getAddress().getHostAddress());
                System.out.println(getClass().getName() + ">>>Packet received; data: " + new String(packet.getData()));
                msg += ">>>Discovery packet received from: " + packet.getAddress().getHostAddress() + "\n";
                msg += ">>>Packet received; data: " + new String(packet.getData()) + "\n";
                SetMessage(msg);

                //See if the packet holds the right command (message)
                String message = new String(packet.getData()).trim();
                if (message.equals("DISCOVER_FUIFSERVER_REQUEST")) {
                    byte[] sendData = "DISCOVER_FUIFSERVER_RESPONSE".getBytes();

                    //Send a response
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());
                    socket.send(sendPacket);

                    System.out.println(getClass().getName() + ">>>Sent packet to: " + sendPacket.getAddress().getHostAddress());
                    msg += "-----------\n";
                    msg += getClass().getName() + ">>>Sent packet to: " + sendPacket.getAddress().getHostAddress() + " Port: " + sendPacket.getPort()  + "\n";
                    msg += "-----------\n";
                    SetMessage(msg);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(DiscoverFromServer.class.getName()).log(Level.SEVERE, null, ex);
            msg += ex.toString();
           SetMessage(msg);
        }
    }

    public void SetMessage(final String txtMsg) {
        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                activity.msg.setText(txtMsg);
            }
        });
    }
}
