package chess.push.server.property;

import javax.annotation.PostConstruct;

/**
 * Push 서버 모듈의 기본 속성 정의
 */
public class PushBaseProperty {

    private int inboundServerPort;				// Inbound Server listen port
    private int inboundQueueCheckInterval;		// InboundQueue 상태 모니터링 주기 (초)
    private int outboundQueueCheckInterval;		// OutboundQueue 상태 모니터링 주기 (초)

    @PostConstruct
    public void afterPropertiesSet() {
        if (inboundServerPort <= 0) {
            throw new IllegalArgumentException("The 'inboundServerPort' property is invalid [" + inboundServerPort + "]");
        }
        if (inboundQueueCheckInterval <= 0) {
            throw new IllegalArgumentException("The 'inboundQueueCheckInterval' property is invalid [" + inboundQueueCheckInterval + "]");
        }
        if (outboundQueueCheckInterval <= 0) {
            throw new IllegalArgumentException("The 'outboundQueueCheckInterval' property is invalid [" + outboundQueueCheckInterval + "]");
        }
    }

    public int getInboundServerPort() {
        return inboundServerPort;
    }
    public void setInboundServerPort(int inboundServerPort) {
        this.inboundServerPort = inboundServerPort;
    }

    public int getInboundQueueCheckInterval() {
        return inboundQueueCheckInterval;
    }
    public void setInboundQueueCheckInterval(int inboundQueueCheckInterval) {
        this.inboundQueueCheckInterval = inboundQueueCheckInterval;
    }

    public int getOutboundQueueCheckInterval() {
        return outboundQueueCheckInterval;
    }
    public void setOutboundQueueCheckInterval(int outboundQueueCheckInterval) {
        this.outboundQueueCheckInterval = outboundQueueCheckInterval;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getClass().getSimpleName()).append("[")
               .append("inboundServerPort=").append(inboundServerPort)
               .append(", inboundQueueCheckInterval=").append(inboundQueueCheckInterval)
               .append(", outboundQueueCheckInterval=").append(outboundQueueCheckInterval)
               .append("]");
        return builder.toString();
    }

}
