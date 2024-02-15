package com.cairnfg.waypoint.authorization.endpoints.user.getallusers;

import com.cairnfg.waypoint.authorization.endpoints.ErrorMessage;
import com.cairnfg.waypoint.authorization.endpoints.user.getallusers.dto.UserInfoDto;
import com.cairnfg.waypoint.authorization.entity.Account;
import com.cairnfg.waypoint.authorization.service.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDateTime;

@Slf4j
@RestController
@SuppressWarnings({"squid:S1075", "squid:S1452"})
public class GetAllUsersEndpoint {
    public static final String PATH = "/api/user";

    private final AccountService accountService;
    private final UserInfoDto.UserInfoDtoMapper mapper = UserInfoDto.UserInfoDtoMapper.INSTANCE;

    public GetAllUsersEndpoint(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping(PATH)
    public ResponseEntity<?> getAllUsers(Principal principal) {
        log.info("Retrieving user account with username [{}]", principal.getName());
        final ResponseEntity<?>[] response = new ResponseEntity<?>[1];

        accountService.findByUsername(principal.getName())
            .ifPresentOrElse(
                returnedAccount -> response[0] = generateSuccessResponse(returnedAccount),
                () -> response[0] = generateFailureResponse(principal.getName())
            );

        return response[0];
    }

    public ResponseEntity<UserInfoDto> generateSuccessResponse(Account account) {
        return ResponseEntity.ok(mapper.accountToUserInfoDto(account));
    }

    public ResponseEntity<ErrorMessage> generateFailureResponse(String username) {
        log.info("Account with username [{}] not found", username);
        return new ResponseEntity<>(
                ErrorMessage.builder()
                        .path(PATH)
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.NOT_FOUND.value())
                        .error("Account with username [" + username + "] not found")
                        .build(),
                HttpStatus.NOT_FOUND
        );
    }
}
