package edu.gvsu.cis.getterj.wordgame;

import android.app.ProgressDialog;
import android.content.Context;
import android.inputmethodservice.KeyboardView;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;

public class MainActivity extends ActionBarActivity {

    TextView displayWord;
    Button getWord;
    String URL = "http://api.wordnik.com/v4/words.json/randomWord?hasDictionaryDef=true&minCorpusCount=10000&maxCorpusCount=-1&minDictionaryCount=20&maxDictionaryCount=-1&minLength=5&maxLength=-1&api_key=a2a73e7b926c924fad7001ca3111acd55af2ffabf50eb4ae5";
    String scrambled = "";
    String currentWord = "";
    Button checkWord;
    EditText inputWord;
    TextView score;
    Button newGame;
    int scoreValue = 0;
    TextView time;
    ProgressDialog progress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        newGame = (Button) findViewById(R.id.newGame);
        displayWord = (TextView) findViewById(R.id.scrambledWord);
        getWord = (Button) findViewById(R.id.getWord);
        time = (TextView) findViewById(R.id.time);
        checkWord = (Button) findViewById(R.id.checkWord);
        inputWord = (EditText) findViewById(R.id.input);
        score = (TextView) findViewById(R.id.score);
        newGame.setOnClickListener(new View.OnClickListener()
             {
             public void onClick(View v)
                {
                gameTimer();
                }
             }
        );
        getWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "The word was: " + currentWord, Toast.LENGTH_LONG).show();
                GetWord newWord = new GetWord();
                newWord.execute();
            }
        });
        checkWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String guess = inputWord.getText().toString().toLowerCase();
                if (guess.equals(currentWord)) {
                    //You get a reward...
                    scoreValue = scoreValue + 10;
                    score.setText("Score is:" + scoreValue);
                    GetWord resetWord = new GetWord();
                    resetWord.execute();
                    inputWord.setText(null);

                } else {
                    //try again...
                    Toast.makeText(MainActivity.this, "Sorry, you guessed wrong!", Toast.LENGTH_LONG).show();

                }

            }
        });


        GetWord firstWord = new GetWord();
        firstWord.execute();
        this.gameTimer();

    }

    public void gameTimer() {
        newGame.setEnabled(false);
        inputWord.setFocusableInTouchMode(true);
        getWord.setEnabled(true);
        checkWord.setEnabled(true);
        score.setText("Score is:");

        new CountDownTimer(60000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                time.setText("Time Left: " + millisUntilFinished / 1000);
            }

            @Override
            public void onFinish() {
                time.setText("Time Left: 0");
                inputWord.setFocusable(false);
                newGame.setEnabled(true);
                getWord.setEnabled(false);
                checkWord.setEnabled(false);

            }
        }.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public String scramble(String input) {
        ArrayList<Character> chars = new ArrayList<Character>(input.length());
        for (char c : input.toCharArray()) {
            chars.add(c);
            Collections.shuffle(chars);
            char[] shuffled = new char[chars.size()];
            for (int i = 0; i < shuffled.length; i++) {
                shuffled[i] = chars.get(i);
            }
            scrambled = new String(shuffled);


        }
        return scrambled;
    }


    private class GetWord extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            progress = new ProgressDialog(MainActivity.this);
            progress.setMessage("Loading Word");
            progress.show();

        }

        @Override

        protected String doInBackground(Void... params) {
            try {

                HttpClient client = new DefaultHttpClient();
                HttpGet hget = new HttpGet(URL);
                HttpResponse resp = client.execute(hget);
                InputStream stream = resp.getEntity().getContent();
                char[] buffer = new char[1024];
                InputStreamReader reader = new InputStreamReader(stream);
                int len;
                StringBuffer sb = new StringBuffer();
                len = reader.read(buffer, 0, 1024);
                while (len != -1) { /* len is -1 when no more data to read */
                    sb.append(buffer, 0, len);
                    len = reader.read(buffer, 0, 1024); /* read the next chunk */
                }
                try {
                    JSONObject obj = new JSONObject(sb.toString());
                    currentWord = obj.getString("word").toLowerCase();

                } catch (JSONException e) {
                    Log.e("Oops!", "Error when parsing JSON string" + e.getMessage());
                }


                return currentWord;
            } catch (IOException e) {
                Log.e("OOPS", "There was an error " + e.getMessage());
                return null;
            }


        }

        @Override
        protected void onPostExecute(String s) {
            displayWord.setText(scramble(s));
            progress.dismiss();
        }
    }
}
