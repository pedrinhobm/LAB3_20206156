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

        // para la obtencion de resultados
        // recogemos de la vista anterior la cantidad de rptas
        // correctas, incorrectas y sin responder
        int correct = getIntent().getIntExtra("correct", 0);
        int wrong = getIntent().getIntExtra("wrong", 0);
        int unanswered = getIntent().getIntExtra("unanswered", 0);

        // recogen los datos para mostrarlos
        // eso será representados por color
        // aquí ya no tuve ningún problema o duda para implementarlo en el código
        TextView correctText = findViewById(R.id.correctText);
        TextView wrongText = findViewById(R.id.wrongText);
        TextView unansweredText = findViewById(R.id.unansweredText);
        correctText.setText(String.valueOf(correct));
        wrongText.setText(String.valueOf(wrong));
        unansweredText.setText(String.valueOf(unanswered));

        // aqui tambien se enceuntra el boton que nos enviará
        // al menu principal , es por ello que para cambiar de vista usamos Intent
        Button playAgainButton = findViewById(R.id.playAgainButton);
        playAgainButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }
}
