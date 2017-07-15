package chess.push.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import chess.push.util.PushConstant;
import chess.push.util.PushMessage;
import chess.push.util.PushMessageEncoder;
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

// TODO Sender - 웹소켓 서버의 테스트 페이지에서 부하 발생시키도록 변경
public final class TestSender {

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 8000;

    private static final PushMessage TEST_MSG = new PushMessage("websocket.test1", null, "1234 abcd ~!@# 테스트 메시지");
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
                             pipeline.addLast(new StringEncoder(CharsetUtil.UTF_8));
                             pipeline.addLast(new PushMessageEncoder(PushConstant.DEFAULT_DELIMITER_STR));
                             pipeline.addLast(new TestSenderHandler(TEST_MSG, TEST_COUNT));
                         }
                     });

            bootstrap.connect(HOST, PORT).sync().channel().closeFuture().sync();

        } finally {
            group.shutdownGracefully();
        }
    }
}

class TestSenderHandler extends SimpleChannelInboundHandler<PushMessage> {

    private static final Logger LOG = LoggerFactory.getLogger(TestSenderHandler.class);

    private final PushMessage testMsg;
    private final int testCount;

    public TestSenderHandler(PushMessage testMsg, int testCount) {
        this.testMsg = testMsg;
        this.testCount = testCount;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ChannelFuture channelFuture = null;
        for (int idx = 0; idx < testCount; idx++) {
            channelFuture = ctx.channel().writeAndFlush(testMsg);
            LOG.info("[TestSenderHandler] sent {} to {}", testMsg, ctx.channel());
        }
        LOG.info("[TestSenderHandler] channel closing...");
        channelFuture.addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, PushMessage msg) throws Exception {
        LOG.info("[TestSenderHandler] received {} from {}", msg, ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOG.error("[TestSenderHandler] error " + ctx.channel() + ", it will be closed", cause);
        ctx.close();
    }

}
