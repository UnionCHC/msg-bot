package com.desprice.unionchc.service;


import com.desprice.unionchc.Utils;
import com.desprice.unionchc.entity.JspMessage;
import com.desprice.unionchc.entity.JspPassord;
import com.desprice.unionchc.entity.UserBot;
import com.desprice.unionchc.sqlite.TUsers;
import com.desprice.unionchc.telegram.BotTelegram;
import org.glassfish.jersey.server.mvc.Viewable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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


    @GET
    @Path("password/{user:\\d+}/{msg:\\d+}")
    @Produces(MediaType.TEXT_HTML)
    public Response getPassword(@PathParam("user") String userName, @PathParam("msg") String msg) {

        //InputStream inputStream = getClass().getClassLoader().getResourceAsStream("/WEB-INF/jsp/password.html");
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("html/password.html");

        BufferedReader bReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuffer sb = new StringBuffer();
        String line = null;

        //read file line by line
        try {
            while ((line = bReader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //finally convert StringBuffer object to String!
        String html = sb.toString();
        Response response = Response.ok(html).build();
        return response;
    }

    @POST
    @Path("/password/update/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response passwordUpdate(@NotNull JspPassord param) {

        LOGGER.debug("passwordUpdate" + Utils.jsonToString(param));

        JspMessage messageInfo = new JspMessage();

        if (!param.password1.equals(param.password2)) {
            messageInfo.setStatus(Response.Status.PRECONDITION_FAILED);
            messageInfo.setMessage("Пароли должны совпадать");

        } else {

            messageInfo.setStatus(Response.Status.OK);
            messageInfo.setMessage("Update");
            try {
                BotTelegram bot = new BotTelegram();

                //param.path


                UserBot userBot = TUsers.getInstance().getUser(param.path1);
                userBot.password = param.password1;
                userBot.messageId = param.path2;
                bot.setUserBot(userBot);
                bot.createUser();
                messageInfo.setStatus(Response.Status.OK);
                messageInfo.setMessage("");
            } catch (Exception ex) {
            }
        }
        return Response.status(Response.Status.OK).entity(messageInfo).build();
    }

    @POST
    @Path("/password/update1")
    // @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response passwordUpdate1(@FormParam("password1") String username, @FormParam("password2") String password, @FormParam("path") String path) {

        LOGGER.debug("");

      /*  JspMessage messageInfo = new JspMessage();
        try {


        } catch (Exception ex) {
        }*/
        //return Response.status(Response.Status.OK).entity(messageInfo).build();
        return Response.status(Response.Status.OK).build();
    }


    // not work
    @GET
    @Path("/info")
    @Produces(MediaType.TEXT_HTML)
    public Response getInfo() {
        Viewable viewable = new Viewable("/info.jsp");
        Response response = Response.ok(viewable).build();
        return response;
    }


}

