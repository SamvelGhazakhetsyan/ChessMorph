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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Board extends AppCompatActivity {

    // Размер доски
    private static final int BOARD_SIZE = 8;
    private GridLayout chessBoard;
    private ImageView selectedPiece = null;
    private int selectedRow = -1, selectedCol = -1;
    private boolean isWhiteTurn = true;
    private Piece[][] boardSetup = new Piece[8][8];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

        chessBoard = findViewById(R.id.chessBoard);

        setupBoard();
    }

    private void setupBoard() {

        boardSetup[0][0] = new Rook(0, 0, false);
        boardSetup[0][1] = new Knight(0, 1, false);
        boardSetup[0][2] = new Bishop(0, 2, false);
        boardSetup[0][3] = new Queen(0, 3, false);
        boardSetup[0][4] = new King(0, 4, false);
        boardSetup[0][5] = new Bishop(0, 5, false);
        boardSetup[0][6] = new Knight(0, 6, false);
        boardSetup[0][7] = new Rook(0, 7, false);

        // Расставляем белые пешки
        for (int i = 0; i < 8; i++) {
            boardSetup[1][i] = new Pawn(1, i, false);
        }

        // Расставляем черные фигуры
        boardSetup[7][0] = new Rook(7, 0, true);
        boardSetup[7][1] = new Knight(7, 1, true);
        boardSetup[7][2] = new Bishop(7, 2, true);
        boardSetup[7][3] = new Queen(7, 3, true);
        boardSetup[7][4] = new King(7, 4, true);
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

                // Добавляем в GridLayout
                chessBoard.addView(cell);

                int finalRow = row;
                int finalCol = col;
                cell.setOnClickListener(v -> onCellClick(finalRow, finalCol, cell));
            }
        }
    }

    private void onCellClick(int row, int col, ImageView cell) {
        if (selectedPiece == null) {
            // Выбираем фигуру
            if (boardSetup[row][col] != null) {
                selectedPiece = cell;
                selectedRow = row;
                selectedCol = col;
                cell.setBackgroundColor(getResources().getColor(R.color.yellow)); // Подсветка выбранной фигуры
            }
        } else {
            // Проверяем ход
            if (boardSetup[selectedRow][selectedCol].isValidMove(row, col)) {
                System.out.println("YES");
                // Перемещаем фигуру
                cell.setImageDrawable(selectedPiece.getDrawable());
                selectedPiece.setImageDrawable(null);
                boardSetup[row][col] = boardSetup[selectedRow][selectedCol];
                boardSetup[selectedRow][selectedCol] = null;

                // Меняем ход
                isWhiteTurn = !isWhiteTurn;
            }else {
                System.out.println(boardSetup[selectedRow][selectedCol].y+"NO"+boardSetup[selectedRow][selectedCol].x);
                System.out.println(selectedRow+"NO"+selectedCol);
            }
            selectedPiece.setBackgroundColor((selectedRow + selectedCol) % 2 == 0 ? getResources().getColor(R.color.white) : getResources().getColor(R.color.green));
            selectedPiece = null;
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






    abstract class Piece {
        protected int x, y, pic;
        protected boolean isWhite;

        public Piece(int x, int y, boolean isWhite) {
            this.x = x;
            this.y = y;
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
        }

        @Override
        public boolean isValidMove(int newX, int newY) {
            if (x != newX && y != newY) return false; // Ладья двигается только по прямым
            return isPathClear(x, y, newX, newY); // Проверяем путь
        }
    }
    class Bishop extends Piece {
        public Bishop(int x, int y, boolean isWhite) {
            super(x, y, isWhite);
            if (isWhite) {
                pic=R.drawable.bishop_white;
            }else pic=R.drawable.bishop_black;
        }

        @Override
        public boolean isValidMove(int newX, int newY) {
            if (Math.abs(newX - x) != Math.abs(newY - y)) return false; // Только по диагонали

            return isPathClear(x, y, newX, newY);
        }
    }
    class Knight extends Piece {
        public Knight(int x, int y, boolean isWhite) {
            super(x, y, isWhite);
            if (isWhite) {
                pic=R.drawable.knight_white;
            }else pic=R.drawable.knight_black;
        }

        @Override
        public boolean isValidMove(int newX, int newY) {
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
        }

        @Override
        public boolean isValidMove(int newX, int newY) {
            if (x != newX && y != newY && Math.abs(newX - x) != Math.abs(newY - y)) return false;

            return isPathClear(x, y, newX, newY);
        }
    }
    class Pawn extends Piece {
        public Pawn(int x, int y, boolean isWhite) {
            super(x, y, isWhite);
            if (isWhite) {
                pic=R.drawable.pawn_white;
            }else pic=R.drawable.pawn_black;
        }

        @Override
        public boolean isValidMove(int newX, int newY) {
            int direction = isWhite ? 1 : -1;

            if (newX == x && newY == y + direction) {
                return !isOccupied(newX, newY); // Вперед только если клетка свободна
            }

            // Взятие по диагонали
            if (Math.abs(newX - x) == 1 && newY == y + direction) {
                return getPiece(newX, newY) != null; // Должна быть вражеская фигура
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
        }

        @Override
        public boolean isValidMove(int newX, int newY) {
            // Король может ходить максимум на 1 клетку в любом направлении
            if (Math.abs(newX - x) > 1 || Math.abs(newY - y) > 1 || (newX == x && newY == y)) {
                return false;
            }

            // Проверяем, свободна ли клетка или занята фигурой противника
            Piece targetPiece = getPiece(newX, newY);
            return targetPiece == null || targetPiece.isWhite != this.isWhite;
        }
    }









}