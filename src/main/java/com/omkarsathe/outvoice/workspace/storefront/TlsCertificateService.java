package com.omkarsathe.outvoice.workspace.storefront;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TlsCertificateService {

    public TlsStatus provisionOrCheckCertificate(StorefrontDomain domain) {
        if (domain.getVerificationStatus() == DomainVerificationStatus.VERIFIED) {
            log.info("TLS certificate active for verified domain: {}", domain.getHostname());
            return TlsStatus.ACTIVE;
        }
        return TlsStatus.PENDING;
    }
}
