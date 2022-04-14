

public class Host {

	public static int white = 2;
	public static int black = 1;
	
	public static void main(String[] args) {
		Qlearner white_player = new Qlearner(white);
		Qlearner black_player = new Qlearner(black);
		TestPlayer white_rand = new TestPlayer(white);
		TestPlayer black_rand = new TestPlayer(black);
		int itr = 10000;
		
        //Training
		Board board = new Board(null, false, false);
		
//		//Training As White
//		System.out.println("Training As White...");
//	
//		System.out.println("Training against random player...");
//		battle(board, white_player, itr, black_rand, 1, true, false);
//		
//		System.out.println("Training against Qlearner...");
//		battle(board, white_player, itr, black_rand, 2, true, false);
//		
//		System.out.println("Training against greedy player...");
//		battle(board, white_player, itr, black_rand, 3, true, false);
//		
//		System.out.println("Training against aggresive player...");
//		battle(board, white_player, itr, black_rand, 4, true, false);
//		
//		System.out.println("Training against MinMax...");
//		battle(board, white_player, 1000, black_rand, 5, true, false);
//		
//		
//		Training As Black
//		System.out.println("Training As Black...");
//		
//		System.out.println("Training against random player...");
//		battle(board, black_player, 2 * itr, white_rand, 1, true, false);
//		
//		System.out.println("Training against Qlearner...");
//		battle(board, black_player, itr, white_rand, 2, true, false);
//		
//		System.out.println("Training against greedy player...");
//		battle(board, black_player, itr, white_rand, 3, true, false);
//		
//		System.out.println("Training against aggresive player...");
//		battle(board, black_player, itr, white_rand, 4, true, false);
//		
//		System.out.println("Training against MinMax...");
//		battle(board, black_player, itr, white_rand, 5, true, false);
//		
//		testing
		Board board2 = new Board(null, true, true);
	//	board2 = new Board(null, false, false);
		int test_itr = 50;
////		
//		System.out.println("Testing against random player...");
//		battle(board2, white_player, test_itr , black_rand, 1, false, true);
//		battle(board2, black_player, test_itr, white_rand, 1, false, true);
////		
////		System.out.println("Testing against Qlearner...");
////		battle(board2, white_player, test_itr , black_rand, 2, false, true);
////		battle(board2, black_player, test_itr, white_rand, 2, false, true);
////		
//		System.out.println("Testing greedy ...");
//		battle(board2, white_player, test_itr , black_rand, 3, false, true);
//		battle(board2, black_player, test_itr, white_rand, 3, false, true);
////		
//		System.out.println("Testing against Aggresive...");
//		battle(board2, white_player, test_itr , black_rand, 4, false, true);
//		battle(board2, black_player, test_itr, white_rand, 4, false, true);
		
		System.out.println("Testing against MinMax...");
		battle(board2, white_player, test_itr, black_rand, 5, false, true);
		battle(board2, black_player, test_itr, white_rand, 5, false, true);

	}
	
	public static int play(Board board, Qlearner player1, TestPlayer player2, int mode, boolean learn) {
		if(mode == 1) {
			random(board, player1, player2, learn);
		}
		
		if(mode == 2) {
			qplayer(board, player1, player2, learn);
		}
		
		if(mode == 3) {
			greedyplayer(board, player1, player2, learn);
		}
		
		if(mode == 4) {
			aggresiveplayer(board, player1, player2, learn);
		}
		
		if(mode == 5) {
			minmaxplayer(board, player1, player2, learn);
		}
		return board.game_result;
	}
	
	public static int[] battle(Board board, Qlearner player1, int itr, TestPlayer player2, int mode, boolean learn, boolean show) {
		int[] states= new int[3];
		for(int i = 0; i < itr; i++) {
			int res = play(board, player1, player2, mode, learn);
			board.cleanup(); 
			if(res == player1.side) {
				states[0]++;
			}else if(res == 0) {
				states[1]++;
			}else {
				states[2]++;
			}
//			if(mode == 5) {
//				String name = player1.side == 1 ? "Black" : "White";
//				System.out.println(name + ": iteration " + (i+1) + " finished");
//			}else {
				if(i % 1000 == 0 && learn) {
					String name = player1.side == 1 ? "Black" : "White";
					System.out.println(name + ": iteration " + (i+1000) + " finished");
				}	
			}
				
	//	}
		
		if(learn) {
			player1.writeFile();
		}
		
		if(show) {
			String player = player1.side == 1 ? "Black" : "White";
			int invalid_moves = player1.side == 1 ? board.invalid_move_black : board.invalid_move_white;
			System.out.println("---------------------------------------------------");
			System.out.print("As " + player + "| Wins: " + Math.round(states[0]*1.0/itr*100) + "% (" + states[0] + ")");
			System.out.print("| Draws: " + Math.round(states[1]*1.0/itr*100) + "% (" + states[1] + ")");
			System.out.println("| Losses: " + Math.round(states[2]*1.0/itr*100) + "% (" + states[2] + ")");
			System.out.println("invalid moves: " + invalid_moves);
			System.out.println("---------------------------------------------------");
		}
		
		return states;
	}

	public static void random(Board board, Qlearner player1, TestPlayer player2, boolean learn) {
		while(!board.game_over()) {
			if(player1.side == black) {
				player1.move(board);
				player2.random_move(board);
			}else {
				player2.random_move(board);			
				player1.move(board);		 
			}
		}
		
		if(learn) {
			player1.learn(board);
		}
	}
	
	public static void qplayer(Board board, Qlearner player1, TestPlayer player2, boolean learn) {
		while(!board.game_over()) {
			if(player1.side == black) {
				player1.move(board);
				player2.q_move(board);
			}else {
				player2.q_move(board);
				player1.move(board);
			}			
		}
		
		if(learn) {
			player1.learn(board);
		}
	}
	
	public static void greedyplayer(Board board, Qlearner player1, TestPlayer player2, boolean learn) {
		while(!board.game_over()) {
			if(player1.side == black) {
				player1.move(board);
				player2.greedy_move(board);
			}else {
				player2.greedy_move(board);
				player1.move(board);
			}			
		}
		
		if(learn) {
			player1.learn(board);
		}
	}
	
	public static void aggresiveplayer(Board board, Qlearner player1, TestPlayer player2, boolean learn) {
		while(!board.game_over()) {
			if(player1.side == black) {
				player1.move(board);
				player2.aggresive_move(board);
			}else {
				player2.aggresive_move(board);
				player1.move(board);
			}			
		}
		
		if(learn) {
			player1.learn(board);
		}
	}
	
	public static void minmaxplayer(Board board, Qlearner player1, TestPlayer player2, boolean learn) {
		while(!board.game_over()) {
			if(player1.side == black) {
				player1.move(board);
				player2.min_max(board);
				//board.print_board();
			}else {
				player2.min_max(board);
				player1.move(board);
				//board.print_board();
			}			
		}
		
		if(learn) {
			player1.learn(board);
		}
	}
}
