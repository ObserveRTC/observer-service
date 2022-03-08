package org.observertc.observer.utils;

import io.micronaut.context.annotation.Prototype;
import org.observertc.observer.TestUtils;
import org.observertc.observer.dto.StreamDirection;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Prototype
public class RandomGenerators {
    private final Random rand = new Random();
    private Long maxTime = Instant.now().toEpochMilli();
    private Long minTime = Instant.now().minusSeconds(100000).toEpochMilli();

    public<T> T getRandomFromList(List<T> givenList) {
        int randomElement = this.rand.nextInt(givenList.size());
        return givenList.get(randomElement);
    }

    public <T> Supplier<T> randomProviderFrom(List<T> givenList) {
        return () -> getRandomFromList(givenList);
    }

    public Long getRandomTimestamp() {
        Integer diff = this.maxTime.intValue() - this.minTime.intValue();
        Long randomElapsed = Long.valueOf(this.rand.nextInt(diff));
        return this.minTime + randomElapsed;
    }

    public RandomGenerators setMinTime(Long epochTImeInMs) {
        this.minTime = epochTImeInMs;
        return this;
    }

    public RandomGenerators sethMaxTime(Long epochTImeInMs) {
        this.maxTime = epochTImeInMs;
        return this;
    }

    public Integer getRandomPositiveInteger() {
        return this.rand.nextInt();
    }

    public Double getRandomPositiveDouble() {
        Integer num = this.rand.nextInt();
        return this.rand.nextDouble() * num.doubleValue();

    }

    public Long getRandomPositiveLong() {
        Long part1 = Integer.valueOf(this.rand.nextInt()).longValue();
        Long part2 = Integer.valueOf(this.rand.nextInt()).longValue();
        return part1 * part2;
    }

    public int getRandomPort() {
        int maxPort = 65535;
        int minPort = 1024;
        return this.rand.nextInt(maxPort - minPort) + minPort;
    }

    public String getRandomIPv4Address() {
        String result = this.rand.nextInt(256) + "." +
                this.rand.nextInt(256) + "." +
                this.rand.nextInt(256) + "." +
                this.rand.nextInt(256);
        return result;
    }

    private List<String> userIds = TestUtils.getTestUserIds();
    public String getRandomTestUserIds() {
        return this.getRandomFromList(this.userIds);
    }

    private List<String> zoneIds = ZoneId.SHORT_IDS.keySet().stream().collect(Collectors.toList());
    public String getRandomTimeZoneId() {
        return this.getRandomFromList(this.zoneIds);
    }

    private List<String> serviceIds = TestUtils.getServiceIds();
    public String getRandomServiceId() {
        return this.getRandomFromList(this.serviceIds);
    }

    private List<String> roomIds = TestUtils.getTestRoomIds();
    public String getRandomTestRoomIds() {
        return this.getRandomFromList(roomIds);
    }

    private List<String> clientSideMediaUnitIds = TestUtils.getClientSideMediaUintIds();
    public String getRandomClientSideMediaUnitId() {
        return this.getRandomFromList(this.clientSideMediaUnitIds);
    }

    private List<String> sfuSideMediaUnitIds = TestUtils.getSFUSideMediaUintIds();
    public String getRandomSFUSideMediaUnitId() {
        return this.getRandomFromList(this.sfuSideMediaUnitIds);
    }

    private List<String> pcLabels = TestUtils.getLabels();
    public String getRandomLabels() {
        return this.getRandomFromList(pcLabels);
    }

    private List<String> iceRoles = TestUtils.getIceRole();
    public String getRandomIceRole() {
        return this.getRandomFromList(iceRoles);
    }

    private List<String> dtlsStates = TestUtils.getDtlsState();
    public String getRandomDtlsState() {
        return this.getRandomFromList(dtlsStates);
    }

    private List<String> iceStates = TestUtils.getIceState();
    public String getRandomIceState() {
        return this.getRandomFromList(iceStates);
    }

    private List<String> dtlsCiphers = TestUtils.getDtlsCipher();
    public String getRandomDtlsCipher() {
        return this.getRandomFromList(dtlsCiphers);
    }

    private List<String> srtpCiphers = TestUtils.getSrtpCipher();
    public String getRandomSrtpCipher() {
        return this.getRandomFromList(srtpCiphers);
    }

    private List<String> candidatePairStates = TestUtils.getCandidatePairState();
    public String getRandomCandidatePairState() {
        return this.getRandomFromList(candidatePairStates);
    }

    List<String> transportProtocols = TestUtils.getNetworkTransportProtocols();
    public String getRandomNetworkTransportProtocols() {
        return this.getRandomFromList(transportProtocols);
    }

    List<String> iceCandidateTypes = TestUtils.getICECandidateTypes();
    public String getRandomICECandidateTypes() {
        return this.getRandomFromList(iceCandidateTypes);
    }

    List<String> relayProtocols = TestUtils.getRelayProtocols();
    public String getRandomRelayProtocols() {
        return this.getRandomFromList(relayProtocols);
    }

    public Long getRandomSSRC() {
        var result = this.rand.nextLong() % 4_294_967_296L;
        return Math.abs(result);
    }

    private List<StreamDirection> streamDirections = Arrays.asList(StreamDirection.values());
    public StreamDirection getRandomStreamDirection() {
        return this.getRandomFromList(streamDirections);
    }
}
