package grow.action;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import exceptions.CanceledException;
import grow.Game;
import grow.Rule;
import grow.Scene;

/**
 * A utility class, full of only static methods, that simplifies collecting user
 * input.
 *
 * @author Jacob Glueck
 *
 */
public class Util {

	/**
	 * The reserved separator character which in not permitted in any input.
	 */
	public static final char RESERVED_SEPERATOR = '`';

	/**
	 * A converter which converts a string to an action, and is null if the
	 * string is not a valid action.
	 */
	public static final Function<String, Action> actionConverter = (str) -> Action.parseAction(str);

	/**
	 * Effect: prints to the output stream a numbered list of things. Each line
	 * in the list starts with an indent, followed by a prefix, the number, the
	 * suffix, and then padding. If the strings for each line are more than one
	 * line each, they are indented.
	 *
	 * @param prefix
	 *            the prefix which appears before the number
	 * @param suffix
	 *            the suffix which appears after the number
	 * @param indent
	 *            the number of spaces before the prefix
	 * @param numSpace
	 *            the length the prefix, number, and suffix should be padded to
	 * @param out
	 *            the output stream
	 * @param list
	 *            the list of things to print
	 */
	public static void printNumberedList(String prefix, String suffix, int indent, int numSpace, PrintStream out, Collection<?> list) {
		int i = 1;
		for (Object o : list) {
			String[] str = o.toString().split("\n");
			if (indent != 0) {
				out.printf("%-" + indent + "s%-" + numSpace + "s %s", "", prefix + i + suffix, str[0]);
			} else {
				out.printf("%-" + numSpace + "s %s", prefix + i + suffix, str[0]);
			}
			out.println();
			i++;
			for (int x = 1; x < str.length; x++) {
				if (indent != 0) {
					out.printf("%-" + indent + "s%-" + numSpace + "s %s", "", "", str[x]);
				} else {
					out.printf("%-" + numSpace + "s %s", "", str[x]);
				}
				out.println();
			}
		}
	}

