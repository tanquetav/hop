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
 *
 */

package org.apache.hop.execution;

import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.logging.LogLevel;
import org.apache.hop.core.metadata.SerializableMetadataProvider;
import org.apache.hop.core.parameters.INamedParameters;
import org.apache.hop.core.parameters.UnknownParamException;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.execution.Execution.EnvironmentDetailType;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.engine.IPipelineEngine;
import org.apache.hop.workflow.WorkflowMeta;
import org.apache.hop.workflow.engine.IWorkflowEngine;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public final class ExecutionBuilder {
  public String name;
  public String filename;
  public String id;
  public String parentId;
  public ExecutionType executionType;
  public String executorXml;
  public String metadataJson;
  public Map<String, String> variableValues;
  public String runConfigurationName;
  public LogLevel logLevel;
  public Map<String, String> parameterValues;
  public Map<String, String> environmentDetails;
  public Date registrationDate;
  public Date executionStartDate;

  private ExecutionBuilder() {
    this.variableValues = new HashMap<>();
    this.parameterValues = new HashMap<>();
    this.environmentDetails = new HashMap<>();
    this.registrationDate = new Date();
  }

  public static ExecutionBuilder anExecutionRegistration() {
    return new ExecutionBuilder();
  }

  public static ExecutionBuilder fromExecutor(IPipelineEngine<PipelineMeta> pipeline)
      throws HopException {
    ExecutionBuilder builder = anExecutionRegistration();
    builder
        .withFilename(pipeline.getPipelineMeta().getFilename())
        .withName(pipeline.getPipelineMeta().getName())
        .withId(pipeline.getLogChannelId())
        .withParentId(pipeline.getParent()==null ? null : pipeline.getParent().getLogChannelId())
        .withExecutorType(ExecutionType.Pipeline)
        .withExecutorXml(pipeline.getPipelineMeta().getXml(pipeline))
        .withMetadataJson(new SerializableMetadataProvider(pipeline.getMetadataProvider()).toJson())
        .withRunConfigurationName(pipeline.getPipelineRunConfiguration().getName())
        .withLogLevel(pipeline.getLogLevel())
        .withExecutionStartDate(pipeline.getExecutionStartDate());

    builder.environmentDetails.put(
        EnvironmentDetailType.ContainerId.name(), pipeline.getContainerId());

    builder.getVariableInformation(pipeline);
    builder.getParameterInformation(pipeline);
    builder.updateRuntimeInformation();

    return builder;
  }

  public static ExecutionBuilder fromExecutor(IWorkflowEngine<WorkflowMeta> workflow)
      throws HopException {
    ExecutionBuilder builder = anExecutionRegistration();
    builder
        .withFilename(workflow.getWorkflowMeta().getFilename())
        .withName(workflow.getWorkflowMeta().getName())
        .withId(workflow.getLogChannelId())
        .withParentId(workflow.getParent().getLogChannelId())
        .withExecutorType(ExecutionType.Workflow)
        .withExecutorXml(workflow.getWorkflowMeta().getXml(workflow))
        .withMetadataJson(new SerializableMetadataProvider(workflow.getMetadataProvider()).toJson())
        .withRunConfigurationName(workflow.getWorkflowRunConfiguration().getName())
        .withLogLevel(workflow.getLogLevel())
        .withExecutionStartDate(workflow.getExecutionStartDate());

    builder.environmentDetails.put(
        EnvironmentDetailType.ContainerId.name(), workflow.getContainerId());

    builder.getVariableInformation(workflow);
    builder.getParameterInformation(workflow);
    builder.updateRuntimeInformation();

    return builder;
  }

  private void getVariableInformation(IVariables variables) {
    for (String variableName : variables.getVariableNames()) {
      String variableValue = variables.getVariable(variableName);
      this.variableValues.put(variableName, variableValue);
    }
  }

  private void getParameterInformation(INamedParameters parameters) throws UnknownParamException {
    for (String parameterName : parameters.listParameters()) {
      String parameterValue = parameters.getParameterValue(parameterName);
      this.parameterValues.put(parameterName, parameterValue);
    }
  }

  public ExecutionBuilder updateRuntimeInformation() {
    Runtime runtime = Runtime.getRuntime();
    environmentDetails.put(
        EnvironmentDetailType.MaxMemory.name(), Long.toString(runtime.maxMemory()));
    environmentDetails.put(
        EnvironmentDetailType.FreeMemory.name(), Long.toString(runtime.freeMemory()));
    environmentDetails.put(
        EnvironmentDetailType.TotalMemory.name(), Long.toString(runtime.totalMemory()));
    environmentDetails.put(
        EnvironmentDetailType.AvailableProcessors.name(),
        Integer.toString(runtime.availableProcessors()));
    environmentDetails.put(
        EnvironmentDetailType.JavaVersion.name(), System.getProperty("java.version"));
    environmentDetails.put(EnvironmentDetailType.JavaUser.name(), System.getProperty("user.name"));
    try {
      environmentDetails.put(
          EnvironmentDetailType.HostName.name(), InetAddress.getLocalHost().getHostName());
      environmentDetails.put(
          EnvironmentDetailType.HostAddress.name(), InetAddress.getLocalHost().getHostAddress());
    } catch (UnknownHostException e) {
      environmentDetails.put(EnvironmentDetailType.HostName.name(), "<unknown>");
      environmentDetails.put(EnvironmentDetailType.HostAddress.name(), "<unknown>");
    }

    return this;
  }

  public ExecutionBuilder withName(String name) {
    assert name != null : "the registration of an execution needs to have a name";
    this.name = name;
    return this;
  }

  public ExecutionBuilder withFilename(String filename) {
    this.filename = filename;
    return this;
  }

  public ExecutionBuilder withId(String id) {
    assert id != null : "the registration of an execution needs to have a unique ID";
    this.id = id;
    return this;
  }

  public ExecutionBuilder withParentId(String parentId) {
    this.parentId = parentId;
    return this;
  }

  public ExecutionBuilder withExecutorType(ExecutionType executionType) {
    assert executionType != null : "Please specify execution type Pipeline or Workflow, not null";
    assert executionType == ExecutionType.Pipeline || executionType == ExecutionType.Workflow
        : "Please specify execution type Pipeline or Workflow";
    this.executionType = executionType;
    return this;
  }

  public ExecutionBuilder withExecutorXml(String executorXml) {
    this.executorXml = executorXml;
    return this;
  }

  public ExecutionBuilder withMetadataJson(String metadataJson) {
    this.metadataJson = metadataJson;
    return this;
  }

  public ExecutionBuilder withVariableValues(Map<String, String> variableValues) {
    this.variableValues = variableValues;
    return this;
  }

  public ExecutionBuilder withRunConfigurationName(String runConfigurationName) {
    this.runConfigurationName = runConfigurationName;
    return this;
  }

  public ExecutionBuilder withLogLevel(LogLevel logLevel) {
    this.logLevel = logLevel;
    return this;
  }

  public ExecutionBuilder withParameterValues(Map<String, String> parameterValues) {
    this.parameterValues = parameterValues;
    return this;
  }

  public ExecutionBuilder withEnvironmentDetails(Map<String, String> environmentDetails) {
    this.environmentDetails = environmentDetails;
    return this;
  }

  public ExecutionBuilder withRegistrationDate(Date registrationDate) {
    this.registrationDate = registrationDate;
    return this;
  }

  public ExecutionBuilder withExecutionStartDate(Date executionStartDate) {
    this.executionStartDate = executionStartDate;
    return this;
  }

  public Execution build() {
    Execution executionRegistration = new Execution();
    executionRegistration.setName(name);
    executionRegistration.setFilename(filename);
    executionRegistration.setId(id);
    executionRegistration.setParentId(parentId);
    executionRegistration.setExecutionType(executionType);
    executionRegistration.setExecutorXml(executorXml);
    executionRegistration.setMetadataJson(metadataJson);
    executionRegistration.setVariableValues(variableValues);
    executionRegistration.setRunConfigurationName(runConfigurationName);
    executionRegistration.setLogLevel(logLevel);
    executionRegistration.setParameterValues(parameterValues);
    executionRegistration.setEnvironmentDetails(environmentDetails);
    executionRegistration.setRegistrationDate(registrationDate);
    executionRegistration.setExecutionStartDate(executionStartDate);
    return executionRegistration;
  }
}
