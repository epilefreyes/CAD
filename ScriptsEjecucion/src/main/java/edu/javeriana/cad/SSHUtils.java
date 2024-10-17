package edu.javeriana.cad;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import edu.javeriana.cad.beans.SSHConnectionInfo;

public class SSHUtils {
	
	static {
		JSch.setConfig("StrictHostKeyChecking", "no");
	}
	
	public static byte[] executeRemoteCommand(SSHConnectionInfo connectionInfo, String commandToExecute) throws Exception{
		byte[] result = null;
		JSch jsch = new JSch();
		
		Session session=jsch.getSession(connectionInfo.getUsername(), connectionInfo.getHost(), connectionInfo.getPort());
		session.setUserInfo(connectionInfo);
		session.connect();
		ChannelExec channel=(ChannelExec) session.openChannel("exec");

		try {
			channel.setCommand(commandToExecute+ "\n");
			channel.setInputStream(null);
			
			try(ByteArrayOutputStream out = new ByteArrayOutputStream()){
				try(InputStream inStr = channel.getInputStream()){
					channel.connect();
					IOUtils.copy(inStr, out);
				}
				
				out.flush();
				result = out.toByteArray();
			}
		} finally {
		      channel.disconnect();
		      session.disconnect();				
		}
		
		return result;
	}
	
}
