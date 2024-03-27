package com.wlkjyy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * @Author: wlkjyy <wlkjyy@vip.qq.com>
 * @Date: 2024/3/26
 * @Project: EasyDB
 */
public class DB {
    /***
     * JDBC驱动
     */
    protected String driver = "com.mysql.cj.jdbc.Driver";

    /***
     * 数据库连接
     */
    private Connection connection;

    /***
     * 表名
     */
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
     * 编译后的sql
     */
    private String compileSql;

    /***
     * 绑定参数
     */
    private ArrayList<String> bind;


    /***
     * 数据库名称
     */
    protected String DB_DATABASE = "jianzhan";

    /***
     * 数据库用户名
     */

    protected String DB_USERNAME = "root";

    /***
     * 数据库密码
     */

    protected String DB_PASSWORD = "123456";

    /***
     * 数据库HOST
     */

    protected String DB_HOST = "localhost";

    /***
     * 数据库端口
     */

    protected Integer DB_PORT = 3306;


    public DB() {

    }

    /***
     * 创建数据库连接
     * @param host
     * @param username
     * @param password
     * @param database
     * @param port
     * @return
     */
    private Connection create_connect(String host, String username, String password, String database, Integer port) {

        String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC";

        try {
            Class.forName(this.driver);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("找不到驱动类");
        }

        try {
            return DriverManager.getConnection(jdbcUrl, username, password);
        } catch (SQLException e) {
            throw new RuntimeException("数据库连接失败");
        }
    }

    /***
     * 检查连接
     */
    private void checkConnection() {
        if (this.connection == null) {
            throw new RuntimeException("数据库未初始化之前不能执行操作。");
        }
    }

    /***
     * 获取JDBC连接
     * @return
     */
    public Connection getConnection() {
        checkConnection();
        return this.connection;
    }

    public void create_connect() {
        this.connection = this.create_connect(this.DB_HOST, this.DB_USERNAME, this.DB_PASSWORD, this.DB_DATABASE, this.DB_PORT);
    }

    /***
     * 指定表名
     * @param table_name
     * @return
     */
    public static DB table(String table_name) {

        DB instance = new DB();
        instance.table = table_name;
        instance.connection = instance.create_connect(instance.DB_HOST, instance.DB_USERNAME, instance.DB_PASSWORD, instance.DB_DATABASE, instance.DB_PORT);

        return instance;
    }

    /***
     * where子句
     * @param column 字段名称
     * @param operate 操作
     * @param value 值
     * @return
     */
    public DB where(String column, String operate, Object value) {

        // 判断操作符是否被允许
        if (!Arrays.asList(this.operateList).contains(operate)) {
            throw new RuntimeException("操作符不合法");
        }

//        重复检查
        for (String[] item : this.where) {
            if (item[0].equals(column) && item[1].equals(operate) && item[2].equals(String.valueOf(value))) {
                return this;
            }
        }

        this.where.add(new String[]{column, operate, String.valueOf(value)});
        return this;
    }

    /***
     * where子句
     * @param column 字段名称
     * @param value 值
     * 如果没有指定操作符，默认为=
     * @return
     */
    public DB where(String column, Object value) {
        return this.where(column, "=", value);
    }

    /***
     * or where子句
     * @param column
     * @param operate
     * @param value
     * 用法：db.table("kaxiao_config").where("vkey","site_name").orWhere("vkey","site_name");
     * @return
     */
    public DB orWhere(String column, String operate, Object value) {

//        重复检查
        for (String[] item : this.orWhere) {
            if (item[0].equals(column) && item[1].equals(operate) && item[2].equals(String.valueOf(value))) {
                return this;
            }
        }

        this.orWhere.add(new String[]{column, operate, String.valueOf(value)});
        return this;
    }

    /***
     * orWhere子句
     * @param column
     * @param value
     * @return
     */
    public DB orWhere(String column, Object value) {
        return this.orWhere(column, "=", value);
    }

    /***
     * 选择字段
     * @param column
     * @return
     */
    public DB select(String... column) {
        this.select = new ArrayList<>(Arrays.asList(column));
        return this;
    }


    /***
     * 添加选择字段
     * @param column
     * @return
     */
    public DB addSelect(String... column) {
        this.select.addAll(Arrays.asList(column));
        return this;
    }

    /***
     * 设置为删除
     */
    public int delete() {
        this.delete = true;
        this.getSql();
        return this.execute(this.compileSql, this.bind.toArray());
    }

