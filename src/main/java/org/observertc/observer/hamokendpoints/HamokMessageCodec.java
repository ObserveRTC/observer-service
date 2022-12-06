package org.observertc.observer.hamokendpoints;

import com.google.protobuf.ByteString;
import io.github.balazskreith.hamok.storagegrid.messages.Message;
import org.observertc.observer.mappings.Codec;
import org.observertc.observer.mappings.Mapper;
import org.observertc.schemas.dtos.Hamokmessage;

import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class HamokMessageCodec implements Codec<Message, Hamokmessage.HamokMessage> {

    @Override
    public Message decode(Hamokmessage.HamokMessage data) throws Throwable {
        var message = new Message();
        if (data.hasSourceId()) {
            message.sourceId = UUID.fromString(data.getSourceId());
        }
        if (data.hasDestinationId()) {
            message.destinationId = UUID.fromString(data.getDestinationId());
        }
        if (data.hasStorageId()) {
            message.storageId = data.getStorageId();
        }
        if (data.hasProtocol()) {
            message.protocol = data.getProtocol();
        }
        if (data.hasRequestId()) {
            message.sourceId = UUID.fromString(data.getRequestId());
        }

        if (data.hasStorageSize()) {
            message.storageSize = data.getStorageSize();
        }
        if (data.hasTimestamp()) {
            message.timestamp = data.getTimestamp();
        }
        if (data.hasType()) {
            message.type = data.getType();
        }

        if (0 < data.getKeysCount()) {
            message.keys = data.getKeysList().stream()
                    .map(ByteString::toByteArray)
                    .collect(Collectors.toList());
        }
        if (0 < data.getValuesCount()) {
            message.values = data.getValuesList().stream()
                    .map(ByteString::toByteArray)
                    .collect(Collectors.toList());
        }
        if (0 < data.getActiveEndpointIdsCount()) {
            message.activeEndpointIds = data.getActiveEndpointIdsList().stream()
                    .map(UUID::fromString)
                    .collect(Collectors.toList());
        }
        if (0 < data.getEmbeddedMessagesCount()) {
            var mapper = Mapper.create(this::decode);
            message.embeddedMessages = data.getEmbeddedMessagesList().stream()
                    .map(m -> mapper.map(m))
                    .collect(Collectors.toList());
        }

        if (data.hasSuccess()) {
            message.success = data.getSuccess();
        }
        if (data.hasExecuteSync()) {
            message.executeSync = data.getExecuteSync();
        }
        if (data.hasRaftLeaderId()) {
            message.raftLeaderId = UUID.fromString(data.getRaftLeaderId());
        }
        if (data.hasRaftNumberOfLogs()) {
            message.raftNumberOfLogs = data.getRaftNumberOfLogs();
        }
        if (data.hasRaftLastAppliedIndex()) {
            message.raftLastAppliedIndex = data.getRaftLastAppliedIndex();
        }
        if (data.hasRaftCommitIndex()) {
            message.raftCommitIndex = data.getRaftCommitIndex();
        }
        if (data.hasRaftLeaderNextIndex()) {
            message.raftLeaderNextIndex = data.getRaftLeaderNextIndex();
        }
        if (data.hasRaftPrevLogTerm()) {
            message.raftPrevLogTerm = data.getRaftPrevLogTerm();
        }
        if (data.hasRaftPrevLogIndex()) {
            message.raftPrevLogIndex = data.getRaftPrevLogIndex();
        }
        if (data.hasRaftTerm()) {
            message.raftTerm = data.getRaftTerm();
        }
        if (data.hasRaftPeerNextIndex()) {
            message.raftPeerNextIndex = data.getRaftPeerNextIndex();
        }
        if (data.hasRaftCandidateId()) {
            message.raftCandidateId = UUID.fromString(data.getRaftCandidateId());
        }
        if (data.hasSequence()) {
            message.sequence = data.getSequence();
        }
        if (data.hasLastMessage()) {
            message.lastMessage = data.getLastMessage();
        }
        return message;
    }

    @Override
    public Hamokmessage.HamokMessage encode(Message data) throws Throwable {
        var builder = Hamokmessage.HamokMessage.newBuilder();
        if (data.sourceId != null) {
            builder.setSourceId(data.sourceId.toString());
        }
        if (data.destinationId != null) {
            builder.setDestinationId(data.destinationId.toString());
        }
        if (data.storageId != null) {
            builder.setStorageId(data.storageId);
        }
        if (data.protocol != null) {
            builder.setProtocol(data.protocol);
        }
        if (data.requestId != null) {
            builder.setRequestId(data.requestId.toString());
        }
        if (data.storageSize != null) {
            builder.setStorageSize(data.storageSize);
        }
        if (data.timestamp != null) {
            builder.setTimestamp(data.timestamp);
        }
        if (data.type != null) {
            builder.setType(data.type);
        }

        if (data.keys != null) {
            data.keys.stream()
                    .map(ByteString::copyFrom)
                    .forEach(builder::addKeys);
        }
        if (data.values != null) {
            data.values.stream()
                    .map(ByteString::copyFrom)
                    .forEach(builder::addValues);
        }
        if (data.activeEndpointIds != null) {
            data.activeEndpointIds.stream()
                    .map(UUID::toString)
                    .forEach(builder::addActiveEndpointIds);
        }
        if (data.embeddedMessages != null) {
            var mapper = Mapper.create(this::encode);
            data.embeddedMessages.stream()
                    .map(m -> mapper.map(m))
                    .filter(Objects::nonNull)
                    .forEach(builder::addEmbeddedMessages);
        }

        if (data.success != null) {
            builder.setSuccess(data.success);
        }
        if (data.executeSync != null) {
            builder.setExecuteSync(data.executeSync);
        }

        if (data.raftLeaderId != null) {
            builder.setRaftLeaderId(data.raftLeaderId.toString());
        }
        if (data.raftNumberOfLogs != null) {
            builder.setRaftNumberOfLogs(data.raftNumberOfLogs);
        }
        if (data.raftLastAppliedIndex != null) {
            builder.setRaftLastAppliedIndex(data.raftLastAppliedIndex);
        }
        if (data.raftCommitIndex != null) {
            builder.setRaftCommitIndex(data.raftCommitIndex);
        }
        if (data.raftLeaderNextIndex != null) {
            builder.setRaftLeaderNextIndex(data.raftLeaderNextIndex);
        }
        if (data.raftPrevLogTerm != null) {
            builder.setRaftPrevLogTerm(data.raftPrevLogTerm);
        }
        if (data.raftPrevLogIndex != null) {
            builder.setRaftPrevLogIndex(data.raftPrevLogIndex);
        }
        if (data.raftTerm != null) {
            builder.setRaftTerm(data.raftTerm);
        }
        if (data.raftPeerNextIndex != null) {
            builder.setRaftPeerNextIndex(data.raftPeerNextIndex);
        }
        if (data.raftCandidateId != null) {
            builder.setRaftCandidateId(data.raftCandidateId.toString());
        }
        if (data.sequence != null) {
            builder.setSequence(data.sequence);
        }
        if (data.lastMessage != null) {
            builder.setLastMessage(data.lastMessage);
        }
        return builder.build();
    }
}
