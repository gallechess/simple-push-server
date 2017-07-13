package chess.push.server;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import chess.push.server.inbound.InboundServer;
import chess.push.server.outbound.OutboundServer;
import chess.push.server.property.PushServiceProperty;
import chess.push.server.queue.InboundQueue;

/**
 * simple-push-server 서버 프로그램 entry class
 */
public class ServerMain {

    private static final Logger LOG = LoggerFactory.getLogger(ServerMain.class);

    private Map<String, PushServiceProperty> serviceProps;	// PushServiceProperty collection (key: Service ID)
    private Map<String, OutboundServer> outboundServers;	// OutboundServer collection (key: Service ID)
    private Map<String, InboundQueue> inboundQueues;		// InboundQueue collection (key: Service ID)

//    private static OutboundQueueCounter outboundQueueCounterThread;
//    private static InboundQueueCounter inboundQueueCounterThread;
    private InboundServer inboundServer;

    /**
     * 서버 컴포넌트 기동
     * @param context Spring ApplicationContext
     */
    public void startupServer(ApplicationContext context) {
        LOG.info("[simple-push-server] starting...");

        Map<String, PushServiceProperty> propertyBeans = context.getBeansOfType(PushServiceProperty.class);
        if (!propertyBeans.isEmpty()) {
            // PushServiceProperty 타입 빈들을 Service ID를 key로 하는 collection에 저장
            serviceProps = new HashMap<String, PushServiceProperty>();
            outboundServers = new HashMap<String, OutboundServer>();
            inboundQueues = new HashMap<String, InboundQueue>();
            propertyBeans.forEach((beanName, property) -> {
                String serviceId = property.getServiceId();
                serviceProps.put(serviceId, property);
                outboundServers.put(serviceId, new OutboundServer(property));
                inboundQueues.put(serviceId, new InboundQueue(serviceId, property.getInboundQueueCapacity()));
            });

            // startup OutboundServers
            outboundServers.forEach((serviceId, outboundServer) -> outboundServer.startup());

            // startup InboundQueue threads
            inboundQueues.forEach((serviceId, inboundQueue) -> inboundQueue.start());
        }

        // TODO startup OutboundQueueCounter

        // TODO startup InboundQueueCounter

        // startup InboundServer
        inboundServer = context.getBean(InboundServer.class);
        // TODO inject InboundQueues
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

        // TODO shtudown InboundQueueCounter

        // TODO shtudown OutboundQueueCounter

        // TODO shtudown InboundQueue threads
        if (inboundQueues != null) {
            inboundQueues.forEach((serviceId, inboundQueue) -> inboundQueue.shutdown());
        }

        // TODO shtudown OutboundServers

    }

    /**
     * simple-push-server 서버 프로그램 entry point
     */
    public static void main(String[] args) {
        ServerMain serverMain = new ServerMain();
        try {
            serverMain.startupServer(new ClassPathXmlApplicationContext("application.xml"));
            synchronized (ServerMain.class) {
                ServerMain.class.wait();
            }
        } catch (Exception e) {
            LOG.error("[simple-push-server] startup failed", e);
        } finally {
            serverMain.shutdownServer();
        }
    }

}
