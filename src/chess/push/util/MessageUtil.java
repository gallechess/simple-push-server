package chess.push.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class MessageUtil {

    /**
     * Push Message Delimiter
     */
    public static final ByteBuf MSG_DELIMITER = Unpooled.wrappedBuffer(new byte[] { '\r', '\0' });

}
