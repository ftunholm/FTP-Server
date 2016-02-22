package com.company.workers;

import com.company.ConnectedClient;
import javax.swing.*;
import java.io.File;
import java.io.OutputStreamWriter;

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
                System.out.println(listOfFiles[i].length() + " " + listOfFiles[i].getName() + "\n");
                out.write(listOfFiles[i].length() + " " + listOfFiles[i].getName() + "\n");
            }
            out.flush();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            out.close();
            client.getPassiveConnection().getDataSocket().close();
            client.write("226 Directory send OK.");
        }
        return null;
    }
}
