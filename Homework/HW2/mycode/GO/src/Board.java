import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Board {
	public int BOARD_SIZE = 5;

	public final static int ONGOING = -1;
	public final static int DRAW = 0;
	public final static int B_WIN = 1;
	public final static int W_WIN = 2;

	public int[][] state;
	public int[][] previous_state;

	public boolean show_board;
	public boolean show_result;
	public int game_result;
	public int steps;
	public boolean wasPass;
	public int captured;
	public int invalid_move_white;
	public int invalid_move_black;
	public int white_stones;
	public int black_stones;
	public boolean isKO = false;
	
	public Board(int[][] state, boolean show_board, boolean show_result) {
		if (state != null) {
			this.state = new int[this.BOARD_SIZE][this.BOARD_SIZE];
			for (int i = 0; i < this.BOARD_SIZE; i++) {
				for (int j = 0; j < this.BOARD_SIZE; j++) {
					this.state[i][j] = state[i][j];
				}
			}
		} else {
			this.state = new int[this.BOARD_SIZE][this.BOARD_SIZE];
		}
		this.previous_state = new int[this.BOARD_SIZE][this.BOARD_SIZE];
		this.show_board = show_board;
		this.show_result = show_result;
		this.game_result = Board.ONGOING;
		
		this.steps = 0;
		this.wasPass = false;
		this.captured = 0;
		this.invalid_move_black = 0;
		this.invalid_move_white = 0;
		
		this.white_stones = 0;
		this.black_stones = 0;
	}

	public int[][] copy_board(int[][] board){
		int size = board.length;
		int[][] ret = new int[size][size];
		for(int i = 0; i < size; i++) {
			for(int j = 0; j < size; j++) {
				ret[i][j] = board[i][j];
			}
		}
		return ret;
	}
	
	public void cleanup() {
		this.state = new int[this.BOARD_SIZE][this.BOARD_SIZE];
		this.game_result = Board.ONGOING;	
		this.steps = 0;
		this.wasPass = false;
		this.captured = 0;
		this.invalid_move_black = 0;
		this.invalid_move_white = 0;
		this.white_stones = 0;
		this.black_stones = 0;
	}

	public String encode_state() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < this.BOARD_SIZE; i++) {
			for (int j = 0; j < this.BOARD_SIZE; j++) {
				sb.append(this.state[i][j]);
			}
		}
		return sb.toString();
	}

	public boolean checkRange(int row, int col) {
		return row >= 0 && row < this.BOARD_SIZE && col >= 0 && col < this.BOARD_SIZE;
	}

	public boolean is_valid_move(int player, int row, int col) {
		
		this.isKO = false;
		//check in range
		if (!checkRange(row, col)) {
			return false;
		}

		// check is empty
		if (this.state[row][col] != 0) {
			return false;
		}

		// check liberty rule
		int[][] test_board = this.copy_board(this.state);
		test_board[row][col] = player;
		if (this.has_liberty(test_board, row, col, player, new int[this.BOARD_SIZE][this.BOARD_SIZE])){
			return true;
		}else{
			int captured_stone = 0;
			int enemy = 3 - player;
			
			if (!this.has_liberty(test_board, row + 1, col, enemy, new int[5][5])) {
				captured_stone += capture(test_board, row + 1, col, enemy);
			}

			if (!this.has_liberty(test_board, row - 1, col, enemy, new int[5][5])) {
				captured_stone += capture(test_board, row - 1, col, enemy);
			}
			
			if (!this.has_liberty(test_board, row, col + 1, enemy, new int[5][5])) {
				captured_stone += capture(test_board, row, col + 1, enemy);
			}
			
			if (!this.has_liberty(test_board, row, col - 1, enemy, new int[5][5])) {
				captured_stone += capture(test_board, row, col - 1, enemy);
			}

			if(captured_stone == 0) {
				return false;
			}else if(captured_stone == 1 && this.same_board(this.previous_state, test_board)){
				this.isKO = true;
				return false;
			}
		}
		
		return true;
	}

	public boolean same_board(int[][] b1, int[][] b2) {
		for(int i = 0; i < this.BOARD_SIZE; i++) {
			for(int j = 0; j < this.BOARD_SIZE; j++) {
				if(b1[i][j] != b2[i][j]) {
					return false;
				}
			}
		}
		return true;
	}
	
	public boolean has_liberty(int[][] board, int i, int j, int player, int[][] visited) {
		if (!checkRange(i, j) || visited[i][j] != 0) {
			return false;
		}

		visited[i][j] = 1;
		
		int stone = board[i][j];
		
		if (stone == 0) {
			return true;
		} 
		
		if(stone != player) {
			return false;
		}
		
		return has_liberty(board, i + 1, j, player, visited) ||
				has_liberty(board, i - 1, j, player, visited) ||
				has_liberty(board, i, j + 1, player, visited) ||
				has_liberty(board, i, j - 1, player, visited);
	}

	public int move(int row, int col, int player, boolean pass) {
		
		//pass
		if (pass) {
			if (this.wasPass) {
				this.game_result = this._check_winner(true);
				this.previous_state = this.state;
				return this.game_result;
			}
			
			this.wasPass = true;
			this.steps++;
			this.game_result = this._check_winner(false);

			if (this.show_board) {
				String gamer = player == 1 ? "Black" : "White";
				System.out.println("Step" + this.steps + ": " + gamer + " moved: PASS");
				this.print_board();
			}
			if (this.show_result) {
				this.game_result_report();
			}
			this.captured = 0;
			this.previous_state = this.copy_board(this.state);
			return this.game_result;
		}
		this.wasPass = false;

		//not valid 
		if (!this.is_valid_move(player, row, col)) {	
			String gamer, winner;	
			if(player == 1) {
				gamer = "Black";
				winner = "White";
				this.game_result = Board.W_WIN;
				this.invalid_move_black ++;
			}else {
				winner = "Black";
				gamer = "White";
				this.game_result = Board.B_WIN;
				this.invalid_move_white ++;
			}
			System.out.println(gamer + " made an invalid move on (" + row + " , " + col + "), " + winner + " win!");
			this.print_board();
			return this.game_result;
		}

		this.previous_state = this.copy_board(this.state);
		this.state[row][col] = player;
	
		int enemy = player == 1 ? 2 : 1;
		
		if (!this.has_liberty(this.state, row + 1, col, enemy, new int[this.BOARD_SIZE][this.BOARD_SIZE])) {
			this.capture(this.state, row + 1, col, enemy);
		}
		
		if (!this.has_liberty(this.state, row - 1, col, enemy, new int[this.BOARD_SIZE][this.BOARD_SIZE])) {
			this.capture(this.state, row - 1, col, enemy);
		}
		
		if (!this.has_liberty(this.state, row, col + 1, enemy, new int[this.BOARD_SIZE][this.BOARD_SIZE])) {
			this.capture(this.state, row, col + 1, enemy);
		}
		
		if (!this.has_liberty(this.state, row, col - 1, enemy, new int[this.BOARD_SIZE][this.BOARD_SIZE])) {
			this.capture(this.state, row, col - 1, enemy);
		}
		
		this.steps++;
		this.game_result = this._check_winner(false);

		if (this.show_board) {
			String gamer = player == 1 ? "Black" : "White";
			System.out.println("Step" + this.steps + ": " + gamer + " moved: (" + row + " , " + col + ")");
			this.print_board();
		}
		if (this.show_result) {
			this.game_result_report();
		}
			
		return this.game_result;
	}

	public int capture(int[][] board, int row, int col, int player) {

		if (!this.checkRange(row, col)) {
			return 0;
		}
		if (board[row][col] != player) {
			return 0;
		}

		board[row][col] = 0;
		return 1 + 
				this.capture(board, row + 1, col, player) + 
				this.capture(board, row - 1, col, player) + 
				this.capture(board, row, col + 1, player) + 
				this.capture(board, row, col - 1, player);
	}

	public boolean game_over() {
		return this.game_result != Board.ONGOING;
	}

	public void print_board() {
		String board = this.encode_state();
		board = board.replace('0', ' ');
		board = board.replace('1', 'B');
		board = board.replace('2', 'W');
		String vert = " | ";
		String hori = "--- --- --- --- ---";
		String[] lines = new String[5];
		int cnt = 0;
		for (int i = 0; i < 5; i++) {
			StringBuilder sb = new StringBuilder();
			sb.append(board.charAt(cnt++)).append(vert);
			sb.append(board.charAt(cnt++)).append(vert);
			sb.append(board.charAt(cnt++)).append(vert);
			sb.append(board.charAt(cnt++)).append(vert);
			sb.append(board.charAt(cnt++));
			lines[i] = sb.toString();
		}
		for (int i = 0; i < 4; i++) {
			System.out.println(lines[i]);
			System.out.println(hori);
		}
		System.out.println(lines[4] + "\n");
	}

	public int _check_winner(boolean twoPass) {
		if (this.game_result != Board.ONGOING) {
			return this.game_result;
		}

		if (this.steps >= 24 || twoPass) {
			int komi = this.BOARD_SIZE * 10 / 2;
			int white = komi, black = 0;
			for (int i = 0; i < this.BOARD_SIZE; i++) {
				for (int j = 0; j < this.BOARD_SIZE; j++) {
					if (this.state[i][j] == 1) {
						black += 10;
					}
					if (this.state[i][j] == 2) {
						white += 10;
					}
				}
			}

			this.white_stones  = white + 25;
			this.black_stones = black;
			if (white == black)
				return Board.DRAW;
			if (white > black)
				return Board.W_WIN;
			return Board.B_WIN;
		}

		return Board.ONGOING;
	}

	public void game_result_report() {
		if (this.game_result == Board.ONGOING)
			return;
		if (this.game_result == Board.B_WIN) {
			System.out.println("Game Over: Black Wins");
		}
		if (this.game_result == Board.W_WIN) {
			System.out.println("Game Over: White Wins");
		}
		if (this.game_result == Board.DRAW) {
			System.out.println("Game Over: Draw");
		}
	}
	
	public int[] curr_score() {
		int[] ret = new int[2];
		ret[1] = 25;
		for(int i = 0; i < this.BOARD_SIZE; i++) {
			for(int j = 0; j < this.BOARD_SIZE; j++) {
				if(this.state[i][j] == 1) {
					ret[0] += 10;
				}
				if(this.state[i][j] == 2) {
					ret[1] += 10;
				}
			}
		}
		return ret;
	}
}
