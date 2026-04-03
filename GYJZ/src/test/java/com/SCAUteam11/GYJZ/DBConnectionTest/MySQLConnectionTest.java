package com.SCAUteam11.GYJZ.DBConnectionTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@SpringBootTest
public class MySQLConnectionTest {
    @Autowired
    @Qualifier("mysqlDataSource")
    private DataSource mysqlDataSource;
    @Test
    void testMySqlConnection() throws SQLException {
        System.out.println("========== 测试 MySQL 连接 ==========");
        System.out.println("数据源类型: " + mysqlDataSource.getClass().getName());

        try (Connection conn = mysqlDataSource.getConnection()) {
            System.out.println("✅ MySQL 连接成功!");
            System.out.println("   数据库: " + conn.getCatalog());
            System.out.println("   用户: " + conn.getMetaData().getUserName());
            System.out.println("   驱动: " + conn.getMetaData().getDriverName());
            System.out.println("   版本: " + conn.getMetaData().getDatabaseProductVersion());
        }
    }

}
