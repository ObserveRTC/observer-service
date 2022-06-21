package org.observertc.observer.security.credentialbuilders;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;

interface Builder {
    AwsCredentialsProvider build();
}
