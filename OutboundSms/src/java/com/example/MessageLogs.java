package com.example;

import com.twilio.Twilio;
import com.twilio.base.ResourceSet;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.rest.api.v2010.account.Message;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Amal ELF
 */
public class MessageLogs extends HttpServlet {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/smsdb";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "amlahmad12345";

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String messageLogsHtml;
        try {
            messageLogsHtml = retrieveMessageLogFromDatabase();
        } catch (SQLException ex) {
            Logger.getLogger(MessageLogs.class.getName()).log(Level.SEVERE, null, ex);

            messageLogsHtml = "Error retrieving Messageing logs: " + ex.getMessage();
        }

        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Message Logs</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Message Logs</h1>");
            out.println(messageLogsHtml);
            out.println("</body>");
            out.println("</html>");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Message Logs Servlet";
    }

    private static void saveMessagesToDatabase() throws SQLException {
        Twilio.init("AC782451390947f7f888ca070a31fe5e8b", "5eee3e4f4edc9c379ee6dedf47b6dc6f");
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MessageLogs.class.getName()).log(Level.SEVERE, null, ex);
        }
        ResourceSet<Message> mesg = Message.reader().limit(1).read();

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

            if (mesg.iterator().hasNext()) {
                Message record = mesg.iterator().next();

                String sql = "SELECT * FROM message_log WHERE message_sid = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, record.getSid());
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        if (!resultSet.next()) {

                            try (PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO message_log (message_sid, body,status) VALUES (?, ?, ?)")) {
                                insertStatement.setString(1, record.getSid());
                                String updatedMessage = record.getBody().replace("Sent from your Twilio trial account - ", "");
                                insertStatement.setString(2, updatedMessage);
                                insertStatement.setString(3, record.getStatus().toString());

                                insertStatement.executeUpdate();
                                System.out.println("Message details saved to the database.");
                            }
                        } else {
                            System.out.println("Message SID already exists in the database, skipping insertion.");
                        }
                    }
                }
            } else {
                System.out.println("No Message found.");
            }
        }
    }

    private static String retrieveMessageLogFromDatabase() throws SQLException {
        saveMessagesToDatabase();
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MessageLogs.class.getName()).log(Level.SEVERE, null, ex);
        }

        StringBuilder htmlBuilder = new StringBuilder();

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT * FROM message_log";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {

                        String message_sid = resultSet.getString("message_sid");

                        String message_body = resultSet.getString("body");
                        String status = resultSet.getString("status");

                        htmlBuilder.append("<p>");
                        htmlBuilder.append("Message SID: ").append(message_sid).append("<br>");
                        htmlBuilder.append("Message: ").append(message_body).append("<br>");
                        htmlBuilder.append("Status: ").append(status).append("<br>");

                        htmlBuilder.append("--------------").append("<br>");
                        htmlBuilder.append("</p>");
                    }
                }
            }
        }

        return htmlBuilder.toString();
    }
}
