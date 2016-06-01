package com.example.chess2;

import java.util.*;
import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.util.*;
import android.view.*;
import android.widget.Toast;

public class boardView extends View {

	private boolean frame;
	private int press_x, press_y, release_X, release_y, width,  size;
	private Resources res;
	private Canvas can;
	private RectF bounding_box;
	private Paint light,dark,border;
	private chess_board deadsquare;
	private chess_board board[][];
	private player white, black;
	private piece_color turn;
	
	private interface availableMoves {
		public ArrayList<chess_board> getAvailableMoves(piece piece);
	}
	
	private enum piece_type {
		pawn,rook,knight,bishop, queen,king;
	}
	private enum piece_color {
		black, white;
	}
	private enum piece_state {
		alive, dead;
	}
	private enum game_state {
		reset, check_w, check_b, stalemate;
	}
	private enum move_status {
		yes, no, tmp;
	}
	public boardView(Context c) {
		super(c);
		init();
	}
	
	public boardView(Context c,AttributeSet as) {
		super(c,as);
		init();
	}
	
	public boardView(Context c, AttributeSet as, int default_style) {
		super(c, as, default_style);
		init();
	}
	
	public void init() {
		frame = false;
		press_x =-1;
		press_y = -1;
		release_X = -1;
		release_y = -1;
		res = getResources();
		bounding_box = new RectF();
		dark = new Paint(Paint.ANTI_ALIAS_FLAG);
		dark.setColor(0xFFCC9900);
		light = new Paint(Paint.ANTI_ALIAS_FLAG);
		light.setColor(0x00FFCC00);
		border = new Paint(Paint.ANTI_ALIAS_FLAG);
		border.setColor(0xFF000000);
		border.setStrokeWidth(2);
		deadsquare = new chess_board(-1, -1);
		board = new chess_board[8][8];
		assign_pieces(board);
		turn = piece_color.white;
	}
	
	@Override
	protected void onDraw(Canvas canvas) 
	{		
		this.drawBoard(getBoard(),canvas);
		if(frame)
			available_moves(getBoard(), press_x, press_y);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() != MotionEvent.ACTION_DOWN)
			return super.onTouchEvent(event);
		
