package edu.javeriana.cad.beans;

import org.apache.commons.lang3.StringUtils;

import com.jcraft.jsch.UserInfo;

public class SSHConnectionInfo implements UserInfo{
	private boolean debug=false;
	private String host;
	private int port=22;
	private String username;
	private String passphrase;
	private String password;
	
	public SSHConnectionInfo(String host, String username, String passphrase, String password) {
		this.host = host;
		this.username = username;
		this.passphrase = passphrase;
		this.password = password;
	}
	
	public SSHConnectionInfo(String host, int port, String username, String passphrase, String password) {
		this.host = host;
		this.port = port;
		this.username = username;
		this.passphrase = passphrase;
		this.password = password;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
	public boolean isDebug() {
		return debug;
	}
	
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getHost() {
		return host;
	}
	
	public String getUsername() {
		return username;
	}
	
	@Override
	public String getPassphrase() {
		return passphrase;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public boolean promptPassword(String message) {
		return StringUtils.isNotBlank(password);
	}

	@Override
	public boolean promptPassphrase(String message) {
		return StringUtils.isNotBlank(passphrase);
	}

	@Override
	public boolean promptYesNo(String message) {
		return true;
	}

	@Override
	public void showMessage(String message) {
		if (debug) {
			System.out.println(message);
		}
	}

}
