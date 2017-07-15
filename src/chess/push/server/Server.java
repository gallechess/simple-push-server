package chess.push.server;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import chess.push.server.inbound.InboundServer;
import chess.push.server.outbound.OutboundServer;
import chess.push.server.outbound.OutboundServerFactory;
import chess.push.server.property.PushBaseProperty;
import chess.push.server.property.PushServiceProperty;
import chess.push.server.queue.InboundQueue;
import chess.push.server.queue.InboundQueueChecker;
import chess.push.server.queue.OutboundQueueChecker;
import chess.push.server.queue.OutboundQueueManager;

/**
 * simple-push-server 서버 라이프사이클 관리
 */
public class Server {

    private static final Logger LOG = LoggerFactory.getLogger(Server.class);

    private final Map<String, OutboundServer> outboundServers;		// Service ID를 key로 하는 OutboundServer collection
    private final Map<String, InboundQueue> inboundQueues;			// Service ID를 key로 하는 InboundQueue collection
    private final OutboundQueueManager outboundQueueManager;		// 클라이언트 채널마다 생성될 OutboundQueue 인스턴스 관리자

    private OutboundQueueChecker outboundQueueChecker;
    private InboundQueueChecker inboundQueueChecker;
    private InboundServer inboundServer;

    public Server() {
        outboundServers = new HashMap<String, OutboundServer>();
        inboundQueues = new HashMap<String, InboundQueue>();
        outboundQueueManager = new OutboundQueueManager();
    }

    /**
     * 서버 컴포넌트 기동
     * @param baseProperty Push 서버 컴포너트 기본 속성 정보
     * @param serviceProperties Push 서비스 속성 정보 collection
     */
    public void startupServer(PushBaseProperty baseProperty, Collection<PushServiceProperty> serviceProperties) {
        LOG.info("[simple-push-server] starting...");

        if (!serviceProperties.isEmpty()) {
            // Push 서비스 속성 정보에 따라 필요한 인스턴스 생성하고 Service ID를 key로 하는 collection에 저장
            serviceProperties.forEach(property -> {
                String serviceId = property.getServiceId();
                outboundServers.put(serviceId, OutboundServerFactory.getInstance(property, outboundQueueManager));
                inboundQueues.put(serviceId, new InboundQueue(serviceId, property.getInboundQueueCapacity(), outboundQueueManager));
                outboundQueueManager.addOutboundQueueGroup(serviceId);
            });
        }

        // startup OutboundServers
        outboundServers.forEach((serviceId, outboundServer) -> outboundServer.startup());

        // startup InboundQueue threads
        inboundQueues.forEach((serviceId, inboundQueue) -> inboundQueue.start());

        // startup OutboundQueueChecker
        outboundQueueChecker = new OutboundQueueChecker(outboundQueueManager, baseProperty.getOutboundQueueCheckInterval());
        outboundQueueChecker.start();

        // startup InboundQueueChecker
        inboundQueueChecker = new InboundQueueChecker(inboundQueues, baseProperty.getInboundQueueCheckInterval());
        inboundQueueChecker.start();

        // startup InboundServer
        inboundServer = new InboundServer(baseProperty.getInboundServerPort());
        inboundServer.startup(inboundQueues);

        LOG.info("[simple-push-server] startup complete....");
    }

    /**
     * 서버 컴포넌트 종료
     */
    public void shutdownServer() {
        // shtudown InboundServer
        if (inboundServer != null) {
            inboundServer.shutdown();
        }

        // shtudown InboundQueueChecker
        if (inboundQueueChecker != null) {
            inboundQueueChecker.shutdown();
        }

        // shtudown OutboundQueueChecker
        if (outboundQueueChecker != null) {
            outboundQueueChecker.shutdown();
        }

        // shtudown InboundQueue threads
        if (inboundQueues != null) {
            inboundQueues.forEach((serviceId, inboundQueue) -> inboundQueue.shutdown());
        }

        // shtudown OutboundServers
        if (outboundServers != null) {
            outboundServers.forEach((serviceId, outboundServer) -> outboundServer.shutdown());
        }
    }

}
