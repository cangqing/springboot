package com.cangqing.autoconfig;

import com.alibaba.fastjson.JSON;

import com.alibaba.fastjson.JSON;
import com.github.rholder.retry.*;

import javax.net.ssl.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/*****
 Project gag-spider
 @Author shenjia
 @Date 2018/8/31
 *****/

public class RestUtil {
    private static final SSLSocketFactory trustAllSSLSocketFactory;

    static {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };

        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            trustAllSSLSocketFactory = sc.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private RestUtil() {

    }

    public static <T> T getWithRetry(String str, int timeout, Class<T> clazz) throws IOException {
        Retryer<T> retryer = RetryerBuilder.<T>newBuilder()
                .retryIfExceptionOfType(IOException.class)
                .withStopStrategy(StopStrategies.stopAfterAttempt(3))
                .withWaitStrategy(WaitStrategies.fixedWait(300L, TimeUnit.MILLISECONDS))
                .build();

        try {
            return retryer.call(() -> get(str, timeout, clazz));
        } catch (ExecutionException e) {
            throw new IOException(e);
        } catch (RetryException e) {
            throw new IOException(e);
        }
    }

    public static <T> T get(String str, int timeout, Class<T> clazz) throws IOException {
        final HttpsURLConnection conn = (HttpsURLConnection) URI.create(str).toURL().openConnection();

        conn.setSSLSocketFactory(trustAllSSLSocketFactory);
        conn.setConnectTimeout(timeout);
        conn.setReadTimeout(timeout);
        conn.setDoOutput(false);
        conn.setDoInput(true);
        conn.setHostnameVerifier((s, sslSession) -> true);

        try {
            final int code = conn.getResponseCode();
            if (code != 200) throw new IOException(conn.getResponseMessage());
            final InputStream input = conn.getInputStream();
            return JSON.parseObject(readAll(input), clazz);
        } finally {
            conn.disconnect();
        }
    }

    private static byte[] readAll(InputStream input) throws IOException {
        try {
            final ByteArrayOutputStream output = new ByteArrayOutputStream();
            copy(input, output);
            return output.toByteArray();
        } finally {
            input.close();
        }
    }

    private static long copy(InputStream input, OutputStream output) throws IOException {
        long nread = 0L;
        byte[] buf = new byte[1024];
        int n;
        while ((n = input.read(buf)) > 0) {
            output.write(buf, 0, n);
            nread += n;
        }
        return nread;
    }
}
