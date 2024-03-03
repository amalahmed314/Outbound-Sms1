package com.example;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;

/**
 *
 * @author Amal ELF
 */
public class OutboundSms extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String toPhoneNumber = request.getParameter("toPhoneNumber");
        String message = request.getParameter("message");

        try {
            sendMessage(toPhoneNumber, message);

            response.getWriter().write("<h1>SMS made successfully!</h1>");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error making the call.");
        }
    }

    private static void sendMessage(String toPhoneNumber, String mesg) {
         Twilio.init("AC782451390947f7f888ca070a31fe5e8b", "5eee3e4f4edc9c379ee6dedf47b6dc6f");
        Message message = Message.creator(
                new com.twilio.type.PhoneNumber(toPhoneNumber),
                new com.twilio.type.PhoneNumber("+12062023856"),
                mesg)
                .create();
        System.out.println(message.getSid());

    }
}
