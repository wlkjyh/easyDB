/*
 * Copyright (c) 2024. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wlkjyy.Eloquent;

import lombok.Getter;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * @Author: wlkjyy <wlkjyy@vip.qq.com>
 * @Date: 2024/3/28
 * @Project: EasyDB
 */
public class DB {


    private final DataSource connect;


    public DB(DataSource connect) {
        this.connect = connect;
    }

    /***
     * 获取一个连接
     * @return
     * @throws SQLException
     */
    public Connection getConnect() throws SQLException {
        return this.connect.getConnection();
    }

    /***
     * 关闭连接
     * @param connection
     * @throws SQLException
     */
    public void closeConnect(Connection connection) throws SQLException {
        connection.close();
    }

    private String table = null;


    /***
     * Where子句
     */
    private ArrayList<String[]> where = new ArrayList<>();

    /***
     * orWhere子句
     */
    private ArrayList<String[]> orWhere = new ArrayList<>();


    /***
     * 允许的操作符
     */
    private final String[] operateList = new String[]{"=", ">", "<", ">=", "<=", "<>", "like", "in", "between", "not in", "not like"};

    /***
     * 是否删除
     */
    private boolean delete = false;

    /***
     * 更新数据参数
     */
    private HashMap<String, Object> update = new HashMap<>();

    /***
     * 插入数据参数
     */
    private HashMap<String, Object> insert = new HashMap<>();

    /***
     * 选择字段
     */
    private ArrayList<String> select = new ArrayList<>() {
        {
            add("*");
        }
    };

    /***
     * Limit限制
     */
    private Integer[] limit = null;

    /***
     * OrderBy
     */
    private ArrayList<String[]> orderBy = new ArrayList<>();


    /***
     * GroupBy
     */

    private ArrayList<String> groupBy = new ArrayList<>();

    /***
     * 编译后的sql
     */
    private String compileSql;

    /***
     * 绑定参数
     */
    private ArrayList<String> bind;

    public DB table(String table_name) {
        return new DB(this.connect).table_name(table_name);
    }

    private DB table_name(String tableName) {
        this.table = tableName;
        return this;
    }

    public DB where(String column, String operate, Object value) {

        if (!Arrays.asList(this.operateList).contains(operate)) {
            throw new RuntimeException("The operator is not allowed");
        }

        for (String[] item : this.where) {
            if (item[0].equals(column) && item[1].equals(operate) && item[2].equals(String.valueOf(value))) {
                return this;
            }
        }

        this.where.add(new String[]{column, operate, String.valueOf(value)});
        return this;
    }

    public DB where(String column, Object value) {
        return this.where(column, "=", value);
    }

    public DB orWhere(String column, String operate, Object value) {

        for (String[] item : this.orWhere) {
            if (item[0].equals(column) && item[1].equals(operate) && item[2].equals(String.valueOf(value))) {
                return this;
            }
        }

        this.orWhere.add(new String[]{column, operate, String.valueOf(value)});
        return this;
    }

    public DB orWhere(String column, Object value) {
        return this.orWhere(column, "=", value);
    }

    public DB select(String... column) {
        this.select = new ArrayList<>(Arrays.asList(column));
        return this;
    }


    public DB addSelect(String... column) {

        if (this.select.size() == 1 && this.select.getFirst().equals("*")) {
            this.select = new ArrayList<>();
        }

        this.select.addAll(Arrays.asList(column));
        return this;
    }

    public int delete() throws SQLException {
        this.delete = true;
        this.getSql();
        return new Query(this.getConnect(), this.compileSql, this.bind).execute();
    }

    /***
     * 修改数据
     * @param data
     * @return
     */
    public int update(HashMap<String, Object> data) throws SQLException {
        this.update = data;
        this.getSql();

        return new Query(this.getConnect(), this.compileSql, this.bind).execute();
    }

