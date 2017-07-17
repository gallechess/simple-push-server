package chess.push.server.inbound;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import chess.push.common.PushMessage;
import chess.push.server.queue.InboundQueue;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Inbound Server와 송신자간 연결된 채널에서 발생하는 이벤트 처리용 핸들러
 */
public class InboundServerHandler extends SimpleChannelInboundHandler<PushMessage> {

    private static final Logger LOG = LoggerFactory.getLogger(InboundServerHandler.class);

    private final Map<String, InboundQueue> inboundQueues;		// Inbound Queue collection

    /**
     * constructor with a parameter
     * @param inboundQueues Inbound Queue collection
     */
    public InboundServerHandler(Map<String, InboundQueue> inboundQueues) {
        this.inboundQueues = inboundQueues;
    }

    /**
     * 클라이언트와 채널이 연결되어 사용 가능한 상태가 되었을 때 동작<br>
     * -연결 정보 로깅
     * @param ctx ChannelHandlerContext object
     * @see io.netty.channel.ChannelInboundHandlerAdapter#channelActive(io.netty.channel.ChannelHandlerContext)
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        LOG.info("[InboundServerHandler] connected {}", ctx.channel());
        ctx.fireChannelActive();
    }

    /**
     * 클라이언트로부터 메시지 수신했을 때 동작<br>
     * -Service ID에 해당하는 InboundQueue에 추가
     * @param ctx ChannelHandlerContext object
     * @param msg 수신된 메시지
     * @see io.netty.channel.SimpleChannelInboundHandler#channelRead0(io.netty.channel.ChannelHandlerContext, java.lang.Object)
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PushMessage msg) {
        LOG.info("[InboundServerHandler] received {} from {}", msg, ctx.channel());

        // Service ID에 해당하는 Inbound Queue에 메시지 추가
        String serviceId = msg.getServiceId();
        if (inboundQueues.containsKey(serviceId)) {
            inboundQueues.get(serviceId).enqueue(msg);
        } else {
            LOG.warn("[InboundServerHandler] invalid service id in message {}", msg);
        }
    }

    /**
     * 클라이언트와 채널이 해제되어 사용 불가능한 상태가 되었을 때 동작<br>
     * -연결해제 정보 로깅
     * @param ctx ChannelHandlerContext object
     * @see io.netty.channel.ChannelInboundHandlerAdapter#channelInactive(io.netty.channel.ChannelHandlerContext)
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOG.info("[InboundServerHandler] disconnected {}", ctx.channel());
    }

    /**
     * 채널의 I/O 오퍼레이션 도중 예외가 발생했을 때 동작<br>
     * -예외 정보 로깅<br>
     * -채널 연결해제
     * @param ctx ChannelHandlerContext object
     * @param cause 발생한 예외
     * @see io.netty.channel.ChannelInboundHandlerAdapter#exceptionCaught(io.netty.channel.ChannelHandlerContext, java.lang.Throwable)
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOG.error("[InboundServerHandler] error " + ctx.channel() + ", it will be closed", cause);
        ctx.close();
    }

}
