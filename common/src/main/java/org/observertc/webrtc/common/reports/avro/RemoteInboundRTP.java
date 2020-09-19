/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package org.observertc.webrtc.common.reports.avro;

import org.apache.avro.generic.GenericArray;
import org.apache.avro.specific.SpecificData;
import org.apache.avro.util.Utf8;
import org.apache.avro.message.BinaryMessageEncoder;
import org.apache.avro.message.BinaryMessageDecoder;
import org.apache.avro.message.SchemaStore;

@org.apache.avro.specific.AvroGenerated
public class RemoteInboundRTP extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  private static final long serialVersionUID = -4119309954517992199L;
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"RemoteInboundRTP\",\"namespace\":\"org.observertc.webrtc.common.reports.avro\",\"fields\":[{\"name\":\"peerConnectionUUID\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"mediaUnit\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"codecID\",\"type\":[\"null\",{\"type\":\"string\",\"avro.java.string\":\"String\"}],\"default\":null},{\"name\":\"id\",\"type\":[\"null\",{\"type\":\"string\",\"avro.java.string\":\"String\"}],\"default\":null},{\"name\":\"jitter\",\"type\":[\"null\",\"float\"],\"default\":null},{\"name\":\"localID\",\"type\":[\"null\",{\"type\":\"string\",\"avro.java.string\":\"String\"}],\"default\":null},{\"name\":\"mediaType\",\"type\":{\"type\":\"enum\",\"name\":\"MediaType\",\"symbols\":[\"AUDIO\",\"VIDEO\",\"UNKNOWN\"]},\"default\":\"UNKNOWN\"},{\"name\":\"packetsLost\",\"type\":[\"null\",\"int\"],\"default\":null},{\"name\":\"roundTripTime\",\"type\":[\"null\",\"double\"],\"default\":null},{\"name\":\"ssrc\",\"type\":\"long\"},{\"name\":\"transportID\",\"type\":[\"null\",{\"type\":\"string\",\"avro.java.string\":\"String\"}],\"default\":null}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }

  private static SpecificData MODEL$ = new SpecificData();

  private static final BinaryMessageEncoder<RemoteInboundRTP> ENCODER =
      new BinaryMessageEncoder<RemoteInboundRTP>(MODEL$, SCHEMA$);

  private static final BinaryMessageDecoder<RemoteInboundRTP> DECODER =
      new BinaryMessageDecoder<RemoteInboundRTP>(MODEL$, SCHEMA$);

  /**
   * Return the BinaryMessageEncoder instance used by this class.
   * @return the message encoder used by this class
   */
  public static BinaryMessageEncoder<RemoteInboundRTP> getEncoder() {
    return ENCODER;
  }

  /**
   * Return the BinaryMessageDecoder instance used by this class.
   * @return the message decoder used by this class
   */
  public static BinaryMessageDecoder<RemoteInboundRTP> getDecoder() {
    return DECODER;
  }

  /**
   * Create a new BinaryMessageDecoder instance for this class that uses the specified {@link SchemaStore}.
   * @param resolver a {@link SchemaStore} used to find schemas by fingerprint
   * @return a BinaryMessageDecoder instance for this class backed by the given SchemaStore
   */
  public static BinaryMessageDecoder<RemoteInboundRTP> createDecoder(SchemaStore resolver) {
    return new BinaryMessageDecoder<RemoteInboundRTP>(MODEL$, SCHEMA$, resolver);
  }

  /**
   * Serializes this RemoteInboundRTP to a ByteBuffer.
   * @return a buffer holding the serialized data for this instance
   * @throws java.io.IOException if this instance could not be serialized
   */
  public java.nio.ByteBuffer toByteBuffer() throws java.io.IOException {
    return ENCODER.encode(this);
  }

  /**
   * Deserializes a RemoteInboundRTP from a ByteBuffer.
   * @param b a byte buffer holding serialized data for an instance of this class
   * @return a RemoteInboundRTP instance decoded from the given buffer
   * @throws java.io.IOException if the given bytes could not be deserialized into an instance of this class
   */
  public static RemoteInboundRTP fromByteBuffer(
      java.nio.ByteBuffer b) throws java.io.IOException {
    return DECODER.decode(b);
  }

   private java.lang.String peerConnectionUUID;
   private java.lang.String mediaUnit;
   private java.lang.String codecID;
   private java.lang.String id;
   private java.lang.Float jitter;
   private java.lang.String localID;
   private org.observertc.webrtc.common.reports.avro.MediaType mediaType;
   private java.lang.Integer packetsLost;
   private java.lang.Double roundTripTime;
   private long ssrc;
   private java.lang.String transportID;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>.
   */
  public RemoteInboundRTP() {}

  /**
   * All-args constructor.
   * @param peerConnectionUUID The new value for peerConnectionUUID
   * @param mediaUnit The new value for mediaUnit
   * @param codecID The new value for codecID
   * @param id The new value for id
   * @param jitter The new value for jitter
   * @param localID The new value for localID
   * @param mediaType The new value for mediaType
   * @param packetsLost The new value for packetsLost
   * @param roundTripTime The new value for roundTripTime
   * @param ssrc The new value for ssrc
   * @param transportID The new value for transportID
   */
  public RemoteInboundRTP(java.lang.String peerConnectionUUID, java.lang.String mediaUnit, java.lang.String codecID, java.lang.String id, java.lang.Float jitter, java.lang.String localID, org.observertc.webrtc.common.reports.avro.MediaType mediaType, java.lang.Integer packetsLost, java.lang.Double roundTripTime, java.lang.Long ssrc, java.lang.String transportID) {
    this.peerConnectionUUID = peerConnectionUUID;
    this.mediaUnit = mediaUnit;
    this.codecID = codecID;
    this.id = id;
    this.jitter = jitter;
    this.localID = localID;
    this.mediaType = mediaType;
    this.packetsLost = packetsLost;
    this.roundTripTime = roundTripTime;
    this.ssrc = ssrc;
    this.transportID = transportID;
  }

  public org.apache.avro.specific.SpecificData getSpecificData() { return MODEL$; }
  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call.
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return peerConnectionUUID;
    case 1: return mediaUnit;
    case 2: return codecID;
    case 3: return id;
    case 4: return jitter;
    case 5: return localID;
    case 6: return mediaType;
    case 7: return packetsLost;
    case 8: return roundTripTime;
    case 9: return ssrc;
    case 10: return transportID;
    default: throw new IndexOutOfBoundsException("Invalid index: " + field$);
    }
  }

  // Used by DatumReader.  Applications should not call.
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: peerConnectionUUID = value$ != null ? value$.toString() : null; break;
    case 1: mediaUnit = value$ != null ? value$.toString() : null; break;
    case 2: codecID = value$ != null ? value$.toString() : null; break;
    case 3: id = value$ != null ? value$.toString() : null; break;
    case 4: jitter = (java.lang.Float)value$; break;
    case 5: localID = value$ != null ? value$.toString() : null; break;
    case 6: mediaType = (org.observertc.webrtc.common.reports.avro.MediaType)value$; break;
    case 7: packetsLost = (java.lang.Integer)value$; break;
    case 8: roundTripTime = (java.lang.Double)value$; break;
    case 9: ssrc = (java.lang.Long)value$; break;
    case 10: transportID = value$ != null ? value$.toString() : null; break;
    default: throw new IndexOutOfBoundsException("Invalid index: " + field$);
    }
  }

  /**
   * Gets the value of the 'peerConnectionUUID' field.
   * @return The value of the 'peerConnectionUUID' field.
   */
  public java.lang.String getPeerConnectionUUID() {
    return peerConnectionUUID;
  }



  /**
   * Gets the value of the 'mediaUnit' field.
   * @return The value of the 'mediaUnit' field.
   */
  public java.lang.String getMediaUnit() {
    return mediaUnit;
  }



  /**
   * Gets the value of the 'codecID' field.
   * @return The value of the 'codecID' field.
   */
  public java.lang.String getCodecID() {
    return codecID;
  }



  /**
   * Gets the value of the 'id' field.
   * @return The value of the 'id' field.
   */
  public java.lang.String getId() {
    return id;
  }



  /**
   * Gets the value of the 'jitter' field.
   * @return The value of the 'jitter' field.
   */
  public java.lang.Float getJitter() {
    return jitter;
  }



  /**
   * Gets the value of the 'localID' field.
   * @return The value of the 'localID' field.
   */
  public java.lang.String getLocalID() {
    return localID;
  }



  /**
   * Gets the value of the 'mediaType' field.
   * @return The value of the 'mediaType' field.
   */
  public org.observertc.webrtc.common.reports.avro.MediaType getMediaType() {
    return mediaType;
  }



  /**
   * Gets the value of the 'packetsLost' field.
   * @return The value of the 'packetsLost' field.
   */
  public java.lang.Integer getPacketsLost() {
    return packetsLost;
  }



  /**
   * Gets the value of the 'roundTripTime' field.
   * @return The value of the 'roundTripTime' field.
   */
  public java.lang.Double getRoundTripTime() {
    return roundTripTime;
  }



  /**
   * Gets the value of the 'ssrc' field.
   * @return The value of the 'ssrc' field.
   */
  public long getSsrc() {
    return ssrc;
  }



  /**
   * Gets the value of the 'transportID' field.
   * @return The value of the 'transportID' field.
   */
  public java.lang.String getTransportID() {
    return transportID;
  }



  /**
   * Creates a new RemoteInboundRTP RecordBuilder.
   * @return A new RemoteInboundRTP RecordBuilder
   */
  public static org.observertc.webrtc.common.reports.avro.RemoteInboundRTP.Builder newBuilder() {
    return new org.observertc.webrtc.common.reports.avro.RemoteInboundRTP.Builder();
  }

  /**
   * Creates a new RemoteInboundRTP RecordBuilder by copying an existing Builder.
   * @param other The existing builder to copy.
   * @return A new RemoteInboundRTP RecordBuilder
   */
  public static org.observertc.webrtc.common.reports.avro.RemoteInboundRTP.Builder newBuilder(org.observertc.webrtc.common.reports.avro.RemoteInboundRTP.Builder other) {
    if (other == null) {
      return new org.observertc.webrtc.common.reports.avro.RemoteInboundRTP.Builder();
    } else {
      return new org.observertc.webrtc.common.reports.avro.RemoteInboundRTP.Builder(other);
    }
  }

  /**
   * Creates a new RemoteInboundRTP RecordBuilder by copying an existing RemoteInboundRTP instance.
   * @param other The existing instance to copy.
   * @return A new RemoteInboundRTP RecordBuilder
   */
  public static org.observertc.webrtc.common.reports.avro.RemoteInboundRTP.Builder newBuilder(org.observertc.webrtc.common.reports.avro.RemoteInboundRTP other) {
    if (other == null) {
      return new org.observertc.webrtc.common.reports.avro.RemoteInboundRTP.Builder();
    } else {
      return new org.observertc.webrtc.common.reports.avro.RemoteInboundRTP.Builder(other);
    }
  }

  /**
   * RecordBuilder for RemoteInboundRTP instances.
   */
  @org.apache.avro.specific.AvroGenerated
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<RemoteInboundRTP>
    implements org.apache.avro.data.RecordBuilder<RemoteInboundRTP> {

    private java.lang.String peerConnectionUUID;
    private java.lang.String mediaUnit;
    private java.lang.String codecID;
    private java.lang.String id;
    private java.lang.Float jitter;
    private java.lang.String localID;
    private org.observertc.webrtc.common.reports.avro.MediaType mediaType;
    private java.lang.Integer packetsLost;
    private java.lang.Double roundTripTime;
    private long ssrc;
    private java.lang.String transportID;

    /** Creates a new Builder */
    private Builder() {
      super(SCHEMA$);
    }

    /**
     * Creates a Builder by copying an existing Builder.
     * @param other The existing Builder to copy.
     */
    private Builder(org.observertc.webrtc.common.reports.avro.RemoteInboundRTP.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.peerConnectionUUID)) {
        this.peerConnectionUUID = data().deepCopy(fields()[0].schema(), other.peerConnectionUUID);
        fieldSetFlags()[0] = other.fieldSetFlags()[0];
      }
      if (isValidValue(fields()[1], other.mediaUnit)) {
        this.mediaUnit = data().deepCopy(fields()[1].schema(), other.mediaUnit);
        fieldSetFlags()[1] = other.fieldSetFlags()[1];
      }
      if (isValidValue(fields()[2], other.codecID)) {
        this.codecID = data().deepCopy(fields()[2].schema(), other.codecID);
        fieldSetFlags()[2] = other.fieldSetFlags()[2];
      }
      if (isValidValue(fields()[3], other.id)) {
        this.id = data().deepCopy(fields()[3].schema(), other.id);
        fieldSetFlags()[3] = other.fieldSetFlags()[3];
      }
      if (isValidValue(fields()[4], other.jitter)) {
        this.jitter = data().deepCopy(fields()[4].schema(), other.jitter);
        fieldSetFlags()[4] = other.fieldSetFlags()[4];
      }
      if (isValidValue(fields()[5], other.localID)) {
        this.localID = data().deepCopy(fields()[5].schema(), other.localID);
        fieldSetFlags()[5] = other.fieldSetFlags()[5];
      }
      if (isValidValue(fields()[6], other.mediaType)) {
        this.mediaType = data().deepCopy(fields()[6].schema(), other.mediaType);
        fieldSetFlags()[6] = other.fieldSetFlags()[6];
      }
      if (isValidValue(fields()[7], other.packetsLost)) {
        this.packetsLost = data().deepCopy(fields()[7].schema(), other.packetsLost);
        fieldSetFlags()[7] = other.fieldSetFlags()[7];
      }
      if (isValidValue(fields()[8], other.roundTripTime)) {
        this.roundTripTime = data().deepCopy(fields()[8].schema(), other.roundTripTime);
        fieldSetFlags()[8] = other.fieldSetFlags()[8];
      }
      if (isValidValue(fields()[9], other.ssrc)) {
        this.ssrc = data().deepCopy(fields()[9].schema(), other.ssrc);
        fieldSetFlags()[9] = other.fieldSetFlags()[9];
      }
      if (isValidValue(fields()[10], other.transportID)) {
        this.transportID = data().deepCopy(fields()[10].schema(), other.transportID);
        fieldSetFlags()[10] = other.fieldSetFlags()[10];
      }
    }

    /**
     * Creates a Builder by copying an existing RemoteInboundRTP instance
     * @param other The existing instance to copy.
     */
    private Builder(org.observertc.webrtc.common.reports.avro.RemoteInboundRTP other) {
      super(SCHEMA$);
      if (isValidValue(fields()[0], other.peerConnectionUUID)) {
        this.peerConnectionUUID = data().deepCopy(fields()[0].schema(), other.peerConnectionUUID);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.mediaUnit)) {
        this.mediaUnit = data().deepCopy(fields()[1].schema(), other.mediaUnit);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.codecID)) {
        this.codecID = data().deepCopy(fields()[2].schema(), other.codecID);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.id)) {
        this.id = data().deepCopy(fields()[3].schema(), other.id);
        fieldSetFlags()[3] = true;
      }
      if (isValidValue(fields()[4], other.jitter)) {
        this.jitter = data().deepCopy(fields()[4].schema(), other.jitter);
        fieldSetFlags()[4] = true;
      }
      if (isValidValue(fields()[5], other.localID)) {
        this.localID = data().deepCopy(fields()[5].schema(), other.localID);
        fieldSetFlags()[5] = true;
      }
      if (isValidValue(fields()[6], other.mediaType)) {
        this.mediaType = data().deepCopy(fields()[6].schema(), other.mediaType);
        fieldSetFlags()[6] = true;
      }
      if (isValidValue(fields()[7], other.packetsLost)) {
        this.packetsLost = data().deepCopy(fields()[7].schema(), other.packetsLost);
        fieldSetFlags()[7] = true;
      }
      if (isValidValue(fields()[8], other.roundTripTime)) {
        this.roundTripTime = data().deepCopy(fields()[8].schema(), other.roundTripTime);
        fieldSetFlags()[8] = true;
      }
      if (isValidValue(fields()[9], other.ssrc)) {
        this.ssrc = data().deepCopy(fields()[9].schema(), other.ssrc);
        fieldSetFlags()[9] = true;
      }
      if (isValidValue(fields()[10], other.transportID)) {
        this.transportID = data().deepCopy(fields()[10].schema(), other.transportID);
        fieldSetFlags()[10] = true;
      }
    }

    /**
      * Gets the value of the 'peerConnectionUUID' field.
      * @return The value.
      */
    public java.lang.String getPeerConnectionUUID() {
      return peerConnectionUUID;
    }


    /**
      * Sets the value of the 'peerConnectionUUID' field.
      * @param value The value of 'peerConnectionUUID'.
      * @return This builder.
      */
    public org.observertc.webrtc.common.reports.avro.RemoteInboundRTP.Builder setPeerConnectionUUID(java.lang.String value) {
      validate(fields()[0], value);
      this.peerConnectionUUID = value;
      fieldSetFlags()[0] = true;
      return this;
    }

    /**
      * Checks whether the 'peerConnectionUUID' field has been set.
      * @return True if the 'peerConnectionUUID' field has been set, false otherwise.
      */
    public boolean hasPeerConnectionUUID() {
      return fieldSetFlags()[0];
    }


    /**
      * Clears the value of the 'peerConnectionUUID' field.
      * @return This builder.
      */
    public org.observertc.webrtc.common.reports.avro.RemoteInboundRTP.Builder clearPeerConnectionUUID() {
      peerConnectionUUID = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /**
      * Gets the value of the 'mediaUnit' field.
      * @return The value.
      */
    public java.lang.String getMediaUnit() {
      return mediaUnit;
    }


    /**
      * Sets the value of the 'mediaUnit' field.
      * @param value The value of 'mediaUnit'.
      * @return This builder.
      */
    public org.observertc.webrtc.common.reports.avro.RemoteInboundRTP.Builder setMediaUnit(java.lang.String value) {
      validate(fields()[1], value);
      this.mediaUnit = value;
      fieldSetFlags()[1] = true;
      return this;
    }

    /**
      * Checks whether the 'mediaUnit' field has been set.
      * @return True if the 'mediaUnit' field has been set, false otherwise.
      */
    public boolean hasMediaUnit() {
      return fieldSetFlags()[1];
    }


    /**
      * Clears the value of the 'mediaUnit' field.
      * @return This builder.
      */
    public org.observertc.webrtc.common.reports.avro.RemoteInboundRTP.Builder clearMediaUnit() {
      mediaUnit = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    /**
      * Gets the value of the 'codecID' field.
      * @return The value.
      */
    public java.lang.String getCodecID() {
      return codecID;
    }


    /**
      * Sets the value of the 'codecID' field.
      * @param value The value of 'codecID'.
      * @return This builder.
      */
    public org.observertc.webrtc.common.reports.avro.RemoteInboundRTP.Builder setCodecID(java.lang.String value) {
      validate(fields()[2], value);
      this.codecID = value;
      fieldSetFlags()[2] = true;
      return this;
    }

    /**
      * Checks whether the 'codecID' field has been set.
      * @return True if the 'codecID' field has been set, false otherwise.
      */
    public boolean hasCodecID() {
      return fieldSetFlags()[2];
    }


    /**
      * Clears the value of the 'codecID' field.
      * @return This builder.
      */
    public org.observertc.webrtc.common.reports.avro.RemoteInboundRTP.Builder clearCodecID() {
      codecID = null;
      fieldSetFlags()[2] = false;
      return this;
    }

    /**
      * Gets the value of the 'id' field.
      * @return The value.
      */
    public java.lang.String getId() {
      return id;
    }


    /**
      * Sets the value of the 'id' field.
      * @param value The value of 'id'.
      * @return This builder.
      */
    public org.observertc.webrtc.common.reports.avro.RemoteInboundRTP.Builder setId(java.lang.String value) {
      validate(fields()[3], value);
      this.id = value;
      fieldSetFlags()[3] = true;
      return this;
    }

    /**
      * Checks whether the 'id' field has been set.
      * @return True if the 'id' field has been set, false otherwise.
      */
    public boolean hasId() {
      return fieldSetFlags()[3];
    }


    /**
      * Clears the value of the 'id' field.
      * @return This builder.
      */
    public org.observertc.webrtc.common.reports.avro.RemoteInboundRTP.Builder clearId() {
      id = null;
      fieldSetFlags()[3] = false;
      return this;
    }

    /**
      * Gets the value of the 'jitter' field.
      * @return The value.
      */
    public java.lang.Float getJitter() {
      return jitter;
    }


    /**
      * Sets the value of the 'jitter' field.
      * @param value The value of 'jitter'.
      * @return This builder.
      */
    public org.observertc.webrtc.common.reports.avro.RemoteInboundRTP.Builder setJitter(java.lang.Float value) {
      validate(fields()[4], value);
      this.jitter = value;
      fieldSetFlags()[4] = true;
      return this;
    }

    /**
      * Checks whether the 'jitter' field has been set.
      * @return True if the 'jitter' field has been set, false otherwise.
      */
    public boolean hasJitter() {
      return fieldSetFlags()[4];
    }


    /**
      * Clears the value of the 'jitter' field.
      * @return This builder.
      */
    public org.observertc.webrtc.common.reports.avro.RemoteInboundRTP.Builder clearJitter() {
      jitter = null;
      fieldSetFlags()[4] = false;
      return this;
    }

    /**
      * Gets the value of the 'localID' field.
      * @return The value.
      */
    public java.lang.String getLocalID() {
      return localID;
    }


    /**
      * Sets the value of the 'localID' field.
      * @param value The value of 'localID'.
      * @return This builder.
      */
    public org.observertc.webrtc.common.reports.avro.RemoteInboundRTP.Builder setLocalID(java.lang.String value) {
      validate(fields()[5], value);
      this.localID = value;
      fieldSetFlags()[5] = true;
      return this;
    }

    /**
      * Checks whether the 'localID' field has been set.
      * @return True if the 'localID' field has been set, false otherwise.
      */
    public boolean hasLocalID() {
      return fieldSetFlags()[5];
    }


    /**
      * Clears the value of the 'localID' field.
      * @return This builder.
      */
    public org.observertc.webrtc.common.reports.avro.RemoteInboundRTP.Builder clearLocalID() {
      localID = null;
      fieldSetFlags()[5] = false;
      return this;
    }

    /**
      * Gets the value of the 'mediaType' field.
      * @return The value.
      */
    public org.observertc.webrtc.common.reports.avro.MediaType getMediaType() {
      return mediaType;
    }


    /**
      * Sets the value of the 'mediaType' field.
      * @param value The value of 'mediaType'.
      * @return This builder.
      */
    public org.observertc.webrtc.common.reports.avro.RemoteInboundRTP.Builder setMediaType(org.observertc.webrtc.common.reports.avro.MediaType value) {
      validate(fields()[6], value);
      this.mediaType = value;
      fieldSetFlags()[6] = true;
      return this;
    }

    /**
      * Checks whether the 'mediaType' field has been set.
      * @return True if the 'mediaType' field has been set, false otherwise.
      */
    public boolean hasMediaType() {
      return fieldSetFlags()[6];
    }


    /**
      * Clears the value of the 'mediaType' field.
      * @return This builder.
      */
    public org.observertc.webrtc.common.reports.avro.RemoteInboundRTP.Builder clearMediaType() {
      mediaType = null;
      fieldSetFlags()[6] = false;
      return this;
    }

    /**
      * Gets the value of the 'packetsLost' field.
      * @return The value.
      */
    public java.lang.Integer getPacketsLost() {
      return packetsLost;
    }


    /**
      * Sets the value of the 'packetsLost' field.
      * @param value The value of 'packetsLost'.
      * @return This builder.
      */
    public org.observertc.webrtc.common.reports.avro.RemoteInboundRTP.Builder setPacketsLost(java.lang.Integer value) {
      validate(fields()[7], value);
      this.packetsLost = value;
      fieldSetFlags()[7] = true;
      return this;
    }

    /**
      * Checks whether the 'packetsLost' field has been set.
      * @return True if the 'packetsLost' field has been set, false otherwise.
      */
    public boolean hasPacketsLost() {
      return fieldSetFlags()[7];
    }


    /**
      * Clears the value of the 'packetsLost' field.
      * @return This builder.
      */
    public org.observertc.webrtc.common.reports.avro.RemoteInboundRTP.Builder clearPacketsLost() {
      packetsLost = null;
      fieldSetFlags()[7] = false;
      return this;
    }

    /**
      * Gets the value of the 'roundTripTime' field.
      * @return The value.
      */
    public java.lang.Double getRoundTripTime() {
      return roundTripTime;
    }


    /**
      * Sets the value of the 'roundTripTime' field.
      * @param value The value of 'roundTripTime'.
      * @return This builder.
      */
    public org.observertc.webrtc.common.reports.avro.RemoteInboundRTP.Builder setRoundTripTime(java.lang.Double value) {
      validate(fields()[8], value);
      this.roundTripTime = value;
      fieldSetFlags()[8] = true;
      return this;
    }

    /**
      * Checks whether the 'roundTripTime' field has been set.
      * @return True if the 'roundTripTime' field has been set, false otherwise.
      */
    public boolean hasRoundTripTime() {
      return fieldSetFlags()[8];
    }


    /**
      * Clears the value of the 'roundTripTime' field.
      * @return This builder.
      */
    public org.observertc.webrtc.common.reports.avro.RemoteInboundRTP.Builder clearRoundTripTime() {
      roundTripTime = null;
      fieldSetFlags()[8] = false;
      return this;
    }

    /**
      * Gets the value of the 'ssrc' field.
      * @return The value.
      */
    public long getSsrc() {
      return ssrc;
    }


    /**
      * Sets the value of the 'ssrc' field.
      * @param value The value of 'ssrc'.
      * @return This builder.
      */
    public org.observertc.webrtc.common.reports.avro.RemoteInboundRTP.Builder setSsrc(long value) {
      validate(fields()[9], value);
      this.ssrc = value;
      fieldSetFlags()[9] = true;
      return this;
    }

    /**
      * Checks whether the 'ssrc' field has been set.
      * @return True if the 'ssrc' field has been set, false otherwise.
      */
    public boolean hasSsrc() {
      return fieldSetFlags()[9];
    }


    /**
      * Clears the value of the 'ssrc' field.
      * @return This builder.
      */
    public org.observertc.webrtc.common.reports.avro.RemoteInboundRTP.Builder clearSsrc() {
      fieldSetFlags()[9] = false;
      return this;
    }

    /**
      * Gets the value of the 'transportID' field.
      * @return The value.
      */
    public java.lang.String getTransportID() {
      return transportID;
    }


    /**
      * Sets the value of the 'transportID' field.
      * @param value The value of 'transportID'.
      * @return This builder.
      */
    public org.observertc.webrtc.common.reports.avro.RemoteInboundRTP.Builder setTransportID(java.lang.String value) {
      validate(fields()[10], value);
      this.transportID = value;
      fieldSetFlags()[10] = true;
      return this;
    }

    /**
      * Checks whether the 'transportID' field has been set.
      * @return True if the 'transportID' field has been set, false otherwise.
      */
    public boolean hasTransportID() {
      return fieldSetFlags()[10];
    }


    /**
      * Clears the value of the 'transportID' field.
      * @return This builder.
      */
    public org.observertc.webrtc.common.reports.avro.RemoteInboundRTP.Builder clearTransportID() {
      transportID = null;
      fieldSetFlags()[10] = false;
      return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public RemoteInboundRTP build() {
      try {
        RemoteInboundRTP record = new RemoteInboundRTP();
        record.peerConnectionUUID = fieldSetFlags()[0] ? this.peerConnectionUUID : (java.lang.String) defaultValue(fields()[0]);
        record.mediaUnit = fieldSetFlags()[1] ? this.mediaUnit : (java.lang.String) defaultValue(fields()[1]);
        record.codecID = fieldSetFlags()[2] ? this.codecID : (java.lang.String) defaultValue(fields()[2]);
        record.id = fieldSetFlags()[3] ? this.id : (java.lang.String) defaultValue(fields()[3]);
        record.jitter = fieldSetFlags()[4] ? this.jitter : (java.lang.Float) defaultValue(fields()[4]);
        record.localID = fieldSetFlags()[5] ? this.localID : (java.lang.String) defaultValue(fields()[5]);
        record.mediaType = fieldSetFlags()[6] ? this.mediaType : (org.observertc.webrtc.common.reports.avro.MediaType) defaultValue(fields()[6]);
        record.packetsLost = fieldSetFlags()[7] ? this.packetsLost : (java.lang.Integer) defaultValue(fields()[7]);
        record.roundTripTime = fieldSetFlags()[8] ? this.roundTripTime : (java.lang.Double) defaultValue(fields()[8]);
        record.ssrc = fieldSetFlags()[9] ? this.ssrc : (java.lang.Long) defaultValue(fields()[9]);
        record.transportID = fieldSetFlags()[10] ? this.transportID : (java.lang.String) defaultValue(fields()[10]);
        return record;
      } catch (org.apache.avro.AvroMissingFieldException e) {
        throw e;
      } catch (java.lang.Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumWriter<RemoteInboundRTP>
    WRITER$ = (org.apache.avro.io.DatumWriter<RemoteInboundRTP>)MODEL$.createDatumWriter(SCHEMA$);

  @Override public void writeExternal(java.io.ObjectOutput out)
    throws java.io.IOException {
    WRITER$.write(this, SpecificData.getEncoder(out));
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumReader<RemoteInboundRTP>
    READER$ = (org.apache.avro.io.DatumReader<RemoteInboundRTP>)MODEL$.createDatumReader(SCHEMA$);

  @Override public void readExternal(java.io.ObjectInput in)
    throws java.io.IOException {
    READER$.read(this, SpecificData.getDecoder(in));
  }

  @Override protected boolean hasCustomCoders() { return true; }

  @Override public void customEncode(org.apache.avro.io.Encoder out)
    throws java.io.IOException
  {
    out.writeString(this.peerConnectionUUID);

    out.writeString(this.mediaUnit);

    if (this.codecID == null) {
      out.writeIndex(0);
      out.writeNull();
    } else {
      out.writeIndex(1);
      out.writeString(this.codecID);
    }

    if (this.id == null) {
      out.writeIndex(0);
      out.writeNull();
    } else {
      out.writeIndex(1);
      out.writeString(this.id);
    }

    if (this.jitter == null) {
      out.writeIndex(0);
      out.writeNull();
    } else {
      out.writeIndex(1);
      out.writeFloat(this.jitter);
    }

    if (this.localID == null) {
      out.writeIndex(0);
      out.writeNull();
    } else {
      out.writeIndex(1);
      out.writeString(this.localID);
    }

    out.writeEnum(this.mediaType.ordinal());

    if (this.packetsLost == null) {
      out.writeIndex(0);
      out.writeNull();
    } else {
      out.writeIndex(1);
      out.writeInt(this.packetsLost);
    }

    if (this.roundTripTime == null) {
      out.writeIndex(0);
      out.writeNull();
    } else {
      out.writeIndex(1);
      out.writeDouble(this.roundTripTime);
    }

    out.writeLong(this.ssrc);

    if (this.transportID == null) {
      out.writeIndex(0);
      out.writeNull();
    } else {
      out.writeIndex(1);
      out.writeString(this.transportID);
    }

  }

  @Override public void customDecode(org.apache.avro.io.ResolvingDecoder in)
    throws java.io.IOException
  {
    org.apache.avro.Schema.Field[] fieldOrder = in.readFieldOrderIfDiff();
    if (fieldOrder == null) {
      this.peerConnectionUUID = in.readString();

      this.mediaUnit = in.readString();

      if (in.readIndex() != 1) {
        in.readNull();
        this.codecID = null;
      } else {
        this.codecID = in.readString();
      }

      if (in.readIndex() != 1) {
        in.readNull();
        this.id = null;
      } else {
        this.id = in.readString();
      }

      if (in.readIndex() != 1) {
        in.readNull();
        this.jitter = null;
      } else {
        this.jitter = in.readFloat();
      }

      if (in.readIndex() != 1) {
        in.readNull();
        this.localID = null;
      } else {
        this.localID = in.readString();
      }

      this.mediaType = org.observertc.webrtc.common.reports.avro.MediaType.values()[in.readEnum()];

      if (in.readIndex() != 1) {
        in.readNull();
        this.packetsLost = null;
      } else {
        this.packetsLost = in.readInt();
      }

      if (in.readIndex() != 1) {
        in.readNull();
        this.roundTripTime = null;
      } else {
        this.roundTripTime = in.readDouble();
      }

      this.ssrc = in.readLong();

      if (in.readIndex() != 1) {
        in.readNull();
        this.transportID = null;
      } else {
        this.transportID = in.readString();
      }

    } else {
      for (int i = 0; i < 11; i++) {
        switch (fieldOrder[i].pos()) {
        case 0:
          this.peerConnectionUUID = in.readString();
          break;

        case 1:
          this.mediaUnit = in.readString();
          break;

        case 2:
          if (in.readIndex() != 1) {
            in.readNull();
            this.codecID = null;
          } else {
            this.codecID = in.readString();
          }
          break;

        case 3:
          if (in.readIndex() != 1) {
            in.readNull();
            this.id = null;
          } else {
            this.id = in.readString();
          }
          break;

        case 4:
          if (in.readIndex() != 1) {
            in.readNull();
            this.jitter = null;
          } else {
            this.jitter = in.readFloat();
          }
          break;

        case 5:
          if (in.readIndex() != 1) {
            in.readNull();
            this.localID = null;
          } else {
            this.localID = in.readString();
          }
          break;

        case 6:
          this.mediaType = org.observertc.webrtc.common.reports.avro.MediaType.values()[in.readEnum()];
          break;

        case 7:
          if (in.readIndex() != 1) {
            in.readNull();
            this.packetsLost = null;
          } else {
            this.packetsLost = in.readInt();
          }
          break;

        case 8:
          if (in.readIndex() != 1) {
            in.readNull();
            this.roundTripTime = null;
          } else {
            this.roundTripTime = in.readDouble();
          }
          break;

        case 9:
          this.ssrc = in.readLong();
          break;

        case 10:
          if (in.readIndex() != 1) {
            in.readNull();
            this.transportID = null;
          } else {
            this.transportID = in.readString();
          }
          break;

        default:
          throw new java.io.IOException("Corrupt ResolvingDecoder.");
        }
      }
    }
  }
}









