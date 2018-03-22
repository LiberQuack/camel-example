package poc.routes

import org.apache.camel.builder.RouteBuilder

import static org.apache.camel.LoggingLevel.DEBUG
import static org.apache.camel.LoggingLevel.INFO
import static org.apache.camel.util.ExchangeHelper.getOriginalInMessage

class MainRoute extends RouteBuilder {

    protected final String INVOICES_FOLDER = "file:invoices-folder?noop=true&sortBy=file:name"
    protected final String INVOICE_UNMARSHALLER = "direct:invoice_validator"
    protected final String VALID_INVOICES_QUEUE = "activemq:queue:valid_queue?acknowledgementModeName=CLIENT_ACKNOWLEDGE&concurrentConsumers=1"
    protected final String INVALID_INVOICES_QUEUE = "activemq:queue:invalid_queue"
    protected final String DLQ = "activemq:queue:DLQ"
    protected final String DATABASE = "jdbc:postgres"
    protected final String INVALID_FOLDER = "file:invalid-invoices-folder"

    void configure() {

        onException(Exception)
                .retryAttemptedLogLevel(DEBUG)
                .retriesExhaustedLogLevel(DEBUG)
                .logRetryStackTrace(false)
                .maximumRedeliveries(2)
                .log('Retry on ${headers.CamelFileName}')
                .to(DLQ)

        from(INVOICES_FOLDER)
                .log('Reading file ${headers.CamelFileName}')
                .unmarshal().jacksonxml()
                .choice()
                .when { it.in.body.valid == "true" }.to(VALID_INVOICES_QUEUE)
                .otherwise().setBody { getOriginalInMessage(it).body }.to(INVALID_INVOICES_QUEUE)

        from(VALID_INVOICES_QUEUE)
                .log('READING FROM QUEUE fileName:${headers.CamelFileName} exchangeId:${exchangeId}')
                .delay(500)
                .throwException(new Exception("Potato"))
                .setBody(simple('resource:classpath:invoice-insert.sql')).transform(body().regexReplaceAll('\\n', ''))
                .to(DATABASE)
                .log('Inserted ${body.size} registries into the database')

        from(INVALID_INVOICES_QUEUE)
                .to(INVALID_FOLDER)
//                .log('Saved invalid invoice into ${headers.CamelFileNameProduced}')
    }
}
