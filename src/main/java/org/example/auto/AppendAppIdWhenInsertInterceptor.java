package org.example.auto;

import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitor;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.expression.operators.relational.NamedExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.SubSelect;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.builder.annotation.ProviderSqlSource;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.scripting.defaults.RawSqlSource;
import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;
import org.apache.ibatis.scripting.xmltags.TextSqlNode;
import org.apache.ibatis.type.StringTypeHandler;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 动态化插入appId,mybatis 插件
 */
@Intercepts({@Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})})
public class AppendAppIdWhenInsertInterceptor implements Interceptor {
    private String APP_ID_COLUMN_NAME = AppIdHolder.APP_ID_COLUMN_NAME;

    //反射时使用变量
    Field sqlFieldInBoundSql = ReflectionUtils.findField(BoundSql.class, "sql");
    Method createSqlSourceMethodInProviderSqlSource = null;
    Field sqlSourceFiledInMappedStatement = ReflectionUtils.findField(MappedStatement.class, "sqlSource");
    Field parameterMappingFieldInStaticSqlSource = ReflectionUtils.findField(StaticSqlSource.class, "parameterMappings");
    Field rootSqlNodeFieldInDynamicSqlSource = ReflectionUtils.findField(DynamicSqlSource.class, "rootSqlNode");
    Field sqlFieldInStaticSqlSource = ReflectionUtils.findField(StaticSqlSource.class, "sql");
    Field sqlSourceFieldInRawSqlSource = ReflectionUtils.findField(RawSqlSource.class, "sqlSource");
    public AppendAppIdWhenInsertInterceptor()
    {
        rootSqlNodeFieldInDynamicSqlSource.setAccessible(true);

        sqlFieldInStaticSqlSource.setAccessible(true);

        sqlSourceFiledInMappedStatement.setAccessible(true);

        parameterMappingFieldInStaticSqlSource.setAccessible(true);

        sqlFieldInBoundSql.setAccessible(true);

        sqlSourceFieldInRawSqlSource.setAccessible(true);

        Method[] declaredMethods = ReflectionUtils.getDeclaredMethods(ProviderSqlSource.class);
        if(declaredMethods!=null){
            for (Method declaredMethod : declaredMethods) {
                if(declaredMethod.getName().equals("createSqlSource")){
                    createSqlSourceMethodInProviderSqlSource = declaredMethod;
                    createSqlSourceMethodInProviderSqlSource.setAccessible(true);
                    break;
                }
            }
        }
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        //当sqlSource 为 ProviderSqlSource 时，修改sql要替换 MappedStatement 中的sqlSource ,处理完后，需要替换回旧的
        ProviderSqlSource backUpOriginalProviderSqlSource = null;
        MappedStatement mappedStatementObj = (MappedStatement) invocation.getArgs()[0];
        try {
            //获取前端传递的appid
            String appid = AppIdHolder.appId.get();
            //非插入不处理
            if (!mappedStatementObj.getSqlCommandType().equals(SqlCommandType.INSERT) || appid==null || "".equals(appid.trim())) {
                return invocation.proceed();
            }

            Object parameterObj = invocation.getArgs()[1];

            // 增加入参
            if (parameterObj instanceof MapperMethod.ParamMap) {
                ((MapperMethod.ParamMap) parameterObj).put(APP_ID_COLUMN_NAME, appid);
            } else {
                if(parameterObj!=null){
                    //下面反射操作中不能缓存
                    Field appidField = ReflectionUtils.findField(parameterObj.getClass(), APP_ID_COLUMN_NAME);
                    if(appidField!=null){
                        appidField.setAccessible(true);
                        appidField.set(parameterObj,appid);
                    }else {
                        Field[] declaredFields = parameterObj.getClass().getDeclaredFields();
                        Map argsMap = new HashMap();
                        for (Field declaredField : declaredFields) {
                            declaredField.setAccessible(true);
                            argsMap.put(declaredField.getName(),declaredField.get(parameterObj));
                        }
                        argsMap.put(APP_ID_COLUMN_NAME,appid);
                        invocation.getArgs()[1] = argsMap;
                    }
                }else {
                    MapperMethod.ParamMap paramMap = new MapperMethod.ParamMap();
                    paramMap.put(APP_ID_COLUMN_NAME,appid);
                    invocation.getArgs()[1] = paramMap;
                }
            }

            SqlSource sqlSourceOriginal = mappedStatementObj.getSqlSource();
            String mybatisSqlStr = null;
            //用于保存mybatis ${} #{} 等表达式
            List<String> sqlReplaceStr = new ArrayList<>();

            if(sqlSourceOriginal instanceof DynamicSqlSource ){
                mybatisSqlStr = getOriginnalSqlFromDynamicSqlSource(sqlSourceOriginal, mybatisSqlStr, sqlReplaceStr);
            } else if (sqlSourceOriginal instanceof ProviderSqlSource) {
                //备份原sqlSource ,finally 中后替换回去
                backUpOriginalProviderSqlSource = (ProviderSqlSource)sqlSourceOriginal;

                SqlSource templateSqlSource = (SqlSource) createSqlSourceMethodInProviderSqlSource.invoke(sqlSourceOriginal, parameterObj);

                mybatisSqlStr = getOriginnalSqlFromDynamicSqlSource(templateSqlSource, mybatisSqlStr, sqlReplaceStr);
                sqlSourceFiledInMappedStatement.set(mappedStatementObj,templateSqlSource);
                sqlSourceOriginal = templateSqlSource;
            } else {
                BoundSql boundSql = mappedStatementObj.getBoundSql(parameterObj);
                mybatisSqlStr = (String) sqlFieldInBoundSql.get(boundSql);
            }

            //修改sql
            Insert insert = (Insert) CCJSqlParserUtil.parse(mybatisSqlStr);
            boolean flag = false;
            //表内没有appid 字段不处理
            if(! AppIdHolder.tableSet.contains(insert.getTable().getName().toLowerCase())){
                return invocation.proceed();
            }

            //原sql有操作appid 不处理
            for (Column column : insert.getColumns()) {
                if (column.getColumnName().equals(APP_ID_COLUMN_NAME)) {
                    flag = true;
                    break;
                }
            }

            if (!flag) {
                // sql 中增加字段
                insert.addColumns(new Column(APP_ID_COLUMN_NAME));
                //adding a value using a visitor
                SqlSource finalSqlSource = sqlSourceOriginal;
                // sql 中增加参数
                insert.getItemsList().accept(new ItemsListVisitor() {
                    public void visit(SubSelect subSelect) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                    public void visit(ExpressionList expressionList) {
                        //在sql 中增加参数
                        if(finalSqlSource instanceof DynamicSqlSource){
                            expressionList.getExpressions().add(new StringValue("${"+APP_ID_COLUMN_NAME+"}"));
                        }else {
                            expressionList.getExpressions().add(new JdbcParameter());
                        }
                    }
                    @Override
                    public void visit(NamedExpressionList namedExpressionList) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                    public void visit(MultiExpressionList multiExprList) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                });

                //sql 中增加 插入字段值
                if (sqlSourceOriginal instanceof RawSqlSource) {
                    SqlSource sqlSourceInRawSqlSource = (SqlSource) sqlSourceFieldInRawSqlSource.get(sqlSourceOriginal);
                    replaceSqlInStaticSqlSource(mappedStatementObj, sqlSourceInRawSqlSource, insert);
                } else if (sqlSourceOriginal instanceof StaticSqlSource) {
                    replaceSqlInStaticSqlSource(mappedStatementObj, sqlSourceOriginal, insert);
                } else if (sqlSourceOriginal instanceof ProviderSqlSource) {
                    //处理过程中被替换成其它类型，理论上不会有 ProviderSqlSource
                } else if (sqlSourceOriginal instanceof DynamicSqlSource) {
                    replaceSqlInDynamicSqlSource(sqlSourceOriginal, sqlReplaceStr, insert);
                }
            }

            return invocation.proceed();
        } finally {
            //ProviderSqlSource 时替换为原 sqlSource
            if(backUpOriginalProviderSqlSource!=null){
                sqlSourceFiledInMappedStatement.set(mappedStatementObj,backUpOriginalProviderSqlSource);
            }
        }
    }

