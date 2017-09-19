package com.desprice.unionchc.service;


import com.desprice.unionchc.Utils;
import com.desprice.unionchc.entity.JspAddress;
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

        String html = getResourceFile("html/password.html");
        Response response = Response.ok(html).build();
        return response;
    }

    @GET
    @Path("address/{user:\\d+}/{msg:\\d+}")
    @Produces(MediaType.TEXT_HTML)
    public Response getAddress(@PathParam("user") String userName, @PathParam("msg") String msg) {
        String html = getResourceFile("html/address.html");
        Response response = Response.ok(html).build();
        return response;
    }

    private String getResourceFile(String fileName) {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);

        BufferedReader bReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuffer sb = new StringBuffer();
        String line = null;

        //read file line by line
        try {
            while ((line = bReader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //finally convert StringBuffer object to String!
        return sb.toString();
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

                UserBot userBot = TUsers.getInstance().getUser(param.path1);
                if (userBot.verify == 1) {
                    messageInfo.setStatus(Response.Status.PRECONDITION_FAILED);
                    messageInfo.setMessage("Вы уже зарегистрированы");
                } else {
                    userBot.password = param.password1;
                    //userBot.messageId = param.path2;
                    bot.setUserBot(userBot);
                    bot.createUser();
                    messageInfo.setStatus(Response.Status.OK);
                    messageInfo.setMessage("Вам создан адрес");
                }
            } catch (Exception ex) {
            }
        }
        return Response.status(Response.Status.OK).entity(messageInfo).build();
    }

    @POST
    @Path("/address/update/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addressUpdate(@NotNull JspAddress param) {
        LOGGER.debug("addressUpdate" + Utils.jsonToString(param));
        JspMessage messageInfo = new JspMessage();

        if (param.password.isEmpty() || param.address.isEmpty()) {
            messageInfo.setStatus(Response.Status.PRECONDITION_FAILED);
            messageInfo.setMessage("Укажите адрес и пароль");
        } else {
            messageInfo.setStatus(Response.Status.OK);
            try {
                BotTelegram bot = new BotTelegram();
                UserBot userBot = TUsers.getInstance().getUser(param.path);
                userBot.address = param.address;
                userBot.password = param.password;
                bot.setUserBot(userBot);
                if (!bot.checkAddress()) {
                    messageInfo.setStatus(Response.Status.PRECONDITION_FAILED);
                    messageInfo.setMessage("Неверный пароль или адрес");
                } else {
                    messageInfo.setStatus(Response.Status.OK);
                    messageInfo.setMessage("Вы вошли в систему");
                }
            } catch (Exception ex) {
            }
        }
        return Response.status(Response.Status.OK).entity(messageInfo).build();
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

