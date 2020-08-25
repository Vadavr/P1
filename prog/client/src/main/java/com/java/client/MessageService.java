package com.java.client;

import com.java.client.dto.AuthEnum;
import com.java.client.dto.AuthenticatedDTO;
import com.java.client.dto.Credentials;
import com.java.client.dto.HistoryDTO;
import com.java.client.dto.LoadHistoryDTO;
import com.java.client.dto.MessageEnvelope;
import com.java.client.kafka.Topics;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Scanner;

import static com.java.client.Utils.getHttpEntityFromDTO;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final Scanner scanner;
    private final RestTemplate restTemplate;
    private final AppConfig.Manager manager;
    private final KafkaTemplate<String, MessageEnvelope> kafkaTemplate;

    private static final String AUTH_URL = "auth";
    private static final String CHECK_IF_REGISTERED_URL = "auth/registration/check";
    private static final String SAVE_MESSAGE = "message/save";
    private static final String LOAD_HISTORY = "message/history";
    private static final LocalDateTime now = LocalDateTime.now();


    private static User CURRENT_USER = new User();


    public boolean login() {

        System.out.println("Welcome to messenger app! Identify yourself. ");
        System.out.println("Input username: ");
        var username = scanner.nextLine();
        System.out.println("Input password: ");
        var password = scanner.nextLine();
        var credentials = new Credentials(username, password);

        var userStatus = restTemplate.postForEntity(manager.getCompleteUrl() + CHECK_IF_REGISTERED_URL,
                getHttpEntityFromDTO(credentials),
                AuthEnum.class).getBody();

        assert userStatus != null;
        if (userStatus.equals(AuthEnum.UNREGISTERED)) {
            System.out.println("It seems you are not registered in the system yet. " +
                    "Would you like to become a new user? [yes/no]");
            while (true) {
                var answer = scanner.nextLine().toLowerCase();
                switch (answer) {
                    case "yes" -> {
                        var user = restTemplate.postForEntity(manager.getCompleteUrl() + AUTH_URL,
                                getHttpEntityFromDTO(credentials),
                                AuthenticatedDTO.class).getBody();

                        System.out.println("You have successfully signed up! ");
                        CURRENT_USER.setUserId(Objects.requireNonNull(user).getUserId());
                        CURRENT_USER.setUsername(username);
                        CURRENT_USER.setStatus(AuthEnum.AUTHENTICATED);
                        return true;
                    }
                    case "no" -> System.out.println("Sadly, hope to see you later!");
                    default -> System.out.println("Please type [Yes, No]!");
                }
            }
        } else {
            var res = restTemplate.postForEntity(manager.getCompleteUrl() + AUTH_URL,
                    getHttpEntityFromDTO(credentials),
                    AuthenticatedDTO.class).getBody();

            CURRENT_USER.setUserId(Objects.requireNonNull(res).getUserId());
            CURRENT_USER.setUsername(username);
            CURRENT_USER.setStatus(AuthEnum.AUTHENTICATED);
            System.out.println("Welcome %" + username);
            return true;
        }
    }

    public boolean findFriend() {
        System.out.println("Now please enter the nickname of your friend: ");
        String friendName = scanner.nextLine();

        try {
            var res = restTemplate.postForEntity(manager.getCompleteUrl() + LOAD_HISTORY,
                    getHttpEntityFromDTO(LoadHistoryDTO.builder()
                            .friendUserName(friendName)
                            .ownerUserId(CURRENT_USER.getUserId())
                            .build()),
                    HistoryDTO.class);
            var body = res.getBody();
            if (body != null) {
                if (body.getMessageEnvelopes() != null) {
                    body.getMessageEnvelopes().forEach(envelope -> {
                        System.out.println(envelope.getTimestamp() + envelope.getPayload());
                    });
                }
            }
            CURRENT_USER.setStatus(AuthEnum.READY_FOR_CHATTING);
            return true;
        } catch (Exception e) {
            System.out.println("User with this nickname does not exist! Check name correctness.");
            return false;
        }

    }

    public void run() {
        System.out.println("__________________MESSENGER_FIELD__________________");
        while (true) {
            var user = "%" + CURRENT_USER.getUsername() + " ";
            var message = user + scanner.nextLine();
            publishMessage(message);
        }
    }

    private void publishMessage(String text) {
        var message = MessageEnvelope.builder()
                .payload(text)
                .userId(CURRENT_USER.getUserId())
                .timestamp(Timestamp.valueOf(LocalDateTime.now()))
                .build();
        kafkaTemplate.send(Topics.MESSAGE_POOL_TOPIC, message);
        try {
            restTemplate.postForEntity(manager.getCompleteUrl() + SAVE_MESSAGE,
                    getHttpEntityFromDTO(message),
                    Void.class);
        } catch (Exception ignored) {
        }
    }


    @KafkaListener(topics = Topics.MESSAGE_POOL_TOPIC)
    public void poolListener(MessageEnvelope envelope) {
        if (envelope.getTimestamp().compareTo(Timestamp.valueOf(now)) > 0
                && CURRENT_USER.getStatus().equals(AuthEnum.READY_FOR_CHATTING)) {
            System.out.println(envelope.getTimestamp() + ":" + envelope.getPayload());
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class User {
        private BigInteger userId;
        private String username;
        @Builder.Default
        private AuthEnum status = AuthEnum.UNAUTHORIZED;

    }

}
