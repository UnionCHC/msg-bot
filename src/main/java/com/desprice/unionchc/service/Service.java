package com.desprice.unionchc.service;


import com.desprice.unionchc.EthereumSer;
import com.desprice.unionchc.Options;
import com.desprice.unionchc.entity.Config;
import com.desprice.unionchc.sqlite.SQLite;
import com.desprice.unionchc.telegram.BotTelegram;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.glassfish.grizzly.http.server.CLStaticHttpHandler;
import org.glassfish.grizzly.http.server.HttpHandler;
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

import static com.desprice.unionchc.Utils.logException;

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

            Config config = Options.getInstance().getConfig();
            final URI uri = UriBuilder.fromUri("http://" + config.host)
                    .port(config.port)
                    .build();
            mServer = GrizzlyHttpServerFactory.createHttpServer(uri, resourceConfig, false);

            HttpHandler httpHandler = new CLStaticHttpHandler(HttpServer.class.getClassLoader(), "/web/", "/html/");
            mServer.getServerConfiguration().addHttpHandler(httpHandler, "/res");

            System.out.println(uri.toString());


            Runtime.getRuntime().addShutdownHook(new Thread(mServer::shutdownNow));
            mServer.start();
            LOGGER.debug(" Server start() ");

            SQLite sqlite = SQLite.getInstance();
            sqlite.checkTables();
            BotTelegram.init();
            EthereumSer.getInstance().setSubscribe(Options.getInstance().getAddress(), Options.getInstance().getContract());


        } catch (IOException ex) {
            logException(ex);
        } catch (Exception ex) {
            logException(ex);
        }

    }

    @Override
    public void stop() throws Exception {
        EthereumSer.getInstance().unSubscribe();
        SQLite.getInstance().closeConnection();
        if (mServer != null) {
            mServer.shutdown();
            LOGGER.debug(" Server shutdown() ");
        }
    }

    @Override
    public void destroy() {
        EthereumSer.getInstance().unSubscribe();
        SQLite.getInstance().closeConnection();
        if (mServer != null) {
            mServer.shutdown();
        }
    }
}
