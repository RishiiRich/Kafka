package com.kafkaSource;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Properties;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import com.kafkaSource.*;

import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.flume.Channel;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.PollableSource;
import org.apache.flume.PollableSource.Status;
import org.apache.flume.annotations.InterfaceAudience.Private;
import org.apache.flume.conf.Configurable;
import org.apache.flume.event.EventBuilder;
import org.apache.flume.instrumentation.SourceCounter;
import org.apache.flume.source.AbstractSource;
import org.codehaus.jackson.util.BufferRecycler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Kafka_Source extends AbstractSource implements Configurable,PollableSource{

	
	private String path;
	private BufferedReader bufferedreader;
	private String topic;
	private Properties props;
	private Producer<String, String> producer;
	private ProducerConfig config;
	private KeyedMessage<String, String> data;
	private final static Logger logger=LoggerFactory.getLogger(Kafka_Source.class);
	
	
	@Override
	public synchronized void configure(Context context) {
		// TODO Auto-generated method stub
			this.path=context.getString("path");
			this.topic=context.getString("topic");
			
			this.props=new Properties();
			this.props.put("metadata.broker.list", "localhost:9092");
			this.props.put("producer.type", "async");
			this.props.put("serializer.class", "kafka.serializer.StringEncoder");
			this.props.put("request.required.acks", "1");
			
			this.config=new ProducerConfig(this.props);
		
	}

	public synchronized void start() {
		
		logger.info("starting...",this);
		
		producer=new Producer<>(config);
		try
		{
			bufferedreader = new BufferedReader(new FileReader(path));
		}
		catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}
		super.start();
		logger.info("Started",this);
		
	}
	
	
	public synchronized void stop()
	{
		logger.info("Stopping Flume",this);
		super.stop();
	}
	
	@Override
	public Status process() throws EventDeliveryException {
		// TODO Auto-generated method stub
		
		String line;
		Status status;
		try {
			line=bufferedreader.readLine();
			while(line==null)
			{
				line=bufferedreader.readLine();
			}
			Event e=EventBuilder.withBody(line, Charset.forName("UTF-8"));
			getChannelProcessor().processEvent(e);
			
			status=Status.READY;
			
			data=new KeyedMessage<String, String>(topic, line);
			producer.send(data);
			
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			status=Status.BACKOFF;
		}
		
		
		//producer.close();
		return status;
	}
}
