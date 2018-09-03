package com.cangqing.autoconfig;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import javax.sql.DataSource;
import java.text.MessageFormat;
import java.util.List;

/*****
 Project gag-spider
 @Author shenjia
 @Date 2018/8/31
 *****/

public class AutoDataSourceFactoryBean extends AbstractFactoryBean<DataSource> {
    private static final Logger log = LoggerFactory.getLogger(AutoDataSourceFactoryBean.class);

    private static final int HTTP_TIMEOUT = 3000;

    private final String decryptKey;
    private final String propsUrl;

    private String database;
    private String urlPattern = "jdbc:mysql://{0}/{1}";
    private String urlParams;

    private String url;
    private String username;
    private String password;

    private int initialSize = 1;
    private int minIdle = 1;
    private int maxActive = 10;
    private int maxWait = 60000;
    private int timeBetweenEvictionRunsMillis = 60000;
    private int minEvictableIdleTimeMillis = 300000;
    private String validationQuery = "select 1";
    private boolean testWhileIdle = true;
    private boolean testOnBorrow = false;
    private boolean testOnReturn = false;
    private boolean poolPreparedStatements = true;
    private int maxPoolPreparedStatementPerConnectionSize = 10;

    private boolean removeAbandoned = false;
    private long removeAbandonedTimeoutMillis = 300000;
    private boolean logAbandoned = false;
    private List<String> connectionInitSqls;

    private String filters = "stat";

    public AutoDataSourceFactoryBean(String decryptKey, String propsUrl) {
        this.decryptKey = decryptKey;
        this.propsUrl = propsUrl;
    }

    @Override
    public Class<?> getObjectType() {
        return DataSource.class;
    }

    @Override
    public DataSource createInstance() throws Exception {
        final DruidDataSource source = initDruidDataSource();

        source.setInitialSize(initialSize);
        source.setMinIdle(minIdle);
        source.setMaxActive(maxActive);
        source.setMaxWait(maxWait);
        source.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        source.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        source.setValidationQuery(validationQuery);
        source.setTestWhileIdle(testWhileIdle);
        source.setTestOnBorrow(testOnBorrow);
        source.setTestOnReturn(testOnReturn);
        source.setPoolPreparedStatements(poolPreparedStatements);
        source.setMaxPoolPreparedStatementPerConnectionSize(maxPoolPreparedStatementPerConnectionSize);

        source.setRemoveAbandoned(removeAbandoned);
        source.setRemoveAbandonedTimeoutMillis(removeAbandonedTimeoutMillis);
        source.setLogAbandoned(logAbandoned);
        source.setConnectionInitSqls(connectionInitSqls);

        source.addFilters(filters);

        source.init();
        return source;
    }

    private DruidDataSource initDruidDataSource() throws Exception {
        final DruidDataSource source = new DruidDataSource();

        if (StringUtils.isNotEmpty(url) && StringUtils.isNotEmpty(username) && (StringUtils.isEmpty(database) || StringUtils.isEmpty(propsUrl))) {
            // url + username + password
            log.info("Get DataSource info from local [{}]", url);
            source.setUrl(url);
            source.setUsername(username);
            source.setPassword(password);
        } else {
            if (StringUtils.isEmpty(database) || StringUtils.isEmpty(propsUrl) || StringUtils.isEmpty(decryptKey)) {
                throw new IllegalArgumentException("[Assertion failed] - (database, propsUrl, decryptKey) " +
                        "parameters is required; it must not be null");
            }

            String remoteUrl = propsUrl + "/" + database + "/v2";
            log.info("Get DataSource info from remote strongbox [{}]", remoteUrl);
            JdbcProperties props;
            try {
                props = RestUtil.getWithRetry(remoteUrl, HTTP_TIMEOUT, JdbcProperties.class);
            } catch (Exception e){
                String err = String.format("cause : may be is missing database %s or not safe period get user/pwd", database);
                log.error(err);
                throw new Exception(err, e);
            }
            source.setUrl(this.buildUrl(props));
            source.setUsername(props.getUsername());
            source.setPassword(props.getPassword());
            // enable decrypt
            source.addFilters("config");
            source.setConnectionProperties("config.decrypt=true;config.decrypt.key=" + decryptKey);
            source.setName(escapeName(database));
        }

        return source;
    }

    /** escape name for jmx bean **/
    private static String escapeName(String name) {
        return name.replace(':', '_');
    }

    private static String trimPrefix(String name) {
        final int i = name.indexOf(":");
        return i > -1 ? name.substring(i + 1) : name;
    }

    private String buildUrl(JdbcProperties props) {
        String url = trimPrefix(props.getName()) + (StringUtils.isNotEmpty(urlParams) ? '?' + urlParams : "");
        return MessageFormat.format(urlPattern, props.getAddress(), url);
    }

    public void setDatabase(String database) {
        this.database = database;
    }
}
