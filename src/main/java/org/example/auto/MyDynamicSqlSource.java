package org.example.auto;

import org.apache.ibatis.builder.SqlSourceBuilder;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.xmltags.DynamicContext;
import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.session.Configuration;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

public class MyDynamicSqlSource extends DynamicSqlSource {

    private Configuration configuration;
    private SqlNode rootSqlNode;

    private String  newSql;
    Field sqlFieldInBoundSql = ReflectionUtils.findField(BoundSql.class, "sql");

    public MyDynamicSqlSource(Configuration configuration, SqlNode rootSqlNode,String newSql) {
        super(configuration, rootSqlNode);
        this.configuration = configuration;
        this.rootSqlNode = rootSqlNode;
        this.newSql = newSql;

        sqlFieldInBoundSql.setAccessible(true);
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        DynamicContext context = new DynamicContext(configuration, parameterObject);
        rootSqlNode.apply(context);
        SqlSourceBuilder sqlSourceParser = new SqlSourceBuilder(configuration);
        Class<?> parameterType = parameterObject == null ? Object.class : parameterObject.getClass();
        SqlSource sqlSource = sqlSourceParser.parse(context.getSql(), parameterType, context.getBindings());
//        SqlSource sqlSource = sqlSourceParser.parse(newSql, parameterType, context.getBindings());
        BoundSql boundSql = sqlSource.getBoundSql(parameterObject);
        context.getBindings().forEach(boundSql::setAdditionalParameter);
        try {
            sqlFieldInBoundSql.set(boundSql,newSql);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return boundSql;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public SqlNode getRootSqlNode() {
        return rootSqlNode;
    }

    public void setRootSqlNode(SqlNode rootSqlNode) {
        this.rootSqlNode = rootSqlNode;
    }

    public String getNewSql() {
        return newSql;
    }

    public void setNewSql(String newSql) {
        this.newSql = newSql;
    }
}
