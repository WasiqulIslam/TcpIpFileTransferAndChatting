//12:36 AM 2/7/2005

public class FileData implements java.io.Serializable
{
   private int length;
   private byte bytes[];
   public FileData( byte [] b, int l )
   {
      bytes = b;
      length = l;
   }
   public int getLength()
   {
      return length;
   }
   public byte[] getData()
   {
      return bytes;
   }
}