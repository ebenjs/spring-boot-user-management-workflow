package com.ebenjs.services.mail;

import com.ebenjs.models.mails.MailPayloads;
import org.thymeleaf.context.Context;

import java.util.Optional;

public interface MailSendingService {
    void sendSimpleToOne(String from, String to, String subject, String body);
    void sendHtmlToOne(String from, String to, String subject);
    void sendHtmlWithTemplateToOne(String from, String to, String subject, Context thymeleafContext, String templateLocation);
}
