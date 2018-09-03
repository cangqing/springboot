package com.cangqing.spider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MiscTest {

    @Test
    public void testDate() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar c = Calendar.getInstance();
        c.add(Calendar.YEAR, -1); //年份减1

        Date date = new Date(System.currentTimeMillis() - 60 * 1000 * 30);
        System.out.println(date);
        date = new Date(System.currentTimeMillis() - 3600 * 1000 * 10);
        System.out.println(date);
    }
}
