//6:12 PM 2/6/05 u 11:59 PM 2/6/2005
//completion 1.0 3:10 AM 2/9/2005
//u 3:10 AM 2/9/2005

//programmed by Wasiqul Islam

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.net.*;

public class Connector implements Serializable, ActionListener
{
   private static final int MESSAGE = 0, FILE_SENDING_REQUEST = 1, FILE_SENDING_RESPONSE = 2, FILE_DATA_REQUEST = 3, FILE_DATA_RESPONSE = 4, FILE_SENDING_COMPLETE = 5;

   private String userName;

   private boolean isThisAServer = false;
   private boolean isConnectionEstablished = false;

   private ServerSocket connection;
   private Socket socket;

   private JTextArea messageArea;
   private JButton fileSendingButton;
   private JTextField sendField;
   private JWindow waitWindow;
   private JFrame mainFrame;

   private ObjectInputStream input;
   private ObjectOutputStream output;

   private boolean isFileSending = false;
   private File sendFileName;
   private RandomAccessFile sendPointer;
   private long length;
   private long currentPoint;

   private boolean isFileReceiving = false, isReceivePointerSet = false;
   private File receiveFileName;
   private DataOutputStream receivePointer;

   public Connector()
   {
      JOptionPane.showMessageDialog( mainFrame, "Connector v1.0 (3:10 AM 2/9/2005)\nProgrammed by Wasiqul Islam\ne_mail: islam.wasiqul@gmail.com", "About", JOptionPane.INFORMATION_MESSAGE );
      try
      {
         String a = JOptionPane.showInputDialog( null, "Please type your name below:" );
         if( a != null && !( a.trim() ).equals( "" ) )
         {
            userName = a;
         }
         else
         {
            JOptionPane.showMessageDialog( null, "Can not proceed without a user name.\nPress OK to exit" );
            System.exit( 0 );
         }
         int result = JOptionPane.showConfirmDialog( null, "Do you want to act as a server?\n( click YES to act as server/host and NO to act as client/guest )", "Client/Server", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE );
         if( result == JOptionPane.YES_OPTION )
         {
            int port = Integer.parseInt( JOptionPane.showInputDialog( null, "Please enter a port address( e.g. 9999 )" ) );
            connection = new ServerSocket( port, 2 );
            waitWindow = new JWindow()
            {
               public void paint( Graphics g )
               {
                  g.setColor( Color.white );
                  g.fillRect( 0, 0, 800, 600 );
                  g.setColor( Color.black );
                  g.drawString( "Waiting for a client", 100, 100 );
               }
               public void update( Graphics g )
               {
                  paint( g );
               }
            };
            waitWindow.setSize( 300, 200 );
            waitWindow.setLocation( 100, 100 );
            waitWindow.show();
            new ServerUI().start();
         }
         else
         {
            String ipa = JOptionPane.showInputDialog( null, "Please type Host IP address below( e.g. 127.0.0.1 )" );
            int port = Integer.parseInt( JOptionPane.showInputDialog( null, "Please type Host port address below(e.g. 9999)" ) );
            socket = new Socket( InetAddress.getByName( ipa ), port );
            isConnectionEstablished = true;
            isThisAServer = false;
            setupStreams();
         }
      }
      catch( Exception  e )
      {
         e.printStackTrace();
         System.exit( 0 );
      }
   }
   private void setupStreams()
   {
      try
      {
         output = new ObjectOutputStream( socket.getOutputStream() );
         output.flush();
         input =  new ObjectInputStream( socket.getInputStream() );
         Receiver tmp = new Receiver();
         tmp.setStream( input );
         tmp.start();
         mainFrame = new JFrame( "TCP/IP Single File Transfer and Chatter program" );
         mainFrame.setSize( 400, 300 );
         Container con = mainFrame.getContentPane();
         con.setLayout( new BorderLayout() );
         messageArea = new JTextArea();
         messageArea.setEditable( false );
         con.add(  new JScrollPane( messageArea ), BorderLayout.CENTER );
         JPanel lowerPanel = new JPanel();
         lowerPanel.setLayout( new FlowLayout() );
         sendField = new JTextField( 25 );
         sendField.addActionListener( this );
         lowerPanel.add( sendField );
         fileSendingButton = new JButton( "Send a file" );
         fileSendingButton.addActionListener( this );
         lowerPanel.add( fileSendingButton );
         con.add( lowerPanel, BorderLayout.SOUTH );
         mainFrame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
         mainFrame.show();
      }
      catch( Exception e )
      {
         e.printStackTrace();
         System.exit( 1 );
      }
   }
   public void actionPerformed( ActionEvent event )
   {
      try
      {
         if( event.getSource() == sendField )
         {
            String s = event.getActionCommand();
            Message m = new Message( userName, MESSAGE );
            m.setObject( s );
            output.writeObject( (Object)m );
            output.flush();
            sendField.setText( "" );
            messageArea.append( "\n" + userName + " >>> " + s  );
         }
         else if( event.getSource() == fileSendingButton )
         {
            if( isFileSending )
            {
               JOptionPane.showMessageDialog( mainFrame, "Can't send two files at a time" );
               return;
            }
            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode( JFileChooser.FILES_ONLY );
            int result = fc.showOpenDialog( mainFrame );
            if( result != JFileChooser.APPROVE_OPTION )
               return;
            File fileName = fc.getSelectedFile();
            if( fileName == null || !fileName.exists() || !fileName.canRead() )
               return;
            String desc = JOptionPane.showInputDialog( mainFrame, "Please Add a description for the file below:" );
            sendFileName = fileName;
            FileInfo fi = new FileInfo( fileName.getName(), desc, fileName.length() );
            Message msg = new Message( userName, FILE_SENDING_REQUEST );
            msg.setObject( ( Object ) fi );
            output.writeObject( msg );
            output.flush();
            length = fileName.length();
            currentPoint = 0;
            isFileSending = true;
         }
      }
      catch( Exception e )
      {
         e.printStackTrace();
         System.exit( 1 );
      }
   }
   public static void main( String args[] )
   {
      new Connector();
   }
   private class ServerUI extends Thread
   {
      public void run()
      {
         try
         {
            Connector.this.socket = Connector.this.connection.accept();
            JOptionPane.showMessageDialog( null , "Connection established with:\n" + Connector.this.socket.getInetAddress().getHostAddress() );
            Connector.this.isConnectionEstablished = true;
            Connector.this.isThisAServer = true;
            Connector.this.waitWindow.hide();
            Connector.this.setupStreams();
         }
         catch( Exception e )
         {
            e.printStackTrace();
            System.exit( 1 );
         }
      }
   }
   private class Receiver extends Thread
   {
      private ObjectInputStream in;
      private Message received;
      public void setStream( ObjectInputStream ois )
      {
         in = ois;
      }
      public void run()
      {
         try
         {
            while( true )
            {
               received = ( Message ) input.readObject();
               processMessage();
            }
         }
         catch( Exception e )
         {
            e.printStackTrace();
            System.exit( 1 );
         }
      }
      public void processMessage()
      {
         try
         {
            if( received.getMessageType() == MESSAGE )
            {
               String s = "";
               s += received.getSendersName();
               s += " >>> ";
               s += ( String ) received.getObject();
               messageArea.append( "\n" + s );
            }
            else if( received.getMessageType() == FILE_SENDING_REQUEST )
            {
               if( isFileReceiving )
                  return;
               FileInfo fi = ( FileInfo ) received.getObject();
               int result = JOptionPane.showConfirmDialog( mainFrame, "[ " + received.getSendersName() + " ] wants to send you a file.\nFile Name: " +  fi.getFileName() + "\nFile Description: " + fi.getFileDescription() + "\nFile Size: " + fi.getFileSize() + " bytes.\nDo you want to receive it?", "File receive confirmation", JOptionPane.YES_NO_OPTION );
               Boolean b = new Boolean( false );
               if( result != JOptionPane.YES_OPTION )
               {
                  b = Boolean.FALSE;
               }
               else
               {
                  b = Boolean.TRUE;
                  JFileChooser fc = new JFileChooser();
                  fc.setFileSelectionMode( JFileChooser.FILES_ONLY );
                  result = fc.showSaveDialog( mainFrame );
                  if( result != JFileChooser.APPROVE_OPTION )
                     return;
                  File fileName = fc.getSelectedFile();
                  if( fileName == null || fileName.getName().trim().equals( "" ) )
                     return;
                  receiveFileName =fileName;
                  if( receiveFileName.exists() )
                  {
                     int r = JOptionPane.showConfirmDialog( mainFrame, "Overwrite Existing File?\nFile: " + receiveFileName.getAbsolutePath() , "Overwrite", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE );
                     if( r == JOptionPane.YES_OPTION )
                     {
                        b = Boolean.TRUE;
                     }
                     else
                     {
                        b = Boolean.FALSE;
                     }
                  }
               }
               Message send = new Message( userName, FILE_SENDING_RESPONSE );
               send.setObject( b );
               output.writeObject( send );
               output.flush();
               if( b.booleanValue() )
               {
                  isFileReceiving = true;               
               }
               else
               {
                  isFileReceiving = false;
               }
            }
            else if( received.getMessageType() == FILE_SENDING_RESPONSE )
            {
               if( !isFileSending )
                  return;
               sendPointer = new RandomAccessFile( sendFileName, "r" );
               sendFile();
            }
            else if( received.getMessageType() == FILE_DATA_REQUEST )
            {
               if( !isFileReceiving )
                  return;
               if( !isReceivePointerSet )
               {
                  receivePointer = new DataOutputStream( new FileOutputStream( receiveFileName ) );
                  isReceivePointerSet = true;
               }
               receiveFile();
            }
            else if( received.getMessageType() == FILE_DATA_RESPONSE )
            {
               if( !isFileSending )
                  return;
               sendFile();
            }
            else if( received.getMessageType() == FILE_SENDING_COMPLETE )
            {
               receivePointer.close();
               JOptionPane.showMessageDialog( mainFrame, "File fully received, Name: " + receiveFileName.getAbsolutePath(), "Transfer Complete", JOptionPane.INFORMATION_MESSAGE );
               receiveFileName = null;
               isReceivePointerSet = false;
               isFileReceiving = false;
            }
         }
         catch( Exception e )
         {
            e.printStackTrace();
            System.exit( 1 );
         }
      }
      private void receiveFile()
      {
         try
         {
            FileData fd = ( FileData ) received.getObject();
            receivePointer.write( fd.getData(), 0, fd.getLength() );
            Message send = new Message( userName, FILE_DATA_RESPONSE );
            send.setObject( null );
            output.writeObject( send );
            output.flush();
         }
         catch( Exception e )
         {
            e.printStackTrace();
            System.exit( 1 );
         }
      }
      private void sendFile()
      {
         try
         {
            sendPointer.seek( currentPoint );
            if( currentPoint == length )
            {
               Message send = new Message( userName, FILE_SENDING_COMPLETE );
               send.setObject( null );
               output.writeObject( send );
               output.flush();
               currentPoint = 0;
               length = 0;
               sendPointer.close();
               sendFileName = null;
               isFileSending = false;
            }
            else if( ( currentPoint + 100 ) < length )
            {
               byte b[] = new byte[ 100 ];
               sendPointer.readFully( b, 0,100);
               Message send = new Message( userName, FILE_DATA_REQUEST );
               FileData fd = new FileData( b, 100 );
               send.setObject( fd );
               output.writeObject( send );
               output.flush();
               currentPoint += 100;
            }
            else if( ( currentPoint + 100 ) == length )
            {
               byte b[] = new byte[ 100 ];
               sendPointer.readFully( b, 0, 100);
               Message send = new Message( userName, FILE_DATA_REQUEST );
               FileData fd = new FileData( b, 100 );
               send.setObject( fd );
               output.writeObject( send );
               output.flush();
               currentPoint = length;
            }
            else if( ( currentPoint + 100 ) > length )
            {
               byte b[] = new byte[ 100 ];
               sendPointer.readFully( b, 0, ( int ) ( length - currentPoint ) );
               Message send = new Message( userName, FILE_DATA_REQUEST );
               FileData fd = new FileData( b, ( int ) ( length - currentPoint ) );
               send.setObject( fd );
               output.writeObject( send );
               output.flush();
               currentPoint = length;
            }
         }
         catch( Exception e )
         {
            e.printStackTrace();
            System.exit( 1 );
         }
      }
   }
}