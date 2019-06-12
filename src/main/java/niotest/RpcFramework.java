package niotest;

/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */

import com.alibaba.fastjson.JSON;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.*;

/**
 * RpcFramework
 *
 * @author william.liangf
 */
public class RpcFramework {

    private static SocketChannel serverSide =null;
    private static SocketChannel clientSide =null;


    /**
     * 暴露服务
     *
     * @param service 服务实现
     * @param port 服务端口
     * @throws Exception
     */
    public static void export(final Object service, int port) throws Exception {
        if(service == null){
            throw new IllegalArgumentException("service instance == null");
        }
        if(port <= 0 || port > 65535){
            throw new IllegalArgumentException("Invalid port " + port);
        }
        System.out.println("Export service " + service.getClass().getName() + " on port " + port);
        //监听一个socket端口
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        ServerSocket serverSocket = serverSocketChannel.socket();
        serverSocket.bind(new InetSocketAddress(port));

        Selector selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        for(;;) {
            try {
                selector.select(3000);

                Set<SelectionKey> selectionKeys = selector.selectedKeys();

                selectionKeys.forEach(selectionKey -> {

                    try {
                        if (selectionKey.isAcceptable()) {
                            ServerSocketChannel server = (ServerSocketChannel) selectionKey.channel();
                            serverSide = server.accept();
                            serverSide.configureBlocking(false);
                            serverSide.register(selector, SelectionKey.OP_READ);
                        } else if (selectionKey.isReadable()) {
                            serverSide = (SocketChannel) selectionKey.channel();
                            ByteBuffer readBuffer = ByteBuffer.allocate(1024);

                            int count = serverSide.read(readBuffer);

                            if (count > 0) {
                                readBuffer.flip();

                                Charset charset = Charset.forName("utf-8");
                                String receivedMessage = String.valueOf(charset.decode(readBuffer).array());
                                RpcRequest request = JSON.parseObject(receivedMessage,RpcRequest.class);

                                System.out.println(serverSide + ": " + receivedMessage);
                                //从输入流中读取方法名，参数等
                                String methodName = "hello";
                                Class<?>[] parameterTypes = {String.class};
                                Object[] arguments = {receivedMessage};
                                Method method = service.getClass().getMethod(methodName, parameterTypes);
                                Object result = method.invoke(service, arguments);

                                String senderKey = null;

                                ByteBuffer writeBuffer = ByteBuffer.allocate(1024);

                                writeBuffer.put(((String)result).getBytes());

                                writeBuffer.flip();

                                serverSide.write(writeBuffer);
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

                selectionKeys.clear();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 引用服务
     *
     * @param <T> 接口泛型
     * @param interfaceClass 接口类型
     * @param host 服务器主机名
     * @param port 服务器端口
     * @return 远程服务
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static <T> T refer(final Class<T> interfaceClass, final String host, final int port) throws Exception {
        if(interfaceClass == null){
            throw new IllegalArgumentException("Interface class == null");
        }
        if(! interfaceClass.isInterface()){
            throw new IllegalArgumentException("The " + interfaceClass.getName() + " must be interface class!");
        }
        if (host == null || host.length() == 0){
            throw new IllegalArgumentException("Host == null!");
        }
        if (port <= 0 || port > 65535){
            throw new IllegalArgumentException("Invalid port " + port);
        }
        System.out.println("Get remote service " + interfaceClass.getName() + " from server " + host + ":" + port);
         new Thread(()->{
                     try {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);

            Selector selector = Selector.open();
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
            socketChannel.connect(new InetSocketAddress("127.0.0.1", 1234));

            while (true) {
                selector.select(3000);
                Set<SelectionKey> keySet = selector.selectedKeys();

                for (SelectionKey selectionKey : keySet) {
                    if (selectionKey.isConnectable()) {
                        clientSide = (SocketChannel) selectionKey.channel();

                        if (clientSide.isConnectionPending()) {
                            clientSide.finishConnect();
                        }

                        clientSide.register(selector, SelectionKey.OP_READ);
                    } else if (selectionKey.isReadable()) {
                        SocketChannel client = (SocketChannel) selectionKey.channel();

                        ByteBuffer readBuffer = ByteBuffer.allocate(1024);

                        int count = client.read(readBuffer);

                        if (count > 0) {
                            String receivedMessage = new String(readBuffer.array(), 0, count);
                            System.out.println(receivedMessage);
                        }
                    }
                }

                keySet.clear();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
         }).start();

        ExecutorService executorService = new ThreadPoolExecutor(45,45,0, TimeUnit.MILLISECONDS,new LinkedBlockingQueue());

        //创建一个接口代理
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[] {interfaceClass},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] arguments) throws Throwable {

                        Future<Object> futrue = executorService.submit(() -> {
                            RpcFuture rpcFuture = new RpcFuture();

                            try {
                                RpcRequest request = new RpcRequest();
                                request.setMethodName(method.getName());
                                request.setParamTypes(method.getParameterTypes());
                                request.setArgs(arguments);
                                byte[] bytes = JSON.toJSONBytes(request);

                                ByteBuffer writeBuffer = ByteBuffer.allocate(1024);
                                writeBuffer.put(bytes);
                                clientSide.write(writeBuffer);
                                RpcRequestHolder.put(request.getRequestId(), rpcFuture);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            return rpcFuture.get();

                        });
                        return futrue.get();
                    }
                });

        }


}