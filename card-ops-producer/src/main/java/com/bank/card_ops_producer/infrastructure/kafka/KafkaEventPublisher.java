package com.bank.card_ops_producer.infrastructure.kafka;

import com.bank.card_ops_producer.domain.port.EventPublisher;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.reactivex.rxjava3.core.Single;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

//Qué hace? :
//Es la implementación real del puerto EventPublisher.
//Usa KafkaTemplate de Spring para enviar mensajes a Kafka.
//Envuelve la operación en un Single de RxJava para manejarlo de forma reactiva/asíncrona.
//Instrumenta métricas con Micrometer para Prometheus/Grafana:
//Counters → cuántos eventos publicados con éxito y con error.
//Timers → cuánto tiempo tardan las publicaciones.
//Flujo:
//Arranca el timer (startNanos).
//Construye métricas por tópico (okByTopic, errByTopic).
//Ejecuta template.send(...) (retorna CompletableFuture).
//En el callback:
//Si hay error: incrementa publishedErr y falla el Single.
//Si éxito: incrementa publishedOk y emite el SendResult.
//Resumen: Es el motor real que publica en Kafka + monitorea métricas.

@Component("kafkaEventPublisher")
@RequiredArgsConstructor
public class KafkaEventPublisher implements EventPublisher<Object> {

    private final KafkaTemplate<String, Object> template;
    private final MeterRegistry meter;

    // se inicializan en @PostConstruct (no aquí)
    private Counter publishedOk;
    private Counter publishedErr;
    private Timer   publishTimer;

    @PostConstruct
    void initMeters() {
        this.publishedOk  = Counter.builder("card_ops_events_published_total")
                .description("Eventos publicados a Kafka (OK)")
                .register(meter);

        this.publishedErr = Counter.builder("card_ops_events_published_error_total")
                .description("Errores al publicar eventos a Kafka")
                .register(meter);

        this.publishTimer = Timer.builder("card_ops_event_publish_timer")
                .description("Duración de publish() a Kafka")
                .publishPercentileHistogram()
                .register(meter);
    }

    @Override
    public Single<SendResult<String, Object>> publish(String topic, String key, Object value) {
        long startNanos = System.nanoTime();

        // métricas por topic (opcionales)
        Counter okByTopic = Counter.builder("card_ops_events_published_by_topic_total")
                .description("Eventos publicados a Kafka por topic")
                .tag("topic", topic).register(meter);

        Counter errByTopic = Counter.builder("card_ops_events_published_by_topic_error_total")
                .description("Errores de publicación por topic")
                .tag("topic", topic).register(meter);

        Timer timerByTopic = Timer.builder("card_ops_event_publish_by_topic_timer")
                .description("Duración de publish() por topic")
                .tag("topic", topic).publishPercentileHistogram().register(meter);

        var future = template.send(topic, key, value); // CompletableFuture

        return Single.create(emitter ->
                future.whenComplete((result, ex) -> {
                    long elapsed = System.nanoTime() - startNanos;
                    publishTimer.record(elapsed, java.util.concurrent.TimeUnit.NANOSECONDS);
                    timerByTopic.record(elapsed, java.util.concurrent.TimeUnit.NANOSECONDS);

                    if (ex != null) {
                        publishedErr.increment();
                        errByTopic.increment();
                        if (!emitter.isDisposed()) emitter.onError(ex);
                    } else {
                        publishedOk.increment();
                        okByTopic.increment();
                        if (!emitter.isDisposed()) emitter.onSuccess(result);
                    }
                })
        );
    }
}
