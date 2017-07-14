package chess.push.server.outbound;

import chess.push.server.property.PushServiceProperty;
import chess.push.server.property.ServerType;
import chess.push.server.queue.OutboundQueueManager;

/**
 * Push 서비스 속성에 따른 Outbound Server 인스턴스 factory
 */
public class OutboundServerFactory {

    /**
     * Push 서비스 속성에 따라 Outbound Server 인스턴스를 생성한다.
     * @param property Push 서비스 속성 정보
     * @param outboundQueueManager 클라이언트 채널마다 생성될 OutboundQueue 인스턴스 관리자
     * @return Outbound Server 인스턴스
     */
    public static OutboundServer getInstance(PushServiceProperty property, OutboundQueueManager outboundQueueManager) {
        if (property == null) {
            throw new IllegalArgumentException("The PushServiceProperty argument is null");
        }

        ServerType type = property.getOutboundServerType();
        if (type == null) {
            throw new IllegalArgumentException("The outbound server type is null");
        }

        switch (type) {
            case TCPSOCKET:
                return new OutboundTcpSocketServer(property, outboundQueueManager);
            case WEBSOCKET:
                return new OutboundWebSocketServer(property, outboundQueueManager);
            default:
                throw new IllegalArgumentException("Unknown server type [" + type + "]");
        }
    }

}
