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

package org.apache.hadoop.mapreduce.split;

import java.io.IOException;
import java.util.Arrays;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.WritableUtils;
import org.apache.hadoop.mapreduce.JobID;
import org.apache.hadoop.mapreduce.JobSubmissionFiles;
import org.apache.hadoop.mapreduce.MRJobConfig;
import java.lang.*;
import java.util.*;
import java.io.*;

/**
 * A utility that reads the split meta info and creates split meta info objects
 */
@InterfaceAudience.Private
@InterfaceStability.Unstable
public class SplitMetaInfoReader {

	public static JobSplit.TaskSplitMetaInfo[] readSplitMetaInfo(JobID jobId,
			FileSystem fs, Configuration conf, Path jobSubmitDir)
			throws IOException {
		
		 System.out.println("\n\n================MRJobConfig.BFT_FLAG = "+conf.getInt(MRJobConfig.BFT_FLAG, 1));
		 
		
		long maxMetaInfoSize = conf.getLong(MRJobConfig.SPLIT_METAINFO_MAXSIZE,
				MRJobConfig.DEFAULT_SPLIT_METAINFO_MAXSIZE);
		Path metaSplitFile = JobSubmissionFiles
				.getJobSplitMetaFile(jobSubmitDir);
		String jobSplitFile = JobSubmissionFiles.getJobSplitFile(jobSubmitDir)
				.toString();
		FileStatus fStatus = fs.getFileStatus(metaSplitFile);
		if (maxMetaInfoSize > 0 && fStatus.getLen() > maxMetaInfoSize) {
			throw new IOException("Split metadata size exceeded "
					+ maxMetaInfoSize + ". Aborting job " + jobId);
		}
		FSDataInputStream in = fs.open(metaSplitFile);
		byte[] header = new byte[JobSplit.META_SPLIT_FILE_HEADER.length];
		in.readFully(header);
		if (!Arrays.equals(JobSplit.META_SPLIT_FILE_HEADER, header)) {
			throw new IOException("Invalid header on split file");
		}
		int vers = WritableUtils.readVInt(in);
		if (vers != JobSplit.META_SPLIT_VERSION) {
			in.close();
			throw new IOException("Unsupported split version " + vers);
		}
		int numSplits = WritableUtils.readVInt(in); // TODO: check for insane
													// values
		
		//--- bft: the following line is unnecessary, I added it just for default return value for allSplitMetaInfo
		JobSplit.TaskSplitMetaInfo[] allSplitMetaInfo = new JobSplit.TaskSplitMetaInfo[numSplits];
		
		
		switch (conf.getInt(MRJobConfig.BFT_FLAG, 1)) 
		{
	        case 1://No BFT
	        {
	        	//JobSplit.TaskSplitMetaInfo[] 
	        			allSplitMetaInfo = new JobSplit.TaskSplitMetaInfo[numSplits];
			    for (int i = 0; i < numSplits; i++) {
			      JobSplit.SplitMetaInfo splitMetaInfo = new JobSplit.SplitMetaInfo();
			      splitMetaInfo.readFields(in);
			      JobSplit.TaskSplitIndex splitIndex = new JobSplit.TaskSplitIndex(
			          jobSplitFile,
			          splitMetaInfo.getStartOffset());
			      allSplitMetaInfo[i] = new JobSplit.TaskSplitMetaInfo(splitIndex,
			          splitMetaInfo.getLocations(),
			          splitMetaInfo.getInputDataLength());
			    }			
	            break;
	        }
	        case 2://BFT: replicate the AM(it should replicate the mappers and reducers by itself)   //deal with it as No BFT
	        {
	        	//JobSplit.TaskSplitMetaInfo[] 
    			allSplitMetaInfo = new JobSplit.TaskSplitMetaInfo[numSplits];
			    for (int i = 0; i < numSplits; i++) {
			      JobSplit.SplitMetaInfo splitMetaInfo = new JobSplit.SplitMetaInfo();
			      splitMetaInfo.readFields(in);
			      JobSplit.TaskSplitIndex splitIndex = new JobSplit.TaskSplitIndex(
			          jobSplitFile,
			          splitMetaInfo.getStartOffset());
			      allSplitMetaInfo[i] = new JobSplit.TaskSplitMetaInfo(splitIndex,
			          splitMetaInfo.getLocations(),
			          splitMetaInfo.getInputDataLength());
			    }
	            break;
	        }
	        case 3://BFT: replicate mappers and reducers (both r times ?), single AM
	        {
	        	//JobSplit.TaskSplitMetaInfo[] 
	        			allSplitMetaInfo = new JobSplit.TaskSplitMetaInfo[numSplits * 4];// wasnumSplits				
				// --------------note that we didn't change the original numSplits,
				// //--------------we just replaced it with numSplits*4 where necessary
				for (int i = 0; i < numSplits; i++) {
					JobSplit.SplitMetaInfo splitMetaInfo = new JobSplit.SplitMetaInfo();
					splitMetaInfo.readFields(in);
					JobSplit.TaskSplitIndex splitIndex = new JobSplit.TaskSplitIndex(
							jobSplitFile, splitMetaInfo.getStartOffset());// ---------note the offset
					// original code:
					// allSplitMetaInfo[i] = new JobSplit.TaskSplitMetaInfo(splitIndex,
					// splitMetaInfo.getLocations(),
					// splitMetaInfo.getInputDataLength());
					// ----------------new code for bft replication:
					for (int r = 0; r < 4; r++)// r=number of replicas
					{
						allSplitMetaInfo[(4 * i) + r] = new JobSplit.TaskSplitMetaInfo(
								splitIndex, splitMetaInfo.getLocations(),
								splitMetaInfo.getInputDataLength());
					}
				}
	        	break;
	        }
	        case 4://BFT: replicate the AM (r3 times in WordCount.java) and replicate mappers and reducers (both r times)
	        {
	        	//JobSplit.TaskSplitMetaInfo[] 
	        			allSplitMetaInfo = new JobSplit.TaskSplitMetaInfo[numSplits * 4];// was numSplits				
				// --------------note that we didn't change the original numSplits,
				// //--------------we just replaced it with numSplits*4 where necessary
				for (int i = 0; i < numSplits; i++) {
					JobSplit.SplitMetaInfo splitMetaInfo = new JobSplit.SplitMetaInfo();
					splitMetaInfo.readFields(in);
					JobSplit.TaskSplitIndex splitIndex = new JobSplit.TaskSplitIndex(
							jobSplitFile, splitMetaInfo.getStartOffset());// ---------note the offset
					// original code:
					// allSplitMetaInfo[i] = new JobSplit.TaskSplitMetaInfo(splitIndex,
					// splitMetaInfo.getLocations(),
					// splitMetaInfo.getInputDataLength());
					// ----------------new code for bft replication:
					for (int r = 0; r < 4; r++)// r=number of replicas
					{
						allSplitMetaInfo[(4 * i) + r] = new JobSplit.TaskSplitMetaInfo(
								splitIndex, splitMetaInfo.getLocations(),
								splitMetaInfo.getInputDataLength());
					}
				}
	        	break;
	        }
	        default://deal with it as No BFT
	        {
	        	//JobSplit.TaskSplitMetaInfo[] 
	        			allSplitMetaInfo = new JobSplit.TaskSplitMetaInfo[numSplits];
			    for (int i = 0; i < numSplits; i++) {
			      JobSplit.SplitMetaInfo splitMetaInfo = new JobSplit.SplitMetaInfo();
			      splitMetaInfo.readFields(in);
			      JobSplit.TaskSplitIndex splitIndex = new JobSplit.TaskSplitIndex(
			          jobSplitFile,
			          splitMetaInfo.getStartOffset());
			      allSplitMetaInfo[i] = new JobSplit.TaskSplitMetaInfo(splitIndex,
			          splitMetaInfo.getLocations(),
			          splitMetaInfo.getInputDataLength());
			    }		
	            break;
	        }
		}
		
		
		/*
		if(conf.getInt(MRJobConfig.BFT_FLAG, 1)==1)//No BFT
		{
			 JobSplit.TaskSplitMetaInfo[] allSplitMetaInfo = new JobSplit.TaskSplitMetaInfo[numSplits];
				    for (int i = 0; i < numSplits; i++) {
				      JobSplit.SplitMetaInfo splitMetaInfo = new JobSplit.SplitMetaInfo();
				      splitMetaInfo.readFields(in);
				      JobSplit.TaskSplitIndex splitIndex = new JobSplit.TaskSplitIndex(
				          jobSplitFile,
				          splitMetaInfo.getStartOffset());
				      allSplitMetaInfo[i] = new JobSplit.TaskSplitMetaInfo(splitIndex,
				          splitMetaInfo.getLocations(),
				          splitMetaInfo.getInputDataLength());
				    }			
		}
		
		if(conf.getInt(MRJobConfig.BFT_FLAG, 1)==2)//BFT: replicate the AM(it should replicate the mappers and reducers by itself)
		{
			
		}
		
		if(conf.getInt(MRJobConfig.BFT_FLAG, 1)==3)//BFT: replicate mappers and reducers (both r times ?), single AM
		{
			JobSplit.TaskSplitMetaInfo[] allSplitMetaInfo = new JobSplit.TaskSplitMetaInfo[numSplits * 4];// ----------numSplits*4
			// was
			// numSplits
			// --------------note that we didn't change the original numSplits,
			// //--------------we just replaced it with numSplits*4 where necessary
			for (int i = 0; i < numSplits; i++) {
				JobSplit.SplitMetaInfo splitMetaInfo = new JobSplit.SplitMetaInfo();
				splitMetaInfo.readFields(in);
				JobSplit.TaskSplitIndex splitIndex = new JobSplit.TaskSplitIndex(
						jobSplitFile, splitMetaInfo.getStartOffset());// ---------note
				// the
				// offset
				// original code:
				// allSplitMetaInfo[i] = new JobSplit.TaskSplitMetaInfo(splitIndex,
				// splitMetaInfo.getLocations(),
				// splitMetaInfo.getInputDataLength());
				// ----------------new code for bft replication:
				for (int r = 0; r < 4; r++)// r=number of replicas
				{
					allSplitMetaInfo[(4 * i) + r] = new JobSplit.TaskSplitMetaInfo(
							splitIndex, splitMetaInfo.getLocations(),
							splitMetaInfo.getInputDataLength());
				}
			}
			
		}
		*/
		
		
		
		in.close();
		return allSplitMetaInfo;
	}

}

