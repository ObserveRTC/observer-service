/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package org.observertc.webrtc.schemas.reports;
@org.apache.avro.specific.AvroGenerated
public enum NetworkType implements org.apache.avro.generic.GenericEnumSymbol<NetworkType> {
  BLUETOOTH, CELLULAR, ETHERNET, UNKNOWN, VPN, WIFI, WIMAX, NULL  ;
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"enum\",\"name\":\"NetworkType\",\"namespace\":\"org.observertc.webrtc.schemas.reports\",\"symbols\":[\"BLUETOOTH\",\"CELLULAR\",\"ETHERNET\",\"UNKNOWN\",\"VPN\",\"WIFI\",\"WIMAX\",\"NULL\"]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
}