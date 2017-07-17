package chess.push.server.outbound;

import chess.push.common.PushMessageDecoder;
import chess.push.common.PushMessageEncoder;
import chess.push.common.WebSocketFrameDecoder;
import chess.push.common.WebSocketFrameEncoder;
import chess.push.server.property.PushServiceProperty;
import chess.push.server.queue.OutboundQueueManager;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;

/**
 * WebSocket 통신을 사용하는 Outbound Server 타입
 */
public class OutboundWebSocketServer extends OutboundServer {

    private final PushServiceProperty property;					// Push Service property
    private final OutboundQueueManager outboundQueueManager;	// OutboundQueue 인스턴스 관리자

    /**
     * constructor with parameters
     * @param property Push Service property
     * @param outboundQueueManager OutboundQueue 인스턴스 관리자
     */
    public OutboundWebSocketServer(PushServiceProperty property, OutboundQueueManager outboundQueueManager) {
        super(property);
        this.property = property;
        this.outboundQueueManager = outboundQueueManager;
    }

    /**
     * WebSocket 통신용 이벤트 핸들러를 설정하는 ChannelInitializer 인스턴스를 생성한다.<br>
     * @return ChannelInitializer 인스턴스
     * @see chess.push.server.outbound.OutboundServer#getChannelInitializer()
     */
    @Override
    protected ChannelInitializer<SocketChannel> getChannelInitializer() {
        return new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel socketChannel) {
                ChannelPipeline pipeline = socketChannel.pipeline();
                pipeline.addLast(new HttpServerCodec());
                pipeline.addLast(new HttpObjectAggregator(65536));
                pipeline.addLast(new WebSocketServerCompressionHandler());
                pipeline.addLast(new WebSocketServerProtocolHandler(property.getOutboundServerWsUri(), null, true));
                // 필요시 HTTP 요청 핸들러 설정
                // pipeline.addLast(???);
                pipeline.addLast(new WebSocketFrameDecoder(), new WebSocketFrameEncoder());
                pipeline.addLast(new PushMessageDecoder(), new PushMessageEncoder());
                pipeline.addLast(new OutboundServerHandler(property, outboundQueueManager));
            }
        };
    }

}
