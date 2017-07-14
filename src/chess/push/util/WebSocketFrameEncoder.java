package chess.push.util;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * String 타입의 메시지를 WebSocketFrame 타입으로 인코딩하는 코덱<br>
 * -WebSocketFrame 타입 중 TextWebSocketFrame 타입으로 처리
 */
public class WebSocketFrameEncoder extends MessageToMessageEncoder<String> {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketFrameEncoder.class);

    /**
     * String 타입 메시지를 WebSocketFrame 타입 메시지로 변환한다.<br>
     * -WebSocketFrame 타입 중 TextWebSocketFrame 타입으로 처리한다.
     * @param ctx the {@link ChannelHandlerContext} which this {@link MessageToMessageEncoder} belongs to
     * @param msg the message to encode to an other one
     * @param out the {@link List} into which the encoded msg should be added
     * @see io.netty.handler.codec.MessageToMessageEncoder#encode(io.netty.channel.ChannelHandlerContext, java.lang.Object, java.util.List)
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, String msg, List<Object> out) {
        LOG.info("[WebSocketFrameEncoder] encode {} to channel {}", msg, ctx.channel());

        out.add(new TextWebSocketFrame(msg));
    }

}
