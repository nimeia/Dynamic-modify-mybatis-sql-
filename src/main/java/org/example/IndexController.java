package org.example;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Database;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import org.example.auto.AppIdHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;

@RestController
public class IndexController {

    @Resource
    CityMapper cityMapper;

    @Resource
    CityDAO cityDAO;

    @Resource
    OtherCityDAO otherCityDAO;

    @RequestMapping("sql")
    public void sql() throws JSQLParserException {
//        String sql = " select * from `mybatis-test`.city left join hospital h on city.name = h.name left join school s on city.appid " +
//                " = s.appid where city.name =? and city.name =? and city.name = ? and city.name = ? or city.top_id = ? ";

        String sql = " select * from city a ,city b where a.name = b.name and (a.top_id=? or a.name=?) ";

        Select select = (Select) CCJSqlParserUtil.parse(sql);
        PlainSelect selectBody = (PlainSelect) select.getSelectBody();

    }

    private Boolean modifySelect(PlainSelect selectBody) {
        FromItem fromItem = selectBody.getFromItem();

        List<Join> joins = selectBody.getJoins();

        Expression where = selectBody.getWhere();

        Map<String, String> tables = new LinkedHashMap<>();

        if (fromItem instanceof Table) {
            String name = ((Table) fromItem).getName();
            Alias alias = fromItem.getAlias();
            Database database = ((Table) fromItem).getDatabase();
            String schemaName = ((Table) fromItem).getSchemaName();
            Pivot pivot = fromItem.getPivot();
            tables.put(alias != null ? alias.getName() : (schemaName == null ? name : schemaName + "." + name), name);
        } else if (fromItem instanceof SubSelect) {
            PlainSelect selectBody1 = (PlainSelect) ((SubSelect) fromItem).getSelectBody();
            return modifySelect(selectBody1);
        }
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

        Boolean result = false;
        for (String tableName : tables.values()) {
            List<EqualsTo> equalsTos = new ArrayList<>();
            if (AppIdHolder.tableSet.contains(tableName)) {
                for (Map.Entry<String, String> entry : tables.entrySet()) {
                    if (entry.getValue().equals(tableName)) {
                        EqualsTo equalsTo = new EqualsTo();
                        equalsTo.withLeftExpression(new Column(entry.getKey() + "." + AppIdHolder.APP_ID_COLUMN_NAME));
                        equalsTo.withRightExpression(new JdbcParameter());
                        equalsTos.add(equalsTo);
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
                result = true;
            }
        }
        return result;
    }


    @Resource
    SchoolDAO schoolDAO;

    @RequestMapping({"", "/"})
    public void index() throws Exception {


//        City city = new City();
//        city.setName("xx");
//        city.setState("xxxx");
//        cityDAO.insert(city);

//        List<Map> asdf = cityMapper.selectCity("55555", "asdf");

//        List<Map> iidd = cityMapper.selectCityTwo("xxxx", "iidd");
//        List<Map> iidd = cityMapper.selectCityThree("xxxx", "iidd");
        City city = new City("sss", "55555");


//        cityMapper.selectCitySix(city);//会报错
//        cityMapper.selectCity7(city);
//        cityMapper.selectCity7(city,"appid");

//        cityMapper.selectCity8("xx",city);
//        cityMapper.selectCity8("xx",city,"appid");

        cityMapper.selectCity9("xx", city);
        cityMapper.selectCity9("xx", city, "appid");


//        cityMapper.selectCityFive(city);
//        cityMapper.selectCityFive(city, "appid");
//
////
//        cityMapper.selectCityThree("xxxx","appid");
//
//        cityMapper.selectCityFour(city);
//        cityMapper.selectCityFour(city,"appid");
////
//        SchoolExample schoolExample = new SchoolExample();
//        schoolExample.createCriteria().andNameEqualTo("1111").andAppidEqualTo("====");
//        schoolDAO.countByExample(schoolExample);
//        schoolDAO.countByExample(schoolExample,"appid");
//        schoolDAO.selectByExample(schoolExample);
//        schoolDAO.selectByExample(schoolExample,"appid");

        /*if (true) return;
        OtherCity otherCity = new OtherCity("11", "222");
        otherCityDAO.insert(otherCity);


//        cityMapper.insertOne();
        for (int i = 0; i < 10; i++) {
            city = new City();
            city.setName("55555");
            city.setState("444444");
            city.setTopId(11111);
            cityMapper.insertSix(city);
        }
//        city.setAppid("-----");

        cityMapper.insert(city);
        cityMapper.insertOne();

        cityMapper.insertTwo(city);

        cityMapper.insertThree(city);
        cityMapper.insertFour(city);
        cityMapper.insertFive(city)*/
        ;


//        cityMapper.insertThree(city);
//        OtherCity otherCity = new OtherCity("111","3333");
//        otherCity.setId(74);
//        otherCityDAO.updateByPrimaryKey(otherCity);


//        this.cityMapper.insert(new City("CC","CC"));

//        Insert insert = (Insert)CCJSqlParserUtil.parse("insert into city ( state, name) " +
//                "values ( ? , ? );");
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
//        System.out.println(insert);

//        this.cityMapper.insert(new City("CC","CC"));
    }
}
