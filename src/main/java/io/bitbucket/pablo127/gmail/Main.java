package io.bitbucket.pablo127.gmail;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import io.bitbucket.pablo127.gmail.service.google.GmailCredentials;
import io.bitbucket.pablo127.gmail.service.google.GmailService;
import io.bitbucket.pablo127.gmail.service.google.GmailServiceImpl;

import javax.mail.MessagingException;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class Main {

    public static void main(String[] args) {
        try {
            GmailService gmailService = new GmailServiceImpl(GoogleNetHttpTransport.newTrustedTransport());
            gmailService.setGmailCredentials(GmailCredentials.builder()
                    .userEmail("YOUR_EMAIL@gmail.com")
                    .clientId("1078329436949-rspqf1hkbedrqavvcakn8k6l6qd9asnl.apps.googleusercontent.com")
                    .clientSecret("7Axm_PMdL3lppLdkA-MmPo7h")
                    .accessToken("ya29.GluCBY6YE-TzEU2-F86sRl_Gq5QyPmUNW2wEV0MynFN-L3HK2AHEUD09pknXfrvk8UY6NYnGwuCIxAh97s6ipVylgwoNIsxLs7uouIBqj8vWiAODGiS2a1ZDNa8D")
                    .refreshToken("1/XyMnZb4UfU8WDt6SnHIeE3wFTPyTAg2K16ZA7NIF0bY")
                    .build());

            gmailService.sendMessage("RECIPIENT_EMAIL@gmail.com", "Subject", "body text");
        } catch (GeneralSecurityException | IOException | MessagingException e) {
            e.printStackTrace();
        }
    }
}
