package com.desprice.unionchc.entity;

import javax.ws.rs.core.Response;


public class JspMessage {

    public Response.Status status;
    public String message;
    public Long data;

    public String getStatusName() {
        return status.name();
    }

    public Integer getStatus() {
        return status.getStatusCode();
    }

    public void setStatus(Response.Status status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
