package org.example;

import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitor;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.expression.operators.relational.NamedExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.SubSelect;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@MapperScan(basePackages = {"org.example.**"})
public class SampleMybatisApplication implements CommandLineRunner {

    private final CityMapper cityMapper;

    public SampleMybatisApplication(CityMapper cityMapper) {
        this.cityMapper = cityMapper;
    }

    public static void main(String[] args) {
        SpringApplication.run(SampleMybatisApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
//        System.out.println(this.cityMapper.findByState("CA"));
//        this.cityMapper.insert(new City("CC","CC"));
//        System.exit(0);
    }



}