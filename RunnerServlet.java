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
import java.lang.reflect.InvocationTargetException;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutionException;
 
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import java.security.SecureClassLoader;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.FileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

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
    public ServletRoute()
    {
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        // System.out.println("Get");
        response.getWriter().print("RunnerServlet");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        String code = request.getParameter("code");
        String name = request.getParameter("name");
        // System.out.println("Post:");
        // System.out.println(code);
        // System.out.println("----");

        ByteArrayOutputStream runnerOut = new ByteArrayOutputStream();
        ByteArrayOutputStream runnerErr = new ByteArrayOutputStream();
        PrintStream _$sysOut = System.out;
        PrintStream _$sysErr = System.err;
        System.setOut(new PrintStream(runnerOut));
        System.setErr(new PrintStream(runnerErr));
        JavaRunner.compile(name,code);
        System.setOut(_$sysOut);
        System.setErr(_$sysErr);

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);

        Map<String,String> res = new HashMap<String,String>();
        res.put("stout", runnerOut.toString());
        res.put("sterr", runnerErr.toString());
        response.getWriter().print(JSON.toString(res));
        // response.getWriter().println("{"+
        //         // "\"code\":\"" +code+"\""+
        //         "\"stout\":\""+runnerOut.toString()+"\""+
        //         "\"sterr\":\""+runnerErr.toString()+"\""+
        //     "}");
    }
//     public static void compile(String name, String code){
//       compile(name,code,5000);
//     }
//     public static void compile(String name, String code, int timeLimit){
//         // System.out.println(code);
//         /*Creating dynamic java source code file object*/
//         SimpleJavaFileObject fileObject = new DynamicSource (name, code) ;
//         JavaFileObject javaFileObjects[] = new JavaFileObject[]{fileObject} ;
// // Read content of file
// // try {
// // Reader fr = fileObject.openReader(true);
// //         char [] a = new char[500];
// //         fr.read(a); // reads the content to the array
// //         for(char c : a)
// //             System.out.print(c); //prints the characters one by one
// //         fr.close();
// //         System.out.println();
// // } catch(IOException e) {

// // }
        
        
//         // StandardJavaFileManager stdFileManager = compiler.getStandardFileManager(null, Locale.getDefault(), null);
//          // We get an instance of JavaCompiler. Then
//         // we create a file manager
//         // (our custom implementation of it)
//         JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
//         JavaFileManager stdFileManager = new ClassFileManager(compiler.getStandardFileManager(null, null, null));
//         /* Prepare a list of compilation units (java source code file objects) to input to compilation task*/
//         Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(javaFileObjects);
 
//         /*Prepare any compilation options to be used during compilation*/
//         // List<String> compileOptions = new ArrayList<String>();
//         // compileOptions.addAll(Arrays.asList("-classpath", System.getProperty("java.class.path")));
//         // Iterable<String> compilationOptionss = Arrays.asList(compileOptions);
 
//         /*Create a diagnostic controller, which holds the compilation problems*/
//         DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
 
//         /*Create a compilation task from compiler by passing in the required input objects prepared above*/
//         CompilationTask compilerTask = compiler.getTask(null, stdFileManager, diagnostics, null, null, compilationUnits) ;
 
//         //Perform the compilation by calling the call method on compilerTask object.
//         boolean status = compilerTask.call();
 
//         if (!status){//If compilation error occurs
//             /*Iterate through each compilation problem and print it*/
//             for (Diagnostic diagnostic : diagnostics.getDiagnostics()){
//                 System.err.format("Error on line %d in %s", diagnostic.getLineNumber(), diagnostic);
//             }
//         } else {
//           ExecutorService service = Executors.newSingleThreadExecutor();

//           try {
//               Runnable r = new Runnable() {
//                   @Override
//                   public void run() {
//                     try {
//                       stdFileManager.getClassLoader(null).loadClass(name).getDeclaredMethod("main", new Class[] { String[].class }).invoke(null, new Object[] { null });
//                     } catch (ClassNotFoundException e) {
//                       System.err.println("Class not found: " + e);
//                     } catch (NoSuchMethodException e) {
//                       System.err.println("No such method: " + e);
//                     } catch (IllegalAccessException e) {
//                       System.err.println("Illegal access: " + e);
//                     } catch (InvocationTargetException e) {
//                       System.err.println("RuntimeError: "+e.getTargetException());
//                     }
//                   }
//               };

//               Future<?> f = service.submit(r);

