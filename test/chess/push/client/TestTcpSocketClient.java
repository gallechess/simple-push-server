package chess.push.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import chess.push.common.PushConstant;
import chess.push.common.PushMessage;
import chess.push.common.PushMessageDecoder;
import chess.push.common.PushMessageEncoder;
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

public final class TestTcpSocketClient {

    private static final String CLIEND_ID = "testTcpSocketClient1";
    private static final String DEFAULT_OUTBOUND_SERVER_HOST = "127.0.0.1";
    private static final int DEFAULT_OUTBOUND_SERVER_PORT = 8001;

    public static void main(String[] args) throws Exception {
        String clientId = System.getProperty("clientId", CLIEND_ID);
        String outboundServerHost = System.getProperty("inboundServerHost", DEFAULT_OUTBOUND_SERVER_HOST);
        int outboundServerPort = Integer.parseInt(System.getProperty("inboundServerPort", String.valueOf(DEFAULT_OUTBOUND_SERVER_PORT)));

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
                             pipeline.addLast(new TestClientHandler(clientId));
                         }
                     });

            bootstrap.connect(outboundServerHost, outboundServerPort).sync().channel().closeFuture().sync();

        } finally {
            group.shutdownGracefully();
        }
    }
}

class TestClientHandler extends SimpleChannelInboundHandler<PushMessage> {

    private static final Logger LOG = LoggerFactory.getLogger(TestClientHandler.class);

    private final PushMessage clientIdMsg;

    public TestClientHandler(String clientId) {
        this.clientIdMsg = new PushMessage(null, clientId, null);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.channel().writeAndFlush(clientIdMsg);
        LOG.info("[TestClientHandler] sent {} to {}", clientIdMsg, ctx.channel());
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
