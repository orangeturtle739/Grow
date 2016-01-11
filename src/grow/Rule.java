package grow;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import exceptions.SyntaxError;
import grow.action.Action;

/**
 * Represents: a rule in the grow game.
 *
 * @author Jacob Glueck
 *
 */
public class Rule {

	/**
	 * The delimeter used when saving lists
	 */
	public static final String SAVE_DELIMETER = "`";

	/**
	 * The set of strings this rule matches.
	 */
	private final Set<String> toMatch;
	/**
	 * The action to do when a match is found
	 */
	private final List<Action> toDo;

	/**
	 * Creates: a new rule with the specified action and matches.
	 *
	 * @param toDo
	 *            the action
	 * @param toMatch
	 *            the matches
	 */
	public Rule(List<Action> toDo, Set<String> toMatch) {
		this.toMatch = toMatch;
		this.toDo = toDo;
	}

	/**
	 * Creates: a new rule with no matches and the specified action
	 *
	 * @param toDo
	 *            the action
	 */
	private Rule(List<Action> toDo) {
		this(toDo, new HashSet<>());
	}

	/**
	 * Creates: a new rule with the specified action and matches
	 *
	 * @param toDo
	 *            the action
	 * @param toMatch
	 *            the matches
	 */
	public Rule(List<Action> toDo, String... toMatch) {
		this(toDo);
		this.toMatch.addAll(Arrays.asList(toMatch));
	}

	/**
	 * @return the set of strings to match
	 */
	public Set<String> toMatch() {
		return toMatch;
	}

	/**
	 * @return the action to do.
	 */
	public List<Action> toDo() {
		return toDo;
	}

	/**
	 * Determines if this rule matches the input. All matches are case
	 * insensitive.
	 *
	 * @param input
	 *            the input
	 * @return true only if this rule should execute.
	 */
	public boolean matches(String input) {
		// If this rule has no actions, it is equivalent to not matching
		// anything, since it will not do anything.
		if (toDo.size() == 0) {
			return false;
		}
		for (String str : toMatch) {
			if (Pattern.compile(".*\\b(" + str.toUpperCase() + ")\\b.*").matcher(input.toUpperCase()).matches()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "[" + String.join(SAVE_DELIMETER, toMatch) + "] -> [" + toDo.stream().map(object -> object.toString()).collect(Collectors.joining(SAVE_DELIMETER)) + "]";
	}

	/**
	 * Effect: adds a pattern to the set of patterns.
	 *
	 * @param str
	 *            the pattern.
	 */
	public void addPattern(String str) {
		toMatch.add(str);
	}

	/**
	 * Effect: adds an action to the end of the list of actions
	 *
	 * @param a
	 *            the action to add
	 */
	public void addAction(Action a) {
		toDo.add(a);
	}

	/**
	 * @return the patterns.
	 */
	public Set<String> patterns() {
		return toMatch;
	}

	/**
	 * @return the actions
	 */
	public List<Action> actions() {
		return toDo;
	}

	/**
	 * Parses a rule from a string
	 *
	 * @param rule
	 *            the rule
	 * @param line
	 *            the line number to use for the syntax error
	 * @return the rule
	 * @throws SyntaxError
	 *             if there is a problem
	 */
	public static Rule parseRule(String rule, int line) throws SyntaxError {
		String[] split = rule.split("->");
		if (split.length != 2) {
			throw new SyntaxError(line, "Rule format error. No ->");
		}
		String[] cl = parseList(split[0], line);
		Set<String> conditions = new HashSet<>();
		for (String cond : cl) {
			if (cond.length() != 0) {
				conditions.add(cond);
			}
		}

		String[] al = parseList(split[1], line);
		List<Action> actions = new LinkedList<>();
		for (String a : al) {
			// Handle no action
			if (a.length() != 0) {
				Action action = Action.parseAction(a);
				if (action == null) {
					throw new SyntaxError(line, "Action not valid: " + a);
				}
				actions.add(action);
			}
		}

		return new Rule(actions, conditions);
	}

	/**
	 * Input: a list that starts with [ and ends with ] in which the elements
	 * are delimited by {@link #SAVE_DELIMETER}.<br>
	 * Output: a list of the elements
	 *
	 * @param str
	 *            the input
	 * @param line
	 *            the line number to use for the syntax error
	 * @return the list
	 * @throws SyntaxError
	 *             if there is a problem
	 */
	private static String[] parseList(String str, int line) throws SyntaxError {
		str = str.trim();
		if (!str.startsWith("[") && !str.endsWith("]")) {
			throw new SyntaxError(line, "Rule condition list not enclosed by [].");
		}
		return str.substring(1, str.length() - 1).split(SAVE_DELIMETER);
	}
}
