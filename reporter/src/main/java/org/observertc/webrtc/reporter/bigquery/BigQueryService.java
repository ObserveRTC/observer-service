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

package org.observertc.webrtc.reporter.bigquery;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;

public class BigQueryService {
	private final String projectName;
	private final String datasetName;
	private BigQuery bigQuery;

	public BigQueryService(String projectName, String datasetName) {
		this.bigQuery = BigQueryOptions.getDefaultInstance().getService();
		this.projectName = projectName;
		this.datasetName = datasetName;

	}

	public BigQuery getBigQuery() {
		return this.bigQuery;
	}

	public String getProjectName() {
		return this.projectName;
	}

	public String getDatasetName() {
		return this.datasetName;
	}

}
