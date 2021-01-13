package org.observertc.webrtc.observer.models;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import org.observertc.webrtc.observer.common.ObjectToString;
import org.observertc.webrtc.observer.common.UUIDAdapter;
import org.observertc.webrtc.schemas.reports.CandidateType;
import org.observertc.webrtc.schemas.reports.ICEState;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class ICEConnectionEntity implements Portable {
    public static final int CLASS_ID = 4000;
    private static final String SERVICE_UUID_FIELD_NAME = "serviceUUID";
    private static final String MEDIA_UNIT_ID_FIELD_NAME = "mediaUnitId";
    private static final String PC_UUID_FIELD_NAME = "pcUUID";
    private static final String LOCAL_CANDIDATE_ID_FIELD_NAME = "localCandidateId";
    private static final String REMOTE_CANDIDATE_ID_FIELD_NAME = "remoteCandidateId";
    private static final String LOCAL_CANDIDATE_TYPE_FIELD_NAME = "localCandidateType";
    private static final String REMOTE_CANDIDATE_TYPE_FIELD_NAME = "remoteCandidateType";
    private static final String NOMINATED_FIELD_NAME = "nominated";
    private static final String ICE_STATE_FIELD_NAME = "iceState";

    public static ICEConnectionEntity of(
            UUID serviceUUID,
            String mediaUnitId,
            UUID pcUUID,
            String localCandidateId,
            String remoteCandidateId,
            CandidateType localCandidateType,
            CandidateType remoteCandidateType,
            boolean nominated,
            ICEState iceState
    ) {
        ICEConnectionEntity result = new ICEConnectionEntity();
        result.serviceUUID = serviceUUID;
        result.pcUUID = pcUUID;
        result.remoteCandidateId = remoteCandidateId;
        result.localCandidateId = localCandidateId;
        result.mediaUnitId = mediaUnitId;
        result.localCandidateType = localCandidateType;
        result.remoteCandidateType = remoteCandidateType;
        result.nominated = nominated;
        return result;
    }

    public UUID serviceUUID;
    public UUID pcUUID;
    public String localCandidateId;
    public String remoteCandidateId;
    public String mediaUnitId;
    public boolean nominated;
    public CandidateType localCandidateType;
    public CandidateType remoteCandidateType;
    public ICEState state;

    @Override
    public int getFactoryId() {
        return EntityFactory.FACTORY_ID;
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {

        writer.writeByteArray(SERVICE_UUID_FIELD_NAME, UUIDAdapter.toBytesOrDefault(this.serviceUUID, EntityFactory.DEFAULT_UUID_BYTES));
        writer.writeByteArray(PC_UUID_FIELD_NAME, UUIDAdapter.toBytesOrDefault(this.pcUUID, EntityFactory.DEFAULT_UUID_BYTES));
        writer.writeUTF(MEDIA_UNIT_ID_FIELD_NAME, this.mediaUnitId);
        writer.writeUTF(LOCAL_CANDIDATE_ID_FIELD_NAME, this.localCandidateId);
        writer.writeUTF(REMOTE_CANDIDATE_ID_FIELD_NAME, this.remoteCandidateId);
        if (Objects.nonNull(this.localCandidateType)) {
            writer.writeUTF(LOCAL_CANDIDATE_TYPE_FIELD_NAME, this.localCandidateType.name());
        }
        if (Objects.nonNull(this.remoteCandidateType)) {
            writer.writeUTF(REMOTE_CANDIDATE_TYPE_FIELD_NAME, this.remoteCandidateType.name());
        }
        writer.writeBoolean(NOMINATED_FIELD_NAME, this.nominated);

        if (Objects.nonNull(this.state)) {
            writer.writeUTF(ICE_STATE_FIELD_NAME, this.state.name());
        }
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        this.serviceUUID = UUIDAdapter.toUUIDOrDefault(reader.readByteArray(SERVICE_UUID_FIELD_NAME), null);
        this.pcUUID = UUIDAdapter.toUUIDOrDefault(reader.readByteArray(PC_UUID_FIELD_NAME), null);
        this.mediaUnitId = reader.readUTF(MEDIA_UNIT_ID_FIELD_NAME);
        this.localCandidateId = reader.readUTF(LOCAL_CANDIDATE_ID_FIELD_NAME);
        this.remoteCandidateId = reader.readUTF(REMOTE_CANDIDATE_ID_FIELD_NAME);
        if (reader.hasField(LOCAL_CANDIDATE_TYPE_FIELD_NAME)) {
            this.localCandidateType = CandidateType.valueOf(reader.readUTF(LOCAL_CANDIDATE_TYPE_FIELD_NAME));
        }
        if (reader.hasField(REMOTE_CANDIDATE_TYPE_FIELD_NAME)) {
            this.remoteCandidateType = CandidateType.valueOf(reader.readUTF(REMOTE_CANDIDATE_TYPE_FIELD_NAME));
        }
        this.nominated = reader.readBoolean(NOMINATED_FIELD_NAME);
        if (reader.hasField(ICE_STATE_FIELD_NAME)) {
            this.state = ICEState.valueOf(reader.readUTF(ICE_STATE_FIELD_NAME));
        }
    }

    @Override
    public String toString() {
        return ObjectToString.toString(this);
    }
}
