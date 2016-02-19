package com.company;

import javax.swing.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by LogiX on 2016-02-19.
 */
public class PassiveConnection extends SwingWorker<Void, Void> {
    private Socket dataSocket;
    private ServerSocket serverSocket;

    public PassiveConnection(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    @Override
    protected Void doInBackground() throws Exception {
        dataSocket = serverSocket.accept();
        System.out.println("client connected");
        return null;
    }

    public Socket getDataSocket() {
        return this.dataSocket;
    }
}