//               f.get(timeLimit, TimeUnit.MILLISECONDS);     // attempt the task for timelimit default 5 seconds
//           }
//           catch (final InterruptedException e) {
//             System.err.println("Thread Interrupted: " + e);
//           }
//           catch (final TimeoutException e) {
//             System.err.println("TimeoutException: Your program ran for more than "+timeLimit);
//           }
//           catch (final ExecutionException e) {
//             e.printStackTrace();
//           }
//           finally {
//               service.shutdown();
//           }
//         }
//         try {
//             stdFileManager.close() ;//Close the file manager
//         } catch (IOException e) {
//             e.printStackTrace();
//         }
//     }
}

// /**
//  * Creates a dynamic source code file object
//  *
//  * This is an example of how we can prepare a dynamic java source code for compilation.
//  * This class reads the java code from a string and prepares a JavaFileObject
//  *
//  */
// class DynamicSource extends SimpleJavaFileObject{
//     private String qualifiedName ;
//     private String sourceCode ;
 
//     /**
//      * Converts the name to an URI, as that is the format expected by JavaFileObject
//      *
//      *
//      * @param fully qualified name given to the class file
//      * @param code the source code string
//      */
//     protected DynamicSource(String name, String code) {
//         super(URI.create("string:///" +name.replaceAll("\\.", "/") + Kind.SOURCE.extension), Kind.SOURCE);
//         this.qualifiedName = name ;
//         this.sourceCode = code ;
//     }
 
//     @Override
//     public CharSequence getCharContent(boolean ignoreEncodingErrors)
//             throws IOException {
//         return sourceCode ;
//     }
 
//     public String getQualifiedName() {
//         return qualifiedName;
//     }
 
//     public void setQualifiedName(String qualifiedName) {
//         this.qualifiedName = qualifiedName;
//     }
 
//     public String getSourceCode() {
//         return sourceCode;
//     }
 
//     public void setSourceCode(String sourceCode) {
//         this.sourceCode = sourceCode;
//     }
// }
// class JavaClassObject extends SimpleJavaFileObject {

//     /**
//     * Byte code created by the compiler will be stored in this
//     * ByteArrayOutputStream so that we can later get the
//     * byte array out of it
//     * and put it in the memory as an instance of our class.
//     */
//     protected final ByteArrayOutputStream bos =
//         new ByteArrayOutputStream();

//     /**
//     * Registers the compiled class object under URI
//     * containing the class full name
//     *
//     * @param name
//     *            Full name of the compiled class
//     * @param kind
//     *            Kind of the data. It will be CLASS in our case
//     */
//     public JavaClassObject(String name, Kind kind) {
//         super(URI.create("string:///" + name.replace('.', '/')
//             + kind.extension), kind);
//     }

//     /**
//     * Will be used by our file manager to get the byte code that
//     * can be put into memory to instantiate our class
//     *
//     * @return compiled byte code
//     */
//     public byte[] getBytes() {
//         return bos.toByteArray();
//     }

//     /**
//     * Will provide the compiler with an output stream that leads
//     * to our byte array. This way the compiler will write everything
//     * into the byte array that we will instantiate later
//     */
//     @Override
//     public OutputStream openOutputStream() throws IOException {
//         return bos;
//     }
// }

// class ClassFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {
//     /**
//     * Instance of JavaClassObject that will store the
//     * compiled bytecode of our class
//     */
//     private JavaClassObject jclassObject;

//     /**
//     * Will initialize the manager with the specified
//     * standard java file manager
//     *
//     * @param standardManger
//     */
//     public ClassFileManager(StandardJavaFileManager standardManager) {
//         super(standardManager);
//     }

//     /**
//     * Will be used by us to get the class loader for our
//     * compiled class. It creates an anonymous class
//     * extending the SecureClassLoader which uses the
//     * byte code created by the compiler and stored in
//     * the JavaClassObject, and returns the Class for it
//     */
//     @Override
//     public ClassLoader getClassLoader(Location location) {
//         return new SecureClassLoader() {
//             @Override
//             protected Class<?> findClass(String name)
//                 throws ClassNotFoundException {
//                 byte[] b = jclassObject.getBytes();
//                 return super.defineClass(name, jclassObject
//                     .getBytes(), 0, b.length);
//             }
//         };
//     }

//     /**
//     * Gives the compiler an instance of the JavaClassObject
//     * so that the compiler can write the byte code into it.
//     */
//     @Override
//     public JavaFileObject getJavaFileForOutput(Location location,
//         String className, Kind kind, FileObject sibling)
//             throws IOException {
//             jclassObject = new JavaClassObject(className, kind);
//         return jclassObject;
//     }
// }