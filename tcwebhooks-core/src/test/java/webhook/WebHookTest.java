package webhook;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.Test;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.auth.UsernamePasswordAuthenticator;
import webhook.teamcity.auth.WebHookAuthConfig;

import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;

import static org.junit.Assert.assertTrue;


public class WebHookTest {
    public String proxy = "127.0.0.1";
    public Integer proxyPort = 58002;
    String proxyPortString = "58002";
    public Integer webserverPort = 58001;
    public Integer proxyserverPort = 58002;
    public String webserverHost = "127.0.0.1";
    String url = "http://127.0.0.1:58001";

    public String proxyUsername = "foo";
    public String proxyPassword = "bar";

    TestingWebHookFactory factory = new TestingWebHookFactory();

    @Test
    public void test_BuildStates() {
        assertTrue(BuildStateEnum.BUILD_STARTED.getShortName().equals("buildStarted"));
        assertTrue(BuildStateEnum.BUILD_FINISHED.getShortName().equals("buildFinished"));
        assertTrue(BuildStateEnum.BEFORE_BUILD_FINISHED.getShortName().equals("beforeBuildFinish"));
        assertTrue(BuildStateEnum.RESPONSIBILITY_CHANGED.getShortName().equals("responsibilityChanged"));
        assertTrue(BuildStateEnum.BUILD_INTERRUPTED.getShortName().equals("buildInterrupted"));


    }


    @Test
    public void test_ProxyPort() {
        WebHook W = factory.getWebHook(url, proxy, proxyPort);
        assertTrue(W.getProxyPort() == proxyPort);
    }

    @Test
    public void test_ProxyHost() {
        WebHook W = factory.getWebHook(url, proxy, proxyPort);
        assertTrue(W.getProxyHost() == proxy);
    }

    @Test
    public void test_URL() {
        WebHook W = factory.getWebHook(url, proxy, proxyPort);
        assertTrue(W.getUrl() == url);
    }

    @Test(expected = java.io.FileNotFoundException.class)
    public void test_FileNotFoundExeption() throws FileNotFoundException, IOException {
        System.out.print("Testing for FileNotFound exception");
        WebHook w = factory.getWebHook(url, proxy, proxyPort);
        w.setFilename("WebHooks/src/test/resources/fileWithDoesNotExist.txt");
        w.setEnabled(true);
        w.post();
        System.out.print(".. done");
    }

    @Test(expected = java.net.ConnectException.class)
    public void test_ConnectionRefused() throws ConnectException, IOException {
        WebHook w = factory.getWebHook(url);
        w.setEnabled(true);
        w.post();
    }

    @Test(expected = java.io.IOException.class)
    public void test_IOExeption() throws IOException {
        System.out.println("Testing for IO exception");
        WebHook w = factory.getWebHook(url, "localhost", proxyPort);
        w.setEnabled(true);
        w.post();
        System.out.print(".. done");
    }

    @Test
    public void test_200() throws FileNotFoundException, IOException, Exception {
        WebHookTestServer s = startWebServer();
        WebHook w = factory.getWebHook(url + "/200");
        w.addParam("buildID", "foobar");
        w.addParam("notifiedFor", "someUser");
        w.addParam("buildResult", "failed");
        w.addParam("triggeredBy", "Subversion");
        w.setEnabled(true);
        w.post();
        System.out.println(w.getUrl());
        System.out.println(w.getContent());
        stopWebServer(s);
        assertTrue(w.getStatus() == HttpStatus.SC_OK);
    }

