package com.ebenjs.services.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class MailSendingServiceJavaMailImpl implements MailSendingService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Override
    public void sendSimpleToOne(String from, String to, String subject, String body) {

    }

    @Override
    public void sendHtmlToOne(String from, String to, String subject) {

    }

    @Override
    public void sendHtmlWithTemplateToOne(String from, String to, String subject, Context thymeleafContext, String templateLocation) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message, true);
            String htmlEmail = templateEngine.process(templateLocation, thymeleafContext);

            mimeMessageHelper.setFrom(from);
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(htmlEmail, true);

            javaMailSender.send(message);
            System.out.println("E-mail envoyé avec succès !");
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }


    }
}
