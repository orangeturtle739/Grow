package util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Represents: a zip file with internal files that can be read and written.
 *
 * @author Jacob Glueck
 *
 */
public class ZipLocker {

	/**
	 * The zip file
	 */
	private final FileSystem fs;
	/**
	 * The root file
	 */
	private final File root;

	/**
	 * Creates: a new zip locker at the specified root directory.
	 *
	 * @param root
	 *            the root directory.
	 * @throws IOException
	 *             if there is a problem
	 */
	public ZipLocker(File root) throws IOException {
		// Makes it create the file if it is not there.
		Map<String, String> env = new HashMap<>();
		env.put("create", "true");
		fs = FileSystems.newFileSystem(URI.create("jar:" + root.toURI().toString()), env);
		this.root = root;
	}

	/**
	 * Effect: copies all the files from one ZipLocker into this locker.
	 *
	 * @param other
	 *            the other locker
	 * @throws IOException
	 *             if it fails
	 */
	public void copy(ZipLocker other) throws IOException {
		Files.walkFileTree(other.fs.getPath(other.rootFileName()), new FileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				String[] internalPath = new String[file.getNameCount() - 1];
				for (int x = 0; x < internalPath.length; x++) {
					internalPath[x] = file.getName(x + 1).toString();
				}
				Path to = fs.getPath(rootFileName(), internalPath);
				Files.createDirectories(to.toAbsolutePath().getParent());
				Files.copy(file, to);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}
		});
	}

	/**
	 * Opens a stream to write to the internal directory.
	 *
	 * @param parts
	 *            the parts of the file path.
	 *
	 * @return the stream
	 * @throws IOException
	 *             if there is a problem
	 */
	public OutputStream write(String... parts) throws IOException {
		Path path = fs.getPath(rootFileName(), parts);
		Files.createDirectories(path.toAbsolutePath().getParent());
		return Files.newOutputStream(path, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
	}

	/**
	 * Opens a stream to read from the internal file.
	 *
	 * @param more
	 *            the parts of the file path.
	 * @return the stream
	 * @throws IOException
	 *             if there is a problem
	 * @throws NoSuchFileException
	 *             if the file could not be found.
	 */
	public InputStream read(String... more) throws IOException {
		return Files.newInputStream(fs.getPath(rootFileName(), more));
	}

	/**
	 * Constructs a URI to a specified file. The file may or may not exist.
	 * 
	 * @param more
	 *            the path to the file
	 * @return the URI
	 */
	public URI getURI(String... more) {
		return fs.getPath(rootFileName(), more).toUri();
	}

	/**
	 * Effect: deletes a file from the ZipLocker, if it exists. Not sure what
	 * happens if the file does not exist.
	 *
	 * @param more
	 *            the path to the file
	 * @throws IOException
	 *             if there is a problem
	 */
	public void delete(String... more) throws IOException {
		Files.delete(fs.getPath(rootFileName(), more));
	}

	/**
	 * Effect: closes the ZipLocker. Must be called, otherwise the file system
	 * gets corrupted.
	 *
	 * @throws IOException
	 *             if there is a problem.
	 */
	public void close() throws IOException {
		fs.close();
	}

	/**
	 * @return the name of the root file without the .zip extension
	 */
	private String rootFileName() {
		return root.getName().substring(0, root.getName().lastIndexOf('.'));
	}

	/**
	 * For testing
	 *
	 * @param args
	 *            fun
	 * @throws IOException
	 *             bad
	 */
	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {

		ZipLocker test = new ZipLocker(new File("test.zip"));
		PrintStream out = new PrintStream(test.write("bob", "cow", "test.txt"));
		out.println("I love cows!!!!");
		out.close();
		out = new PrintStream(test.write("egg.txt"));
		out.println("Alligators!!");
		out.close();
		out = new PrintStream(test.write("zack", "carl.txt"));
		out.println("Carlz!!!");
		out.close();
		System.out.println(new Scanner(test.read("bob", "cow", "test.txt")).nextLine());
		System.out.println(new Scanner(test.read("egg.txt")).nextLine());
		test.close();
	}
}
