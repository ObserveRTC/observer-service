package org.observertc.webrtc.observer;

import io.micronaut.context.annotation.Prototype;

import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

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

    public RandomGenerators withMinTime(Long epochTImeInMs) {
        this.minTime = epochTImeInMs;
        return this;
    }

    public RandomGenerators withMaxTime(Long epochTImeInMs) {
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

    public String getRandomIpAddress() {
        String result = this.rand.nextInt(256) + "." +
                this.rand.nextInt(256) + "." +
                this.rand.nextInt(256) + "." +
                this.rand.nextInt(256);
        return result;
    }
}
