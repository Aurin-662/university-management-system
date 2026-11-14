package com.university.ums.security;

import com.university.ums.model.UserAccount;
import com.university.ums.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/** Used in @PreAuthorize expressions: is the logged-in user the owner of this record? */
@Component("access")
@RequiredArgsConstructor
public class AccessChecker {
    private final UserRepository repo;

    public boolean isSelf(Long id, Authentication auth) {
        return repo.findByUsername(auth.getName())
                .map(UserAccount::getId)
                .map(myId -> myId.equals(id))
                .orElse(false);
    }
}
