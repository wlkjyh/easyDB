package com.wlkjyy;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @Author: wlkjyy <wlkjyy@vip.qq.com>
 * @Date: 2024/3/27
 * @Project: EasyDB
 */
public class Query {

    /***
     * 编译后的SQL
     */
    private String sql;

    /***
     * 绑定参数
     */
    private ArrayList<String> bind;


    /***
     * 数据库连接
     */
    private Connection connection;

    public Query(Connection connection, String sql, ArrayList<String> bind) {
        this.connection = connection;
        this.sql = sql;
        this.bind = bind;
    }

    /***
     * 执行SQL查询
     * @return
     */
    private ResultSet statement() {
        try {
            PreparedStatement stmt = this.connection.prepareStatement(sql);
            for (int i = 0; i < bind.size(); i++) {
                stmt.setString(i + 1, bind.get(i));
            }
            return stmt.executeQuery();
        } catch (SQLException e) {
            return null;
        }
    }

    public boolean execute() {
        try {
            PreparedStatement stmt = this.connection.prepareStatement(sql);
            for (int i = 0; i < bind.size(); i++) {
                stmt.setString(i + 1, bind.get(i));
            }
//            判断是否执行成功
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    /***
     * 查询一条数据
     * @param first 是否只查询一条数据
     * @return ArrayList<HashMap < String, Object>>
     */
    public ArrayList<HashMap<String, Object>> get(boolean first) {
        try {
            ResultSet resultSet = this.statement();
            ResultSetMetaData metaData = null;
            if (resultSet != null) {
                metaData = resultSet.getMetaData();
            }
            int columnCount = 0;
            if (metaData != null) {
                columnCount = metaData.getColumnCount();
            }
            ArrayList<HashMap<String, Object>> rows = new ArrayList<>();
            if (resultSet != null) {
                while (resultSet.next()) {
                    HashMap<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        row.put(metaData.getColumnName(i), resultSet.getObject(i));
                    }
                    rows.add(row);
                    if (first) {
                        return rows;
                    }
                }
            }
            if (first) {
                return new ArrayList<>();
            }
            return rows;
        } catch (SQLException e) {
            return new ArrayList<>();
        }
    }

}
