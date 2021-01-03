/*
 * Copyright  2020 Balazs Kreith
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.observertc.webrtc.observer;

import io.reactivex.rxjava3.core.Observer;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.observertc.webrtc.schemas.reports.ReportType;

import java.util.UUID;
import java.util.concurrent.Future;

public interface ReportSink extends Observer<ReportRecord> {

	 Future<RecordMetadata> sendReport(UUID reportKey,
											 UUID serviceUUID,
											 String serviceName,
											 String marker,
											 ReportType type,
											 Long timestamp,
											 Object payload);

}
