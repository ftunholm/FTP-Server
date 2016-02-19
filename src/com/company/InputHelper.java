package com.company;

import com.company.passive.workers.PassiveFileTransferAsync;
import com.company.passive.workers.PassiveListAsync;

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
        if (client.isLoggedIn()) {
            if (input.startsWith("PASV")) {
                startPassiveConnection();
            }
            else if (input.startsWith("RETR")) {
                retreiveFile(input);
            }
            else if (input.startsWith("LIST")) {
                getList();
            }
            else if (input.startsWith("PWD")) {
                printWorkingDir();
            }
            else if (input.startsWith("CWD")) {
                changeWorkingDir(input);
            }
            else if (input.equals("CDUP")) {
                moveWorkingDirUp();
            }
            else if (input.equals("QUIT")) {
                quit();
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
                checkPassword(input);
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
    private void retreiveFile(String input) throws IOException {
        if (client.getPassiveConnection().getDataSocket().isConnected()) {
            String filename = input.replace("RETR ", "");
            File file = new File(client.getWorkingDir() + "/" + filename);
            if (!file.exists()) {
                client.write("550 Failed to open file.");
                return;
            }
            client.write("150 Opening BINARY mode data connection for " + filename + " (" + file.length() + " bytes).");
            PassiveFileTransferAsync pasvAsync = new PassiveFileTransferAsync(client, file.getAbsolutePath());
            pasvAsync.execute();
        }
        else {
            client.write("425 Use PORT or PASV first.");
        }
    }

    private void getList() throws IOException {
        if (client.getPassiveConnection().getDataSocket().isConnected()) {
            client.write("150 Here comes the directory listing.");
            PassiveListAsync pasvAsync = new PassiveListAsync(client);
            pasvAsync.execute();
        }
        else {
            client.write("425 Use PORT or PASV first.");
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