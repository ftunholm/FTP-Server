package com.company;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by LogiX on 2016-02-19.
 */
public class FtpServer {
    private final static int PORT = 21;
    private final static int NUMBER_OF_CLIENTS_ALLOWED = 20;
    public static final String PASSWORD = "kanske";
    public static final String USERNAME = "kalle";

    public FtpServer() {
        startServer();
    }

    private void startServer() {
        final ExecutorService clientProcessingPool = Executors.newFixedThreadPool(NUMBER_OF_CLIENTS_ALLOWED);
        Runnable serverTask = () -> {
            try {
                ServerSocket serverSocket = new ServerSocket(PORT);
                System.out.println("Waiting for clients to connect...");
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    ConnectedClient c = new ConnectedClient(clientSocket);
                    clientProcessingPool.submit(c);
                }
            } catch (IOException e) {
                System.out.println("Unable to process client request");
                e.printStackTrace();
            }
        };
        Thread serverThread = new Thread(serverTask);
        serverThread.start();
    }
}
