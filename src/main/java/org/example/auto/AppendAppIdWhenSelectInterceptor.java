package org.example.auto;


import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Database;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.builder.SqlSourceBuilder;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.builder.annotation.ProviderSqlSource;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.scripting.defaults.RawSqlSource;
import org.apache.ibatis.scripting.xmltags.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.StringTypeHandler;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "queryCursor", args = {MappedStatement.class, Object.class, RowBounds.class}),})
public class AppendAppIdWhenSelectInterceptor implements Interceptor {

    //反射时使用变量
    Field sqlFieldInBoundSql = ReflectionUtils.findField(BoundSql.class, "sql");
    Method createSqlSourceMethodInProviderSqlSource = null;
    Field sqlSourceFiledInMappedStatement = ReflectionUtils.findField(MappedStatement.class, "sqlSource");
    Field parameterMappingFieldInStaticSqlSource = ReflectionUtils.findField(StaticSqlSource.class, "parameterMappings");
    Field rootSqlNodeFieldInDynamicSqlSource = ReflectionUtils.findField(DynamicSqlSource.class, "rootSqlNode");
    Field sqlFieldInStaticSqlSource = ReflectionUtils.findField(StaticSqlSource.class, "sql");
    Field sqlSourceFieldInRawSqlSource = ReflectionUtils.findField(RawSqlSource.class, "sqlSource");

    Field parameterMappingsFieldInStaticSqlSource = ReflectionUtils.findField(StaticSqlSource.class,"parameterMappings");
    Field additionalParametersFieldInBoundSql =ReflectionUtils.findField(BoundSql.class, "additionalParameters");

    Field textFieldInTextSqlNode = ReflectionUtils.findField(TextSqlNode.class, "text");
    public AppendAppIdWhenSelectInterceptor() {
        parameterMappingsFieldInStaticSqlSource.setAccessible(true);
        additionalParametersFieldInBoundSql.setAccessible(true);
        textFieldInTextSqlNode.setAccessible(true);
        rootSqlNodeFieldInDynamicSqlSource.setAccessible(true);
        sqlFieldInStaticSqlSource.setAccessible(true);
        sqlSourceFiledInMappedStatement.setAccessible(true);
        parameterMappingFieldInStaticSqlSource.setAccessible(true);
        sqlFieldInBoundSql.setAccessible(true);
        sqlSourceFieldInRawSqlSource.setAccessible(true);

        Method[] declaredMethods = ReflectionUtils.getDeclaredMethods(ProviderSqlSource.class);
        if (declaredMethods != null) {
            for (Method declaredMethod : declaredMethods) {
                if (declaredMethod.getName().equals("createSqlSource")) {
                    createSqlSourceMethodInProviderSqlSource = declaredMethod;
                    createSqlSourceMethodInProviderSqlSource.setAccessible(true);
                    break;
                }
            }
        }
    }

