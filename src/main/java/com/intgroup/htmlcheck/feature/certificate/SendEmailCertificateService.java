package com.intgroup.htmlcheck.feature.certificate;

import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

@Service
public class SendEmailCertificateService {
    public void sendCertificate(String email, String fullName) throws IOException {
        BufferedImage certificate = CreateCertificateImageService.makeCertificate(fullName);

        // Recipient's email ID needs to be mentioned.
        String to = email;

        // Sender's email ID needs to be mentioned
        String from = "gofrontendcert@gmail.com";

        // Assuming you are sending email from through gmails smtp
        String host = "smtp.gmail.com";

        // Get system properties
        Properties properties = System.getProperties();

        // Setup mail server
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.mime.charset", "utf-8");

        // Get the Session object.// and pass username and password
        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {

            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, "fdsag54ehtwrgRT4h5w$%GF4qr");
            }

        });

        // Used to debug SMTP issues
        session.setDebug(true);

        try {
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);
            message.setHeader("Content-Type","text/html;charset=utf-8");

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));

            // Set To: header field of the header.
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            // Set Subject: header field
            message.setSubject("Ваш сертификат от GoIT");

            // Now set the actual message
            String msg = "<h1>Поздравляем!</h1> " +
                    "<p>Если вы видите это сообщение, значит вы успешно решили набор задач по Javascript.<p>" +
                    " <p>Вот ваш сертификат:<p>";

            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setContent(msg, "text/html; charset=utf-8");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(mimeBodyPart);

            message.setContent(multipart, "text/html; charset=utf-8");

            //Attach certificate image
            MimeBodyPart certificateImageAttachment = new MimeBodyPart();
            //Save image to tmp file

            //Prepare directory
            String directory = "./data/cert-" + System.currentTimeMillis();
            new File(directory).mkdirs();

            String tmpFileName = directory + "/certificate.png";
            ImageIO.write(certificate,"png", new File(tmpFileName));
            certificateImageAttachment.attachFile(new File(tmpFileName));
            multipart.addBodyPart(certificateImageAttachment);

            System.out.println("sending...");
            // Send message
            Transport.send(message);

            //Delete tmp file & directory
            new File(tmpFileName).delete();
            new File(directory).delete();
            System.out.println("Sent message successfully....");

        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        SendEmailCertificateService sendEmailCertificateService = new SendEmailCertificateService();
        sendEmailCertificateService.sendCertificate("melnichuk.cadet@gmail.com", "Ivan Melnichuk");
    }
}
