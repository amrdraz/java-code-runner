import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.OutputStream;

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
    private ByteArrayOutputStream stream;

    public ServletRoute()
    {
      // Call replaceSystemOut which replaces the
        // normal System.out with a ThreadPrintStream.
        // ThreadPrintStream.replaceSystemOut();
        // System.out.println("Works");
      out = System.out;
      err = System.err;
      ThreadPrintStream.replaceSystemOut();
      ThreadPrintStream.replaceSystemErr();
    }

    public void print(String s) {
      out.print(stream);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        // out.println("Get");
        response.getWriter().print("RunnerServlet");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        String code = request.getParameter("code");
        String name = request.getParameter("name");

        // out.println(":--------Recived POST for "+name+"-------:");
        // out.println(code);
        // out.println("-----------------------------------------");
        ByteArrayOutputStream runnerOut = new ByteArrayOutputStream();
        ByteArrayOutputStream runnerErr = new ByteArrayOutputStream();
        ((ThreadPrintStream)System.out).setThreadOut(new PrintStream(runnerOut));
        ((ThreadPrintStream)System.err).setThreadOut(new PrintStream(runnerErr));

        JavaRunner.compile(name,code);


        ((ThreadPrintStream)System.out).setThreadOut(new PrintStream(out));
        ((ThreadPrintStream)System.err).setThreadOut(new PrintStream(err));

        // out.println(":"+name+":"+out[0].toString()+":----:");
        // out.println(":--------Sending response for "+name+"-------:");

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);

        Map<String,String> res = new HashMap<String,String>();
        res.put("stout", runnerOut.toString());
        res.put("sterr", runnerErr.toString());
        response.getWriter().print(JSON.toString(res));
    }
}

/** A ThreadPrintStream replaces the normal System.out and ensures
 * that output to System.out goes to a different PrintStream for
 * each thread.  It does this by using ThreadLocal to maintain a
 * PrintStream for each thread. 
 * from http://maiaco.com/articles/java/threadOut.php
 */
class ThreadPrintStream extends PrintStream {

    /** Changes System.out to a ThreadPrintStream which will
     * send output to a separate streem for each thread. */
    public static void replaceSystemOut() {

        // Save the existing System.out
        PrintStream console = System.out;

        // Create a ThreadPrintStream and install it as System.out
        ThreadPrintStream threadOut = new ThreadPrintStream();
        System.setOut(threadOut);

        // Use the original System.out as the current thread's System.out
        threadOut.setThreadOut(console);
    }

    /** Changes System.err to a ThreadPrintStream which will
     * send output to a separate streem for each thread. */
    public static void replaceSystemErr() {

        // Save the existing System.out
        PrintStream console = System.err;

        // Create a ThreadPrintStream and install it as System.out
        ThreadPrintStream threadErr = new ThreadPrintStream();
        System.setErr(threadErr);

        // Use the original System.out as the current thread's System.out
        threadErr.setThreadOut(console);
    }

    /** Thread specific storage to hold a PrintStream for each thread */
    private ThreadLocal<PrintStream> out;

    private ThreadPrintStream() {
        super(new ByteArrayOutputStream(0));
        out = new ThreadLocal<PrintStream>();
    }

    /** Sets the PrintStream for the currently executing thread. */
    public void setThreadOut(PrintStream out) {
        this.out.set(out);
    }

    /** Returns the PrintStream for the currently executing thread. */
    public PrintStream getThreadOut() {
        return this.out.get();
    }

    @Override public boolean checkError() {
        return getThreadOut().checkError();
    }

    @Override public void write(byte[] buf, int off, int len) {
        getThreadOut().write(buf, off, len);
    }

    @Override public void write(int b) { getThreadOut().write(b); }

    @Override public void flush() { getThreadOut().flush(); }
    @Override public void close() { getThreadOut().close(); }
}
