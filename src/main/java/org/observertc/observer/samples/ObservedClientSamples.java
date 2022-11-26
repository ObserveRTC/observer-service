package org.observertc.observer.samples;

import org.observertc.observer.common.FlatIterator;
import org.observertc.observer.common.MinuteToTimeZoneOffsetConverter;
import org.observertc.schemas.samples.Samples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface ObservedClientSamples extends Iterable<ObservedClientSample> {
    static final Logger logger = LoggerFactory.getLogger(ObservedClientSamples.class);

    static ObservedClientSamples EMPTY_SAMPLES = ObservedClientSamples.builder().build();

    static Builder builder() {
        return new Builder();
    }

    static Builder builderFrom(ObservedClientSamples other) {
        var result = new Builder();
        other.stream().forEach(observedClientSample -> {
            result.add(
                    observedClientSample.getServiceId(),
                    observedClientSample.getMediaUnitId(),
                    observedClientSample.getClientSample()
            );
        });
        return result;
    }

    boolean isEmpty();
    int size();
    Stream<ObservedClientSample> stream();
    Iterable<ObservedRoom> observedRooms();
    ObservedRoom getRoom(ServiceRoomId serviceRoomId);

    Set<ServiceRoomId> getServiceRoomIds();
    Set<String> getClientIds();
    Set<String> getPeerConnectionIds();
    Set<String> getInboundTrackIds();
    Set<String> getOutboundTrackIds();

    class Builder {
        MinuteToTimeZoneOffsetConverter minuteToTimeZoneOffsetConverter = new MinuteToTimeZoneOffsetConverter();
        Set<String> clientIds = new HashSet<>();
        Set<String> peerConnectionIds = new HashSet<>();
        Set<String> inboundTrackIds = new HashSet<>();
        Set<String> outboundTrackIds = new HashSet<>();
        private Set<ServiceRoomId> serviceRoomIds = new HashSet<>();
        private int size = 0;

        private Map<String, Map<String, ObservedRoom>> observedServices = new HashMap<>();
        private Map<ServiceRoomId, ObservedRoom> observedServiceRooms = new HashMap<>();

        public Builder add(String serviceId, String mediaUnitId, Samples.ClientSample clientSample) {
            var observedRooms = this.observedServices.get(serviceId);
            if (observedRooms == null) {
                observedRooms = new HashMap<>();
                this.observedServices.put(serviceId, observedRooms);
            }
            ObservedRoom.Builder observedRoom = (ObservedRoom.Builder) observedRooms.get(clientSample.roomId);
            if (observedRoom == null) {
                var serviceRoomId = ServiceRoomId.make(serviceId, clientSample.roomId);
                observedRoom = new ObservedRoom.Builder(this, serviceRoomId);
                observedRooms.put(clientSample.roomId, observedRoom);
                this.observedServiceRooms.put(serviceRoomId, observedRoom);
                this.serviceRoomIds.add(serviceRoomId);
            }
            observedRoom.add(mediaUnitId, clientSample);
            ++this.size;
            return this;
        }

        public ObservedClientSamples build() {
            return new ObservedClientSamples() {


                @Override
                public boolean isEmpty() {
                    return observedServiceRooms.isEmpty();
                }

                @Override
                public Iterable<ObservedRoom> observedRooms() {
                    return () -> observedServiceRooms.values().iterator();
                }

                @Override
                public int size() {
                    return size;
                }

                @Override
                public ObservedRoom getRoom(ServiceRoomId serviceRoomId) {
                    return observedServiceRooms.get(serviceRoomId);
                }

                @Override
                public Set<ServiceRoomId> getServiceRoomIds() {
                    return serviceRoomIds;
                }

                @Override
                public Set<String> getClientIds() {
                    return clientIds;
                }

                @Override
                public Set<String> getPeerConnectionIds() {
                    return peerConnectionIds;
                }

                @Override
                public Set<String> getInboundTrackIds() {
                    return inboundTrackIds;
                }

                @Override
                public Set<String> getOutboundTrackIds() {
                    return outboundTrackIds;
                }

                @Override
                public Stream<ObservedClientSample> stream() {
                    return StreamSupport.stream(
                            Spliterators.spliteratorUnknownSize(this.iterator(), Spliterator.ORDERED),
                            false);
                }

                @Override
                public Iterator<ObservedClientSample> iterator() {
                    return new FlatIterator<ObservedClientSample>(
                            observedServiceRooms.values().iterator(),
                            room -> room.observedClientSamples().iterator()
                    );
                }
            };
        }

        public Builder addObservedClientSample(ObservedClientSample observedClientSample) {
            return this.add(
                    observedClientSample.getServiceId(),
                    observedClientSample.getMediaUnitId(),
                    observedClientSample.getClientSample()
            );
        }
    }
}
