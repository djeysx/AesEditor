package org.djeysx.secureStorage.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CipherOutputStream extends FilterOutputStream {
	protected Cipher cipher;

	public CipherOutputStream(OutputStream out, Cipher cipher) {
		super(out);
		this.cipher = cipher;
	}

	protected CipherOutputStream(OutputStream out) {
		super(out);
	}

	@Override
	public void write(int b) throws IOException {
		out.write(this.cipher.update(new byte[] { (byte) b }));
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		out.write(this.cipher.update(b, off, len));
	}

	@Override
	public void write(byte[] b) throws IOException {
		out.write(this.cipher.update(b));
	}

	@Override
	public void close() throws IOException {
		try {
			out.write(this.cipher.doFinal());
		} catch (IllegalBlockSizeException e) {
			throw new IOException(e);
		} catch (BadPaddingException e) {
			throw new IOException(e);
		}
		flush();
		out.close();
	}

	protected static Cipher createAESCipher(final SecretKeySpec skeySpec, final int mode, final byte[] iv) {
		try {
			Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding");
			IvParameterSpec ivParam = new IvParameterSpec(iv);
			cipher.init(mode, skeySpec, ivParam);
			return cipher;
		} catch (InvalidKeyException e) {
			throw new RuntimeException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (NoSuchPaddingException e) {
			throw new RuntimeException(e);
		} catch (InvalidAlgorithmParameterException e) {
			throw new RuntimeException(e);
		}
	}

	public static byte[] createKey_128(final String password) {
		try {
			byte[] passwordRaw = password.getBytes("UTF-8");
			byte[] md5 = null;
			try {
				md5 = MessageDigest.getInstance("MD5").digest(passwordRaw);
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			}
			return md5;
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static SecretKeySpec createAesKey(final byte[] key128) {
		return new SecretKeySpec(key128, "AES");
	}

	protected static byte[] createIv() {
		return new SecureRandom().generateSeed(16);
	}

	protected byte[] xorArray(byte[] a1, byte[] a2) {
		byte[] res = new byte[a1.length];
		for (int i = 0; i < a1.length; i++)
			res[i] = (byte) (a1[i] ^ a2[i]);

		return res;
	}
}