package server;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;


public class Server {
    private List<ClientHandler> clients;
    private AuthService authService;

    public Server() {
        clients = new Vector<>();
        authService = new SimpleAuthService();
        ServerSocket server = null;
        Socket socket;

        final int PORT = 8189;

        try {
            server = new ServerSocket(PORT);
            System.out.println("Сервер запущен!");

            while (true) {
                socket = server.accept();
                System.out.println("Клиент подключился ");
                new ClientHandler(this, socket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void broadcastMsg(String msg){
        for (ClientHandler c:clients) {
            c.sendMsg(msg);
        }
    }

    public void privateMsg(String msg, String prvNick, ClientHandler sender){
        for (ClientHandler c:clients) {
            if (c.getNick().equals(prvNick)) {
                c.sendMsg(msg);
                if(!sender.getNick().equals(prvNick)) {
                    sender.sendMsg(msg);
                }
            }
        }
    }

//    public void privateMsg(ClientHandler sender, String receiver, String msg){
//
//        String message = String.format("[ %s ] private [ %3] : %s",
//                sender.getNick(), receiver, msg);
//        for (ClientHandler c:clients) {
//            if (c.getNick().equals(receiver)) {
//                c.sendMsg(message);
//                if(!sender.getNick().equals(receiver)) {
//                    sender.sendMsg(message);
//                }
//                return;
//            }
//        }
//
//    }

    public boolean getClientByLogin(String login){

        for (ClientHandler c:clients) {
            if (c.getLogin().equals(login)) {
                return true;
            }
        }
        return false;
    }

    public void subscribe(ClientHandler clientHandler){
        clients.add(clientHandler);
        broadcastClientList();
    }

    public void unsubscribe(ClientHandler clientHandler){
        clients.remove(clientHandler);
        broadcastClientList();
    }

    public AuthService getAuthService() {
        return authService;
    }
    private void broadcastClientList(){
        StringBuilder sb = new StringBuilder(("/clientlist "));

        for (ClientHandler c:clients) {
            sb.append(c.getNick()).append(" ");
        }
        String msg = sb.toString();
        for (ClientHandler c:clients) {
            c.sendMsg(msg);
        }
    }
}
