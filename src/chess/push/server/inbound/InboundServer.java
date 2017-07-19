package chess.push.server.inbound;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import chess.push.common.PushConstant;
import chess.push.common.PushMessageDecoder;
import chess.push.server.queue.InboundQueue;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.CharsetUtil;

/**
 * Business Application으로부터 Push를 요청받는 Inbound Server<br>
 * -비동기 TCP 통신으로 Push할 메시지 수신
 */
public class InboundServer {

    private static final Logger LOG = LoggerFactory.getLogger(InboundServer.class);

    private final int port;		// Inbound Server listen port

    private EventLoopGroup bossGroup;		// EventLoopGroup that accepts an incoming connection
    private EventLoopGroup workerGroup;	// EventLoopGroup that handles the traffic of the accepted connection

    /**
     * constructor with a paramter
     * @param port Inbound Server listen port
     */
    public InboundServer(int port) {
        this.port = port;
    }

    /**
     * InboundServer 인스턴스를 기동한다.<br>
     * -TCP 스트림에 대한 메시지 구분자 지정<br>
     * -소켓채널에 대한 이벤트 핸들러 지정<br>
     * -소켓옵션 지정
     * @param inboundQueues Inbound Queue collection
     */
    public void startup(Map<String, InboundQueue> inboundQueues) {
        LOG.info("[InboundServer] starting...");

        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                     .channel(NioServerSocketChannel.class)
                     .handler(new LoggingHandler(LogLevel.INFO))
                     .childHandler(new ChannelInitializer<SocketChannel>() {
                         @Override
                         public void initChannel(SocketChannel ch) {
                             ChannelPipeline pipeline = ch.pipeline();
                             pipeline.addLast(new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, PushConstant.DEFAULT_DELIMITER));
                             pipeline.addLast(new StringDecoder(CharsetUtil.UTF_8));
                             pipeline.addLast(new PushMessageDecoder());
                             pipeline.addLast(new InboundServerHandler(inboundQueues));
                         }
                     })
                     .option(ChannelOption.SO_REUSEADDR, true)
                     .childOption(ChannelOption.SO_KEEPALIVE, true)
                     .childOption(ChannelOption.TCP_NODELAY, true);

            bootstrap.bind(port).sync();

            LOG.info("[InboundServer] started, listening on port " + port);

        } catch (InterruptedException e) {
            LOG.error("[InboundServer] failed to startup", e);
            shutdown();
        }
    }

    /**
     * InboundServer 인스턴스를 중지한다.<br>
     * -shutdown worker EventLoopGroup<br>
     * -shutdown boss EventLoopGroup
     */
    public void shutdown() {
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }

        LOG.info("[InboundServer] shutdown");
    }

}
