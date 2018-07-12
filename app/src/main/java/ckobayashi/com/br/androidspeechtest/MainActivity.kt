package ckobayashi.com.br.androidspeechtest

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.*


class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {


    private val REQ_CODE_SPEECH_INPUT = 100
    private var phrase: Phrase? = null

    private var quotesDAO: QuotesDAO? = null
    private var phrases: List<Phrase>? = null

    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        tts = TextToSpeech(this, this)

        quotesDAO = QuotesDAO(this)

//        quotesDAO!!.insertQuote("At what time?", "", 0)
//        quotesDAO!!.insertQuote("Follow me.", "", 0)
//        quotesDAO!!.insertQuote("From here to there.", "", 0)
//        quotesDAO!!.insertQuote("Turn around.", "", 0)
//        quotesDAO!!.insertQuote("Why not?", "", 0)

        phrases = quotesDAO!!.getFavorites()

        changePhrase()

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        btnSpeak.setOnClickListener{
            promptSpeechInput();
        };
    }

    /**
     * Receiving speech input
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        txtSpeechInput.text = ""
        txtSpeechInputSnd.text = ""

        when (requestCode) {
            REQ_CODE_SPEECH_INPUT -> {
                if (resultCode == RESULT_OK && null != data) {

                    val result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    if (result.size > 0) {
                        txtSpeechInput.text = result[0]
                        if (isCorrect(result[0], phrase!!.phrase)) {
                            changePhrase();
                        }
                    }
                    if (result.size > 1)
                        txtSpeechInputSnd.text = result[1]
                }
            }
        }
    }

    private fun changePhrase() {
        var rnd = Random(System.nanoTime())
        phrase = phrases?.get(rnd.nextInt(phrases!!.size))

        txtRead.text = phrase!!.phrase
    }

    private fun isCorrect(heard: String?, phrase: String): Boolean {

        if (trim(heard).equals(trim(phrase))) {
            return true
        }

        return false

    }

    private fun trim(heard: String?): String {

        val re = Regex("[^a-z0-9 ]")
        return re.replace(heard!!.toLowerCase().trim(), "")

    }

    /**
     * Showing google speech input dialog
     */
    private fun promptSpeechInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt))
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT)
        } catch (a: ActivityNotFoundException) {
            Toast.makeText(applicationContext,
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show()
        }

    }

    override fun onInit(status: Int) {

        if (status == TextToSpeech.SUCCESS) {

            val result = tts?.setLanguage(Locale("en", "US"))

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported")
            }

        } else {
            Log.e("TTS", "Initilization Failed!")
        }

    }

    private fun speakOut() {

        val text = phrase?.phrase
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

}
