package com.desprice.unionchc.service;


import com.desprice.unionchc.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("telegram")
public class TelegramRes {
    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramRes.class);

    @GET
    @Produces("text/plain")
    public String getHelloText() {
        return String.valueOf("Test text");
    }


    @POST
    @Consumes("application/json")
    public Response postJson(@NotNull String activity) {
        LOGGER.debug("+++ input request +++");
        LOGGER.debug(Utils.jsonToString(activity));
        return Response.ok().build();
    }


}

