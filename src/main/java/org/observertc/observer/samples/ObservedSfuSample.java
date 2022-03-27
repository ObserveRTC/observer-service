package org.observertc.observer.samples;

import org.observertc.schemas.samples.Samples;
import org.observertc.schemas.samples.Samples.SfuSample;

public interface ObservedSfuSample {

    static ObservedSfuSample.Builder builder() {
        return null;
    }

    String getMediaUnitId();

    String getTimeZoneId();

    String getServiceId();

    SfuSample getSfuSample();

    class Builder {
        private String serviceId = null;
        private String mediaUnitId = null;
        private String timeZoneId = null;
        private Samples.SfuSample sfuSample = null;
        private ServiceRoomId serviceRoomId = null;

        public ObservedSfuSample.Builder setServiceId(String value) {
            this.serviceId = value;
            return this;
        }

        public ObservedSfuSample.Builder setMediaUnitId(String value) {
            this.mediaUnitId = value;
            return this;
        }

        public ObservedSfuSample.Builder setTimeZoneId(String value) {
            this.timeZoneId = value;
            return this;
        }

        public ObservedSfuSample.Builder setSfuSample(Samples.SfuSample value) {
            this.sfuSample = value;
            return this;
        }

        public ObservedSfuSample build() {
            return new ObservedSfuSample() {
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
                public Samples.SfuSample getSfuSample() {
                    return sfuSample;
                }
            };
        }
    }

}
