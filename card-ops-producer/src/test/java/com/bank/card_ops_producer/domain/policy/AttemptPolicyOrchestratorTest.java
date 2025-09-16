package com.bank.card_ops_producer.domain.policy;

import com.bank.card_ops_producer.api.dto.CardReplacementRequestDto;
import com.bank.card_ops_producer.domain.port.AttemptStateRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AttemptPolicyOrchestratorTest {

    private FirstTimePolicy first;
    private SecondTimePolicy second;
    private AttemptStateRepository repo;
    private ObjectMapper objectMapper;
    private AttemptPolicy orchestrator;

    @BeforeEach
    void setUp() {
        first = mock(FirstTimePolicy.class);
        second = mock(SecondTimePolicy.class);
        repo = mock(AttemptStateRepository.class);
        objectMapper = mock(ObjectMapper.class);
        orchestrator = new AttemptPolicyOrchestrator(first, second, repo, objectMapper);
    }

    @Test
    void whenRequestIsNew_callsFirst_afterSnapshot_ok() throws Exception {
        var dto = CardReplacementRequestDto.builder().requestId("REQ-NEW").build();

        when(objectMapper.writeValueAsString(dto)).thenReturn("{json}");
        when(repo.saveEventSnapshot(eq("REQ-NEW"), anyString())).thenReturn(Single.just(true));
        when(repo.existsByRequestId("REQ-NEW")).thenReturn(Single.just(false));
        when(repo.saveFirstAttempt("REQ-NEW")).thenReturn(Single.just(true));
        when(first.resolveAttempt(dto)).thenReturn(Single.just(1));

        int attempt = orchestrator.resolveAttempt(dto).blockingGet();

        assertThat(attempt).isEqualTo(1);
        verify(repo).saveEventSnapshot(eq("REQ-NEW"), anyString());
        verify(repo).existsByRequestId("REQ-NEW");
        verify(repo).saveFirstAttempt("REQ-NEW");
        verify(first).resolveAttempt(dto);
        verifyNoInteractions(second);
    }

    @Test
    void whenRequestIsRepeated_callsSecond_afterSnapshot_ok() throws Exception {
        var dto = CardReplacementRequestDto.builder().requestId("REQ-REP").build();

        when(objectMapper.writeValueAsString(dto)).thenReturn("{json}");
        when(repo.saveEventSnapshot(eq("REQ-REP"), anyString())).thenReturn(Single.just(true));
        when(repo.existsByRequestId("REQ-REP")).thenReturn(Single.just(true));
        when(second.resolveAttempt(dto)).thenReturn(Single.just(2));

        int attempt = orchestrator.resolveAttempt(dto).blockingGet();

        assertThat(attempt).isEqualTo(2);
        verify(repo).saveEventSnapshot(eq("REQ-REP"), anyString());
        verify(repo).existsByRequestId("REQ-REP");
        verify(second).resolveAttempt(dto);
        verifyNoInteractions(first);
    }

    @Test
    void snapshotSerializationFails_flowContinues_withEmptyJson() throws Exception {
        var dto = CardReplacementRequestDto.builder().requestId("REQ-J").build();

        when(objectMapper.writeValueAsString(dto)).thenThrow(new JsonProcessingException("boom") {});
        when(repo.saveEventSnapshot(eq("REQ-J"), eq("{}"))).thenReturn(Single.just(true));
        when(repo.existsByRequestId("REQ-J")).thenReturn(Single.just(false));
        when(repo.saveFirstAttempt("REQ-J")).thenReturn(Single.just(true));
        when(first.resolveAttempt(dto)).thenReturn(Single.just(1));

        int attempt = orchestrator.resolveAttempt(dto).blockingGet();

        assertThat(attempt).isEqualTo(1);
        verify(repo).saveEventSnapshot(eq("REQ-J"), eq("{}"));
        verify(first).resolveAttempt(dto);
    }

    @Test
    void existsByRequestIdErrors_bubblesError() throws Exception {
        var dto = CardReplacementRequestDto.builder().requestId("REQ-ERR").build();

        when(objectMapper.writeValueAsString(dto)).thenReturn("{json}");
        when(repo.saveEventSnapshot(eq("REQ-ERR"), anyString())).thenReturn(Single.just(true));
        when(repo.existsByRequestId("REQ-ERR"))
                .thenReturn(Single.error(new IllegalStateException("redis down")));

        orchestrator.resolveAttempt(dto).test().assertError(IllegalStateException.class);

        verifyNoInteractions(first, second);
    }

    @Test
    void saveEventSnapshotErrors_isIgnored_andDelegates() throws Exception {
        var dto = CardReplacementRequestDto.builder().requestId("REQ-IGN").build();

        when(objectMapper.writeValueAsString(dto)).thenReturn("{json}");
        when(repo.saveEventSnapshot(eq("REQ-IGN"), anyString()))
                .thenReturn(Single.error(new RuntimeException("ttl fail"))); // onErrorReturnItem(false)
        when(repo.existsByRequestId("REQ-IGN")).thenReturn(Single.just(true));
        when(second.resolveAttempt(dto)).thenReturn(Single.just(2));

        orchestrator.resolveAttempt(dto).test().assertValue(2);

        verify(second).resolveAttempt(dto);
    }
}
