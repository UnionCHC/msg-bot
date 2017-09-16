package com.desprice.unionchc.service;


import com.desprice.unionchc.Options;
import com.desprice.unionchc.entity.Config;
import com.desprice.unionchc.sqlite.SQLite;
import com.desprice.unionchc.telegram.BotTelegram;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.MvcFeature;
import org.glassfish.jersey.server.mvc.jsp.JspMvcFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;

import static com.desprice.unionchc.Utils.stackTrace;


public class Service implements Daemon {
    private final static Logger LOGGER = LoggerFactory.getLogger(Service.class);

    private HttpServer mServer;

    @Override
    public void init(DaemonContext context) throws Exception {
    }

    @Override
    public void start() throws Exception {
        try {

            // input handlers
            // output handlers

            final ResourceConfig resourceConfig = new ResourceConfig(TelegramRes.class);
            resourceConfig.register(JacksonFeature.class);

            // MVC.
            resourceConfig.register(MvcFeature.class);
            resourceConfig.register(JspMvcFeature.class);
            resourceConfig.register(MultiPartFeature.class);
            resourceConfig.property(JspMvcFeature.TEMPLATE_BASE_PATH, "/WEB-INF/jsp");


            Config config = Options.getInstance().getConfig();
            final URI uri = UriBuilder.fromUri("http://" + config.host)
                    .port(config.port)
                    .build();
            mServer = GrizzlyHttpServerFactory.createHttpServer(uri, resourceConfig, false);


          /*  WebappContext context = new WebappContext("WebappContext", "");
            // Initialize and register Jersey Servlet
            FilterRegistration registration = context.addFilter("ServletContainer",
                    ServletContainer.class);
            registration.setInitParameter("javax.ws.rs.Application",
                    WebApplication.class.getName());

            WebappContext context = new WebappContext("WebAppContext", "");
            // Initialize and register Jersey Servlet
            context.addFilter("ServletContainer", ServletContainer.class)
                    .setInitParameter("javax.ws.rs.Application", WebApplication.class.getName());
*/

            Runtime.getRuntime().addShutdownHook(new Thread(mServer::shutdownNow));
            mServer.start();
            LOGGER.debug(" Server start() ");

            SQLite sqlite = SQLite.getInstance();
            sqlite.checkTables();
            BotTelegram.init();


        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            LOGGER.error(stackTrace(ex));
        }

    }

    @Override
    public void stop() throws Exception {
        SQLite.getInstance().closeConnection();
        if (mServer != null) {
            mServer.shutdown();
            LOGGER.debug(" Server shutdown() ");
        }
    }

    @Override
    public void destroy() {
        SQLite.getInstance().closeConnection();
        if (mServer != null) {
            mServer.shutdown();
        }
    }
}
