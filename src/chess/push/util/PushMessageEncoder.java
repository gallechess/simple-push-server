package chess.push.util;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

/**
 * PushMessage 타입의 메시지를 String 타입으로 인코딩하는 코덱
 */
public class PushMessageEncoder extends MessageToMessageEncoder<PushMessage> {

    private static final Logger LOG = LoggerFactory.getLogger(PushMessageEncoder.class);

    private String delimiter;

    /**
     * default constructor
     */
    public PushMessageEncoder() {}

    /**
     * constructor with a parameter
     * @param delimiter 인코딩 메시지에 추가할 delimiter
     */
    public PushMessageEncoder(String delimiter) {
        this.delimiter = delimiter;
    }

    /**
     * PushMessage 타입 메시지를 String 타입 메시지(JSON 문자열)로 변환한다.<br>
     * -지정된 delimiter가 있으면 String 타입 메시지 끝에 delimiter를 추가한다.
     * @param ctx the {@link ChannelHandlerContext} which this {@link MessageToMessageEncoder} belongs to
     * @param msg the message to encode to an other one
     * @param out the {@link List} into which the encoded msg should be added
     * @see io.netty.handler.codec.MessageToMessageEncoder#encode(io.netty.channel.ChannelHandlerContext, java.lang.Object, java.util.List)
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, PushMessage msg, List<Object> out) {
        LOG.debug("[PushMessageEncoder] encode {} to channel {}", msg, ctx.channel());

        String encoded = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            encoded = mapper.writeValueAsString(msg);
            if (delimiter != null) {
                encoded += delimiter;
            }
        } catch (IOException e) {
            LOG.error("[PushMessageEncoder] failed to encode " + msg, e);
        }

        if (encoded != null) {
            out.add(encoded);
        }
    }

}
