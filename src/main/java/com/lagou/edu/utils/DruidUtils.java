package com.lagou.edu.utils;

import com.alibaba.druid.pool.DruidDataSource;

/**
 * @author 应癫
 */
public class DruidUtils {

    private DruidUtils(){
    }

    private static DruidDataSource druidDataSource = new DruidDataSource();

    static {
        druidDataSource.setDriverClassName("com.mysql.jdbc.Driver");
        druidDataSource.setUrl("jdbc:mysql://localhost:3306/bank?useUnicode=true&characterEncoding=UTF-8");
        druidDataSource.setUsername("root");
        druidDataSource.setPassword("zhu12345");

    }

    public static DruidDataSource getInstance() {
        return druidDataSource;
    }

}
