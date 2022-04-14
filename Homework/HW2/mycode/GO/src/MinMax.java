import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class MinMax {
	int side;
	int enemy;
	int[][] currboard;
	int[][] prevboard;
	int depth = 6;
	int width = 10;
	int size = 5;
	
	// update board : for testing
	public void update(Board board) {
		this.currboard = this.copy_board(board.state);
		this.prevboard = this.copy_board(board.previous_state);
	}
	
	public MinMax(int side, int[][] currboard, int[][] prevboard) {
		this.side = side;
		this.enemy = 3 - side;
		this.currboard = this.copy_board(currboard);
		this.prevboard = this.copy_board(prevboard);
	}

	//estimate the steps of the board
	public int cntSteps() {
		int cnt = 0;
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if(this.currboard[i][j] != 0) {
					cnt++;
				}
			}
		}
		return cnt;
	}
	

	
	// make a move
	public int[] move() {
		
		//first two steps
		int steps = this.cntSteps();
		if(steps == 0) {
		    return new int[]{2, 2};
	    }else if(steps == 1) {
	    	if(this.currboard[2][2] != 0) {
	    		return new int[]{2, 1};
	    	}else {
	    		return new int[]{2, 2};	
	    	}
	    }
		
		//check pass
		if(same_board(prevboard, currboard)) {
			int scores = this.score(currboard, false);
			if(scores > 0) {
				return new int[]{-1, -1};
			}
		}
		
		int[] values = this.max_value(this.currboard, Integer.MIN_VALUE, Integer.MAX_VALUE, 1);
		
		if(values[0] < 0) {
			return new int[]{-1, -1};

		}
		
		return new int[]{values[0], values[1]};
	}

	public int[] max_value(int[][] board, int alpha, int beta, int level) {
		if (level > this.depth) {
			return new int[] { -1, -1, score(board, true) };
		}
		
		ArrayList<int[]> valid_moves = this.find_valid_move(board, this.side);
		
		if(valid_moves.isEmpty()) {
			return new int[] { -1, -1, score(board, true) };
		}
		
		valid_moves.sort((o1, o2) -> ((o2[2] - o1[2])));
		if(valid_moves.size() > this.width) {
			while(valid_moves.size() > this.width) {
				valid_moves.remove(valid_moves.size() - 1);
			}
		}
		
	    if(this.side == 1 && valid_moves.get(0)[2] > 100) {
	    	int[] pos = valid_moves.get(0);
	    	board[pos[0]][pos[1]] = this.side;
	    	return new int[] {pos[0], pos[1], this.score(board, true)};
	    }
		
		int[] ret = new int[] {-1, -1, Integer.MIN_VALUE};
		
		for(int[] coord : valid_moves) {
			int[][] tmp_board = this.copy_board(board);
			tmp_board[coord[0]][coord[1]] = this.side;
			int captured = this.captureAll(tmp_board, this.enemy);
			int[] min_score =  this.min_value(tmp_board, alpha, beta, level + 1);
			int utility = min_score[2] + captured * 10;
			if(ret[2] < utility) {
				ret[0] = coord[0];
				ret[1] = coord[1];
				ret[2] = utility;
			}
			if(ret[2] >= beta) {
				return ret;
			}
			alpha = Math.max(alpha, ret[2]);
		}
		return ret;
	}

	public int[] min_value(int[][] board, int alpha, int beta, int level) {
		if (level > this.depth) {
			return new int[] { -1, -1, score(board, true) };
		}
		
		ArrayList<int[]> valid_moves = this.find_valid_move(board, this.enemy);
		
		if(valid_moves.isEmpty()) {
			return new int[] { -1, -1, score(board, true) };
		}
		
		valid_moves.sort((o1, o2) -> (o2[2] - o1[2]));
		if(valid_moves.size() > this.width) {
			
			while(valid_moves.size() > this.width) {
				valid_moves.remove(valid_moves.size() - 1);
			}
		}
		
		if(this.enemy == 1 && valid_moves.get(0)[2] > 100) {
		    int[] pos = valid_moves.get(0);
		    board[pos[0]][pos[1]] = this.enemy;
		   	return new int[] {pos[0], pos[1], this.score(board, true)};
		 }
		  
		int[] ret = new int[] {-1, -1, Integer.MAX_VALUE};
		
		for(int[] coord : valid_moves) {
			int[][] tmp_board = this.copy_board(board);
			tmp_board[coord[0]][coord[1]] = this.enemy;
		    int captured = this.captureAll(tmp_board, this.side);
			int[] max_score = this.max_value(tmp_board, alpha, beta, level + 1);
			int utility = max_score[2] + captured * 10;
			if(ret[2] > utility) {
				ret[0] = coord[0];
				ret[1] = coord[1];
				ret[2] = utility;
			}
			if(ret[2] < alpha) {
				return ret;
			}
			beta = Math.min(beta, ret[2]);
		}
		return ret;
	}
	
	//return valid_move with score
	public ArrayList<int[]> find_valid_move(int[][] board, int player) {
		int[][] weight = new int[][] {		
			{0, 0, 0, 0, 0},
			{0, 5, 5, 5, 0},
			{0, 5, 10, 5, 0},
			{0, 5, 5, 5, 0},
			{0, 0, 0, 0, 0}
		};
		ArrayList<int[]> ret = new ArrayList<int[]>();
		for (int i = 0; i < this.size; i++) {
			for (int j = 0; j < this.size; j++) {
				if(this.is_valid_move(board, i, j, player, false)) {
					int captured = this.try_capture(board, i, j, player);
					int allies = this.allies_around(i, j, board, player);
					ret.add(new int[] {i, j, weight[i][j] + captured * 100 + allies * 10});
				}
			}
		}
		return ret;
	}

	//calculate the score of the board
	public int score(int[][] board, boolean as_utility) {
		int me_cnt = 0, enemy_cnt = 0, me_no_liber = 0, enemy_no_liber = 0;
		if(this.side == 2) {
			me_cnt = 3;
		}else {
			enemy_cnt = 3;
		}
		
		for (int i = 0; i < this.size; i++) {
			for (int j = 0; j < this.size; j++) {
				if (board[i][j] == this.side) {
					me_cnt += 1;
					me_no_liber += (as_utility && !this.has_liberty(board, i, j, this.side, new int[this.size][this.size])) ? 1 : 0;
				}else if (board[i][j] == this.enemy) {
					enemy_cnt += 1;
					enemy_no_liber += (as_utility && !this.has_liberty(board, i, j, this.enemy, new int[this.size][this.size])) ? 1 : 0;
				}
			}
		}
		
		return (me_cnt - enemy_cnt) * 10 + 5 * enemy_no_liber - 5 * me_no_liber;
	}
	
	//giving a board, a location, a player and whether is a pass move 
	//return true if the move is valid
	public boolean is_valid_move(int[][] board, int i, int j, int player, boolean pass) {
		//check pass
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
		int[][] test_board = this.copy_board(board);
		test_board[i][j] = player;
		if (has_liberty(test_board, i, j, player, new int[5][5])) {
			return true;
		} else {
			int captured = captureAll(test_board, 3-player);
			if (captured == 0) {
				return false;
			} else if (captured == 1 && same_board(this.prevboard, test_board)) {
				// KO rule
				return false;
			}
		}
		return true;
	}
	

	//check whether the given position has liberty for a player
	private boolean has_liberty(int[][] board, int i, int j, int player, int[][] visited) {

		if(!in_range(i, j) || visited[i][j] != 0) {
			return false;
		}
		
		visited[i][j] = 1;
		
		if(board[i][j] == 0) {
			return true;
		}	
		
		if(board[i][j] != player) {
			return false;
		}
		
		return has_liberty(board, i + 1, j, player, visited) ||
				has_liberty(board, i - 1, j, player, visited) ||
				has_liberty(board, i, j + 1, player, visited) ||
				has_liberty(board, i, j - 1, player, visited);
	}
	
	private boolean in_range(int i, int j) {
		return i < size && j < size && i >= 0 && j >= 0;
	}
	
	private int capture(int[][] board, int i, int j, int player) {
		if(!in_range(i, j)) return 0;
		if(board[i][j] == 0) return 0;
		
		if(board[i][j] != player) {
			return 0;
		}
		
		board[i][j] = 0;
		return 1 + 
				capture(board, i + 1, j, player) + 
				capture(board, i - 1, j, player) + 
				capture(board, i, j + 1, player) + 
				capture(board, i, j - 1, player);
	}
	
	public boolean same_board(int[][] b1, int[][] b2) {
		for(int i = 0; i < size; i++) {
			for(int j = 0; j < size; j++) {
				if(b1[i][j] != b2[i][j]) {
					return false;
				}
			}
		}
		return true;
	}
	
	public String encode(int[][] board) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < this.size; i++) {
			for (int j = 0; j < this.size; j++) {
				sb.append(board[i][j]);
			}
		}
		return sb.toString();
	}
	
	public void print_board(int[][] boardd) {
		String board = this.encode(boardd);
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
	
	
	public int allies_around(int i, int j, int[][] board, int player ) {
		int cnt = 0;
		
		if(in_range(i + 1, j)) {
			cnt += board[i+1][j] == player ? 1 : 0;
		}
		
		if(in_range(i - 1, j)) {
			cnt += board[i-1][j] == player ? 1 : 0;
		}
		
		if(in_range(i, j + 1)) {
			cnt += board[i][j+1] == player ? 1 : 0;
		}
		
		if(in_range(i, j - 1)) {
			cnt += board[i][j-1] == player ? 1 : 0;
		}
		
		if(cnt > 0) {
			return cnt;
		}
		if(in_range(i - 1, j - 1)) {
			cnt += board[i-1][j-1] == player ? 1 : 0;
		}
		
		if(in_range(i - 1, j + 1)) {
			cnt += board[i-1][j+1] == player ? 1 : 0;
		}
		
		if(in_range(i + 1, j + 1)) {
			cnt += board[i+1][j+1] == player ? 1 : 0;
		}
		
		if(in_range(i + 1, j - 1)) {
			cnt += board[i+1][j-1] == player ? 1 : 0;
		}
		
		return (cnt + 1) / 2;
	}
	
	//capture all the stones that has 0 liberty on board
 	public int captureAll(int[][] board, int be_captured){
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
 	
 	//giving a board state, a location (i, j), and player who tries to capture
 	//return the number of captured enemy stones, and players liberty in new board;
 	private int try_capture(int[][] board, int i, int j, int player) {
 		
 		int enemy = 3 - player;
		board = this.copy_board(board);
		board[i][j] = player;
		
		int captured = 0;
		
		int rowdown = i + 1;
		int rowup = i - 1;
		int colright = j + 1;
		int colleft = j - 1;
		
		if(rowdown < 5 && 
				board[rowdown][j] == enemy && 
				!has_liberty(board, rowdown, j, enemy, new int[5][5])) {
			captured += this.capture(board, rowdown, j, enemy);
		}
		
		if(rowup >= 0 && 
				board[rowup][j] ==  enemy && 
				!has_liberty(board, rowup, j,  enemy, new int[5][5])) {
			captured += capture(board, rowup, j,  enemy);
		}
		
		if(colright < 5 && 
				board[i][colright] ==  enemy && 
				!has_liberty(board, i, colright,  enemy, new int[5][5])) {
			captured += capture(board, i, colright,  enemy);
		}
		
		if(colleft >= 0 && 
				board[i][colleft] ==  enemy && 
				!has_liberty(board, i, colleft,  enemy, new int[5][5])) {
			captured += capture(board, i, colleft,  enemy);
		}
		
		return captured;
	}
 	
	//deep copy a board
	public int[][] copy_board(int[][] board) {
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
