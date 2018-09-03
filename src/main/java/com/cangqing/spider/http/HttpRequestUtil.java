package com.cangqing.spider.http;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.Args;
import org.apache.http.util.ByteArrayBuffer;
import org.mozilla.universalchardet.UniversalDetector;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by song on 2016/11/30.
 */
public class HttpRequestUtil {
    static PoolingHttpClientConnectionManager connectionManager;
    /***
     *
     * 最大连接数就是连接池允许的最大连接数，最大路由连接数就是没有路由站点的最大连接数,如果要连接的url只有一个，两个必须配置成一样，否则只会取最小值。
     httpclient设置的最大连接数绝对不能超过tomcat设置的最大连接数
     */
    /**
     * 最大连接数
     */
    public final static int MAX_TOTAL_CONNECTIONS = 200;
    /**
     * 每个路由最大连接数
     */
    public final static int MAX_ROUTE_CONNECTIONS = 200;
    /**
     * 获取连接的最大等待时间
     */
    public final static int WAIT_TIMEOUT = 60000;
    /**
     * 连接超时时间
     */
    public final static int CONNECT_TIMEOUT = 30000;
    /**
     * 读取超时时间
     */
    public final static int READ_TIMEOUT = 60000;

    static {
        try {
            SSLContext ctx = SSLContext.getInstance("SSL");

            // 使用TrustManager来初始化该上下文，TrustManager只是被SSL的Socket所使用
            ctx.init(null, new TrustManager[]{new X509TrustManager() {

                public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                }

                public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                }

                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }
            }, null);
            SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(
                    ctx);
            //创建可用Scheme
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create().
                    register("http", PlainConnectionSocketFactory.INSTANCE).
                    register("https", socketFactory).build();
            //创建ConnectionManager，添加Connection配置信息
            connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            connectionManager.setMaxTotal(MAX_TOTAL_CONNECTIONS);
            connectionManager.setDefaultMaxPerRoute(MAX_ROUTE_CONNECTIONS);
        } catch (Exception e) {

        }
    }

    public static HttpUriRequest buildSimpleGetRequest(String url) {
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        httpGet.setHeader("Accept-Encoding", "gzip, deflate");
        httpGet.setHeader("Content-Type", "text/xml;charset=UTF-8");
        httpGet.setHeader("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6");
        httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.101 Safari/537.36");
        return httpGet;
    }

    public static HttpUriRequest buildGetRequest(String url, Map<String, String> headers) {
        HttpGet httpGet = new HttpGet(url);
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            httpGet.setHeader(entry.getKey(), entry.getValue());
        }
        return httpGet;
    }

    public static CloseableHttpResponse submitRequestInternal(HttpUriRequest uriRequest, boolean useProxy) throws IOException {
        HttpHost proxyHost = new HttpHost(proxyServer, proxyPort, "HTTP");
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        AuthScope authScope = new AuthScope(proxyHost.getHostName(), proxyHost.getPort(), AuthScope.ANY_SCHEME);
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(proxyUser, proxyPass);
        credentialsProvider.setCredentials(authScope, credentials);
        //InetSocketAddress addr = new InetSocketAddress(proxyServer, proxyPort);
        RequestConfig requestConfig = RequestConfig.custom().
                setConnectionRequestTimeout(WAIT_TIMEOUT)
                .setConnectTimeout(CONNECT_TIMEOUT)
                .setSocketTimeout(READ_TIMEOUT)
                .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                .build();
        //Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);
        HttpClientBuilder httpClientBuilder = HttpClients.custom()
                .setConnectionManager(connectionManager)
                //.setRoutePlanner(routePlanner)
                //.setRetryHandler(new DefaultHttpRequestRetryHandler())
                .setDefaultRequestConfig(requestConfig);
        if (useProxy) {
            httpClientBuilder.setProxy(proxyHost);
            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        }
        CloseableHttpClient httpClient = httpClientBuilder.build();
        CloseableHttpResponse httpResponse = httpClient.execute(uriRequest, HttpClientContext.create());
        return httpResponse;
    }

    public static Object submitRequest(HttpUriRequest uriRequest,boolean useProxy) throws IOException {
        RequestCallBack callBack = new RequestCallBack() {
            public Object parse(String content) {
                return content;
            }
        };
        return submitRequest(uriRequest, callBack,useProxy);
    }

    static String toContent(final HttpEntity entity, final Charset defaultCharset) throws IOException {
        Args.notNull(entity, "Entity");
        final InputStream instream = entity.getContent();
        if (instream == null) {
            return null;
        }
        Charset charset = null;
        try {
            final ContentType contentType = ContentType.get(entity);
            if (contentType != null) {
                charset = contentType.getCharset();
            }
        } catch (final UnsupportedCharsetException ex) {
            if (defaultCharset == null) {
                throw new UnsupportedEncodingException(ex.getMessage());
            }
        }
        Args.check(entity.getContentLength() <= Integer.MAX_VALUE,
                "HTTP entity too large to be buffered in memory");
        int i = (int) entity.getContentLength();
        if (i < 0) {
            i = 4096;
        }
        final ByteArrayBuffer buffer = new ByteArrayBuffer(i);
        final byte[] tmp = new byte[1024];
        UniversalDetector encDetector = new UniversalDetector(null);
        int l;
        while ((l = instream.read(tmp)) != -1) {
            buffer.append(tmp, 0, l);
            if (!encDetector.isDone()) {
                encDetector.handleData(tmp, 0, l);
            }
        }
        encDetector.dataEnd();
        if (charset == null) {
            String encoding = encDetector.getDetectedCharset();
            if (encoding != null) {
                charset = Charset.forName(encoding);
            }
        }
        encDetector.reset();
        if (charset == null) {
            charset = defaultCharset;
        }
        if (charset == null) {
            charset = HTTP.DEF_CONTENT_CHARSET;
        }
        return new String(buffer.toByteArray(), charset);
    }

    public static Object submitRequest(HttpUriRequest uriRequest, RequestCallBack callBack,boolean useProxy) throws IOException {
        CloseableHttpResponse httpResponse = null;
        try {
            httpResponse = submitRequestInternal(uriRequest,useProxy);
            HttpEntity entity = httpResponse.getEntity();
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                String content = toContent(entity, Charset.forName("UTF-8"));
                return callBack.parse(content);
            } else {
                throw new RuntimeException("response status code:" + httpResponse.getStatusLine().getStatusCode());
            }
        } finally {
            if (httpResponse != null)
                httpResponse.close();
        }
    }

    public static Object submitRequest(String url,boolean useProxy) {
        HttpUriRequest uriRequest = buildSimpleGetRequest(url);
        try {
            return submitRequest(uriRequest,useProxy);
        } catch (Exception e) {
            throw new RuntimeException(String.format("request %s failed,errorMsg:%s", url, e.getMessage()));
        }
    }

    public static Object submitRequestWithHeaders(String url, boolean useProxy,Map<String, String> headers) {
        Random random = new Random();
        String userAgent = String.format("%s", random.nextInt(10000000));
        headers.put("User-Agent", userAgent);
        HttpUriRequest uriRequest = buildGetRequest(url, headers);
        try {
            return submitRequest(uriRequest,useProxy);
        } catch (Exception e) {
            throw new RuntimeException(String.format("request %s failed,errorMsg:%s", url, e.getMessage()));
        }
    }

    /****
     * 代理服务器
     * http://ip.hahado.cn/help/sapi/#2curl
     */
    static String proxyServer = "ip4.hahado.cn";
    public static int proxyPort = 32918;
    // 代理隧道验证信息
    public static String proxyUser = "ydzicycrgu";
    public static String proxyPass = "AkI3OByg6QYKJ";

    //// TODO: 2017/9/28 可以做全局代理
    public static String httpResponse(String targetUrl, Map<String, String> headers) throws Exception {
        URL url = new URL(targetUrl);
        // 创建代理服务器地址对象
        InetSocketAddress addr = new InetSocketAddress(proxyServer, proxyPort);
        // 创建HTTP类型代理对象
        Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);

        // 设置通过代理访问目标页面
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection(proxy);
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setRequestProperty("Content-type", "text/html");
        httpURLConnection.setRequestProperty("Accept-Charset", "utf-8");
        httpURLConnection.setRequestProperty("contentType", "utf-8");
        httpURLConnection.setRequestProperty("Host", "www.dianping.com");
        httpURLConnection.setRequestProperty("User-Agent", "" + (new Random().nextInt(10000)));
        // 设置IP切换头
        httpURLConnection.setRequestProperty("Proxy-Switch-Ip", "yes");
        Authenticator authenticator = new Authenticator() {
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(proxyUser, proxyPass.toCharArray());
            }
        };
        Authenticator.setDefault(authenticator);
        httpURLConnection.setRequestMethod("GET");
        // 解析返回数据
        byte[] response = readStream(httpURLConnection.getInputStream());
        return new String(response, "UTF-8");
    }

    public static byte[] readStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = -1;

        while ((len = inStream.read(buffer)) != -1) {
            outSteam.write(buffer, 0, len);
        }
        outSteam.close();
        inStream.close();
        return outSteam.toByteArray();
    }

    public static void main(String[] args) {
        String content = (String) submitRequest("http://jskp.jss.com.cn/nuonuo/invoice/findInvoiceMake.action?paramName=%E6%9D%AD%E5%B7%9E%E4%B9%9D%E9%98%B3%E8%B1%86%E4%B8%9A%E6%9C%89%E9%99%90%E5%85%AC%E5%8F%B8&counter=2&source=0",false);
        System.out.println(content);
    }
}