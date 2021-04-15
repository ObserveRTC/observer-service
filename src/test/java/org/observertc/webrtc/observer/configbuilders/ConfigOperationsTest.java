package org.observertc.webrtc.observer.configbuilders;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ConfigOperationsTest {

    @Test
    public void shouldAssign_1() throws IOException {
        Map values = Map.of("a", 2);
        ConfigOperations configOps = new ConfigOperations(Map.of("a", 1));

        configOps.add(values);

        ConfigBuildersTestUtils.assertMapsEqual(configOps.makeConfig(), Map.of("a", 2));
    }

    @Test
    public void shouldAssign_2() throws IOException {
        Map values = Map.of("a", Map.of("b", 2));
        ConfigOperations configOps = new ConfigOperations(Map.of("a", Map.of("b", 1)));

        configOps.add(values);

        ConfigBuildersTestUtils.assertMapsEqual(configOps.makeConfig(), Map.of("a", Map.of("b", 2)));
    }

    @Test
    public void shouldAssign_3() throws IOException {
        Map values = Map.of("a", List.of(1, 2));
        ConfigOperations configOps = new ConfigOperations(Map.of("a", List.of(1, 3)));

        configOps.add(values);

        ConfigBuildersTestUtils.assertMapsEqual(configOps.makeConfig(), Map.of("a", List.of(1, 2, 3)));
    }

    @Test
    public void shouldAssign_withKeyMaker() throws IOException {
        Map values = Map.of("a", List.of(Map.of("id", 1, "value", 2)));
        ConfigNode configNode = new ConfigNode().withConfigNode(List.of("a"), new ConfigNode().withKeyMaker(o -> o instanceof Map ? ((Map) o).get("id").toString() : o.toString()));
        ConfigOperations configOps = new ConfigOperations(Map.of("a", List.of(Map.of("id", 1, "value", 1))))
                .withConfigPredicate(configNode);

        configOps.add(values);

        ConfigBuildersTestUtils.assertMapsEqual(configOps.makeConfig(), Map.of("a", List.of(Map.of("id", 1, "value", 2))));
    }

    @Test
    public void shouldRemove_withKeyMaker() throws IOException {
        ConfigNode configNode = new ConfigNode().withConfigNode(List.of("a"), new ConfigNode().withKeyMaker(o -> o instanceof Map ? ((Map) o).get("id").toString() : o.toString()));
        ConfigOperations configOps = new ConfigOperations(Map.of("a", List.of(Map.of("id", 2), Map.of("id", 1))))
                .withConfigPredicate(configNode);

        configOps.remove(List.of("a", "2"));

        ConfigBuildersTestUtils.assertMapsEqual(configOps.makeConfig(), Map.of("a", List.of(Map.of("id", 1))));
    }

    @Test
    public void shouldRemove_withKeyMaker_2() throws IOException {
        ConfigNode configNode = new ConfigNode().withConfigNode(List.of("connectors"), new ConfigNode().withKeyMaker(o -> o instanceof Map ? ((Map) o).get("name").toString() : o.toString()));
        ConfigOperations configOps = new ConfigOperations(Map.of("connectors", List.of(Map.of("name", "mongo", "config", Map.of("addMetaKey", true, "server", "localhost")))))
               .withConfigPredicate(configNode);

        configOps.remove(List.of("connectors", "mongo", "config", "addMetaKey"));

        ConfigBuildersTestUtils.assertMapsEqual(configOps.makeConfig(), Map.of("connectors", List.of(Map.of("name", "mongo", "config", Map.of("server", "localhost")))));
    }


}