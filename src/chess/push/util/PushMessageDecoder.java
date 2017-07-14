package chess.push.util;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

/**
 * String 타입의 메시지를 PushMessage 타입으로 디코딩하는 코덱
 */
public class PushMessageDecoder extends MessageToMessageDecoder<String> {

    private static final Logger LOG = LoggerFactory.getLogger(PushMessageDecoder.class);

    /**
     * String 타입 메시지(JSON 문자열)를 PushMessage 타입으로 변환한다.
     * @param ctx the {@link ChannelHandlerContext} which this {@link MessageToMessageDecoder} belongs to
     * @param msg the message to decode to an other one
     * @param out the {@link List} to which decoded messages should be added
     * @see io.netty.handler.codec.MessageToMessageDecoder#decode(io.netty.channel.ChannelHandlerContext, java.lang.Object, java.util.List)
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, String msg, List<Object> out) {
        LOG.info("[PushMessageDecoder] decode {} from channel {}", msg, ctx.channel());

        PushMessage decoded = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            decoded = mapper.readValue(msg, PushMessage.class);
        } catch (IOException e) {
            LOG.error("[PushMessageDecoder] failed to decode " + msg, e);
        }

        if (decoded != null) {
            out.add(decoded);
        }
    }

}
