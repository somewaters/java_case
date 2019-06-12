package xuji.RpcNioUpdate;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.Set;

public class NioClient {
    public static SocketChannel channel;
    public void init() throws IOException {
        channel = SocketChannel.open();
        channel.configureBlocking(false);
        channel.connect(new InetSocketAddress(1234));
        Selector selector = Selector.open();
        channel.register(selector, SelectionKey.OP_CONNECT);
        int nkeys = selector.select();
        SelectionKey skey = null;
        if(nkeys>0) {
            Set<SelectionKey> keys = selector.selectedKeys();
            for (SelectionKey key : keys) {
                if(key.isConnectable()){
                    channel = (SocketChannel) key.channel();
                    channel.configureBlocking(false);
                    skey = channel.register(selector,SelectionKey.OP_READ);
                    channel.finishConnect();
                }else if(key.isReadable()){
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

    public void send(ByteBuffer buffer) throws IOException {
        channel.write(buffer);

    }

    public static void main(String[] args){
        NioClient nioClient = new NioClient();
        try {
            nioClient.init();
            ByteBuffer writeBuffer = ByteBuffer.allocate(1024);
            writeBuffer.put((LocalDateTime.now() + " 连接成功").getBytes());
            writeBuffer.flip();
            channel.write(writeBuffer);
            while(channel.finishConnect()){
                writeBuffer.clear();
                writeBuffer.put("hello".getBytes("utf-8"));
                writeBuffer.flip();
                channel.write(writeBuffer);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