    Map<SqlSource,SqlSource> newSqlSourceCache = new HashMap<>();
    Map<SqlSource,Integer> newSqlSourceCacheAddParamsCount = new HashMap<>();
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        //当sqlSource 为 ProviderSqlSource 时，修改sql要替换 MappedStatement 中的sqlSource ,处理完后，需要替换回旧的
        SqlSource backUpOriginalProviderSqlSource = null;
        MappedStatement mappedStatementObj = (MappedStatement) invocation.getArgs()[0];
        System.out.println(mappedStatementObj);
        try {
            //非插入不处理
            if (!mappedStatementObj.getSqlCommandType().equals(SqlCommandType.SELECT) /*|| appid == null || "".equals(appid.trim())*/) {
                return invocation.proceed();
            }

            Object parameterObj = invocation.getArgs()[1];
            if (parameterObj instanceof MapperMethod.ParamMap) {
                MapperMethod.ParamMap paramMap = (MapperMethod.ParamMap) parameterObj;
                if (!(paramMap.containsKey(AppIdHolder.APP_ID_COLUMN_NAME))) {
                    return invocation.proceed();
                }
                Object appidObj = paramMap.get(AppIdHolder.APP_ID_COLUMN_NAME);
                if (appidObj == null || (appidObj instanceof String[] && ((String[]) appidObj).length == 0)) {
                    //如果有appid ，确没有传，且加上appid 只有二个参数，把appid参数去掉
                    if(appidObj!=null && paramMap.size()==4){
                        if(paramMap.containsKey("arg0")){
                            //未命名参数
                            Object arg01 = paramMap.get("arg0");
                            invocation.getArgs()[1]= arg01;
                            parameterObj = arg01;
                        }
                        //命名参数 不用处理
                        /*else if(paramMap.containsKey("param1")){
                            //命名参数
                            Object arg01 = paramMap.get("param1");
                            invocation.getArgs()[1]= arg01;
                            parameterObj = arg01;
                        }*/

                    }
                    return invocation.proceed();
                } else if (appidObj != null && appidObj instanceof String[]) {
                    //如果原方法只有一个入参，把原参数分解成map
                    Object arg0 = null;
                    if(paramMap.containsKey("arg0")) {
                        arg0 = paramMap.get("arg0");
                    }else if(paramMap.containsKey("param1")) {
                        arg0= paramMap.get("param1");
                    }
                    Field[] declaredFields = arg0.getClass().getDeclaredFields();
                    Map argsMap = new HashMap();
                    for (Field declaredField : declaredFields) {
                        declaredField.setAccessible(true);
                        if(!declaredField.getName().equals(AppIdHolder.APP_ID_COLUMN_NAME)){
                            argsMap.put(declaredField.getName(),declaredField.get(arg0));
                        }
                    }
                    paramMap.putAll(argsMap);

                    //appid 数组入参换成字符串
                    String[] appids = (String[]) paramMap.get(AppIdHolder.APP_ID_COLUMN_NAME);
                    paramMap.put(AppIdHolder.APP_ID_COLUMN_NAME, appids[0]);
                }else {
                    return invocation.proceed();
                }
            } else {
                //todo 待验证
                return invocation.proceed();
            }

            SqlSource sqlSourceOriginal = mappedStatementObj.getSqlSource();
            //备份原sqlSource ,finally 中后替换回去
            backUpOriginalProviderSqlSource = sqlSourceOriginal;

            boolean newSqlSourceCanCacheFlag = true;
            if (sqlSourceOriginal instanceof DynamicSqlSource) {
                Object sqlNode = rootSqlNodeFieldInDynamicSqlSource.get(sqlSourceOriginal);
                if (sqlNode instanceof MixedSqlNode) {
                    newSqlSourceCanCacheFlag = false;
                }
            }
            //判断是否已经缓存，providerSqlSource除外
            if (sqlSourceOriginal != null && !(sqlSourceOriginal instanceof ProviderSqlSource)) {
                SqlSource newSqlSource = newSqlSourceCache.get(sqlSourceOriginal);
                if (newSqlSource != null && newSqlSourceCanCacheFlag) {
                    sqlSourceFiledInMappedStatement.set(mappedStatementObj,newSqlSource);
                    /*int addConditionCount = newSqlSourceCacheAddParamsCount.get(sqlSourceOriginal);
                    if(sqlSourceOriginal instanceof  RawSqlSource || sqlSourceOriginal instanceof StaticSqlSource){
                        SqlSource tempSqlSource = sqlSourceOriginal;
                        if(sqlSourceOriginal instanceof  RawSqlSource){
                            tempSqlSource = (SqlSource) sqlSourceFieldInRawSqlSource.get(sqlSourceOriginal);
                        }

                        List<ParameterMapping> parameterMappingList = (List<ParameterMapping>) parameterMappingFieldInStaticSqlSource.get(tempSqlSource);
                        ParameterMapping parameterMapping = new ParameterMapping.Builder(mappedStatementObj.getConfiguration(), AppIdHolder.APP_ID_COLUMN_NAME,
                                String.class.getClass()).typeHandler(new StringTypeHandler()).build();
                        for (int i = 0; i < addConditionCount; i++) {
                            parameterMappingList.add(parameterMapping);
                        }
                    }*/
                    return invocation.proceed();
                }
            }

            String mybatisSqlStr = null;
            //用于保存mybatis ${} #{} 等表达式
            List<String> sqlReplaceStr = new ArrayList<>();

            boolean providreSqlSourceFlag = false;


            if (sqlSourceOriginal instanceof DynamicSqlSource) {
                mybatisSqlStr = getOriginnalSqlFromDynamicSqlSource(sqlSourceOriginal, mybatisSqlStr, sqlReplaceStr, mappedStatementObj, parameterObj);
            } else if (sqlSourceOriginal instanceof ProviderSqlSource) {
                SqlSource templateSqlSource = (SqlSource) createSqlSourceMethodInProviderSqlSource.invoke(sqlSourceOriginal, parameterObj);
                mybatisSqlStr = getOriginnalSqlFromDynamicSqlSource(templateSqlSource, mybatisSqlStr, sqlReplaceStr, mappedStatementObj, parameterObj);
                sqlSourceFiledInMappedStatement.set(mappedStatementObj, templateSqlSource);
                sqlSourceOriginal = templateSqlSource;
                providreSqlSourceFlag = true;
            } else {
                BoundSql boundSql = mappedStatementObj.getBoundSql(parameterObj);
                mybatisSqlStr = (String) sqlFieldInBoundSql.get(boundSql);
            }

            //分析SQL
            Select select = (Select) CCJSqlParserUtil.parse(mybatisSqlStr);
            PlainSelect selectBody = (PlainSelect) select.getSelectBody();
            Integer addConditionCount = modifySelect(selectBody, sqlSourceOriginal);

            Boolean result = (addConditionCount != 0);
            if (!result) {
                return invocation.proceed();
            } else {
                //替换 SQL
                if (sqlSourceOriginal instanceof RawSqlSource) {
                    SqlSource sqlSourceInRawSqlSource = (SqlSource) sqlSourceFieldInRawSqlSource.get(sqlSourceOriginal);
                    RawSqlSource newSqlSource = new RawSqlSource(mappedStatementObj.getConfiguration()
                            ,sqlFieldInStaticSqlSource.get(sqlSourceInRawSqlSource).toString()
                            ,parameterObj.getClass());
                    SqlSource tempSqlSourceInRawSqlSource = (SqlSource) sqlSourceFieldInRawSqlSource.get(newSqlSource);

                    List<ParameterMapping> tempParameterMappingList =  (List<ParameterMapping>) parameterMappingsFieldInStaticSqlSource.get(sqlSourceInRawSqlSource);
                    List<ParameterMapping> tempParameterMappingListNew = new ArrayList<>();
                    tempParameterMappingListNew.addAll(tempParameterMappingList);
                    parameterMappingsFieldInStaticSqlSource.set(tempSqlSourceInRawSqlSource,tempParameterMappingListNew );

                    replaceSqlInStaticSqlSource(mappedStatementObj, tempSqlSourceInRawSqlSource, select, addConditionCount);

                    if(newSqlSourceCanCacheFlag){
                        newSqlSourceCache.put(sqlSourceOriginal,newSqlSource);
                        newSqlSourceCacheAddParamsCount.put(sqlSourceOriginal,addConditionCount);
                    }
                    sqlSourceFiledInMappedStatement.set(mappedStatementObj,newSqlSource);

                } else if (sqlSourceOriginal instanceof StaticSqlSource) {
                    List<ParameterMapping> tempParameterMappingList =  (List<ParameterMapping>) parameterMappingsFieldInStaticSqlSource.get(sqlSourceOriginal);
                    List<ParameterMapping> tempParameterMappingListNew = new ArrayList<>();
                    tempParameterMappingListNew.addAll(tempParameterMappingList);

                    StaticSqlSource newSqlSource = new StaticSqlSource(mappedStatementObj.getConfiguration()
                            , sqlFieldInStaticSqlSource.get(sqlSourceOriginal).toString()
                            , tempParameterMappingListNew );
                    replaceSqlInStaticSqlSource(mappedStatementObj, sqlSourceOriginal, select, addConditionCount);
                    if(newSqlSourceCanCacheFlag){
                        newSqlSourceCache.put(sqlSourceOriginal,newSqlSource);
                        newSqlSourceCacheAddParamsCount.put(sqlSourceOriginal,addConditionCount);
                    }
                    sqlSourceFiledInMappedStatement.set(mappedStatementObj,newSqlSource);

                } else if (sqlSourceOriginal instanceof ProviderSqlSource) {
                    //处理过程中被替换成其它类型，理论上不会有 ProviderSqlSource
                } else if (sqlSourceOriginal instanceof DynamicSqlSource) {
//                    SqlSource newSqlSource = new MyDynamicSqlSource(mappedStatementObj.getConfiguration()
//                            , ((SqlNode) rootSqlNodeFieldInDynamicSqlSource.get(backUpOriginalProviderSqlSource)),select.toString());
                    DynamicSqlSource newSqlSource =new DynamicSqlSource(mappedStatementObj.getConfiguration(),new TextSqlNode(""));
                    replaceSqlInDynamicSqlSource(newSqlSource, sqlReplaceStr, select);
                    if(!providreSqlSourceFlag && newSqlSourceCanCacheFlag){
                        newSqlSourceCache.put(sqlSourceOriginal,newSqlSource);
                        newSqlSourceCacheAddParamsCount.put(sqlSourceOriginal,addConditionCount);
                    }
                    sqlSourceFiledInMappedStatement.set(mappedStatementObj,newSqlSource);
                }
            }
            return invocation.proceed();
        } finally {
            //ProviderSqlSource 时替换为原 sqlSource
            if (backUpOriginalProviderSqlSource != null) {
                sqlSourceFiledInMappedStatement.set(mappedStatementObj, backUpOriginalProviderSqlSource);
            }
        }
    }

    /**
     * 替换sql
     *
     * @param sqlSourceOriginal
     * @param sqlReplaceStr
     * @param insert
     * @throws IllegalAccessException
     */
    private void replaceSqlInDynamicSqlSource(SqlSource sqlSourceOriginal, List<String> sqlReplaceStr, Select insert) throws IllegalAccessException {

        Object sqlNode = rootSqlNodeFieldInDynamicSqlSource.get(sqlSourceOriginal);
        if(sqlNode instanceof TextSqlNode){
            //替换 ? 为 # $,恢复原sql
            String newSql = insert.toString();
            String[] split = newSql.split("\\?");
            StringBuilder newSqlBuffer = new StringBuilder();
            for (int i = 0; i < split.length; i++) {
                if (i < sqlReplaceStr.size()) {
                    newSqlBuffer.append(split[i]);
                    newSqlBuffer.append(sqlReplaceStr.get(i));
                } else {
                    newSqlBuffer.append(split[i]);
                    if (i < split.length - 1) {
                        newSqlBuffer.append("?");
                    }
                }
            }

            textFieldInTextSqlNode.set(sqlNode, newSqlBuffer.toString());
        }
    }

    /**
     * 替换SQL
     *
     * @param mappedStatementObj
     * @param sqlSourceOriginal
     * @param insert
     * @param addConditionCount
     * @throws IllegalAccessException
     */
    private void replaceSqlInStaticSqlSource(MappedStatement mappedStatementObj, SqlSource sqlSourceOriginal, Select insert, Integer addConditionCount) throws IllegalAccessException {
        sqlFieldInStaticSqlSource.set(sqlSourceOriginal, insert.toString());
        List<ParameterMapping> parameterMappingList = (List<ParameterMapping>) parameterMappingFieldInStaticSqlSource.get(sqlSourceOriginal);
        ParameterMapping parameterMapping = new ParameterMapping.Builder(mappedStatementObj.getConfiguration(), AppIdHolder.APP_ID_COLUMN_NAME,
                String.class.getClass()).typeHandler(new StringTypeHandler()).build();
        for (int i = 0; i < addConditionCount; i++) {
            parameterMappingList.add(parameterMapping);
        }
    }

    /**
     * 生成修改Sql
     *
     * @param selectBody
     * @param sqlSourceOriginal
     * @return
     */
    private Integer modifySelect(PlainSelect selectBody, SqlSource sqlSourceOriginal) {
        FromItem fromItem = selectBody.getFromItem();

        List<Join> joins = selectBody.getJoins();

        Expression where = selectBody.getWhere();

        Map<String, String> tables = new LinkedHashMap<>();
        Integer count = 0;

        if (fromItem instanceof Table) {
            String name = ((Table) fromItem).getName();
            Alias alias = fromItem.getAlias();
            Database database = ((Table) fromItem).getDatabase();
            String schemaName = ((Table) fromItem).getSchemaName();
            Pivot pivot = fromItem.getPivot();
            tables.put(alias != null ? alias.getName() : (schemaName == null ? name : schemaName + "." + name), name);
        } else if (fromItem instanceof SubSelect) {
            //todo 参数顺序有问题
            PlainSelect selectBody1 = (PlainSelect) ((SubSelect) fromItem).getSelectBody();
            count += modifySelect(selectBody1, sqlSourceOriginal);
        } else {
            throw new RuntimeException("未确认");
        }
        if(joins!=null){
            for (Join join : joins) {
                FromItem rightItem = join.getRightItem();
                if (rightItem instanceof Table) {
                    String name = ((Table) rightItem).getName();
                    Alias alias = rightItem.getAlias();
                    Database database = ((Table) rightItem).getDatabase();
                    String schemaName = ((Table) rightItem).getSchemaName();
                    Pivot pivot = rightItem.getPivot();
                    tables.put(alias != null ? alias.getName() : (schemaName == null ? name : schemaName + "." + name), name);
                }
            }
        }

        List<EqualsTo> equalsTos = new ArrayList<>();
        for (String tableName : tables.values()) {
            if (AppIdHolder.tableSet.contains(tableName.toLowerCase())) {
                for (Map.Entry<String, String> entry : tables.entrySet()) {
                    if (entry.getValue().equals(tableName)) {
                        EqualsTo equalsTo = new EqualsTo();
                        equalsTo.withLeftExpression(new Column(entry.getKey() + "." + AppIdHolder.APP_ID_COLUMN_NAME));
                        //在sql 中增加参数
                        if (sqlSourceOriginal instanceof DynamicSqlSource) {
                            equalsTo.withRightExpression(new StringValue("${" + AppIdHolder.APP_ID_COLUMN_NAME + "}"));
                        } else {
                            equalsTo.withRightExpression(new JdbcParameter());
                        }
                        equalsTos.add(equalsTo);
                    }
                }
            }
        }
        if (equalsTos.size() > 0) {
            Iterator<EqualsTo> iterator = equalsTos.iterator();
            Expression expression = where;
            while (iterator.hasNext()) {
                AndExpression andExpression = new AndExpression();
                andExpression.withLeftExpression(expression).withRightExpression(iterator.next());
                expression = andExpression;
            }
            selectBody.setWhere(expression);
        }
        count += equalsTos.size();

        return count;
    }

    /**
     * 获取mybatis 去掉${} #{}后的sql
     *
     * @param sqlSourceOriginal
     * @param sqlOriginal
     * @param sqlReplaceStr
     * @return
     * @throws IllegalAccessException
     */
    private String getOriginnalSqlFromDynamicSqlSource(SqlSource sqlSourceOriginal, String sqlOriginal
            , List<String> sqlReplaceStr, MappedStatement mappedStatement, Object parameterObj) throws IllegalAccessException {

        Object sqlNode = rootSqlNodeFieldInDynamicSqlSource.get(sqlSourceOriginal);

        if(sqlNode instanceof  TextSqlNode){
            sqlOriginal = (String) textFieldInTextSqlNode.get(sqlNode);

            return getString(sqlOriginal, sqlReplaceStr);
        }else {
            DynamicContext context = new DynamicContext(mappedStatement.getConfiguration(), parameterObj);
            SqlNode rootSqlNode = (SqlNode) sqlNode;
            rootSqlNode.apply(context);
            sqlOriginal =  context.getSql();

            SqlSourceBuilder sqlSourceParser = new SqlSourceBuilder(mappedStatement.getConfiguration());
            Class<?> parameterType = parameterObj == null ? Object.class : parameterObj.getClass();
            SqlSource sqlSource = sqlSourceParser.parse(context.getSql(), parameterType, context.getBindings());
            BoundSql boundSql = sqlSource.getBoundSql(parameterObj);
            context.getBindings().forEach(boundSql::setAdditionalParameter);
            Map parameterObjMap = (Map) parameterObj;
            Map additionalParam = (Map) additionalParametersFieldInBoundSql.get(boundSql);
            parameterObjMap.putAll(additionalParam);
            return getString(sqlOriginal, sqlReplaceStr);
        }
    }

    private static String getString(String sqlOriginal, List<String> sqlReplaceStr) {
        //替换 ${} 为 ?
        Pattern pattern = Pattern.compile("[\\$\\#]\\{[^}]*\\}");
        Matcher matcher = pattern.matcher(sqlOriginal);
        while (matcher.find()) {
            sqlReplaceStr.add(matcher.group());
        }
        return matcher.replaceAll("\\?");
    }
}
