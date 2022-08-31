package com.metehanbolat.voiceinput

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.MotionEvent
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar
import com.metehanbolat.voiceinput.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val speechRecognizer: SpeechRecognizer by lazy { SpeechRecognizer.createSpeechRecognizer(this) }

    private val allowPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        it?.let {
            if (it) {
                Snackbar.make(binding.root, resources.getString(R.string.permission_granted), Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.voiceButton.setOnTouchListener { _, motionEvent ->
            when(motionEvent.action) {
                MotionEvent.ACTION_UP -> {
                    lottieState(false)
                    return@setOnTouchListener true
                }
                MotionEvent.ACTION_DOWN -> {
                    getPermissionOverO(this) {
                        startListen()
                        lottieState(true)
                    }
                    return@setOnTouchListener true
                }
                else -> return@setOnTouchListener true
            }
        }
    }

    private fun startListen() {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())

            speechRecognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(p0: Bundle?) {}

                override fun onBeginningOfSpeech() {
                    binding.inputVoice.setText(resources.getString(R.string.listening))
                }

                override fun onRmsChanged(p0: Float) {}
                override fun onBufferReceived(p0: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onError(p0: Int) {}

                override fun onResults(bundle: Bundle?) {
                    bundle?.let {
                        val result = it.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        binding.inputVoice.setText(result?.get(0))
                    }
                }

                override fun onPartialResults(p0: Bundle?) {}
                override fun onEvent(p0: Int, p1: Bundle?) {}
            })
            speechRecognizer.startListening(this)
        }

    }

    private fun getPermissionOverO(context: Context, call: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                call.invoke()
            } else {
                allowPermission.launch(android.Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    private fun lottieState(state: Boolean) {
        binding.lottieCircle.apply {
            if (state) {
                visibility = View.VISIBLE
                playAnimation()
            } else {
                visibility = View.GONE
                pauseAnimation()
            }
        }
    }
}