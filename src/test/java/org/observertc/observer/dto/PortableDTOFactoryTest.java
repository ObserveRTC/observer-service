package org.observertc.observer.dto;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PortableDTOFactoryTest {

    private PortableDTOFactory factory = new PortableDTOFactory();

    @Test
    void shouldCreateCallDTO() {
        var classId = PortableDTOFactory.CALL_DTO_CLASS_ID;
        var dto = this.factory.create(classId);

        Assertions.assertEquals(PortableDTOFactory.FACTORY_ID, dto.getFactoryId());
        Assertions.assertEquals(classId, dto.getClassId());
        Assertions.assertTrue(dto instanceof CallDTO);
    }

    @Test
    void shouldCreateClientDTO() {
        var classId = PortableDTOFactory.CLIENT_DTO_CLASS_ID;
        var dto = this.factory.create(classId);

        Assertions.assertEquals(PortableDTOFactory.FACTORY_ID, dto.getFactoryId());
        Assertions.assertEquals(classId, dto.getClassId());
        Assertions.assertTrue(dto instanceof ClientDTO);
    }

    @Test
    void shouldCreateConfigDTO() {
        var classId = PortableDTOFactory.CONFIG_DTO_CLASS_ID;
        var dto = this.factory.create(classId);

        Assertions.assertEquals(PortableDTOFactory.FACTORY_ID, dto.getFactoryId());
        Assertions.assertEquals(classId, dto.getClassId());
        Assertions.assertTrue(dto instanceof ConfigDTO);
    }

    @Test
    void shouldCreateGenericEntryDTO() {
        var classId = PortableDTOFactory.GENERAL_ENTRY_DTO_CLASS_ID;
        var dto = this.factory.create(classId);

        Assertions.assertEquals(PortableDTOFactory.FACTORY_ID, dto.getFactoryId());
        Assertions.assertEquals(classId, dto.getClassId());
        Assertions.assertTrue(dto instanceof GeneralEntryDTO);
    }

    @Test
    void shouldCreateMediaTrackDTO() {
        var classId = PortableDTOFactory.MEDIA_TRACK_DTO_CLASS_ID;
        var dto = this.factory.create(classId);

        Assertions.assertEquals(PortableDTOFactory.FACTORY_ID, dto.getFactoryId());
        Assertions.assertEquals(classId, dto.getClassId());
        Assertions.assertTrue(dto instanceof MediaTrackDTO);
    }

    @Test
    void shouldCreatePeerConnectionDTO() {
        var classId = PortableDTOFactory.PEER_CONNECTION_DTO_CLASS_ID;
        var dto = this.factory.create(classId);

        Assertions.assertEquals(PortableDTOFactory.FACTORY_ID, dto.getFactoryId());
        Assertions.assertEquals(classId, dto.getClassId());
        Assertions.assertTrue(dto instanceof PeerConnectionDTO);
    }

    @Test
    void shouldCreateSfuDTO() {
        var classId = PortableDTOFactory.SFU_DTO_CLASS_ID;
        var dto = this.factory.create(classId);

        Assertions.assertEquals(PortableDTOFactory.FACTORY_ID, dto.getFactoryId());
        Assertions.assertEquals(classId, dto.getClassId());
        Assertions.assertTrue(dto instanceof SfuDTO);
    }

    @Test
    void shouldCreateSfuRtpPadDTO() {
        var classId = PortableDTOFactory.SFU_RTP_PAD_DTO_CLASS_ID;
        var dto = this.factory.create(classId);

        Assertions.assertEquals(PortableDTOFactory.FACTORY_ID, dto.getFactoryId());
        Assertions.assertEquals(classId, dto.getClassId());
        Assertions.assertTrue(dto instanceof SfuRtpPadDTO);
    }

    @Test
    void shouldCreateSfuSinkDTO() {
        var classId = PortableDTOFactory.SFU_SINK_DTO_CLASS_ID;
        var dto = this.factory.create(classId);

        Assertions.assertEquals(PortableDTOFactory.FACTORY_ID, dto.getFactoryId());
        Assertions.assertEquals(classId, dto.getClassId());
        Assertions.assertTrue(dto instanceof SfuSinkDTO);
    }

    @Test
    void shouldCreateSfuStreamDTO() {
        var classId = PortableDTOFactory.SFU_STREAM_DTO_CLASS_ID;
        var dto = this.factory.create(classId);

        Assertions.assertEquals(PortableDTOFactory.FACTORY_ID, dto.getFactoryId());
        Assertions.assertEquals(classId, dto.getClassId());
        Assertions.assertTrue(dto instanceof SfuStreamDTO);
    }

    @Test
    void shouldCreateSfuTransportDTO() {
        var classId = PortableDTOFactory.SFU_TRANSPORT_DTO_CLASS_ID;
        var dto = this.factory.create(classId);

        Assertions.assertEquals(PortableDTOFactory.FACTORY_ID, dto.getFactoryId());
        Assertions.assertEquals(classId, dto.getClassId());
        Assertions.assertTrue(dto instanceof SfuTransportDTO);
    }

    @Test
    void shouldCreateWeakLockDTO() {
        var classId = PortableDTOFactory.WEAKLOCKS_DTO_CLASS_ID;
        var dto = this.factory.create(classId);

        Assertions.assertEquals(PortableDTOFactory.FACTORY_ID, dto.getFactoryId());
        Assertions.assertEquals(classId, dto.getClassId());
        Assertions.assertTrue(dto instanceof WeakLockDTO);
    }
}