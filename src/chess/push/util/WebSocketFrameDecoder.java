package chess.push.util;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

/**
 * WebSocketFrame 타입의 메시지를 String 타입으로 디코딩하는 코덱<br>
 * -WebSocketFrame 타입 중 TextWebSocketFrame 타입만 처리
 */
public class WebSocketFrameDecoder extends MessageToMessageDecoder<WebSocketFrame> {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketFrameDecoder.class);

    /**
     * WebSocketFrame 타입 메시지를 String 타입으로 변환한다.<br>
     * -WebSocketFrame 타입 중 TextWebSocketFrame 타입만 처리하며, 다른 타입은 {@link UnsupportedOperationException}를 발생시킨다.
     * @param ctx the {@link ChannelHandlerContext} which this {@link MessageToMessageDecoder} belongs to
     * @param msg the message to decode to an other one
     * @param out the {@link List} to which decoded messages should be added
     * @see io.netty.handler.codec.MessageToMessageDecoder#decode(io.netty.channel.ChannelHandlerContext, java.lang.Object, java.util.List)
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, WebSocketFrame frame, List<Object> out) {
        if (frame instanceof TextWebSocketFrame) {
            String decoded = ((TextWebSocketFrame) frame).text();
            LOG.info("[WebSocketFrameDecoder] decoded {} from channel {}", decoded, ctx.channel());
            out.add(decoded);
        } else {
            throw new UnsupportedOperationException("Unsupported frame type [" + frame.getClass().getName() + "]");
        }
    }

}
