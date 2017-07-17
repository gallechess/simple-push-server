package chess.push.server;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import chess.push.common.PushMessage;
import chess.push.server.property.PushBaseProperty;
import chess.push.server.property.PushServiceProperty;
import chess.push.server.queue.InboundQueue;

public class TestEmbedServerMain {

    private static final Logger LOG = LoggerFactory.getLogger(TestEmbedServerMain.class);

    public static void main(String[] args) {
        Server server = new Server();

        try (ConfigurableApplicationContext context = new ClassPathXmlApplicationContext("application.xml")) {
            PushBaseProperty baseProperty = context.getBean(PushBaseProperty.class);
            Collection<PushServiceProperty> serviceProperties = context.getBeansOfType(PushServiceProperty.class).values();

            // Push 서버 모듈 기동
            Map<String, InboundQueue> inboundQueues = server.startupServer(true, baseProperty, serviceProperties);

            // 각 서비스ID에 지속적으로 1초 단위 테스트 메시지 송신
            for (;;) {
                inboundQueues.forEach((serviceId, inboundQueue) -> {
                    inboundQueue.enqueue(new PushMessage(serviceId, null, "test message [" + System.currentTimeMillis() + "]"));
                });
                TimeUnit.SECONDS.sleep(1L);
            }

        } catch (Exception e) {
            LOG.error("startup failed", e);

        } finally {
            server.shutdownServer();
        }
    }

}
