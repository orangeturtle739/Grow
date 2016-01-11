package grow.action;

import java.io.PrintStream;
import java.util.Scanner;

import grow.Game;
import grow.Scene;

public class Util {
	public static int getRuleNumber(Scene current, Game world, Scanner input, PrintStream output, String prompt) {
		current = new View().act(current, world, input, output);
		int num = -1;
		while (num < 1 || num > current.rules().size()) {
			output.println(prompt);
			String line = input.nextLine();
			try {
				num = Integer.parseInt(line);
			} catch (NumberFormatException e) {
				num = -1;
			}
		}
		return num - 1;
	}
}
