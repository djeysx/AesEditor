package org.djeysx.secureStorage.io;

import java.io.IOException;
import java.io.OutputStream;

import javax.crypto.Cipher;

public class AesEncryptOutputStream extends CipherOutputStream {
	private byte[] iv = createIv();
	private byte[] key;

	/**
	 */
	public AesEncryptOutputStream(OutputStream out, String password) {
		super(out);
		this.key = createKey_128(password);
		this.cipher = createAESCipher(createAesKey(this.key), Cipher.ENCRYPT_MODE, iv);
	}

	protected void checkIv() throws IOException {
		if (this.iv != null) {
			byte[] xoredIv = xorArray(this.iv, this.key);
			super.out.write(xoredIv);
			iv = null;
			key = null;
		}
	}

	@Override
	public void write(int b) throws IOException {
		checkIv();
		super.write(b);
	}

	@Override
	public void write(byte[] b) throws IOException {
		checkIv();
		super.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		checkIv();
		super.write(b, off, len);
	}
}
