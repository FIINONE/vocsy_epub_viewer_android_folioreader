package com.folioreader.ui.fragment

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.folioreader.Constants
import com.folioreader.FolioReader
import com.folioreader.R
import kotlinx.android.synthetic.main.folio_page_fragment.view.*
import kotlinx.android.synthetic.main.layout_add_to_my_words.*
import kotlinx.android.synthetic.main.layout_add_to_my_words.view.*
import java.util.*


class AddToMyWordsFragment(word: String, addToWardView: View) : TextToSpeech.OnInitListener {
    private val TAG = "AddToMyWordsFragment"

    private lateinit var myDialog: Dialog

    private var selectedWord: String = ""
    private var translatedWord: String = ""
    private var wordExists: Boolean = false

    private lateinit var addToWordsView: View
    private lateinit var wordTextView: TextView
    private lateinit var translatedWordTextView: TextView
    private lateinit var addWordButton: Button


    private var tts: TextToSpeech? = null
    private lateinit var speechButton: ImageButton

    companion object {
        const val EXTRA_TRANSLATE = "EXTRA_TRANSLATE"
        const val EXTRA_CHECK = "EXTRA_CHECK"
        const val ACTION_TRANSLATE_AND_CHECK = "ACTION_TRANSLATE_AND_CHECK"
    }

    private val translateAndCheckReceiver = object : BroadcastReceiver () {
        override fun onReceive(context: Context, intent: Intent) {
            val extras = intent.extras;
            Log.d(TAG, "-> translateAndCheckReceiver -> onReceive")

            if (extras != null) {
                translatedWord = extras.getString(EXTRA_TRANSLATE, "")
                wordExists = extras.getBoolean(EXTRA_CHECK, false)
                addToWordsView.addWordButton.isEnabled = !wordExists

                Log.d(TAG, "-> translateAndCheckReceiver -> translatedWord -> $translatedWord")
                Log.d(TAG, "-> translateAndCheckReceiver -> wordExists -> $wordExists")
                Log.d(TAG, "-> isEnabled -> ${!wordExists}")

                translatedWordTextView.text = translatedWord
                addWordButton.text =  if (wordExists) "Word added" else "Add to Pocket"
            }
        }
    }

    init {
        Log.d(TAG, "-> init -> ")

        addToWordsView = addToWardView
        bindViews(addToWordsView)

        selectedWord = word.substring(1, word.length-1)

        tts = TextToSpeech(addToWordsView.context, this)
        speechButton.setOnClickListener {
            tts?.speak(selectedWord, TextToSpeech.QUEUE_FLUSH, null,"")
        }

        wordTextView.text = selectedWord;
        translatedWordTextView.text = "Translated"

        Log.d(TAG, "-> registerReceiver")
        LocalBroadcastManager.getInstance(addToWordsView.context).registerReceiver(
            translateAndCheckReceiver, IntentFilter(AddToMyWordsFragment.ACTION_TRANSLATE_AND_CHECK)
        )

        addWordButton.visibility = if(showAddButton(selectedWord)) View.VISIBLE else View.GONE

        translateAndCheckWord(selectedWord);
        onClickAddWord(selectedWord)
    }

    private fun showAddButton (selectedText: String): Boolean {
        Log.v(TAG, "-> wordExist -> $selectedText")
        val trimedText: String = selectedText.replace("\u00A0", "");
        Log.v(TAG, "-> wordExist -> $trimedText")
        val words = trimedText.split("\\s+".toRegex()).map { word ->
            word.replace("""^[,\.]|[,\.]$""".toRegex(), "")
        }
        Log.v(TAG, "-> wordExist -> $words")
        Log.v(TAG, "-> wordExist -> ${words.size}")
        val showAddWord =  words.size <= 3
        Log.v(TAG, "-> wordExist -> $showAddWord")

        return showAddWord
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS","The Language specified is not supported!")
            } else {
                speechButton.isEnabled = true
            }
        } else {
            Log.e("TTS", "Initilization Failed!")
        }
    }

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        Log.d(TAG, "-> onCreate -> ")
//    }

//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        Log.d(TAG, "-> onCreateDialog -> ")
//        myDialog = Dialog(context!!)
//        addToWordsView = View.inflate(context, R.layout.layout_add_to_my_words, null)
//        myDialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
//        bindViews(addToWordsView)
//        word = arguments!!.getString(Constants.SELECTED_WORD, "")
//
//        myDialog.setContentView(addToWordsView)
//
//        tts = TextToSpeech(addToWordsView.context, this)
//        speechButton.setOnClickListener {
//            tts?.speak(word, TextToSpeech.QUEUE_FLUSH, null,"")
//        }
//
//        wordTextView.text = word;
//        translatedWordTextView.text = "Translated"
//
//        val wmlp = myDialog!!.window!!.attributes;
//        wmlp.gravity = Gravity.TOP or Gravity.LEFT
//
//
//        val xPos = arguments!!.getInt("x_pos")
//        val yPos = arguments!!.getInt("y_pos")
////        Log.d(TAG, "-> onCreateDialog -> $xPos")
////        Log.d(TAG, "-> onCreateDialog -> $yPos")
//
//        Log.d(TAG, "-> registerReceiver")
//        LocalBroadcastManager.getInstance(addToWordsView.context).registerReceiver(
//            translateAndCheckReceiver, IntentFilter(AddToMyWordsFragment.ACTION_TRANSLATE_AND_CHECK)
//        )
//
//        translateAndCheckWord(word);
//        onClickAddWord(word)
//
//        wmlp.x = xPos;
//        wmlp.y = yPos;
//        return myDialog
//    }

     fun destroy () {
        Log.d(TAG, "-> onDestroy")
        LocalBroadcastManager.getInstance(addToWordsView.context)
            .unregisterReceiver(translateAndCheckReceiver)

        tts?.stop()
        tts?.shutdown()
    }


    private fun translateAndCheckWord (word: String) {
        Log.d(TAG, "-> translateAndCheckWord")
        val localBroadcastManager = LocalBroadcastManager.getInstance(addToWordsView.context)
        val intent = Intent(FolioReader.ACTION_TRANSLATE_AND_CHECK)
        intent.putExtra(FolioReader.EXTRA_TRANSLATE_AND_CHECK, word)
        localBroadcastManager.sendBroadcast(intent)
    }

    private fun onClickAddWord (word: String) {
        addWordButton.setOnClickListener {
            Log.d(TAG, "-> addWordButton ->")


            val localBroadcastManager = LocalBroadcastManager.getInstance(it.context);
            val intent = Intent(FolioReader.ACTION_ADD_WORD)
            intent.putExtra(FolioReader.EXTRA_ADD_WORD, word)


            localBroadcastManager.sendBroadcast(intent)
//            dismiss()
        }
    }

    private fun bindViews(view: View) {
        Log.d(TAG, "-> bindViews -> ")
        wordTextView = addToWordsView.selectedWord
        translatedWordTextView = addToWordsView.translatedWord
        addWordButton = addToWordsView.addWordButton
        speechButton = addToWordsView.speechButton
    }

//    override fun onDestroy() {
//        Log.d(TAG, "-> onDestroy")
//        LocalBroadcastManager.getInstance(addToWordsView.context)
//            .unregisterReceiver(translateAndCheckReceiver)
//
//        tts?.stop()
//        tts?.shutdown()
//        super.onDestroy()
//    }
}