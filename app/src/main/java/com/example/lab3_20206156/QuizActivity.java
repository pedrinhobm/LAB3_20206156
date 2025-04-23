package com.example.lab3_20206156;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class QuizActivity extends AppCompatActivity {
    private TextView timerText, questionText, questionCountText, categoryText;
    private RadioGroup optionsGroup;
    private Button nextButton;
    private List<Question> questions = new ArrayList<>(); // se establece en un arreglo la lista de preguntas
    private int currentQuestionIndex = 0; // aca se contabilizarán preguntas acertadas, incorrectas y sin rpta
    private int correctAnswers = 0;
    private int wrongAnswers = 0;
    private int unanswered = 0;
    private CountDownTimer countDownTimer; // este es el tiempo restante de acuerdo a la multiplicacion impleementada
    private int totalTime; // en la anterior vista del menu principal
    private int remainingTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        initializeViews();

        int amount = getIntent().getIntExtra("amount", 10);
        int category = getIntent().getIntExtra("category", 9);
        String difficulty = getIntent().getStringExtra("difficulty");
        totalTime = getIntent().getIntExtra("totalTime", 60);
        remainingTime = totalTime;

        setupTimer();
        fetchQuestions(amount, category, difficulty);

        nextButton.setOnClickListener(v -> {
            if (currentQuestionIndex < questions.size() - 1) {
                checkAnswer();
                currentQuestionIndex++;
                showQuestion();
            } else {
                checkAnswer();
                showResults();
            }
        });
    }

    private void initializeViews() {
        timerText = findViewById(R.id.timerText);
        questionText = findViewById(R.id.questionText);
        questionCountText = findViewById(R.id.questionCountText);
        categoryText = findViewById(R.id.categoryText);
        optionsGroup = findViewById(R.id.optionsGroup);
        nextButton = findViewById(R.id.nextButton);
    }

    private void setupTimer() { // aquí se están implementando los hilos acorde al tiempo total ...
        countDownTimer = new CountDownTimer(totalTime * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) { // ... hasta el momento que se finaliza
                remainingTime = (int) (millisUntilFinished / 1000);
                updateTimerText();
            }

            @Override
            public void onFinish() { // aqui es cuando termina el juego y se contabiliza
                int totalAnswered = correctAnswers + wrongAnswers; // el numero total de preguntas
                unanswered = questions.size() - totalAnswered; // para ese momento solo están correctas e incorrectas
                showResults(); // más adelante indicaré acerca de las preguntas sin responder
            }
        }.start();
    }

    private void updateTimerText() { // aqui sigue la continuacion de los hilos
        int minutes = remainingTime / 60; // ya ahi está distribuido por los tiempos , tanto minutos como segundo
        int seconds = remainingTime % 60; // y se extrae y muestra en la vista del juego
        String timeFormatted = String.format("%02d:%02d", minutes, seconds);
        timerText.setText(timeFormatted);
    }

    // aqui si presente dificultades para llamar a los links , primero dado que no corria y no seleccionaba a los campos que he escogido
    // por lo que me rechazaba y volvia al menu principal. Al inicio quise realizar selectivas multiples para que de acuerdo
    // a lo que he escogido seleccione un link especifco pero sería muy extenso
    // y use ia para que solo rastreará la fuente de acuerdo a los campos de cantidad,categoria y dificultad
    // a las variables que llamé en la anterior vista serán colocadas dentro del link para que vaya directo con el balotario de preguntas
    private String getSpecificApiUrl(int amount, int category, String difficulty) {
        return String.format(
                "https://opentdb.com/api.php?amount=%d&category=%d&difficulty=%s&type=multiple",
                amount,
                category,
                difficulty
        );
    }

    private void fetchQuestions(int amount, int category, String difficulty) {
        String url = getSpecificApiUrl(amount, category, difficulty);
        Log.d("QuizActivity", "URL solicitada: " + url);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request request = chain.request();
                    Log.d("QuizActivity", "Solicitando URL: " + request.url());
                    return chain.proceed(request);
                })
                .build();

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Log.e("QuizActivity", "Error en la conexión: " + e.getMessage());
                    Toast.makeText(QuizActivity.this,
                            "Error al conectar con el servidor. Verifica tu conexión a Internet.",
                            Toast.LENGTH_LONG).show();
                    finish();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(QuizActivity.this,
                                "Error del servidor: " + response.code(),
                                Toast.LENGTH_LONG).show();
                        finish();
                    });
                    return;
                }

                try {
                    String jsonData = response.body().string();
                    JSONObject jsonObject = new JSONObject(jsonData);

                    if (jsonObject.getInt("response_code") != 0) {
                        runOnUiThread(() -> {
                            Toast.makeText(QuizActivity.this,
                                    "No hay preguntas disponibles para esta combinación",
                                    Toast.LENGTH_LONG).show();
                            finish();
                        });
                        return;
                    }

                    JSONArray results = jsonObject.getJSONArray("results");
                    questions.clear();

                    for (int i = 0; i < results.length(); i++) {
                        JSONObject questionObj = results.getJSONObject(i);
                        String question = decodeHtml(questionObj.getString("question"));
                        String correctAnswer = decodeHtml(questionObj.getString("correct_answer"));
                        JSONArray incorrectAnswers = questionObj.getJSONArray("incorrect_answers");

                        List<String> options = new ArrayList<>();
                        options.add(correctAnswer);
                        for (int j = 0; j < incorrectAnswers.length(); j++) {
                            options.add(decodeHtml(incorrectAnswers.getString(j)));
                        }
                        Collections.shuffle(options);

                        questions.add(new Question(
                                question,
                                correctAnswer,
                                options,
                                decodeHtml(questionObj.getString("category")),
                                questionObj.getString("difficulty")
                        ));
                    }

                    runOnUiThread(() -> {
                        if (questions.isEmpty()) {
                            Toast.makeText(QuizActivity.this,
                                    "No se encontraron preguntas",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            showQuestion();
                            categoryText.setText(questions.get(0).getCategory());
                        }
                    });
                } catch (JSONException e) {
                    Log.e("QuizActivity", "Error al parsear JSON", e);
                    runOnUiThread(() -> {
                        Toast.makeText(QuizActivity.this,
                                "Error al procesar las preguntas",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
            }
        });
    }
    private void showQuestion() { // esta funcion sirve para mostrar las preguntas
        Question currentQuestion = questions.get(currentQuestionIndex); // esto que está al costado del tiempo
        questionCountText.setText(String.format(
                "Pregunta %d/%d",
                currentQuestionIndex + 1,
                questions.size()
        )); // irá al costado del órden de número de pregunta sobre el total de preguntas ( por eso le puse size()

        questionText.setText(currentQuestion.getQuestion());

        optionsGroup.removeAllViews();
        optionsGroup.clearCheck();

        for (String option : currentQuestion.getOptions()) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(option);
            radioButton.setTextSize(16);
            radioButton.setPadding(8, 16, 8, 16);
            optionsGroup.addView(radioButton);
        }
    }

    private void checkAnswer() {
        int selectedId = optionsGroup.getCheckedRadioButtonId();
        if (selectedId != -1) {
            RadioButton selectedRadioButton = findViewById(selectedId);
            String selectedAnswer = selectedRadioButton.getText().toString();

            if (selectedAnswer.equals(questions.get(currentQuestionIndex).getCorrectAnswer())) {
                correctAnswers++;
            } else {
                wrongAnswers++;
            }
        }
        unanswered++;
    }

    private void showResults() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        // Calcular preguntas no respondidas
        int totalAnswered = correctAnswers + wrongAnswers;
        int actuallyUnanswered = questions.size() - totalAnswered;

        unanswered = actuallyUnanswered;  // Sobreescribir el valor

        Intent intent = new Intent(this, ResultsActivity.class);
        intent.putExtra("correct", correctAnswers);
        intent.putExtra("wrong", wrongAnswers);
        intent.putExtra("unanswered", unanswered);
        intent.putExtra("total", questions.size());
        startActivity(intent);
        finish();
    }

    private String decodeHtml(String html) {
        return android.text.Html.fromHtml(html, android.text.Html.FROM_HTML_MODE_LEGACY).toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private static class Question {
        private final String question;
        private final String correctAnswer;
        private final List<String> options;
        private final String category;
        private final String difficulty;

        public Question(String question, String correctAnswer, List<String> options, String category, String difficulty) {
            this.question = question;
            this.correctAnswer = correctAnswer;
            this.options = options;
            this.category = category;
            this.difficulty = difficulty;
        }

        public String getQuestion() { return question; }
        public String getCorrectAnswer() { return correctAnswer; }
        public List<String> getOptions() { return options; }
        public String getCategory() { return category; }
        public String getDifficulty() { return difficulty; }
    }
}