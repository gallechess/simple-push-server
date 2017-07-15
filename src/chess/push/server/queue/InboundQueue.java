package chess.push.server.queue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import chess.push.util.PushMessage;

/**
 * Inbound Server가 수신하는 메시지를 Service ID에 따라 보관하는 큐<br>
 * -Service ID에 따라 개별 인스턴스 존재<br>
 * -큐에 담긴 메시지를 OutboundQueue로 전달하기 위한 쓰레드 동작
 */
public class InboundQueue extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger(InboundQueue.class);

    private final String serviceId;								// Push Service ID
    private final BlockingQueue<PushMessage> queue;				// message queue
    private final int capacity;									// message queue capacity
    private final OutboundQueueManager outboundQueueManager;	// OutboundQueue 인스턴스 관리자

    /**
     * constructor with parameters
     * @param serviceId Push Service ID
     * @param capacity message queue capacity
     * @param outboundQueueManager OutboundQueue 인스턴스 관리자
     */
    public InboundQueue(String serviceId, int capacity, OutboundQueueManager outboundQueueManager) {
        this.serviceId = serviceId;
        this.capacity = capacity;
        this.queue = new LinkedBlockingQueue<PushMessage>(capacity);
        this.outboundQueueManager = outboundQueueManager;
    }

    /**
     * 큐에 메시지를 추가한다.
     * @param message Push 메시지
     */
    public void enqueue(PushMessage message) {
        if (message == null || !serviceId.equals(message.getServiceId())) {
            LOG.error("[InboundQueue:{}] invalid message {}", serviceId, message);
            return;
        }

        boolean result = queue.offer(message);
        if (result) {
            LOG.info("[InboundQueue:{}] enqueued {}", serviceId, message);
        } else {
            LOG.error("[InboundQueue:{}] failed to enqueue {}", serviceId, message);
        }
    }

    /**
     * 큐의 현재 상태를 문자열로 반환한다.
     * @return 큐 상태 문자열
     */
    public String status() {
        return "capacity: " + capacity + ", current: " + queue.size();
    }

    /**
     * InboundQueue 쓰레드가 종료되도록 한다.
     */
    public void shutdown() {
        this.interrupt();
    }

    /**
     * 큐에서 메시지를 추출하여 MessageBroker에 전달한다.
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        setName("InboundQueueThread:" + serviceId);

        LOG.info("[{}] started", getName());

        PushMessage message = null;
        while (!isInterrupted()) {
            try {
                message = queue.take();
                LOG.info("[{}] take {}", getName(), message);
            } catch (InterruptedException e) {
                break;
            }

            if (message != null) {
                try {
                    outboundQueueManager.transfer(message);
                } catch (Exception e) {
                    LOG.error("[" + getName() + "] failed to transfer " + message, e);
                } finally {
                    message = null;
                }
            }
        }

        LOG.info("[{}] shutdown", getName());
    }

}
