//12:28 AM 2/7/2005

public class FileInfo implements java.io.Serializable
{
   private String fileName;
   private String fileDescription;
   private long fileSize;
   public FileInfo( String n, String d, long s )
   {
      fileName = n;
      fileDescription = d;
      fileSize = s;
   }
   public String getFileName()
   {
      return new String( fileName );
   }
   public String getFileDescription()
   {
      return new String( fileDescription );
   }
   public long getFileSize()
   {
      return fileSize;
   }
}