/*
 * Copyright (c) 2024. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wlkjyy.Eloquent;

/**
 * @Author: wlkjyy <wlkjyy@vip.qq.com>
 * @Date: 2024/3/29
 * @Project: EasyDB
 */

import java.util.ArrayList;
import java.util.HashMap;

/***
 * 分页器
 */
public class Paginate {
    private final int total;
    private final ArrayList<HashMap<String, Object>> pageData;
    private final String pageParamterName;
    private final int page;
    private final int perPage;

    private String viewType = "bootstrap5";

    private String userDefinedParamter = null;


    public Paginate(int total, ArrayList<HashMap<String, Object>> pageData, String pageParamterName, int page, int perPage) {
        this.total = total;
        this.pageData = pageData;
        this.pageParamterName = pageParamterName;
        this.page = page;
        this.perPage = perPage;


    }


    /***
     * 生成分页参数
     * @param page
     * @return
     */
    public String generateParamter(int page) {
        return "?" + this.pageParamterName + "=" + page + (this.userDefinedParamter == null ? "" : "&" + this.userDefinedParamter);
    }

    public Paginate setViewType(String viewType) {
        this.viewType = viewType;
        return this;
    }

    /***
     * 是否有分页
     * @return
     */
    public boolean hasPages() {
        return this.total > 0;
    }

    /**
     * 获取分页的HTML
     *
     * @param paramter
     * @return
     */
    public String links(String paramter) {

        if (paramter.startsWith("?")) {
            paramter = paramter.substring(1);
        }
        this.userDefinedParamter = paramter;

        return links();
    }

    public Paginate setParamter(String paramter) {
        if (paramter.startsWith("?")) {
            paramter = paramter.substring(1);
        }
        this.userDefinedParamter = paramter;
        return this;
    }

    public String links() {
        switch (this.viewType) {
            case "bootstrap5":
                return this.renderBootstrap5();
            default:
                return this.renderBootstrap5();
        }
    }

    /***
     * Bootstrap5分页渲染
     * @return
     */
    private String renderBootstrap5() {

        StringBuilder html = new StringBuilder();
        if (this.hasPages()) {
            html.append("<nav class=\"d-flex justify-items-center justify-content-between\">\n");
            html.append("<div class=\"d-flex justify-content-between flex-fill d-sm-none\">\n");
            html.append("<ul class=\"pagination\">\n");
            if (this.onFirstPage()) {
                html.append("<li class=\"page-item disabled\" aria-disabled=\"true\">\n");
                html.append("<span class=\"page-link\">&laquo;</span>\n");
                html.append("</li>\n");
            } else {
                html.append("<li class=\"page-item\">\n");
                html.append("<a class=\"page-link\" href=\"").append(this.firstPageUrl()).append("\" rel=\"prev\">&laquo;</a>\n");
                html.append("</li>\n");
            }
            if (this.hasMorePages()) {
                html.append("<li class=\"page-item\">\n");
                html.append("<a class=\"page-link\" href=\"").append(this.nextPageUrl()).append("\" rel=\"next\">&raquo;</a>\n");
                html.append("</li>\n");
            } else {
                html.append("<li class=\"page-item disabled\" aria-disabled=\"true\">\n");
                html.append("<span class=\"page-link\">&raquo;</span>\n");
                html.append("</li>\n");

            }
            html.append("</ul>\n");
            html.append("</div>\n");

            html.append("<div class=\"d-none flex-sm-fill d-sm-flex align-items-sm-center justify-content-sm-between\">\n");
            html.append("<div>\n");
            html.append("<p class=\"small text-muted\">");
            html.append("Showing ");
            html.append("<span class=\"fw-semibold\">");
            html.append((this.currentPage() - 1) * this.perPage + 1);
            html.append("</span>");
            html.append(" to ");
            html.append("<span class=\"fw-semibold\">");
            html.append((this.currentPage() - 1) * this.perPage + this.count());
            html.append("</span>");
            html.append(" of ");
            html.append("<span class=\"fw-semibold\">");
            html.append(this.total());
            html.append("</span>");
            html.append(" results");
            html.append("</p>\n");
            html.append("</div>\n");
            html.append("<div>\n");
            html.append("<ul class=\"pagination\">\n");
            if (this.onFirstPage()) {
                html.append("<li class=\"page-item disabled\" aria-disabled=\"true\">\n");
                html.append("<span class=\"page-link\">&laquo;</span>\n");
                html.append("</li>\n");
            } else {
                html.append("<li class=\"page-item\">\n");
                html.append("<a class=\"page-link\" href=\"").append(this.firstPageUrl()).append("\" rel=\"prev\">&laquo;</a>\n");
                html.append("</li>\n");
            }

            for (int i = 1; i <= this.lastPage(); i++) {
                if (i == this.currentPage()) {
                    html.append("<li class=\"page-item active\" aria-current=\"page\">\n");
                    html.append("<span class=\"page-link\">").append(i).append("</span>\n");
                    html.append("</li>\n");
                } else {
                    html.append("<li class=\"page-item\">\n");
                    html.append("<a class=\"page-link\" href=\"").append(this.url(i)).append("\">").append(i).append("</a>\n");
                    html.append("</li>\n");
                }
            }

            if (this.hasMorePages()) {
                html.append("<li class=\"page-item\">\n");
                html.append("<a class=\"page-link\" href=\"").append(this.nextPageUrl()).append("\" rel=\"next\">&raquo;</a>\n");
                html.append("</li>\n");
            } else {
                html.append("<li class=\"page-item disabled\" aria-disabled=\"true\">\n");
                html.append("<span class=\"page-link\">&raquo;</span>\n");
                html.append("</li>\n");
            }

            html.append("</ul>\n");
            html.append("</div>\n");
            html.append("</div>\n");
            html.append("</nav>\n");


        }


        return html.toString();

    }


