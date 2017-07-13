package chess.push.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import chess.push.util.MessageUtil;
import chess.push.util.PushMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
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
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.util.CharsetUtil;

public final class TestClient {

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 8000;

    private static final PushMessage TEST_MSG = new PushMessage("tcpsocket.test1", null, "1234 abcd ~!@# 테스트 메시지");
    private static final int TEST_COUNT = 10;

    public static void main(String[] args) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                     .channel(NioSocketChannel.class)
                     .handler(new ChannelInitializer<SocketChannel>() {
                         @Override
                         protected void initChannel(SocketChannel ch) throws Exception {
                             ChannelPipeline pipeline = ch.pipeline();
                             pipeline.addLast(new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, MessageUtil.MSG_DELIMITER));
                             pipeline.addLast(new TestClientHandler(TEST_MSG, TEST_COUNT));
                         }
                     });

            bootstrap.connect(HOST, PORT).sync().channel().closeFuture().sync();

        } finally {
            group.shutdownGracefully();
        }
    }
}

class TestClientHandler extends SimpleChannelInboundHandler<Object> {

    private static final Logger LOG = LoggerFactory.getLogger(TestClientHandler.class);

    private final PushMessage testMsg;
    private final int testCount;

    public TestClientHandler(PushMessage testMsg, int testCount) {
        this.testMsg = testMsg;
        this.testCount = testCount;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String jsonStr = mapper.writeValueAsString(testMsg);
        ChannelFuture channelFuture = null;
        for (int idx = 0; idx < testCount; idx++) {
            ByteBuf testBuf = Unpooled.wrappedBuffer((jsonStr + MessageUtil.MSG_DELIMITER.toString(CharsetUtil.UTF_8)).getBytes(CharsetUtil.UTF_8));
            channelFuture = ctx.channel().writeAndFlush(testBuf);
            LOG.info("[TestClient] sent[{}]: {}", idx, jsonStr);
        }
        LOG.info("[TestClient] channel closing...");
        channelFuture.addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        // do nothing
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOG.error("[TestClient] error " + ctx.channel() + ", it will be closed", cause);
        ctx.close();
    }

}