    /***
     * 插入数据
     * @param data
     * 相当于insert into kaxiao_config (vkey,value) values ('site_name','wlkjyy')
     * @return
     */
    public boolean insert(HashMap<String, Object> data) throws SQLException {
        this.insert = data;
        this.getSql();

        return new Query(this.getConnect(), this.compileSql, this.bind).execute() > 0;

    }

    private Builder Builder() {
        Builder builder = new Builder(this.table, this.where, this.orWhere, this.delete, this.update, this.insert, this.select, this.limit, this.orderBy, this.groupBy);
        builder.compileSql();
        return builder;

    }

    public String getSql() {
        Builder builder = this.Builder();
        this.compileSql = builder.getSql();
        this.bind = builder.getBind();
        return builder.getSql();
    }

    public ArrayList<HashMap<String, Object>> get() throws SQLException {

        Builder builder = this.Builder();

        ArrayList<String> bind = builder.getBind();
        String Sql = builder.getSql();

        return new Query(this.getConnect(), Sql, bind).get(false);


    }

    public HashMap<String, Object> first() throws SQLException {
        this.limit(1);
        Builder builder = this.Builder();
        ArrayList<String> bind = builder.getBind();
        String Sql = builder.getSql();

        ArrayList<HashMap<String, Object>> res = new Query(this.getConnect(), Sql, bind).get(true);
        if (!res.isEmpty()) {
            return res.getFirst();
        } else {
            return null;
        }
    }

    public HashMap<String, Object> find(String primaryKey, Object value) throws SQLException {

        this.where = new ArrayList<>();

        return this.where(primaryKey, value).first();
    }

    public HashMap<String, Object> find(Object value) throws SQLException {
        return this.find("id", value);
    }


    public Integer count() throws SQLException {
        this.select = new ArrayList<>();
        this.select.add("count(*) as count");
        HashMap<String, Object> res = this.first();
        if (res != null) {
            return Integer.parseInt(res.get("count").toString());
        } else {
            return 0;
        }
    }


    public Double max(String column) throws SQLException {

        this.select = new ArrayList<>();
        this.select.add("max(" + column + ") as max");
        HashMap<String, Object> res = this.first();
        if (res != null) {
            return Double.parseDouble(res.get("max").toString());
        } else {
            return 0.00;
        }

    }

    public Double min(String column) throws SQLException {

        this.select = new ArrayList<>();
        this.select.add("min(" + column + ") as min");
        HashMap<String, Object> res = this.first();
        if (res != null) {
            return Double.parseDouble(res.get("min").toString());
        } else {
            return 0.00;
        }
    }

    public Double sum(String column) throws SQLException {
        this.select = new ArrayList<>();
        this.select.add("sum(" + column + ") as sum");
        HashMap<String, Object> res = this.first();
        if (res != null) {
            return Double.parseDouble(res.get("sum").toString());
        } else {
            return 0.00;
        }
    }


    /***
     * 取平均
     */
    public Double avg(String column) throws SQLException {
        this.select = new ArrayList<>();
        this.select.add("avg(" + column + ") as avg");
        HashMap<String, Object> res = this.first();
        if (res != null) {
            return Double.parseDouble(res.get("avg").toString());
        } else {
            return 0.00;
        }
    }

    public DB whereBetween(String column, Object value1, Object value2) {
        this.where.add(new String[]{column, "between", String.valueOf(value1) + " and " + String.valueOf(value2), null});
        return this;
    }


    public DB orWhereBetween(String column, Object value1, Object value2) {
        this.orWhere.add(new String[]{column, "between", String.valueOf(value1) + " and " + String.valueOf(value2), null});
        return this;
    }

    public DB whereNotBetween(String column, Object value1, Object value2) {
        this.where.add(new String[]{column, "not between", String.valueOf(value1) + " and " + String.valueOf(value2), null});
        return this;
    }

    public DB orWhereNotBetween(String column, Object value1, Object value2) {
        this.orWhere.add(new String[]{column, "not between", String.valueOf(value1) + " and " + String.valueOf(value2), null});
        return this;
    }

    public DB md5Where(String column, Object value) {
        this.where.add(new String[]{column, "=", "md5(" + String.valueOf(value) + ")", null});
        return this;
    }

