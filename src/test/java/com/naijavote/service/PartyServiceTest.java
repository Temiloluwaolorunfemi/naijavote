package com.naijavote.service;

import com.naijavote.dto.PartyRequest;
import com.naijavote.entity.Party;
import com.naijavote.repository.PartyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PartyServiceTest {

    @Mock
    private PartyRepository partyRepository;

    @InjectMocks
    private PartyService partyService;

    @Test
    void createPartySavesPartyWhenAbbreviationIsAvailable() {
        PartyRequest request = new PartyRequest("Unity Party", "UP", "A party");
        Party savedParty = new Party();
        when(partyRepository.findByAbbreviation("UP")).thenReturn(Optional.empty());
        when(partyRepository.save(any(Party.class))).thenReturn(savedParty);

        Party result = partyService.createParty(request);

        assertEquals(savedParty, result);
        verify(partyRepository).save(any(Party.class));
    }

    @Test
    void createPartyRejectsDuplicateAbbreviation() {
        PartyRequest request = new PartyRequest("Unity Party", "UP", "A party");
        when(partyRepository.findByAbbreviation("UP")).thenReturn(Optional.of(new Party()));

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> partyService.createParty(request));

        assertEquals("Party abbreviation already exists", error.getMessage());
        verify(partyRepository, never()).save(any(Party.class));
    }

    @Test
    void updatePartyChangesNameAndDescription() {
        Party party = new Party("Old", "UP", "Old description");
        PartyRequest request = new PartyRequest("New", "UP", "New description");
        when(partyRepository.findById(1L)).thenReturn(Optional.of(party));

        partyService.updateParty(1L, request);

        assertEquals("New", party.getName());
        assertEquals("New description", party.getDescription());
        assertEquals("UP", party.getAbbreviation());
        verify(partyRepository).save(party);
    }

    @Test
    void updatePartyRejectsUnknownParty() {
        when(partyRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException error = assertThrows(
                RuntimeException.class,
                () -> partyService.updateParty(1L, new PartyRequest()));

        assertEquals("Party not found", error.getMessage());
        verify(partyRepository, never()).save(any(Party.class));
    }
}
