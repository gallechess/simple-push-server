package chess.push.server.outbound;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import chess.push.server.property.PushServiceProperty;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

/**
 * Outbound Server와 클라이언트간 연결된 채널에서 발생하는 이벤트 처리용 핸들러
 */
public class OutboundServerHandler extends SimpleChannelInboundHandler<Object> {

    private static final Logger LOG = LoggerFactory.getLogger(OutboundServerHandler.class);

    private PushServiceProperty property;	// Push Service property

    /**
     * constructor with a parameter
     * @param property Push Service property
     */
    public OutboundServerHandler(PushServiceProperty property) {
        this.property = property;
    }

    /**
     * 클라이언트와 채널이 연결되어 사용 가능한 상태가 되었을 때 동작<br>
     * -연결 정보 로깅
     * @param ctx ChannelHandlerContext object
     * @see io.netty.channel.ChannelInboundHandlerAdapter#channelActive(io.netty.channel.ChannelHandlerContext)
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        LOG.info("[OutboundServer:{}] connected {}", property.getServiceId(), ctx.channel());
        ctx.fireChannelActive();
    }

    /**
     * 클라이언트로부터 메시지 수신했을 때 동작<br>
     * -
     * @param ctx ChannelHandlerContext object
     * @param msg 수신된 메시지
     * @see io.netty.channel.SimpleChannelInboundHandler#channelRead0(io.netty.channel.ChannelHandlerContext, java.lang.Object)
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        String msgStr = ((ByteBuf) msg).toString(CharsetUtil.UTF_8);

        LOG.info("[OutboundServer:{}] received {} from {}", property.getServiceId(), msgStr, ctx.channel());

        // TODO 클라이언트로부터 ID 수신 시 클라이언트 채널에 설정
    }

    /**
     * 클라이언트와 채널이 해제되어 사용 불가능한 상태가 되었을 때 동작<br>
     * -연결해제 정보 로깅
     * @param ctx ChannelHandlerContext object
     * @see io.netty.channel.ChannelInboundHandlerAdapter#channelInactive(io.netty.channel.ChannelHandlerContext)
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOG.info("[OutboundServer:{}] disconnected {}", property.getServiceId(), ctx.channel());
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
        LOG.error("[OutboundServer:" + property.getServiceId() + "] error " + ctx.channel() + ", it will be closed", cause);
        ctx.close();
    }

}
