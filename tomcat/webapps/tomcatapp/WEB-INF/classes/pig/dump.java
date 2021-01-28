package pig;

import javax.servlet.http.*;
import javax.servlet.ServletException;
import java.io.*;
import java.util.*;

public class dump
    extends HttpServlet {

    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws ServletException,
             IOException {
        dump(req,resp);
    }

    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws ServletException,
             IOException {
        dump(req,resp);
    }

    private void dump(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws ServletException,
             IOException {
        resp.setContentType("text/plain");
        PrintWriter out = resp.getWriter();
        out.println("method: " + req.getMethod());
        out.println("query: " + req.getQueryString());
        out.println("path translated: " + req.getPathTranslated());
        out.println("path info: " + req.getPathInfo());
        out.println("request uri: " + req.getRequestURI());
        out.println("servlet path: " + req.getServletPath());
        out.println("context path: " +
                    req.getServletContext().getContextPath());
        out.println("requested session id: " + req.getRequestedSessionId());
        out.println("session id: " + req.getSession().getId());
        out.println("remote user: " + req.getRemoteUser());

        Map<String,String[]> params = req.getParameterMap();
        out.println("number params: " + params.size());
        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            for (String vx : entry.getValue()) {
                out.println(" * " + entry.getKey() + ": " + vx);
            }
        }

        Map<String,String> trailers = req.getTrailerFields();
        out.println("number trailers: " + trailers.size());
        for (Map.Entry<String, String> entry : trailers.entrySet()) {
            out.println(" * " + entry.getKey() + ": " + entry.getValue());
        }

        Cookie[] cs = req.getCookies();
        if (cs != null) {
            out.println("number cookies: " + cs.length);
            for (Cookie c : cs) {
                out.println(" * " + c.getName() + ": " + c.getValue());
            }
        } else {
            out.println("number cookies: 0");
        }

        for (
                Enumeration<String> e = req.getAttributeNames();
                e.hasMoreElements();
        ) {
            out.println(" * " + e.nextElement());
        }

        out.println("headers:");
        for (
                Enumeration<String> e = req.getHeaderNames();
                e.hasMoreElements();
        ) {
            String header = e.nextElement();
            for (
                    Enumeration<String> e2 = req.getHeaders(header);
                    e2.hasMoreElements();
            ) {
                out.println(" * " + header + ": " + e2.nextElement());
            }
        }
    }
}
