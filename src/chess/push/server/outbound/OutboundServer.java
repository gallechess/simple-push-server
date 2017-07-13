package chess.push.server.outbound;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import chess.push.server.property.PushServiceProperty;
import chess.push.server.property.ServerType;
import chess.push.util.MessageUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * 클라이언트와의 연결을 대기하는 Outbound Server
 */
public class OutboundServer {

    private static final Logger LOG = LoggerFactory.getLogger(OutboundServer.class);

    private PushServiceProperty property;	// Push Service property
    private EventLoopGroup bossGroup;		// EventLoopGroup that accepts an incoming connection
    private EventLoopGroup workerGroup;	// EventLoopGroup that handles the traffic of the accepted connection
    private ChannelFuture channelFuture;	// Outbound Server channel asynchronous bind result

    /**
     * constructor with a parameter
     * @param property Push Service property
     */
    public OutboundServer(PushServiceProperty property) {
        this.property = property;
    }

    /**
     * OutboundServer 인스턴스를 기동한다.<br>
     * -통신방식이 TCP Socket인 경우 TCP 스트림에 대한 메시지 구분자 지정<br>
     * -소켓채널에 대한 이벤트 핸들러 지정<br>
     * -소켓옵션 지정
     */
    public void startup() {
        LOG.info("[OutboundServer:{}] starting...", property.getServiceId());

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
                             if (property.getOutboundServerType() == ServerType.TCPSOCKET) {
                                 pipeline.addLast(new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, MessageUtil.MSG_DELIMITER));
                                 pipeline.addLast(new OutboundServerHandler(property));
                             } else if (property.getOutboundServerType() == ServerType.WEBSOCKET) {
                                 // TODO WebSocket 타입 서버 핸들러 설정
                             }
                         }
                     })
                     .option(ChannelOption.SO_REUSEADDR, true)
                     .childOption(ChannelOption.SO_KEEPALIVE, true)
                     .childOption(ChannelOption.TCP_NODELAY, true);

            channelFuture = bootstrap.bind(property.getOutboundServerPort()).sync();

            LOG.info("[OutboundServer:{}] started, listening on port {}", property.getServiceId(), property.getOutboundServerPort());

        } catch (InterruptedException e) {
            LOG.error("[OutboundServer:" + property.getServiceId() + "] failed to startup", e);
            shutdown();
        }
    }

    /**
     * OutboundServer 인스턴스를 중지한다.<br>
     * -close Outbound Server channel<br>
     * -shutdown worker EventLoopGroup<br>
     * -shutdown boss EventLoopGroup
     */
    public void shutdown() {
        if (channelFuture != null) {
            Channel channel = channelFuture.channel();
            if (channel != null) {
                try {
                    channel.closeFuture().sync();
                } catch (InterruptedException e) {
                    LOG.error("[OutboundServer:" + property.getServiceId() + "] interrupted during closing channel " + channel, e);
                }
            }
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
    }

}
