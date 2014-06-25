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
package org.apache.hadoop.examples;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.MRJobConfig;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import java.io.*;
import java.util.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;
 



public class WordCount {

  public static class TokenizerMapper 
       extends Mapper<Object, Text, Text, IntWritable>{
    
    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();
      
    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
      StringTokenizer itr = new StringTokenizer(value.toString());
      while (itr.hasMoreTokens()) {
        word.set(itr.nextToken());
        context.write(word, one);
      }
    }
  }
  
  public static class IntSumReducer 
       extends Reducer<Text,IntWritable,Text,IntWritable> {
    private IntWritable result = new IntWritable();

    public void reduce(Text key, Iterable<IntWritable> values, 
                       Context context
                       ) throws IOException, InterruptedException {
      int sum = 0;
      for (IntWritable val : values) {
        sum += val.get();
      }
      result.set(sum);
      context.write(key, result);
    }
  }

  public static void main(String[] args) throws Exception {
      
      
    
      
	  int r3=0;//default//number of AM replicas
	  int BFT_FLAG_LOCAL = 0;
	  
	  try {//---- mapred-site.xml parser // new for bft
      	File fXmlFile = new File("etc/hadoop/mapred-site.xml");
      	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      	Document doc = dBuilder.parse(fXmlFile);
       	doc.getDocumentElement().normalize();
       	NodeList nList = doc.getElementsByTagName("property");
       	for (int temp = 0; temp < nList.getLength(); temp++) {
       		Node nNode = nList.item(temp);
       		if (nNode.getNodeType() == Node.ELEMENT_NODE) {
       			Element eElement = (Element) nNode;
      			if(eElement.getElementsByTagName("name").item(0).getTextContent().equals("mapred.job.bft"))
      			{
      				System.out.println(".........name : " + eElement.getElementsByTagName("name").item(0).getTextContent());
      				System.out.println(".........value : " + eElement.getElementsByTagName("value").item(0).getTextContent());
      				BFT_FLAG_LOCAL=Integer.parseInt(eElement.getElementsByTagName("value").item(0).getTextContent().toString());
      			}
      		}
      	}
          } catch (Exception e) {
      	e.printStackTrace();
          }
	  
	     switch (BFT_FLAG_LOCAL) 
		{
	        case 1://No BFT
	        {
	        	System.out.println("------ENTERED case 1---------");
	        	r3=1;
	        	break;
	        }
	        case 2://BFT: replicate the AM(it should replicate the mappers and reducers by itself)   //deal with it as No BFT
	        {
	        	System.out.println("------ENTERED case 2---------");
	        	r3=8;
	        	break;	        
	        }
	        case 3://BFT: replicate mappers and reducers (both r times ?), single AM
	        {
	        	System.out.println("------ENTERED case 3---------");
	        	r3=1;
	        	break;
	        }
	        case 4://BFT: replicate the AM (r3 times in WordCount.java) and replicate mappers and reducers (both r times)
	        {
	        	System.out.println("------ENTERED case 4---------");
	        	r3=8;
	        	break;	        
	        }
	        default://deal with it as No BFT
	        {
	        	System.out.println("------ENTERED default---------");
	        	r3=1;
	        	break;
	        }
		}
	    
	    
	    Configuration[] conf = new Configuration[r3];
	    for( int i=0; i<r3; i++ )
	    	conf[i] = new Configuration();
	    
	    String[] otherArgs = new GenericOptionsParser(conf[0], args).getRemainingArgs();
	    if (otherArgs.length != 2) {
	      System.err.println("Usage: wordcount <in> <out>");
	      System.exit(2);
	    }
	    
	    
	    
	    long startTime = System.currentTimeMillis();
	    long elapsedTime = 0L;
	    		
	  for (int i=0;i<r3;i++)
	  {
		  System.out.println("------INSIDE the for loop , r3 = --------- "+r3+" -------------- ");
		  
		  Job job = new Job(conf[i], "word count");
	    
	    job.setJarByClass(WordCount.class);
	    job.setMapperClass(TokenizerMapper.class);
	    job.setCombinerClass(IntSumReducer.class);
	    job.setReducerClass(IntSumReducer.class);
	    job.setOutputKeyClass(Text.class);
	    job.setOutputValueClass(IntWritable.class);
	    FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
	    FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]+Integer.toString(i)));
	    //System.exit(job.waitForCompletion(true) ? 0 : 1);
	    job.submit();
	    //job.waitForCompletion(true);//was true
	    

	    
	    try {
	        Thread.sleep(2000);
	    } catch(InterruptedException ex) {
	        Thread.currentThread().interrupt();
	    }
	    
	    
	  }
	  elapsedTime = (new Date()).getTime() - startTime;	  
	  System.out.println("\n\n----------- elapsedTime = "+elapsedTime+"\n\n");
  }
}






/*//---bft : this is the the Original Code
public static void main(String[] args) throws Exception {
	  
	    Configuration conf = new Configuration();
	    String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
	    if (otherArgs.length != 2) {
	      System.err.println("Usage: wordcount <in> <out>");
	      System.exit(2);
	    }
	    Job job = new Job(conf, "word count");
	    job.setJarByClass(WordCount.class);
	    job.setMapperClass(TokenizerMapper.class);
	    job.setCombinerClass(IntSumReducer.class);
	    job.setReducerClass(IntSumReducer.class);
	    job.setOutputKeyClass(Text.class);
	    job.setOutputValueClass(IntWritable.class);
	    FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
	    FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
	    System.exit(job.waitForCompletion(true) ? 0 : 1);
	  
}
*/









