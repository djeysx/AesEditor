package org.djeysx.secureStorage.io;

import java.io.IOException;
import java.io.OutputStream;

import javax.crypto.Cipher;

public class AesDecryptOutputStream extends CipherOutputStream {
	private byte[] iv = new byte[16];
	private int ivCount = 0;
	private String password;

	/**
	 */
	public AesDecryptOutputStream(OutputStream out, String password) {
		super(out);
		this.password = password;
	}

	protected int checkIv(byte[] b, int off, int len) throws IOException {
		if (iv != null) {
			for (int i = 0; i < len; i++) {
				byte ivb = b[off + i];
				iv[ivCount++] = ivb;
				if (ivCount == iv.length) {
					byte[] key = createKey_128(password);
					this.cipher = createAESCipher(createAesKey(key), Cipher.DECRYPT_MODE, xorArray(iv, key));
					iv = null;
					password = null;
					return i + 1;
				}
			}
		}
		return 0;
	}

	@Override
	public void write(int b) throws IOException {
		if (checkIv(new byte[] { (byte) b }, 0, 1) == 0)
			super.write(b);
	}

	@Override
	public void write(byte[] b) throws IOException {
		int offIv = checkIv(b, 0, b.length);
		if (offIv == 0)
			super.write(b);
		else
			super.write(b, offIv, b.length - offIv);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		int offIv = checkIv(b, off, len);
		if (offIv == 0)
			super.write(b, off, len);
		else
			super.write(b, off + offIv, len - offIv);
	}
}
