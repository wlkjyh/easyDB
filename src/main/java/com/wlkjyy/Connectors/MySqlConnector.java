/*
 * Copyright (c) 2024. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wlkjyy.Connectors;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.*;

/**
 * @Author: wlkjyy <wlkjyy@vip.qq.com>
 * @Date: 2024/3/28
 * @Project: EasyDB
 */
public class MySqlConnector {

    protected String driver = "com.mysql.cj.jdbc.Driver";

    /***
     * 创建一个连接，返回一个连接池
     * @param host
     * @param username
     * @param password
     * @param database
     * @param port
     * @return
     */
    public DataSource connect(String host, String username, String password, String database, int port) {

        String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC";


        HikariConfig config = new HikariConfig();
//        设置JDBC连接URL
        config.setJdbcUrl(jdbcUrl);
//        设置用户名
        config.setUsername(username);
//        设置密码
        config.setPassword(password);
//        设置驱动名称
        config.setDriverClassName(driver);
//        设置连接超时时间：1000ms
        config.setConnectionTimeout(1000);
//        设置空闲超时时间：60000ms = 60s
        config.setIdleTimeout(60000);
//        设置最大连接数：10
        config.setMaximumPoolSize(10);

        return new HikariDataSource(config);
    }


}
