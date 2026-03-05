package com.bank.card_ops_producer.api;

import com.bank.card_ops_producer.api.dto.CardReplacementRequestDto;
import com.bank.card_ops_producer.domain.service.EventService;
import io.reactivex.rxjava3.core.Single;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/card-replacements")
//Último paso: expone el endpoint REST.
//Solo recibe DTO, lo transforma y delega al EventService.
public class CardReplacementController {

    private final EventService service;
    public CardReplacementController(EventService service){ this.service = service; }

    @Operation(summary="Solicita reemplazo de tarjeta y publica evento Avro en Kafka")
    @PostMapping

    public Single<ResponseEntity<String>> create(@Valid @RequestBody CardReplacementRequestDto dto){
        return service.process(dto).map(id -> ResponseEntity.accepted().body(id));
    }
}
