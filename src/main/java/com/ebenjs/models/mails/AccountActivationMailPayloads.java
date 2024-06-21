package com.ebenjs.models.mails;

import lombok.Data;

@Data
public class AccountActivationMailPayloads extends MailPayloads {
    private String name;
    private String url;
}