    public DB orMd5Where(String column, Object value) {
        this.orWhere.add(new String[]{column, "=", "md5(" + String.valueOf(value) + ")", null});
        return this;
    }

    public DB whereIn(String column, Object[] values) {
        StringBuilder in = new StringBuilder();
        for (Object value : values) {
            in.append(value).append(",");
        }
        in.deleteCharAt(in.length() - 1);
        this.where.add(new String[]{column, "in", "(" + in + ")", null});
        return this;
    }

    public DB orWhereIn(String column, Object[] values) {
        StringBuilder in = new StringBuilder();
        for (Object value : values) {
            in.append(value).append(",");
        }
        in.deleteCharAt(in.length() - 1);
        this.orWhere.add(new String[]{column, "in", "(" + in + ")", null});
        return this;
    }


    public boolean exists() throws SQLException {
        return this.count() > 0;
    }

    public boolean doesntExist() throws SQLException {
        return this.count() == 0;
    }

    private ArrayList<String> ObjectFormat(Object[] params) {
        ArrayList<String> res = new ArrayList<>();
        for (Object param : params) {
            res.add(String.valueOf(param));
        }
        return res;
    }

    public DB limit(int limit, int offset) {
        this.limit = new Integer[]{limit, offset};
        return this;
    }

    public DB limit(int limit) {
        this.limit = new Integer[]{limit, null};
        return this;
    }

    public DB orderBy(String column, String order) {
        this.orderBy.add(new String[]{column, order});
        return this;
    }


    public DB orderBy(String column) {
        return this.orderBy(column, "asc");
    }

    public DB latest(String column) {
        return this.orderBy(column, "desc");
    }

    public DB latest() {
        return this.orderBy("created_at", "desc");
    }

    public DB oldest(String column) {
        return this.orderBy(column, "asc");
    }

    public DB oldest() {
        return this.orderBy("created_at", "asc");
    }

    public DB inRandomOrder() {
        return this.orderBy("rand()");
    }

