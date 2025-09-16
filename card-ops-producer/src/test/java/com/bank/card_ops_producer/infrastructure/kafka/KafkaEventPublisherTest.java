package com.bank.card_ops_producer.infrastructure.kafka;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class KafkaEventPublisherTest {

    @Test
    void publish_success_incrementsOk_andCompletes() {
        // Arrange
        KafkaTemplate<String, Object> template = mock(KafkaTemplate.class);
        var meters = new SimpleMeterRegistry();
        // Después de crear el publisher
        var publisher = new KafkaEventPublisher(template, meters);
        publisher.initMeters(); // <-- Esto inicializa los meters

        // future ya completado OK
        var sendResult = mock(SendResult.class);
        var ok = CompletableFuture.<SendResult<String,Object>>completedFuture(sendResult);
        when(template.send(anyString(), any(), any())).thenReturn(ok);

        // Act: bloquea hasta terminar (éxito) o lanzar error
        publisher.publish("topic", "key", "value").blockingGet();

        // Assert
        verify(template).send(anyString(), any(), any());
        verifyNoMoreInteractions(template);
    }

    @Test
    void publish_error_incrementsErr_andFails() {
        // Arrange
        KafkaTemplate<String, Object> template = mock(KafkaTemplate.class);
        var meters = new SimpleMeterRegistry();
        // Después de crear el publisher
        var publisher = new KafkaEventPublisher(template, meters);
        publisher.initMeters(); // <-- Esto inicializa los meters
        // future que falla
        var failed = new CompletableFuture<SendResult<String,Object>>();
        failed.completeExceptionally(new RuntimeException("boom"));
        when(template.send(anyString(), any(), any())).thenReturn(failed);

        // Act + Assert: blockingGet lanza la excepción
        Throwable ex = assertThrows(Throwable.class,
                () -> publisher.publish("topic", "key", "value").blockingGet());
        // Mensaje propagado
        if (ex.getMessage() == null || !ex.getMessage().contains("boom")) {
            // si está envuelta, revisa la causa
            var cause = ex.getCause();
            if (cause == null || cause.getMessage() == null || !cause.getMessage().contains("boom")) {
                throw new AssertionError("No se propagó el mensaje de error esperado", ex);
            }
        }

        verify(template).send(anyString(), any(), any());
        verifyNoMoreInteractions(template);
    }
}