    /***
     * 修改数据
     * @param data
     * @return
     */
    public int update(HashMap<String, Object> data) {
        this.update = data;
        this.getSql();

        return this.execute(this.compileSql, this.bind.toArray());
    }

    /***
     * 插入数据
     * @param data
     * 相当于insert into kaxiao_config (vkey,value) values ('site_name','wlkjyy')
     * @return
     */
    public boolean insert(HashMap<String, Object> data) {
        this.insert = data;
        this.getSql();

        return this.execute(this.compileSql, this.bind.toArray()) > 0;

    }

    private Builder Builder() {
        Builder builder = new Builder(this.table, this.where, this.orWhere, this.delete, this.update, this.insert, this.select, this.limit, this.orderBy);
        builder.compileSql();
        return builder;

    }

    public String getSql() {
        Builder builder = this.Builder();
        this.compileSql = builder.getSql();
        this.bind = builder.getBind();
        return builder.getSql();
    }

    /***
     * 执行查询，返回结果
     * 用法：db.table("kaxiao_config").get();
     * @return
     */
    public ArrayList<HashMap<String, Object>> get() {
        this.checkConnection();

        Builder builder = this.Builder();

        ArrayList<String> bind = builder.getBind();
        String Sql = builder.getSql();

//        System.out.println(Sql);

//        执行查询
//        return new Query(this.getConnection(), Sql, bind).get(false);
        return new Query(this.getConnection(), Sql, bind).get(false);


    }

    public HashMap<String, Object> first() {
        this.checkConnection();

        this.limit(1);

        Builder builder = this.Builder();
        ArrayList<String> bind = builder.getBind();
        String Sql = builder.getSql();

//        System.out.println(Sql);

        ArrayList<HashMap<String, Object>> res = new Query(this.getConnection(), Sql, bind).get(true);
        if (!res.isEmpty()) {
            return res.getFirst();
        } else {
            return null;
        }
    }

    /***
     * 通过主键查找一条数据
     * @param primaryKey
     * @param value
     * @return
     */
    public HashMap<String, Object> find(String primaryKey, Object value) {
        this.checkConnection();

        this.where = new ArrayList<>();

        return this.where(primaryKey, value).first();
    }

    /***
     * 通过主键查找一条数据，主键默认为id
     * @param value
     * @return
     */
    public HashMap<String, Object> find(Object value) {
        return this.find("id", value);
    }

    /***
     * 执行原生查询
     * @param sql
     * @return
     */
    public int statement(String sql) {
        return this.execute(sql);
    }


    private int execute(String sql, Object[] params) {
        try {
            this.checkConnection();
            PreparedStatement stmt = this.getConnection().prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
//            stmt.execute();
//
//            return true;
//            获取受影响的行数
            return stmt.executeUpdate();

        } catch (SQLException e) {
            return 0;
        }
    }

    private int execute(String sql) {
        return this.execute(sql, new Object[]{});
    }

    /**
     * 获取条目数量
     *
     * @return
     */
    public Integer count() {
//        return this.get().size();
        this.select = new ArrayList<>();
        this.select.add("count(*) as count");
        HashMap<String, Object> res = this.first();
        if (res != null) {
            return Integer.parseInt(res.get("count").toString());
        } else {
            return 0;
        }
    }

    /***
     * 取最大值
     * @param column
     * @return
     */
    public Double max(String column) {

        this.select = new ArrayList<>();
        this.select.add("max(" + column + ") as max");
        HashMap<String, Object> res = this.first();
        if (res != null) {
            return Double.parseDouble(res.get("max").toString());
        } else {
            return 0.00;
        }

    }

    /***
     * 取最小值
     * @param column
     * @return
     */
    public Double min(String column) {

        this.select = new ArrayList<>();
        this.select.add("min(" + column + ") as min");
        HashMap<String, Object> res = this.first();
        if (res != null) {
            return Double.parseDouble(res.get("min").toString());
        } else {
            return 0.00;
        }
    }

