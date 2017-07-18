package chess.push.sender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import chess.push.common.PushConstant;
import chess.push.common.PushMessage;
import chess.push.common.PushMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

public final class TestWebSocketSender {

    private static final String TEST_SERVICE_ID = "test2.websocket";
    private static final String TEST_GROUP_ID = null;
//    private static final String TEST_GROUP_ID = "testGroup1";
    private static final String TEST_CLIENT_ID = null;
//    private static final String TEST_CLIENT_ID = "testWebSocketClient1";
    private static final int TEST_COUNT = 1000;
    private static final String DEFAULT_INBOUND_SERVER_HOST = "127.0.0.1";
    private static final int DEFAULT_INBOUND_SERVER_PORT = 8000;

    public static void main(String[] args) throws Exception {
        String testServiceId = System.getProperty("testServiceId", TEST_SERVICE_ID);
        String testGroupId = System.getProperty("testGroupId", TEST_GROUP_ID);
        String testClientId = System.getProperty("testClientId", TEST_CLIENT_ID);
        int testCount = Integer.parseInt(System.getProperty("testCount", String.valueOf(TEST_COUNT)));

        String inboundServerHost = System.getProperty("inboundServerHost", DEFAULT_INBOUND_SERVER_HOST);
        int inboundServerPort = Integer.parseInt(System.getProperty("inboundServerPort", String.valueOf(DEFAULT_INBOUND_SERVER_PORT)));

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                     .channel(NioSocketChannel.class)
                     .handler(new ChannelInitializer<SocketChannel>() {
                         @Override
                         protected void initChannel(SocketChannel ch) throws Exception {
                             ChannelPipeline pipeline = ch.pipeline();
                             pipeline.addLast(new StringEncoder(CharsetUtil.UTF_8));
                             pipeline.addLast(new PushMessageEncoder(PushConstant.DEFAULT_DELIMITER_STR));
                             pipeline.addLast(new TestWebSocketSenderHandler());
                         }
                     });

            ChannelFuture future = bootstrap.connect(inboundServerHost, inboundServerPort);

            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    for (int cnt = 1; cnt <= testCount; cnt++) {
                        PushMessage message = new PushMessage(testServiceId, testGroupId, testClientId, "WebSocket test message [" + cnt + "]");
                        future.channel().writeAndFlush(message);
                        Thread.sleep(10L);
                    }
                }
            }).addListener(ChannelFutureListener.CLOSE).sync();

        } finally {
            group.shutdownGracefully();
        }
    }
}

class TestWebSocketSenderHandler extends SimpleChannelInboundHandler<PushMessage> {

    private static final Logger LOG = LoggerFactory.getLogger(TestWebSocketSenderHandler.class);

    @Override
    public void channelRead0(ChannelHandlerContext ctx, PushMessage msg) throws Exception {
        // do nothing
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOG.error("[TestWebSocketSenderHandler] error " + ctx.channel() + ", it will be closed", cause);
        ctx.close();
    }

}
