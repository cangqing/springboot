package com.cangqing.autoconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Created by shenjia on 2018/08/31.
 */
@Configuration
@ConditionalOnExpression("'${spring.autoconfig.ds.default.database}' != '' || '${spring.autoconfig.ds.default.url}' != ''")
@EnableConfigurationProperties
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
public class DataSourceConfigure {
    private static final Logger log = LoggerFactory.getLogger(DataSourceConfigure.class);

    // Empty string means no decrypting.
    @Value("${DRUID_DECRYPT_KEY:}")
    private String decryptKey = "";
    // Empty url means using props from configuration
    @Value("${JDBC_PROPS_URL:}")
    private String propsUrl = "";

    @Bean(name = { "datasource", "dataSource", "DataSource"})
    @ConditionalOnMissingBean(DataSource.class)
    @ConditionalOnExpression("'${spring.autoconfig.ds.default.database}' != '' || '${spring.autoconfig.ds.default.url}' != ''")
    @ConfigurationProperties("spring.autoconfig.ds.default")
    FactoryBean<DataSource> dataSource() {
        AutoDataSourceFactoryBean factoryBean = createSecureDataSourceFactoryBean();
        log.info("Init default DataSource bean (name: dataSource)");
        return factoryBean;
    }

    /** Create bean with default configuration */
    protected AutoDataSourceFactoryBean createSecureDataSourceFactoryBean() {
        return new AutoDataSourceFactoryBean(decryptKey, propsUrl);
    }
}
