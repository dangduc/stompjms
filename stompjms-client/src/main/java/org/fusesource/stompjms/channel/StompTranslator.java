/**
 * Copyright (C) 2010-2011, FuseSource Corp.  All rights reserved.
 *
 *     http://fusesource.com
 *
 * The software in this package is published under the terms of the
 * CDDL license a copy of which has been included with this distribution
 * in the license.txt file.
 */

package org.fusesource.stompjms.channel;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import org.fusesource.hawtbuf.AsciiBuffer;
import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.BufferInputStream;
import org.fusesource.hawtbuf.DataByteArrayOutputStream;
import org.fusesource.stompjms.StompJmsDestination;
import org.fusesource.stompjms.StompJmsExceptionSupport;
import org.fusesource.stompjms.message.*;
import org.fusesource.stompjms.util.ClassLoadingAwareObjectInputStream;

import javax.jms.JMSException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import static org.fusesource.stompjms.channel.Stomp.*;
import static org.fusesource.hawtbuf.Buffer.*;


/**
 * Frame translator implementation that uses XStream to convert messages to and
 * from XML and JSON
 */
public class StompTranslator {

    public static Object readObjectFromBuffer(Buffer buffer) throws JMSException {
        try {
            BufferInputStream bufferIn = new BufferInputStream(buffer);
            ClassLoadingAwareObjectInputStream is = new ClassLoadingAwareObjectInputStream(bufferIn);
            return is.readObject();
        } catch (Exception e) {
            throw new JMSException("Could not decode: "+e);
        }
    }

    public static Buffer writeBufferFromObject(Object object) throws JMSException {
        try {
            DataByteArrayOutputStream buffer = new DataByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(buffer);
            out.writeObject(object);
            out.close();
            return buffer.toBuffer();
        } catch (IOException e) {
            throw new JMSException("Could not encode: "+e);
        }
    }

//    public static Object readObjectFromBuffer(Buffer buffer) {
//        BufferInputStream bufferIn = new BufferInputStream(buffer);
//        HierarchicalStreamReader in = new JettisonMappedXmlDriver().createReader(bufferIn);
//        XStream xs = new XStream();
//        return xs.unmarshal(in);
//    }
//
//    public static Buffer writeBufferFromObject(Object object) {
//        DataByteArrayOutputStream buffer = new DataByteArrayOutputStream();
//        HierarchicalStreamWriter out = new JettisonMappedXmlDriver().createWriter(buffer);
//        XStream xs = new XStream();
//        xs.marshal(object, out);
//        return buffer.toBuffer();
//    }

    public static StompJmsMessage convert(StompFrame frame) throws JMSException {
        Map<AsciiBuffer, AsciiBuffer> headers = frame.headers;
        StompJmsMessage result = null;
        AsciiBuffer type = headers.get(TRANSFORMATION);
        if (type != null) {
            switch (StompJmsMessage.JmsMsgType.valueOf(type.toString())) {
                case BYTES:
                    result = new StompJmsBytesMessage();
                    break;
                case MAP:
                    result = new StompJmsMapMessage();
                    break;
                case OBJECT:
                    result = new StompJmsObjectMessage();
                    break;
                case STREAM:
                    result = new StompJmsStreamMessage();
                    break;
                case MESSAGE:
                    result = new StompJmsMessage();
                    break;
                default:
                    result = new StompJmsTextMessage();
            }
        }
        if (result == null) {
            result = new StompJmsMessage();
        }
        result.setFrame(frame);
        return result;
    }

    static public String toString(AsciiBuffer buffer) {
        if( buffer == null ) {
            return null;
        } else {
            return buffer.toString();
        }
    }

}