		if(!frame) {	
			press_x = (int)(event.getX() / (size));
			press_y = (int)(event.getY() / (size));
			if( press_x < 0 || press_x > 7 || press_y < 0 || press_y > 7)
				return true;
		}
		else {
			release_X = (int)(event.getX() / (size));
			release_y = (int)(event.getY() / (size));
			if( release_X < 0 || release_X > 7 || release_y < 0 || release_y > 7 || (release_X == press_x && release_y == press_y));
			else {
				try {
					move_player(press_x, press_y, release_X,release_y);
					frame = false;
					return true;
				}catch( Exception ex) {
					frame = false;
					return true;
				}
			}
		}
		frame = !frame;
		return true;
	}
	
	public int getBoardX(int x) {
		x=x* size;
		return x;
	}
	
	public int getBoardY(int y) {
		y=y*size;
		return y;
	}
	
	public void drawBoard(chess_board[][] board, Canvas canvas) {	
		Paint activeColor;
		this.can = canvas;
		super.invalidate();
		width =  can.getWidth();
		size = width/8;
		for(int i = 0; i < 8; i++) {
			for(int j = 0; j < 8; j++) {
				bounding_box.top = i*size;
				bounding_box.left = j*size;
				bounding_box.bottom = (i+1) * size;
				bounding_box.right = (j+1) * size;
				
				if(i%2 == 0) {
					if(j%2 == 0) {
						activeColor = light;
					}
					else {
						activeColor = dark;
					}
				}
				else {
					if(j%2 == 0) {
						activeColor = dark;
					}
					else {
						activeColor = light;
					}
				}
				can.drawRect(bounding_box, activeColor);
			}
		}
		can.drawLine(0, 0, 0, 8*size, border);
		can.drawLine(0, 0, 8*size, 0, border);
		can.drawLine(8*size, 0, 8*size, 8*size, border);
		can.drawLine(0, 8*size, 8*size, 8*size, border);
		
		for(int i=0 ; i<8 ; i++) {
			for(int j=0 ; j<8 ; j++) {
				piece piece = board[i][j].get_piece();
				if(piece != null) {
					Drawable figure = res.getDrawable(piece.getImageResource());
					figure.setBounds(getBoardX(i), getBoardY(j), getBoardX(i) + size, getBoardY(j) + size );
					figure.draw(canvas);
				}
			}
		}
	}
	
	void assign_pieces(chess_board[][] board) {
		piece blackPieces[] = new piece[16];
		piece whitePieces[] = new piece[16];

		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 8; y++) {
				board[x][y] = new chess_board(x, y);
			}
		}

		for (int i = 0; i < 8; i++) {
			piece whitePawn = new piece(piece_color.white, piece_type.pawn, board[i][6], new pawn(), R.drawable.white_pawn);
			whitePieces[i] = whitePawn;
			board[i][6].set_piece(whitePawn);
			piece blackPawn = new piece(piece_color.black, piece_type.pawn,board[i][1], new pawn(), R.drawable.black_pawn);
			blackPieces[i] = blackPawn;
			board[i][1].set_piece(blackPawn);
		}

		whitePieces[8] = new piece(piece_color.white, piece_type.rook,board[0][7], new rook(), R.drawable.white_rook);
		board[0][7].set_piece(whitePieces[8]);
		whitePieces[9] = new piece(piece_color.white, piece_type.rook,board[7][7], new rook(), R.drawable.white_rook);
		board[7][7].set_piece(whitePieces[9]);
		blackPieces[8] = new piece(piece_color.black, piece_type.rook,board[0][0], new rook(), R.drawable.black_rook);
		board[0][0].set_piece(blackPieces[8]);
		blackPieces[9] = new piece(piece_color.black, piece_type.rook,board[7][0], new rook(), R.drawable.black_rook);
		board[7][0].set_piece(blackPieces[9]);
		
		whitePieces[10] = new piece(piece_color.white, piece_type.knight,board[1][7], new knight(), R.drawable.white_knight);
		board[1][7].set_piece(whitePieces[10]);
		whitePieces[11] = new piece(piece_color.white, piece_type.knight,board[6][7], new knight(), R.drawable.white_knight);
		board[6][7].set_piece(whitePieces[11]);
		blackPieces[10] = new piece(piece_color.black, piece_type.knight,board[1][0], new knight(), R.drawable.black_knight);
		board[1][0].set_piece(blackPieces[10]);
		blackPieces[11] = new piece(piece_color.black, piece_type.knight,board[6][0], new knight(), R.drawable.black_knight);
		board[6][0].set_piece(blackPieces[11]);
		
		whitePieces[12] = new piece(piece_color.white, piece_type.bishop,board[2][7], new bishop(), R.drawable.white_bishop);
		board[2][7].set_piece(whitePieces[12]);
		whitePieces[13] = new piece(piece_color.white, piece_type.bishop,board[5][7], new bishop(), R.drawable.white_bishop);
		board[5][7].set_piece(whitePieces[13]);
		blackPieces[12] = new piece(piece_color.black, piece_type.bishop,board[2][0], new bishop(), R.drawable.black_bishop);
		board[2][0].set_piece(blackPieces[12]);
		blackPieces[13] = new piece(piece_color.black, piece_type.bishop,board[5][0], new bishop(), R.drawable.black_bishop);
		board[5][0].set_piece(blackPieces[13]);
		
		whitePieces[14] = new piece(piece_color.white, piece_type.queen,board[3][7], new queen(), R.drawable.white_queen);
		board[3][7].set_piece(whitePieces[14]);
		blackPieces[14] = new piece(piece_color.black, piece_type.queen,board[3][0], new queen(), R.drawable.black_queen);
		board[3][0].set_piece(blackPieces[14]);
		
		whitePieces[15] = new piece(piece_color.white, piece_type.king,board[4][7], new king(), R.drawable.white_king);
		board[4][7].set_piece(whitePieces[15]);
		blackPieces[15] = new piece(piece_color.black, piece_type.king,board[4][0], new king(), R.drawable.black_king);
		board[4][0].set_piece(blackPieces[15]);

		white = new player(piece_color.white, whitePieces);
		black = new player(piece_color.black, blackPieces);
	}
	
	public void available_moves(chess_board[][] board, int x, int y) {
		piece piece = board[x][y].get_piece();
		if(piece != null && piece.get_piece_color() == getTurn()) {
			Drawable selection = res.getDrawable(R.drawable.frame);
			selection.draw(can);
			
			ArrayList<chess_board> possible_moves = piece.getAvailableMoves();
			for(int i = 0; i < possible_moves.size(); i ++) {
				if(piece.valid_move(possible_moves.get(i))) {
					chess_board availMove = possible_moves.get(i);
					Drawable circle = res.getDrawable(R.drawable.frame);
					circle.setBounds(getBoardX(availMove.get_x()), getBoardY(availMove.get_y()), getBoardX(availMove.get_x()) + size, getBoardY(availMove.get_y()) + size);
					circle.draw(can);
				}
			}
		}
	}
	public boolean move_player(int from_x, int from_y, int toX, int toY) throws Exception {
		player currentPlayer = turn == piece_color.white ? white : black;
		piece piece = board[from_x][from_y].get_piece();
		boolean yes = currentPlayer.move(piece, board[toX][toY]);
		if (yes)
			if(turn == piece_color.white)
				turn = piece_color.black;
			else
				turn = piece_color.white;
		Toast.makeText(getContext(), turn.toString() + " turn", Toast.LENGTH_LONG).show();
		return yes;
	}

	private class player {
		private piece_color Color;
		private piece[] pieces;
		
		player(piece_color Color, piece[] pieces) {
			this.Color = Color;
			this.pieces = pieces;
		}

		public piece[] get_pieces() {
			return pieces;
		}

		public boolean move(piece piece, chess_board move_to) {
			if (piece == null || piece.get_piece_color() != this.Color || move_to == null || move_to == deadsquare)
				return false;
			ArrayList<chess_board> availableMoves = piece.getAvailableMoves();
			if (availableMoves.contains(move_to)) {
				move_status status = piece.try_move(move_to);
				if (status != move_status.tmp) {
					if (status == move_status.yes) {
						return true;
					} else
						return false;
				} else {
					try {
						piece_type type = piece_type.bishop;
						piece.set_piece_type(type);
						return true;
					} catch (Exception ex) {}
				}
			}
			return false;
		}
	}

	public class chess_board {
		private int x;
		private int y;
		private piece piece;
		
		chess_board(int x, int y) {
			this.x = x;
			this.y = y;
			this.piece = null;
		}
		public int get_x() {
			return x;
		}
		public int get_y() {
			return y;
		}
		public piece get_piece() {
			return piece;
		}
		public void set_piece(piece piece) {
			if (this.piece != null)
				this.piece.set_piece_state(piece_state.dead);
			this.piece = piece;
		}
	}

	public class piece {

		 chess_board location;
		 piece_state state;
		 int imageResource;
		 piece_type type;
		 availableMoves possible_moves;
		 piece_color Color;
		
		piece(piece_color Color, piece_type type, chess_board location, availableMoves movementPattern, int imageResource) {
			this.Color = Color;
			this.type = type;
			this.location = location;
			this.possible_moves = movementPattern;
			this.imageResource = imageResource;
		}

		public chess_board getLocation() {
			return location;
		}

		public piece_state get_piece_state() {
			return state;
		}

		public void set_piece_state(piece_state newState) {
			state = newState;
			if (newState == piece_state.dead) {
				location.piece  = null;
				location = deadsquare;
			}
		}

		public int getImageResource() {
			return imageResource;
		}

		public piece_color get_piece_color() {
			return Color;
		}

		public piece_type get_piece_type() {
			return type;
		}

		public void set_piece_type(piece_type newType) {
			if (type != piece_type.pawn){ }
			else {
				switch (newType) {
				case bishop:
					possible_moves = new bishop();
					break;
				case knight:
					possible_moves = new knight();
					break;
				case rook:
					possible_moves = new rook();
					break;
				case queen:
					possible_moves = new queen();
					break;
				case pawn:
				case king:
					
				}

			}
		}

		public boolean valid_move(chess_board move_to) {
			
			if(move_to.get_piece() != null)
				if(move_to.get_piece().get_piece_color() == this.Color)
					return false;
			
			chess_board targetsquare = board[move_to.get_x()][move_to.get_y()];
			piece current_piece = targetsquare.get_piece();
			chess_board sourcesquare = this.getLocation();
			targetsquare.set_piece(this);
			sourcesquare.set_piece(null);
			this.location = targetsquare;
			this.set_piece_state(piece_state.alive);
			game_state tryState = checkForCheck(board, this.Color);
			sourcesquare.set_piece(this);
			targetsquare.set_piece(current_piece);
			this.location = sourcesquare;
			this.set_piece_state(piece_state.alive);
			if(current_piece!= null) {
				current_piece.location = targetsquare;
				current_piece.set_piece_state(piece_state.alive);
			}
		
			if ((Color == piece_color.white && tryState == game_state.check_w) || (Color == piece_color.black && tryState == game_state.check_b))
				return false;
			return true;
		}

		public move_status try_move(chess_board move_to) {

			if (!valid_move(move_to))
				return move_status.no;
			this.location.set_piece(null);
			move_to.set_piece(this);
			this.location = move_to;

			if (type == piece_type.pawn) {
				return (location.y == 0 || location.y == 7) ? move_status.tmp: move_status.yes;
			}

			return move_status.yes;
		}

		public ArrayList<chess_board> getAvailableMoves() {
			return possible_moves.getAvailableMoves(this);
		}
	}

	

	private class pawn implements availableMoves {
		public ArrayList<chess_board> getAvailableMoves(piece piece) {
			if (piece.getLocation() == deadsquare)
				return null;
			ArrayList<chess_board> chess_list = new ArrayList<chess_board>();
			int x_value = piece.getLocation().get_x();
			int y_value = piece.getLocation().get_y();

			if (piece.get_piece_color() == piece_color.white) {
				if (board[x_value][y_value - 1].get_piece() == null)
					chess_list.add(board[x_value][y_value - 1]);

				if (x_value > 0 && board[x_value - 1][y_value - 1].get_piece() != null && board[x_value - 1][y_value - 1].get_piece().get_piece_color() == piece_color.black)
					chess_list.add(board[x_value - 1][y_value - 1]);

				if (x_value < 7 && board[x_value + 1][y_value - 1].get_piece() != null && board[x_value + 1][y_value - 1].get_piece().get_piece_color() == piece_color.black)
					chess_list.add(board[x_value + 1][y_value - 1]);

				if (y_value == 6 && board[x_value][y_value - 1].get_piece() == null && board[x_value][y_value - 2].get_piece() == null)
					chess_list.add(board[x_value][y_value - 2]);
			}
			if (piece.get_piece_color() == piece_color.black) {
				if (board[x_value][y_value + 1].get_piece() == null)
					chess_list.add(board[x_value][y_value + 1]);

				if (x_value > 0&& board[x_value - 1][y_value + 1].get_piece() != null && board[x_value - 1][y_value + 1].get_piece().get_piece_color() == piece_color.white)
					chess_list.add(board[x_value - 1][y_value + 1]);

				if (x_value < 7 && board[x_value + 1][y_value + 1].get_piece() != null && board[x_value + 1][y_value + 1].get_piece().get_piece_color() == piece_color.white)
					chess_list.add(board[x_value + 1][y_value + 1]);

				if (y_value == 1 && board[x_value][y_value + 1].get_piece() == null && board[x_value][y_value + 2].get_piece() == null)
					chess_list.add(board[x_value][y_value + 2]);
			}
			return chess_list;
		}
	}

	private class bishop implements availableMoves {
		public ArrayList<chess_board> getAvailableMoves(piece piece) {
			if (piece.getLocation() == deadsquare)
				return null;
			ArrayList<chess_board> chess_list = new ArrayList<chess_board>();
			int x_value = piece.getLocation().get_x();
			int y_value = piece.getLocation().get_y();
			int i = 1;

			while ((x_value + i < 8 && y_value + i < 8) && (board[x_value + i][y_value + i].get_piece() == null || board[x_value + i][y_value + i].get_piece().get_piece_color() != piece.get_piece_color())) {
				if (board[x_value + i][y_value + i].get_piece() != null && board[x_value + i][y_value + i].get_piece().get_piece_color() != piece.get_piece_color()) {
					chess_list.add(board[x_value + i][y_value + i]);
					break;
				}
				chess_list.add(board[x_value + i][y_value + i]);
				i++;
			}

			i = 1;
			while ((x_value + i < 8 && y_value - i > -1) && (board[x_value + i][y_value - i].get_piece() == null || board[x_value + i][y_value - i].get_piece().get_piece_color() != piece.get_piece_color())) {
				if (board[x_value + i][y_value - i].get_piece() != null && board[x_value + i][y_value - i].get_piece().get_piece_color() != piece.get_piece_color()) {
					chess_list.add(board[x_value + i][y_value - i]);
					break;
				}
				chess_list.add(board[x_value + i][y_value - i]);
				i++;
			}

			i = 1;
			while ((x_value - i > -1 && y_value + i < 8) && (board[x_value - i][y_value + i].get_piece() == null || board[x_value - i][y_value + i].get_piece().get_piece_color() != piece.get_piece_color())) {
				if (board[x_value - i][y_value + i].get_piece() != null && board[x_value - i][y_value + i].get_piece().get_piece_color() != piece.get_piece_color()) {
					chess_list.add(board[x_value - i][y_value + i]);
					break;
				}
				chess_list.add(board[x_value - i][y_value + i]);
				i++;
			}

			i = 1;
			while ((x_value - i > -1 && y_value - i > -1) && (board[x_value - i][y_value - i].get_piece() == null || board[x_value - i][y_value - i].get_piece().get_piece_color() != piece.get_piece_color())) {
				if (board[x_value - i][y_value - i].get_piece() != null && board[x_value - i][y_value - i].get_piece().get_piece_color() != piece.get_piece_color()) { 
					chess_list.add(board[x_value - i][y_value - i]);
					break;
				}
				chess_list.add(board[x_value - i][y_value - i]);
				i++;
			}

			return chess_list;
		}
	}

	private class knight implements availableMoves {
		public ArrayList<chess_board> getAvailableMoves(piece piece) {
			if (piece.getLocation() == deadsquare)
				return null;
			ArrayList<chess_board> chess_list = new ArrayList<chess_board>();
			int x_value = piece.getLocation().get_x();
			int y_value = piece.getLocation().get_y();

			if (x_value > 1 && y_value > 0 && (board[x_value - 2][y_value - 1].get_piece() == null || board[x_value - 2][y_value - 1].get_piece().get_piece_color() != piece.get_piece_color())) chess_list.add(board[x_value - 2][y_value - 1]);

			if (x_value > 0 && y_value > 1 && (board[x_value - 1][y_value - 2].get_piece() == null || board[x_value - 1][y_value - 2].get_piece().get_piece_color() != piece.get_piece_color())) chess_list.add(board[x_value - 1][y_value - 2]);

			if (x_value < 7 && y_value > 1 && (board[x_value + 1][y_value - 2].get_piece() == null || board[x_value + 1][y_value - 2].get_piece().get_piece_color() != piece.get_piece_color())) chess_list.add(board[x_value + 1][y_value - 2]);

			if (x_value < 6 && y_value > 0 && (board[x_value + 2][y_value - 1].get_piece() == null || board[x_value + 2][y_value - 1].get_piece().get_piece_color() != piece.get_piece_color())) chess_list.add(board[x_value + 2][y_value - 1]);

			if (x_value < 6 && y_value < 7 && (board[x_value + 2][y_value + 1].get_piece() == null || board[x_value + 2][y_value + 1].get_piece().get_piece_color() != piece.get_piece_color())) chess_list.add(board[x_value + 2][y_value + 1]);

			if (x_value < 7 && y_value < 6 && (board[x_value + 1][y_value + 2].get_piece() == null || board[x_value + 1][y_value + 2].get_piece().get_piece_color() != piece.get_piece_color())) chess_list.add(board[x_value + 1][y_value + 2]);

			if (x_value > 0 && y_value < 6 && (board[x_value - 1][y_value + 2].get_piece() == null || board[x_value - 1][y_value + 2].get_piece().get_piece_color() != piece.get_piece_color())) chess_list.add(board[x_value - 1][y_value + 2]);

			if (x_value > 1 && y_value < 7 && (board[x_value - 2][y_value + 1].get_piece() == null || board[x_value - 2][y_value + 1].get_piece().get_piece_color() != piece.get_piece_color())) chess_list.add(board[x_value - 2][y_value + 1]);

			return chess_list;
		}
	}

	private class rook implements availableMoves {
		public ArrayList<chess_board> getAvailableMoves(piece piece) {
			if (piece.getLocation() == deadsquare)
				return null;
			ArrayList<chess_board> chess_list = new ArrayList<chess_board>();
			int x_value = piece.getLocation().get_x();
			int y_value = piece.getLocation().get_y();
			int i = 1;

			while (x_value + i < 8 && (board[x_value + i][y_value].get_piece() == null || board[x_value + i][y_value].get_piece().get_piece_color() != piece.get_piece_color())) {
				if (board[x_value + i][y_value].get_piece() != null && board[x_value + i][y_value].get_piece().get_piece_color() != piece.get_piece_color()) {
					chess_list.add(board[x_value + i][y_value]);
					break;
				}
				chess_list.add(board[x_value + i][y_value]);
				i++;
			}

			i = 1;
			while (x_value - i > -1 && (board[x_value - i][y_value].get_piece() == null || board[x_value - i][y_value].get_piece().get_piece_color() != piece.get_piece_color())) {
				if (board[x_value - i][y_value].get_piece() != null && board[x_value - i][y_value].get_piece().get_piece_color() != piece.get_piece_color()) {
					chess_list.add(board[x_value - i][y_value]);
					break;
				}
				chess_list.add(board[x_value - i][y_value]);
				i++;
			}

			i = 1;
			while (y_value + i < 8 && (board[x_value][y_value + i].get_piece() == null || board[x_value][y_value + i].get_piece().get_piece_color() != piece.get_piece_color())) {
				if (board[x_value][y_value + i].get_piece() != null && board[x_value][y_value + i].get_piece().get_piece_color() != piece.get_piece_color()) {
					chess_list.add(board[x_value][y_value + i]);
					break;
				}
				chess_list.add(board[x_value][y_value + i]);
				i++;
			}

			i = 1;
			while (y_value - i > -1 && (board[x_value][y_value - i].get_piece() == null || board[x_value][y_value - i].get_piece().get_piece_color() != piece.get_piece_color())) {
				if (board[x_value][y_value - i].get_piece() != null && board[x_value][y_value - i].get_piece().get_piece_color() != piece.get_piece_color()) {
					chess_list.add(board[x_value][y_value - i]);
					break;
				}
				chess_list.add(board[x_value][y_value - i]);
				i++;
			}
			return chess_list;
		}
	}

	private class queen implements availableMoves {
		private rook horizontalVerical;
		private bishop diagonal;

		public ArrayList<chess_board> getAvailableMoves(piece piece) {
			if (piece.getLocation() == deadsquare)
				return null;
			horizontalVerical = new rook();
			diagonal = new bishop();
			ArrayList<chess_board> chess_list = horizontalVerical.getAvailableMoves(piece);
			ArrayList<chess_board> new_move = diagonal.getAvailableMoves(piece);
			for (int i = 0; i < new_move.size(); i++) {
				chess_list.add(new_move.get(i));
			}
			return chess_list;
		}
	}

	private class king implements availableMoves {
		public ArrayList<chess_board> getAvailableMoves(piece piece) {
			if (piece.getLocation() == deadsquare)
				return null;
			ArrayList<chess_board> chess_list = new ArrayList<chess_board>();
			int x_value = piece.getLocation().get_x();
			int y_value = piece.getLocation().get_y();

			if (x_value > 0 && y_value > 0 && (piece.valid_move(board[x_value - 1][y_value - 1])))
				chess_list.add(board[x_value - 1][y_value - 1]);

			if (y_value > 0 && (piece.valid_move(board[x_value][y_value - 1])))
				chess_list.add(board[x_value][y_value - 1]);

			if (x_value < 7 && y_value > 0 && (piece.valid_move(board[x_value + 1][y_value - 1])))
				chess_list.add(board[x_value + 1][y_value - 1]);

			if (x_value < 7 && (piece.valid_move(board[x_value + 1][y_value])))
				chess_list.add(board[x_value + 1][y_value]);

			if (x_value < 7 && y_value < 7 && (piece.valid_move(board[x_value + 1][y_value + 1])))
				chess_list.add(board[x_value + 1][y_value + 1]);

			if (y_value < 7 && (piece.valid_move(board[x_value][y_value + 1])))
				chess_list.add(board[x_value][y_value + 1]);

			if (x_value > 0 && y_value < 7 && (piece.valid_move(board[x_value - 1][y_value + 1])))
				chess_list.add(board[x_value - 1][y_value + 1]);

			if (x_value > 0 && piece.valid_move(board[x_value - 1][y_value]))
				chess_list.add(board[x_value - 1][y_value]);

			return chess_list;
		}
	}

	
	public chess_board[][] getBoard() {
		return board;
	}

	public piece_color getTurn() {
		return turn;
	}

	private game_state checkForCheck(chess_board board[][], piece_color check) {
		piece current_player[];
		piece opposit_player[];
		if (check == piece_color.white) {
			current_player = white.get_pieces();
			opposit_player = black.get_pieces();
		} else {
			current_player = black.get_pieces();
			opposit_player = white.get_pieces();
		}
		
		for (int i = 0; i < 15; i++) {
			ArrayList<chess_board> moves = opposit_player[i].getAvailableMoves();
			if (moves != null && (!moves.isEmpty() && moves.contains(current_player[15].getLocation()))) {
				if (check == piece_color.white)
					return game_state.check_w;
				else
					return game_state.check_b;
			}
		}
		return game_state.reset;
	}

	
}