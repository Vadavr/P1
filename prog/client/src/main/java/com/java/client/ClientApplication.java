package com.java.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class ClientApplication implements CommandLineRunner {

    private final MessageService messageService;

    public static void main(String[] args) {
        System.out.println("Starting messenger...");
        SpringApplication.run(ClientApplication.class, args);
        System.out.println("Application finished");
    }


    @Override
    public void run(String... args) throws Exception {
        System.out.println("EXECUTING: command line runner");
        try {
            System.out.println("Chat is loading please wait!");
            Thread.sleep(10000);
            if (messageService.login()) {
                while (true) {
                    if (messageService.findFriend()) {
                        break;
                    }
                }
                messageService.run();
            }
        } catch (Exception e) {
            System.out.println("Remote server error.");
            System.exit(1);
        }

    }
}
