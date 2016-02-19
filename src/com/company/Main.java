package com.company;

import java.io.File;

public class Main {

    public static void main(String[] args) {
        File sharedDir = new File("shared");
        if (!sharedDir.exists()) {
            sharedDir.mkdir();
        }
        new FtpServer();
    }
}
