package org.example;

import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitor;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.expression.operators.relational.NamedExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.SubSelect;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.builder.annotation.ProviderSqlSource;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.PreparedStatementHandler;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.scripting.defaults.RawSqlSource;
import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Properties;

@Intercepts({@Signature(type = ParameterHandler.class, method = "setParameters", args = {PreparedStatement.class})})
public class ParameterHandlerPlugin implements Interceptor {
    private Properties properties = new Properties();

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
//
//        DefaultParameterHandler target = (DefaultParameterHandler)invocation.getTarget();
//
//        Field mappedStatement = ReflectionUtils.findField(DefaultParameterHandler.class, "mappedStatement");
//        mappedStatement.setAccessible(true);
//        MappedStatement mappedStatementObj = (MappedStatement) mappedStatement.get(target);
//
//        BoundSql boundSql = mappedStatementObj.getBoundSql(target.getParameterObject());
//        Field sql1 = ReflectionUtils.findField(BoundSql.class, "sql");
//        sql1.setAccessible(true);
//        String sql = (String) sql1.get(boundSql);
//
//        Insert insert = (Insert) CCJSqlParserUtil.parse(sql);
//        insert.addColumns(new Column("appid"));
//        //adding a value using a visitor
//        insert.getItemsList().accept(new ItemsListVisitor() {
//            public void visit(SubSelect subSelect) {
//                throw new UnsupportedOperationException("Not supported yet.");
//            }
//
//            public void visit(ExpressionList expressionList) {
//                expressionList.getExpressions().add(new JdbcParameter());
//            }
//
//            @Override
//            public void visit(NamedExpressionList namedExpressionList) {
//                throw new UnsupportedOperationException("Not supported yet.");
//            }
//
//            public void visit(MultiExpressionList multiExprList) {
//                throw new UnsupportedOperationException("Not supported yet.");
//            }
//        });
//
//        SqlSource sqlSource = mappedStatementObj.getSqlSource();
//        if(sqlSource instanceof RawSqlSource){
//            Field sqlSource1 = ReflectionUtils.findField(RawSqlSource.class, "sqlSource");
//            sqlSource1.setAccessible(true);
//            Field sql2 = ReflectionUtils.findField(StaticSqlSource.class, "sql");
//            sql2.setAccessible(true);
//            Object o = sqlSource1.get(sqlSource);
//            sql2.set(o,insert.toString());
//        }else if(sqlSource instanceof StaticSqlSource){
//
//        }else if(sqlSource instanceof ProviderSqlSource){
//
//        }else if(sqlSource instanceof DynamicSqlSource){
//
//        }
//
//        sql1.set(boundSql,insert.toString());

        // implement pre-processing if needed
        Object returnObject = invocation.proceed();
        // implement post-processing if needed
        return returnObject;
    }

    @Override
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    @Override
    public Object plugin(Object target) {
        return Interceptor.super.plugin(target);
    }
}