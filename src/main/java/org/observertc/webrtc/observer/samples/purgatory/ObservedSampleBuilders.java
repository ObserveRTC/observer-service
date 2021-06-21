//package org.observertc.webrtc.observer.samples;
//
//import org.observertc.webrtc.observer.common.UUIDAdapter;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.*;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
//public class ObservedSampleBuilders {
//    private static final Logger logger = LoggerFactory.getLogger(ObservedSampleBuilders.class);
//
//    private Map<String, List<ObservedSampleBuilder>> builders = new HashMap<>();
//    private Set<ServiceRoomId> serviceRoomIds = new HashSet<>();
//
//    public void add(ObservedSampleBuilder observedSampleBuilder) {
//        String clientId = observedSampleBuilder.getClientId();
//        if (Objects.isNull(clientId)) {
//            logger.warn("Null client id is not allowed to be processed");
//            return;
//        }
//        List<ObservedSampleBuilder> samples = this.builders.get(clientId);
//        if (Objects.isNull(samples)) {
//            samples = new LinkedList<>();
//            this.builders.put(clientId, samples);
//        }
//        samples.add(observedSampleBuilder);
//
//        var serviceRoomId = ServiceRoomId.make(
//                observedSampleBuilder.getServiceId(),
//                observedSampleBuilder.getRoomId()
//        );
//        serviceRoomIds.add(serviceRoomId);
//    }
//
//    public Set<UUID> getClientIds() {
//        Set<UUID> result = this.builders.keySet()
//                .stream()
//                .map(clientId -> UUIDAdapter.tryParse(clientId))
//                .filter(Optional::isPresent)
//                .map(Optional::get)
//                .collect(Collectors.toSet());
//        return result;
//    }
//
//    public Set<ServiceRoomId> getServiceRoomIds() {
//        return this.serviceRoomIds;
//    }
//
//    private Stream<ObservedSampleBuilder> stream() {
//        return this.builders.values().stream().flatMap(List::stream);
//    }
//
//    public Iterator<Map.Entry<UUID, ObservedSampleBuilder>> iterateByClients() {
//        return this.stream()
//                .map(builder -> {
//                    Optional<UUID> clientId = UUIDAdapter.tryParse(builder.getClientId());
//                    if (clientId.isEmpty()) {
//                        return null;
//                    }
//                    return Map.entry(clientId.get(), builder);
//                })
//                .filter(Objects::nonNull)
//                .iterator();
//    }
//
//    public Iterator<Map.Entry<ServiceRoomId, ObservedSampleBuilder>> iterateByServiceRoomIds() {
//        return this.stream()
//                .filter(Objects::nonNull)
//                .map(builder -> {
//                    var serviceRoomId = builder.getServiceRoomId();
//                    return Map.entry(serviceRoomId, builder);
//                })
//                .iterator();
//    }
//
////    public Map<UUID, ObservedSamples> build() {
////        Map<UUID, ObservedSamples> result = new HashMap<>();
////        this.builders.forEach((clientIdStr, builders) -> {
////            var clientId = UUID.fromString(clientIdStr);
////            var observedSamples = new ObservedSamples(clientId);
////            builders.forEach(builder -> observedSamples.add(builder.build()));
////            result.put(clientId, observedSamples);
////        });
////        return result;
////    }
//
//}
