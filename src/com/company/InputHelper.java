package com.company;

import com.company.workers.PassiveConnection;
import com.company.workers.PassiveFileDownloadAsync;
import com.company.workers.PassiveFileUploadAsync;
import com.company.workers.PassiveListAsync;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

/**
 * Created by LogiX on 2016-02-19.
 */
public class InputHelper {
    private ConnectedClient client;
    public InputHelper(ConnectedClient client) {
        this.client = client;
    }

    public void processInput(String input) throws IOException {
        System.out.println(input);
        if (client.isLoggedIn()) {
            if (input.startsWith("PASV")) {
                startPassiveConnection();
            }
            else if (input.startsWith("RETR")) {
                retreiveFile(input);
            }
            else if (input.startsWith("STOR")) {
                storeFile(input);
            }
            else if (input.startsWith("DELE")) {
                deleteFile(input);
            }
            else if (input.startsWith("LIST")) {
                getList();
            }
            else if (input.startsWith("TYPE")) {
                type(input);
            }
            else if (input.startsWith("PWD")) {
                printWorkingDir();
            }
            else if (input.startsWith("CWD")) {
                changeWorkingDir(input);
            }
            else if (input.startsWith("MKD")) {
                createRemoteDir(input);
            }
            else if (input.equals("CDUP")) {
                moveWorkingDirUp();
            }
            else if (input.equals("QUIT")) {
                quit();
            }
            else if (input.startsWith("EPSV")) {
                client.write("500 Not supported.");
            }
            else if (input.startsWith("FEAT")) {
                client.write("500 Not supported.");
            }
            else if (input.startsWith("EPRT")) {
                client.write("500 Not supported.");
            }
            else {
                client.write("502 Command unknown or not implemented.");
            }
        }
        else {
            if (input.startsWith("USER")) {
                checkUsername(input);
            }
            else if (input.startsWith("PASS")) {
                //No password required, remove write and setIsLogged in and uncomment checkPassword() to user authentication
                client.write("230 Login successful.");
                client.setIsLoggedIn(true);
                //checkPassword(input);
            }
            else {
                client.write("530 please login with USER and PASS.");
            }
        }
    }

    private void checkUsername(String input) throws IOException {
        String user = input.replace("USER", "").trim();
        if (user.equals(FtpServer.USERNAME)) {
            client.setUsernameVerified(true);
            client.write("331 please specify the password");
        }
        else {
            client.setUsernameVerified(false);
            client.write("530 please login with USER and PASS.");
        }
    }
    private void checkPassword(String input) throws IOException {
        String user = input.replace("PASS", "").trim();
        if (user.equals(FtpServer.PASSWORD)) {
            if (client.isUsernameVerified()) {
                client.write("230 Login successful.");
                client.setIsLoggedIn(true);
            }
        }
        else {
            client.setUsernameVerified(false);
            client.write("530 please login with USER and PASS.");
        }
    }
    private void startPassiveConnection() throws IOException {
        ServerSocket socket = new ServerSocket(0);
        PassiveConnection pasv = new PassiveConnection(socket);
        pasv.execute();
        client.setPassiveConnection(pasv);

        int port = socket.getLocalPort();
        String[] addr = InetAddress.getLocalHost().getHostAddress().split("\\."); //split the ip address into parts
        String output = "227 Entering Passive Mode (" + addr[0] + "," + addr[1] + "," + addr[2] + "," + addr[3] + ","
                + (port >> 8) + ","+ (port & 0xFF) + ").";

        client.write(output);
    }
    private void type(String input) throws IOException {
        String type = input.replace("TYPE ", "");
        if (type.equals("I")) {
            client.write("200 Switching to Binary mode.");
        }
        else {
            client.write("500 Unrecognised TYPE command.");
        }
    }
    private void deleteFile(String input) throws IOException {
        String filename = input.replace("DELE ", "");
        File file = new File(client.getWorkingDir() + "/" + filename);
        if (!file.exists()) {
            client.write("550 File not found.");
            return;
        }
        boolean isDeleted = file.delete();
        if (isDeleted) {
            client.write("250 File deleted.");
        }
        else {
            client.write("550 Permission denied.");
        }
    }
    private void retreiveFile(String input) throws IOException {
        if (client.hasPassiveConnection()) {
            String filename = input.replace("RETR ", "");
            File file = new File(client.getWorkingDir() + "/" + filename);
            if (!file.exists()) {
                client.write("550 Failed to open file.");
                return;
            }
            client.write("150 Opening BINARY mode data connection for " + filename + " (" + file.length() + " bytes).");
            PassiveFileDownloadAsync pasvAsync = new PassiveFileDownloadAsync(client, file.getAbsolutePath());
            pasvAsync.execute();
        }
    }
    private void storeFile(String input) throws IOException {
        if (client.hasPassiveConnection()) {
            String filename = input.replace("STOR ", "");
            File file = new File(client.getWorkingDir() + "/" + filename);
            if (file.exists()) {
                client.write("553 Could not create file.");
                return;
            }
            client.write("150 Ok to send data.");
            PassiveFileUploadAsync pasvAsync = new PassiveFileUploadAsync(client, file.getAbsolutePath());
            pasvAsync.execute();
        }
    }
    private void getList() throws IOException {
        if (client.hasPassiveConnection()) {
            client.write("150 Here comes the directory listing.");
            PassiveListAsync pasvAsync = new PassiveListAsync(client);
            pasvAsync.execute();
        }
    }
    private void createRemoteDir(String input) throws IOException {
        String filename = input.replace("MKD ", "");
        File file = new File(client.getWorkingDir() + "/" + filename);
        if (file.exists()) {
            client.write("550 Directory already exists.");
            return;
        }
        boolean isCreated = file.mkdirs();
        if (isCreated) {
            client.write("250 Directory created successfully.");
        }
        else {
            client.write("550 Permission denied.");
        }
    }
    private void printWorkingDir() throws IOException {
        client.write("257 " + client.getWorkingDir().substring(client.getWorkingDir().indexOf("/shared"), client.getWorkingDir().length()));
    }
    private void changeWorkingDir(String input) throws IOException {
        String path = input.replace("CWD", "").trim();
        File newDir = new File(client.getWorkingDir() + path);
        if (!newDir.exists() || !newDir.getAbsolutePath().replace("\\", "/").contains("/shared") || path.contains("..")) {
            client.write("550 Failed to change directory.");
        }
        else {
            client.setWorkingDir(newDir.getAbsolutePath().replace("\\", "/"));
            client.write("250 Directory successfully changed.");
        }
    }
    private void moveWorkingDirUp() throws IOException {
        File newDir = new File(client.getWorkingDir().substring(0, client.getWorkingDir().lastIndexOf("/")));

        if (!newDir.exists() || !newDir.getAbsolutePath().replace("\\", "/").contains("/shared")) {
            client.write("550 Failed to change directory.");
        }
        else {
            client.setWorkingDir(newDir.getAbsolutePath().replace("\\", "/"));
            client.write("250 Directory successfully changed.");
        }
    }
    private void quit() throws IOException {
        client.disconnect();
    }
}