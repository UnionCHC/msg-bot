package com.desprice.unionchc.service;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.jsp.JspMvcFeature;

import javax.ws.rs.ApplicationPath;


@ApplicationPath("/*")
public class WebApplication extends ResourceConfig {

    public WebApplication() {
        super(WebApplication.class);

        // MVC.
        //  register(MvcFeature.class);
        register(JspMvcFeature.class);

        register(TelegramRes.class);


    }


}
