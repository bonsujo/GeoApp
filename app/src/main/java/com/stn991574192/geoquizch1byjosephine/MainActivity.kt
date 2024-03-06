package com.stn991574192.geoquizch1byjosephine

import android.app.Activity
import android.content.Intent
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import com.stn991574192.geoquizch1byjosephine.databinding.ActivityMainBinding

private const val TAG = "MainActivity"


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // associating the activity with an instance of QuizViewModel
    private val quizViewModel : QuizViewModel by viewModels()


    // contract to take intent as input
    private val cheatLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Handle the result
        if (result.resultCode == Activity.RESULT_OK) {
            quizViewModel.isCheater =
                result.data?.getBooleanExtra(EXTRA_ANSWER_SHOWN, false) ?: false
        }
    }

    private var quizGradeIndex = 0
    private var answerCorrectCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate(Bundle?) called")
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "Got a QuizViewModel: $quizViewModel")

        updateQuestion()
        binding.tokenTextView.text = "Cheat Tokens left: ${quizViewModel.numOfTokens}"


        if (quizViewModel.numOfTokens == 0) {
            binding.cheatButton.isEnabled = false
        }

        //getting the build version of the current hardware device used to run application
        binding.apiTextView.append(Build.VERSION.SDK_INT.toString())

        binding.trueButton.setOnClickListener { view: View ->
            checkAnswer(true)
        }

        binding.falseButton.setOnClickListener { view: View ->
            checkAnswer(false)
        }

        binding.nextButton.setOnClickListener {
            quizViewModel.moveToNext()
            quizGradeIndex++
            updateQuestion()
        }

        binding.cheatButton.setOnClickListener {
            //starting cheat activity
            val answerIsTrue = quizViewModel.currentQuestionAnswer
            val intent = CheatActivity.newIntent(this@MainActivity, answerIsTrue)
            //startActivity(intent)
            cheatLauncher.launch(intent)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            blurCheatButton()
        }

        binding.previousButton.setOnClickListener {
            quizViewModel.moveToPrevious()
            quizGradeIndex--
            updateQuestion()
        }
        binding.questionTextView.setOnClickListener {
            quizViewModel.moveToNext()
            quizGradeIndex++
            updateQuestion()
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart() called")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume() called")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause() called")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop() called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy() called")
    }

    //updates question when next or previous button is pressed
    private fun updateQuestion() {
        val questionTextResId = quizViewModel.currentQuestionText
        binding.questionTextView.setText(questionTextResId)

        //used when question is answered, disable true and false buttons
        binding.trueButton.isEnabled = true
        binding.falseButton.isEnabled = true
        //println("question res id:" + quizViewModel.currentQuestionText)
    }

    //checking whether user has cheated when true or false button is pressed
    private fun checkAnswer(userAnswer: Boolean) {
        val correctAnswer = quizViewModel.currentQuestionAnswer

        //checking when user goes to cheat activity, present judgment toast, if not present correct or incorrect toast
        val messageResId = when {
            quizViewModel.isCheater -> {
                quizViewModel.numOfTokens--
                if (quizViewModel.numOfTokens == 0) {
                   binding.cheatButton.isEnabled = false
                }
                R.string.judgment_toast
            }
            userAnswer == correctAnswer -> {
                R.string.correct_toast
            }
            else -> R.string.incorrect_toast
        }

        //println("NUMBER OF TOKENS:  " + quizViewModel.numOfTokens)

        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show()

        binding.trueButton.isEnabled = false
        binding.falseButton.isEnabled = false

        if (quizGradeIndex == quizViewModel.questionBankSize - 1) {
            val quizPercentage = (answerCorrectCount.toDouble() / quizViewModel.questionBankSize * 100).toInt()
            Toast.makeText(this, "You got a $quizPercentage%", Toast.LENGTH_LONG).show()
        }
    }

    //used to blur cheat button when version code is API 31
    @RequiresApi(Build.VERSION_CODES.S)
    private fun blurCheatButton() {
        val effect = RenderEffect.createBlurEffect(
            10.0f,
            10.0f,
            Shader.TileMode.CLAMP
        )
        binding.cheatButton.setRenderEffect(effect)
    }
}