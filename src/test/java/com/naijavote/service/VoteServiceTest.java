package com.naijavote.service;

import com.naijavote.entity.Party;
import com.naijavote.entity.User;
import com.naijavote.entity.Vote;
import com.naijavote.repository.PartyRepository;
import com.naijavote.repository.UserRepository;
import com.naijavote.repository.VoteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VoteServiceTest {

    @Mock
    private VoteRepository voteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PartyRepository partyRepository;

    @InjectMocks
    private VoteService voteService;

    @Test
    void castVoteCreatesVoteForUserAndParty() {
        User user = new User();
        Party party = new Party();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(partyRepository.findById(2L)).thenReturn(Optional.of(party));
        when(voteRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(voteRepository.save(any(Vote.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Vote result = voteService.castVote(1L, 2L);

        assertNotNull(result);
        assertEquals(user, result.getUser());
        assertEquals(party, result.getParty());
        assertEquals(result, user.getVote());
        verify(voteRepository).save(result);
    }

    @Test
    void castVoteRejectsSecondVote() {
        User user = new User();
        Party party = new Party();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(partyRepository.findById(2L)).thenReturn(Optional.of(party));
        when(voteRepository.findByUserId(1L)).thenReturn(Optional.of(new Vote(user, party)));

        RuntimeException error = assertThrows(
                RuntimeException.class,
                () -> voteService.castVote(1L, 2L));

        assertEquals("You have already voted. Your vote cannot be changed.", error.getMessage());
        verify(voteRepository, never()).save(any(Vote.class));
    }

    @Test
    void castVoteRejectsUnknownUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException error = assertThrows(
                RuntimeException.class,
                () -> voteService.castVote(1L, 2L));

        assertEquals("User not found", error.getMessage());
        verify(voteRepository, never()).save(any(Vote.class));
    }

    @Test
    void removeVoteRejectsExistingVote() {
        when(voteRepository.findByUserId(1L)).thenReturn(Optional.of(new Vote()));

        RuntimeException error = assertThrows(
                RuntimeException.class,
                () -> voteService.removeVote(1L));

        assertEquals("Your vote has been cast and cannot be removed.", error.getMessage());
    }

    @Test
    void getPartyVotesReturnsOnlyVotesForRequestedParty() {
        Party requestedParty = new Party();
        requestedParty.setId(2L);
        Party otherParty = new Party();
        otherParty.setId(3L);
        Vote matchingVote = new Vote(new User(), requestedParty);
        Vote otherVote = new Vote(new User(), otherParty);
        when(voteRepository.findAll()).thenReturn(List.of(matchingVote, otherVote));

        assertEquals(List.of(matchingVote), voteService.getPartyVotes(2L));
    }
}
