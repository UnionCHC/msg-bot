package com.desprice.unionchc.exceptions;
/*
import javax.servlet.ServletException;
//import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;


//@WebServlet("/AppException")
//@Path("/AppException")
public class AppException extends HttpServlet {

    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {
        processError(request, response);
    }

    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {
        processError(request, response);
    }

    private void processError(HttpServletRequest httpRequest,
                              HttpServletResponse response) throws IOException {
        // Analyze the servlet exception
        Throwable throwable = (Throwable) httpRequest
                .getAttribute("javax.servlet.error.exception");
        Integer statusCode = (Integer) httpRequest
                .getAttribute("javax.servlet.error.status_code");
        String servletName = (String) httpRequest
                .getAttribute("javax.servlet.error.servlet_name");
        if (servletName == null) {
            servletName = "Unknown";
        }
        String requestUri = (String) httpRequest
                .getAttribute("javax.servlet.error.request_uri");
        if (requestUri == null) {
            requestUri = "Unknown";
        }


        String message = "";
        if (statusCode != 500) {
            message += "<h4>Сведения об ошибке</h3>";
            message += "<strong>Status Code</strong>: " + statusCode + "<br>";
            message += "<strong>Requested URI</strong>: " + requestUri;
            message += "<br>contextPath URI: " + httpRequest.getContextPath();
        } else {
            message += "<h3>Exception Details</h3>";
            message += "<ul><li>Servlet Name: " + servletName + "</li>";
            message += "<li>Exception Name: " + throwable.getClass().getName() + "</li>";
            message += "<li>Requested URI: " + requestUri + "</li>";
            message += "<li>contextPath URI: " + httpRequest.getContextPath() + "</li>";
            message += "<li>Exception Message: " + throwable.getMessage() + "</li>";
            message += "</ul>";

        }
        HttpSession session = httpRequest.getSession(true);
        session.setAttribute("errorCode", statusCode);
        session.setAttribute("errorMessage", message);
        if (!requestUri.contains("error/page404")) {
            response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
            response.setHeader("Location", httpRequest.getContextPath() + "/error/page404");
        }


    }
}

*/