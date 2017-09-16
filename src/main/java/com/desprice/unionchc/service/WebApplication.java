package com.desprice.unionchc.service;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.TracingConfig;
import org.glassfish.jersey.server.mvc.MvcFeature;
import org.glassfish.jersey.server.mvc.jsp.JspMvcFeature;

import javax.ws.rs.ApplicationPath;


@ApplicationPath("/*")
public class WebApplication extends ResourceConfig {

    public WebApplication() {
        super(WebApplication.class);

        packages(true, "com.desprice.unionchc.resources");
        register(JacksonFeature.class);

        // MVC.
        register(MvcFeature.class);
        register(JspMvcFeature.class);
        register(MultiPartFeature.class);
        property(JspMvcFeature.TEMPLATE_BASE_PATH, "/WEB-INF/jsp");

        // Logging.
        register(LoggingFeature.class);

        // Tracing support.
        property(ServerProperties.TRACING, TracingConfig.ON_DEMAND.name());


    }


}