	/**
	 * Effect: just like
	 * {@link #printNumberedList(String, String, int, int, PrintStream, Collection)}
	 * , but returns a string.
	 *
	 * @param prefix
	 *            the prefix
	 * @param suffix
	 *            the suffix
	 * @param indent
	 *            the indent
	 * @param numSpace
	 *            the padding
	 * @param list
	 *            the list
	 * @return a string
	 */
	public static String printNumberedList(String prefix, String suffix, int indent, int numSpace, Collection<?> list) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		PrintStream print = new PrintStream(bytes);
		printNumberedList(prefix, suffix, indent, numSpace, print, list);
		String result = bytes.toString();
		print.close();
		return result;
	}

	/**
	 * Formats a rule in a nice way
	 *
	 * @param r
	 *            the rule
	 * @return the rule, formatted in a nice way
	 */
	public static String prettyRule(Rule r) {
		StringBuilder result = new StringBuilder();
		if (r.patterns().size() <= 1) {
			result.append("Pattern: " + firstOrNone(r.patterns()) + "\n");
		} else {
			result.append("Patterns:\n");
			result.append(printNumberedList("", ".", 2, 5, r.patterns()));
		}
		if (r.actions().size() <= 1) {
			result.append("Action:  " + firstOrNone(r.actions()) + "\n");
		} else {
			result.append("Actions:\n");
			result.append(printNumberedList("", ".", 2, 5, r.actions()));
		}
		return result.toString();
	}

	/**
	 * @param things
	 *            a list with either 1 or 0 things in it
	 * @return {@code "[none]"} if the list has 0 things in it, or the only
	 *         element if the list has things in it.
	 */
	private static String firstOrNone(Collection<?> things) {
		return things.size() == 0 ? "[none]" : things.iterator().next().toString();
	}

	/**
	 * Effect: tries to read from the input, with a prompt, until the user
	 * cancels or produces valid input
	 *
	 * @param <T>
	 *            the type of input from the user
	 * @param output
	 *            the output stream
	 * @param input
	 *            the input stream
	 * @param prompt
	 *            the prompt to display the first time
	 * @param onError
	 *            the text to display when the converter returns null
	 * @param converter
	 *            a function which parses a string into a T. Returns null if the
	 *            string is invalid.
	 * @return the T the user typed.
	 * @throws CanceledException
	 *             if the user typed {@code :cancel}.
	 */
	public static <T> T read(PrintStream output, Scanner input, String prompt, String onError, Function<String, T> converter) throws CanceledException {
		T result = null;
		// Print nothing if there is not prompt
		if (prompt.length() != 0) {
			output.println(prompt);
		}
		while (result == null) {
			String line = input.nextLine().trim();
			if (line.equals(":cancel")) {
				throw new CanceledException();
			} else if (line.contains(Character.toString(RESERVED_SEPERATOR))) {
				output.println("Your input may not contain " + RESERVED_SEPERATOR + ".");
			} else {
				result = converter.apply(line);
				if (result == null) {
					output.println(onError);
				}
			}
		}
		return result;
	}

	/**
	 * Effect: reads a list of things from the input stream. Each thing is on
	 * its own line. Tries to end the list when the user enters a blank line.
	 *
	 * @param <T>
	 *            the type of thing in the list
	 * @param output
	 *            the output stream
	 * @param input
	 *            the input stream
	 * @param prompt
	 *            the prompt to display the first time
	 * @param onError
	 *            the text to display if one of the items has a bad format
	 * @param converter
	 *            the converter which converts the items
	 * @param onListError
	 *            the text to display if the user types a blank line (to end the
	 *            list), but the list does not pass the predicate.
	 * @param checker
	 *            the predicate which checks the list for completeness.
	 * @return the list
	 * @throws CanceledException
	 *             if the user typed {@code :cancel}.
	 */
	public static <T> List<T> readList(PrintStream output, Scanner input, String prompt, String onError, Function<String, T> converter, String onListError, Predicate<List<T>> checker)
			throws CanceledException {
		List<T> result = new LinkedList<>();

		// Modify the converter to return null when it gets a blank line.
		Function<String, T> modConverter = (str) -> {
			if (str.length() == 0) {
				throw new Blank();
			} else {
				return converter.apply(str);
			}
		};

		while (true) {
			T toAdd = null;
			try {
				toAdd = read(output, input, prompt, onError, modConverter);
			} catch (Blank b) {

			}
			if (toAdd == null) {
				if (checker.test(result)) {
					return result;
				} else {
					output.println(onListError);
				}
			} else {
				result.add(toAdd);
			}
			// Make sure the prompt is only displayed the first time
			prompt = "";

		}
	}

	/**
	 * Represents: an exception thrown when the user types a blank line.
	 *
	 * @author Jacob Glueck
	 */
	private static class Blank extends RuntimeException {
		/**
		 * Default UID.
		 */
		private static final long serialVersionUID = 1L;

	}

	/**
	 * Effect: reads an int from the input stream and keeps asking until the int
	 * is in the specified range.
	 *
	 * @param output
	 *            the output stream
	 * @param input
	 *            the input stream
	 * @param prompt
	 *            the prompt
	 * @param onError
	 *            the text to display when the int is not valid
	 * @param min
	 *            the minimum allowable value
	 * @param max
	 *            the maximum allowable value
	 * @return the int
	 * @throws CanceledException
	 *             if the user typed {@code :cancel}.
	 */
	public static int readInt(PrintStream output, Scanner input, String prompt, String onError, int min, int max) throws CanceledException {
		return read(output, input, prompt, onError, (str) -> {
			try {
				int i = Integer.parseInt(str);
				if (i >= min && i <= max) {
					return i;
				} else {
					return null;
				}
			} catch (NumberFormatException e) {
				return null;
			}
		});
	}

	/**
	 * Effect: gets a rule number (index) for the current scene
	 *
	 * @param prompt
	 *            the prompt
	 * @param output
	 *            the output stream
	 * @param input
	 *            the input stream
	 * @param world
	 *            the world
	 * @return the rule number
	 * @throws CanceledException
	 *             if the user typed {@code :cancel}.
	 */
	public static int getRuleNumber(String prompt, PrintStream output, Scanner input, Game world) throws CanceledException {
		output.println(prompt);
		Util.printNumberedList("", ".", 0, 5, output, world.current().rules().stream().map(r -> Util.prettyRule(r)).collect(Collectors.toList()));
		int num = Util.readInt(output, input, "", "Not a valid rule number.", 1, world.current().rules().size());
		return num - 1;
	}

	/**
	 * Effect: reads a list of space-separated integers from the input stream,
	 * and checks them for validity.
	 *
	 * @param output
	 *            the output stream
	 * @param input
	 *            the input stream
	 * @param onError
	 *            the text to print if the list fails the validity check
	 * @param checker
	 *            the checker which checks the list for validity
	 * @return the integers
	 * @throws CanceledException
	 *             if the user typed {@code :cancel}.
	 */
	public static List<Integer> readInts(PrintStream output, Scanner input, String onError, Predicate<List<Integer>> checker) throws CanceledException {
		List<Integer> result = null;
		while (result == null) {
			String str = read(output, input, "", onError, (s) -> s);
			if (str.length() == 0) {
				result = new ArrayList<>();
			} else {
				String[] in = str.split("\\s");
				result = new ArrayList<>(in.length);
				for (String element : in) {
					try {
						result.add(Integer.parseInt(element));
					} catch (NumberFormatException e) {
						result = null;
						output.println(onError);
						break;
					}
				}
			}
			if (result != null && !checker.test(result)) {
				result = null;
				output.println(onError);
			}
		}
		return result;
	}

	/**
	 * Creates: a predicate which checks to make sure all the integers in a list
	 * are between the specified min and max, and that they are unique.
	 *
	 * @param min
	 *            the minimum value
	 * @param max
	 *            the maximum value
	 * @return the predicate
	 */
	private static Predicate<List<Integer>> intListMinMax(int min, int max) {
		return (l) -> {
			HashSet<Integer> set = new HashSet<>();
			for (Integer i : l) {
				if (i < min || i > max) {
					return false;
				} else {
					set.add(i);
				}
			}
			return set.size() == l.size();
		};
	}

	/**
	 * Effect: just like
	 * {@link #readInts(PrintStream, Scanner, String, Predicate)}, but with a
	 * predicate that ensures that all the ints are unique and between a
	 * specified min and max.
	 *
	 * @param output
	 *            the output stream
	 * @param input
	 *            the input stream
	 * @param onError
	 *            the text to display if the list does not pass the checks.
	 * @param min
	 *            the minimum value
	 * @param max
	 *            the maximum value
	 * @return the list of integers
	 * @throws CanceledException
	 *             if the user typed {@code :cancel}.
	 */
	public static List<Integer> readInts(PrintStream output, Scanner input, String onError, int min, int max) throws CanceledException {
		return readInts(output, input, onError, intListMinMax(min, max));
	}

	/**
	 * Effect: just like
	 * {@link #readInts(PrintStream, Scanner, String, int, int)}, but also makes
	 * sure the number of integers is a certain value.
	 *
	 * @param output
	 *            the output stream
	 * @param input
	 *            the input stream
	 * @param onError
	 *            the text to display if the list does not pass the checks.
	 * @param min
	 *            the minimum value
	 * @param max
	 *            the maximum value
	 * @param count
	 *            the number of values required
	 * @return the list of integers
	 * @throws CanceledException
	 *             if the user typed {@code :cancel}.
	 */
	public static List<Integer> readInts(PrintStream output, Scanner input, String onError, int min, int max, int count) throws CanceledException {
		return readInts(output, input, onError, min, max, count, count);
	}

	/**
	 * Effect: just like
	 * {@link #readInts(PrintStream, Scanner, String, int, int)}, but also makes
	 * sure the number of integers is in the range {@code [minCount, maxCount]}.
	 *
	 * @param output
	 *            the output stream
	 * @param input
	 *            the input stream
	 * @param onError
	 *            the text to display if the list does not pass the checks.
	 * @param min
	 *            the minimum value
	 * @param max
	 *            the maximum value
	 * @param minCount
	 *            the minimum number of values permitted
	 * @param maxCount
	 *            the maximum number of values permitted
	 * @return the list of integers
	 * @throws CanceledException
	 *             if the user typed {@code :cancel}.
	 */
	public static List<Integer> readInts(PrintStream output, Scanner input, String onError, int min, int max, int minCount, int maxCount) throws CanceledException {
		return readInts(output, input, onError, (l) -> {
			return intListMinMax(min, max).test(l) && l.size() >= minCount && l.size() <= maxCount;
		});
	}

	/**
	 * Represents: a task that can be canceled.
	 *
	 * @author Jacob Glueck
	 *
	 */
	public interface CancelTask {
		/**
		 * Effect: runs the task.
		 *
		 * @return the scene as a result of the task
		 * @throws CanceledException
		 *             if the task was canceled. The program must be in a valid
		 *             state.
		 */
		Scene run() throws CanceledException;
	}

	/**
	 * Runs a task that can be canceled, and prints out {@code "Canceled."} and
	 * returns {@code current} if the task is canceled.
	 *
	 * @param current
	 *            the current scene
	 * @param output
	 *            the output stream
	 * @param c
	 *            the task
	 * @return the scene after the task has finished
	 */
	public static Scene handleCancel(Scene current, PrintStream output, CancelTask c) {
		try {
			return c.run();
		} catch (CanceledException e) {
			output.println("Canceled.");
			return current;
		}
	}
}
