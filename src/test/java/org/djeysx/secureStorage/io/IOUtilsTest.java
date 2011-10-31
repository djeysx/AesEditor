package org.djeysx.secureStorage.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import junit.framework.Assert;

import org.djeysx.secureStorage.io.IOUtils;
import org.junit.Test;

public class IOUtilsTest {

	@Test
	public void test_fileEncrypt() throws IOException {
		File clearFile = new File("src/test/resources/datafile1.txt");
		File cryptedFile = File.createTempFile("test", "crypted");
		cryptedFile.deleteOnExit();
		IOUtils.streamEncryptToFile(new FileInputStream(clearFile), cryptedFile, "my password");
		System.out.println(clearFile.length() + " " + cryptedFile.length());
		System.out.println(clearFile.getCanonicalPath());
		System.out.println(cryptedFile.getCanonicalPath());

	}

	@Test
	public void test_fileCryptDecrypt() throws IOException {
		String password = "my password";
		File clearFile = new File("src/test/resources/datafile1.txt");
		File cryptedFile = File.createTempFile("test", "crypted");
		File recryptedFile = File.createTempFile("test", "recrypted");
		cryptedFile.deleteOnExit();
		recryptedFile.deleteOnExit();
		ByteArrayOutputStream original = new ByteArrayOutputStream();
		IOUtils.copy(new FileInputStream(clearFile), original);
		System.out.println(original.toString("UTF8"));

		IOUtils.streamEncryptToFile(new ByteArrayInputStream(original.toByteArray()), cryptedFile, password);

		ByteArrayOutputStream decrypted = new ByteArrayOutputStream();
		IOUtils.streamDecryptFromFile(cryptedFile, decrypted, password);
		Assert.assertTrue(Arrays.equals(original.toByteArray(), decrypted.toByteArray()));

	}

}
