package net.davtyan.playKODI;

import java.io.Serializable;

public class Host  implements Serializable {
    private String nickName;
    private String host;
    private String port;
    private String login;
    private String password;
    private String color;
    private int order;

    Host(String nickName,
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

