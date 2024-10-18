package edu.javeriana.cad;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import edu.javeriana.cad.beans.SSHConnectionInfo;

public class SSHUtils {

	static {
		JSch.setConfig("StrictHostKeyChecking", "no");
//		JSch.setLogger(new Logger() {
//
//			@Override
//			public boolean isEnabled(int level) {
//				return true;
//			}
//
//			@Override
//			public void log(int level, String message) {
//				String lvlStr = getLevel(level);
//				System.out.println(String.format("%s [%s]\t%s",now(),lvlStr,message));
//				
//			}
//
//			private String now() {
//				DateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
//				return fmt.format(new Date());
//			}
//
//			private String getLevel(int level) {
//				if (level == Logger.DEBUG) {
//					return "DEBUG";
//				} else if (level == Logger.ERROR) {
//					return "ERROR";
//				} else if (level == Logger.FATAL) {
//					return "FATAL";
//				} else if (level == Logger.INFO) {
//					return "INFO";
//				}
//				return "WARN";
//			}
//			
//		});
	}
	
	public static boolean isRunningMachine(SSHConnectionInfo connectionInfo) {
		try {
			executeRemoteCommand(connectionInfo, "ls", false,false);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public static byte[] executeRemoteCommand(SSHConnectionInfo connectionInfo, String commandToExecute, boolean executeWithSudo, boolean writeOutputStdOut)
			throws Exception {
		byte[] result = null;
		Session session = generateSession(connectionInfo);
		ChannelExec channel = (ChannelExec) session.openChannel("exec");
		try {
			StringBuffer fullCommand = new StringBuffer();
			if (executeWithSudo) {
				boolean usePwd = connectionInfo.promptPassword(null);
				if (usePwd) {
					fullCommand.append("echo " + connectionInfo.getPassword() + " | sudo -S ");
				} else {
					fullCommand.append("sudo ");
				}
			}
			fullCommand.append(commandToExecute + "\n");
			
			channel.setCommand(fullCommand.toString());
			channel.setInputStream(null);
			channel.setErrStream(null);

			try (ByteArrayOutputStream outErr = new ByteArrayOutputStream()) {
				try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
					try (InputStream errStr = channel.getErrStream()) {
						try (InputStream inStr = channel.getInputStream()) {
							channel.connect();
							if (writeOutputStdOut) {
								copyWrittingOutput(inStr,out);
							} else {
								IOUtils.copy(inStr, out);
							}
						}
						IOUtils.copy(errStr, outErr);
						
					}
	
					out.flush();
					result = out.toByteArray();
					if (channel.getExitStatus()!=0) {
						outErr.flush();
						byte[] errStr = outErr.toByteArray();
						throw new Exception("Output exit code: " + channel.getExitStatus() + ", errStr:" + new String(errStr));
					}
				}
			}
		} finally {
			channel.disconnect();
			session.disconnect();
		}

		return result;
	}

	private static void copyWrittingOutput(InputStream inStr, OutputStream out) throws IOException{
		int byteRead = inStr.read();
		try(ByteArrayOutputStream line = new ByteArrayOutputStream()) {
			while (byteRead > 0) {
				out.write(byteRead);
				if (byteRead == '\n') {
					line.flush();
					System.out.println(new String(line.toByteArray(), StandardCharsets.UTF_8));
					line.reset();
				} else {
					line.write(byteRead);
				}
				byteRead = inStr.read();
			}
			
			line.flush();
			byte[] data = line.toByteArray();
			if (data.length>0) {
				System.out.println(new String(data, StandardCharsets.UTF_8));
			}
		}
	}

	private static Session generateSession(SSHConnectionInfo connectionInfo) throws Exception {
		JSch jsch = new JSch();
		
		if (StringUtils.isNotBlank(connectionInfo.getPassphrase())) {
			jsch.addIdentity(connectionInfo.getPassphrase());
		}
		
		Session session = jsch.getSession(connectionInfo.getUsername(), connectionInfo.getHost(),
				connectionInfo.getPort());
		session.setUserInfo(connectionInfo);
		session.connect();
		return session;
	}

	public static Path scpRemoteFile(SSHConnectionInfo connectionInfo, String fileToGet) throws Exception {
		String ext = FilenameUtils.getExtension(fileToGet);
		Path tmpOutputFile = Files.createTempFile("tmp", "." + ext);
		
		Session session = generateSession(connectionInfo);
		ChannelExec channel = (ChannelExec) session.openChannel("exec");
		String command = "scp -f " + fileToGet;
		channel.setCommand(command);
		byte[] buf = new byte[1024];

		// get I/O streams for remote scp
		OutputStream out = channel.getOutputStream();
		InputStream in = channel.getInputStream();

		channel.connect();

		// send '\0'
		buf[0] = 0;
		out.write(buf, 0, 1);
		out.flush();

		while (true) {
			int c = checkAck(in);
			if (c != 'C') {
				break;
			}

			// read '0644 '
			in.read(buf, 0, 5);

			long filesize = 0L;
			while (true) {
				if (in.read(buf, 0, 1) < 0) {
					// error
					break;
				}
				if (buf[0] == ' ')
					break;
				filesize = filesize * 10L + (long) (buf[0] - '0');
			}

			//String file = null;
			for (int i = 0;; i++) {
				in.read(buf, i, 1);
				if (buf[i] == (byte) 0x0a) {
//					file = new String(buf, 0, i);
					break;
				}
			}
			// send '\0'
			buf[0] = 0;
			out.write(buf, 0, 1);
			out.flush();

			// read a content of lfile
			try(OutputStream fos = Files.newOutputStream(tmpOutputFile, StandardOpenOption.CREATE)){
				int foo;
				while (true) {
					if (buf.length < filesize)
						foo = buf.length;
					else
						foo = (int) filesize;
					foo = in.read(buf, 0, foo);
					if (foo < 0) {
						// error
						break;
					}
					fos.write(buf, 0, foo);
					filesize -= foo;
					if (filesize == 0L)
						break;
				}
			}

			if (checkAck(in) != 0) {
				System.exit(0);
			}

			// send '\0'
			buf[0] = 0;
			out.write(buf, 0, 1);
			out.flush();
		}

		session.disconnect();

		return tmpOutputFile;
	}

	static int checkAck(InputStream in) throws IOException {
		int b = in.read();
		// b may be 0 for success,
		// 1 for error,
		// 2 for fatal error,
		// -1
		if (b == 0)
			return b;
		if (b == -1)
			return b;

		if (b == 1 || b == 2) {
			StringBuffer sb = new StringBuffer();
			int c;
			do {
				c = in.read();
				sb.append((char) c);
			} while (c != '\n');
			if (b == 1) { // error
				System.out.print(sb.toString());
			}
			if (b == 2) { // fatal error
				System.out.print(sb.toString());
			}
		}
		return b;
	}

	public static String getCadTestingPem() throws IOException {
		try(ByteArrayOutputStream out = new ByteArrayOutputStream()){
			try(InputStream inStr = SSHUtils.class.getResourceAsStream("/cad-testing.pem")){
				IOUtils.copy(inStr, out);
			}
			out.flush();
			return new String(out.toByteArray(),StandardCharsets.UTF_8);
		}
	}
}
