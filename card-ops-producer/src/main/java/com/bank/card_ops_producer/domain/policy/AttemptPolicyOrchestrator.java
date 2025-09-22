// src/main/java/com/bank/card_ops_producer/domain/policy/AttemptPolicyOrchestrator.java
package com.bank.card_ops_producer.domain.policy;

import com.bank.card_ops_producer.api.dto.CardReplacementRequestDto;
import com.bank.card_ops_producer.domain.port.AttemptStateRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

//Qué hace?
//Es el cerebro/orquestador de todas las políticas.
//Decide qué policy aplicar en base a lo que responda Redis.
//Flujo:
//Convierte el DTO a JSON → para guardar un snapshot en Redis.
//Llama a repo.existsByRequestId(requestId):
//Si existe → significa que es al menos la segunda vez.
//Guarda snapshot → aplica SecondTimePolicy (intento = 2).
//Si NO existe → significa que es la primera vez.
//Guarda saveFirstAttempt en Redis → guarda snapshot → aplica FirstTimePolicy (intento = 1).
//Usa onErrorReturnItem(false) para que errores de Redis no bloqueen la lógica → si Redis falla, igual sigue con la política.
//Detalles importantes
//@Primary → este bean será el principal que se inyecte como AttemptPolicy en el dominio.
//ObjectMapper → serializa DTO → JSON, si falla devuelve "{}".
//Devuelve Single<Integer> con el número de intento resuelto.
//Básicamente: decide si este request es el primer intento o un reintento y asegura que siempre haya snapshot en Redis.

@Component
@Primary
@RequiredArgsConstructor
public class AttemptPolicyOrchestrator implements AttemptPolicy {

    private final FirstTimePolicy first;
    private final SecondTimePolicy second;
    private final AttemptStateRepository repo;
    private final ObjectMapper objectMapper; // Boot ya provee este bean

    @Override
    public Single<Integer> resolveAttempt(CardReplacementRequestDto dto) {
        final String requestId = dto.getRequestId();
        final String snapshotJson = toJson(dto); // 1 sola vez

        return repo.existsByRequestId(requestId)
                .flatMap(exists -> {
                    if (exists) {
                        // Segunda vez: guarda snapshot (opcional) y delega a second
                        return repo.saveEventSnapshot(requestId, snapshotJson)
                                .onErrorReturnItem(false)
                                .flatMap(ignored -> second.resolveAttempt(dto));
                    } else {
                        // Primera vez: marca intento=1, guarda snapshot y delega a first
                        return repo.saveFirstAttempt(requestId)
                                .onErrorReturnItem(false)
                                .flatMap(ignored -> repo.saveEventSnapshot(requestId, snapshotJson))
                                .onErrorReturnItem(false)
                                .flatMap(ignored -> first.resolveAttempt(dto));
                    }
                });
    }

    private String toJson(CardReplacementRequestDto dto) {
        try { return objectMapper.writeValueAsString(dto); }
        catch (Exception e) { return "{}"; } // no bloquees el flujo si falla serialización
    }

    @Override
    public String name() { return "orchestrator"; }
}
