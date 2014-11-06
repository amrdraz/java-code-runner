import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutionException;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class TerminalRunner {
    public static void main(String args[]){
        String name = args[0];
        String code = args[1];
        long timeLimit = Long.parseLong(args[2]);
        ByteArrayInputStream  runnerIn  = new ByteArrayInputStream(args[3].getBytes(StandardCharsets.UTF_8));
        
        ExecutorService service = Executors.newSingleThreadExecutor();

          try {
              Runnable r = new Runnable() {
                  @Override
                  public void run() {
                    JavaRunner.compile(name,code);
                  }
              };

              Future<?> f = service.submit(r);

              f.get(timeLimit, TimeUnit.MILLISECONDS);     // attempt the task for timelimit default 5 seconds
          }
          catch (InterruptedException e) {
            System.err.println("Thread Interrupted: " + e);
          }
          catch (TimeoutException e) {
            System.out.print("");
            System.err.print("TimeoutException: Your program ran for more than "+timeLimit+"ms");
          }
          catch (final ExecutionException e) {
            e.printStackTrace();
          }
          catch (Exception e) {
            e.printStackTrace();
          }
          finally {
            service.shutdown();
          }
    }
}