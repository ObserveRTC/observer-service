package org.observertc.webrtc.observer.connector.transformations;

import org.observertc.webrtc.schemas.reports.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class ReportObfuscator extends Transformation {

    private static final Logger logger = LoggerFactory.getLogger(ReportObfuscator.class);
    private final MessageDigest messageDigest;
    private Function<String, String> serviceNameObfuscator;
    private Function<String, String> markerObfuscator;

    public ReportObfuscator(MessageDigest messageDigest) {
        this.messageDigest = messageDigest;
        this.serviceNameObfuscator = this::digest;
        this.markerObfuscator = this::digest;
    }

    ReportObfuscator withServiceName(String newServiceName) {
        this.serviceNameObfuscator = in -> newServiceName;
        return this;
    }

    ReportObfuscator withMarker(String newMarkerName) {
        this.markerObfuscator = in -> newMarkerName;
        return this;
    }

    @Override
    protected Optional<Report> transform(Report original) throws Throwable {
        Report.Builder builder = Report.newBuilder();
        builder.setVersion(original.getVersion());
        builder.setTimestamp(original.getTimestamp());
        this.baseChange(builder, original);

        builder.setPayload(original.getPayload());
        return Optional.of(builder.build());
    }

    private void baseChange(Report.Builder builder, final Report original) {
        String newServiceUUID = obfuscateUUIDSource(original.getServiceUUID());
        builder.setServiceUUID(newServiceUUID);
        String newServiceName = this.serviceNameObfuscator.apply(original.getServiceName());
        builder.setServiceName(newServiceName);
        String newMarker = this.markerObfuscator.apply(original.getMarker());
        builder.setMarker(newMarker);
    }

    private String digest(String source) {
        if (Objects.isNull(source)) {
            return null;
        }
        messageDigest.reset();
        messageDigest.update(source.getBytes(StandardCharsets.UTF_8));
        return new String(messageDigest.digest());
    }

    private String obfuscateUUIDSource(String source) {
        if (Objects.isNull(source)) {
            return null;
        }
        String digestedStr = this.digest(source);
        UUID result = UUID.nameUUIDFromBytes(digestedStr.getBytes());
        return result.toString();
    }
}
