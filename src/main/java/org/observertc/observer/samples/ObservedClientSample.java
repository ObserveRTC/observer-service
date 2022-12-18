package org.observertc.observer.samples;

import org.observertc.schemas.samples.Samples.ClientSample;

import java.util.Objects;

public interface ObservedClientSample {

    static Builder builder() {
        return new Builder();
    }

    String getServiceId();

    ServiceRoomId getServiceRoomId();

    String getMediaUnitId();

    String getTimeZoneId();

    ClientSample getClientSample();

    class Builder {
        private String serviceId = null;
        private String mediaUnitId = null;
        private String timeZoneId = null;
        private ClientSample clientSample = null;
        private ServiceRoomId serviceRoomId = null;

        public Builder setServiceId(String value) {
            this.serviceId = value;
            return this;
        }

        public Builder setMediaUnitId(String value) {
            this.mediaUnitId = value;
            return this;
        }

        public Builder setTimeZoneId(String value) {
            this.timeZoneId = value;
            return this;
        }

        public Builder setClientSample(ClientSample value) {
            this.clientSample = value;
            return this;
        }

        public ObservedClientSample build() {
            return new ObservedClientSample() {
                @Override
                public String getServiceId() {
                    return serviceId;
                }

                @Override
                public String getMediaUnitId() {
                    return mediaUnitId;
                }

                @Override
                public String getTimeZoneId() {
                    return timeZoneId;
                }

                @Override
                public ServiceRoomId getServiceRoomId() {
                    if (Objects.nonNull(serviceRoomId)) return serviceRoomId;
                    serviceRoomId = ServiceRoomId.make(serviceId, clientSample.roomId);
                    return serviceRoomId;
                }

                @Override
                public ClientSample getClientSample() {
                    return clientSample;
                }
            };
        }
    }
}
