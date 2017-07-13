package chess.push.server.inbound;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import chess.push.server.queue.InboundQueue;
import chess.push.util.PushMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

/**
 * Inbound Server와 클라이언트간 연결된 채널에서 발생하는 이벤트 처리용 핸들러
 */
public class InboundServerHandler extends SimpleChannelInboundHandler<Object> {

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
        LOG.info("[InboundServer] connected {}", ctx.channel());
        ctx.fireChannelActive();
    }

    /**
     * 클라이언트로부터 메시지 수신했을 때 동작<br>
     * -JSON 메시지를 객체로 바인딩<br>
     * -InboundQueue에 추가
     * @param ctx ChannelHandlerContext object
     * @param msg 수신된 메시지
     * @see io.netty.channel.SimpleChannelInboundHandler#channelRead0(io.netty.channel.ChannelHandlerContext, java.lang.Object)
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        String msgStr = ((ByteBuf) msg).toString(CharsetUtil.UTF_8);

        LOG.info("[InboundServer] received {} from {}", msgStr, ctx.channel());

        PushMessage pushMessage = bindMessage(msgStr);
        if (pushMessage == null) {
            LOG.warn("[InboundServer] invalid message {}", pushMessage);
            return;
        }

        // Service ID에 해당하는 Inbound Queue에 메시지 추가
        String serviceId = pushMessage.getServiceId();
        if (inboundQueues.containsKey(serviceId)) {
            inboundQueues.get(serviceId).enqueue(pushMessage);
        } else {
            LOG.warn("[InboundServer] invalid service id in message {}", pushMessage);
        }
    }

    private PushMessage bindMessage(String jsonStr) {
        PushMessage pushMessage = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            pushMessage = mapper.readValue(jsonStr, PushMessage.class);
        } catch (IOException e) {
            LOG.error("[InboundServer] failed to bind a message " + jsonStr, e);
        }
        return pushMessage;
    }

    /**
     * 클라이언트와 채널이 해제되어 사용 불가능한 상태가 되었을 때 동작<br>
     * -연결해제 정보 로깅
     * @param ctx ChannelHandlerContext object
     * @see io.netty.channel.ChannelInboundHandlerAdapter#channelInactive(io.netty.channel.ChannelHandlerContext)
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOG.info("[InboundServer] disconnected {}", ctx.channel());
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
        LOG.error("[InboundServer] error " + ctx.channel() + ", it will be closed", cause);
        ctx.close();
    }

}
