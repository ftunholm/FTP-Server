package com.company;

import java.io.*;
import java.net.Socket;

/**
 * Created by LogiX on 2016-02-19.
 */
public class ConnectedClient implements Runnable {
    private Socket clientSocket;
    private OutputStreamWriter out;
    private boolean isLoggedIn = false;
    private boolean usernameVerified = false;
    private PassiveConnection passiveConnection;
    public String workingDir;

    public ConnectedClient(Socket clientSocket) throws IOException {
        this.workingDir = (System.getProperty("user.dir") + "/shared").replace("\\", "/");
        this.out = new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8");
        this.clientSocket = clientSocket;
        write("220 (elHeffeFTPPro+++ By LanfeaR - 2k)");
    }

    public void write(String s) throws IOException {
        //out.write(s);
        out.write(s + "\n\r");
        out.flush();
    }

    @Override
    public void run() {
        BufferedReader in = null;
        try {
            InputHelper helper = new InputHelper(this);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
            String line;
            while ((line = in.readLine()) != null) {
                helper.processInput(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public boolean isLoggedIn() {
        return this.isLoggedIn;
    }
    public void setIsLoggedIn(boolean b) {
        this.isLoggedIn = b;
    }
    public boolean isUsernameVerified() {
        return usernameVerified;
    }
    public void setUsernameVerified(boolean usernameVerified) {
        this.usernameVerified = usernameVerified;
    }
    public PassiveConnection getPassiveConnection() {
        return passiveConnection;
    }
    public void setPassiveConnection(PassiveConnection passiveConnection) {
        this.passiveConnection = passiveConnection;
    }
    public String getWorkingDir() {
        return workingDir;
    }
    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir.replace("\\", "");
    }
}
