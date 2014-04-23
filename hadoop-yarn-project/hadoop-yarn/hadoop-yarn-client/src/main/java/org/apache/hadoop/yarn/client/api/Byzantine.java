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

package org.apache.hadoop.yarn.client.api;

import java.io.PrintWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;

import java.lang.Boolean;
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
import org.apache.hadoop.yarn.api.ApplicationConstants;
import org.apache.hadoop.yarn.api.records.ContainerLaunchContext;
import org.apache.hadoop.yarn.api.records.Container;
import org.apache.hadoop.yarn.api.records.ContainerId;
import org.apache.hadoop.yarn.api.records.ContainerStatus;
import org.apache.hadoop.yarn.api.records.FinalApplicationStatus;
import org.apache.hadoop.yarn.api.records.NodeReport;
import org.apache.hadoop.yarn.api.records.Priority;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.client.api.AMRMClient;
import org.apache.hadoop.yarn.client.api.AMRMClient.ContainerRequest;
import org.apache.hadoop.yarn.client.api.impl.AMRMClientImpl;
import org.apache.hadoop.yarn.client.api.impl.NMClientImpl;
import org.apache.hadoop.yarn.api.protocolrecords.AllocateResponse;
import org.apache.hadoop.yarn.exceptions.YarnException;

import com.google.common.annotations.VisibleForTesting;

import org.apache.hadoop.yarn.api.records.ContainerLaunchContext;
import org.apache.hadoop.yarn.api.records.NodeId;

import org.apache.hadoop.yarn.util.Records;
import org.apache.hadoop.yarn.api.records.ContainerExitStatus;


/**
 * <code>Byzantine</code> handles Byzantine Fault tolerance. It does this by 
 * overriding the communications with the RM and NM. 
 * 

 */
@Public
@Stable

public class Byzantine<T extends ContainerRequest> {
    //Had to make this class extend AbstractService because Java does not
    //allow multiple inheritance. Now AMRMClientAsyncByz can extend Byzantine instead
    //of AbstractService
    
    //create some variables for tracking created containers    
    private Boolean inByzantineMode = true;
    public int NUM_REPLICAS = 4;
    
    //need access to logger..
    private static final Log LOG = LogFactory.getLog(Byzantine.class);

    public static ConcurrentMap<ContainerRequest,ArrayList<Container>> allocationTable =
        new ConcurrentHashMap<ContainerRequest, ArrayList<Container>>();

    public List<ArrayList<Boolean>> finishedContainers =
        new ArrayList<ArrayList<Boolean>>();

    private String outputLocation = "stdout";

    private Map<String, String> env = System.getenv();

    private Boolean isDuplicateRequest = false;
    private Boolean isDuplicateStart = false;
    private int duplicateStartCount = 0;

    // Here are the functions from AMRMClientAsync which we need to have
    // Byzantine implementations of
    public void addContainerRequestByz(AMRMClientImpl amClient, T req){
        LOG.info("***addContainerRequestByz***");

        if (!allocationTable.containsKey(req)) {
            allocationTable.put((ContainerRequest)req, new ArrayList<Container>(1));
            finishedContainers.add(new ArrayList<Boolean>(1));
        }
        isDuplicateRequest = true;
        for (int i=0; i<NUM_REPLICAS; i++)
            amClient.addContainerRequest(req);
        isDuplicateRequest = false;
    }


    public Map<String, ByteBuffer> startContainerByz(NMClientImpl nmClient, Container container, ContainerLaunchContext containerLaunchContext){
        LOG.info("***startContainerByz::"+duplicateStartCount+"***");

        //here we need to duplicate this start message for all the containers
        List<Container> dups = getDups(container.getId());
        for( Container c : dups ){
            //create a new container launch context for the duplicate
            ContainerLaunchContext ctx = Records.newRecord(ContainerLaunchContext.class);

            ctx.setTokens(containerLaunchContext.getTokens());
            ctx.setLocalResources(containerLaunchContext.getLocalResources());
            ctx.setServiceData(containerLaunchContext.getServiceData());
            ctx.setEnvironment(containerLaunchContext.getEnvironment());
            ctx.setCommands(fixCommand(containerLaunchContext.getCommands(),String.valueOf(c.getId().getId())));
            ctx.setApplicationACLs(containerLaunchContext.getApplicationACLs());
            
            //then launch him
            try{
                if (duplicateStartCount++ < NUM_REPLICAS-1) {
                    LOG.info("*** < "+duplicateStartCount+"****");
                    nmClient.startContainer(c, ctx);
                }
                else {
                    LOG.info("*** >= "+duplicateStartCount+"****");
                    return nmClient.startContainer(c, ctx);
                }
            }
            catch(Exception ex){
                LOG.error("Error calling nmClient.startContainer()");
            }
        }
        return null;
    }

    public static String join(String r[], String d) {
            if (r.length == 0) return "";
            StringBuilder sb = new StringBuilder();
            int i;
            for(i=0; i<r.length-1;i++)
                sb.append(r[i]+d);
            return sb.toString()+r[i];
    }
    
