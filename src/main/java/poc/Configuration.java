package poc;

import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.CamelContext;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.impl.PropertyPlaceholderDelegateRegistry;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.main.MainListenerSupport;
import org.postgresql.ds.PGSimpleDataSource;
import poc.routes.MainRoute;

public class Configuration extends MainListenerSupport {

    @Override
    public void configure(CamelContext context) {
        try {
            context.addComponent("activemq", ActiveMQComponent.activeMQComponent("tcp://localhost:61616"));
            context.setAllowUseOriginalMessage(true);
            context.addRoutes(new MainRoute());
            JndiRegistry registry = context.getRegistry(JndiRegistry.class);
            registry.bind("postgres", buildPostgresDataSource());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private PGSimpleDataSource buildPostgresDataSource() {
        PGSimpleDataSource postgres = new PGSimpleDataSource();
        postgres.setUser("postgres");
        postgres.setPassword("postgres");
        postgres.setServerName("localhost");
        postgres.setPortNumber(5432);
        postgres.setDatabaseName("postgres");
        return postgres;
    }
}
