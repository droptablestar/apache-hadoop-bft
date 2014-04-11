/**
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.apache.hadoop.yarn.client.api.async;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import org.apache.hadoop.classification.InterfaceAudience.Private;
import org.apache.hadoop.classification.InterfaceAudience.Public;
import org.apache.hadoop.classification.InterfaceStability.Stable;
import org.apache.hadoop.service.AbstractService;
import org.apache.hadoop.yarn.api.protocolrecords.RegisterApplicationMasterResponse;
import org.apache.hadoop.yarn.api.records.Container;
import org.apache.hadoop.yarn.api.records.ContainerId;
import org.apache.hadoop.yarn.api.records.ContainerStatus;
import org.apache.hadoop.yarn.api.records.FinalApplicationStatus;
import org.apache.hadoop.yarn.api.records.NodeReport;
import org.apache.hadoop.yarn.api.records.Priority;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.client.api.AMRMClient;
import org.apache.hadoop.yarn.client.api.async.AMRMClientAsync;
import org.apache.hadoop.yarn.client.api.AMRMClient.ContainerRequest;
import org.apache.hadoop.yarn.client.api.impl.AMRMClientImpl;
import org.apache.hadoop.yarn.exceptions.YarnException;

import com.google.common.annotations.VisibleForTesting;

import org.apache.hadoop.yarn.api.records.ContainerLaunchContext;
import org.apache.hadoop.yarn.api.records.NodeId;

/**
 * <code>Byzantine</code> handles Byzantine Fault tolerance. It does this by 
 * overriding the communications with the RM and NM. 
 * 
 */
@Public
@Stable

public class Byzantine<T extends ContainerRequest> extends AbstractService{

   
    //Had to make this class extend AbstractService because Java does not
    //allow multiple inheritance. Now AMRMClientAsyncByz can extend Byzantine instead
    //of AbstractService
    
    //create some variables for tracking created containers    
    public static Boolean byzantine_mode = true;
   
    //need access to logger..
    private static final Log LOG = LogFactory.getLog(Byzantine.class);

 
    @Private
    @VisibleForTesting
    public Byzantine(String name) {
      super(name);
    }

    public void set_byzantine_mode(Boolean value){
        this.byzantine_mode = value;
    }


    //Here are the functions from AMRMClientAsync which we need to have Byzantine implementations of 
    public void addContainerRequestByz(T req){
        LOG.info("***addContainerRequestByz***");
    }


    public void removeContainerRequestByz(T req){
        LOG.info("***removeContainerRequestByz***");
    }


    public void releaseAssignedContainerByz(ContainerId containerId){
        LOG.info("***releaseAssignedContainerByz***");
    }

 
    //AMRMClientAsync Callback functions
    public void onContainersCompletedByz(List<ContainerStatus> statuses){
        LOG.info("***onContainersCompletedByz***");
    }
    
    public void onContainersAllocatedByz(List<Container> containers){
        LOG.info("***onContainersAllocatedByz***");
    }
    
    public void onShutdownRequestByz(){
        LOG.info("***onShutdownRequestByz***");
    }
    
    public void onNodesUpdatedByz(List<NodeReport> updatedNodes){
        LOG.info("***onNodesUpdatedByz***");
    }
    
    public float getProgressByz(){
        return 0; 
    }
    
    public void onErrorByz(Throwable e){
        LOG.info("***onErrorByz***");
    }




    //Here are the functions from NMClientAsycn which we need to have Byzantine implementations of
    public void startContainerAsyncByz(Container container, ContainerLaunchContext containerLaunchContext){
         LOG.info("***startContainerAsyncByz***");
    }
    
    public void stopContainerAsyncByz(ContainerId containerId, NodeId nodeId){
         LOG.info("***stopContainerAsyncByz***");
    }

    public void getContainerStatusAsyncByz(ContainerId containerId, NodeId nodeId){
         LOG.info("***getContainerStatusAsyncByz***"); 
    }

    //NMClientAsync Callabck Functions
    public void onContainerStartedByz(ContainerId containerId,Map<String, ByteBuffer> allServiceResponse){
        LOG.info("***onContainerStartedByz***");
    }

    public void onContainerStatusReceivedByz(ContainerId containerId, ContainerStatus containerStatus){
        LOG.info("***onContainerStatusReceivedByz***");
    }

    public void onContainerStoppedByz(ContainerId containerId){
        LOG.info("***onContainerStoppedByz***");
    }

    public void onStartContainerErrorByz(ContainerId containerId, Throwable t){
        LOG.info("***onStartContainerErrorByz***");
    }

    public void onGetContainerStatusErrorByz(ContainerId containerId, Throwable t){
        LOG.info("***onGetContainerStatusErrorByz***");
    }

    public void onStopContainerErrorByz(ContainerId containerId, Throwable t){
        LOG.info("***onStopContainerErrorByz***");
    }
 }
