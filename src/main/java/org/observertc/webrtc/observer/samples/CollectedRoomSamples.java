//package org.observertc.webrtc.observer.samples;
//
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.Map;
//import java.util.Objects;
//import java.util.stream.Stream;
//
///**
// * Call assigned and organized ObservedSamples
// */
//public class CollectedRoomSamples implements Iterable<RoomSamples> {
//
//    private final Map<ServiceRoomId, RoomSamples> samples = new HashMap<>();
//    public static Builder builder() {
//        return new Builder();
//    }
//
//    public CollectedRoomSamples() {
//
//    }
//
//    @Override
//    public Iterator<RoomSamples> iterator() {
//        return this.samples.values().iterator();
//    }
//
//    public Stream<RoomSamples> stream() {
//        return this.samples.values().stream();
//    }
//
//    public int size() {
//        return this.samples.size();
//    }
//
//    public static class Builder {
//        private final CollectedRoomSamples result = new CollectedRoomSamples();
//        private final Map<ServiceRoomId, RoomSamples.Builder> builders = new HashMap<>();
//
//        public Builder withObservedClientSample(ObservedClientSample observedClientSample) {
//            var serviceRoomId = ServiceRoomId.make(observedClientSample.getServiceId(), observedClientSample.getRoomId());
//            RoomSamples.Builder builder = this.builders.get(serviceRoomId);
//            if (Objects.isNull(builder)) {
//                builder = RoomSamples.builderFrom(serviceRoomId);
//                this.builders.put(serviceRoomId, builder);
//            }
//            builder.withObservedClientSample(observedClientSample);
//            return this;
//        }
//
//        public CollectedRoomSamples build() {
//            this.builders
//                    .values()
//                    .stream()
//                    .map(RoomSamples.Builder::build)
//                    .forEach(roomSamples -> this.result.samples.put(roomSamples.getServiceRoomId(), roomSamples));
//            return this.result;
//        }
//    }
//}
