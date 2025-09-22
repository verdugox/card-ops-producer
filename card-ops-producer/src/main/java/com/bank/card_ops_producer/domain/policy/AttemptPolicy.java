package com.bank.card_ops_producer.domain.policy;

import com.bank.card_ops_producer.api.dto.CardReplacementRequestDto;
import io.reactivex.rxjava3.core.Single;

//Qué hace?
//Define el contrato que todas las políticas deben seguir.
//Cada política:
//Recibe un CardReplacementRequestDto (la solicitud de reemplazo de tarjeta).
//Retorna un Single<Integer> → el número de intento resuelto (ej. 1 o 2).
//Tiene un name() para identificarla ("firstTimePolicy", "secondTimePolicy", "orchestrator").
//Esto permite que diferentes políticas (FirstTime, SecondTime, etc.) implementen su propia lógica sin que el resto del sistema se acople a ellas.

public interface AttemptPolicy {
    Single<Integer> resolveAttempt(CardReplacementRequestDto dto);
    String name(); // <- existe y es abstracto
}
