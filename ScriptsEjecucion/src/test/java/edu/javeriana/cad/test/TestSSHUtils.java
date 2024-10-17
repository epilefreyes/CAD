package edu.javeriana.cad.test;

import org.junit.Test;

import edu.javeriana.cad.SSHUtils;
import edu.javeriana.cad.beans.SSHConnectionInfo;

public class TestSSHUtils {

	@Test
	public void testConnectWithPassword() throws Exception {
		SSHConnectionInfo connInfo = new SSHConnectionInfo("190.131.236.58", 20022, "summar", null, "");
		byte[] result = SSHUtils.executeRemoteCommand(connInfo, "ls -la");
		
		System.out.println("Resultado:");
		System.out.println(new String(result));

	}
}
