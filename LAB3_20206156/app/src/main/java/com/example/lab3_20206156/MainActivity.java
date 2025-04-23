package com.example.lab3_20206156;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private AutoCompleteTextView categoryDropdown, difficultyDropdown;
    private TextInputEditText amountInput;
    private TextInputLayout categoryLayout, amountLayout, difficultyLayout;
    private Button checkConnectionButton, startButton;

    private final Map<String, Integer> categoryMap = new HashMap<String, Integer>() {{
        put("Cultura General", 9);
        put("Libros", 10);
        put("Películas", 11);
        put("Música", 12);
        put("Computación", 18);
        put("Matemática", 19);
        put("Deportes", 21);
        put("Historia", 23);
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar vistas
        initializeViews();
        setupDropdowns();
        setupButtonListeners();
    }

    private void initializeViews() {
        categoryDropdown = findViewById(R.id.categoryDropdown);
        difficultyDropdown = findViewById(R.id.difficultyDropdown);
        amountInput = findViewById(R.id.amountInput);
        categoryLayout = findViewById(R.id.categoryLayout);
        amountLayout = findViewById(R.id.amountLayout);
        difficultyLayout = findViewById(R.id.difficultyLayout);
        checkConnectionButton = findViewById(R.id.checkConnectionButton);
        startButton = findViewById(R.id.startButton);
    }

    private void setupDropdowns() {
        String[] categories = getResources().getStringArray(R.array.categories);
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, R.layout.dropdown_item, categories);
        categoryDropdown.setAdapter(categoryAdapter);

        String[] difficulties = getResources().getStringArray(R.array.difficulties);
        ArrayAdapter<String> difficultyAdapter = new ArrayAdapter<>(this, R.layout.dropdown_item, difficulties);
        difficultyDropdown.setAdapter(difficultyAdapter);
    }

    private void setupButtonListeners() {
        checkConnectionButton.setOnClickListener(v -> checkConnection());
        startButton.setOnClickListener(v -> startQuiz());
    }

    private boolean validateInputs() {
        boolean isValid = true;

        if (categoryDropdown.getText().toString().isEmpty()) {
            categoryLayout.setError("Seleccione una categoría");
            isValid = false;
        } else {
            categoryLayout.setError(null);
        }

        if (amountInput.getText().toString().isEmpty()) {
            amountLayout.setError("Ingrese una cantidad");
            isValid = false;
        } else {
            try {
                int amount = Integer.parseInt(amountInput.getText().toString());
                if (amount <= 0) {
                    amountLayout.setError("La cantidad debe ser positiva");
                    isValid = false;
                } else {
                    amountLayout.setError(null);
                }
            } catch (NumberFormatException e) {
                amountLayout.setError("Ingrese un número válido");
                isValid = false;
            }
        }

        if (difficultyDropdown.getText().toString().isEmpty()) {
            difficultyLayout.setError("Seleccione una dificultad");
            isValid = false;
        } else {
            difficultyLayout.setError(null);
        }

        return isValid;
    }

    private void checkConnection() {
        if (!validateInputs()) {
            return;
        }

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities nc = cm.getNetworkCapabilities(cm.getActiveNetwork());
        boolean isConnected = nc != null && nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);

        if (isConnected) {
            Toast.makeText(this, "Conexión a Internet exitosa", Toast.LENGTH_SHORT).show();
            startButton.setEnabled(true);
        } else {
            Toast.makeText(this, "Error de conexión a Internet", Toast.LENGTH_SHORT).show();
            startButton.setEnabled(false);
        }
    }

    private void startQuiz() {
        String category = categoryDropdown.getText().toString();
        String difficulty = difficultyDropdown.getText().toString();
        int amount = Integer.parseInt(amountInput.getText().toString());
        int categoryId = categoryMap.get(category);

        // Convertir dificultad al formato correcto
        String apiDifficulty;
        switch(difficulty.toLowerCase()) {
            case "fácil": apiDifficulty = "easy"; break;
            case "medio": apiDifficulty = "medium"; break;
            case "difícil": apiDifficulty = "hard"; break;
            default: apiDifficulty = "medium"; // valor por defecto
        }

        int timePerQuestion = 5; // fácil por defecto
        if (difficulty.equalsIgnoreCase("medio")) {
            timePerQuestion = 7;
        } else if (difficulty.equalsIgnoreCase("difícil")) {
            timePerQuestion = 10;
        }
        int totalTime = amount * timePerQuestion;

        Intent intent = new Intent(this, QuizActivity.class);
        intent.putExtra("amount", amount);
        intent.putExtra("category", categoryId);
        intent.putExtra("difficulty", apiDifficulty); // Usar el valor convertido
        intent.putExtra("totalTime", totalTime);
        startActivity(intent);
    }
}