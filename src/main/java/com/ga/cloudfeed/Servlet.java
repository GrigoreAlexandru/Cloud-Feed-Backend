package com.ga.cloudfeed;


import com.google.apphosting.api.ApiProxy;
import com.google.apphosting.api.ApiProxy.Environment;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(
        name = "Servlet",
        urlPatterns = {"/"}
)
public class Servlet extends HttpServlet {
    public static String url;
    private static StringBuilder postUrl = new StringBuilder();
    public static StringBuilder getUrl = new StringBuilder();

    public Servlet() {
    }

    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String date = DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC));
        String feed = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<rss version=\"2.0\">\n\n<channel>\n  <title>W3Schools Home Page</title>\n  <link>https://www.w3schools.com</link>\n  <description>Free web building tutorials</description>\n<lastBuildDate>" + date + "</lastBuildDate>\n<link rel='hub' href='https://pubsubhubbub.appspot.com/'/>\n  <item>\n    <title>RSS Tutorial</title>\n    <link>https://www.w3schools.com/xml/xml_rss.asp</link>\n    <description>New RSS tutorial on W3Schools</description>\n  </item>\n  <item>\n    <title>XML Tutorial</title>\n    <link>https://www.w3schools.com/xml</link>\n    <description>New XML tutorial on W3Schools</description>\n  </item>\n</channel>\n</rss>\n";
        PrintWriter out = resp.getWriter();
        resp.setContentType("text/plain");
        switch (req.getServletPath()) {
            case "/get": {
                out.println(getUrl.toString());
                break;
            }
            case "/post": {
                out.println(postUrl.toString());
                break;
            }
            case "/feed": {
                out.println(feed);
                break;
            }
            case "/cron": {
                Firebase.start();
                break;
            }
            case "/websub": {
                out.println(req.getParameter("hub.challenge"));
                Firebase.logger.info("Verification successful for " + req.getParameter("hub.topic"));
                getUrl.append("Verification successful for " + req.getParameter("hub.topic") + "\n");
            }
        }
        out.close();
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        if (req.getServletPath().equals("/websub")) {
            String encoded = req.getParameter("encoded");
            Feed feed = new Feed(encoded);
            feed.sendToTopic();
            postUrl.append("Update for " + feed.getTitle() + "\n");
        }

    }

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        Environment env = ApiProxy.getCurrentEnvironment();
        Map<String, Object> attributes = env.getAttributes();
        String hostAndPort = (String)attributes.get("com.google.appengine.runtime.default_version_hostname");
        url = "http://" + hostAndPort + "/";
        Firebase.start();
    }
}
