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

package org.observertc.webrtc.observer.repositories.mappers;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.inject.Singleton;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.RecordMapperProvider;
import org.jooq.RecordType;
import org.jooq.impl.DefaultRecordMapper;

@Singleton
public class RecordMapperProviderImpl implements RecordMapperProvider {

	private Map<Class, RecordMapper> mappers = new HashMap<>();

	public RecordMapperProviderImpl() {

//		this.mappers.put(ObserverDTO.class, new ObserversRecordMapper());
	}

	@Override
	public <R extends Record, E> RecordMapper<R, E> provide(RecordType<R> recordType, Class<? extends E> type) {
		RecordMapper<R, E> result = this.mappers.get(type);
		if (Objects.isNull(result)) {
			result = new DefaultRecordMapper<>(recordType, type);
			this.mappers.put(type, result);
		}
		return result;
	}
}
