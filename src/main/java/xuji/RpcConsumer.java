package xuji;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RpcConsumer {
    public static void main(String[] args) throws Exception {
        ExecutorService executor = new ThreadPoolExecutor(45,45,0, TimeUnit.MILLISECONDS,new LinkedBlockingQueue());
        HelloService service = RpcFramework.refer(HelloService.class, "127.0.0.1", 1234);
        long start =System.currentTimeMillis();
        for (int i = 0; i < 10000; i ++) {
            executor.submit(()->{
                String hello = service.hello("World");
                System.out.println(hello);

            });
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
            // pass
        }
        long end = System.currentTimeMillis();
        System.out.println("---cost---"+""+(end-start));
    }
}
