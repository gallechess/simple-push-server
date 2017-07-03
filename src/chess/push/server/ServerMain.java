package chess.push.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ServerMain {

    private static final Logger LOG = LoggerFactory.getLogger(ServerMain.class);

    private static final String MODULE_NAME = "SimplePushServer";

    public static void main(String[] args) {
        ServerMain server = new ServerMain();
        LOG.info("{} starting....", MODULE_NAME);

        try {
            ApplicationContext context = new ClassPathXmlApplicationContext("spring-context.xml");
            LOG.debug("context loaded");

            // TODO

            LOG.info("{} startup complete....", MODULE_NAME);

            synchronized (ServerMain.class) {
                ServerMain.class.wait();
            }

        } catch (Exception e) {
            LOG.error(MODULE_NAME + " startup failed, shutting down...", e);
            System.exit(1);
        }
    }

}
