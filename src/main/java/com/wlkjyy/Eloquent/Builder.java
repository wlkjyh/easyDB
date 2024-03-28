/*
 * Copyright (c) 2024. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wlkjyy.Eloquent;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @Author: wlkjyy <wlkjyy@vip.qq.com>
 * @Date: 2024/3/26
 * @Project: EasyDB
 */
public class Builder {

    private final String table;
    private final ArrayList<String[]> where;
    private final ArrayList<String[]> orWhere;

    private final boolean delete;
    private final HashMap<String, Object> update;
    private final HashMap<String, Object> insert;
    private final ArrayList<String> select;
    private final Integer[] limit;
    private final ArrayList<String[]> orderBy;
    private final ArrayList<String> groupBy;

    private ArrayList<String> bind = new ArrayList<>();

    private String sql;

    public Builder(String table, ArrayList<String[]> where, ArrayList<String[]> orWhere, boolean delete, HashMap<String, Object> update, HashMap<String, Object> insert, ArrayList<String> select, Integer[] limit, ArrayList<String[]> orderBy, ArrayList<String> groupBy) {
        this.table = table;
        this.where = where;
        this.orWhere = orWhere;
        this.delete = delete;
        this.update = update;
        this.insert = insert;
        this.select = select;
        this.limit = limit;
        this.orderBy = orderBy;
        this.groupBy = groupBy;

    }

    private String getWhere() {
        StringBuilder whereStr = new StringBuilder();
        if (!where.isEmpty()) {
            whereStr.append(" where ");
            for (String[] strings : where) {


//                如果包含第4个参数，不需要绑定
                if (strings.length == 4) {
                    whereStr.append("`" + strings[0] + "`").append(" ").append(strings[1]).append(" ").append(strings[2]).append(" and ");
                } else {

                    whereStr.append("`" + strings[0] + "`").append(" ").append(strings[1]).append(" ? and ");
                    bind.add(strings[2]);
                }
            }
            whereStr.delete(whereStr.length() - 4, whereStr.length());
        }
        return whereStr.toString();
    }

    private String getOrWhere() {
        StringBuilder whereStr = new StringBuilder();
        if (!orWhere.isEmpty()) {
            whereStr.append(" or ");
            for (String[] strings : orWhere) {
//                whereStr.append(strings[0]).append(" ").append(strings[1]).append(" ? and ");
//                bind.add(strings[2]);
                if (strings.length == 4) {
                    whereStr.append("`" + strings[0] + "`").append(" ").append(strings[1]).append(" ").append(strings[2]).append(" or ");
                } else {
                    whereStr.append("`" + strings[0] + "`").append(" ").append(strings[1]).append(" ? or ");
                    bind.add(strings[2]);
                }
            }
            whereStr.delete(whereStr.length() - 4, whereStr.length());
        }
        return whereStr.toString();
    }

    private String getUpdate() {
        StringBuilder updateStr = new StringBuilder();
        if (update != null) {
            updateStr.append(" set ");
            for (String key : update.keySet()) {

//                如果包含(EasyDBNoBind)字符串则不需要绑定
                if (update.get(key).toString().contains("EasyDBNoBind")) {
                    updateStr.append("`" + key + "`").append(" = ").append(update.get(key).toString().replace("(EasyDBNoBind)", "")).append(", ");
                } else {
                    updateStr.append("`" + key + "`").append(" = ?, ");
                    bind.add(update.get(key).toString());
                }
            }
            updateStr.delete(updateStr.length() - 2, updateStr.length());
        }
        return updateStr.toString();
    }

    private String getInsert() {
        StringBuilder insertStr = new StringBuilder();
        if (insert != null) {
            insertStr.append(" (");
            for (String key : insert.keySet()) {
                insertStr.append("`" + key + "`").append(", ");
                bind.add(insert.get(key).toString());
            }
            insertStr.delete(insertStr.length() - 2, insertStr.length());
            insertStr.append(") values (");
            for (String key : insert.keySet()) {
                insertStr.append("?, ");
            }
            insertStr.delete(insertStr.length() - 2, insertStr.length());
            insertStr.append(")");
        }
        return insertStr.toString();
    }

    private String getSelect() {
        StringBuilder selectStr = new StringBuilder();
        if (!select.isEmpty()) {
            selectStr.append("select ");
            for (String s : select) {
                if (s.equals("*")) {
                    return "select *";
                } else {
                    selectStr.append(s).append(", ");
                }
            }
            selectStr.delete(selectStr.length() - 2, selectStr.length());
        }
        return selectStr.toString();
    }

    public String getLimit() {
        if (limit != null) {
//            return " limit " + limit[0] + "," + limit[1];
            if (limit[1] == null) {
                return " limit " + limit[0];
            } else {
                return " limit " + limit[0] + "," + limit[1];
            }
        }
        return "";
    }

    public String getOrderBy() {
        StringBuilder orderByStr = new StringBuilder();
        if (!orderBy.isEmpty()) {
            orderByStr.append(" order by ");
            for (String[] strings : orderBy) {
                if ("rand()".equals(strings[0])) {
                    orderByStr.append("rand()").append(" ").append(strings[1]).append(", ");
                } else {
                    orderByStr.append("`" + strings[0] + "`").append(" ").append(strings[1]).append(", ");
                }
            }
            orderByStr.delete(orderByStr.length() - 2, orderByStr.length());
        }
        return orderByStr.toString();
    }

    public String getGroupBy() {
        StringBuilder groupByStr = new StringBuilder();
        if (!groupBy.isEmpty()) {
            groupByStr.append(" group by ");
            for (String s : groupBy) {
                groupByStr.append(s).append(", ");
            }
            groupByStr.delete(groupByStr.length() - 2, groupByStr.length());
        }
        return groupByStr.toString();
    }


    public Builder compileSql() {
        StringBuilder sql = new StringBuilder();
        if (delete) {
            sql.append("delete from ").append("`" + table + "`").append(getWhere()).append(getOrWhere());
        } else if (!update.isEmpty()) {
            sql.append("update ").append("`" + table + "`").append(getUpdate()).append(getWhere()).append(getOrWhere());
        } else if (!insert.isEmpty()) {
            sql.append("insert into ").append("`" + table + "`").append(getInsert());
        } else {
//            sql.append(getSelect()).append(" from ").append("`" + table + "`").append(getWhere()).append(getOrWhere()).append(getOrderBy()).append(getLimit());
            sql.append(getSelect()).append(" from ").append("`" + table + "`").append(getWhere()).append(getOrWhere()).append(getGroupBy()).append(getOrderBy()).append(getLimit());

        }
//        System.out.println(bind);
        this.sql = sql.toString();
        return this;
    }

    public String getSql() {


        return this.sql;
    }

    public ArrayList<String> getBind() {
        return this.bind;
    }


}
