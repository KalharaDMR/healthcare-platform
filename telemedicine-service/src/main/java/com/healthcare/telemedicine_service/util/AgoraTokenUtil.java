package com.healthcare.telemedicine_service.util;

import io.agora.media.RtcTokenBuilder2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.time.Instant;

@Component
public class AgoraTokenUtil {

    @Value("${agora.appId}")
    private String appId;

    @Value("${agora.appCertificate}")
    private String appCertificate;

    @Value("${agora.expiration}")
    private int expirationInSeconds;

    public String generateToken(String channelName, int uid, int role) {
        RtcTokenBuilder2 tokenBuilder = new RtcTokenBuilder2();
        RtcTokenBuilder2.Role tokenRole;

        // Assign the correct enum constant based on the 'role' parameter
        // 1 = Publisher (host), other = Subscriber (audience)
        if (role == 1) {
            tokenRole = RtcTokenBuilder2.Role.ROLE_PUBLISHER;
        } else {
            tokenRole = RtcTokenBuilder2.Role.ROLE_SUBSCRIBER;
        }

        // Agora expects absolute privilege-expiration timestamps (epoch seconds),
        // not a duration value like 3600.
        int currentTs = (int) Instant.now().getEpochSecond();
        int privilegeExpireTs = currentTs + expirationInSeconds;

        // Build and return the token
        String token = tokenBuilder.buildTokenWithUid(
                appId,
                appCertificate,
                channelName,
                uid,
                tokenRole,
                privilegeExpireTs,
                privilegeExpireTs
        );
        return token;
    }
}