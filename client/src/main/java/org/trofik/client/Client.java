package org.trofik.client;

import java.net.*;
import java.io.*;
import java.security.spec.RSAOtherPrimeInfo;
import java.util.Locale;

public class Client {
    static int PORT = 8080;
    static String HOST = "127.0.0.1";

    public static void main(String[] args) {
        try (Socket socket=new Socket(HOST, PORT);
             DataInputStream inStream = new DataInputStream(socket.getInputStream());
             DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
             BufferedReader cmdReader = new BufferedReader(new InputStreamReader(System.in))) {

            String[] serverMessage;

            while (true) {
                serverMessage = inStream.readUTF().split("@");
                switch (serverMessage[0]) {
                    case "!welcome":
                        System.out.println(serverMessage[1]);
                        String mode = cmdReader.readLine().strip().toLowerCase(Locale.ROOT);
                        outStream.writeUTF("!enterMode@" + mode);
                        outStream.flush();
                        break;
                    case "!enterAdminUser":
                        System.out.println(serverMessage[1]);
                        String adminClient = cmdReader.readLine().strip().toLowerCase();
                        outStream.writeUTF("!enterAdminUser@" + adminClient);
                        break;
                }

//                System.out.println("Enter number :");
//                clientMessage = cmdReader.readLine();
//                outStream.writeUTF(clientMessage);
//                outStream.flush();
            }
        }catch(Exception e){
            System.out.println(e);
        }
    }
}
