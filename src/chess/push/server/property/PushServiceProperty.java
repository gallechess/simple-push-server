package chess.push.server.property;

import javax.annotation.PostConstruct;

/**
 * Push 서비스 속성을 설정하는 타입
 */
public class PushServiceProperty {

    private String serviceId;				// Push Service ID
    private int inboundQueueCapacity;		// Inbound Message Queue capacity
    private int outboundQueueCapacity;		// Outbound Message Queue capacity
    private int outboundServerPort;		// Outbound Server listen port
    private ServerType outboundServerType;	// Outbound Server communication type
    private String outboundServerWsUri;	// Outbound Server WebSocket URI, if Outbound Server type is WEBSOCKET

    @PostConstruct
    public void afterPropertiesSet() {
        if (serviceId == null) {
            throw new IllegalArgumentException("The 'serviceId' property is null");
        }
        if (inboundQueueCapacity <= 0) {
            throw new IllegalArgumentException("The 'inboundQueueCapacity' property is invalid [" + inboundQueueCapacity + "]");
        }
        if (outboundQueueCapacity <= 0) {
            throw new IllegalArgumentException("The 'inboundQueueCapacity' property is invalid [" + outboundQueueCapacity + "]");
        }
        if (outboundServerPort <= 0) {
            throw new IllegalArgumentException("The 'outboundServerPort' property is invalid [" + outboundServerPort + "]");
        }
        if (outboundServerType == null) {
            throw new IllegalArgumentException("The 'outboundServerType' property is null");
        }
        if (outboundServerType == ServerType.WEBSOCKET && outboundServerWsUri == null) {
            throw new IllegalArgumentException("The 'outboundServerWsUri' property is null");
        }
    }

    public String getServiceId() {
        return serviceId;
    }
    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public int getInboundQueueCapacity() {
        return inboundQueueCapacity;
    }
    public void setInboundQueueCapacity(int inboundQueueCapacity) {
        this.inboundQueueCapacity = inboundQueueCapacity;
    }

    public int getOutboundQueueCapacity() {
        return outboundQueueCapacity;
    }
    public void setOutboundQueueCapacity(int outboundQueueCapacity) {
        this.outboundQueueCapacity = outboundQueueCapacity;
    }

    public int getOutboundServerPort() {
        return outboundServerPort;
    }
    public void setOutboundServerPort(int outboundServerPort) {
        this.outboundServerPort = outboundServerPort;
    }

    public ServerType getOutboundServerType() {
        return outboundServerType;
    }
    public void setOutboundServerType(ServerType outboundServerType) {
        this.outboundServerType = outboundServerType;
    }

    public String getOutboundServerWsUri() {
        return outboundServerWsUri;
    }
    public void setOutboundServerWsUri(String outboundServerWsUri) {
        if (outboundServerWsUri != null && !outboundServerWsUri.startsWith("/")) {
            this.outboundServerWsUri = "/" + outboundServerWsUri;
        } else {
            this.outboundServerWsUri = outboundServerWsUri;
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getClass().getSimpleName()).append("[")
               .append("serviceId=").append(serviceId)
               .append(", inboundQueueCapacity=").append(inboundQueueCapacity)
               .append(", outboundQueueCapacity=").append(outboundQueueCapacity)
               .append(", outboundServerPort=").append(outboundServerPort)
               .append(", outboundServerType=").append(outboundServerType)
               .append(", outboundServerWsUri=").append(outboundServerWsUri)
               .append("]");
        return builder.toString();
    }

}
