package org.trofik.server;

import org.trofik.banking_system.banks.*;
import org.trofik.banking_system.users.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Locale;
import java.util.Map;

public class ServerClient extends Thread {
    Socket serverClient;
    int clientNo;

    Map<Integer, String> ADMIN_MENU = Map.of(
            1, "Get info about your banks",
            2, "Create new 'Loan' bank",
            3, "Create new 'Saving' bank",
            4, "Exit"
    );

    Map<Integer, String> CLIENT_MENU = Map.of(
            1, "Get info about your 'Loans' or 'Saves'",
            2, "Take 'Loan'",
            3, "Create a deposit in 'Saving' bank",
            4, "Make payment (for 'Loan')",
            5, "Take money from 'Saving' bank",
            6, "Exit"
    );

    ServerClient(Socket serverClient, int clientNo){
        this.serverClient = serverClient;
        this.clientNo = clientNo;
    }

    @Override
    public void run(){
        try (DataInputStream inStream = new DataInputStream(serverClient.getInputStream());
             DataOutputStream outStream = new DataOutputStream(serverClient.getOutputStream())) {

            String[] clientMessage;
            User user = null;
            LoanBank loanBank = null;
            SavingBank savingBank = null;
            String saveMode = "";
            boolean isAdmin = false;

            outStream.writeUTF("!welcome@####################\n" +
                    "##### Welcome! #####\n" +
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
                        saveMode = clientMessage[1].toLowerCase();
                        sendMessage(outStream, "!enterAdminUser@Please write how you want to enter to system: 'Admin' or 'Client'");
                        break;
                    case "!enterAdminUser":
                        isAdmin = clientMessage[1].equalsIgnoreCase("admin");
                        if (saveMode.equals("login")) {
                            sendMessage(outStream, "!enterLoginPassword@Please enter 'login password' with space:");
                        } else {
                            sendMessage(outStream, "!enterForRegister@Please enter 'name surname login password'");
                        }
                        break;
                    case "!enterLoginPassword":
                        String[] logPassw = clientMessage[1].split("\\s+");
                        if (isAdmin) {
                            user = new Admin(logPassw[0], logPassw[1]);

                            try {
                                loanBank = new LoanBank((Admin) user);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            try {
                                savingBank = new SavingBank((Admin) user);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            user = new Client(logPassw[0], logPassw[1]);
                        }
                        printMainMenu(outStream, isAdmin);
                        break;
                    case "!enterForRegister":
                        String[] data = clientMessage[1].split("\\s+");

                        if (isAdmin) {
                            user = new Admin(data[0], data[1], data[2], data[3]);
                        } else {
                            user = new Client(data[0], data[1], data[2], data[3]);
                        }
                        printMainMenu(outStream, isAdmin);
                        break;
                    case "!menuMode":
                        int modeNo = Integer.parseInt(clientMessage[1]);

                        if (isAdmin) {
                            switch (modeNo) {
                                case 1:
//                                    System.out.println(user.getInfo());
                                    sendMessage(outStream, "!info@" + user.getInfo());
                                    break;
                                case 2:
                                    if (loanBank != null) {
                                        sendMessage(outStream, "!info@You already have 'Loan' bank");
                                    } else {
                                        sendMessage(outStream, "!createLBank@Write 'name country loanInterestRate'");
                                    }
                                    break;
                                case 3:
                                    if (savingBank != null) {

                                        sendMessage(outStream, "!info@You already have 'Saving' bank");
                                    } else {
                                        sendMessage(outStream, "!createSBank@Write 'name country saveInterestRate'");
                                    }
                                    break;
                                case 4:
                                    sendMessage(outStream, "!stop@Bye");
                                    break;
                            }
                        } else {
                            switch (modeNo) {
                                case 1:
                                    sendMessage(outStream, "!info@" + user.getInfo());
                                    break;
                                case 2:
                                    sendMessage(outStream, "!takeLoan@Please write 'sumLoan currency{R, U, E}'");
                                    break;
                            }
                        }
                        break;
                }
            }

//            serverClient.close();
        } catch (Exception e) {
            e.printStackTrace();
//            System.out.println(e);
        } finally {
            System.out.println("Client -" + clientNo + " exit!! ");
        }
    }

    private void printMainMenu(DataOutputStream outStream, boolean isAdmin) throws IOException {
        StringBuilder menu = new StringBuilder();
        String head = "##################### MENU #####################";
        menu.append("!menu@").append(head).append("\n");
        menu.append("#").append(" ".repeat(head.length() - 2)).append("#\n");
        Map<Integer, String> menuType = isAdmin ? ADMIN_MENU : CLIENT_MENU;
        for (int i = 1; i < menuType.keySet().size() + 1; i++) {
            menu.append("# ").append(i).append(". ").append(menuType.get(i))
                    .append(" ".repeat(head.length() - 6 - menuType.get(i).length())).append("#\n");


        }
        menu.append("#").append(" ".repeat(head.length() - 2)).append("#\n");
        menu.append("# Please, choose a number").append(" ".repeat(head.length() - 26)).append("#\n");

        menu.append("#".repeat(head.length()));

        sendMessage(outStream, menu.toString());

    }

    private void sendMessage(DataOutputStream outStream, String message) throws IOException {
        outStream.writeUTF(message);
        outStream.flush();
    }
}
