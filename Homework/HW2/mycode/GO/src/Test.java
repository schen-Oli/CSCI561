import java.util.ArrayList;
import java.util.Arrays;

public class Test {

	public static int black = 1;
	public static int white = 2;
	
	public static void main(String[] args) {
		//Board board = new Board(null, false, false);
		Board board = new Board(null, true, true);
		
		TestPlayer mm_black = new TestPlayer(black);
		TestPlayer mm_white = new TestPlayer(white);
		
		TestPlayer white_rand = new TestPlayer(white);
		TestPlayer black_rand = new TestPlayer(black);
		
		int test_itr = 50;
//		
//		System.out.println("Testing against random player...");
//		battle(board, mm_white, test_itr/10 , black_rand, 1, true);
//		battle(board, mm_black, test_itr, white_rand, 1, true);
		
//		System.out.println("Testing against Qlearner...");
		battle(board, mm_white, test_itr , black_rand, 2, true);
		battle(board, mm_black, test_itr, white_rand, 2, true);
		
		System.out.println("Testing greedy...");
		battle(board, mm_white, test_itr/10 , black_rand, 3, true);
		battle(board, mm_black, test_itr, white_rand, 3, true);
//		
		System.out.println("Testing against Aggresive...");
		battle(board, mm_white, test_itr/10 , black_rand, 4,  true);
		battle(board, mm_black, test_itr, white_rand, 4,  true);
		
//		System.out.println("Testing against MinMax...");
//		battle(board, mm_white, test_itr, black_rand, 5, true);
//		battle(board, mm_black, test_itr, white_rand, 5,  true);
	}
	
	public static int play(Board board, TestPlayer player1, TestPlayer player2, int mode) {
		if(mode == 1) {
			random(board, player1, player2);
		}
		
		if(mode == 2) {
			qplayer(board, player1, player2);
		}
		
		if(mode == 3) {
			greedyplayer(board, player1, player2);
		}
		
		if(mode == 4) {
			aggresiveplayer(board, player1, player2);
		}
		
		if(mode == 5) {
			minmaxplayer(board, player1, player2);
		}
		
		return board.game_result;
	}
	
	public static int[] battle(Board board, TestPlayer player1, int itr, TestPlayer player2, int mode, boolean show) {
		int[] states= new int[3];
		for(int i = 0; i < itr; i++) {
			int res = play(board, player1, player2, mode);
			board.cleanup(); 
			if(res == player1.side) {
				states[0]++;
			}else if(res == 0) {
				states[1]++;
			}else {
				states[2]++;
			}
//			if(i % 10 == 0 ) {
//				String name = player1.side == 1 ? "Black" : "White";
//				System.out.println(name + ": iteration " + i + " finished");
//			}	
		}
		
		if(show) {
			String player = player1.side == 1 ? "Black" : "White";
			int invalid_moves = player1.side == 1 ? board.invalid_move_black : board.invalid_move_white;
			System.out.println("---------------------------------------------------");
			System.out.print("As " + player + "| Wins: " + Math.round(states[0]*1.0/itr*100) + "% (" + states[0] + ")");
			System.out.print("| Draws: " + Math.round(states[1]*1.0/itr*100) + "% (" + states[1] + ")");			System.out.println("| Losses: " + Math.round(states[2]*1.0/itr*100) + "% (" + states[2] + ")");
			System.out.println("invalid moves: " + invalid_moves);
			System.out.println("---------------------------------------------------");
		}
		
		return states;
	}
	
	
	public static void testMove(Board board, String s) {
		String[] arr = s.split(",");
		for(String m : arr) {
			board.move(m.charAt(0) - '0', m.charAt(1) - '0', m.charAt(2) - '0', false);
		}
	}
	public static int[][] decode(String s) {
		int cnt = 0;
		int[][] ret = new int[5][5];
		
		for(int i = 0; i < 5; i++) {
			for(int j = 0; j < 5; j++) {
				ret[i][j] = s.charAt(cnt++) - '0';
			}
		}
		
		return ret;
	}
	
	public static int[][] rotate(int[][] ori) {
    	int[][] new_state = new int[5][5];
    	for(int i = 0; i < 5; i++) {
    		for(int j = 0; j < 5; j++){
    			new_state[j][4 - i] = ori[i][j];
    		}
    	}
    	return new_state;
    }
	
	public static int[][] mirror(int[][] ori, int axis){
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
	
	public static void printArr(int[][] arr) {
		for(int i = 0; i < arr.length; i++) {
			for(int j = 0; j < arr.length; j++) {
				System.out.print(arr[i][j] + " ");
			}
			System.out.println("");
		}
		System.out.println("");
	}
	
	 public static int[] dedup(int row, int col, int mirrored, int rotation) {
	    	
	    	if(mirrored == 1) {
	    		row = 4 - row;
	    	}else if(mirrored == 2) {
	    		col = 4 - col;
	    	}
	    
	    	
	    	while(rotation > 0) {
	    		int new_row = 4 - col;
	    		int new_col = row;
	    		row = new_row;
	    		col = new_col;
	    		rotation --;
	    	}
	    	
	    	return new int[] {row, col};
	    }
	 
	 public static void random(Board board, TestPlayer player1, TestPlayer player2) {
			while(!board.game_over()) {
				
				if(player1.side == black) {
					player1.min_max(board);
					player2.random_move(board);
				}else {
					player2.random_move(board);			
					player1.min_max(board); 
				}
			}
			
		}
		
		public static void qplayer(Board board, TestPlayer player1, TestPlayer player2) {
			while(!board.game_over()) {
				if(player1.side == black) {
					player1.min_max(board);
					player2.q_move(board);
				}else {
					player2.q_move(board);
					player1.min_max(board);
				}			
			}
			
		}
		
		public static void greedyplayer(Board board, TestPlayer player1, TestPlayer player2) {
			while(!board.game_over()) {
				if(player1.side == black) {
					player1.min_max(board);
					player2.greedy_move(board);
				}else {
					player2.greedy_move(board);
					player1.min_max(board);
				}			
			}
			
		}
		
		public static void aggresiveplayer(Board board, TestPlayer player1, TestPlayer player2) {
			while(!board.game_over()) {
				if(player1.side == black) {
					player1.min_max(board);
					player2.aggresive_move(board);
				}else {
					player2.aggresive_move(board);
					player1.min_max(board);
				}			
			}
		
		}
		
		public static void minmaxplayer(Board board, TestPlayer player1, TestPlayer player2) {
			while(!board.game_over()) {
				if(player1.side == black) {
					player1.min_max(board);
					player2.min_max(board);
				}else {
					player2.min_max(board);
					player1.min_max(board);
				}			
			}
		
		}
	 
}
