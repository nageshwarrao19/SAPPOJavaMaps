/* This Java mapping swaps the payload content into a Email Attachment. The target message type would of type EmailPackage.
The scenario in this case is SFTP to Proxy. Proxy structure is similar to Mail Package. */


package payloadToEmailAttachment;

import com.sap.aii.mapping.api.*;

import com.sap.aii.mapping.api.AbstractTrace;
import com.sap.aii.mapping.api.AbstractTransformation;
import com.sap.aii.mapping.api.StreamTransformationException;
import com.sap.aii.mapping.api.Attachment;
import com.sap.aii.mapping.api.DynamicConfigurationKey;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;


public class PayloadToEmailAttachment extends AbstractTransformation{
	
	TransformationInput transInput = null;
	TransformationOutput transOutput = null;
	

	public static AbstractTrace trace;
	public boolean traceon;
	public static String messageID = "dummyMessageID";
	public static String senderName = "dummySender";
	public static  String sourceFilename = "dummyFileName";
	public static String messageSentTime = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'").format(new Date()); // default current datetime	

		
	public PayloadToEmailAttachment() throws Exception
	{
	}
	
	public void transform(TransformationInput arg0, TransformationOutput arg1) throws StreamTransformationException 
	{
		
		transInput=arg0;
		transOutput=arg1;
		trace = (AbstractTrace) getTrace(); //Capture trace object and write trace for debugging purpose.
		
		// Below properties only available if run from PI.
		messageID = (String) arg0.getInputHeader().getMessageId(); 
		senderName = (String) arg0.getInputHeader().getSenderService();
		messageSentTime = (String) arg0.getInputHeader().getTimeSent();
		sourceFilename = (String) arg0.getDynamicConfiguration().get(DynamicConfigurationKey.create("http://sap.com/xi/XI/System/File","FileName")); //Using Dynamic Config to access Source FileName.
		
		
		traceon = true; //make it false if need to run the program locally. Make it true always before compiling the code and deployment.	
		
		if ( traceon ){	trace.addInfo("Java Mapping Program Started!");}else{System.out.println("Java Mapping Program Started!");}

		this.execute((InputStream) arg0.getInputPayload().getInputStream(), (OutputStream) arg1.getOutputPayload().getOutputStream());

		if ( traceon ){ trace.addInfo("Java Mapping Program Completed!"); }else{ System.out.println("Java Mapping Program Completed!"); }
	}
	
	public void execute(InputStream in, OutputStream out) throws StreamTransformationException {

		try {

			if ( traceon ){	trace.addInfo("Inside the Execute Method!");}else{System.out.println("Inside the Execute Method!");}	
			
			byte[] byarray = new byte[in.available()];
			in.read(byarray);
			
			//Creating attachment only if running on PI Server
			if ( traceon ){	
			Attachment newAttachment = transOutput.getOutputAttachments().create("Attachment", "application/xml;" + "name=\"" + sourceFilename + "\"", byarray);
			transOutput.getOutputAttachments().setAttachment(newAttachment);	
			}
			
			
            // creating XML structure of mail package

			String CRLF = "\r\n";
			String emailPackage= "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + CRLF +
								 "<xim:Mail xmlns:xim=\"http://sap.com/xi/XI/Mail/30\">"+ CRLF +
								 "<Subject>" + "Subject" + "</Subject>" + CRLF +
								 "<From>" + senderName + "</From>" + CRLF +
								 "<To>" + "TargetEmailAddress" + "</To>" + CRLF +
								 "<Content_Type>" + "text/plain" + "</Content_Type>" + CRLF +
								 "<Date>" + messageSentTime + "</Date>" + CRLF +
								 "<Message_ID>" + messageID + "</Message_ID>"+ CRLF +
								 "<Content>" + "Legacy ASN XML (In Transit) from DSP Server !" + CRLF + "File Name -> " +sourceFilename+ "</Content>" + CRLF +
								 "</xim:Mail>" ;				
			
			String output=emailPackage;
								
			System.out.println(output);
	
			out.write(output.getBytes());
			out.flush();
			out.close();

			if (traceon){ 
				trace.addInfo("Outside the Execute Method!");}
			else{ 
				System.out.println("Outside the Execute Method!"); }

			
		} catch (Exception e) {
			
			if( traceon ){ trace.addWarning("[Execute Method]**Error. " + e); }else{ System.out.println("[Execute Method]**Error. " + e); }
		}
	}
	
	public static void main(String[] args) {
		// This method is executed only for local running from NWDS and no effect running from PI mapping program 
		try {
			FileInputStream fin = new FileInputStream("D://BinaryData.txt");
			FileOutputStream fout = new FileOutputStream("BinaryOutputfile.txt");

			PayloadToEmailAttachment instance = new PayloadToEmailAttachment();
			instance.traceon = false; //This will help to print the output instead of trace.			
			instance.execute(fin, fout);
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		} catch (StreamTransformationException e) {

			e.printStackTrace();
		} catch (Exception e) {

			e.printStackTrace();
		}		

	}

}
