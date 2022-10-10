package org.example;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class IndexController {

    @Resource
    CityMapper cityMapper;

    @Resource
    CityDAO cityDAO;

    @Resource
    OtherCityDAO otherCityDAO;

    @RequestMapping({"", "/"})
    public void index() throws Exception {


//        City city = new City();
//        city.setName("xx");
//        city.setState("xxxx");
//        cityDAO.insert(city);

        OtherCity otherCity = new OtherCity("11","222");
        otherCityDAO.insert(otherCity);


//        cityMapper.insertOne();

        City city = null;
        for (int i =0 ;i <10 ;i++) {
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
        cityMapper.insertFive(city);


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
