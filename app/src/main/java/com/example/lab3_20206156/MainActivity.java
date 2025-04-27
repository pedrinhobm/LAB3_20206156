package com.example.lab3_20206156;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
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
    // aqui tuve que mapear las categorias debido que en el link están colocados por un cierto número , y justo ahi
    // investigando el link propuesto, encontre cada uno con los siguientes números
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
        // la vista del menu de seleccion se  va dividir en 3 partes
        initializeViews();
        setupDropdowns();
        setupButtonListeners();
    }

    private void initializeViews() {
        categoryDropdown = findViewById(R.id.categoryDropdown); // categorias
        difficultyDropdown = findViewById(R.id.difficultyDropdown); // nivel de dificultad
        amountInput = findViewById(R.id.amountInput); // cantidad de preguntas
        categoryLayout = findViewById(R.id.categoryLayout);
        amountLayout = findViewById(R.id.amountLayout);
        difficultyLayout = findViewById(R.id.difficultyLayout);
        checkConnectionButton = findViewById(R.id.checkConnectionButton); // botones de conexion y comenzar juego
        startButton = findViewById(R.id.startButton);
    }

    private void setupDropdowns() { // en este modulo nos va servir para seleccionar la categoria y el nivel del juego
        String[] categories = getResources().getStringArray(R.array.categories); // por los arreglos que hay dentro de cada uno
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, R.layout.dropdown_item, categories);
        categoryDropdown.setAdapter(categoryAdapter);

        String[] difficulties = getResources().getStringArray(R.array.difficulties);
        ArrayAdapter<String> difficultyAdapter = new ArrayAdapter<>(this, R.layout.dropdown_item, difficulties);
        difficultyDropdown.setAdapter(difficultyAdapter);
    }

    private void setupButtonListeners() { // como indico más adelante aqui tambien use ia para la funciones de conexion a internet
        checkConnectionButton.setOnClickListener(v -> checkConnection()); // y tener el acceso a las preguntas
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
                if (amount <= 0) { // como nos han indicado el número no debe ser negativo ni cero , por eso tambien agregamos esta advertencia
                    amountLayout.setError("La cantidad debe ser positiva"); // ademas la opcion solo se puede con numeros naturales
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

    private void checkConnection() { // aquí si use inteligencia artificial debido a que estaba buscando funciones correctas
        if (!validateInputs()) { // para que se establezca la conexion hacia Internet por eso atribuyo a la librería
            return; // de networkCapabilities
        }
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities nc = cm.getNetworkCapabilities(cm.getActiveNetwork());
        boolean isConnected = nc != null && nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);

        if (isConnected) { // si llega a establecer conexion, enhorabuena e indicará el mensaje y podemos empezar a jugar
            Toast.makeText(this, "Conexión a Internet exitosa", Toast.LENGTH_SHORT).show();
            startButton.setEnabled(true);
        } else { // si no , una pena
            Toast.makeText(this, "Error de conexión a Internet", Toast.LENGTH_SHORT).show();
            startButton.setEnabled(false);
        }
    }

    private void startQuiz() { // ya como ultima parte para cambiar de vista y empezar el juego
        String category = categoryDropdown.getText().toString(); // se recoge los datos que uno a escogido
        String difficulty = difficultyDropdown.getText().toString(); // en particular la categoria , dificultad, cantidad
        int amount = Integer.parseInt(amountInput.getText().toString());
        int categoryId = categoryMap.get(category);


        String apiDifficulty;
        switch(difficulty.toLowerCase()) { // para el caso de nivel de dificultad lo cambiamos un api
            case "fácil": apiDifficulty = "easy"; break; // en su version en ingles para que de frente
            case "medio": apiDifficulty = "medium"; break; // puedan rastrear el link a la dificultad a la que vas a jugar
            case "difícil": apiDifficulty = "hard"; break;
            default: apiDifficulty = "medium";
        }

        int timePerQuestion = 5; // así mismo , están los tiempos que ha indicado el enunciado
        if (difficulty.equalsIgnoreCase("medio")) {
            timePerQuestion = 7; // facil 5 segundos , medio 7" y dificil 10"
        } else if (difficulty.equalsIgnoreCase("difícil")) {
            timePerQuestion = 10;
        } // asi como el tiempo total que se multiplica el tiempo de dificutlad por pregunta por la ccantiad de preguntas
        int totalTime = amount * timePerQuestion; // ese será el tiempo total

        Intent intent = new Intent(this, TriviaActivity.class);
        intent.putExtra("amount", amount); // con el Intent nos dirigimos a la vista del juego QuizActivity
        intent.putExtra("category", categoryId); // y transladamos los datos de seleccion y el tiempo
        intent.putExtra("difficulty", apiDifficulty); // que serán fundamentales para el desarrollo del juego
        intent.putExtra("totalTime", totalTime);
        startActivity(intent);
    }
}