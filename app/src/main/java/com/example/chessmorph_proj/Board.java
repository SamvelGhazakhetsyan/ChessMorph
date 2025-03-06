package com.example.chessmorph_proj;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class Board extends AppCompatActivity {

    // Размер доски
    private static final int BOARD_SIZE = 8;
    private GridLayout chessBoard;
    private ImageView selectedPiece = null;
    private int selectedRow = -1, selectedCol = -1;
    private boolean isWhiteTurn = true, isBlackCheck = false, isWhiteCheck = false, isCheck = false, morphModeOn=true;
    private Piece BlackKing = new King(0, 4, false);
    private Piece WhiteKing = new King(7, 4, true);
    private Piece checker=null;
    private Piece[][] boardSetup = new Piece[8][8];
    private ImageView[][] cells = new ImageView[8][8];
    private Piece[][] morphBoard = new Piece[8][8];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //ориентация вертикальная
        setContentView(R.layout.activity_board);

        chessBoard = findViewById(R.id.chessBoard);

        setupBoard();
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
                if (boardSetup[row][col] != null) {
                    cell.setImageResource(boardSetup[row][col].pic);
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
    }

    private void onCellClick(int row, int col, ImageView cell) {
        System.out.println("row: "+row+" col: "+col);
        boolean hell=false;

        if (selectedPiece == null) {
            // Выбираем фигуру
            if (boardSetup[row][col] != null) {
                if(isWhiteTurn==boardSetup[row][col].isWhite){
                    selectedPiece = cell;
                    selectedRow = row;
                    selectedCol = col;
                    cell.setBackgroundColor(getResources().getColor(R.color.yellow));// Подсветка выбранной фигуры
                    hell=true;
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
                    cell.setImageDrawable(selectedPiece.getDrawable());
                    selectedPiece.setImageDrawable(null);

                    //chessMorph pice
                    if(morphModeOn){
                        System.out.println(boardSetup[row][col].isMorphable);
                        if(boardSetup[row][col].isMorphable){
                            System.out.println("AYOOOOOOO");
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





                    isWhiteTurn = !isWhiteTurn;

                    if(isKingInCheck(isWhiteTurn,true)){
                        if(isCheckmate(isWhiteTurn)){
                            System.out.println("CHECKMATE");
                            if(isWhiteTurn){
                                theEndgame("White");
                            }else{
                                theEndgame("Black");
                            }
                        }
                    }

                    if(!isWhiteTurn) {
                        for (ImageView[] i : cells) {
                            for (ImageView j : i) {
                                j.setRotation(180f);
                            }
                        }
                    }else{
                        for (ImageView[] i : cells) {
                            for (ImageView j : i) {
                                j.setRotation(0f);
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
                    return !isOccupied(newX, newY); // Вперед только если клетка свободна
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







    public void theEndgame(String color){
        AlertDialog.Builder endGame = new AlertDialog.Builder(this);
        endGame.setTitle(color+" wins");

        endGame.setPositiveButton("Back to menu", (dialog, which) -> {
            backToMenu(null);
        });
        endGame.setNegativeButton("Play again", (dialog, which) -> {
            startActivity(new Intent(getApplicationContext(),Board.class));
            finish();
        });

        AlertDialog dialog = endGame.create();
        dialog.show();
    }


}