package com.example.chessmorph_proj;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
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
    private boolean isWhiteTurn = true, isBlackCheck = false, isWhiteCheck = false, hell=false, promoted=false, isMate = false, isgameend=false, isCheck=false, morphModeOn=true, turnTheBoardOn=true, canMove = false, isOnlineGame, isWhite=true;
    private Piece BlackKing = new King(0, 4, false);
    private Piece WhiteKing = new King(7, 4, true);
    private Piece checker=null;
    private List<int[]> validMovesForDraw;
    private Piece[][] boardSetup = new Piece[8][8];
    private ImageView[][] cells = new ImageView[8][8];
    private Piece[][] morphBoard = new Piece[8][8];

    private TextView whiteTimerText, blackTimerText;
    private CountDownTimer whiteTimer, blackTimer;
    private long whiteTimeLeft = 300000, blackTimeLeft = 300000, plusTime=0, lastBackPressedTime = 0; // 5
    private static final int BACK_PRESS_DELAY = 1500;
    private String gameId, userId, onlineTurn, opponentId, opponentNickname;
    DatabaseReference gameRef;
    FirebaseAuth fAuth;
    SharedPreferences prefs;
    ImageButton blackImage;
    ImageButton whiteImage;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //ориентация вертикальная
        setContentView(R.layout.activity_board);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);


            window.setStatusBarColor(Color.parseColor("#55000000"));


            window.setNavigationBarColor(Color.TRANSPARENT);


            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
        }
        blackImage = findViewById(R.id.blackImage);
        whiteImage = findViewById(R.id.whiteImage);


        fAuth = FirebaseAuth.getInstance();
        prefs = getSharedPreferences(fAuth.getCurrentUser().getUid(), MODE_PRIVATE);

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

        whiteTimerText = findViewById(R.id.whiteTimer);
        blackTimerText = findViewById(R.id.blackTimer);
        updateTimerUI();
        if(isOnlineGame){

            gameRef = FirebaseDatabase.getInstance().getReference("games").child(gameId);
            DatabaseReference playersRef = gameRef.child("players");

            listenForMoves();

            if (isWhite) {
                startWhiteTimer();
            } else {
                startBlackTimer();
            }
            String imageUri = prefs.getString("image", "");

            if(imageUri != "") {
                ImageView avatarImageView = findViewById(R.id.whiteImage);
                Glide.with(Board.this)
                        .load(imageUri)
                        .placeholder(R.drawable.profile_images)
                        .into(avatarImageView);
            }

            //opponents information
            playersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    opponentId = snapshot.child(isWhite ? "blackPlayer" : "whitePlayer").getValue(String.class);


                    DatabaseReference opponentRef = FirebaseDatabase.getInstance().getReference("users").child(opponentId);
                    opponentRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String opponentNickname = snapshot.child("nickname").getValue(String.class);
                            String opponentAvatar = snapshot.child("avatarUrl").getValue(String.class);
                            TextView nickText = findViewById(R.id.opponentNick);
                            nickText.setText(opponentNickname);
                            if(opponentAvatar!=null) {
                                ImageView avatarImageView = findViewById(R.id.blackImage);
                                Glide.with(Board.this)
                                        .load(opponentAvatar)
                                        .placeholder(R.drawable.profile_images)
                                        .into(avatarImageView);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getApplicationContext(), "Internet Error", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getApplicationContext(), "Internet Error", Toast.LENGTH_SHORT).show();
                }
            });

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(userId);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Integer games = snapshot.child("games").getValue(Integer.class);
                    if (games != null) {
                        int gamesCount = games + 1;
                        ref.child("games").setValue(gamesCount);
                        prefs.edit().putInt("games",gamesCount).apply();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getApplicationContext(), "Internet Error", Toast.LENGTH_SHORT).show();
                }
            });

            TextView myNickText = findViewById(R.id.myNick);
            myNickText.setText(prefs.getString("nickname", ""));
        }else{
            startWhiteTimer();
            blackImage.setVisibility(View.GONE);
            whiteImage.setVisibility(View.GONE);
        }
        if(morphModeOn){
            TextView textView = findViewById(R.id.textView2);
            textView.setText("Morph mode");
        }else{
            TextView textView = findViewById(R.id.textView2);
            textView.setText("Classic");
        }

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
                cell.setBackgroundColor((row + col) % 2 == 0 ? getResources().getColor(R.color.bej) : getResources().getColor(R.color.brown));

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
                cell.setAdjustViewBounds(true);
                cell.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                cell.setPadding(8, 8, 8, 8);
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
        if(isgameend){
            return;
        }
        System.out.println("row: "+row+" col: "+col);
        boolean moved=false;

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
                boolean rok=false;
                System.out.println("YES");
                Piece eatenPiece = boardSetup[row][col];
                System.out.println("ORIG: "+boardSetup[selectedRow][selectedCol].x+" "+boardSetup[selectedRow][selectedCol].y);

                // Перемещаем фигуру
                if (boardSetup[selectedRow][selectedCol] instanceof King && Math.abs(col - selectedCol) == 2) {
                    // Рокировка
                    if (col == 6) {
                        boardSetup[row][5] = boardSetup[row][7];
                        boardSetup[row][7] = null;
                        boardSetup[row][5].x = row;
                        boardSetup[row][5].y = 5;
                        boardSetup[row][5].hasMoved=true;
                        cells[row][5].setImageDrawable(getResources().getDrawable(boardSetup[row][3].pic));
                        cells[row][7].setImageDrawable(null);
                        rok=true;
                    } else if (col == 2) {
                        boardSetup[row][3] = boardSetup[row][0];
                        boardSetup[row][0] = null;
                        boardSetup[row][3].x = row;
                        boardSetup[row][3].y = 3;
                        boardSetup[row][3].hasMoved=true;
                        cells[row][3].setImageDrawable(getResources().getDrawable(boardSetup[row][3].pic));
                        cells[row][0].setImageDrawable(null);
                        rok=true;
                    }
                }
                boardSetup[selectedRow][selectedCol].setY(col);
                boardSetup[selectedRow][selectedCol].setX(row);

                boardSetup[row][col] = boardSetup[selectedRow][selectedCol];
                boardSetup[selectedRow][selectedCol] = null;

                //check if king is in check
                if (isKingInCheck(isWhiteTurn,false)){
                    boardSetup[row][col].setY(selectedCol);
                    boardSetup[row][col].setX(selectedRow);
                    boardSetup[selectedRow][selectedCol]=boardSetup[row][col];
                    boardSetup[row][col]=eatenPiece;
                    if(hell){
                        cells[selectedRow][selectedCol].setBackgroundColor((selectedRow + selectedCol) % 2 == 0 ? getResources().getColor(R.color.bej) : getResources().getColor(R.color.brown));
                    }
                    System.out.println(WhiteKing.x+" "+ WhiteKing.y+":"+boardSetup[selectedRow][selectedCol].x+" "+boardSetup[selectedRow][selectedCol].y);
                }else {
                    hell=false;
                    if (boardSetup[row][col] instanceof Pawn) {
                        if ((boardSetup[row][col].isWhite && row == 0) || (!boardSetup[row][col].isWhite && row == 7)) {
                            promotePawn(row, col, boardSetup[row][col].isWhite);
                        }
                    }

                    cell.setImageDrawable(selectedPiece.getDrawable());
                    selectedPiece.setImageDrawable(null);

                    if (boardSetup[row][col] instanceof King || boardSetup[row][col] instanceof Rook) {
                        boardSetup[row][col].hasMoved = true;
                    }

                    //chessMorph part
                    if (morphModeOn) {
                        morphMode(row, col, selectedRow, selectedCol, cell);
                    }


                    if (isOnlineGame) {
                        sendMove(gameId, selectedRow, selectedCol, row, col);


                        if (isWhite) {
                            if (isWhiteTurn) {
                                startBlackTimer();
                                whiteTimeLeft += plusTime;
                                whiteTimer.cancel();
                            } else {
                                startWhiteTimer();
                                blackTimeLeft += plusTime;
                                blackTimer.cancel();
                            }
                        } else {
                            if (isWhiteTurn) {
                                startWhiteTimer();
                                blackTimeLeft += plusTime;
                                blackTimer.cancel();
                            } else {
                                startBlackTimer();
                                whiteTimeLeft += plusTime;
                                whiteTimer.cancel();
                            }
                        }
                    } else {
                        if (isWhiteTurn) {
                            startBlackTimer();
                            whiteTimeLeft += plusTime;
                            whiteTimer.cancel();
                        } else {
                            startWhiteTimer();
                            blackTimeLeft += plusTime;
                            blackTimer.cancel();
                        }
                    }
                    System.out.println("ALOXAMORA");

                    isWhiteTurn = !isWhiteTurn;


                    if (isKingInCheck(isWhiteTurn, true)) {
                        if (isCheckmate(isWhiteTurn)) {
                            isMate = true;
                            if (isWhiteTurn) {
                                theEndgame("Black wins", false, "black");
                            } else {
                                theEndgame("White wins", false, "white");
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


                    if (turnTheBoardOn && !isOnlineGame) {
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


                    System.out.println(boardSetup[row][col].y + "  " + boardSetup[row][col].x);
                    System.out.println(row + "  " + col);
                    moved=true;
                    for (int i = 0; i < BOARD_SIZE; i++) {
                        for (int j = 0; j < BOARD_SIZE; j++) {
                            cells[i][j].setBackgroundColor((i + j) % 2 == 0 ? getResources().getColor(R.color.bej) : getResources().getColor(R.color.brown));
                        }
                    }
                    if (!rok){
                        cells[selectedRow][selectedCol].setBackgroundColor((selectedRow + selectedCol) % 2 == 0 ? getResources().getColor(R.color.yellow) : getResources().getColor(R.color.darkYellow));
                        cells[row][col].setBackgroundColor((row + col) % 2 == 0 ? getResources().getColor(R.color.yellow) : getResources().getColor(R.color.darkYellow));
                    }
                }

            }else {
                if(hell){
                    cells[selectedRow][selectedCol].setBackgroundColor((selectedRow + selectedCol) % 2 == 0 ? getResources().getColor(R.color.bej) : getResources().getColor(R.color.brown));
                }
                System.out.println(boardSetup[selectedRow][selectedCol].y+"  "+boardSetup[selectedRow][selectedCol].isValidMove(row, col)+"  "+boardSetup[selectedRow][selectedCol].x);
                System.out.println(selectedRow+"  "+boardSetup[selectedRow][selectedCol].isValidMove(row, col)+"  "+selectedCol);
            }

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

        if(isKingInCheck(isWhiteTurn,false)){
            if (boardSetup[selectedRow][selectedCol] instanceof King) {
                if (selectedRow==row && selectedCol==col&&!hell){
                    isCheck=isKingInCheck(isWhiteTurn,true);
                }
            }
        }

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
        int fromX = piece.x;
        int fromY = piece.y;

        List<int[]> validMoves = getValidMoves(piece);

        List<int[]> kingsValidMoves = new ArrayList<>();

        for (int[] move : validMoves) {
            Piece eatenPiece = boardSetup[move[0]][move[1]];

            boardSetup[fromX][fromY].setY(move[1]);
            boardSetup[fromX][fromY].setX(move[0]);

            boardSetup[move[0]][move[1]] = boardSetup[fromX][fromY];
            boardSetup[fromX][fromY] = null;

            if (isKingInCheck(isWhiteTurn, false)) {
                boardSetup[move[0]][move[1]].setY(fromY);
                boardSetup[move[0]][move[1]].setX(fromX);
                boardSetup[fromX][fromY] = boardSetup[move[0]][move[1]];
                boardSetup[move[0]][move[1]] = eatenPiece;
            }else{
                boardSetup[move[0]][move[1]].setY(fromY);
                boardSetup[move[0]][move[1]].setX(fromX);
                boardSetup[fromX][fromY] = boardSetup[move[0]][move[1]];
                boardSetup[move[0]][move[1]] = eatenPiece;
                kingsValidMoves.add(new int[]{move[0], move[1]});
            }
        }
        return kingsValidMoves;
    }
    public List<int[]> getValidMovesForCheck(Piece piece) {
        int fromX = piece.x;
        int fromY = piece.y;

        List<int[]> validMoves = getValidMoves(piece);

        List<int[]> ValidMovesForCheck = new ArrayList<>();

        for (int[] move : validMoves) {
            Piece eatenPiece = boardSetup[move[0]][move[1]];

            boardSetup[fromX][fromY].setY(move[1]);
            boardSetup[fromX][fromY].setX(move[0]);

            boardSetup[move[0]][move[1]] = boardSetup[fromX][fromY];
            boardSetup[fromX][fromY] = null;

            if (isKingInCheck(isWhiteTurn, false)) {
                boardSetup[move[0]][move[1]].setY(fromY);
                boardSetup[move[0]][move[1]].setX(fromX);
                boardSetup[fromX][fromY] = boardSetup[move[0]][move[1]];
                boardSetup[move[0]][move[1]] = eatenPiece;
            }else{
                boardSetup[move[0]][move[1]].setY(fromY);
                boardSetup[move[0]][move[1]].setX(fromX);
                boardSetup[fromX][fromY] = boardSetup[move[0]][move[1]];
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
                cells[kingPos[0]][kingPos[1]].setBackgroundColor((kingPos[0] + kingPos[1]) % 2 == 0 ? getResources().getColor(R.color.bej) : getResources().getColor(R.color.brown));
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

    public boolean isCellUnderAttack(int row, int col, boolean byWhite) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece p = boardSetup[i][j];
                if (p != null && p.isWhite == byWhite) {
                    if (p instanceof King) continue;
                    List<int[]> moves = getValidMoves(p);
                    for (int[] move : moves) {
                        if (move[0] == row && move[1] == col) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }






    abstract class Piece {
        protected int x, y, pic;
        protected boolean isWhite, isMorphable, hasMoved=false;

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
            hasMoved=false;
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
            hasMoved=false;
        }

        @Override
        public boolean isValidMove(int newX, int newY) {
            if (x == newX && y == newY) return false;

            if (Math.abs(newX - x) <= 1 && Math.abs(newY - y) <= 1) {
                Piece targetPiece = getPiece(newX, newY);
                return targetPiece == null || targetPiece.isWhite != this.isWhite;
            }

            if (!hasMoved && x == newX && (y == 4)) {
                // Короткая рокировка
                if (newY == 6) {
                    Piece rook = getPiece(x, 7);
                    if (rook instanceof Rook && !((Rook) rook).hasMoved) {
                        if (getPiece(x, 5) == null && getPiece(x, 6) == null) {
                            if (!isCellUnderAttack(x, 5, !isWhite) && !isCellUnderAttack(x, 6, !isWhite) && !isCellUnderAttack(x, 4, !isWhite)) {
                                return true;
                            }
                        }
                    }
                }

                // Длинная рокировка
                if (newY == 2) {
                    Piece rook = getPiece(x, 0);
                    if (rook instanceof Rook && !((Rook) rook).hasMoved) {
                        if (getPiece(x, 1) == null && getPiece(x, 2) == null && getPiece(x, 3) == null) {
                            if (!isCellUnderAttack(x, 3, !isWhite) && !isCellUnderAttack(x, 2, !isWhite) && !isCellUnderAttack(x, 4, !isWhite)) {
                                return true;
                            }
                        }
                    }
                }
            }

            return false;
        }
    }



    private void move(int selectedRow,int selectedCol, int row, int col, ImageView cell,ImageView selectedPiece,int promoteTo){
        boolean rok=false;
        if (boardSetup[selectedRow][selectedCol]==null){
            return;
        }
        isWhiteTurn = !isWhiteTurn;
        Piece eatenPiece = boardSetup[row][col];

        if (boardSetup[selectedRow][selectedCol] instanceof King && Math.abs(col - selectedCol) == 2) {
            // Рокировка
            if (col == 6) {
                boardSetup[row][5] = boardSetup[row][7];
                boardSetup[row][7] = null;
                boardSetup[row][5].x = row;
                boardSetup[row][5].y = 5;
                boardSetup[row][5].hasMoved = true;
                cells[row][5].setImageDrawable(getResources().getDrawable(boardSetup[row][5].pic));
                cells[row][7].setImageDrawable(null);
                rok = true;
            } else if (col == 2) {
                boardSetup[row][3] = boardSetup[row][0];
                boardSetup[row][0] = null;
                boardSetup[row][3].x = row;
                boardSetup[row][3].y = 3;
                boardSetup[row][3].hasMoved = true;
                cells[row][3].setImageDrawable(getResources().getDrawable(boardSetup[row][3].pic));
                cells[row][0].setImageDrawable(null);
                rok = true;
            }
        }

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





            if (isWhite){
                if (!isWhiteTurn) {
                    startBlackTimer();
                    whiteTimeLeft+=plusTime;
                    whiteTimer.cancel();
                } else {
                    startWhiteTimer();
                    blackTimeLeft+=plusTime;
                    blackTimer.cancel();
                }
            }else{
                if (!isWhiteTurn) {
                    startWhiteTimer();
                    blackTimeLeft+=plusTime;
                    blackTimer.cancel();
                } else {
                    startBlackTimer();
                    whiteTimeLeft+=plusTime;
                    whiteTimer.cancel();
                }
            }



            if(isKingInCheck(isWhiteTurn,true)){
                if(isCheckmate(isWhiteTurn)){
                    System.out.println("CHECKMATE");
                    if(isWhiteTurn){
                        theEndgame("Black wins",false, "black");
                    }else{
                        theEndgame("White wins",false, "white");
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
                if(isWhite){
                    theEndgame("Black wins by time",true, "black");
                }else{
                    theEndgame("White wins by time",true, "white");
                }
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
                if(isWhite){
                    theEndgame("White wins by time",true, "white");
                }else{
                    theEndgame("Black wins by time",true, "black");
                }
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
                    Integer isLeft = snapshot.child("isLeft").getValue(Integer.class);
                    if (isLeft != null && isLeft == 1) {
                        if (isWhite){
                            theEndgame("Opponent left the game",false, "white");
                        }else{
                            theEndgame("Opponent left the game",false, "black");
                        }
                        return;
                    }
                    Integer promoteTo = snapshot.child("promoteTo").getValue(Integer.class);
                    Integer x1 = snapshot.child("x1").getValue(Integer.class);
                    Integer y1 = snapshot.child("y1").getValue(Integer.class);
                    Integer x2 = snapshot.child("x2").getValue(Integer.class);
                    Integer y2 = snapshot.child("y2").getValue(Integer.class);

                    if (x1 != null && y1 != null && x2 != null && y2 != null) {
                        if (isWhite != isWhiteTurn) {

                            for (int row = 0; row < BOARD_SIZE; row++) {
                                for (int col = 0; col < BOARD_SIZE; col++) {
                                    cells[row][col].setBackgroundColor((row + col) % 2 == 0 ? getResources().getColor(R.color.bej) : getResources().getColor(R.color.brown));
                                }
                            }

                            move(x1, y1, x2, y2, cells[x2][y2], cells[x1][y1], promoteTo);

                            cells[x1][y1].setBackgroundColor((x1 + y1) % 2 == 0 ? getResources().getColor(R.color.yellow) : getResources().getColor(R.color.darkYellow));
                            cells[x2][y2].setBackgroundColor((x2 + y2) % 2 == 0 ? getResources().getColor(R.color.yellow) : getResources().getColor(R.color.darkYellow));
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
            int promoteCode = 0;

            if (isWhite) {
                if (v == queenButton) {
                    newPiece = new Queen(row, col, true);
                    promoteCode = 1;
                } else if (v == rookButton) {
                    newPiece = new Rook(row, col, true);
                    promoteCode = 2;
                } else if (v == bishopButton) {
                    newPiece = new Bishop(row, col, true);
                    promoteCode = 3;
                } else if (v == knightButton) {
                    newPiece = new Knight(row, col, true);
                    promoteCode = 4;
                }
            } else {
                if (v == knightButton) {
                    newPiece = new Queen(row, col, false);
                    promoteCode = 1;
                } else if (v == bishopButton) {
                    newPiece = new Rook(row, col, false);
                    promoteCode = 2;
                } else if (v == rookButton) {
                    newPiece = new Bishop(row, col, false);
                    promoteCode = 3;
                } else if (v == queenButton) {
                    newPiece = new Knight(row, col, false);
                    promoteCode = 4;
                }
            }
            if(isOnlineGame) {
                if (newPiece != null) {
                    boardSetup[row][col] = newPiece;
                    cells[row][col].setImageDrawable(getResources().getDrawable(newPiece.pic));

                    DatabaseReference lastMoveRef = FirebaseDatabase.getInstance()
                            .getReference("games")
                            .child(gameId)
                            .child("lastMove");

                    Map<String, Object> update = new HashMap<>();
                    update.put("promoteTo", promoteCode);
                    lastMoveRef.updateChildren(update);
                }
            }

            dialog.dismiss();
        };

        queenButton.setOnClickListener(listener);
        rookButton.setOnClickListener(listener);
        bishopButton.setOnClickListener(listener);
        knightButton.setOnClickListener(listener);
    }







    public void toMyProfile(View view){
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("isMyProfile", true);
        startActivity(intent);
    }

    public void toOpponentProfile(View view){
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("isMyProfile", false);
        intent.putExtra("opponentId", opponentId);
        startActivity(intent);
    }

    public void backToMenu(View view) {
        if (isgameend){
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finish();
        }else{
            AlertDialog.Builder suerToLeave = new AlertDialog.Builder(this);
            suerToLeave.setTitle("Are you sure?");

            suerToLeave.setPositiveButton("OK", (dialog, which) -> {
                if(isOnlineGame){
                    if (isWhite){
                        theEndgame("White left the game",false, "black");
                    }else{
                        theEndgame("Black left the game",false, "white");
                    }
                    DatabaseReference lastMoveRef = FirebaseDatabase.getInstance().getReference("games").child(gameId).child("lastMove");

                    Map<String, Object> data = new HashMap<>();
                    data.put("isLeft", 1);
                    lastMoveRef.updateChildren(data);
                }else{

                    if (whiteTimer != null) {
                        whiteTimer.cancel();
                    }
                    if (blackTimer != null) {
                        blackTimer.cancel();
                    }

                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                    finish();
                }
            });
            suerToLeave.setNegativeButton("CANCEL", (dialog, which) -> {
                return;
            });

            AlertDialog dialog = suerToLeave.create();
            dialog.show();
        }
    }
    public void theEndgame(String text, boolean withTime,String whoWon){
        isgameend=true;
        if(!withTime){
            if (whiteTimer != null) {
                whiteTimer.cancel();
            }
            if (blackTimer != null) {
                blackTimer.cancel();
            }
        }
        if(isOnlineGame){
            if ((whoWon.equals("white") && isWhite) ||(whoWon.equals("black") && !isWhite)){

                int wins = prefs.getInt("wins",0)+1;
                prefs.edit().putInt("wins",wins).apply();

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(userId);
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Integer wins = snapshot.child("wins").getValue(Integer.class);
                        if (wins != null) {
                            int winsCount = wins + 1;
                            ref.child("wins").setValue(winsCount);
                        }
                        if(morphModeOn){

                            Integer chessMorphRating = snapshot.child("chessRating").getValue(Integer.class);
                            if (chessMorphRating != null) {
                                int chessMorphRat = chessMorphRating + 5;
                                ref.child("chessMorphRating").setValue(chessMorphRat);
                                prefs.edit().putInt("chessMorphRating",chessMorphRat).apply();
                            }


                        }else{
                            Integer chessRating = snapshot.child("chessRating").getValue(Integer.class);
                            if (chessRating != null) {
                                int chessRat = chessRating + 5;
                                ref.child("chessRating").setValue(chessRat);
                                prefs.edit().putInt("chessRating",chessRat).apply();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getApplicationContext(), "Internet Error", Toast.LENGTH_SHORT).show();
                    }
                });
            }else{

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(userId);
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(morphModeOn){
                            Integer chessMorphRating = snapshot.child("chessRating").getValue(Integer.class);
                            if (chessMorphRating != null) {
                                int chessMorphRat = chessMorphRating - 5;
                                ref.child("chessMorphRating").setValue(chessMorphRat);
                                prefs.edit().putInt("chessMorphRating",chessMorphRat).apply();
                            }
                        }else{
                            Integer chessRating = snapshot.child("chessRating").getValue(Integer.class);
                            if (chessRating != null) {
                                int chessRat = chessRating - 5;
                                ref.child("chessRating").setValue(chessRat);
                                prefs.edit().putInt("chessRating",chessRat).apply();
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getApplicationContext(), "Internet Error", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            FirebaseDatabase.getInstance().getReference("games").child(gameId).removeValue();
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

    @Override
    public void onBackPressed() {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastBackPressedTime < BACK_PRESS_DELAY) {

            backToMenu(null);
        } else {
            lastBackPressedTime = currentTime;
            Toast.makeText(this, "Press again to leave", Toast.LENGTH_SHORT).show();
        }
    }

}