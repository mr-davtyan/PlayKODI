package net.davtyan.playKODI;

import java.io.Serializable;

public class Host implements Serializable {
    String nickName;
    String host;
    String port;
    String login;
    String password;
    String color;
    int order;

    public Host(
            String nickName,
            String host,
            String port,
            String login,
            String password,
            String color,
            int order
    ) {
        this.nickName = nickName;
        this.host = host;
        this.port = port;
        this.login = login;
        this.password = password;
        this.color = color;
        this.order = order;
    }


}

