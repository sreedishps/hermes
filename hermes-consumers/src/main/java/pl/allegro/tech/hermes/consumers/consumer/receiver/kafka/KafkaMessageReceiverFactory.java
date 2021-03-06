package pl.allegro.tech.hermes.consumers.consumer.receiver.kafka;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import pl.allegro.tech.hermes.api.Subscription;
import pl.allegro.tech.hermes.api.TopicName;
import pl.allegro.tech.hermes.common.config.ConfigFactory;
import pl.allegro.tech.hermes.common.config.Configs;
import pl.allegro.tech.hermes.common.json.MessageContentWrapper;
import pl.allegro.tech.hermes.consumers.consumer.receiver.MessageReceiver;
import pl.allegro.tech.hermes.consumers.consumer.receiver.ReceiverFactory;

import javax.inject.Inject;
import java.util.Properties;

public class KafkaMessageReceiverFactory implements ReceiverFactory {

    private ConfigFactory configFactory;
    private MessageContentWrapper contentWrapper;

    @Inject
    public KafkaMessageReceiverFactory(ConfigFactory configFactory, MessageContentWrapper contentWrapper) {
        this.configFactory = configFactory;
        this.contentWrapper = contentWrapper;
    }

    @Override
    public MessageReceiver createMessageReceiver(Subscription subscription) {
        return create(subscription.getTopicName(), createConsumerConfig(subscription.getId()));
    }

    MessageReceiver create(TopicName topicName, ConsumerConfig consumerConfig) {
        return new KafkaMessageReceiver(
                topicName.qualifiedName(),
                Consumer.createJavaConsumerConnector(consumerConfig),
                configFactory,
                contentWrapper);
    }

    private ConsumerConfig createConsumerConfig(String subscriptionName) {
        Properties props = new Properties();

        props.put("group.id", subscriptionName);
        props.put("zookeeper.connect", configFactory.getStringProperty(Configs.KAFKA_ZOOKEEPER_CONNECT_STRING));
        props.put("zookeeper.connection.timeout.ms", configFactory.getIntPropertyAsString(Configs.ZOOKEEPER_CONNECTION_TIMEOUT));
        props.put("zookeeper.session.timeout.ms", configFactory.getIntPropertyAsString(Configs.ZOOKEEPER_SESSION_TIMEOUT));
        props.put("zookeeper.sync.time.ms", configFactory.getIntPropertyAsString(Configs.ZOOKEEPER_SYNC_TIME));
        props.put("auto.commit.enable", "false");
        props.put("fetch.wait.max.ms", "10000");
        props.put("consumer.timeout.ms", configFactory.getIntPropertyAsString(Configs.KAFKA_CONSUMER_TIMEOUT_MS));
        props.put("auto.offset.reset", configFactory.getStringProperty(Configs.KAFKA_CONSUMER_AUTO_OFFSET_RESET));

        return new ConsumerConfig(props);
    }

}
