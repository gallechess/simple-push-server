package chess.push.server.queue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import chess.push.util.PushMessage;

/**
 * Inbound Server가 수신하는 메시지를 분류하는 큐 타입<br>
 * -Service ID에 따라 별도의 인스턴스 존재<br>
 * -큐에 담긴 메시지를 OutboundQueue로 전달하기 위한 쓰레드 동작
 */
public class InboundQueue extends Thread{

    private static final Logger LOG = LoggerFactory.getLogger(InboundQueue.class);

    private String serviceId;					// Push Service ID
    private BlockingQueue<PushMessage> queue;	// message queue

    /**
     * constructor with paramters
     * @param serviceId Push Service ID
     * @param capacity message queue capacity
     */
    public InboundQueue(String serviceId, int capacity) {
        this.serviceId = serviceId;
        queue = new LinkedBlockingQueue<PushMessage>(capacity);
    }

    public String serviceId() {
        return serviceId;
    }

    public int count() {
        return queue.size();
    }

    /**
     * 큐에 메시지를 추가한다.
     * @param message 추가할 Push 메시지
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

    public void shutdown() {
        this.interrupt();
    }

    /**
     * 큐에서 메시지를 추출하여 MessageBroker에 전달한다.
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        this.setName("InboundQueueThread:" + serviceId);

        LOG.info("[{}] thread started", getName());

        PushMessage message = null;
        while (true) {
            try {
                message = queue.take();
                LOG.info("[{}] thread take {}", getName(), message);
            } catch (InterruptedException e) {
                break;
            }

            if (message != null) {
                // TODO MessageBroker에 메시지 전달
                message = null;
            }
        }

        LOG.info("[{}] thread shutdown", getName());
    }

}
