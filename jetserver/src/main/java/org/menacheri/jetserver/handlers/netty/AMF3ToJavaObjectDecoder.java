package org.menacheri.jetserver.handlers.netty;

import flex.messaging.io.SerializationContext;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.menacheri.jetserver.convert.Transform;
import org.menacheri.jetserver.convert.flex.AMFDeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class takes a {@link ByteBuf} containing AMF3 object as input and
 * creates a java object from it using the {@link AMFDeSerializer} class. 
 * 
 * @author Abraham Menacherry.
 * 
 */
@Sharable
public class AMF3ToJavaObjectDecoder extends ByteToMessageDecoder implements Transform<ByteBuf, Object>
{
	private static final Logger LOG = LoggerFactory.getLogger(AMF3ToJavaObjectDecoder.class);
	
	@Override
	protected Object decode(ChannelHandlerContext ctx, ByteBuf in)
			throws Exception {
		if(null == in)
		{
			LOG.warn("Incoming message is null");
			return in;
		}
		// buffer.array() will ignore the readerIndex. Hence readBytes is used
		// and then .array is called
		ByteArrayInputStream bis = new ByteArrayInputStream(in.readBytes(
				in.readableBytes()).array());
		return deSerializeObjectFromStream(bis);
	}
	
	@Override
	public Object convert(ByteBuf buffer) throws Exception {
		ByteArrayInputStream bis = new ByteArrayInputStream(buffer.array());
		return deSerializeObjectFromStream(bis);
	}
	
	protected Object deSerializeObjectFromStream(ByteArrayInputStream bis) throws Exception
	{
		AMFDeSerializer serializer = new AMFDeSerializer(SerializationContext
				.getSerializationContext());
		Object o = null;
		try
		{
			// do the deserialization.
			o = serializer.fromAmf(bis);
			LOG.trace("Serialized object: {}",o);
			bis.close();
		}
		catch (IOException e)
		{
			LOG.error("IO error in AMF3ToJavaObjectDecoder: {}",e);
			throw e;
		}
		catch (ClassNotFoundException e)
		{
			LOG.error("Error in AMF3ToJavaObjectDecoder: {}.\n " +
					"Check if flash class has corresponding java class",e);
			throw e;
		}
		return o;
	}

}
