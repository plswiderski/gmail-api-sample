package io.bitbucket.pablo127.gmail.service.google;

import javax.mail.MessagingException;
import java.io.IOException;

public interface GmailService {
    void setGmailCredentials(GmailCredentials gmailCredentials);

    boolean sendMessage(String recipientAddress, String subject, String body) throws MessagingException, IOException;
}
