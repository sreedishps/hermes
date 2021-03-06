package pl.allegro.tech.hermes.integration;

import org.springframework.web.client.AsyncRestTemplate;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import pl.allegro.tech.hermes.api.TopicName;
import pl.allegro.tech.hermes.client.HermesClient;
import pl.allegro.tech.hermes.client.HermesResponse;
import pl.allegro.tech.hermes.client.jersey.JerseyHermesSender;
import pl.allegro.tech.hermes.client.restTemplate.RestTemplateHermesSender;
import pl.allegro.tech.hermes.test.helper.message.TestMessage;

import java.net.URI;

import static java.net.URI.create;
import static javax.ws.rs.client.ClientBuilder.newClient;
import static org.assertj.core.api.Assertions.assertThat;
import static pl.allegro.tech.hermes.client.HermesClientBuilder.hermesClient;

public class HermesClientPublishingTest extends IntegrationTest {

    private TestMessage message = TestMessage.of("hello", "world");

    private TopicName topic = new TopicName("hermesClientGroup", "topic");
    private URI topicURI = create("http://localhost:" + FRONTEND_PORT);

    @BeforeClass
    public void initialize() throws InterruptedException {
        operations.buildTopic(topic.getGroupName(), topic.getName());
    }

    @Test
    public void shouldPublishUsingJerseyClient() {
        // given
        HermesClient client = hermesClient(new JerseyHermesSender(newClient())).withURI(topicURI).build();
        
        // when & then
        runTestSuiteForHermesClient(client);
    }

    @Test
    public void shouldPublishUsingRestTemplate() {
        // given
        HermesClient client = hermesClient(new RestTemplateHermesSender(new AsyncRestTemplate())).withURI(topicURI).build();

        // when & then
        runTestSuiteForHermesClient(client);
    }

    private void runTestSuiteForHermesClient(HermesClient client) {
        shouldPublishUsingHermesClient(client);
        shouldNotPublishUsingHermesClient(client);
    }

    private void shouldPublishUsingHermesClient(HermesClient client) {
        // when
        HermesResponse response = client.publish(topic.qualifiedName(), message.body()).join();

        // then
        assertThat(response.wasPublished()).isTrue();
        assertThat(response.getMessageId()).isNotEmpty();
    }

    private void shouldNotPublishUsingHermesClient(HermesClient client) {
        // given
        TopicName topic = new TopicName("not", "existing");

        // when
        HermesResponse response = client.publish(topic.qualifiedName(), message.body()).join();

        // then
        assertThat(response.wasPublished()).isFalse();
    }
}
