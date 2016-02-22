package com.company.workers;

import com.company.ConnectedClient;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.OutputStream;

/**
 * Created by LogiX on 2016-02-19.
 */
public class PassiveFileTransferAsync extends SwingWorker<Void, Void> {
    private String filename;
    private ConnectedClient client;
    private static final int BUFF_SIZE = 8*1024;

    public PassiveFileTransferAsync(ConnectedClient client, String filename) {
        this.client = client;
        this.filename = filename;
    }

    @Override
    protected Void doInBackground() throws Exception {
        FileInputStream in = new FileInputStream(filename);
        OutputStream out = client.getPassiveConnection().getDataSocket().getOutputStream();

        try {
            byte[] buff = new byte[BUFF_SIZE];
            int len;
            while ((len = in.read(buff)) != -1) {
                out.write(buff, 0, len);
            }
            out.flush();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            in.close();
            out.close();
            client.getPassiveConnection().getDataSocket().close();
            client.write("226 Transfer complete.");
        }
        return null;
    }
}
