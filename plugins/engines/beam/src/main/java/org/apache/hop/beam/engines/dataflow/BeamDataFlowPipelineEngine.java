/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hop.beam.engines.dataflow;

import org.apache.hop.beam.engines.BeamPipelineEngine;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.execution.sampler.IExecutionDataSampler;
import org.apache.hop.execution.sampler.IExecutionDataSamplerStore;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.config.IPipelineEngineRunConfiguration;
import org.apache.hop.pipeline.engine.IPipelineEngine;
import org.apache.hop.pipeline.engine.PipelineEnginePlugin;

@PipelineEnginePlugin(
    id = "BeamDataFlowPipelineEngine",
    name = "Beam DataFlow pipeline engine",
    description =
        "This allows you to run your pipeline on Google Cloud Platform DataFlow, provided by the Apache Beam community")
public class BeamDataFlowPipelineEngine extends BeamPipelineEngine
    implements IPipelineEngine<PipelineMeta> {
  @Override
  public IPipelineEngineRunConfiguration createDefaultPipelineEngineRunConfiguration() {
    BeamDataFlowPipelineRunConfiguration runConfiguration =
        new BeamDataFlowPipelineRunConfiguration();
    runConfiguration.setUserAgent("Hop");
    return runConfiguration;
  }

  @Override
  public void validatePipelineRunConfigurationClass(
      IPipelineEngineRunConfiguration engineRunConfiguration) throws HopException {
    if (!(engineRunConfiguration instanceof BeamDataFlowPipelineRunConfiguration)) {
      throw new HopException(
          "A Beam Direct pipeline engine needs a direct run configuration, not of class "
              + engineRunConfiguration.getClass().getName());
    }
  }
}
