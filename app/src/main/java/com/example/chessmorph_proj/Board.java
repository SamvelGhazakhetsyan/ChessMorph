package com.example.chessmorph_proj;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Board extends AppCompatActivity {

    // Размер доски
    private static final int BOARD_SIZE = 8;
    private GridLayout chessBoard;
    private ImageView selectedPiece = null;
    private int selectedRow = -1, selectedCol = -1, circle=R.drawable.gray_circle, krug=R.drawable.gray_krug_24;
    private boolean isWhiteTurn = true, isBlackCheck = false, isWhiteCheck = false, isCheck = false, morphModeOn=true, turnTheBoardOn=true, canMove = false, isOnlineGame, isWhite=true;
    private Piece BlackKing = new King(0, 4, false);
    private Piece WhiteKing = new King(7, 4, true);
    private Piece checker=null;
    private List<int[]> validMovesForDraw;
    private Piece[][] boardSetup = new Piece[8][8];
    private ImageView[][] cells = new ImageView[8][8];
    private Piece[][] morphBoard = new Piece[8][8];

    private TextView whiteTimerText, blackTimerText;
    private CountDownTimer whiteTimer, blackTimer;
    private long whiteTimeLeft = 300000, blackTimeLeft = 300000, plusTime=0; // 5
    private String gameId, userId, onlineTurn, opponentId;
    DatabaseReference gameRef;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //ориентация вертикальная
        setContentView(R.layout.activity_board);

        isOnlineGame = getIntent().getBooleanExtra("isOnlineGame", false);
        whiteTimeLeft= getIntent().getLongExtra("time", 300000);
        blackTimeLeft=whiteTimeLeft;
        plusTime=getIntent().getLongExtra("plusTime", 0);
        morphModeOn=getIntent().getBooleanExtra("morphModeOn", false);
        gameId=getIntent().getStringExtra("gameId");
        userId=getIntent().getStringExtra("userId");
        isWhite=getIntent().getBooleanExtra("isWhite", true);
        isWhite=(isOnlineGame)?isWhite:true;
        /*gameRef.child("turn").get().addOnSuccessListener(snapshot -> {
            onlineTurn = snapshot.getValue(String.class);
        });*/
        if(isOnlineGame){
            gameRef = FirebaseDatabase.getInstance().getReference("games").child(gameId);
            listenForMoves();
        }

        chessBoard = findViewById(R.id.chessBoard);

        setupBoard();

        whiteTimerText = findViewById(R.id.whiteTimer);
        blackTimerText = findViewById(R.id.blackTimer);

        updateTimerUI();
        startWhiteTimer();
    }

    private void setupBoard() {
        // row=0
        boardSetup[0][0] = new Rook(0, 0, false);
        boardSetup[0][1] = new Knight(0, 1, false);
        boardSetup[0][2] = new Bishop(0, 2, false);
        boardSetup[0][3] = new Queen(0, 3, false);
        boardSetup[0][4] = BlackKing;
        boardSetup[0][5] = new Bishop(0, 5, false);
        boardSetup[0][6] = new Knight(0, 6, false);
        boardSetup[0][7] = new Rook(0, 7, false);

        // Расставляем белые пешки
        for (int i = 0; i < 8; i++) {
            boardSetup[1][i] = new Pawn(1, i, false);
        }

        // Расставляем черные фигуры, row=7
        boardSetup[7][0] = new Rook(7, 0, true);
        boardSetup[7][1] = new Knight(7, 1, true);
        boardSetup[7][2] = new Bishop(7, 2, true);
        boardSetup[7][3] = new Queen(7, 3, true);
        boardSetup[7][4] = WhiteKing;
        boardSetup[7][5] = new Bishop(7, 5, true);
        boardSetup[7][6] = new Knight(7, 6, true);
        boardSetup[7][7] = new Rook(7, 7, true);

        // Расставляем черные пешки
        for (int i = 0; i < 8; i++) {
            boardSetup[6][i] = new Pawn(6, i, true);
        }



        // Создаём клетки и добавляем фигуры
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                ImageView cell = new ImageView(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 0;
                params.height = 0;
                params.rowSpec = GridLayout.spec(row, 1f);
                params.columnSpec = GridLayout.spec(col, 1f);
                cell.setLayoutParams(params);

                // Задаём цвет клетки
                cell.setBackgroundColor((row + col) % 2 == 0 ? getResources().getColor(R.color.white) : getResources().getColor(R.color.green));

                // Если в клетке есть фигура, устанавливаем изображение

                //cell.setScaleType(ImageView.ScaleType.CENTER);
                //cell.setImageResource(krug);

                if (boardSetup[row][col] != null) {

                    //Drawable overlay = getResources().getDrawable(boardSetup[row][col].pic);
                    //Drawable background = getResources().getDrawable(circle);

                    //Drawable[] layers = new Drawable[]{background, overlay};
                    //LayerDrawable layerDrawable = new LayerDrawable(layers);


                    cell.setImageDrawable(getResources().getDrawable(boardSetup[row][col].pic));


                    cell.setAdjustViewBounds(true);
                    cell.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    cell.setPadding(8, 8, 8, 8);
                }
                cells[row][col] = cell;

                // Добавляем в GridLayout
                chessBoard.addView(cell);

                int finalRow = row;
                int finalCol = col;
                cell.setOnClickListener(v -> onCellClick(finalRow, finalCol, cell));
            }
        }
        if (!isWhite){
            chessBoard.setRotation(180f);
            for (ImageView[] i : cells) {
                for (ImageView j : i) {
                    j.setRotation(180f);
                }
            }
        }
    }

    private void onCellClick(int row, int col, ImageView cell) {
        System.out.println("row: "+row+" col: "+col);
        boolean hell=false;

        if (selectedPiece == null) {
            // Выбираем фигуру
            if (boardSetup[row][col] != null) {
                if(!isOnlineGame || (isOnlineGame&&boardSetup[row][col].isWhite==isWhite)) {
                    if (isWhiteTurn == boardSetup[row][col].isWhite) {
                        selectedPiece = cell;
                        selectedRow = row;
                        selectedCol = col;
                        cell.setBackgroundColor(getResources().getColor(R.color.yellow));// Подсветка выбранной фигуры
                        hell = true;

                        if (boardSetup[row][col] instanceof King) {
                            validMovesForDraw = getKingsValidMoves(boardSetup[row][col]);
                        } else {
                            validMovesForDraw = getValidMovesForCheck(boardSetup[row][col]);
                        }
                        for (int[] move : validMovesForDraw) {
                            if (boardSetup[move[0]][move[1]] != null) {
                                Drawable overlay = getResources().getDrawable(boardSetup[move[0]][move[1]].pic);
                                Drawable background = getResources().getDrawable(circle);

                                Drawable[] layers = new Drawable[]{background, overlay};
                                LayerDrawable layerDrawable = new LayerDrawable(layers);
                                cells[move[0]][move[1]].setImageDrawable(layerDrawable);
                            } else {
                                cells[move[0]][move[1]].setScaleType(ImageView.ScaleType.CENTER);
                                cells[move[0]][move[1]].setImageResource(krug);
                            }
                        }
                    }
                }
            }
        } else {
            // Проверяем ход
            if (boardSetup[selectedRow][selectedCol].isValidMove(row, col)) {
                System.out.println("YES");
                Piece eatenPiece = boardSetup[row][col];
                System.out.println("ORIG: "+boardSetup[selectedRow][selectedCol].x+" "+boardSetup[selectedRow][selectedCol].y);

                // Перемещаем фигуру
                boardSetup[selectedRow][selectedCol].setY(col);
                boardSetup[selectedRow][selectedCol].setX(row);

                boardSetup[row][col] = boardSetup[selectedRow][selectedCol];
                boardSetup[selectedRow][selectedCol] = null;

                if (isKingInCheck(isWhiteTurn,true)){
                    boardSetup[row][col].setY(selectedCol);
                    boardSetup[row][col].setX(selectedRow);
                    boardSetup[selectedRow][selectedCol]=boardSetup[row][col];
                    boardSetup[row][col]=eatenPiece;
                    System.out.println(WhiteKing.x+" "+ WhiteKing.y+":"+boardSetup[selectedRow][selectedCol].x+" "+boardSetup[selectedRow][selectedCol].y);
                }else{

                    if (boardSetup[row][col] instanceof Pawn) {
                        if ((boardSetup[row][col].isWhite && row == 0) || (!boardSetup[row][col].isWhite && row == 7)) {
                            promotePawn(row, col, boardSetup[row][col].isWhite);

                        }
                    }

                    cell.setImageDrawable(selectedPiece.getDrawable());
                    selectedPiece.setImageDrawable(null);


                    //chessMorph part
                    if(morphModeOn){
                        morphMode(row,col,selectedRow,selectedCol,cell);
                    }





                    if (isWhiteTurn) {
                        startBlackTimer();
                        whiteTimeLeft+=plusTime;
                        whiteTimer.cancel();
                    } else {
                        startWhiteTimer();
                        blackTimeLeft+=plusTime;
                        blackTimer.cancel();
                    }


                    if(isOnlineGame){
                        sendMove(gameId,selectedRow, selectedCol, row, col);
                    }

                    isWhiteTurn = !isWhiteTurn;


                    if(isKingInCheck(isWhiteTurn,true)){
                        if(isCheckmate(isWhiteTurn)){
                            System.out.println("CHECKMATE");
                            if(isWhiteTurn){
                                theEndgame("Black wins",false);
                            }else{
                                theEndgame("White wins",false);
                            }
                        }
                    }
                    /*for (int x = 0; x < 8; x++) {
                        canMove=false;
                        for (int y = 0; y < 8; y++) {
                            if (boardSetup[x][y] != null) {
                                System.out.println("AAAAA111111");
                                if(boardSetup[x][y].isWhite != isWhiteTurn){
                                    System.out.println("BBBBB22222");
                                    List<int[]> validMoves = getValidMoves(boardSetup[x][y]);
                                    System.out.println("CCCCC33333");
                                    if (validMoves != null && !validMoves.isEmpty()) {
                                        canMove = true;
                                        break;
                                    }
                                }
                            }
                        }
                        if (canMove) {
                            break;
                        }
                    }
                    if(!canMove){
                        theEndgame("Draw",false);
                    }
                    */


                    if (turnTheBoardOn&&!isOnlineGame) {
                        if (!isWhiteTurn) {
                            for (ImageView[] i : cells) {
                                for (ImageView j : i) {
                                    j.setRotation(180f);
                                }
                            }
                        } else {
                            for (ImageView[] i : cells) {
                                for (ImageView j : i) {
                                    j.setRotation(0f);
                                }
                            }
                        }
                    }



                    System.out.println(boardSetup[row][col].y+"  "+boardSetup[row][col].x);
                    System.out.println(row+"  "+col);
                }


            }else {
                System.out.println(boardSetup[selectedRow][selectedCol].y+"  "+boardSetup[selectedRow][selectedCol].isValidMove(row, col)+"  "+boardSetup[selectedRow][selectedCol].x);
                System.out.println(selectedRow+"  "+boardSetup[selectedRow][selectedCol].isValidMove(row, col)+"  "+selectedCol);
            }

            selectedPiece.setBackgroundColor((selectedRow + selectedCol) % 2 == 0 ? getResources().getColor(R.color.white) : getResources().getColor(R.color.green));
            selectedPiece = null;
            isWhiteCheck = isKingInCheck(true,true);
            isBlackCheck = isKingInCheck(false,true);


            for (int[] move : validMovesForDraw) {
                if (boardSetup[move[0]][move[1]] != null) {
                    cells[move[0]][move[1]].setImageDrawable(getResources().getDrawable(boardSetup[move[0]][move[1]].pic));
                    cells[move[0]][move[1]].setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                }else {
                cells[move[0]][move[1]].setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                cells[move[0]][move[1]].setImageDrawable(null);
                }
            }
            validMovesForDraw=null;

        }
        System.out.println("White: "+isWhiteCheck+"   Black: "+isBlackCheck);
        if(!hell){cell.setBackgroundColor((row + col) % 2 == 0 ? getResources().getColor(R.color.white) : getResources().getColor(R.color.green));}
        if(isKingInCheck(isWhiteTurn,false)){
            if (boardSetup[selectedRow][selectedCol] instanceof King) {
                if (selectedRow==row && selectedCol==col&&!hell){
                    isCheck=isKingInCheck(isWhiteTurn,true);
                }
            }
        }

    }

    public void backToMenu(View view) {
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
        finish();
    }





    public boolean isPathClear(int x1, int y1, int x2, int y2) {
        int dx = Integer.compare(x2, x1); // -1, 0 или 1
        int dy = Integer.compare(y2, y1); // -1, 0 или 1

        int x = x1 + dx;
        int y = y1 + dy;

        while (x != x2 || y != y2) {
            if (boardSetup[x][y] != null) {
                return false; // Нашли препятствие
            }
            x += dx;
            y += dy;
        }
        return true;
    }
    public boolean isOccupied(int x, int y) {
        return boardSetup[x][y] != null;
    }
    public Piece getPiece(int x, int y) {
        return boardSetup[x][y];
    }

    public List<int[]> getValidMoves(Piece piece) {

        List<int[]> validMoves = new ArrayList<>();
        for (int newRow = 0; newRow < 8; newRow++) {
            for (int newCol = 0; newCol < 8; newCol++) {
                if (piece.isValidMove(newRow, newCol)) {
                    validMoves.add(new int[]{newRow, newCol});
                }
            }
        }
        return validMoves;

    }
    public List<int[]> getKingsValidMoves(Piece piece) {

        List<int[]> validMoves = getValidMoves(piece);

        List<int[]> kingsValidMoves = new ArrayList<>();

        for (int[] move : validMoves) {
            Piece eatenPiece = boardSetup[move[0]][move[1]];

            boardSetup[selectedRow][selectedCol].setY(move[1]);
            boardSetup[selectedRow][selectedCol].setX(move[0]);

            boardSetup[move[0]][move[1]] = boardSetup[selectedRow][selectedCol];
            boardSetup[selectedRow][selectedCol] = null;

            if (isKingInCheck(isWhiteTurn, false)) {
                boardSetup[move[0]][move[1]].setY(selectedCol);
                boardSetup[move[0]][move[1]].setX(selectedRow);
                boardSetup[selectedRow][selectedCol] = boardSetup[move[0]][move[1]];
                boardSetup[move[0]][move[1]] = eatenPiece;
            }else{
                boardSetup[move[0]][move[1]].setY(selectedCol);
                boardSetup[move[0]][move[1]].setX(selectedRow);
                boardSetup[selectedRow][selectedCol] = boardSetup[move[0]][move[1]];
                boardSetup[move[0]][move[1]] = eatenPiece;
                kingsValidMoves.add(new int[]{move[0], move[1]});
            }
        }
        return kingsValidMoves;
    }
    public List<int[]> getValidMovesForCheck(Piece piece) {

        List<int[]> validMoves = getValidMoves(piece);

        List<int[]> ValidMovesForCheck = new ArrayList<>();

        for (int[] move : validMoves) {
            Piece eatenPiece = boardSetup[move[0]][move[1]];

            boardSetup[selectedRow][selectedCol].setY(move[1]);
            boardSetup[selectedRow][selectedCol].setX(move[0]);

            boardSetup[move[0]][move[1]] = boardSetup[selectedRow][selectedCol];
            boardSetup[selectedRow][selectedCol] = null;

            if (isKingInCheck(isWhiteTurn, false)) {
                boardSetup[move[0]][move[1]].setY(selectedCol);
                boardSetup[move[0]][move[1]].setX(selectedRow);
                boardSetup[selectedRow][selectedCol] = boardSetup[move[0]][move[1]];
                boardSetup[move[0]][move[1]] = eatenPiece;
            }else{
                boardSetup[move[0]][move[1]].setY(selectedCol);
                boardSetup[move[0]][move[1]].setX(selectedRow);
                boardSetup[selectedRow][selectedCol] = boardSetup[move[0]][move[1]];
                boardSetup[move[0]][move[1]] = eatenPiece;
                ValidMovesForCheck.add(new int[]{move[0], move[1]});
            }
        }
        return ValidMovesForCheck;
    }

    public boolean isKingInCheck(boolean isWhite,boolean draw) {
        int[] kingPos=new int[2];
        boolean isCheck=false;
        if (isWhite){
            kingPos[0]=WhiteKing.x;
            kingPos[1]=WhiteKing.y;
        }
        else {
            kingPos[0]=BlackKing.x;
            kingPos[1]=BlackKing.y;
        }
        for (Piece[] row : boardSetup) {
            for (Piece piece : row) {
                if (piece != null && piece.isWhite != isWhite) {
                    if (piece.isValidMove(kingPos[0], kingPos[1])) {
                        isCheck=true;
                    }
                }
            }
        }
        if(draw) {
            if (isCheck) {
                cells[kingPos[0]][kingPos[1]].setBackgroundColor(getResources().getColor(R.color.red));
            } else {
                cells[kingPos[0]][kingPos[1]].setBackgroundColor((kingPos[0] + kingPos[1]) % 2 == 0 ? getResources().getColor(R.color.white) : getResources().getColor(R.color.green));
            }
        }
        return isCheck;
    }

    public boolean isCheckmate(boolean isWhite) {
        int tryX;
        int tryY;
        int oldX;
        int oldY;
        Piece eatenPiece;
        boolean isCheckmate=true;
        for (Piece[] row : boardSetup) {
            for (Piece piece : row) {
                if (piece != null && piece.isWhite == isWhite) {
                    List<int[]> validMoves=getValidMoves(piece);
                    for (int[] move : validMoves) {
                        tryX=move[0];
                        tryY=move[1];
                        oldX=piece.x;
                        oldY=piece.y;

                        eatenPiece = boardSetup[tryX][tryY];

                        boardSetup[oldX][oldY].setY(tryY);
                        boardSetup[oldX][oldY].setX(tryX);

                        boardSetup[tryX][tryY] = boardSetup[oldX][oldY];
                        boardSetup[oldX][oldY] = null;

                        if (!isKingInCheck(isWhite,false)){
                            isCheckmate=false;
                        }

                        boardSetup[tryX][tryY].setY(oldY);
                        boardSetup[tryX][tryY].setX(oldX);
                        boardSetup[oldX][oldY]=boardSetup[tryX][tryY];
                        boardSetup[tryX][tryY]=eatenPiece;

                        if (!isCheckmate) break;

                    }
                    if (!isCheckmate) break;
                }
                if (!isCheckmate) break;
            }
            if (!isCheckmate) break;
        }
        return isCheckmate;
    }








    abstract class Piece {
        protected int x, y, pic;
        protected boolean isWhite, isMorphable;

        public void setX(int x) {
            this.x = x;
        }
        public void setY(int y) {
            this.y = y;
        }

        public Piece(int x, int y, boolean isWhite) {
            this.y = y;
            this.x = x;
            this.isWhite = isWhite;
        }
        public abstract boolean isValidMove(int newX, int newY);
    }
    class Rook extends Piece {
        public Rook(int x, int y, boolean isWhite) {
            super(x, y, isWhite);
            if (isWhite) {
                pic=R.drawable.rook_white;
            }else pic=R.drawable.rook_black;
            isMorphable=true;
        }

        @Override
        public boolean isValidMove(int newX, int newY) {
            if (x != newX && y != newY) return false;
            if (x==newX && y==newY) return false;// Ладья двигается только по прямым
            if (isPathClear(x, y, newX, newY)){
                Piece targetPiece = getPiece(newX, newY);
                return targetPiece == null || targetPiece.isWhite != this.isWhite;
            }else return false; // Проверяем путь
        }
    }
    class Bishop extends Piece {
        public Bishop(int x, int y, boolean isWhite) {
            super(x, y, isWhite);
            if (isWhite) {
                pic=R.drawable.bishop_white;
            }else pic=R.drawable.bishop_black;
            isMorphable=true;
        }

        @Override
        public boolean isValidMove(int newX, int newY) {
            if (x==newX && y==newY) return false;
            if (Math.abs(newX - x) != Math.abs(newY - y)) return false; // Только по диагонали

            if (isPathClear(x, y, newX, newY)){
                Piece targetPiece = getPiece(newX, newY);
                return targetPiece == null || targetPiece.isWhite != this.isWhite;
            }else return false;
        }
    }
    class Knight extends Piece {
        public Knight(int x, int y, boolean isWhite) {
            super(x, y, isWhite);
            if (isWhite) {
                pic=R.drawable.knight_white;
            }else pic=R.drawable.knight_black;
            isMorphable=true;
        }

        @Override
        public boolean isValidMove(int newX, int newY) {
            if (x==newX && y==newY) return false;
            int dx = Math.abs(newX - x);
            int dy = Math.abs(newY - y);

            // Проверяем, что ход соответствует "букве Г"
            if (!((dx == 2 && dy == 1) || (dx == 1 && dy == 2))) {
                return false;
            }

            // Проверяем, есть ли на новой клетке фигура того же цвета
            Piece targetPiece = getPiece(newX, newY);
            return targetPiece == null || targetPiece.isWhite != this.isWhite;
        }
    }
    class Queen extends Piece {
        public Queen(int x, int y, boolean isWhite) {
            super(x, y, isWhite);
            if (isWhite) {
                pic=R.drawable.queen_white;
            }else pic=R.drawable.queen_black;
            isMorphable=true;
        }

        @Override
        public boolean isValidMove(int newX, int newY) {
            if (x==newX && y==newY) return false;
            if (x != newX && y != newY && Math.abs(newX - x) != Math.abs(newY - y)) return false;


            if (isPathClear(x, y, newX, newY)){
                Piece targetPiece = getPiece(newX, newY);
                return targetPiece == null || targetPiece.isWhite != this.isWhite;
            }else return false;
        }
    }
    class Pawn extends Piece {
        public Pawn(int x, int y, boolean isWhite) {
            super(x, y, isWhite);
            if (isWhite) {
                pic=R.drawable.pawn_white;
            }else pic=R.drawable.pawn_black;
            isMorphable=false;
        }

        @Override
        public boolean isValidMove(int newX, int newY) {
            if (x==newX && y==newY) return false;
            int direction = isWhite ? -1 : 1;
            //System.out.println(x+" "+y+" : "+newX+" "+newY);

            if (x==1 || x==6){
                if (newY == y && newX == x + direction*2) {
                    return (isPathClear(x,y, newX, newY) && !isOccupied(newX,newY)); // Вперед только если клетка свободна
                }
            }

            if (newY == y && newX == x + direction) {
                return !isOccupied(newX, newY); // Вперед только если клетка свободна
            }

            // Взятие по диагонали
            if (Math.abs(newY - y) == 1 && newX == x + direction) {
                Piece targetPiece = getPiece(newX, newY);
                return targetPiece != null && targetPiece.isWhite != this.isWhite; // Должна быть вражеская фигура
            }

            return false;
        }
    }
    class King extends Piece {
        public King(int x, int y, boolean isWhite) {
            super(x, y, isWhite);
            if (isWhite) {
                pic=R.drawable.king_white;
            }else pic=R.drawable.king_black;
            isMorphable=false;
        }

        @Override
        public boolean isValidMove(int newX, int newY) {
            if (x==newX && y==newY) return false;
            // Король может ходить максимум на 1 клетку в любом направлении
            if (Math.abs(newX - x) > 1 || Math.abs(newY - y) > 1 || (newX == x && newY == y)) {
                return false;
            }

            // Проверяем, свободна ли клетка или занята фигурой противника
            Piece targetPiece = getPiece(newX, newY);
            return targetPiece == null || targetPiece.isWhite != this.isWhite;
        }
    }



    private void move(int selectedRow,int selectedCol, int row, int col, ImageView cell,ImageView selectedPiece){
        if (boardSetup[selectedRow][selectedCol]==null){
            return;
        }
        isWhiteTurn = !isWhiteTurn;
        Piece eatenPiece = boardSetup[row][col];

        // Перемещаем фигуру
        boardSetup[selectedRow][selectedCol].setY(col);
        boardSetup[selectedRow][selectedCol].setX(row);

        boardSetup[row][col] = boardSetup[selectedRow][selectedCol];
        boardSetup[selectedRow][selectedCol] = null;

        if (isKingInCheck(!isWhiteTurn,true)){
            boardSetup[row][col].setY(selectedCol);
            boardSetup[row][col].setX(selectedRow);
            boardSetup[selectedRow][selectedCol]=boardSetup[row][col];
            boardSetup[row][col]=eatenPiece;
            System.out.println(WhiteKing.x+" "+ WhiteKing.y+":"+boardSetup[selectedRow][selectedCol].x+" "+boardSetup[selectedRow][selectedCol].y);
        }else{
            if (boardSetup[row][col] instanceof Pawn) {
                if ((boardSetup[row][col].isWhite && row == 0) || (!boardSetup[row][col].isWhite && row == 7)) {
                    promotePawn(row, col, boardSetup[row][col].isWhite);

                }
            }

            cell.setImageDrawable(selectedPiece.getDrawable());
            selectedPiece.setImageDrawable(null);


            //chessMorph part
            if(morphModeOn){
                morphMode(row,col,selectedRow,selectedCol,cell);
            }





            if (!isWhiteTurn) {
                startBlackTimer();
                whiteTimeLeft+=plusTime;
                whiteTimer.cancel();
            } else {
                startWhiteTimer();
                blackTimeLeft+=plusTime;
                blackTimer.cancel();
            }



            if(isKingInCheck(isWhiteTurn,true)){
                if(isCheckmate(isWhiteTurn)){
                    System.out.println("CHECKMATE");
                    if(isWhiteTurn){
                        theEndgame("Black wins",false);
                    }else{
                        theEndgame("White wins",false);
                    }
                }
            }
        }
    }

    private void morphMode(int row,int col,int selectedRow,int selectedCol, ImageView cell){
        if(boardSetup[row][col].isMorphable){
            if (boardSetup[row][col] instanceof Rook) {
                morphBoard[selectedRow][selectedCol]=new Rook(selectedRow, selectedCol, boardSetup[row][col].isWhite);
            }else if(boardSetup[row][col] instanceof Knight){
                morphBoard[selectedRow][selectedCol]=new Knight(selectedRow, selectedCol, boardSetup[row][col].isWhite);
            }else if(boardSetup[row][col] instanceof Bishop){
                morphBoard[selectedRow][selectedCol]=new Bishop(selectedRow, selectedCol, boardSetup[row][col].isWhite);
            }else if(boardSetup[row][col] instanceof Queen){
                morphBoard[selectedRow][selectedCol]=new Queen(selectedRow, selectedCol, boardSetup[row][col].isWhite);
            }

            if(morphBoard[row][col]!=null){
                if (morphBoard[row][col].isWhite==boardSetup[row][col].isWhite){
                    boardSetup[row][col]=morphBoard[row][col];
                    morphBoard[row][col]=null;
                    cell.setImageResource(boardSetup[row][col].pic);
                }else{
                    morphBoard[row][col]=null;
                }
            }
        }
    }







    private void startWhiteTimer() {
        if (whiteTimer != null) whiteTimer.cancel();
        whiteTimer = new CountDownTimer(whiteTimeLeft, 1000) {
            public void onTick(long millisUntilFinished) {
                whiteTimeLeft = millisUntilFinished;
                updateTimerUI();
            }

            public void onFinish() {
                theEndgame("Black wins by time",true);
            }
        }.start();
    }
    private void startBlackTimer() {
        if (blackTimer != null) blackTimer.cancel();
        blackTimer = new CountDownTimer(blackTimeLeft, 1000) {
            public void onTick(long millisUntilFinished) {
                blackTimeLeft = millisUntilFinished;
                updateTimerUI();
            }

            public void onFinish() {
                theEndgame("White wins by time",true);
            }
        }.start();
    }
    private void updateTimerUI() {
        whiteTimerText.setText(formatTime(whiteTimeLeft));
        blackTimerText.setText(formatTime(blackTimeLeft));
    }
    private String formatTime(long millis) {
        int minutes = (int) (millis / 60000);
        int seconds = (int) (millis % 60000 / 1000);
        return String.format("%02d:%02d", minutes, seconds);
    }

    public void sendMove(String gameId, int x1, int y1, int x2, int y2) {
        DatabaseReference gameRef = FirebaseDatabase.getInstance().getReference("games").child(gameId).child("lastMove");

        Map<String, Object> moveData = new HashMap<>();
        moveData.put("x1", x1);
        moveData.put("y1", y1);
        moveData.put("x2", x2);
        moveData.put("y2", y2);

        gameRef.setValue(moveData)
                .addOnSuccessListener(aVoid -> Log.d("Chess", "Ход отправлен: " + x1 + "," + y1 + " → " + x2 + "," + y2))
                .addOnFailureListener(e -> Log.w("Firebase", "Ошибка отправки хода", e));
        getOpponentId(gameId, opponentId -> {
            DatabaseReference turnRef = FirebaseDatabase.getInstance().getReference("games").child(gameId).child("turn");
            turnRef.setValue(opponentId);
        });
    }
    public void listenForMoves() {


        gameRef.child("lastMove").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Integer x1 = snapshot.child("x1").getValue(Integer.class);
                    Integer y1 = snapshot.child("y1").getValue(Integer.class);
                    Integer x2 = snapshot.child("x2").getValue(Integer.class);
                    Integer y2 = snapshot.child("y2").getValue(Integer.class);

                    if (x1 != null && y1 != null && x2 != null && y2 != null) {
                        if (isWhite != isWhiteTurn) {

                            move(x1, y1, x2, y2, cells[x2][y2], cells[x1][y1]);

                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("Firebase", "Ошибка получения хода", error.toException());
            }
        });

    }

    public void getOpponentId(String gameId, OnOpponentIdReceived callback) {
        DatabaseReference playersRef = FirebaseDatabase.getInstance().getReference("games").child(gameId).child("players");

        playersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                for (DataSnapshot playerSnapshot : snapshot.getChildren()) {
                    String playerId = playerSnapshot.getValue(String.class); // Берем значение, а не ключ!

                    if (!playerId.equals(userId)) {
                        callback.onReceived(playerId); // Теперь будет реальный UID
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("Firebase", "Ошибка получения opponentId", error.toException());
            }
        });
    }


    // Интерфейс для передачи данных
    public interface OnOpponentIdReceived {
        void onReceived(String opponentId);
    }


    private void promotePawn(final int row, final int col, final boolean isWhite) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_pawn_promotion, null);
        builder.setView(dialogView);

        ImageButton queenButton = dialogView.findViewById(R.id.queenButton);
        ImageButton rookButton = dialogView.findViewById(R.id.rookButton);
        ImageButton bishopButton = dialogView.findViewById(R.id.bishopButton);
        ImageButton knightButton = dialogView.findViewById(R.id.knightButton);

        if(isWhite){
            queenButton.setImageResource(R.drawable.queen_white);
            rookButton.setImageResource(R.drawable.rook_white);
            bishopButton.setImageResource(R.drawable.bishop_white);
            knightButton.setImageResource(R.drawable.knight_white);
        }

        AlertDialog dialog = builder.create();
        dialog.show();

        if (!isWhite) {
            dialog.getWindow().getDecorView().post(() ->
                    dialog.getWindow().getDecorView().setRotation(180f)
            );
        }

        View.OnClickListener listener = v -> {
            Piece newPiece = null;
            if(isWhite) {
                if (v == queenButton) newPiece = new Queen(row, col, isWhite);
                else if (v == rookButton) newPiece = new Rook(row, col, isWhite);
                else if (v == bishopButton) newPiece = new Bishop(row, col, isWhite);
                else if (v == knightButton) newPiece = new Knight(row, col, isWhite);
            }else{
                if (v == knightButton) newPiece = new Queen(row, col, isWhite);
                else if (v == bishopButton) newPiece = new Rook(row, col, isWhite);
                else if (v == rookButton) newPiece = new Bishop(row, col, isWhite);
                else if (v == queenButton) newPiece = new Knight(row, col, isWhite);
            }
            if (newPiece != null) {
                boardSetup[row][col] = newPiece;
                cells[row][col].setImageDrawable(getResources().getDrawable(newPiece.pic));
            }
            dialog.dismiss();
        };

        queenButton.setOnClickListener(listener);
        rookButton.setOnClickListener(listener);
        bishopButton.setOnClickListener(listener);
        knightButton.setOnClickListener(listener);
    }








    public void theEndgame(String text, boolean withTime){
        if(!withTime){
            whiteTimer.cancel();
            blackTimer.cancel();
        }
        AlertDialog.Builder endGame = new AlertDialog.Builder(this);
        endGame.setTitle(text);

        endGame.setPositiveButton("Back to menu", (dialog, which) -> {
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finish();
        });
        endGame.setNegativeButton("Play again", (dialog, which) -> {
            startActivity(new Intent(getApplicationContext(),Board.class));
            finish();
        });

        AlertDialog dialog = endGame.create();
        dialog.show();
    }


}