    private List<String> fixCommand(List<String> cmds, String containerID){
        List<String> retList = new ArrayList<String>();
        for (String command : cmds){
            if(command.contains("org.apache.spark.executor.CoarseGrainedExecutorBackend")){
                String splits[] = command.split(" ");
                for (int i=0; i<splits.length; i++){
                    //System.out.println(i+" : "+ splits[i]);
                    if(splits[i].contains("org.apache.spark.executor")){
                        splits[i+2] = containerID;
                    }
                }
                command = join(splits, " ");
            }
            retList.add(command);
        }

        return retList;
    }


    // //AMRMClientAsync Callback functions
    public AllocateResponse onContainersCompletedByz(AllocateResponse allocateResponse){
        LOG.info("***onContainersCompletedByz***");
        List<ContainerStatus> completed = allocateResponse.getCompletedContainersStatuses();
        List<ContainerStatus> finalCompleted = new ArrayList<ContainerStatus>();

        for (ContainerStatus c : completed) {
            Object key = findContainerList(c.getContainerId());
            if (key == null) {LOG.error("ERROR: allocateResponse()"+c.getContainerId()); continue;}
            ArrayList<Container> dups = allocationTable.get(key);

            int arrayIndex = findKeyIndex((ContainerRequest)key);
            int containerIndex = findContainerIndex(dups, c.getContainerId());
            finishedContainers.get(arrayIndex).set(containerIndex, Boolean.TRUE);
            if (!finishedContainers.get(arrayIndex).contains(Boolean.FALSE)) {
                 // Boolean isVerified = verify(allocationTable.get(key));
                 // if (isVerified){
                 //     LOG.info("THESE CONTAINERS ARE OK!!!");
                 // }
                 // else{
                 //     LOG.info("THESE CONTAINERS ARE NOTTTTTTTTTTTTT OK!!!");

                 //    //change container exit status to report byzantine failure
                 //    //c.setExitStatus(ContainerExitStatus.BYZANTINE_FAILURE);

                 // }
                finalCompleted.add(c);
            }
        }
        return createAllocateResponse(finalCompleted,
                                      allocateResponse.getAllocatedContainers(),
                                      allocateResponse);
    }

    public AllocateResponse onContainersAllocatedByz(AllocateResponse allocateResponse){
        LOG.info("***onContainersAllocatedByz***");
        List<Container> allocated = allocateResponse.getAllocatedContainers();
        
        // loop through every container and add it to the allocationTable
        for (Container c : allocated) {
            int i = 0;
            for (Map.Entry<ContainerRequest, ArrayList<Container>> e :
                     allocationTable.entrySet()) {
                ContainerRequest key = e.getKey();
                List<Container> dups = e.getValue();
                if (containsId(c.getId())) break;
                if (resourceLessThanEqual(key.getCapability(), c.getResource())
                    && dups.size() < NUM_REPLICAS) {

                    dups.add(c);
                    finishedContainers.get(i).add(Boolean.FALSE);

                    // all duplicates have been allocated
                    if (dups.size() == NUM_REPLICAS) {
                        return createAllocateResponse(allocateResponse.getCompletedContainersStatuses(),
                                                      dups.subList(dups.size()-1, dups.size()),
                                                      allocateResponse);
                    }
                    break;
                }
                i++;
            }
        }
        return createAllocateResponse(allocateResponse.getCompletedContainersStatuses(),
                                      new ArrayList<Container>(),
                                      allocateResponse);
    }

    public void setOutputLocation(String outputLocation) {
        this.outputLocation = outputLocation;
    }

    public Boolean verify(List<Container> containers) {
        int[] outputs = new int[NUM_REPLICAS];
        int i=0;
        LOG.info("***VERIFY***");
        for (Container c : containers) {
            LOG.info(c.getId()+" "+c.getId().getApplicationAttemptId().getApplicationId());
            int output = checkOutput(c, containers);
            if (output == 0) return true;
            outputs[i++] = output;
        }
        int sum = 0;
        for (i=0; i<NUM_REPLICAS; i++) sum+=outputs[i];
        for (i=0; i<NUM_REPLICAS; i++) System.out.print(outputs[i]);
        LOG.info("\nSUM:"+sum+" <= "+((NUM_REPLICAS-Math.ceil(NUM_REPLICAS/2)-1)*2));
        return sum <= ((NUM_REPLICAS-(Math.ceil(NUM_REPLICAS/2)-1))*2) ?
            true : false;
    }

