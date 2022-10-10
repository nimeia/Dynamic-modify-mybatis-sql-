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
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.PreparedStatementHandler;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.javassist.tools.reflect.Reflection;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.sql.Statement;
import java.util.Properties;

@Intercepts({@Signature(type = StatementHandler.class, method = "update", args = {Statement.class})})
public class StatementHandlerPlugin implements Interceptor {
    private Properties properties = new Properties();

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // implement pre-processing if needed
//
//        RoutingStatementHandler target1 = (RoutingStatementHandler)invocation.getTarget();
//
//        Field delegate = ReflectionUtils.findField(RoutingStatementHandler.class, "delegate");
//        delegate.setAccessible(true);
//        Object o = delegate.get(target1);
//
//
//        PreparedStatementHandler target = (PreparedStatementHandler) o;
//        Field mappedStatement = ReflectionUtils.findField(PreparedStatementHandler.class, "mappedStatement");
//        mappedStatement.setAccessible(true);
//        MappedStatement mappedStatementObj = (MappedStatement) mappedStatement.get(target);
//
//
//        BoundSql boundSql1 = target.getBoundSql();
//        String sql = boundSql1.getSql();
//
//        Insert insert = (Insert) CCJSqlParserUtil.parse(sql);
//        insert.addColumns(new Column("appid"));
//        //adding a value using a visitor
//        insert.getItemsList().accept(new ItemsListVisitor() {
//
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
//        Field boundSql = ReflectionUtils.findField(PreparedStatementHandler.class, "boundSql");
//        boundSql.setAccessible(true);
//        BoundSql value = new BoundSql(mappedStatementObj.getConfiguration(),
//                insert.toString(), boundSql1.getParameterMappings(),
//                boundSql1.getParameterObject());
//        boundSql.set(target,value);


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