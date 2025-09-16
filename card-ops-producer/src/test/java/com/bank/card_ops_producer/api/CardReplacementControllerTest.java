package com.bank.card_ops_producer.api;

import com.bank.card_ops_producer.api.dto.CardReplacementRequestDto;
import com.bank.card_ops_producer.domain.service.EventService;
import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CardReplacementController.class)
class CardReplacementControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    EventService service;

    @Test
    void create_returns200() throws Exception {
        Mockito.when(service.process(Mockito.any(CardReplacementRequestDto.class)))
                .thenReturn(Single.just("EVENT-1"));

        mvc.perform(
                post("/api/card-replacements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"requestId":"R1","customerId":"C1","cardPANMasked":"4111****9999",
                             "reasonCode":"DAMAGE","priority":"HIGH","branchCode":"BR1",
                             "deliveryAddress":"X","correlationId":"CORR","status":"REQUESTED"}
                        """)
        ).andExpect(status().isOk());
    }

}
