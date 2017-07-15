package chess.push.server.queue;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelId;

/**
 * OutboundQueue 상태 모니터링 쓰레드
 */
public class OutboundQueueChecker extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger(OutboundQueueChecker.class);

    private final OutboundQueueManager outboundQueueManager;	// 서비스ID에 따라 OutboundQueue 그룹을 보관하는 collection
    private final int outboundQueueCheckInterval;				// OutboundQueue 모니터링 주기 (초)

    /**
     * constructor with parameters
     * @param outboundQueueManager 서비스ID에 따라 OutboundQueue 그룹을 보관하는 collection
     * @param outboundQueueCheckInterval OutboundQueue 모니터링 주기 (초)
     */
    public OutboundQueueChecker(OutboundQueueManager outboundQueueManager, int outboundQueueCheckInterval) {
        this.outboundQueueManager = outboundQueueManager;
        this.outboundQueueCheckInterval = outboundQueueCheckInterval;
    }

    /**
     * OutboundQueueChecker 쓰레드가 종료되도록 한다.
     */
    public void shutdown() {
        this.interrupt();
    }

    /**
     * 주기적으로 OutboundQueue 상태정보를 로깅한다.
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        setName("OutboundQueueCheckerThread");

        LOG.info("[{}] started [interval: {}]", getName(), outboundQueueCheckInterval);

        while (!isInterrupted()) {
            StringBuilder builder = new StringBuilder();
            Map<String, Map<ChannelId, OutboundQueue>> outboundQueueGroups = outboundQueueManager.outboundQueueGroups();
            outboundQueueGroups.forEach((serviceId, outboundQueueGroup) -> {
                builder.append("[").append(serviceId).append("]\n");
                if (outboundQueueGroup.isEmpty()) {
                    builder.append("\tNo Outbound Queue\n");
                } else {
                    outboundQueueGroup.forEach((channelId, outboundQueue) -> {
                        builder.append("\t").append(outboundQueue.status()).append("\n");
                    });
                }
            });
            LOG.info("\n* Inbound Queue Status\n{}", builder);

            if (isInterrupted()) {
                break;
            }

            try {
                TimeUnit.SECONDS.sleep(outboundQueueCheckInterval);
            } catch (InterruptedException e) {
                break;
            }
        }

        LOG.info("[{}] shutdown", getName());
    }

}
