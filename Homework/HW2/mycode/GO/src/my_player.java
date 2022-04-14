import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.ArrayList;

public class my_player {
	
	private static int side;
	private static int enemy;
	
	private static int est_steps = 0;
	
	private static int[][] prev_board;
	private static int[][] curr_board;
	
	private static int size = 5;
	
	private static Map<String, String[][]> q_values_map;
	
	public static void main(String[] args) {
		//initialization
		prev_board = new int[size][size];
		curr_board = new int[size][size];
		q_values_map = new HashMap<String, String[][]>();
		read();
		move();
	}
	
	private static void read() {
		try {
			//read input
			File input_board = new File("input.txt");
			Scanner scanner = new Scanner(input_board);
			
			side = scanner.nextInt();
			enemy = side == 1 ? 2 : 1;
			
			scanner.nextLine();
			for(int i = 0; i < size; i++) {
				String line = scanner.nextLine();
				for(int j = 0; j < size; j++) {
				    prev_board[i][j] = line.charAt(j) - '0';
				}
			}
			
			for(int i = 0; i < size; i++) {
				String line = scanner.nextLine();
				for(int j = 0; j < size; j++) {
				    curr_board[i][j] = line.charAt(j) - '0';
				    if(curr_board[i][j] != 0) {
				    	est_steps ++;
				    }
				}
			}
			scanner.close();
			
			//read q_values;
			String filename = side == 1 ? "parameters_black.txt" : "parameters_white.txt";
			File q_params = new File(filename);
			
			scanner = new Scanner(q_params);
			while(scanner.hasNextLine()) {
				String[] lines = scanner.nextLine().split(" ");
				if(lines.length < 1) {
					break;
				}
				String key = lines[0];
				String[][] value = new String[5][5];
				int cnt = 1;
				for(int i = 0; i < 5; i++) {
					for(int j = 0; j < 5; j++) {
						value[i][j] = lines[cnt++];
					}
				}
				q_values_map.put(key, value);
			}
			scanner.close();

		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private static void write(boolean isPass, int i, int j) {
		try{
			FileWriter output = new FileWriter("output.txt", false);
			if(isPass) {
				output.write("PASS");
			}else {
				output.write(i + "," + j);
			}
			output.close();		
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void move() {
		
		//if is first two steps
	    if(est_steps == 0) {
	    	write(false, 2, 2);
	    	return;
	    }else if(est_steps == 1) {
	    	if(curr_board[2][2] != 0) {
	    		write(false, 1, 1);
	    	}else {
	    		write(false, 2, 2);
	    	}
	    	return;
	    }
	    
	    //if enemy pass
		if(same_board(prev_board, curr_board)) {
			int[] scores = cnt_score(curr_board);
			if(scores[0] > scores[1]) {
				write(true, 0, 0);
				return;
			}
		}
		
		//other
		int[] coords = select_best_move(curr_board);
		if(coords[0] < 0) {
			write(true, 0, 0);
		}else {
			write(false, coords[0], coords[1]);
		}
	}
	
	private static double[][] convertQtoDouble(String[][] q){
		double[][] ret = new double[5][5];
		for(int i = 0; i < 5; i++) {
			for(int j = 0; j < 5; j++) {
				ret[i][j] = Double.parseDouble(q[i][j]);
			}
		}
		return ret;
	}
	
	private static int[] select_best_move(int[][] board){
		
		double[][] q_values;
		String board_code = encode_board(board);
		
		if(q_values_map.containsKey(board_code)){
			q_values = convertQtoDouble(q_values_map.get(board_code));
			ArrayList<int[]> coords = max_q(q_values, curr_board);
			for(int[] coord : coords) {
				if(is_valid_move(coord[0], coord[1], false)) {
					return new int[] {coord[0], coord[1]};
				}
			}
		}
		
		int[] similar = check_similar(curr_board);
		if(similar[0] >= 0) {
			int rotation_cnt = similar[0];
			int mirror_cnt = similar[1];
			int[][] tmp_board = curr_board;
			while(rotation_cnt > 0) {
				tmp_board = rotate(tmp_board);
				rotation_cnt--;
			}
			if(mirror_cnt > 0) {
				tmp_board = mirror(tmp_board, mirror_cnt);
			}
			
			q_values = convertQtoDouble(q_values_map.get(encode_board(tmp_board)));
			ArrayList<int[]> coords = max_q(q_values, tmp_board);
			for(int[] coord : coords) {
				int[] ori_coord = dedup(coord[0], coord[1], similar[0], similar[1]);
				if(is_valid_move(ori_coord[0], ori_coord[1], false)) {
					return new int[] {ori_coord[0], ori_coord[1]};
				}
			}	
		}
		
		ArrayList<int[]> coords = find_valid_move(curr_board);
		if(coords.isEmpty()) {
			return new int[] {-1, -1};
		}else {
			return new int[] {coords.get(0)[0], coords.get(0)[1]};
		}		
	}
	
	private static int[] dedup(int row, int col,  int rotation_cnt, int mirror_cnt) {
		if(mirror_cnt == 1) {
    		row = 4 - row;
    	}else if(mirror_cnt == 2) {
    		col = 4 - col;
    	}
 
    	while(rotation_cnt > 0) {
    		int new_row = 4 - col;
    		int new_col = row;
    		row = new_row;
    		col = new_col;
    		rotation_cnt--;
    	}
    	
    	return new int[] {row, col};
	}
	
	private static int[] check_similar(int[][] board) {
		int[] ret = new int[2];
		
		//rotation
		int[][] rotated_board = rotate(board);
		if(q_values_map.containsKey(encode_board(rotated_board))){
			ret[0] = 1;
			return ret;
		}else {
			int[][] rotate1_mirror1 = mirror(rotated_board, 1);
			if(q_values_map.containsKey(encode_board(rotate1_mirror1))) {
				ret[0] = 1;
				ret[1] = 1;
				return ret;
			}
		}
		
		rotated_board = rotate(rotated_board);
		if(q_values_map.containsKey(encode_board(rotated_board))){
			ret[0] = 2;
			return ret;
		}else {
			int[][] rotate2_mirror2 = mirror(rotated_board, 2);
			if(q_values_map.containsKey(encode_board(rotate2_mirror2))) {
				ret[0] = 2;
				ret[1] = 2;
				return ret;
			}
		}
		
		rotated_board = rotate(rotated_board);;
    	if(q_values_map.containsKey(encode_board(rotated_board))) {
    		ret[0] = 3;
			return ret;
    	}else {
    		int[][] rotate3_mirror1 = mirror(rotated_board, 1);
    		if(q_values_map.containsKey(encode_board(rotate3_mirror1))) {
    			ret[0] = 3;
				ret[1] = 1;
				return ret;
    		}
    	}
    	
    	//check mirror
    	int[][] mirrored = mirror(board, 1);
    	if(q_values_map.containsKey(encode_board(mirrored))) {
    		ret[1] = 1;
    		return ret;
    	}
		
    	mirrored = mirror(board, 2);
    	if(q_values_map.containsKey(encode_board(mirrored))) {
    		ret[1] = 2;
    		return ret;
    	}
    	
    	return new int[] {-1, -1};
	}
	
	private static int[][] rotate(int[][] board){
		int[][] new_state = new int[5][5];
    	for(int i = 0; i < 5; i++) {
    		for(int j = 0; j < 5; j++){
    			new_state[j][4 - i] = board[i][j];
    		}
    	}
    	return new_state;
	}
	
	private static int[][] mirror(int[][] ori, int axis){
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
	
	private static ArrayList<int[]> find_valid_move(int[][] board){
		ArrayList<int[]> ret = new ArrayList<int[]>();
		
		int score_init[][] = new int[][] {
			{0, 0, 0, 0, 0},
			{0, 1, 1, 1, 0},
			{0, 1, 2, 1, 0},
			{0, 1, 1, 1, 0},
			{0, 0, 0, 0, 0}
		};
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if(board[i][j] == 0 && is_valid_move(i, j, false)) {
					int score = score_init[i][j];
					score += allies_around(i, j, curr_board);
					score += (try_capture(encode_board(curr_board), i, j) * 10);
					ret.add(new int[] {i, j, score});
				}
			}
		}
		
		ret.sort((o1, o2)->(o2[2] - o2[1]));
		
		return ret;
	}
	
	private static int[][] decode(String code){
		int cnt = 0;
		int[][] ret = new int[5][5];
		
		for(int i = 0; i < 5; i++) {
			for(int j = 0; j < 5; j++) {
				ret[i][j] = code.charAt(cnt++) - '0';
			}
		}
		
		return ret;
	}
	
	private static int try_capture(String board_code, int i, int j) {
		int[][] board = decode(board_code);
		board[i][j] = side;
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
	
	private static int allies_around(int i, int j, int[][] board) {
		int cnt = 0;
		int player = side;
		
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
		return cnt;
	}
	
	private static ArrayList<int[]> max_q(double[][] q_values, int[][] board) {
		double max = -Double.MAX_VALUE;
		ArrayList<int[]> ret = new ArrayList<int[]>();
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if(q_values[i][j] > max) {
					ret.clear();
					max = q_values[i][j];
					ret.add(new int[] {i, j});
				}else if(q_values[i][j] ==  max) {
					ret.add(new int[] {i, j});
				}
			}
		}
		if(ret.size() > 1) {
			ret.sort((o1, o2)->(
					allies_around(o2[0], o2[1], board) -
					allies_around(o1[0], o1[1], board) + 
					(try_capture(encode_board(board), o2[0], o2[1]) - 
							try_capture(encode_board(board), o1[0], o1[1])) * 10
				));
		}
		return ret;
	}
	
	private static String encode_board(int[][] board) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				sb.append(board[i][j]);
			}
		}
		return sb.toString();
	}
	
	private static int[] cnt_score(int[][] board) {
		int[] ret = new int[2];
		if(side == 1) {
			ret[1] += 25;
		}else {
			ret[0] += 25;
		}
		for(int i = 0; i < size; i++) {
			for(int j = 0; j < size; j++) {
				if(board[i][j] == side) {
					ret[0] += 10;
				}
				if(board[i][j] == enemy) {
					ret[1] += 10;
				}
			}
		}
		return ret;
	}
	
	private static boolean is_valid_move(int i, int j, boolean pass) {
		
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
		int[][] test_board = copy_board(curr_board);
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
			}else if(captured == 1 && same_board(prev_board, test_board)) {
				//KO rule
				return false;
			}
		}
		return true;
	}
	
	private static boolean in_range(int i, int j) {
		return i < size && j < size && i >= 0 && j >=0;
	}
	
	private static int[][] copy_board(int[][] board){
		int[][] ret = new int[size][size];
		for(int i = 0; i < size; i++) {
			for(int j = 0; j < size; j++) {
				ret[i][j] = board[i][j];
			}
		}
		return ret;
	}
	
	private static boolean has_liberty(int[][] board, int i, int j, int player, int[][] visited) {
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
	
	private static int capture(int[][] board, int i, int j) {
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
	
	public static boolean same_board(int[][] b1, int[][] b2) {
		for(int i = 0; i < size; i++) {
			for(int j = 0; j < size; j++) {
				if(b1[i][j] != b2[i][j]) {
					return false;
				}
			}
		}
		return true;
	}
}