    @Test
    public void test_401WithoutAuth() throws FileNotFoundException, IOException, Exception {
        WebHookTestServer s = startWebServer();
        WebHook w = factory.getWebHook(url + "/auth/200");
        w.addParam("buildID", "foobar");
        w.addParam("notifiedFor", "someUser");
        w.addParam("buildResult", "failed");
        w.addParam("triggeredBy", "Subversion");
        w.setEnabled(true);
        w.post();
        System.out.println(w.getContent());
        stopWebServer(s);
        assertTrue(w.getStatus() == HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void test_200WithAuthButWrongRealm() throws FileNotFoundException, IOException, Exception {
        WebHookTestServer s = startWebServer();
        WebHook w = factory.getWebHook(url + "/auth/200");
        UsernamePasswordAuthenticator authenticator = new UsernamePasswordAuthenticator();
        WebHookAuthConfig authConfig = new WebHookAuthConfig();
        authConfig.type = "userPass";
        authConfig.preemptive = false;
        authConfig.parameters.put("username", "user1");
        authConfig.parameters.put("password", "user1pass");
        authConfig.parameters.put("realm", "realmywealmy");
        authenticator.setWebHookAuthConfig(authConfig);
        w.setAuthentication(authenticator);

        w.addParam("buildID", "foobar");
        w.addParam("notifiedFor", "someUser");
        w.addParam("buildResult", "failed");
        w.addParam("triggeredBy", "Subversion");
        w.setEnabled(true);
        w.post();
        System.out.println(w.getContent());
        stopWebServer(s);
        assertTrue(w.getStatus() == HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void test_200WithAuthPreemptionButWrongRealm() throws FileNotFoundException, IOException, Exception {
        WebHookTestServer s = startWebServer();
        WebHook w = factory.getWebHook(url + "/auth/200");
        UsernamePasswordAuthenticator authenticator = new UsernamePasswordAuthenticator();
        WebHookAuthConfig authConfig = new WebHookAuthConfig();
        authConfig.type = "userPass";
        authConfig.parameters.put("username", "user1");
        authConfig.parameters.put("password", "user1pass");
        authConfig.parameters.put("realm", "realmywealmy");
        authenticator.setWebHookAuthConfig(authConfig);
        w.setAuthentication(authenticator);

        w.addParam("buildID", "foobar");
        w.addParam("notifiedFor", "someUser");
        w.addParam("buildResult", "failed");
        w.addParam("triggeredBy", "Subversion");
        w.setEnabled(true);
        w.post();
        System.out.println(w.getContent());
        stopWebServer(s);
        assertTrue(w.getStatus() == HttpStatus.SC_OK);
    }

    @Test
    public void test_200WithAuth() throws FileNotFoundException, IOException, Exception {
        WebHookTestServer s = startWebServer();
        WebHook w = factory.getWebHook(url + "/auth/200");
        UsernamePasswordAuthenticator authenticator = new UsernamePasswordAuthenticator();
        WebHookAuthConfig authConfig = new WebHookAuthConfig();
        authConfig.type = "userPass";
        authConfig.parameters.put("username", "user1");
        authConfig.parameters.put("password", "user1pass");
        authenticator.setWebHookAuthConfig(authConfig);
        w.setAuthentication(authenticator);

        w.addParam("buildID", "foobar");
        w.addParam("notifiedFor", "someUser");
        w.addParam("buildResult", "failed");
        w.addParam("triggeredBy", "Subversion");
        w.setEnabled(true);
        w.post();
        System.out.println(w.getContent());
        stopWebServer(s);
        assertTrue(w.getStatus() == HttpStatus.SC_OK);
    }

    @Test
    public void test_NotEnabled() throws FileNotFoundException, IOException, InterruptedException {
        WebHookTestServer s = startWebServer();
        WebHook w = factory.getWebHook(url + "/200", proxy, proxyPort);
        w.post();
        stopWebServer(s);
        assertTrue(w.getStatus() == null);
    }

    @Test
    public void test_200WithProxy() throws FileNotFoundException, IOException, InterruptedException {
        WebHookTestServer s = startWebServer();
        WebHookTestProxyServer p = startProxyServer();
        WebHook w = factory.getWebHook(url + "/200", proxy, proxyPort);
        w.setEnabled(true);
        w.post();
        stopWebServer(s);
        stopProxyServer(p);
        assertTrue(w.getStatus() == HttpStatus.SC_OK);
    }

    @Test
    public void test_200WithProxyFailAuth() throws FileNotFoundException, IOException, InterruptedException {
        WebHookTestServer s = startWebServer();
        WebHookTestProxyServer p = startProxyServerAuth(proxyUsername, proxyPassword);
        WebHook w = factory.getWebHook(url + "/200", proxy, proxyPort);
        w.setEnabled(true);
        w.post();
        stopWebServer(s);
        stopProxyServer(p);
        assertTrue(w.getStatus() == HttpServletResponse.SC_PROXY_AUTHENTICATION_REQUIRED);
    }

    @Test
    public void test_200WithProxyAuth() throws FileNotFoundException, IOException, InterruptedException {
        WebHookTestServer s = startWebServer();
        WebHookTestProxyServer p = startProxyServerAuth(proxyUsername, proxyPassword);
        WebHook w = factory.getWebHook(url + "/200", proxy, proxyPort);
        w.setEnabled(true);
        w.setProxyUserAndPass(proxyUsername, proxyPassword);
        w.post();
        stopWebServer(s);
        stopProxyServer(p);
        assertTrue(w.getStatus() == HttpStatus.SC_OK);
    }


    @Test
    public void test_200WithFilename() throws FileNotFoundException, IOException, Exception {
        WebHookTestServer s = startWebServer();
        WebHook w = factory.getWebHook(url + "/200");
        w.setFilename("src/test/resources/FileThatDoesExist.txt");
        w.setEnabled(true);
        w.post();
        stopWebServer(s);
        assertTrue(w.getStatus() == HttpStatus.SC_OK);
    }

    @Test
    public void test_200WithFilenameWithProxy() throws FileNotFoundException, IOException, Exception {
        WebHookTestServer s = startWebServer();
        WebHookTestProxyServer p = startProxyServer();
        WebHook w = factory.getWebHook(url + "/200", proxy, proxyPort);
        w.setFilename("src/test/resources/FileThatDoesExist.txt");
        w.setEnabled(true);
        w.post();
        stopWebServer(s);
        stopProxyServer(p);
        assertTrue(w.getStatus() == HttpStatus.SC_OK);

    }

    @Test
    public void test_302() throws FileNotFoundException, IOException, Exception {
        WebHookTestServer s = startWebServer();
        WebHook w = factory.getWebHook(url + "/302");
        w.setEnabled(true);
        w.post();
        stopWebServer(s);
        assertTrue(w.getStatus() == HttpStatus.SC_MOVED_TEMPORARILY);

    }

    @Test
    public void test_302WithAuth() throws FileNotFoundException, IOException, Exception {
        WebHookTestServer s = startWebServer();
        WebHook w = factory.getWebHook(url + "/auth/302");

        UsernamePasswordAuthenticator authenticator = new UsernamePasswordAuthenticator();
        WebHookAuthConfig authConfig = new WebHookAuthConfig();
        authConfig.type = "userPass";
        authConfig.parameters.put("username", "user1");
        authConfig.parameters.put("password", "user1pass");
        authenticator.setWebHookAuthConfig(authConfig);
        w.setAuthentication(authenticator);

        w.setEnabled(true);
        w.post();
        stopWebServer(s);
        assertTrue("Expecting " + HttpStatus.SC_MOVED_TEMPORARILY + " but was " + w.getStatus(), w.getStatus() == HttpStatus.SC_MOVED_TEMPORARILY);

    }


    @Test
    public void test_302WithProxy() throws FileNotFoundException, IOException, InterruptedException {
        WebHookTestServer s = startWebServer();
        WebHookTestProxyServer p = startProxyServer();
        WebHook w = factory.getWebHook(url + "/302", proxy, proxyPort);
        w.setEnabled(true);
        w.post();
        stopWebServer(s);
        stopProxyServer(p);
        assertTrue(w.getStatus() == HttpStatus.SC_MOVED_TEMPORARILY);

    }

    @Test
    public void test_404WithProxyStringPort() throws FileNotFoundException, IOException, InterruptedException {
        WebHookTestServer s = startWebServer();
        WebHookTestProxyServer p = startProxyServer();
        WebHook w = factory.getWebHook(url + "/404", proxy, proxyPortString);
        w.setEnabled(true);
        w.post();
        stopWebServer(s);
        stopProxyServer(p);
        assertTrue(w.getStatus() == HttpStatus.SC_NOT_FOUND);

    }

    @Test
    public void test_404WithProxyConfig() throws FileNotFoundException, IOException, InterruptedException {
        WebHookTestServer s = startWebServer();
        WebHookTestProxyServer p = startProxyServer();
        WebHookProxyConfig pc = new WebHookProxyConfig(proxy, Integer.parseInt(proxyPortString));
        WebHook w = factory.getWebHook(url + "/404", pc);
        w.setEnabled(true);
        w.post();
        stopWebServer(s);
        stopProxyServer(p);
        assertTrue(w.getStatus() == HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void test_404WithAuth() throws FileNotFoundException, IOException, InterruptedException {
        WebHookTestServer s = startWebServer();
        WebHook w = factory.getWebHook(url + "/404");
        w.setEnabled(true);
        w.post();
        stopWebServer(s);
        assertTrue(w.getStatus() == HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void test_302WithFilename() throws FileNotFoundException, IOException, Exception {
        WebHookTestServer s = startWebServer();
        WebHook w = factory.getWebHook(url + "/302");
        w.setFilename("src/test/resources/FileThatDoesExist.txt");
        w.setEnabled(true);
        w.post();
        stopWebServer(s);
        assertTrue(w.getStatus() == HttpStatus.SC_MOVED_TEMPORARILY);

    }

    @Test
    public void test_302WithFilenameWithProxy() throws FileNotFoundException, IOException, Exception {
        WebHookTestServer s = startWebServer();
        WebHookTestProxyServer p = startProxyServer();
        WebHook w = factory.getWebHook(url + "/302", proxy, proxyPort);
        w.setFilename("src/test/resources/FileThatDoesExist.txt");
        w.setEnabled(true);
        w.post();
        stopWebServer(s);
        stopProxyServer(p);
        assertTrue(w.getStatus() == HttpStatus.SC_MOVED_TEMPORARILY);

    }

    public WebHookTestServer startWebServer() {
        try {
            WebHookTestServer s = new WebHookTestServer(webserverHost, webserverPort);
            s.server.start();
            return s;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void stopWebServer(WebHookTestServer s) throws InterruptedException {
        try {
            s.server.stop();
            // Sleep to let the server shutdown cleanly.
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Thread.sleep(1000);
        }
    }

    public WebHookTestProxyServer startProxyServer() {
        try {
            WebHookTestProxyServer p = new WebHookTestProxyServer(webserverHost, proxyserverPort);
            p.server.start();
            return p;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public WebHookTestProxyServer startProxyServerAuth(String username, String password) {
        try {
            WebHookTestProxyServer p = new WebHookTestProxyServer(webserverHost, proxyserverPort,
                    username, password);
            p.server.start();
            return p;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void stopProxyServer(WebHookTestProxyServer p) {
        try {
            p.server.stop();
            // Sleep to let the server shutdown cleanly.
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class TestingWebHookFactory {
        public WebHook getWebHook() {
            return new WebHookImpl();
        }

        public WebHook getWebHook(String url, String proxy, Integer proxyPort) {
            return new WebHookImpl(url, proxy, proxyPort, new HttpClient());
        }

        public WebHook getWebHook(String url) {
            return new WebHookImpl(url, new HttpClient());
        }

        public WebHook getWebHook(String url, String proxy, String proxyPort) {
            return new WebHookImpl(url, proxy, proxyPort, new HttpClient());
        }

        public WebHook getWebHook(String url, WebHookProxyConfig proxyConfig) {
            return new WebHookImpl(url, proxyConfig, new HttpClient());
        }
    }
}
