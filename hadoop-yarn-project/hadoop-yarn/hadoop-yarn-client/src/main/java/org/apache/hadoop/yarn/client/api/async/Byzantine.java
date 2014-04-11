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
import org.apache.hadoop.yarn.client.api.AMRMClient.ContainerRequest;
import org.apache.hadoop.yarn.client.api.async.impl.AMRMClientAsyncImpl;
import org.apache.hadoop.yarn.client.api.impl.AMRMClientImpl;
import org.apache.hadoop.yarn.exceptions.YarnException;

import org.apache.hadoop.yarn.client.api.async.impl.ByzantineImpl;

import com.google.common.annotations.VisibleForTesting;

/**
 * <code>AMRMClientAsync</code> handles communication with the ResourceManager
 * and provides asynchronous updates on events such as container allocations and
 * completions.  It contains a thread that sends periodic heartbeats to the
 * ResourceManager.
 * 
 * It should be used by implementing a CallbackHandler:
 * <pre>
 * {@code
 * class MyCallbackHandler implements AMRMClientAsync.CallbackHandler {
 *   public void onContainersAllocated(List<Container> containers) {
 *     [run tasks on the containers]
 *   }
 *   
 *   public void onContainersCompleted(List<ContainerStatus> statuses) {
 *     [update progress, check whether app is done]
 *   }
 *   
 *   public void onNodesUpdated(List<NodeReport> updated) {}
 *   
 *   public void onReboot() {}
 * }
 * }
 * </pre>
 * 
 * The client's lifecycle should be managed similarly to the following:
 * 
 * <pre>
 * {@code
 * AMRMClientAsync asyncClient = 
 *     createAMRMClientAsync(appAttId, 1000, new MyCallbackhandler());
 * asyncClient.init(conf);
 * asyncClient.start();
 * RegisterApplicationMasterResponse response = asyncClient
 *    .registerApplicationMaster(appMasterHostname, appMasterRpcPort,
 *       appMasterTrackingUrl);
 * asyncClient.addContainerRequest(containerRequest);
 * [... wait for application to complete]
 * asyncClient.unregisterApplicationMaster(status, appMsg, trackingUrl);
 * asyncClient.stop();
 * }
 * </pre>
 */
@Public
@Stable

public class Byzantine<T extends ContainerRequest> extends AbstractService {

   
    //Had to make this class extend AbstractService because Java does not
    //allow multiple inheritance. Now AMRMClientAsyncByz can extend Byzantine instead
    //of AbstractService
    
    //create some variables for tracking created containers    
    public static Boolean byzantine_mode = true;
    
    private static final Log LOG = LogFactory.getLog(Byzantine.class);

 
    @Private
    @VisibleForTesting
    public Byzantine(String name) {
      super(name);
    }

    public void set_byzantine_mode(Boolean value){
        this.byzantine_mode = value;
    }


    /**
    * Request containers for resources before calling <code>allocate</code>
    * @param req Resource request
    */
    public void addContainerRequest(T req){
        LOG.info("Byz Add Container Request");
    }
}
