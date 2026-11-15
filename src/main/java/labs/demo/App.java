package labs.demo;

import java.util.Map;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/** Ponto de entrada: Jetty embarcado, escutando só em 127.0.0.1 por padrão. */
public final class App {

    public static void main(String[] args) throws Exception {
        Map<String, String> env = System.getenv();
        start(env.getOrDefault("BIND", "127.0.0.1"), Integer.parseInt(env.getOrDefault("PORT", "8080")), env).join();
    }

    /** Sobe o servidor; porta 0 escolhe uma porta livre. */
    public static Server start(String bind, int port, Map<String, String> env) throws Exception {
        Server server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setHost(bind);
        connector.setPort(port);
        server.addConnector(connector);
        ServletContextHandler context = new ServletContextHandler();
        context.addServlet(new ServletHolder(new Routes(bind, env)), "/*");
        server.setHandler(context);
        server.start();
        return server;
    }

    static int port(Server server) {
        return ((ServerConnector) server.getConnectors()[0]).getLocalPort();
    }
}
