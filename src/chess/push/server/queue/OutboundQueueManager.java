package chess.push.server.queue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import chess.push.common.PushMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;

/**
 * 클라이언트 채널에 전송할 메시지를 보관하는 OutboundQueue 인스턴스 라이프사이클 관리자<br>
 * -OutboundQueue는 클라이언트 채널마다 개별 인스턴스 존재
 */
public class OutboundQueueManager {

    // 서비스ID에 따른 OutboundQueue 그룹을 보관하는 collection
    // -OutboundQueue 그룹 내부에서는 Netty Channel 인스턴스의 ChannelId를 key로 하여 관리
    private final Map<String, Map<ChannelId, OutboundQueue>> outboundQueueGroups;

    public OutboundQueueManager() {
        outboundQueueGroups = new HashMap<String, Map<ChannelId, OutboundQueue>>();
    }

    /**
     * 서비스ID에 대한 OutboundQueue 그룹을 생성한다.
     * @param serviceId 서비스ID
     */
    public void addOutboundQueueGroup(String serviceId) {
        synchronized (outboundQueueGroups) {
            if (!outboundQueueGroups.containsKey(serviceId)) {
                outboundQueueGroups.put(serviceId, new HashMap<ChannelId, OutboundQueue>());
            }
        }
    }

    /**
     * 신규 클라이언트 채널에 대한 OutboundQueue 인스턴스를 생성하여 쓰레드를 기동하고 OutboundQueue 그룹에 보관한다.
     * @param serviceId 서비스ID
     * @param capacity queue capacity
     * @param channel 클라이언트 채널
     */
    public void startOutboundQueue(String serviceId, int capacity, Channel channel) {
        if (!outboundQueueGroups.containsKey(serviceId)) {
            return;
        }

        OutboundQueue newQueue = new OutboundQueue(serviceId, capacity, channel);
        newQueue.start();

        Map<ChannelId, OutboundQueue> queueGroup = outboundQueueGroups.get(serviceId);
        synchronized (queueGroup) {
            queueGroup.put(channel.id(), newQueue);
        }
    }

    /**
     * 기존 클라이언트 채널에 대한 OutboundQueue 쓰레드를 종료하고 OutboundQueue 그룹에서 제거한다.
     * @param serviceId 서비스ID
     * @param channel 클라이언트 채널
     */
    public void shutdownOutboundQueue(String serviceId, Channel channel) {
        if (!outboundQueueGroups.containsKey(serviceId)) {
            return;
        }

        Map<ChannelId, OutboundQueue> queueGroup = outboundQueueGroups.get(serviceId);
        if (queueGroup.containsKey(channel.id())) {
            OutboundQueue queue = queueGroup.get(channel.id());
            queue.shutdown();
            synchronized (queueGroup) {
                queueGroup.remove(channel.id());
            }
        }
    }

    /**
     * Push 메시지의 서비스ID, 그룹ID, 클라이언트ID에 따라 해당하는 OutboundQueue에 메시지를 추가한다.<br>
     * -클라이언트ID가 null이 아닌 경우, 해당 클라이언트ID를 갖는 채널의 OutboundQueue에 메시지 추가<br>
     * -클라이언트ID가 null이고 그룹ID가 null이 아닌 경우, 해당 그룹ID를 갖는 모든 채널의 OutboundQueue에 메시지 추가<br>
     * -클라이언트ID와 그룹ID가 모두 null인 경우, 해당 서비스ID를 갖는 모든 채널의 OutboundQueue에 메시지 추가<br>
     * @param pushMessage Push 메시지
     */
    public void transfer(PushMessage pushMessage) {
        String serviceId = pushMessage.getServiceId();
        if (!outboundQueueGroups.containsKey(serviceId)) {
            return;
        }

        Map<ChannelId, OutboundQueue> queueGroup = outboundQueueGroups.get(serviceId);
        String clientId = pushMessage.getClientId();
        if (clientId != null) {
            queueGroup.forEach((channelId, queue) -> {
                if (clientId.equals(queue.clientId())) {
                    queue.enqueue(pushMessage);
                }
            });
        } else {
            String groupId = pushMessage.getGroupId();
            if (groupId != null) {
                 queueGroup.forEach((channelId, queue) -> {
                     if (groupId.equals(queue.groupId())) {
                         queue.enqueue(pushMessage);
                     }
                 });
            } else {
                queueGroup.forEach((channelId, queue) -> {
                    queue.enqueue(pushMessage);
                });
            }
        }
    }

    /**
     * 서비스ID에 따른 OutboundQueue 그룹 collection을 반환한다.
     * @return OutboundQueue 그룹 collection (read-only)
     */
    public Map<String, Map<ChannelId, OutboundQueue>> outboundQueueGroups() {
        return Collections.unmodifiableMap(outboundQueueGroups);
    }

}
