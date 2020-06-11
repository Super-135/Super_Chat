package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private String nick;
    private String login;
    private String prvNick = "";
    private String prvMsg = "";
    private boolean loginEnabled = false;

    public ClientHandler(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    //цикл аутентификации
                    // если пользователь зависнет на этапе подключения или регистрации
                    socket.setSoTimeout(5000);
                    while (true) {
                        String str = in.readUTF();

                        if (str.startsWith("/reg ")) {
                            String[] token = str.split(" ");
                            if (token.length < 4) {
                                continue;
                            }
                            System.out.println(token[1]+token[2]+token[3]);
                            boolean succeed = server.
                                    getAuthService().
                                    registration(token[1],token[2],token[3]);
                            if (succeed){
                                sendMsg("Регистрация прошла успешно");
                            }else {
                                sendMsg("Регистрация не удалась, возможно логин уже занят");
                            }
                        }

                        if (str.startsWith("/auth ")) {
                            String[] token = str.split(" ");

                            if (token.length < 3) {
                                continue;
                            }

                            String newNick = server.getAuthService()
                                    .getNicknameByLoginAndPassword(token[1], token[2]);


                            loginEnabled = server.getClientByLogin(token[1]);

                            if (loginEnabled){
                                sendMsg("Клиент уже подключен.");
                            } else {
                                if (newNick != null) {
                                    sendMsg("/authok " + newNick);
                                    nick = newNick;
                                    login = token[1];
                                    server.subscribe(this);
                                    System.out.println("Клиент: " + nick + " подключился");
                                    break;
                                } else {
                                    sendMsg("Неверный логин / пароль");
                                }
                            }
                        }
                    }

                    socket.setSoTimeout(0);

                    //цикл работы
                    while (true) {
                        String str = in.readUTF();

                        if (str.equals("/end")) {
                            sendMsg("/end");
                            break;
                        }
                        if (str.startsWith("/w ")){
                            String[] prv = str.split(" ");

                            System.out.println(str);
                            if (prv.length < 2) {
                                continue;
                            }
                            prvNick = prv[1];
                            prvMsg = prv[2];
                        }
                        if (prvNick != ""){
                            server.privateMsg(nick + ": " + prvMsg, prvNick,this);
                            prvNick = "";
                        }else{
                            server.broadcastMsg(nick + ": " + str);
                        }
                    }


                }

                catch (SocketTimeoutException e){
                    e.printStackTrace();
                    sendMsg("/end");
                }
                catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    server.unsubscribe(this);
                    System.out.println("Клиент отключился");
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNick() {
        return nick;
    }

    public String getLogin() {
        return login;
    }
}
