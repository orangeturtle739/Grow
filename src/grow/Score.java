package grow;

/**
 * Represents: the score of a game
 *
 * @author Jacob Glueck
 */
public class Score {
	/**
	 * The current score
	 */
	private int score;

	/**
	 * Creates: a new score, equal to 0.
	 */
	public Score() {
		score = 0;
	}

	/**
	 * Effect: adds {@code delta} to the score.
	 *
	 * @param delta
	 *            the amount to change the score by.
	 * @return the updated score.
	 */
	public int increment(int delta) {
		score += delta;
		return score;
	}

	/**
	 * Effect: subtracts {@code delta} from the score.
	 *
	 * @param delta
	 *            the amount to change the score by.
	 * @return the updated score.
	 */
	public int decrement(int delta) {
		return increment(-delta);
	}

	/**
	 * Effect: sets the value of the score to the specified value
	 * 
	 * @param value
	 *            the value
	 * @return the new value.
	 */
	public int set(int value) {
		return score = value;
	}

	/**
	 * @return the current score
	 */
	public int score() {
		return score;
	}

	@Override
	public String toString() {
		return Integer.toString(score);
	}
}
