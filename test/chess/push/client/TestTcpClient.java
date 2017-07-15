package chess.push.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import chess.push.util.PushConstant;
import chess.push.util.PushMessage;
import chess.push.util.PushMessageDecoder;
import chess.push.util.PushMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

public final class TestTcpClient {

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 8100;

    private static final PushMessage CLIEND_ID = new PushMessage("tcpsocket.test1", "testClient1", null);

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
                             pipeline.addLast(new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, PushConstant.DEFAULT_DELIMITER));
                             pipeline.addLast(new StringDecoder(CharsetUtil.UTF_8), new StringEncoder(CharsetUtil.UTF_8));
                             pipeline.addLast(new PushMessageDecoder(), new PushMessageEncoder(PushConstant.DEFAULT_DELIMITER_STR));
                             pipeline.addLast(new TestClientHandler(CLIEND_ID));
                         }
                     });

            bootstrap.connect(HOST, PORT).sync().channel().closeFuture().sync();

        } finally {
            group.shutdownGracefully();
        }
    }
}

class TestClientHandler extends SimpleChannelInboundHandler<PushMessage> {

    private static final Logger LOG = LoggerFactory.getLogger(TestClientHandler.class);

    private final PushMessage clientId;

    public TestClientHandler(PushMessage clientId) {
        this.clientId = clientId;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.channel().writeAndFlush(clientId);
        LOG.info("[TestClientHandler] sent {} to {}", clientId, ctx.channel());
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, PushMessage msg) throws Exception {
        LOG.info("[TestClientHandler] received {} from {}", msg, ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOG.error("[TestClientHandler] error " + ctx.channel() + ", it will be closed", cause);
        ctx.close();
    }

}
