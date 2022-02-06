package org.trofik.server;

import java.net.*;

public class MultiThreadServer {
    static int PORT = 8080;

    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(PORT)) {
            int userNo = 0;

            System.out.println("Start Server...");
            while (true) {
                userNo++;

                Socket serverClient = server.accept();
                System.out.println("A user with a number: <" + userNo + "> has connected to the server");

                ServerClient client = new ServerClient(serverClient, userNo);
                client.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}