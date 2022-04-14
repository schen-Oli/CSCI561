import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;


//change allies around
public class my_player_minmax {

	private static int side;
	private static int enemy;

	private static int est_steps = 0;

	private static int[][] prev_board;
	private static int[][] curr_board;

	private static int size = 5;

	private static int depth = 6;
	private static int width = 10;

	public static void main(String[] args) {
		prev_board = new int[size][size];
		curr_board = new int[size][size];
		read();
		move();
	}

	private static void read() {
		try {
			// read input
			File input_board = new File("input.txt");
			Scanner scanner = new Scanner(input_board);

			side = scanner.nextInt();
			enemy = side == 1 ? 2 : 1;

			scanner.nextLine();
			for (int i = 0; i < size; i++) {
				String line = scanner.nextLine();
				for (int j = 0; j < size; j++) {
					prev_board[i][j] = line.charAt(j) - '0';
				}
			}

			for (int i = 0; i < size; i++) {
				String line = scanner.nextLine();
				for (int j = 0; j < size; j++) {
					curr_board[i][j] = line.charAt(j) - '0';
					if (curr_board[i][j] != 0) {
						est_steps++;
					}
				}
			}
			scanner.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void write(boolean isPass, int i, int j) {
		try {
			FileWriter output = new FileWriter("output.txt", false);
			if (isPass) {
				output.write("PASS");
			} else {
				output.write(i + "," + j);
			}
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void move() {

		// if is first two steps
		if (est_steps == 0) {
			write(false, 2, 2);
			return;
		} else if (est_steps == 1) {
			if (curr_board[2][2] != 0) {
				write(false, 2, 1);
			} else {
				write(false, 2, 2);
			}
			return;
		}

		// if enemy pass
		if (same_board(prev_board, curr_board)) {
			int scores = score(curr_board, false);
			if (scores > 0) {
				write(true, 0, 0);
				return;
			}
		}

		int[] values = max_value(curr_board, Integer.MIN_VALUE, Integer.MAX_VALUE, 1);
		if (values[0] < 0) {
			write(true, 0, 0);
			return;
		}
		write(false, values[0], values[1]);
		return;
	}

	public static int[] max_value(int[][] board, int alpha, int beta, int level) {
		if (level > depth) {
			return new int[] { -1, -1, score(board, true) };
		}
		
		ArrayList<int[]> valid_moves = find_valid_move(board, side);
		
		if (valid_moves.isEmpty()) {
			return new int[] { -1, -1, score(board, true) };
		}

        valid_moves.sort((o1, o2) -> (o2[2] - o1[2]));
        
        if(valid_moves.get(0)[2] >= 100) {
	    	int[] pos = valid_moves.get(0);
	    	board[pos[0]][pos[1]] = side;
	    	return new int[] {pos[0], pos[1], score(board, true)};
	    }
        
		if (valid_moves.size() > width) {
			while (valid_moves.size() > width) {
				valid_moves.remove(valid_moves.size() - 1);
			}
		}
		
		int[] ret = new int[] { -1, -1, Integer.MIN_VALUE };
		
		for (int[] coord : valid_moves) {
			int[][] tmp_board = copy_board(board);
			tmp_board[coord[0]][coord[1]] = side;
			int captured = captureAll(tmp_board, enemy);
			int[] min_score = min_value(tmp_board, alpha, beta, level + 1);
			int utility = min_score[2] + captured * 5;
			if (ret[2] < min_score[2]) {
				ret[0] = coord[0];
				ret[1] = coord[1];
				ret[2] = utility;
			}
			if (ret[2] >= beta) {
				return ret;
			}
			alpha = Math.max(alpha, ret[2]);
		}
		return ret;
	}

	public static int[] min_value(int[][] board, int alpha, int beta, int level) {
		if (level > depth) {
			return new int[] { -1, -1, score(board, true) };
		}

		ArrayList<int[]> valid_moves = find_valid_move(board, enemy);
		if (valid_moves.isEmpty()) {
			return new int[] { -1, -1, score(board, true) };
		}

        valid_moves.sort((o1, o2) -> (o2[2] - o1[2]));
        
        
		if(valid_moves.get(0)[2] >= 100) {
		    int[] pos = valid_moves.get(0);
		    board[pos[0]][pos[1]] = enemy;
		   	return new int[] {pos[0], pos[1], score(board, true)};
		}
        
		if (valid_moves.size() > width) {
			while (valid_moves.size() > width) {
				valid_moves.remove(valid_moves.size() - 1);
			}
		}
		
		int[] ret = new int[] { -1, -1, Integer.MAX_VALUE };
		
		for (int[] coord : valid_moves) {
			int[][] tmp_board = copy_board(board);
			tmp_board[coord[0]][coord[1]] = enemy;
			int captured = captureAll(tmp_board, side);
			int[] max_score = max_value(tmp_board, alpha, beta, level + 1);
			int utility = max_score[2] + captured * 5;
			if (ret[2] > max_score[2]) {
				ret[0] = coord[0];
				ret[1] = coord[1];
				ret[2] = utility;
			}
			if (ret[2] < alpha) {
				return ret;
			}
			beta = Math.min(beta, ret[2]);
		}
		return ret;
	}
	
	public static ArrayList<int[]> find_valid_move(int[][] board, int player) {
		ArrayList<int[]> ret = new ArrayList<int[]>();
        int[][] weight = new int[][] {
			{0, 0, 0, 0, 0},
			{0, 5, 5, 5, 0},
			{0, 5, 10, 5, 0},
			{0, 5, 5, 5, 0},
			{0, 0, 0, 0, 0}
		};
        
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (is_valid_move(board, i, j, player, false)) {
					int captured = try_capture(board, i, j, player);
					int allies = allies_around(i, j, board, player);
                    if(allies == 4){
                        ret.add(new int[] {i, j, weight[i][j] + captured * 100 + 100});
                    }else{
                        ret.add(new int[] {i, j, weight[i][j] + captured * 100 + allies * 5});
                    }
				}
			}
		}
		return ret;
	}
	
	public static int score(int[][] board, boolean as_utility) {
		int me_cnt = 0, enemy_cnt = 0, me_no_liber = 0, enemy_no_liber = 0;
		if(side == 2) {
			me_cnt = 3;
		}else {
			enemy_cnt = 3;
		}
		
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (board[i][j] == side) {
					me_cnt += 1;
					me_no_liber += (as_utility && !has_liberty(board, i, j, side, new int[size][size])) ? 1 : 0;
				}else if (board[i][j] == enemy) {
					enemy_cnt += 1;
					enemy_no_liber += (as_utility && !has_liberty(board, i, j, enemy, new int[size][size])) ? 1 : 0;
				}
			}
		}
	
		return (me_cnt - enemy_cnt) * 10 + 5 * enemy_no_liber + 2 * me_no_liber;
	}
	
