package com.university.ums.security;

import com.university.ums.model.UserAccount;
import com.university.ums.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccessCheckerTest {

    @Mock UserRepository repo;
    @InjectMocks AccessChecker checker;

    // Unit test 6: shudhu nijer id te true, onner id te false
    @Test
    void isSelf_trueOnlyForOwnId() {
        UserAccount me = new UserAccount();
        me.setId(2L);
        me.setUsername("t1");
        when(repo.findByUsername("t1")).thenReturn(Optional.of(me));
        Authentication auth = new UsernamePasswordAuthenticationToken("t1", null);

        assertTrue(checker.isSelf(2L, auth));
        assertFalse(checker.isSelf(3L, auth));
    }
}