    private int checkOutput(Container c, List<Container> containers) {
        String path = env.get("HADOOP_PREFIX")+"/logs/userlogs/";
        int errorCount = 0;

        PrintWriter writer = null;
        PrintWriter writer0 = null;
        PrintWriter writer1 = null;
        try {
            writer = new PrintWriter(
            path+containers.get(0).getId().getApplicationAttemptId().getApplicationId()
            +"/"+containers.get(0).getId()+"/"+outputLocation);

            writer.println("The first line");
            writer.flush();

            writer0 = new PrintWriter(
            path+containers.get(1).getId().getApplicationAttemptId().getApplicationId()
            +"/"+containers.get(1).getId()+"/"+outputLocation);

            writer0.println("The second line");
            writer0.flush();

            writer1 = new PrintWriter(
            path+containers.get(2).getId().getApplicationAttemptId().getApplicationId()
            +"/"+containers.get(2).getId()+"/"+outputLocation);

            writer1.println("The third line");
            writer1.flush();
        } catch(FileNotFoundException e) {
            LOG.info("File not found: "+e);
        } finally {
            try {writer.close();writer0.close();writer1.close();} catch(Exception e) {}
        }

        for (Container con : containers) {
            String command = "diff "
                    +path+c.getId().getApplicationAttemptId().getApplicationId()
                    +"/"+c.getId()+"/"+outputLocation
                    +" "+path+con.getId().getApplicationAttemptId().getApplicationId()
                    +"/"+con.getId()+"/"+outputLocation;

            if (c.getId().getId() != con.getId().getId()) {
                LOG.info(command);
                StringBuffer output = new StringBuffer();

                Process p;
                try {
                    p = Runtime.getRuntime().exec(command);
                    p.waitFor();
                    BufferedReader reader =
                        new BufferedReader(new InputStreamReader(p.getInputStream()));

                    String line = "";
                    while ((line = reader.readLine())!= null) 
                        output.append(line);
                    
                    BufferedReader stderrReader = new BufferedReader(
                        new InputStreamReader(p.getErrorStream()));
                    line = "";
                    while ((line = stderrReader.readLine()) != null)
                        output.append(line);
                    LOG.info("["+output.toString()+"]");
                    if (!output.toString().equals("")) errorCount++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return errorCount;
    }
    // HELPER METHODS

    private AllocateResponse createAllocateResponse(List<ContainerStatus> statuses,
                                                    List<Container> duplicates,
                                                    AllocateResponse allocateResponse) {
        return AllocateResponse.newInstance(allocateResponse.getResponseId(),
                                            statuses, duplicates, allocateResponse.getUpdatedNodes(),
                                            allocateResponse.getAvailableResources(), allocateResponse.getAMCommand(),
                                            allocateResponse.getNumClusterNodes(),
                                            allocateResponse.getPreemptionMessage(), allocateResponse.getNMTokens());
    }

    private Boolean containsId(ContainerId id) {
        for (Map.Entry<ContainerRequest, ArrayList<Container>> e :
                 allocationTable.entrySet()) {
            List<Container> dups = e.getValue();
            for (Container c : dups)
                if (c.getId().getId() == id.getId())
                    return true;
        }
        return false;
    }


    private void printLaunchContext(ContainerLaunchContext ctx){
        LOG.info("Command List:");
        for(String cmd : ctx.getCommands()){
            System.out.print(cmd + " ");
        }
        LOG.info("");
    }

    private List<Container> getDups(ContainerId id){
        for (Map.Entry<ContainerRequest, ArrayList<Container>> e :
                 allocationTable.entrySet()) {
            List<Container> dups = e.getValue();
            for (Container c : dups)
                if (c.getId().getId() == id.getId())
                    return dups;
        }
        return null;
    }

    public Boolean inByzantineMode() {
        return inByzantineMode;
    }

    public void setInByzantine(Boolean inByzantineMode){
        this.inByzantineMode = inByzantineMode;
    }
    
    public Boolean isDuplicateRequest() {
        return isDuplicateRequest;
    }

    public Boolean isDuplicateStart(){
        return (duplicateStartCount == 0) ? false : true;
    }

    public int getDuplicateStartCount() {
        return duplicateStartCount;
    }

    public void resetDuplicateStartCount() {
        this.duplicateStartCount = 0;
    }

    public int getNumReplicas() {
        return NUM_REPLICAS;
    }

    public int findContainerIndex(ArrayList<Container> dups, ContainerId cid) {
        int i=0;
        for (Container con : dups) {
            if (con.getId().getId() == cid.getId())
                return i;
            i++;
        }
        LOG.error("CONTAINER INDEX: RETURNING -1");
        return -1;
    }
 
    public int findKeyIndex(ContainerRequest key) {
        int i=0;
        for (ContainerRequest req : allocationTable.keySet()) {
            if (key.equals(req))
                return i;
            i++;
        }
        LOG.error("KEY INDEX: RETURNING -1");
        return -1;
    }

    public ContainerRequest findContainerList(ContainerId cid) {
        for (Map.Entry<ContainerRequest, ArrayList<Container>> e :
                 allocationTable.entrySet()) {
            ContainerRequest key = e.getKey();
            ArrayList<Container> dups = e.getValue();
            for (Container con : dups) {
                if (con.getId().getId() == cid.getId())
                    return key;
            }
        }
        LOG.error("RETURNING NULL");
        return null;
    }

    public boolean resourceLessThanEqual(Object obj0, Object obj1) {
        if (obj0 == null || obj1 == null)
            return false;
        if (!(obj0 instanceof Resource) && !(obj1 instanceof Resource))
            return false;
        Resource r0 = (Resource) obj0;
        Resource r1 = (Resource) obj1;
        if (r0.getMemory() > r1.getMemory() ||
            r0.getVirtualCores() > r1.getVirtualCores()) {
            return false;
        }
        return true;
    }
 }