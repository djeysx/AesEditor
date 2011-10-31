package org.djeysx.secureStorage.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtils {

	public static long copy(InputStream in, OutputStream out) throws IOException {
		long byteRead = 0;
		byte[] buffer = new byte[2048];
		int read = 0;
		while ((read = in.read(buffer)) > 0) {
			byteRead += read;
			out.write(buffer, 0, read);
		}
		out.flush();
		return byteRead;
	}

	public static long streamEncryptToFile(final InputStream inStream, final File outFile, final String password)
			throws IOException {
		FileOutputStream fos = new FileOutputStream(outFile);
		AesEncryptOutputStream out = new AesEncryptOutputStream(fos, password);
		long written = copy(inStream, out);
		out.close();
		fos.close();
		return written;
	}

	public static long streamDecryptFromFile(final File inFile, final OutputStream outStream, final String password)
			throws IOException {
		FileInputStream fis = new FileInputStream(inFile);
		AesDecryptOutputStream out = new AesDecryptOutputStream(outStream, password);
		long written = copy(fis, out);
		fis.close();
		out.close();
		return written;
	}

}
