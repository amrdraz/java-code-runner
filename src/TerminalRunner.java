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
        long timeLimit = 5000;
        if(args.length>2)
          timeLimit = Long.parseLong(args[2]);
        ByteArrayInputStream runnerIn;
        if(args.length>3)
          runnerIn  = new ByteArrayInputStream(args[3].getBytes(StandardCharsets.UTF_8));
        
        Thread thread = new Thread(new Runnable() {
            public void run() {
              JavaRunner.compile(name,code);
            }
        });
        thread.start();
        try {
          thread.join(timeLimit);
          if (thread.isAlive()) {
            System.err.print("TimeoutException: Your program ran for more than "+timeLimit+"ms");
          }
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

    }
}