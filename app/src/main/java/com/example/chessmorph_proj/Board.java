package com.example.chessmorph_proj;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Board extends AppCompatActivity {

    // Размер доски
    private static final int BOARD_SIZE = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //ориентация вертикальная

        setContentView(R.layout.activity_board);  // Убедитесь, что у вас есть соответствующий layout файл

        // Получаем GridLayout из layout
        GridLayout gridLayout = findViewById(R.id.chessBoard); // Используйте правильный ID для GridLayout

        // Устанавливаем количество строк и столбцов
        gridLayout.setRowCount(BOARD_SIZE);
        gridLayout.setColumnCount(BOARD_SIZE);

        // Создаем и добавляем клетки в GridLayout
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Button cell = new Button(this);

                // Вычисляем цвет клетки: черный или белый
                if ((row + col) % 2 == 0) {
                    cell.setBackgroundColor(getResources().getColor(android.R.color.white)); // Белый
                } else {
                    cell.setBackgroundColor(getResources().getColor(R.color.green)); // Черный
                }

                // Устанавливаем размеры кнопки, чтобы они занимали одинаковое пространство
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 0;
                params.height = 0;
                params.rowSpec = GridLayout.spec(row, 1f);  // Растягиваем кнопку на всю ширину
                params.columnSpec = GridLayout.spec(col, 1f);  // Растягиваем кнопку на всю высоту

                // Добавляем кнопку в gridLayout
                cell.setLayoutParams(params);
                gridLayout.addView(cell);

                // Обработчик клика по клетке (для примера можно просто сменить цвет)
                final int finalRow = row;
                final int finalCol = col;
                cell.setOnClickListener(v -> {
                    // Просто меняем цвет клетки при нажатии
                    if ((finalRow + finalCol) % 2 == 0) {
                        cell.setBackgroundColor(getResources().getColor(android.R.color.white));
                    } else {
                        cell.setBackgroundColor(getResources().getColor(R.color.green));
                    }
                });
            }
        }
    }
    public void backToMenu(View view) {
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
        finish();
    }
}