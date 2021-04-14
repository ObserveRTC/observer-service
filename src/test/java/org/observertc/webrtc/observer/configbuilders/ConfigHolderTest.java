package org.observertc.webrtc.observer.configbuilders;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class ConfigHolderTest {

    @Test
    public void testAdditionFlatMaps_1() {
        Map<String, Object> config = Map.of();
        var holder = new ConfigHolder<Map>(config, Map.class, new ConfigNode().withConfigNode(List.of("connectors"), new ConfigNode().withKeyMaker(o -> ((Map) o).get("name").toString())));

        config = Map.of("connectors", List.of(Map.of("name", "a")));
        holder.renew(config);

        ConfigBuildersTestUtils.assertMapsEqual(holder.getAdditions(), config);
        ConfigBuildersTestUtils.assertMapsEqual(holder.getAdditionsFlatMap(), Map.of("connectors.a.name", "a"));
    }

    @Test
    public void testRemovalFlatMaps_1() {
        Map<String, Object> baseConfig = Map.of("connectors", List.of(Map.of("name", "a")));
        var holder = new ConfigHolder<Map>(baseConfig, Map.class, new ConfigNode().withConfigNode(List.of("connectors"), new ConfigNode().withKeyMaker(o -> ((Map) o).get("name").toString())));

        Map<String, Object> newConfig = Map.of();
        holder.renew(newConfig);

        ConfigBuildersTestUtils.assertMapsEqual(holder.getRemovals(), baseConfig);
        ConfigBuildersTestUtils.assertMapsEqual(holder.getRemovalsFlatMap(), Map.of("connectors.a.name", "a"));
    }

    @Test
    public void test_1() throws IOException {
        var config = Map.of("a", Map.of("b", 1), "c", 2);
        var holder = new ConfigHolder<Map>(config, Map.class);

        holder.renew(Map.of("a", 1));

        ConfigBuildersTestUtils.assertMapsEqual(holder.getConfig(), Map.of("a", 1));
        ConfigBuildersTestUtils.assertMapsEqual(holder.getAdditions(), Map.of("a", 1));
        ConfigBuildersTestUtils.assertMapsEqual(holder.getRemovals(), Map.of("a", Map.of("b", 1), "c", 1));
    }

    @Test
    public void test_2() throws IOException {
        var config = Map.of("outboundReports", Map.of("reportOutboundRTP", true, "reportInboundRTP", false), "b", 1);
        var holder = new ConfigHolder<Map>(config, Map.class);

        config = Map.of("outboundReports", Map.of("reportOutboundRTP", false, "reportInboundRTP", false), "b", 1);
        holder.renew(config);

        ConfigBuildersTestUtils.assertMapsEqual(holder.getConfig(), config);
        ConfigBuildersTestUtils.assertMapsEqual(holder.getAdditions(), Map.of("outboundReports", Map.of("reportOutboundRTP", false)));
        ConfigBuildersTestUtils.assertMapsEqual(holder.getRemovals(), Map.of("outboundReports", Map.of("reportOutboundRTP", true)));
    }

    @Test
    public void test_3() throws IOException {
        var config = Map.of("connectors", List.of(Map.of("name", "MyConnector", "type", "Kafka")), "b", 1);
        var holder = new ConfigHolder<Map>(config, Map.class, new ConfigNode().withConfigNode(List.of("connectors"), new ConfigNode().withKeyMaker(o -> ((Map)o).get("name").toString())));

        config = Map.of("connectors", List.of(Map.of("name", "MyConnector", "type", "Kafka2")), "b", 1);
        holder.renew(config);

        ConfigBuildersTestUtils.assertMapsEqual(holder.getConfig(), config);
        ConfigBuildersTestUtils.assertMapsEqual(holder.getAdditions(), Map.of("connectors", List.of(Map.of("type", "Kafka2"))));
        ConfigBuildersTestUtils.assertMapsEqual(holder.getAdditionsFlatMap(), Map.of("connectors.MyConnector.type", "Kafka2"));
        ConfigBuildersTestUtils.assertMapsEqual(holder.getRemovals(), Map.of("connectors", List.of(Map.of("type", "Kafka"))));
    }

}