package com.pinkkila.resourceserver.security;

import java.io.IOException;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.env.PropertySource;

public class MockWebServerPropertySource extends PropertySource<MockWebServer> implements DisposableBean {
    
    public static final String MOCK_WEB_SERVER_PROPERTY_SOURCE_NAME = "mockwebserver";
    
    private static final String NAME = "mockwebserver.url";
    
    private static final Log logger = LogFactory.getLog(MockWebServerPropertySource.class);
    
    private volatile boolean started;
    
    public MockWebServerPropertySource() {
        super(MOCK_WEB_SERVER_PROPERTY_SOURCE_NAME, new MockWebServer());
    }
    
    @Override
    public Object getProperty(String name) {
        if (!name.equals(NAME)) {
            return null;
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Looking up the url for '" + name + "'");
        }
        return getUrl();
    }
    
    @Override
    public void destroy() throws Exception {
        getSource().shutdown();
    }
    
    /**
     * Gets the URL (i.e. "http://localhost:123456")
     * @return the url with the dynamic port
     */
    private String getUrl() {
        MockWebServer mockWebServer = getSource();
        if (!this.started) {
            initializeMockWebServer(mockWebServer);
        }
        String url = mockWebServer.url("").url().toExternalForm();
        return url.substring(0, url.length() - 1);
    }
    
    private synchronized void initializeMockWebServer(MockWebServer mockWebServer) {
        if (this.started) {
            return;
        }
        Dispatcher dispatcher = new Dispatcher() {
            @Override
            @NonNull
            public MockResponse dispatch(RecordedRequest request) {
                if ("/.well-known/jwks.json".equals(request.getPath())) {
                    // Return the public part of the dynamic key
                    String jwks = "{\"keys\":[" + JwtTestHelper.RSA_KEY.toPublicJWK().toJSONString() + "]}";
                    return new MockResponse()
                            .setHeader("Content-Type", "application/json")
                            .setBody(jwks);
                }
                return new MockResponse().setResponseCode(404);
            }
        };
        
        mockWebServer.setDispatcher(dispatcher);
        try {
            mockWebServer.start();
            this.started = true;
        }
        catch (IOException ex) {
            throw new RuntimeException("Could not start " + mockWebServer, ex);
        }
    }
    
}
