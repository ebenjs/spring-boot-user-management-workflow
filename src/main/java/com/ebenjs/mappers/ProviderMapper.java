package com.ebenjs.mappers;

import com.ebenjs.enums.LoginProvider;

public class ProviderMapper {
    public static LoginProvider mapProvider(String provider) {
        switch (provider) {
            case "google":
                return LoginProvider.GOOGLE;
            case "linkedin":
                return LoginProvider.LINKEDIN;
            case "github":
                return LoginProvider.GITHUB;
            default:
                return LoginProvider.LOCAL;
        }
    }
}