    public HashMap<Object, Object> toHashmap() {
        return new HashMap<>() {
            {
                put("total", total());
                put("per_page", perPage);
                put("current_page", page);
                put("last_page", lastPage());
                put("first_page_url", firstPageUrl());
                put("last_page_url", lastPageUrl());
                put("next_page_url", nextPageUrl());
                put("prev_page_url", previousPageUrl());
                put("data", pageData);
            }
        };
    }

    /***
     * 获取当前分页的数据总数
     * @return
     */
    public int count() {
        return this.pageData.size();
    }

    /***
     * 获取当前的页码
     * @return
     */
    public int currentPage() {
        return (Math.max(this.page, 1));
    }

    /***
     * 获取当前也的数据
     * @return
     */
    public ArrayList<HashMap<String, Object>> items() {
        return this.pageData;
    }

    /***
     * 获取最后一页的页码
     * @return
     */
    public int lastPage() {
        return (int) Math.ceil((double) total / perPage);
    }

    /***
     * 获取最后一页的URL
     * @return
     */
    public String lastPageUrl() {
        return this.generateParamter(this.lastPage());
    }

    /***
     * 获取第一页的URL
     * @return
     */
    public String firstPageUrl() {
        return this.generateParamter(1);
    }

    /***
     * 获取下一页的URL
     * @return
     */
    public String nextPageUrl() {
//        return this.generateParamter(this.page + 1);
        if (this.page == this.lastPage()) {
            return this.generateParamter(this.lastPage());
        } else {
            return this.generateParamter(this.page + 1);
        }
    }


    /***
     * 是否是第一页
     * @return
     */
    public boolean onFirstPage() {
        return this.page == 1;
    }

    /***
     * 获取每页显示的数据量
     * @return
     */
    public int perPage() {
        return this.perPage;
    }

    /***
     * 获取上一页的URL
     * @return
     */
    public String previousPageUrl() {
        if (this.page == 1) {
            return this.generateParamter(1);
        } else {
            return this.generateParamter(this.page - 1);
        }
    }

    /***
     * 数据总数
     * @return
     */
    public int total() {
        return this.total;
    }

    /***
     * 获取指定页的URL
     * @param page
     * @return
     */
    public String url(int page) {
        return this.generateParamter(page);
    }


    /***
     * 是否有更多的页
     * @return
     */
    public boolean hasMorePages() {
        return this.page < this.lastPage();
    }
}
