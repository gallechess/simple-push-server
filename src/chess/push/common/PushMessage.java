package chess.push.common;

/**
 * Push 메시지를 바인딩할 객체 타입
 */
public class PushMessage {

    private String serviceId;	// Push 서비스 구분
    private String groupId;	// Push 수신대상 그룹 구분 (null이면 clientId 기준으로 Push)
    private String clientId;	// Push 수신대상 구분 (null이면 serviceId에 해당하는 Outbound Server에 연결된 전체 클라이언트에게 Push)
    private String message;	// Push 전송할 메시지

    public PushMessage() {}

    /**
     * constructor with parameters
     * @param serviceId Push 서비스 구분 (Outbound Server ID로 사용)
     * @param groupId Push 수신대상 그룹 구분 (null이면 clientId 기준으로 Push)
     * @param clientId Push 수신대상 구분 (null이면 serviceId에 해당하는 Outbound Server에 연결된 전체 클라이언트에게 Push)
     * @param message 메시지 본문
     */
    public PushMessage(String serviceId, String groupId, String clientId, String message) {
        this.serviceId = serviceId;
        this.groupId = groupId;
        this.clientId = clientId;
        this.message = message;
    }

    public String getServiceId() {
        return serviceId;
    }
    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getGroupId() {
        return groupId;
    }
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getClientId() {
        return clientId;
    }
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getClass().getSimpleName()).append("[")
               .append("serviceId=").append(serviceId)
               .append(", groupId=").append(groupId)
               .append(", clientId=").append(clientId)
               .append(", message=").append(message)
               .append("]");
        return builder.toString();
    }

}
