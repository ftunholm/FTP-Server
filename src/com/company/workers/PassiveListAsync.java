package com.company.workers;

import com.company.ConnectedClient;
import javax.swing.*;
import java.io.File;
import java.io.OutputStreamWriter;
import java.util.Date;

/**
 * Created by LogiX on 2016-02-19.
 */
public class PassiveListAsync extends SwingWorker<Void, Void> {
    private ConnectedClient client;

    public PassiveListAsync(ConnectedClient client) {
        this.client = client;
    }

    @Override
    protected Void doInBackground() throws Exception {
        OutputStreamWriter out = new OutputStreamWriter(client.getPassiveConnection().getDataSocket().getOutputStream());
        File folder = new File(client.getWorkingDir().replace("\\", "/"));
        try {
            File[] listOfFiles = folder.listFiles();
            for (int i = 0; i < listOfFiles.length; i++) {
                String fileinfo = "";
                fileinfo += listOfFiles[i].canRead() ? "r" : "-";
                fileinfo += listOfFiles[i].canWrite() ? "w" : "-";
                fileinfo += listOfFiles[i].canExecute() ? "x\t" : "-\t";
                Date d = new Date(listOfFiles[i].lastModified());
                fileinfo += listOfFiles[i].length() + "\t" + d + " " + listOfFiles[i].getName();

                out.write(fileinfo + "\n\r");
            }
            out.flush();
            client.write("226 Directory send OK.");
        }
        catch (Exception e) {
            System.out.println("Client has disconnected.");
        } finally {
            out.close();
            client.getPassiveConnection().getDataSocket().close();
        }
        return null;
    }
}
