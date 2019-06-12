package niotest;

import xuji.HelloService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RpcConsumer {
    public static void main(String[] args) throws Exception {
        HelloService service = RpcFramework.refer(HelloService.class, "127.0.0.1", 1234);
        //long start =System.currentTimeMillis();
                String hello = service.hello("World");
                System.out.println(hello);

        //long end = System.currentTimeMillis();
        //System.out.println("---cost---"+""+(end-start));
    }
}