    public boolean truncate() {
        try {
            new Query(this.getConnect(), "alter table " + this.table + " auto_increment=1", new ArrayList<>()).execute();
            new Query(this.getConnect(), "alter table " + this.table + " auto_increment=1", new ArrayList<>()).execute();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public int increment(String column, int value) throws SQLException {
        this.update = new HashMap<>();
        this.update.put(column, column + "(EasyDBNoBind)+" + value);
        this.getSql();
        return new Query(this.getConnect(), this.compileSql, this.bind).execute();

    }

    public int decrement(String column, int value) throws SQLException {
        this.update = new HashMap<>();
        this.update.put(column, column + "(EasyDBNoBind)-" + value);
        this.getSql();
        return new Query(this.getConnect(), this.compileSql, this.bind).execute();
    }

    public int increment(String column) throws SQLException {
        return this.increment(column, 1);
    }

    public int decrement(String column) throws SQLException {
        return this.decrement(column, 1);
    }

    public int incrementEach(HashMap<String, Object> data) throws SQLException {
        this.update = new HashMap<>();
        for (String column : data.keySet()) {
            this.update.put(column, column + "(EasyDBNoBind)+" + data.get(column));
        }
        this.getSql();

        return new Query(this.getConnect(), this.compileSql, this.bind).execute();
    }

    public int decrementEach(HashMap<String, Object> data) throws SQLException {
        this.update = new HashMap<>();
        for (String column : data.keySet()) {
            this.update.put(column, column + "(EasyDBNoBind)-" + data.get(column));
        }
        this.getSql();

        return new Query(this.getConnect(), this.compileSql, this.bind).execute();
    }

    public DB distinct(String... column) {
        this.select = new ArrayList<>();
        StringBuilder select = new StringBuilder();
        select.append("distinct ");
        for (String item : column) {
            select.append(item).append(",");
        }
        select.deleteCharAt(select.length() - 1);
        this.select.add(select.toString());
        return this;
    }

    public DB addColumnCount(String column, String alias) {
        this.addSelect("count(" + column + ") as " + alias);
        return this;
    }

    public DB addColumnCount(String column) {
        return this.addColumnCount(column, "count");
    }

    public DB addColumnSum(String column, String alias) {
        this.addSelect("sum(" + column + ") as " + alias);
        return this;
    }

    public DB addColumnSum(String column) {
        return this.addColumnSum(column, "sum");
    }

    public DB addColumnMax(String column, String alias) {
        this.addSelect("max(" + column + ") as " + alias);
        return this;
    }

    public DB addColumnMax(String column) {
        return this.addColumnMax(column, "max");
    }

    public DB addColumnMin(String column, String alias) {
        this.addSelect("min(" + column + ") as " + alias);
        return this;
    }

    public DB addColumnMin(String column) {
        return this.addColumnMin(column, "min");
    }

    public DB addColumnAvg(String column, String alias) {
        this.addSelect("avg(" + column + ") as " + alias);
        return this;
    }

    public DB addColumnAvg(String column) {
        return this.addColumnAvg(column, "avg");
    }

    public DB groupBy(String... column) {
        if (this.groupBy.isEmpty()) {
            this.groupBy = new ArrayList<>(Arrays.asList(column));
        } else {
            this.groupBy.addAll(Arrays.asList(column));
        }
        return this;
    }

    public DB whereNull(String column) {

        this.where.add(new String[]{column, "=", "null"});

        return this;

    }

    public DB whereNotNull(String column) {
        this.where.add(new String[]{column, "!=", "null"});

        return this;
    }

    public DB whereColumn(String column, String operate, String column2) {

        if (!Arrays.asList(this.operateList).contains(operate)) {
            throw new RuntimeException("The operator is not allowed");
        }

        for (String[] item : this.where) {
            if (item[0].equals(column) && item[1].equals(operate) && item[2].equals(String.valueOf(column))) {
                return this;
            }
        }

        this.where.add(new String[]{column, operate, column2, null});
        return this;

    }

    public DB whereColumn(String column, String column2) {

        return this.whereColumn(column, "=", column2);

    }

    public DB orWhereColumn(String column, String operate, String column2) {

        if (!Arrays.asList(this.operateList).contains(operate)) {
            throw new RuntimeException("The operator is not allowed");
        }

        for (String[] item : this.orWhere) {
            if (item[0].equals(column) && item[1].equals(operate) && item[2].equals(String.valueOf(column))) {
                return this;
            }
        }

        this.orWhere.add(new String[]{column, operate, column2, null});
        return this;

    }

    public DB orWhereColumn(String column, String column2) {

        return this.orWhere(column, "=", column2);

    }

    public DB orderByDesc(String column) {

        return this.orderBy(column, "desc");
    }

    public DB orderByAsc(String column) {

        return this.orderBy(column, "asc");
    }

    public DB reorder() {

        this.orderBy = new ArrayList<>();

        return this;
    }

    public DB reorder(String column, String order) {
        this.reorder();
        this.orderBy.add(new String[]{column, order});
        return this;
    }

    private int execute(String sql, Object[] params) throws SQLException {
        return new Query(this.getConnect(), sql, this.ObjectFormat(params)).execute();
    }

    public int delete(String sql, Object[] params) throws SQLException {
        return this.execute(sql, params);
    }

    public int delete(String sql) throws SQLException {
        return this.execute(sql, new Object[]{});
    }

    public int update(String sql, Object[] params) throws SQLException {
        return this.execute(sql, params);
    }

    public int update(String sql) throws SQLException {
        return this.execute(sql, new Object[]{});
    }

    public ArrayList<HashMap<String, Object>> query(String sql, Object[] params) throws SQLException {
        return new Query(this.getConnect(), sql, this.ObjectFormat(params)).get(false);
    }

    public ArrayList<HashMap<String, Object>> query(String sql) throws SQLException {
        return new Query(this.getConnect(), sql, new ArrayList<>()).get(false);
    }


}
