package com.photostickers.helpers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface FileIO
{
public InputStream readAsset(String fileName) throws IOException;
public InputStream readFile(String fileName) throws IOException;
public OutputStream writeFile(String fileName) throws IOException;
public boolean fileExsist(String fileName);
public File returnFile(String fileName);
}
