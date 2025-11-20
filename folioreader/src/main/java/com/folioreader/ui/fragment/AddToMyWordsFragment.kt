package com.folioreader.ui.fragment

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.folioreader.FolioReader
import com.folioreader.R


class AddToMyWordsFragment(word: String, addToWordView: View) {
    private val TAG = "AddToMyWordsFragment"

    private lateinit var myDialog: Dialog

    private var selectedWord: String = ""
    private var translatedWord: String = ""
    private var wordExists: Boolean = false

    // ROOT VIEW INJECTED FROM OUTSIDE
    private val addToWordsView: View = addToWordView

    // VIEWS THAT WERE PREVIOUSLY SYNTHETIC
    private lateinit var wordTextView: TextView
    private lateinit var translatedWordTextView: TextView
    private lateinit var addWordButton: Button
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

                Log.d(TAG, "-> translateAndCheckReceiver -> translatedWord -> $translatedWord")
                Log.d(TAG, "-> translateAndCheckReceiver -> wordExists -> $wordExists")
                Log.d(TAG, "-> isEnabled -> ${!wordExists}")

                addWordButton.isEnabled = !wordExists
                translatedWordTextView.text = translatedWord

                addWordButton.text =  if (wordExists) "Word added" else "Add to Pocket"
                addWordButton.setBackgroundResource(if (wordExists) R.drawable.inter_grey_button else R.drawable.inter_primary_button)
            }
        }
    }

    init {
        Log.d(TAG, "-> init -> ")

        bindViews(addToWordsView)

        selectedWord = word.substring(1, word.length-1)

//        tts = TextToSpeech(addToWordsView.context, this)
//        speechButton.setOnClickListener {
//            tts?.speak(selectedWord, TextToSpeech.QUEUE_FLUSH, null,"")
//        }
        var text = selectedWord.replace("\\n","\n");
        text = text.replace("\\r","\n");
        text = text.replace("\\t","\n");
        wordTextView.text = text;

        Log.d(TAG, "-> registerReceiver")
        LocalBroadcastManager.getInstance(addToWordsView.context).registerReceiver(
            translateAndCheckReceiver, IntentFilter(AddToMyWordsFragment.ACTION_TRANSLATE_AND_CHECK)
        )

        addWordButton.visibility = if(showAddButton(selectedWord)) View.VISIBLE else View.GONE

        translateAndCheckWord(selectedWord);
        onClickAddWord(selectedWord)
        onClickSpeechButton(selectedWord)
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

    private fun bindViews(view: View) {
        Log.d(TAG, "-> bindViews -> ")

        wordTextView = view.findViewById(R.id.selectedWord)
        translatedWordTextView = view.findViewById(R.id.translatedWord)
        addWordButton = view.findViewById(R.id.addWordButton)
        speechButton = view.findViewById(R.id.speechButton)
    }

//    override fun onInit(status: Int) {
//        if (status == TextToSpeech.SUCCESS) {
//            val result = tts?.setLanguage(Locale.US)
//            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
//                Log.e("TTS","The Language specified is not supported!")
//            } else {
//                speechButton.isEnabled = true
//            }
//        } else {
//            Log.e("TTS", "Initilization Failed!")
//        }
//    }

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

        val intent = Intent(FolioReader.ACTION_DISMISS_POPUP)
        LocalBroadcastManager.getInstance(addToWordsView.context).sendBroadcast(intent)

        LocalBroadcastManager.getInstance(addToWordsView.context)
        .unregisterReceiver(translateAndCheckReceiver)

        //        tts?.stop()
        //        tts?.shutdown()
    }


    private fun translateAndCheckWord (word: String) {
        Log.d(TAG, "-> translateAndCheckWord")

        val trimmedText: String = word.replace("\u00A0", "");
        Log.v(TAG, "-> wordExist -> $trimmedText")
        val words = trimmedText.split("\\s+".toRegex()).map { word ->
            word.replace("""^[,\.]|[,\.]$""".toRegex(), "")
        }
        if (words.size > 20) {
            translatedWordTextView.visibility = View.GONE
            return
        }
        translatedWordTextView.visibility = View.VISIBLE
        translatedWordTextView.text = "Loading..."
        addWordButton.text = "Loading..."
        val localBroadcastManager = LocalBroadcastManager.getInstance(addToWordsView.context)
        val intent = Intent(FolioReader.ACTION_TRANSLATE_AND_CHECK)
        intent.putExtra(FolioReader.EXTRA_TRANSLATE_AND_CHECK, word)
        localBroadcastManager.sendBroadcast(intent)
    }

    private fun onClickAddWord (word: String) {
        addWordButton.setOnClickListener {
            Log.d(TAG, "-> addWordButton ->")


            val localBroadcastManager = LocalBroadcastManager.getInstance(it.context)
            val intent = Intent(FolioReader.ACTION_ADD_WORD)
            intent.putExtra(FolioReader.EXTRA_ADD_WORD, word)

            localBroadcastManager.sendBroadcast(intent)
        }
    }

    private fun onClickSpeechButton (textToSpeech: String) {
        speechButton.setOnClickListener {
            Log.d(TAG, "-> speechButton -> $textToSpeech")

            val localBroadcastManager = LocalBroadcastManager.getInstance(it.context)
            val  intent = Intent(FolioReader.ACTION_TEXT_TO_SPEECH)
            intent.putExtra(FolioReader.EXTRA_TEXT_TO_SPEECH, textToSpeech)

            localBroadcastManager.sendBroadcast(intent)
        }
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