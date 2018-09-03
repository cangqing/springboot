package com.cangqing.spider;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.SQLException;

@SpringBootApplication
@EnableScheduling
@RestController
@MapperScan("com.cangqing.spider.mybatis.mapper")
public class GagSpiderApplication {

	//测试autoconfig
	@Autowired
	private DataSource dataSource;

	@RequestMapping(value = "/ds", method = RequestMethod.GET)
	public String datasource() throws SQLException {
		return dataSource.getConnection().getMetaData().getURL();
	}

	public static void main(String[] args) {
		SpringApplication.run(GagSpiderApplication.class, args);
	}
}
