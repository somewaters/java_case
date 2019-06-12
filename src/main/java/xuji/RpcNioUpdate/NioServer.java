package xuji.RpcNioUpdate;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Set;

public class NioServer {
    private ServerSocketChannel ssc;
    public void init() throws IOException {
        ssc = ServerSocketChannel.open();
        ServerSocket serverSocket = ssc.socket();
        serverSocket.bind(new InetSocketAddress(1234));
        ssc.configureBlocking(false);
        Selector selector = Selector.open();
        ssc.register(selector, SelectionKey.OP_ACCEPT);
        while(true){
            int nkeys = selector.select();
            SelectionKey skey = null;
            if(nkeys>0) {
                Set<SelectionKey> keys = selector.selectedKeys();
                for (SelectionKey key : keys) {
                    if(key.isConnectable()){
                        SocketChannel sc = (SocketChannel) key.channel();
                        sc.configureBlocking(false);
                        skey = sc.register(selector,SelectionKey.OP_READ);
                        sc.finishConnect();
                    }else if(key.isAcceptable()){
                        ServerSocketChannel server = (ServerSocketChannel)key.channel();
                        SocketChannel sc = server.accept();
                        if(sc==null){
                            continue;
                        }
                        sc.configureBlocking(false);
                        sc.register(selector,SelectionKey.OP_READ);
                    }
                    else if(key.isReadable()){
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        SocketChannel sc =(SocketChannel)key.channel();
                        int readBytes = 0;
                        try{
                            int ret = sc.read(buffer);
                            if(ret>0){
                                buffer.flip();

                                Charset charset = Charset.forName("utf-8");
                                String receivedMessage = String.valueOf(charset.decode(buffer).array());

                                System.out.println(sc + ": " + receivedMessage);

                            }

                        }finally {
                            if(buffer!=null){
                                buffer.clear();
                            }
                        }
                        buffer.put("hello".getBytes("utf-8"));
                        buffer.flip();
                        sc.write(buffer);
                    }else if(key.isWritable()){
                        key.interestOps(key.interestOps() & (~SelectionKey.OP_WRITE));
                        SocketChannel sc = (SocketChannel)key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(1024);

                        int writtendSize = sc.write(buffer);

                        if(writtendSize==0){
                            key.interestOps(key.interestOps()| SelectionKey.OP_WRITE);
                        }
                    }

                }
                selector.selectedKeys().clear();
            }

        }
    }

    public static void main(String[] args){
        NioServer server = new NioServer();
        try {
            server.init();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
