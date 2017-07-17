package chess.push.server;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import chess.push.server.property.PushBaseProperty;
import chess.push.server.property.PushServiceProperty;

/**
 * simple-push-server 서버 entry class
 */
public class ServerMain {

    private static final Logger LOG = LoggerFactory.getLogger(ServerMain.class);

    /**
     * entry point
     */
    public static void main(String[] args) {
        Server server = new Server();

        try (ConfigurableApplicationContext context = new ClassPathXmlApplicationContext("application.xml")) {
            PushBaseProperty baseProperty = context.getBean(PushBaseProperty.class);
            Collection<PushServiceProperty> serviceProperties = context.getBeansOfType(PushServiceProperty.class).values();

            // Push 서버 모듈 기동
            server.startupServer(false, baseProperty, serviceProperties);

            synchronized (ServerMain.class) {
                ServerMain.class.wait();
            }

        } catch (Exception e) {
            LOG.error("startup failed", e);

        } finally {
            server.shutdownServer();
        }
    }

}
