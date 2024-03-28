/*
 * Copyright (c) 2024. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wlkjyy.Capsule;

import com.wlkjyy.Connectors.MySqlConnector;
import com.wlkjyy.Eloquent.DB;
import lombok.Getter;

import javax.sql.DataSource;
import java.util.HashMap;

/**
 * @Author: wlkjyy <wlkjyy@vip.qq.com>
 * @Date: 2024/3/28
 * @Project: EasyDB
 */
public class Manager {

    private String host, username, password, database;
    private Integer port;

    /***
     * 创建一个连接
     * @param connectionConfig
     */
    public void addConnection(HashMap<String, Object> connectionConfig) {

        int port = Integer.parseInt(connectionConfig.get("port").toString());
        String host = connectionConfig.get("host").toString();
        String username = connectionConfig.get("username").toString();
        String password = connectionConfig.get("password").toString();
        String database = connectionConfig.get("database").toString();

        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.database = database;


    }

    @Getter
    private DataSource connectDataSource = null;


    /***
     * 获取数据库实例
     * @return
     */
    public DB getEloquentInstance() {

        /***
         * MySQL连接池
         */
        this.connectDataSource = new MySqlConnector().connect(host, username, password, database, port);
        return new DB(this.getConnectDataSource());

    }

}