	public static boolean is_valid_move(int[][] board, int i, int j, int player, boolean pass) {

		if (pass) {
			return true;
		}

		// check if in range
		if (!in_range(i, j)) {
			return false;
		}

		// check if empty
		if (board[i][j] != 0) {
			return false;
		}

		// check liberty rule
		int[][] test_board = copy_board(board);
		test_board[i][j] = player;
		if (has_liberty(test_board, i, j, player, new int[5][5])) {
			return true;
		} else {
			int captured = captureAll(test_board, 3-player);
			if (captured == 0) {
				return false;
			} else if (captured == 1 && same_board(prev_board, test_board)) {
				// KO rule
				return false;
			}
		}
		return true;
	}
	
	private static boolean has_liberty(int[][] board, int i, int j, int player, int[][] visited) {

		if (!in_range(i, j) || visited[i][j] != 0) {
			return false;
		}

		visited[i][j] = 1;

		if (board[i][j] == 0) {
			return true;
		}

		if (board[i][j] != player) {
			return false;
		}

		return has_liberty(board, i + 1, j, player, visited) || 
				has_liberty(board, i - 1, j, player, visited) || 
				has_liberty(board, i, j + 1, player, visited) || 
				has_liberty(board, i, j - 1, player, visited);
	}
	
	private static boolean in_range(int i, int j) {
		return i < size && j < size && i >= 0 && j >= 0;
	}
	
