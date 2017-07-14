package chess.push.server.queue;

import java.util.HashMap;
import java.util.Map;

import chess.push.server.queue.OutboundQueue;
import chess.push.util.PushMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;

// TODO
public class OutboundQueueManager {

    private final Map<String, Map<ChannelId, OutboundQueue>> outboundQueueGroups;

    public OutboundQueueManager() {
        outboundQueueGroups = new HashMap<String, Map<ChannelId, OutboundQueue>>();
    }

    public void addOutboundQueueGroup(String serviceId) {
        synchronized (outboundQueueGroups) {
            if (!outboundQueueGroups.containsKey(serviceId)) {
                outboundQueueGroups.put(serviceId, new HashMap<ChannelId, OutboundQueue>());
            }
        }
    }

    public void startOutboundQueue(String serviceId, int capacity, Channel channel) {
        if (!outboundQueueGroups.containsKey(serviceId)) {
            return;
        }

        OutboundQueue newQueue = OutboundQueue.getInstance(serviceId, capacity, channel);
        newQueue.start();

        Map<ChannelId, OutboundQueue> queueGroup = outboundQueueGroups.get(serviceId);
        synchronized (queueGroup) {
            queueGroup.put(channel.id(), newQueue);
        }
    }

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

    public void transfer(PushMessage message) {
        // TODO PushMessage 확인하여 적절한 OutboundQueue에 메시지 추가
    }

}
