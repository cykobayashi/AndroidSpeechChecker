package ckobayashi.com.br.androidspeechtest

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*
import kotlin.collections.RandomAccess


class MainActivity : AppCompatActivity() {

    private val REQ_CODE_SPEECH_INPUT = 100
    private var phrase: Phrase? = null

    private var quotesDAO: QuotesDAO? = null
    private var phrases: List<Phrase>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        quotesDAO = QuotesDAO(this)

        // importCSV()

        phrases = quotesDAO!!.getFavorites()

        changePhrase()

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
                            markAsCorrentCurrentPhrase()
                        }
                    }
                    if (result.size > 1) {
                        txtSpeechInputSnd.text = result[1]
                    }
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

        var alternatives = getAlternatives(phrase)

        for (alternative in alternatives!!) {
            if (trim(heard) == trim(alternative)) {
                return true
            }
        }

        return false

    }

    private fun getAlternatives(phrase: String): MutableList<String>? {

        var alternatives: MutableList<String> = mutableListOf<String>()

        alternatives.add(phrase)

        if (phrase.contains("'ve")) {
            alternatives.add(phrase.replace("'ve", " have"))
        }

        if (phrase.contains("'m")) {
            alternatives.add(phrase.replace("'m", " am"))
        }

        if (phrase.contains("'d")) {
            alternatives.add(phrase.replace("'d", " would"))
        }


        if (phrase.contains("'s")) {
            alternatives.add(phrase.replace("'s", " is"))
        }

        if (phrase.contains("'ll")) {
            alternatives.add(phrase.replace("'ll", " will"))
        }

        return alternatives

    }

    private fun trim(heard: String?): String {

        val re = Regex("[^a-z0-9 ]")
        return re.replace(heard!!.toLowerCase().trim(), "")

    }

    private fun importCSV() = try {
        val `is` = InputStreamReader(assets.open("scratch.txt"))
        val reader = BufferedReader(`is`)
        var line: String? = null;
        var count = 0

        while ({ line = reader.readLine(); line }() != null) {
            quotesDAO?.insertQuote(line, "", 0)
            count++
        }

        showToast("Lines: $count")

    } catch (e: IOException) {
        e.printStackTrace()
    }

    private fun showToast(text: String) {
        val duration = Toast.LENGTH_LONG

        val toast = Toast.makeText(this, text, duration)
        toast.show()
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
            R.id.action_skip -> skipCurrentPhrase()
            R.id.action_delete -> deleteCurrentPhrase()
            R.id.action_mark_correct -> markAsCorrentCurrentPhrase()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun markAsCorrentCurrentPhrase(): Boolean {

        if (phrase!!.rating + 1 >= 5) {
            quotesDAO?.deleteQuote(phrase!!.id)
        } else {
            quotesDAO?.updateQuoteRating(phrase!!.id, phrase!!.rating + 1)
        }
        changePhrase();

        return true

    }

    private fun skipCurrentPhrase(): Boolean {

        changePhrase()
        return true

    }

    private fun deleteCurrentPhrase(): Boolean {

        quotesDAO?.deleteQuote(phrase!!.id)
        changePhrase()

        return true
    }

}
