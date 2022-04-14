import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class TestPlayer {
	
	public int side;
	public int enemy;
	public Qlearner q;
	public MinMax mx;
	
	public TestPlayer(int player) {
		this.side = player;
		this.enemy = player == 1 ? 2 : 1;
		
	}
	
	public void random_move(Board board) {
		if(board.game_over()) {
			return;
		}
		
		if(board.steps == 0) {
			board.move(2, 2, this.side, false);
			return;
		}
		
		ArrayList<int[]> possible = new ArrayList();
		
		for(int i = 0; i < 5; i++) {
			for(int j = 0; j < 5; j++) {
				if(board.state[i][j] == 0 && board.is_valid_move(this.side, i, j)) {
					possible.add(new int[] {i, j});
				}
			}
		}
		
		if(possible.size() > 0) {
			int index =  ThreadLocalRandom.current().nextInt(0, possible.size());
			int[] ij = possible.get(index);
			board.move(ij[0], ij[1], this.side, false);
		}else {
			board.move(0, 0, this.side, true);
		}
		return;
	}
	
	public void q_move(Board board) {
		if(this.q == null) {
			this.q = new Qlearner(this.side);
		}
		q.move(board);
	}
	
	private int allies_around(int i, int j, int[][] board) {
		int cnt = 0;
		int player = this.side;
		
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
		return cnt;
	}
	
	public void greedy_move(Board board) {
		if(board.steps == 0) {
			board.move(0, 0, this.side, false);
			return;
		}
		ArrayList<int[]> valid_move = this.find_valid_move(board);
		
		if(valid_move.size() > 1) {
			valid_move.sort((o1, o2)->(
					allies_around(o2[0], o2[1], board.state) -
					allies_around(o1[0], o1[1], board.state) +
					try_capture(board.encode_state(), o2[0], o2[1]) - 
					try_capture(board.encode_state(), o1[0], o1[1])
				));
		}
		
		if(valid_move.size() == 0) {
			board.move(0, 0, this.side, true);
		}else {
			int[] ij = valid_move.get(0);
			if(this.try_capture(board.encode_state(), ij[0], ij[1]) != 0) {
				board.move(ij[0], ij[1], this.side, false);
			}else {
				int index =  ThreadLocalRandom.current().nextInt(0, valid_move.size());
				ij = valid_move.get(index);
				board.move(ij[0], ij[1], this.side, false);
			}
		}
	}
	
	private ArrayList<int[]> find_valid_move(Board board){
		ArrayList<int[]> ret = new ArrayList<int[]>();
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 5; j++) {
				if(board.state[i][j] == 0 && board.is_valid_move(this.side, i, j)) {
					ret.add(new int[] {i, j});
				}
			}
		}
		
		return ret;
	}
	
	public int try_capture(String board_code, int i, int j) {
		int[][] board = decode(board_code);
		board[i][j] = this.side;
		int captured = 0;
		
		if(i + 1 < 5 && 
				board[i + 1][j] == enemy && 
				!has_liberty(board, i+1, j, enemy, new int[5][5])) {
			captured += capture(board, i+1, j);
		}
		
		if(i - 1 >= 0 && 
				board[i - 1][j] == enemy && 
				!has_liberty(board, i-1, j, enemy, new int[5][5])) {
			captured += capture(board, i-1, j);
		}
		
		if(j + 1 < 5 && 
				board[i][j+1] == enemy && 
				!has_liberty(board, i, j+1, enemy, new int[5][5])) {
			captured += capture(board, i, j+1);
		}
		
		if(j - 1 >= 0 && 
				board[i][j-1] == enemy && 
				!has_liberty(board, i, j-1, enemy, new int[5][5])) {
			captured += capture(board, i-1, j);
		}
		
		return captured;
	}
	
	private int capture(int[][] board, int i, int j) {
		if(!in_range(i, j)) return 0;
		if(board[i][j] != enemy) {
			return 0;
		}
		
		board[i][j] = 0;
		return 1 + 
				capture(board, i + 1, j) + 
				capture(board, i - 1, j) + 
				capture(board, i, j + 1) + 
				capture(board, i, j - 1);
	}
	
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
	
	private int[][] decode(String code){
		int cnt = 0;
		int[][] ret = new int[5][5];
		
		for(int i = 0; i < 5; i++) {
			for(int j = 0; j < 5; j++) {
				ret[i][j] = code.charAt(cnt++) - '0';
			}
		}
		
		return ret;
	}
	
	private boolean in_range(int i, int j) {
		return i < 5 && j < 5 && i >= 0 && j >=0;
	}
	
	public void aggresive_move(Board board) {
		if(board.steps == 0) {
			board.move(2, 2, this.side, false);
			return;
		}
		ArrayList<int[]> first_level = this.find_valid_move(board);
		int[] best = new int[2];
		
		if(first_level.isEmpty()) {
			board.move(0, 0, this.side, true);
			return;
		}
		
		int max_captured = 0;
		
		for(int[] coords : first_level) {
			int[][] tmp_board = this.copy_board(board.state);
			tmp_board[coords[0]][coords[1]] = this.side;
			
			int captured_first_level = 0;
			
			for(int i = 0; i < 5; i++) {
				for(int j = 0; j < 5; j++) {
					if(tmp_board[i][j] == this.enemy && !this.has_liberty(tmp_board, i, j, this.enemy, new int[5][5])) {
						captured_first_level += this.capture(tmp_board, i, j);
					}
				}
			}
			
			int captured_second_level = 0;
			
			for(int i = 0; i < 5; i++) {
				for(int j = 0; j < 5; j++) {
					if(tmp_board[i][j] == 0) {
						int c = this.try_capture(this.encode(tmp_board), i, j);
						captured_second_level = c > captured_second_level ? c : captured_second_level;
					}
				}
			}
			
			if ((captured_first_level + captured_second_level) > max_captured) {
				max_captured = captured_first_level + captured_second_level;
				best[0] = coords[0];
				best[1] = coords[1];
			}
		}
		
		if(max_captured == 0) {
			this.greedy_move(board);
		}else {
			board.move(best[0], best[1], this.side, false);
		}
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
	
	public String encode(int[][] board) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 5; j++) {
				sb.append(board[i][j]);
			}
		}
		return sb.toString();
	}
	
	public void min_max(Board board) {
		
		if(board.game_over()) {
			return;
		}
		
		if(this.mx == null) {
			mx = new MinMax(this.side, board.state, board.previous_state);
		}else {
			mx.update(board);
		}
		
		int[] moves = this.mx.move();
		if(moves[0] < 0) {
			board.move(0, 0, this.side, true);
			return;
		}else {
			board.move(moves[0], moves[1], this.side, false);
			return;
		}
	}
	
}
