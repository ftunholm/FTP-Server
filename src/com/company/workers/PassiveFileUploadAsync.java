package com.company.workers;

import com.company.ConnectedClient;

import javax.swing.*;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Created by LogiX on 2016-02-24.
 */
public class PassiveFileUploadAsync extends SwingWorker<Void, Void> {

    private String filename;
    private ConnectedClient client;
    private static final int BUFF_SIZE = 8*1024;

    public PassiveFileUploadAsync(ConnectedClient client, String filename) {
        this.client = client;
        this.filename = filename;
    }

    @Override
    protected Void doInBackground() throws Exception {
        FileOutputStream out = new FileOutputStream(filename, false);
        InputStream in = client.getPassiveConnection().getDataSocket().getInputStream();

        try {
            byte[] buff = new byte[BUFF_SIZE];
            int len;
            while ((len = in.read(buff)) != -1) {
                out.write(buff, 0, len);
            }
            out.flush();
            client.write("226 Transfer complete.");
        }
        catch (Exception e) {
            System.out.println("Client has disconnected.");
        }
        finally {
            in.close();
            out.close();
            client.getPassiveConnection().getDataSocket().close();
        }
        return null;
    }
}
