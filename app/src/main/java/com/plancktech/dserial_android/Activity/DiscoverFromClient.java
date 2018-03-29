package com.plancktech.dserial_android.Activity;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DiscoverFromClient implements Runnable {

    MainActivity activity;
    String msg = "";

    public static DiscoverFromClient getInstance() {
        return DiscoverFromClient.DiscoveryThreadHolder.INSTANCE;
    }

    public void setActivity(MainActivity activity) {
        this.activity = activity;
    }

    private static class DiscoveryThreadHolder {

        private static final DiscoverFromClient INSTANCE = new DiscoverFromClient();
    }

    DatagramSocket socket;

    @Override
    public void run() {

        // Find the server using UDP broadcast
        try {
            //Open a random port to send the package
            socket = new DatagramSocket();
            socket.setBroadcast(true);

            byte[] sendData = "DISCOVER_FUIFSERVER_REQUEST".getBytes();

            //Try the 255.255.255.255 first
            try {
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), 8888);
                socket.send(sendPacket);
                System.out.println(getClass().getName() + ">>> Request packet sent to: 255.255.255.255 (DEFAULT)");
                msg = ">>> Request packet sent to: 255.255.255.255 (DEFAULT)\n";
                SetMessage(msg);

            } catch (Exception e) {
                msg += e.toString();
                SetMessage(msg);
            }

            // Broadcast the message over all the network interfaces
            Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();

                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue; // Don't want to broadcast to the loopback interface
                }

                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress broadcast = interfaceAddress.getBroadcast();
                    if (broadcast == null) {
                        continue;
                    }

                    // Send the broadcast package!
                    try {
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, 8888);
                        socket.send(sendPacket);
                    } catch (Exception e) {
                        msg += e.toString();
                        SetMessage(msg);
                    }

                    System.out.println(getClass().getName() + ">>> Request packet sent to: " + broadcast.getHostAddress() + "; Interface: " + networkInterface.getDisplayName());
                    msg += ">>> Request packet sent to: " + broadcast.getHostAddress() + "; Interface: " + networkInterface.getDisplayName() + "\n";
                    SetMessage(msg);
                }
            }

            System.out.println(getClass().getName() + ">>> Done looping over all network interfaces. Now waiting for a reply!");
            msg += ">>> Done looping over all network interfaces. Now waiting for a reply! \n";
            SetMessage(msg);

            //Wait for a response
            byte[] recvBuf = new byte[15000];
            DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
            socket.receive(receivePacket);

            //We have a response
            System.out.println(getClass().getName() + ">>> Broadcast response from server: " + receivePacket.getAddress().getHostAddress());
            msg += ">>> Broadcast response from server: " + receivePacket.getAddress().getHostAddress() + "\n";
            SetMessage(msg);

            //Check if the message is correct
            String message = new String(receivePacket.getData()).trim();
            if (message.equals("DISCOVER_FUIFSERVER_RESPONSE")) {
                //DO SOMETHING WITH THE SERVER'S IP (for example, store it in your controller)
                //Controller_Base.setServerIp(receivePacket.getAddress());
                msg += "-----------\n";
                msg += "Target Address: " + receivePacket.getAddress().getHostAddress() + " Port: " + receivePacket.getPort() + "\n";
                msg += "-----------\n";
                SetMessage(msg);
            }

            //Close the port!
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(DiscoverFromClient.class.getName()).log(Level.SEVERE, null, ex);
            msg += ex.toString();
            SetMessage(msg);
        }
    }

    public void SetMessage(final String txtMsg) {
        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                activity.response.setText(txtMsg);
            }
        });
    }

}
