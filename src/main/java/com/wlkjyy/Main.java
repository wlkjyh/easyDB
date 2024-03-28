/*
 * Copyright (c) 2024. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wlkjyy;

import com.wlkjyy.Capsule.Manager;
import com.wlkjyy.Eloquent.DB;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @Author: wlkjyy <wlkjyy@vip.qq.com>
 * @Date: 2024/3/26
 * @Project: Default (Template) Project
 */
public class Main {
    public static void main(String[] args) throws SQLException {

        Manager manager = new Manager();
        manager.addConnection(new HashMap<>(){
            {
                put("host", "localhost");
                put("port", 3306);
                put("username", "root");
                put("password", "123456");
                put("database", "jianzhan");
            }
        });

        DB db = manager.getEloquentInstance();

        System.out.println(
                db.update("update `users` set k = 1 where username = ?",new Object[]{
                        "wlkjyy"
                })
        );

    }
}