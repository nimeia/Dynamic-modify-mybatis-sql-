package org.example.auto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;


@Component
public class AppIdHolder {
    @Autowired
    DataSource dataSource;

    public static Set tableSet = new HashSet();

    public static final String APP_ID_COLUMN_NAME = "appid";

    @PostConstruct()
    void init() {
        try {
            //自动获取哪些表中有appid字段
            DatabaseMetaData metaData = dataSource.getConnection().getMetaData();

            ResultSet tables = metaData.getTables(dataSource.getConnection().getCatalog(), null, null, new String[]{"TABLE"});
            while (tables.next()) {
                String table_name = tables.getString("TABLE_NAME");
                ResultSet table_column = metaData.getColumns(dataSource.getConnection().getCatalog(), null, table_name, null);
                while (table_column.next()) {
                    String column_name = table_column.getString("COLUMN_NAME");
                    if (column_name.toLowerCase().equals(APP_ID_COLUMN_NAME)) {
                        tableSet.add(table_name.toLowerCase());
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
        }
    }

    public static final ThreadLocal<String> appId = new ThreadLocal<>();
}
