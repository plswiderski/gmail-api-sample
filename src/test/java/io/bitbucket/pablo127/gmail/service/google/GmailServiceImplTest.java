package io.bitbucket.pablo127.gmail.service.google;

import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.util.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({GmailServiceImpl.class, Gmail.class})
public class GmailServiceImplTest {

    private static final String RECIPIENT_ADDRESS = "receiver@gmail.com";
    private static final String SUBJECT = "SUBJECT";
    private static final String BODY = "BODY";

    private static final GmailCredentials GMAIL_CREDENTIALS = GmailCredentials.builder()
            .userEmail("userEmail@gmail.com")
            .clientId("clientId")
            .clientSecret("clientSecret")
            .accessToken("accessToken")
            .refreshToken("refreshToken")
            .build();

    private GmailServiceImpl gmailServiceImpl;

    private MockHttpTransport httpTransport;
    private Gmail.Users.Messages messages;

    @Before
    public void setUp() throws Exception {
        httpTransport = new MockHttpTransport();
        gmailServiceImpl = new GmailServiceImpl(httpTransport);
        gmailServiceImpl.setGmailCredentials(GMAIL_CREDENTIALS);

        messages = mockMessages(mockUsers(mockGmail()));
    }

    @Test
    public void sendMessage() throws Exception {
        Gmail.Users.Messages.Send send = mockSend(messages, true);

        assertThat(gmailServiceImpl.sendMessage(RECIPIENT_ADDRESS, SUBJECT, BODY)).isTrue();
        verify(send).execute();
    }

    @Test
    public void sendMessage_notSent() throws Exception {
        Gmail.Users.Messages.Send send = mockSend(messages, false);

        assertThat(gmailServiceImpl.sendMessage(RECIPIENT_ADDRESS, SUBJECT, BODY)).isFalse();
        verify(send).execute();
    }

    private Gmail.Users.Messages.Send mockSend(Gmail.Users.Messages messages, boolean sent) throws IOException {
        Gmail.Users.Messages.Send send = mock(Gmail.Users.Messages.Send.class);
        doReturn(send).when(messages)
                .send(eq(GMAIL_CREDENTIALS.getUserEmail()), argThat(new ArgumentMatcher<Message>() {
                    @Override
                    public boolean matches(Object argument) {
                        try {
                            return matchMessage((Message) argument);
                        } catch (IOException | MessagingException e) {
                            fail("Error occurred while matching message", e);
                            return false;
                        }
                    }
                }));

        doReturn(new Message().setLabelIds(Collections.singletonList(sent ? "SENT" : ""))).when(send).execute();
        return send;
    }

    private Gmail.Users.Messages mockMessages(Gmail.Users users) {
        Gmail.Users.Messages messages = mock(Gmail.Users.Messages.class);
        doReturn(messages).when(users).messages();
        return messages;
    }

    private Gmail.Users mockUsers(Gmail gmail) {
        Gmail.Users users = mock(Gmail.Users.class);
        doReturn(users).when(gmail).users();
        return users;
    }

    private Gmail mockGmail() throws Exception {
        mockStatic(Gmail.class);
        Gmail gmail = mock(Gmail.class);
        whenNew(Gmail.class).withParameterTypes(Gmail.Builder.class)
                .withArguments(argThat(new ArgumentMatcher<Gmail.Builder>() {
                    @Override
                    public boolean matches(Object argument) {
                        Gmail.Builder builder = (Gmail.Builder) argument;
                        Credential credential = (Credential) builder.getHttpRequestInitializer();
                        return matchMainBuilderProperties(builder) && matchCredentialProperties(credential);
                    }
                }))
                .thenReturn(gmail);
        return gmail;
    }

    private boolean matchMessage(Message message) throws IOException, MessagingException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(Base64.decodeBase64(message.getRaw()));
        Session session = Session.getDefaultInstance(new Properties(), null);

        MimeMessage mimeMessage = new MimeMessage(session, byteArrayInputStream);
        return Arrays.equals(mimeMessage.getFrom(),
                new Address[]{new InternetAddress(GMAIL_CREDENTIALS.getUserEmail())})
                && Arrays.equals(mimeMessage.getRecipients(javax.mail.Message.RecipientType.TO),
                new Address[]{new InternetAddress(RECIPIENT_ADDRESS)})
                && SUBJECT.equals(mimeMessage.getSubject())
                && BODY.equals(mimeMessage.getContent());
    }

    private boolean matchMainBuilderProperties(Gmail.Builder builder) {
        return builder.getTransport() == httpTransport
                && "YOUR_APP_NAME".equals(builder.getApplicationName());
    }

    private boolean matchCredentialProperties(Credential credential) {
        return credential.getTransport() == httpTransport
                && GMAIL_CREDENTIALS.getAccessToken().equals(credential.getAccessToken())
                && GMAIL_CREDENTIALS.getRefreshToken().equals(credential.getRefreshToken())
                && matchClientAuthentication((ClientParametersAuthentication) credential.getClientAuthentication());
    }

    private boolean matchClientAuthentication(ClientParametersAuthentication clientAuthentication) {
        return GMAIL_CREDENTIALS.getClientId().equals(clientAuthentication.getClientId())
                && GMAIL_CREDENTIALS.getClientSecret().equals(clientAuthentication.getClientSecret());
    }
}