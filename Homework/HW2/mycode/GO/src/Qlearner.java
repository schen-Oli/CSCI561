import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Qlearner{
	
	public final double WIN_REWARD = 10;
	public final double DRAW_REWARD = 0;
	public final double LOSS_REWARD = -5;
	
	public double alpha = 0.8;
	public double gamma = 0.9;
	public double initial_value = 5;
	
	public int side;
	public int enemy;
	private String filename;
	
	public Map<String, double[][]> q_values;
	public ArrayList<String> history_states;
	
	public int rotation;
	public int mirrored;
	private String currState; 
	
	public Qlearner(int side) {
		this.side = side;
		this.enemy = side == 1 ? 2 : 1;
		this.q_values = new HashMap<String, double[][]>();
		this.history_states = new ArrayList<String>();
		this.filename = side == 1 ? "parameters_black.txt" : "parameters_white.txt";
		this.rotation = 0;
		this.mirrored = 0;
		this.currState = "";
		this.readFile();
	}
	
	private void readFile() {
		try {
			File params = new File(this.filename);
			Scanner scanner = new Scanner(params);
			while(scanner.hasNextLine()) {
				String[] lines = scanner.nextLine().split(" ");
				if(lines.length < 1) {
					break;
				}
				String key = lines[0];
				double[][] value = new double[5][5];
				int cnt = 1;
				for(int i = 0; i < 5; i++) {
					for(int j = 0; j < 5; j++) {
						value[i][j] = Double.parseDouble(lines[cnt++]);
					}
				}
				this.q_values.put(key, value);
			}
			scanner.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void writeFile(){
		try{
			FileWriter newparams = new FileWriter(this.filename, false);
			StringBuilder output = new StringBuilder();
			for(Map.Entry<String, double[][]> entry : this.q_values.entrySet()) {
				output.append(entry.getKey()).append(" ");
				double[][] q = entry.getValue();
				for(int i = 0; i < 5; i++) {
					for(int j = 0; j < 5; j++) {
						output.append(((int)(q[i][j] * 1000))/1000.0).append(" ");
					}
				}
				output.replace(output.length()-1, output.length(), "\n");
			}
			newparams.write(output.toString());
			newparams.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	

	public double[][] Q(Board board, String state) {	
	
		//contains value
		if(this.q_values.containsKey(state)) {
			this.currState = state;
			return this.q_values.get(state);
		}
		
		this.rotation = 0;
		this.mirrored = 0;
		String rotated_state = checkDup(state);
		
		if(rotated_state !=null) {
			this.currState = rotated_state;
			return this.q_values.get(rotated_state);		
		}
		
		double[][] value = new double[][] {
			{2, 4, 6, 4, 2},
            {4, 6, 8, 6, 4},
            {6, 8, 10, 8, 6},
            {4, 6, 8, 6, 4},
            {2, 4, 6, 4, 2}
		};
	
		for(int i = 0; i < 5; i++) {
			for(int j = 0; j < 5; j++) {
				if(!this.is_valid_move_without_KO(board.state, i, j, false)) {
					value[i][j] = -5;
				}else {
					value[i][j] += try_capture(state, i, j);
				}
			}
		}
		this.q_values.put(state, value);
		
		this.currState = state;
		this.rotation = 0;
		this.mirrored = 0;
		return this.q_values.get(state);
	}

	public int try_capture(String board_code, int i, int j) {
		int[][] board = this.decode(board_code);
		board[i][j] = this.side;
		int captured = 0;
		
		if(i + 1 < 5 && 
				board[i + 1][j] == this.enemy && 
				!this.has_liberty(board, i+1, j, this.enemy, new int[5][5])) {
			captured += this.capture(board, i+1, j);
		}
		
		if(i - 1 >= 0 && 
				board[i - 1][j] == this.enemy && 
				!this.has_liberty(board, i-1, j, this.enemy, new int[5][5])) {
			captured += this.capture(board, i-1, j);
		}
		
		if(j + 1 < 5 && 
				board[i][j+1] == this.enemy && 
				!this.has_liberty(board, i, j+1, this.enemy, new int[5][5])) {
			captured += this.capture(board, i, j+1);
		}
		
		if(j - 1 >= 0 && 
				board[i][j-1] == this.enemy && 
				!this.has_liberty(board, i, j-1, this.enemy, new int[5][5])) {
			captured += this.capture(board, i-1, j);
		}
		
		return captured;
	}
	
	private boolean has_liberty(int[][] board, int i, int j, int player, int[][] visited) {
		if(!in_range(i, j) || visited[i][j] != 0) {
			return false;
		}
		
		if(board[i][j] == 0) {
			visited[i][j] = 1;
			return true;
		}
		
		if(board[i][j] != player) {
			visited[i][j] = 1;
			return false;
		}
		
		visited[i][j] = 1;
		return has_liberty(board, i + 1, j, player, visited) ||
				has_liberty(board, i - 1, j, player, visited) ||
				has_liberty(board, i, j + 1, player, visited) ||
				has_liberty(board, i, j - 1, player, visited);
	}
	
	private int capture(int[][] board, int i, int j) {
		if(!in_range(i, j)) return 0;
		if(board[i][j] != this.enemy) {
			return 0;
		}
		
		board[i][j] = 0;
		return 1 + 
				capture(board, i + 1, j) + 
				capture(board, i - 1, j) + 
				capture(board, i, j + 1) + 
				capture(board, i, j - 1);
	}
	
	public int[][] mirror(int[][] ori, int axis){
		
		int[][] arr = new int[5][5];
		//1 : horizontal axis, 2: vertical axis;
		if(axis == 1) {
			for(int i = 0; i <= 2; i++) {
				for(int j = 0; j < 5; j++) {
					arr[4-i][j] = ori[i][j];
					arr[i][j] = ori[4-i][j];
				}
			}
		}else {
			for(int i = 0; i < 5; i++) {
				for(int j = 0; j <= 2; j++) {
					arr[i][4 - j] = ori[i][j];
					arr[i][j] = ori[i][4 - j];
				}
			}
		}
		return arr;
	}
	
	
    public String checkDup(String state_string) {
    	
    	int[][] ori = decode(state_string);
    	
    	//check rotation
    	int[][] state = rotate(ori);
    	if(this.q_values.containsKey(encode(state))) {
    		this.rotation = 1;
    		return encode(state);
    	}else{
    		int[][] rotate1_mirror1 = this.mirror(state, 1);
    		if(this.q_values.containsKey(encode(rotate1_mirror1))) {
    			this.rotation = 1;
    			this.mirrored = 1;
    			return encode(rotate1_mirror1);
    		}
    	}
    	
    	state = rotate(state);
    	if(this.q_values.containsKey(encode(state))) {
    		this.rotation = 2;
    		return encode(state);
    	}else {
    		int[][] rotate2_mirror2 = this.mirror(state, 2);
    		if(this.q_values.containsKey(encode(rotate2_mirror2))) {
    			this.rotation = 2;
    			this.mirrored = 2;
    			return encode(rotate2_mirror2);
    		}
    	}
    	
    	state = rotate(state);
    	if(this.q_values.containsKey(encode(state))) {
    		this.rotation = 3;
    		return encode(state);
    	}else {
    		int[][] rotate3_mirror1 = this.mirror(state, 1);
    		if(this.q_values.containsKey(encode(rotate3_mirror1))) {
    			this.rotation = 3;
    			this.mirrored = 1;
    			return encode(rotate3_mirror1);
    		}
    	}

    	//check mirror
    	state = mirror(ori, 1);
    	if(this.q_values.containsKey(encode(state))) {
    		this.mirrored = 1;
    		return encode(state);
    	}
    	
    	state = mirror(ori, 2);
    	if(this.q_values.containsKey(encode(state))) {
    		this.mirrored = 2;
    		return encode(state);
    	}
    	
    	return null;
    }
    
    public String encode(int[][] arr) {
    	StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 5; j++) {
				sb.append(arr[i][j]);
			}
		}
		return sb.toString();
    }
    
    public int[] dedup(int row, int col) {
    	
    	if(this.mirrored == 1) {
    		row = 4 - row;
    	}else if(this.mirrored == 2) {
    		col = 4 - col;
    	}
 
    	int i = this.rotation;
    	while(i > 0) {
    		int new_row = 4 - col;
    		int new_col = row;
    		row = new_row;
    		col = new_col;
    		i--;
    	}
    	
    	return new int[] {row, col};
    }
    		
    public int[][] rotate(int[][] ori) {
    	int[][] new_state = new int[5][5];
    	for(int i = 0; i < 5; i++) {
    		for(int j = 0; j < 5; j++){
    			new_state[j][4 - i] = ori[i][j];
    		}
    	}
    	return new_state;
    }
    
    
    
    public int[][] decode(String s) {
		int cnt = 0;
		int[][] ret = new int[5][5];
		
		for(int i = 0; i < 5; i++) {
			for(int j = 0; j < 5; j++) {
				ret[i][j] = s.charAt(cnt++) - '0';
			}
		}
		
		return ret;
	}
    
	public int[] select_best_move(Board board) {
		
		String state_code = board.encode_state();
		double[][] q_value = this.Q(board, state_code);
		
		int cnt = 0;
		while(true) {
			ArrayList<int[]> arr = this._find_max(q_value, this.decode(this.currState));
			
			int row = arr.get(0)[0];
			int col = arr.get(0)[1];
			
			int[] tmpIJ = (this.mirrored > 0 || this.rotation > 0) ? this.dedup(row, col) : new int[] {row, col};
			if(board.is_valid_move(this.side, tmpIJ[0], tmpIJ[1])) {
				return new int[] {row, col};
			}else if(!board.isKO){
				q_value[row][col] -= 2;
			}
			
			if(cnt++ > 24) {
				return new int[] {-1, -1};
			}
		}
	}
	
	
	public ArrayList<int[]> _find_max(double[][] q_value, int[][] curr_board){
		double curr_max = -Double.MAX_VALUE;
		ArrayList<int[]> res = new ArrayList<int[]>();
		for(int i = 0; i < 5; i++) {
			for(int j = 0; j < 5; j++) {
				if(q_value[i][j] > curr_max) {
					curr_max = q_value[i][j];
					res.clear();
					res.add(new int[] {i, j});
				}else if(q_value[i][j] == curr_max) {
					res.add(new int[] {i, j});
				}
			}
		}
		res.sort((o1, o2)->(
				allies_around(o2[0], o2[1], curr_board) - 
				allies_around(o1[0], o1[1], curr_board) + 
				(try_capture(encode(curr_board), o2[0], o2[1]) - 
						try_capture(encode(curr_board), o1[0], o1[1])) * 10
			));
		return res;
	}
	
	private static boolean in_range(int i, int j) {
		return i < 5 && j < 5 && i >= 0 && j >=0;
	}
	
	private static int allies_around(int i, int j, int[][] board) {
		int cnt = 0;
		int player = board[i][j];
		
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
	
	
	public void move(Board board) {
		if(board.game_over()) return;
		
		//first two steps
		if(board.steps == 0) {
			board.move(2, 2, this.side, false);
			this.history_states.add(board.encode_state() + " " + 2 + " " + 2);
			return;
		}else if(board.steps == 1) {
			if(board.state[2][2] == 0) {
				board.move(2, 2, this.side, false);
				this.history_states.add(board.encode_state() + " " + 2 + " " + 2);
				return;
			}else {
				board.move(1, 1, this.side, false);
				this.history_states.add(board.encode_state() + " " + 1 + " " + 1);
				return;
			}
		}
		
		//if enemy pass
		if(board.wasPass) {
			int[] scores = board.curr_score();
			int myscore = scores[this.side - 1];
			int enemyscore = scores[this.enemy - 1];
			if(myscore > enemyscore) {
				board.move(0, 0, this.side, true);
				return;
			}
		}
		
		int[] ij = this.select_best_move(board);
	
		if(ij[0] == -1) {
			board.move(0, 0, this.side, true);
		}else {
			if(this.mirrored > 0 || this.rotation > 0) {
				ij = this.dedup(ij[0], ij[1]);
			}
			this.history_states.add(this.currState + " " + ij[0] + " " + ij[1]);
			board.move(ij[0], ij[1], this.side, false);
		}
		
		this.mirrored = 0;
		this.rotation = 0;
		return;
	}
	
	
	public void learn(Board board) {
		//System.out.println("Function Learn running in QLEARNER");
		double reward;
		if (board.game_result == 0) {
			reward = this.DRAW_REWARD;
		}else if(board.game_result == this.side) {
			reward = Math.abs(board.white_stones - board.black_stones);
		}else {
			reward = -Math.abs(board.white_stones - board.black_stones);
		}
		Collections.reverse(this.history_states);
		double max_q_value = -5;
		for(String s : this.history_states) {
			
			String[] arr = s.split(" ");
			String state = arr[0];
			int row = Integer.valueOf(arr[1]);
			int col = Integer.valueOf(arr[2]);
			double[][] q = this.Q(board, state);
			if(max_q_value < 0) {
				q[row][col] = reward;
			}else{
				q[row][col] = q[row][col] * (1 - this.alpha) + this.alpha * this.gamma * max_q_value;
			}
			for(int i = 0; i < 5; i++) {
				for(int j = 0; j < 5; j++) {
					max_q_value = q[i][j] > max_q_value ? q[i][j] : max_q_value;
				}
			}
		}
		
	}
	
private boolean is_valid_move_without_KO(int[][] curr_board, int i, int j, boolean pass) {
		
		if(pass) {
			return true;
		}
		
		//check if in range
		if(!in_range(i, j)) {
			return false;
		}
		
		//check if empty
		if(curr_board[i][j] != 0) {
			return false;
		}
		
		//check liberty rule
		int[][] test_board = this.copy_board(curr_board);
		test_board[i][j] = side;
		if(has_liberty(test_board, i, j, side, new int[5][5])) {
			return true;
		}else {
			int captured = 0;
			if(!has_liberty(test_board, i + 1, j, enemy, new int[5][5])) {
				captured += capture(test_board, i + 1, j);
			}
			if(!has_liberty(test_board, i - 1, j, enemy, new int[5][5])) {
				captured += capture(test_board, i - 1, j);
			}
			if(!has_liberty(test_board, i, j + 1, enemy, new int[5][5])) {
				captured += capture(test_board, i, j + 1);
			}
			if(!has_liberty(test_board, i, j - 1, enemy, new int[5][5])) {
				captured += capture(test_board, i, j - 1);
			}
			
			if(captured == 0) {
				return false;
			}
		}
		return true;
	}

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
