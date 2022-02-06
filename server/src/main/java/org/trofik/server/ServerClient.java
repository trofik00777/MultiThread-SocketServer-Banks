package org.trofik.server;

import org.trofik.banking_system.banks.*;
import org.trofik.banking_system.users.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
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
                            try {
                                user = new Admin(logPassw[0], logPassw[1]);
                            } catch (Exception e) {
                                e.printStackTrace();
                                sendMessage(outStream, "!stop@Sorry, bad connection or login/password is incorrect");
                                break;
                            }

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
                            try {
                                user = new Client(logPassw[0], logPassw[1]);
                            } catch (Exception e) {
                                e.printStackTrace();
                                sendMessage(outStream, "!stop@Sorry, bad connection or login/password is incorrect");
                                break;
                            }
                        }
                        printMainMenu(outStream, isAdmin);
                        break;
                    case "!enterForRegister":
                        String[] data = clientMessage[1].split("\\s+");
                        try {

                            if (isAdmin) {
                                user = new Admin(data[0], data[1], data[2], data[3]);
                            } else {
                                user = new Client(data[0], data[1], data[2], data[3]);
                            }
                            printMainMenu(outStream, isAdmin);
                        } catch (Exception e) {
                            e.printStackTrace();
                            sendMessage(outStream, "!stop@Sorry, bad connection or login is already used");
                        }
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
                                        sendMessage(outStream, "!createLBank@Write 'country loanInterestRate DaysForLoan name'");
                                    }
                                    break;
                                case 3:
                                    if (savingBank != null) {

                                        sendMessage(outStream, "!info@You already have 'Saving' bank");
                                    } else {
                                        sendMessage(outStream, "!createSBank@Write 'country saveInterestRate DaysForSave name'");
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
                                    sendMessage(outStream, "!takeLoan@Please write " +
                                            "'sumLoan currency{Rubles, Dollar, Euros} nameBank'");
                                    break;
                                case 3:
                                    sendMessage(outStream, "!createDeposit@Please write " +
                                            "'sumToSave currency{Rubles, Dollar, Euros} nameBank'");
                                    break;
                                case 4:
                                    sendMessage(outStream, "!makePayment@Please write " +
                                            "'sumToPay currency{Rubles, Dollar, Euros} nameBank'");
                                    break;
                                case 5:
                                    sendMessage(outStream, "!takeMoney@Please write 'nameBank'");
                                    break;
                                case 6:
                                    sendMessage(outStream, "!stop@Bye");
                                    break;

                            }
                        }
                        break;
                    case "!createDeposit":
                    case "!takeLoan":
                        String[] loan = clientMessage[1].split("\\s+", 3);
                        try {
                            LoanSaveInformation info;
                            if (clientMessage[0].equals("!takeLoan")) {
                                info = ((Client) user).takeLoan(loan[2],
                                        new Money(Float.parseFloat(loan[0]),
                                                Currency.valueOf(loan[1].toUpperCase())));
                            } else {
                                info = ((Client) user).giveMoneyForSaving(loan[2],
                                        new Money(Float.parseFloat(loan[0]),
                                                Currency.valueOf(loan[1].toUpperCase())));
                            }
                            assert info != null;
                            sendMessage(outStream, "!info@" + info.toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                            sendMessage(outStream, "!info@Sorry, bad connection or you have loan/save at this Bank...");
                        }
                        break;
                    case "!makePayment":
                    case "!takeMoney":
                        String[] pay = clientMessage[1].split("\\s+", 3);
                        try {
                            if (clientMessage[0].equals("!makePayment")) {
                                boolean info = ((Client) user).makePayment(pay[2],
                                        new Money(Float.parseFloat(pay[0]),
                                                Currency.valueOf(pay[1].toUpperCase())));
                                assert info;
                                sendMessage(outStream, "!info@Payment success");
                            } else {
                                Money info = ((Client) user).takeMoneyFromBank(pay[2]);

                                assert info != null;
                                sendMessage(outStream, "!info@" + info.toString());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            sendMessage(outStream, "!info@Sorry, bad connection or you have loan/save at this Bank...");
                        }
                        break;
                    case "!createLBank":
                    case "!createSBank":
                        String[] aboutBank = clientMessage[1].split("\\s+", 3);

                        try {
                            if (clientMessage[0].equals("!createLBank")) {
                                loanBank = new LoanBank(aboutBank[3], aboutBank[0], Float.parseFloat(aboutBank[1]),
                                        Long.parseLong(aboutBank[2]) * 86_400_000L);
                            } else {
                                savingBank = new SavingBank(aboutBank[3], aboutBank[0], Float.parseFloat(aboutBank[1]),
                                        Long.parseLong(aboutBank[2]) * 86_400_000L);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            sendMessage(outStream, "!info@Sorry, bad connection or you write incorrect data");
                        }
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
