package org.observertc.observer.simulator;

import org.observertc.observer.utils.RandomGenerators;

public class Networks {
    public static PeerToPeerConnection createPeerToPeerConnection() {
        return new PeerToPeerConnection();
    }

    public static ClientsToSfuMesh createClientsToSfuMesh() {
        return new ClientsToSfuMesh();
    }

    public static ClientsToSfuConnection createClientsToSfuConnection() {
        return new ClientsToSfuConnection();
    }

    private Networks() {

    }

    public static class PeerToPeerConnection {
        public final ClientSurrogate alice;
        public final ClientSurrogate bob;

        private PeerToPeerConnection() {
            var generator = new RandomGenerators();
            var roomId = generator.getRandomTestRoomIds();
            this.alice = new ClientSurrogate(roomId);
            this.bob = new ClientSurrogate(roomId);

            // create connections
            alice.connect(this.bob);

            this.alice.turnCamOn();
            this.alice.turnMicOn();
            this.bob.turnCamOn();
            this.bob.turnMicOn();
        }
    }

    public static class ClientsToSfuMesh {

        public final SfuSurrogate asiaSfu;
        public final SfuSurrogate euSfu;
        public final ClientSurrogate alice;
        public final ClientSurrogate bob;

        private ClientsToSfuMesh() {
            var generator = new RandomGenerators();
            var roomId = generator.getRandomTestRoomIds();
            this.asiaSfu = new SfuSurrogate();
            this.euSfu = new SfuSurrogate();
            this.alice = new ClientSurrogate(roomId);
            this.bob = new ClientSurrogate(roomId);

            // create connections
            this.alice.connect(this.asiaSfu);
            this.bob.connect(this.euSfu);
            this.asiaSfu.pipe(this.euSfu);

            this.alice.turnCamOn();
            this.alice.turnMicOn();
            this.bob.turnCamOn();
            this.bob.turnMicOn();
        }
    }

    public static class ClientsToSfuConnection {

        public final SfuSurrogate sfu;
        public final ClientSurrogate alice;
        public final ClientSurrogate bob;

        private ClientsToSfuConnection() {
            var generator = new RandomGenerators();
            var roomId = generator.getRandomTestRoomIds();
            this.sfu = new SfuSurrogate();
            this.alice = new ClientSurrogate(roomId);
            this.bob = new ClientSurrogate(roomId);

            // create connections
            this.alice.connect(this.sfu);
            this.bob.connect(this.sfu);

            this.alice.turnCamOn();
            this.alice.turnMicOn();
            this.bob.turnCamOn();
            this.bob.turnMicOn();
        }

    }
}
