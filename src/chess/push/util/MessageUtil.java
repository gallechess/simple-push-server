package chess.push.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

public class MessageUtil {

    /**
     * Push Message Delimiter (ByteBuf type)
     */
    public static final ByteBuf MSG_DELIMITER = Unpooled.wrappedBuffer(new byte[] { '\r', '\0' });

    /**
     * Push Message Delimiter (String type)
     */
    public static final String MSG_DELIMITER_STR = MSG_DELIMITER.toString(CharsetUtil.UTF_8);

}
