import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.OutputStream;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import java.util.HashMap;
import java.util.Map;

import java.io.IOException;
import java.io.Reader;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.List;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutionException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ajax.JSON;

public class RunnerServlet
{
  public static void main(String[] args) throws Exception
  {


      int port = 8080;
      if (args.length>0) {
        port = Integer.parseInt(args[0]);
      }
      Server server = new Server(port);

      ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
      context.setContextPath("/");
      server.setHandler(context);

      // Server content from tmp
      // ServletHolder holder = context.addServlet(org.eclipse.jetty.servlet.DefaultServlet.class,"/tmp/*");
      // holder.setInitParameter("resourceBase","/tmp");
      // holder.setInitParameter("pathInfoOnly","true");
      
      // Serve some hello world servlets
      context.addServlet(new ServletHolder(new ServletRoute()),"/*");

      server.start();
      server.join();
  }
}

@SuppressWarnings("serial")
class ServletRoute extends HttpServlet
{

    private PrintStream out;
    private PrintStream err;
    private InputStream in;
    private ByteArrayOutputStream stream;

    public ServletRoute()
    {
      // Call replaceSystemOut which replaces the
        // normal System.out with a ThreadPrintStream.
        // ThreadPrintStream.replaceSystemOut();
        // System.out.println("Works");
      out = System.out;
      err = System.err;
      in = System.in;
      ThreadInputStream.replaceSystemIn();
      ThreadPrintStream.replaceSystemOut();
      ThreadPrintStream.replaceSystemErr();
    }

    public void print(String s) {
      out.print(s);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        // out.println("Get");
        response.getWriter().print("RunnerServlet");
    }

    @SuppressWarnings("deprecation")
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        String code = request.getParameter("code");
        String name = request.getParameter("name");
        long timeLimit = 5000;
        if(request.getParameter("timeLimit")!=null) {
          timeLimit = Integer.parseInt(request.getParameter("timeLimit"));
        }
        final String input;
        if(request.getParameter("input")==null) {
          input = "";
        } else {
          input = request.getParameter("input");
        }

        // print(input);

        // out.println(":--------Recived POST for "+name+"-------:");
        // out.println(code);
        // out.println("-----------------------------------------");

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);

        // response map
        Map<String,String> res = new HashMap<String,String>();

        // start executor in order to timeout
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Thread thread = new Thread(new Runnable() {
         public void run() {
            ByteArrayInputStream runnerIn =  new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));;
            ByteArrayOutputStream runnerOut = new ByteArrayOutputStream();
            ByteArrayOutputStream runnerErr = new ByteArrayOutputStream();

            ((ThreadInputStream)System.in).setThreadIn(runnerIn);
            ((ThreadPrintStream)System.out).setThreadOut(new PrintStream(runnerOut));
            ((ThreadPrintStream)System.err).setThreadOut(new PrintStream(runnerErr));

            JavaRunner.compile(name,code);

            ((ThreadInputStream)System.in).setThreadIn(in);
            ((ThreadPrintStream)System.out).setThreadOut(out);
            ((ThreadPrintStream)System.err).setThreadOut(err);

            try {
              res.put("stout", runnerOut.toString());
              res.put("sterr", runnerErr.toString());
              response.getWriter().print(JSON.toString(res));
            } catch (Exception  e) {
              e.printStackTrace();
            }

          }
        });
        thread.start();
        try {
          thread.join(timeLimit);
          if (thread.isAlive()) {
            thread.stop();
            ((ThreadInputStream)System.in).setThreadIn(in);
            ((ThreadPrintStream)System.out).setThreadOut(out);
            ((ThreadPrintStream)System.err).setThreadOut(err);
            res.put("stout", "");
            res.put("sterr", "TimeoutException: Your program ran for more than "+timeLimit+"ms");
            response.getWriter().print(JSON.toString(res));
          }
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
    }
}
