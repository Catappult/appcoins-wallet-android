/*
 * Copyright (c) 2016.
 * Modified on 04/08/2016.
 */

package com.appcoins.wallet.core.network.eskills.downloadmanager.utils;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import kotlinx.coroutines.flow.Flow;
import org.reactivestreams.Publisher;

/**
 * Created by trinkes on 5/18/16.
 */
public class FileUtils {
  public static final String MOVE = "Move";
  public static final String COPY = "Copy";
  private static final String TAG = FileUtils.class.getSimpleName();
  private Consumer<String> sendFileMoveEvent;

  public FileUtils() {
  }

  public static boolean removeFile(String filePAth) {
    boolean toReturn = false;
    if (!TextUtils.isEmpty(filePAth)) {
      toReturn = new File(filePAth).delete();
    }
    return toReturn;
  }

  public static void createDir(String path) {
    File dir = new File(path);
    if (!dir.exists()) {
      dir.mkdirs();
    }
  }

  public static boolean saveBitmapToFile(File dir, String fileName, Bitmap bm,
      Bitmap.CompressFormat format, int quality) {

    File imageFile = new File(dir, fileName);

    FileOutputStream fos = null;
    try {
      dir.mkdirs();
      fos = new FileOutputStream(imageFile);

      bm.compress(format, quality, fos);

      fos.close();

      return true;
    } catch (IOException e) {
      Log
          .e(TAG, e.getMessage());
      if (fos != null) {
        try {
          fos.close();
        } catch (IOException e1) {
          Log
              .e(TAG, e1.getMessage());
        }
      }
    }
    return false;
  }

  public static boolean fileExists(String path) {
    return !TextUtils.isEmpty(path) && new File(path).exists();
  }

  public long deleteDir(File dir) {
    if (dir == null) {
      throw new RuntimeException("The file to be deleted can't be null");
    }
    long size = 0;
    if (dir.isDirectory()) {
      File[] children = dir.listFiles() == null ? new File[0] : dir.listFiles();
      for (File child : children) {
        size += deleteDir(child);
      }
    }
    size += dir.length();
    if (!dir.exists() || dir.delete()) {
      return size;
    } else {
      throw new RuntimeException("Something went wrong while deleting the file "
          + dir.getPath()
          + " (if the is the file a directory, is it empty?");
    }
  }

  /**
   * Return the size of a directory in bytes
   */
  public long dirSize(File dir) {

    long result = 0;
    if (dir.exists()) {
      File[] fileList = dir.listFiles();
      if (fileList != null) {
        for (int i = 0; i < fileList.length; i++) {
          // Recursive call if it's a directory
          if (fileList[i].isDirectory()) {
            result += dirSize(fileList[i]);
          } else {
            // Sum the file size in bytes
            result += fileList[i].length();
          }
        }
      }
    }
    return result;
  }

  /**
   * Method used to copy files from <code>inputPath</code> to <code>outputPath</code> <p>If any
   * exception occurs,
   * both
   * input and output files will be deleted</p>
   *
   * @param inputPath Path to the directory where the file to be copied is
   * @param outputPath Path to the directory where the file should be copied
   * @param fileName Name of the file to be copied
   */
  public void copyFile(String inputPath, String outputPath, String fileName) {
    if (!inputPath.equals(outputPath)) {
      if (!fileExists(inputPath + fileName)) {
        throw new RuntimeException("Input file(" + inputPath + fileName + ") doesn't exists");
      }

      File file = new File(inputPath + fileName);
      if (!file.renameTo(new File(outputPath + fileName))) {
        cloneFile(inputPath, outputPath, fileName);
      }
    }
  }

  /**
   * this method clones a file, it opens the file and using a stream, the new file will be written
   *
   * @param inputPath Path to the directory where the file to be copied is
   * @param outputPath Path to the directory where the file should be copied
   * @param fileName Name of the file to be copied
   */
  public void cloneFile(String inputPath, String outputPath, String fileName) {
    InputStream in = null;
    OutputStream out = null;
    try {

      //create output directory if it doesn't exist
      File dir = new File(outputPath);
      if (!dir.exists()) {
        dir.mkdirs();
      }

      in = new FileInputStream(inputPath + "/" + fileName);
      out = new FileOutputStream(outputPath + "/" + fileName);

      byte[] buffer = new byte[1024];
      int read;
      while ((read = in.read(buffer)) != -1) {
        out.write(buffer, 0, read);
      }
      in.close();

      // write the output file (You have now copied the file)
      out.flush();
      out.close();
      new File(inputPath + fileName).delete();
    } catch (Exception e) {
      File inputFile = new File(inputPath + "/" + fileName);
      if (inputFile.exists()) {
        inputFile.delete();
      }
      File outputFile = new File(outputPath + "/" + fileName);
      if (outputFile.exists()) {
        outputFile.delete();
      }
      Log
          .e(TAG, e.getMessage());
      //				toReturn = false;
      throw new RuntimeException(e);
    } finally {
      in = null;
      out = null;
    }
  }

  public Flowable<Long> deleteFolder(File... folders) {
    return Flowable.fromArray(folders)
        .observeOn(Schedulers.io())
        .flatMap(filePath -> Flowable.fromCallable(() -> {
          long size = deleteDir(filePath);
          Log
              .d(TAG, "deleting folder " + filePath.getPath() + " size: " + size);
          return size;
        })
            .onErrorResumeNext(throwable -> (Publisher) Flowable.empty()))
        .toList()
        .map(deletedSizes -> {
          long size = 0;
          for (int i = 0; i < deletedSizes.size(); i++) {
            size += deletedSizes.get(i);
          }
          return size;
        }).toFlowable();
  }

  public Flowable<Long> deleteFolder(String... folders) {
    File[] files = new File[folders.length];
    for (int i = 0; i < folders.length; i++) {
      files[i] = new File(folders[i]);
    }
    return deleteFolder(files);
  }
}