    /**
     * 替换sql
     * @param sqlSourceOriginal
     * @param sqlReplaceStr
     * @param insert
     * @throws IllegalAccessException
     */
    private void replaceSqlInDynamicSqlSource(SqlSource sqlSourceOriginal, List<String> sqlReplaceStr, Insert insert) throws IllegalAccessException {
        Object sqlNode = rootSqlNodeFieldInDynamicSqlSource.get(sqlSourceOriginal);
        Field text = ReflectionUtils.findField(TextSqlNode.class, "text");
        text.setAccessible(true);
        //替换 ? 为 # $,恢复原sql
        String newSql = insert.toString();
        String[] split = newSql.split("\\?");
        StringBuilder newSqlBuffer = new StringBuilder();
        for (int i = 0; i < split.length; i++) {
            if(i< sqlReplaceStr.size()){
                newSqlBuffer.append(split[i]);
                newSqlBuffer.append(sqlReplaceStr.get(i));
            }else {
                newSqlBuffer.append(split[i]);
                if(i<split.length-1){
                    newSqlBuffer.append("?");
                }
            }
        }
        text.set(sqlNode,newSqlBuffer.toString());
    }

    /**
     * 替换SQL
     * @param mappedStatementObj
     * @param sqlSourceOriginal
     * @param insert
     * @throws IllegalAccessException
     */
    private void replaceSqlInStaticSqlSource(MappedStatement mappedStatementObj, SqlSource sqlSourceOriginal, Insert insert) throws IllegalAccessException {
        sqlFieldInStaticSqlSource.set(sqlSourceOriginal, insert.toString());
        List<ParameterMapping> parameterMappingList = (List<ParameterMapping>) parameterMappingFieldInStaticSqlSource.get(sqlSourceOriginal);
        ParameterMapping parameterMapping = new ParameterMapping.Builder(mappedStatementObj.getConfiguration(), APP_ID_COLUMN_NAME,
                String.class.getClass()).typeHandler(new StringTypeHandler()).build();
        parameterMappingList.add(parameterMapping);
    }

    /**
     * 获取mybatis 去掉${} #{}后的sql
     * @param sqlSourceOriginal
     * @param sqlOriginal
     * @param sqlReplaceStr
     * @return
     * @throws IllegalAccessException
     */
    private  String getOriginnalSqlFromDynamicSqlSource(SqlSource sqlSourceOriginal, String sqlOriginal, List<String> sqlReplaceStr) throws IllegalAccessException {
        Object sqlNode = rootSqlNodeFieldInDynamicSqlSource.get(sqlSourceOriginal);

        Field text = ReflectionUtils.findField(TextSqlNode.class, "text");
        text.setAccessible(true);
        sqlOriginal = (String) text.get(sqlNode);

        //替换 ${} 为 ?
        Pattern pattern = Pattern.compile("[\\$\\#]\\{[^}]*\\}");
        Matcher matcher = pattern.matcher(sqlOriginal);
        while (matcher.find()){
            sqlReplaceStr.add(matcher.group());
        }
        return matcher.replaceAll("\\?");
    }

}