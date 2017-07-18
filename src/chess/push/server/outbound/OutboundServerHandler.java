package chess.push.server.outbound;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import chess.push.common.PushConstant;
import chess.push.common.PushMessage;
import chess.push.server.property.PushServiceProperty;
import chess.push.server.queue.OutboundQueueManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Outbound Server와 클라이언트간 채널에서 발생하는 이벤트 처리용 핸들러
 */
public class OutboundServerHandler extends SimpleChannelInboundHandler<PushMessage> {

    private static final Logger LOG = LoggerFactory.getLogger(OutboundServerHandler.class);

    private final PushServiceProperty property;					// Push Service property
    private final OutboundQueueManager outboundQueueManager;	// OutboundQueue 인스턴스 관리자

    /**
     * constructor with parameters
     * @param property Push Service property
     * @param outboundQueueManager OutboundQueue 인스턴스 관리자
     */
    public OutboundServerHandler(PushServiceProperty property, OutboundQueueManager outboundQueueManager) {
        this.property = property;
        this.outboundQueueManager = outboundQueueManager;
    }

    /**
     * 클라이언트 채널이 연결되어 사용 가능한 상태가 되었을 때 동작<br>
     * -연결 정보 로깅<br>
     * -OutboundQueue 관리자에게 신규 OutboundQueue 시작 요청
     * @param ctx ChannelHandlerContext object
     * @see io.netty.channel.ChannelInboundHandlerAdapter#channelActive(io.netty.channel.ChannelHandlerContext)
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        LOG.info("[OutboundServerHandler:{}] connected {}", property.getServiceId(), ctx.channel());

        outboundQueueManager.startOutboundQueue(property.getServiceId(), property.getOutboundQueueCapacity(), ctx.channel());

        ctx.fireChannelActive();
    }

    /**
     * 클라이언트로부터 메시지 수신했을 때 동작<br>
     * -클라이언트로부터의 메시지는 ID 전송으로 간주하여 채널에 그룹ID, 클라이언트ID 설정
     * @param ctx ChannelHandlerContext object
     * @param msg 수신 메시지
     * @see io.netty.channel.SimpleChannelInboundHandler#channelRead0(io.netty.channel.ChannelHandlerContext, java.lang.Object)
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PushMessage msg) {
        LOG.info("[OutboundServerHandler:{}] received {} from {}", property.getServiceId(), msg, ctx.channel());

        String groupId = msg.getGroupId();
        if (groupId != null) {
            ctx.channel().attr(PushConstant.GROUP_ID).set(groupId);
            LOG.info("[OutboundServerHandler:{}] set group id [{}] to {}", property.getServiceId(), groupId, ctx.channel());
        }

        String clientId = msg.getClientId();
        if (clientId != null) {
            ctx.channel().attr(PushConstant.CLIENT_ID).set(clientId);
            LOG.info("[OutboundServerHandler:{}] set client id [{}] to {}", property.getServiceId(), clientId, ctx.channel());
        }
    }

    /**
     * 클라이언트 채널이 연결해제되어 사용 불가능한 상태가 되었을 때 동작<br>
     * -연결해제 정보 로깅<br>
     * -OutboundQueue 관리자에게 관련 OutboundQueue 종료 요청
     * @param ctx ChannelHandlerContext object
     * @see io.netty.channel.ChannelInboundHandlerAdapter#channelInactive(io.netty.channel.ChannelHandlerContext)
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        LOG.info("[OutboundServerHandler:{}] disconnected {}", property.getServiceId(), ctx.channel());

        outboundQueueManager.shutdownOutboundQueue(property.getServiceId(), ctx.channel());
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
        LOG.error("[OutboundServerHandler:" + property.getServiceId() + "] error " + ctx.channel() + ", it will be closed", cause);
        ctx.close();
    }

}