	private static int capture(int[][] board, int i, int j, int player) {
		if (!in_range(i, j))
			return 0;
		
		if (board[i][j] == 0)
			return 0;

		if (board[i][j] != player) {
			return 0;
		}

		board[i][j] = 0;
		return 1 + 
				capture(board, i + 1, j, player) + 
				capture(board, i - 1, j, player) + 
				capture(board, i, j + 1, player) + 
				capture(board, i, j - 1, player);
	}

	public static boolean same_board(int[][] b1, int[][] b2) {
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (b1[i][j] != b2[i][j]) {
					return false;
				}
			}
		}
		return true;
	}

	public static int allies_around(int i, int j, int[][] board, int player) {
		int cnt = 0;

		if (in_range(i + 1, j)) { cnt += board[i + 1][j] == player ? 1 : 0; }
		if (in_range(i - 1, j)) { cnt += board[i - 1][j] == player ? 1 : 0; }
		if (in_range(i, j + 1)) { cnt += board[i][j + 1] == player ? 1 : 0; }
		if (in_range(i, j - 1)) { cnt += board[i][j - 1] == player ? 1 : 0; }	
        
        if(cnt > 0) return cnt;
        
		if (in_range(i - 1, j - 1)) { cnt += board[i - 1][j - 1] == player ? 1 : 0; }
		if (in_range(i - 1, j + 1)) { cnt += board[i - 1][j + 1] == player ? 1 : 0; }
		if (in_range(i + 1, j + 1)) { cnt += board[i + 1][j + 1] == player ? 1 : 0; }
		if (in_range(i + 1, j - 1)) { cnt += board[i + 1][j - 1] == player ? 1 : 0; }
		
		return (cnt + 1) / 2;
	}
	
	public static int captureAll(int[][] board, int be_captured) {
		int ret = 0;
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (board[i][j] == be_captured && !has_liberty(board, i, j, be_captured, new int[size][size])) {
					 ret += capture(board, i, j, be_captured);
				} 
			}
		}
		return ret;
	}
	
	private static int try_capture(int[][] board_code, int i, int j, int player) {
		int opponent = 3 - player;
		int[][] board = copy_board(board_code);
		board[i][j] = player;
		
		int captured = 0;

		int rowdown = i + 1;
		int rowup = i - 1;
		int colright = j + 1;
		int colleft = j - 1;
		
		if (rowdown < 5 && 
				board[rowdown][j] == opponent && 
				!has_liberty(board, rowdown, j, opponent, new int[5][5])) {
			captured += capture(board, rowdown, j, opponent);
		}

		if (rowup >= 0 && board[rowup][j] == opponent && !has_liberty(board, rowup, j, opponent, new int[5][5])) {
			captured += capture(board, rowup, j, opponent);
		}

		if (colright < 5 && board[i][colright] == opponent && !has_liberty(board, i, colright, opponent, new int[5][5])) {
			captured += capture(board, i, colright, opponent);
		}

		if (colleft >= 0 && board[i][colleft] == opponent && !has_liberty(board, i, colleft, opponent, new int[5][5])) {
			captured += capture(board, i, colleft, opponent);
		}

		return captured;
	}
	
	//deep copy a board
	public static int[][] copy_board(int[][] board) {
		int size = board.length;
		int[][] ret = new int[size][size];
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				ret[i][j] = board[i][j];
			}
		}
		return ret;
	}
}



