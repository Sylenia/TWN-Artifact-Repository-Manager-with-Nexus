package com.example.myapp;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Random;

public class App {
    public static void main(String[] args) throws Exception {
        Server server = new Server(8080); // Start Jetty server on port 8080

        // Set up the servlet context
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        // Map the game servlet
        context.addServlet(new ServletHolder(new GameServlet()), "/game");

        System.out.println("Starting server at http://localhost:8080...");
        server.start();
        server.join();
    }

    // Inner class to handle game logic
    public static class GameServlet extends HttpServlet {
        private final int targetNumber = new Random().nextInt(100) + 1;
        private int attempts = 0;

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType("text/html");
            resp.getWriter().println(
            "<html>" +
            "<head><title>Number Guessing Game</title></head>" +
            "<body>" +
                "<h1>Welcome to the Number Guessing Game!</h1>" +
                "<form method=\"POST\">" +
                    "<label for=\"guess\">Enter your guess:</label>" +
                    "<input type=\"number\" id=\"guess\" name=\"guess\">" +
                    "<button type=\"submit\">Submit</button>" +
                "</form>" +
            "</body>" +
            "</html>"
            );
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String guessParam = req.getParameter("guess");

            try {
                int guess = Integer.parseInt(guessParam);
                attempts++;

                if (guess == targetNumber) {
                    resp.getWriter().println("<h1>Congratulations! You guessed the number in " + attempts + " attempts.</h1>");
                } else if (guess < targetNumber) {
                    resp.getWriter().println("<h1>Too Low! Try again.</h1>");
                } else {
                    resp.getWriter().println("<h1>Too High! Try again.</h1>");
                }

                // Redirect to allow further attempts
                resp.getWriter().println("<a href='/game'>Play Again</a>");
            } catch (NumberFormatException e) {
                resp.getWriter().println("<h1>Invalid input. Please enter a valid number.</h1>");
                resp.getWriter().println("<a href='/game'>Go Back</a>");
            }
        }
    }
}
