package utiil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
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
