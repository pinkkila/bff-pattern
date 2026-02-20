package com.pinkkila.resourceserver.mockauthserver;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;

public class MockWebServerEnvironmentPostProcessor implements EnvironmentPostProcessor {
    
    private final MockWebServerPropertySource propertySource = new MockWebServerPropertySource();
    
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        environment.getPropertySources().addFirst(this.propertySource);
        application.addInitializers(context -> 
                context.getBeanFactory().registerSingleton("mockWebServerPropertySource", this.propertySource));
    }
    
}
