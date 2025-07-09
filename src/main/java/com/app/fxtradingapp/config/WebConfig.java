package com.app.fxtradingapp.config;


import com.app.fxtradingapp.middleware.VerifiedUserInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final VerifiedUserInterceptor verifiedUserInterceptor;

    public WebConfig(VerifiedUserInterceptor verifiedUserInterceptor) {
        this.verifiedUserInterceptor = verifiedUserInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(verifiedUserInterceptor)
                .addPathPatterns("/api/wallets/fund");
    }
}
