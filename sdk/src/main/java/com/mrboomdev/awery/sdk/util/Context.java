package com.mrboomdev.awery.sdk.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Context {

	InputStream openInputStream(File file) throws IOException;

	OutputStream openOutputStream(File file) throws IOException;
}