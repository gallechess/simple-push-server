package chess.push.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;

/**
 * 상수값 정의
 */
public class PushConstant {

    /**
     * Push Message Default Delimiter (ByteBuf type)
     */
    public static final ByteBuf DEFAULT_DELIMITER = Unpooled.wrappedBuffer(new byte[] { '\r', '\0' });

    /**
     * Push Message Default Delimiter (String type)
     */
    public static final String DEFAULT_DELIMITER_STR = DEFAULT_DELIMITER.toString(CharsetUtil.UTF_8);

    /**
     * 클라이언트 채널에 설정되는 클라이언트ID 속성에 대한 key
     */
    public static final AttributeKey<String> CLIENT_ID = AttributeKey.newInstance("clientId");

}
