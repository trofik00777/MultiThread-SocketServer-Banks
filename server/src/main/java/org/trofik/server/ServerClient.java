package org.trofik.server;

import org.trofik.banking_system.banks.LoanBank;
import org.trofik.banking_system.banks.SavingBank;
import org.trofik.banking_system.users.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class ServerClient extends Thread {
    Socket serverClient;
    int clientNo;

    ServerClient(Socket serverClient, int clientNo){
        this.serverClient = serverClient;
        this.clientNo = clientNo;
    }

    @Override
    public void run(){
        try (DataInputStream inStream = new DataInputStream(serverClient.getInputStream());
             DataOutputStream outStream = new DataOutputStream(serverClient.getOutputStream())) {

            String[] clientMessage;
            Admin admin;
            Client client;
            LoanBank loanBank;
            SavingBank savingBank;
            String saveMode = "";
            boolean isAdmin;

            outStream.writeUTF("!welcome@####################\n" +
                    "##### Welcome ######\n" +
                    "## Banking System ##\n" +
                    "####################\n" +
                    "### Please write ###\n" +
                    "# 'Login' or 'Reg' #\n" +
                    "####################\n" +
                    "####################");
            outStream.flush();

            while (true) {
                clientMessage = inStream.readUTF().split("@");
                switch (clientMessage[0]) {
                    case "!enterMode":
                        saveMode = clientMessage[1];
                        outStream.writeUTF("!enterAdminUser@Please write how you want to enter to system: 'Admin' or 'Client'");
                        outStream.flush();
                        break;
                    case "!enterAdminUser":
                        isAdmin = clientMessage[1].equals("admin");
                        if (saveMode.equals("login")) {
                            outStream.writeUTF("!enterLoginPassword@Please enter 'login password' with space:");
                            outStream.flush();
                        } else {
                            outStream.writeUTF("!enterForRegister@Please enter 'name surname login password'");
                            outStream.flush();
                        }
                        break;
                }
            }

//            serverClient.close();
        } catch (Exception ex) {
            System.out.println(ex);
        } finally {
            System.out.println("Client -" + clientNo + " exit!! ");
        }
    }
}
