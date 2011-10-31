package org.djeysx.secureStorage.io;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import junit.framework.Assert;

import org.djeysx.secureStorage.io.AesDecryptOutputStream;
import org.djeysx.secureStorage.io.AesEncryptOutputStream;
import org.djeysx.secureStorage.io.CipherOutputStream;
import org.junit.Test;

public class CipherOutputStreamTest {

	//@Ignore
	@Test
	public void test_cryptoStream() throws Exception {
		byte[] raw = new byte[128 / 8];
		byte[] password = "my password".getBytes("UTF-8");
		System.arraycopy(password, 0, raw, 0, (password.length <= raw.length ? password.length : raw.length));
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		
		IvParameterSpec ivParam = new IvParameterSpec(new SecureRandom().generateSeed(16));
		
		Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding");
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec,ivParam);
		System.out.println(cipher.getMaxAllowedKeyLength("AES/CFB/NoPadding"));

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		CipherOutputStream cout = new CipherOutputStream(baos, cipher);
		byte[] rawData = "12345678901234567890���".getBytes("UTF-8");
		cout.write(rawData);
		cout.close();
		System.out.println(baos.toString());

		// decode
		cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivParam );
		ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
		CipherOutputStream cout2 = new CipherOutputStream(baos2, cipher);
		cout2.write(baos.toByteArray());
		cout2.close();
		System.out.println(baos2.toByteArray().length);
		System.out.println(new String(baos2.toByteArray(), "UTF-8"));
	}

	@Test
	public void test_AesCipher()throws Exception{
		String password = "my password";
		ByteArrayOutputStream baosCrypted = new ByteArrayOutputStream();
		OutputStream crypter = new AesEncryptOutputStream(baosCrypted, password);
		
		ByteArrayOutputStream baosDecrypted = new ByteArrayOutputStream();
		OutputStream decrypter = new AesDecryptOutputStream(baosDecrypted, password);
		
		byte[] rawData = "12345678901234567890����x".getBytes("UTF-8");
		crypter.write(rawData);
		crypter.close();
		System.out.println(baosCrypted.toString());
		
		decrypter.write(baosCrypted.toByteArray());
		decrypter.close();
		System.out.println(rawData.length+" "+baosCrypted.size()+" "+baosDecrypted.size());
		System.out.println(baosDecrypted.toString("UTF-8"));
		Assert.assertEquals(new String(rawData,"UTF-8"), baosDecrypted.toString("UTF-8"));
	}
	
	@Test
	public void test_xor(){
		CipherOutputStream cout = new CipherOutputStream(null, null);
		byte[] a1={10, (byte)0xaa, (byte)0xff};
		byte[] a2={(byte)0xff,(byte)0xbb, 2 };
		byte[] res = cout.xorArray(a1, a2);
		System.out.println(Arrays.toString(a1));
		System.out.println(Arrays.toString(a2));
		System.out.println(Arrays.toString(res));
		System.out.println(Arrays.toString(cout.xorArray(res, a2)));
		System.out.println( (res[2] & 0x00000ff));
		
	}
}
