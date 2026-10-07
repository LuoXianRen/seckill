package com.example.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 使用普通字节流把资源文件从源目录拷贝到输出目录。
 * 该方式不依赖 Files.copy / RandomAccessFile，在部分云盘/网络文件系统上更稳定。
 */
@Mojo(name = "stream-copy", defaultPhase = LifecyclePhase.PROCESS_RESOURCES, threadSafe = true)
public class StreamCopyMojo extends AbstractMojo {

    /** 资源源目录，例如 src/main/resources */
    @Parameter(property = "sourceDirectory", required = true)
    private File sourceDirectory;

    /** 输出目录，例如 target/classes */
    @Parameter(property = "outputDirectory", required = true)
    private File outputDirectory;

    @Override
    public void execute() throws MojoExecutionException {
        if (sourceDirectory == null || !sourceDirectory.exists() || !sourceDirectory.isDirectory()) {
            getLog().info("skip: source directory not exists -> " + sourceDirectory);
            return;
        }
        if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
            throw new MojoExecutionException("Cannot create output directory: " + outputDirectory);
        }
        int copied = copyDirectory(sourceDirectory, outputDirectory);
        getLog().info("stream-copy copied " + copied + " resource(s) to " + outputDirectory);
    }

    private int copyDirectory(File srcDir, File outDir) throws MojoExecutionException {
        int count = 0;
        File[] children = srcDir.listFiles();
        if (children == null) {
            return 0;
        }
        for (File child : children) {
            File target = new File(outDir, child.getName());
            if (child.isDirectory()) {
                if (!target.exists() && !target.mkdirs()) {
                    throw new MojoExecutionException("Cannot create directory: " + target);
                }
                count += copyDirectory(child, target);
            } else if (child.isFile()) {
                streamCopy(child, target);
                count++;
            }
        }
        return count;
    }

    private void streamCopy(File from, File to) throws MojoExecutionException {
        File parent = to.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new MojoExecutionException("Cannot create directory: " + parent);
        }
        byte[] buffer = new byte[8192];
        try (InputStream in = new BufferedInputStream(new FileInputStream(from));
             OutputStream out = new BufferedOutputStream(new FileOutputStream(to))) {
            int n;
            while ((n = in.read(buffer)) != -1) {
                out.write(buffer, 0, n);
            }
        } catch (IOException e) {
            throw new MojoExecutionException("stream copy failed: " + from + " -> " + to, e);
        }
    }
}
