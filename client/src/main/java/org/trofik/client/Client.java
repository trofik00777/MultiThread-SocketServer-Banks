package org.trofik.client;

import java.net.*;
import java.io.*;
import java.security.spec.RSAOtherPrimeInfo;
import java.util.Locale;
import java.util.Map;

public class Client {
    static int PORT = 8080;
    static String HOST = "127.0.0.1";

    static Map<String, String> CONVERT_TO_ANSWER_LABEL = Map.ofEntries(
            Map.entry("!welcome", "!enterMode"),
            Map.entry("!enterAdminUser", "!enterAdminUser"),
            Map.entry("!enterLoginPassword", "!enterLoginPassword"),
            Map.entry("!enterForRegister", "!enterForRegister"),
            Map.entry("!menu", "!menuMode"),
            Map.entry("!info", "!menuMode"),
            Map.entry("!createBank", "!menuMode"),
            Map.entry("!takeLoan", "!takeLoan"),
            Map.entry("!createDeposit", "!createDeposit"),
            Map.entry("!makePayment", "!makePayment"),
            Map.entry("!takeMoney", "!takeMoney"),
            Map.entry("!createLBank", "!createLBank"),
            Map.entry("!createSBank", "!createSBank"),
            Map.entry("!addCurr", "!addCurr")
    );

    public static void main(String[] args) {
        try (Socket socket=new Socket(HOST, PORT);
             DataInputStream inStream = new DataInputStream(socket.getInputStream());
             DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
             BufferedReader cmdReader = new BufferedReader(new InputStreamReader(System.in))) {

            String[] serverMessage;

            while (true) {
                serverMessage = inStream.readUTF().split("@");
                switch (serverMessage[0]) {
//                    case "!repeat":
//                        System.out.println();
                    case "!welcome":
                    case "!enterAdminUser":
                    case "!enterLoginPassword":
                    case "!enterForRegister":
                    case "!menu":
                    case "!info":
                    case "!createBank":
                    case "!takeLoan":
                    case "!createDeposit":
                    case "!makePayment":
                    case "!takeMoney":
                    case "!createLBank":
                    case "!createSBank":
                    case "!addCurr":
                        System.out.println(serverMessage[1]);
                        String clientAnswer = cmdReader.readLine().strip();
                        outStream.writeUTF(CONVERT_TO_ANSWER_LABEL.get(serverMessage[0]) + "@" + clientAnswer);
                        outStream.flush();
                        break;
                    case "!stop":
                        System.out.println(serverMessage[1]);
                        return;
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
