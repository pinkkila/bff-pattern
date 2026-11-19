package com.pinkkila.bff.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DefaultController {
    
    @Value("${app.spa-uri}")
    private String appSpaUri;
    
    @GetMapping("/")
    public String root() {
        return "redirect:" + this.appSpaUri;
    }
    
}
