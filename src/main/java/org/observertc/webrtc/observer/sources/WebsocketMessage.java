///*
// * Copyright  2020 Balazs Kreith
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package org.observertc.webrtc.observer.sources;
//
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//import com.fasterxml.jackson.annotation.JsonProperty;
//
//import java.util.Locale;
//import java.util.Objects;
//
///**
// * A compound object holds a set of measurements belonging to a aspecific time
// */
//@JsonIgnoreProperties(ignoreUnknown = true)
//public class WebsocketMessage {
//
//	public enum Type {
//		CLIENT_SAMPLE,
//		SFU_SAMPLE,
//		;
//
//		public static Type tryParse(String input) {
//			if (Objects.isNull(input)) {
//				return null;
//			}
//			try {
//				return Type.valueOf(input.toUpperCase(Locale.ROOT));
//			} catch (Exception ex) {
//				return null;
//			}
//		}
//	}
//
//	@JsonProperty("requestId")
//	public String requestId;
//
//	@JsonProperty("type")
//	public String type;
//
//	@JsonProperty("payload")
//	public byte[] payload;
//}
