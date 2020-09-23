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

import com.google.cloud.bigquery.Dataset;
import com.google.cloud.bigquery.DatasetId;
import com.google.cloud.bigquery.DatasetInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BigQueryDataset {
	private static final Logger logger = LoggerFactory.getLogger(BigQueryDataset.class);

	private final BigQueryService bigQueryService;
	private final DatasetId datasetId;

	public BigQueryDataset(BigQueryService bigQueryService) {
		this.bigQueryService = bigQueryService;
		String projectName = this.bigQueryService.getProjectName();
		String datasetName = this.bigQueryService.getDatasetName();
		DatasetInfo datasetInfo = DatasetInfo.newBuilder(projectName, datasetName).build();
		DatasetId datasetId = datasetInfo.getDatasetId();
		this.datasetId = datasetId;
	}

	public Dataset getDataset() {
		return this.bigQueryService.getBigQuery().getDataset(datasetId);
	}

}