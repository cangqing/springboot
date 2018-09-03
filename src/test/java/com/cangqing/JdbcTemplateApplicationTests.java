package com.cangqing;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JdbcTemplateApplicationTests {

    @Test
    public void contextLoads() {
    }

    @Autowired
    @Qualifier("primaryJdbcTemplate")
    protected JdbcTemplate jt1;

    @Autowired
    @Qualifier("secondaryJdbcTemplate")
    protected JdbcTemplate jt2;

    @Before
    public void setUp() {

    }

    @Test
    public void test() {
        System.out.println(jt1.queryForList("select * FROM bill_stat limit 10"));
        System.out.println(jt2.queryForList("select * FROM bill_stat limit 10"));
    }
}
