//12:21 AM 2/7/2005

import java.io.*;

public class Message implements Serializable
{
   private String sender;
   private int messageType;
   private Object message;
   public Message( String s, int t )
   {
      sender = s;
      messageType = t;
   }
   public void setObject( Object o )
   {
      message = o;
   }
   public Object getObject()
   {
      return message;
   }
   public int getMessageType()
   {
      return messageType;
   }
   public String getSendersName()
   {
      return new String( sender );
   }
}