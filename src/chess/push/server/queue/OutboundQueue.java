package chess.push.server.queue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import chess.push.util.ChannelAttrKey;
import chess.push.util.PushMessage;
import io.netty.channel.Channel;

/**
 * Outbound Server에 연결된 클라이언트 채널마다 생성되는 큐 타입<br>
 * -큐에 담긴 메시지를 클라이언트 채널로 전송하기 위한 쓰레드 동작
 */
public class OutboundQueue extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger(OutboundQueue.class);

    private String serviceId;					// Push Service ID
    private BlockingQueue<PushMessage> queue;	// message queue
    private final int capacity;				// message queue capacity
    private Channel channel;					// Client Channel instance

    /**
     * constructor with parameters
     * @param serviceId Push Service ID
     * @param capacity message queue capacity
     * @param channel Netty Channel instance
     * @return OutboundQueue 인스턴스
     */
    public OutboundQueue(String serviceId, int capacity, Channel channel) {
        this.serviceId = serviceId;
        this.capacity = capacity;
        this.queue = new LinkedBlockingQueue<PushMessage>(capacity);
        this.channel = channel;
    }

    /**
     * 큐와 연관된 클라이언트 채널의 클라이언트ID를 반환한다.
     * @return 클라이언트ID
     */
    public String clientId() {
        // 클라이언트ID는 런타임에 변경되므로 항상 채널에서 조회 필요
        return channel.attr(ChannelAttrKey.CLIENT_ID).get();
    }

    /**
     * 큐에 메시지를 추가한다.
     * @param message 추가할 Push 메시지
     */
    public void enqueue(PushMessage message) {
        if (message == null
                || !serviceId.equals(message.getServiceId())
                || (message.getClientId() != null && !message.getClientId().equals(clientId()))) {
            LOG.error("[OutboundQueue:{}] [{}] invalid message {}", serviceId, clientId(), message);
            return;
        }

        boolean result = queue.offer(message);
        if (result) {
            LOG.info("[OutboundQueue:{}] [{}] enqueued {}", serviceId, clientId(), message);
        } else {
            LOG.error("[OutboundQueue:{}] [{}] failed to enqueue {}", serviceId, clientId(), message);
        }
    }

    /**
     * 큐의 현재 상태를 문자열로 반환한다.
     * @return 큐 상태
     */
    public String status() {
        return "clientId: " + clientId() + ", channel: " + channel + ", capacity: " + capacity + ", current: " + queue.size();
    }

    /**
     * OutboundQueue 쓰레드가 종료되도록 한다.
     */
    public void shutdown() {
        this.interrupt();
    }

    /**
     * 큐에서 메시지를 추출하여 클라이언트 채널에 전송한다.
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        setName("OutboundQueueThread:" + serviceId);

        LOG.info("[{}] [{}] started", getName(), clientId());

        PushMessage message = null;
        while (!isInterrupted()) {
            try {
                message = queue.take();
                LOG.info("[{}] [{}] take {}", getName(), clientId(), message);
            } catch (InterruptedException e) {
                break;
            }

            if (message != null) {
                channel.writeAndFlush(message);
                message = null;
            }
        }

        LOG.info("[{}] [{}] shutdown", getName(), clientId());
    }

}