    /***
     * 求和
     * @param column
     * @return
     */
    public Double sum(String column) {
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
    public Double avg(String column) {
        this.select = new ArrayList<>();
        this.select.add("avg(" + column + ") as avg");
        HashMap<String, Object> res = this.first();
        if (res != null) {
            return Double.parseDouble(res.get("avg").toString());
        } else {
            return 0.00;
        }
    }

    /***
     * between语句
     * @param column
     * @param value1
     * @param value2
     * @return
     */
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

    /***
     * md5查询
     * @param column
     * @param value
     * @return
     */
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

    /***
     * 是否存在
     * @return
     */
    public boolean exists() {
        return this.count() > 0;
    }

    /***
     * 是否不存在
     * @return
     */
    public boolean notExists() {
        return this.count() == 0;
    }

    /***
     * 执行原生select
     */
    public ArrayList<HashMap<String, Object>> query(String sql) {
        this.create_connect();
        return new Query(this.getConnection(), sql, new ArrayList<>()).get(false);
    }

    private ArrayList<String> ObjectFormat(Object[] params) {
        ArrayList<String> res = new ArrayList<>();
        for (Object param : params) {
            res.add(String.valueOf(param));
        }
        return res;
    }

    /***
     * 执行原生select
     */
    public ArrayList<HashMap<String, Object>> query(String sql, Object[] params) {
        this.create_connect();
        ArrayList<String> paramsList = this.ObjectFormat(params);
        ArrayList<HashMap<String, Object>> res = new Query(this.getConnection(), sql, paramsList).get(true);
        return res;
    }


    /***
     * 执行原生更新
     */
    public boolean update(String sql) {
        this.create_connect();
        return new Query(this.getConnection(), sql, new ArrayList<>()).execute();
    }

    /***
     * 执行原生更新
     */
    public boolean update(String sql, Object[] params) {
        this.create_connect();
        ArrayList<String> paramsList = this.ObjectFormat(params);

        return new Query(this.getConnection(), sql, paramsList).execute();
    }

    /***
     * 执行原生删除
     */
    public boolean delete(String sql) {
        this.create_connect();
        return new Query(this.getConnection(), sql, new ArrayList<>()).execute();
    }

    /***
     * 执行原生删除
     */
    public boolean delete(String sql, Object[] params) {
        this.create_connect();
        ArrayList<String> paramsList = this.ObjectFormat(params);
        return new Query(this.getConnection(), sql, paramsList).execute();
    }

    /***
     * limit限制
     * @param limit
     *
     * @return
     */
    public DB limit(int limit, int offset) {
        this.limit = new Integer[]{limit, offset};
        return this;
    }

    /***
     * limit限制，对于不需要offset的情况
     * @param limit
     * @return
     */
    public DB limit(int limit) {
        this.limit = new Integer[]{limit, null};
        return this;
    }

    /***
     * OrderBy 排序
     * @param column
     * @param order
     * @return
     */
    public DB orderBy(String column, String order) {
        this.orderBy.add(new String[]{column, order});
        return this;
    }


    /***
     * OrderBy排序
     * @param column
     * @return
     */
    public DB orderBy(String column) {
        return this.orderBy(column, "asc");
    }

    /***
     * 基于时间排序
     * @param column
     * @return
     */
    public DB latest(String column) {
        return this.orderBy(column, "desc");
    }

    public DB latest() {
        return this.orderBy("created_at", "desc");
    }

    /***
     * 基于时间排序
     * @param column
     * @return
     */
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
            this.execute("truncate table " + this.table);
            this.execute("alter table " + this.table + " auto_increment=1");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public int increment(String column, int value) {
        this.update = new HashMap<>();
        this.update.put(column, column + "(EasyDBNoBind)+" + value);
        this.getSql();
        return this.execute(this.compileSql, this.bind.toArray());

    }

    public int decrement(String column, int value) {
        this.update = new HashMap<>();
        this.update.put(column, column + "(EasyDBNoBind)-" + value);
        this.getSql();
        return this.execute(this.compileSql, this.bind.toArray());
    }

    public int increment(String column) {
        return this.increment(column, 1);
    }

    public int decrement(String column) {
        return this.decrement(column, 1);
    }

    public int incrementEach(HashMap<String, Object> data) {
        this.update = new HashMap<>();
        for (String column : data.keySet()) {
            this.update.put(column, column + "(EasyDBNoBind)+" + data.get(column));
        }
        this.getSql();
        return this.execute(this.compileSql, this.bind.toArray());
    }

    public int decrementEach(HashMap<String, Object> data) {
        this.update = new HashMap<>();
        for (String column : data.keySet()) {
            this.update.put(column, column + "(EasyDBNoBind)-" + data.get(column));
        }
        this.getSql();
        return this.execute(this.compileSql, this.bind.toArray());
    }


    public void close() {
        try {
            this.connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
