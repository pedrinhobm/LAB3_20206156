package com.example.lab3_20206156;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class ResultsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        // Obtener resultados del intent
        int correct = getIntent().getIntExtra("correct", 0);
        int wrong = getIntent().getIntExtra("wrong", 0);
        int unanswered = getIntent().getIntExtra("unanswered", 0);

        // Mostrar resultados
        TextView correctText = findViewById(R.id.correctText);
        TextView wrongText = findViewById(R.id.wrongText);
        TextView unansweredText = findViewById(R.id.unansweredText);

        correctText.setText(String.valueOf(correct));
        wrongText.setText(String.valueOf(wrong));
        unansweredText.setText(String.valueOf(unanswered));

        // Configurar botón para volver a jugar
        Button playAgainButton = findViewById(R.id.playAgainButton);
        playAgainButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }
}
