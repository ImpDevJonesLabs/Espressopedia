package com.joneslabs13.espressopedia.ui.brew

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.os.Bundle
import android.os.CountDownTimer
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.joneslabs13.espressopedia.R
import com.joneslabs13.espressopedia.data.AppPreferences
import com.joneslabs13.espressopedia.databinding.FragmentBrewBinding


class BrewFragment : Fragment() {

    private lateinit var binding: FragmentBrewBinding
    private var timer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 0
    private var timerRunning = false
    private var initialTimeInMillis: Long = 0 // Default to 0 minutes and 0 seconds

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBrewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialTimeInMillis = AppPreferences.getDefaultBrewTime(requireContext())
        // Set initial time
        timeLeftInMillis = initialTimeInMillis
        updateCountDownText()
        updateProgressBar()

        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.brew_method_options,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerBrewType.adapter = adapter

        binding.buttonStartPause.setOnClickListener {
            if (timerRunning) {
                pauseTimer()
            } else {
                startTimer()
            }
        }

        binding.buttonReset.setOnClickListener {
            resetTimer()
        }

        binding.buttonSetTime.setOnClickListener {
            showTimePickerDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!timerRunning) {
            initialTimeInMillis = AppPreferences.getDefaultBrewTime(requireContext())
            timeLeftInMillis = initialTimeInMillis
            updateCountDownText()
            updateButtons()
            updateProgressBar()
        }
    }

    private fun startTimer() {
        timer = object : CountDownTimer(timeLeftInMillis, 50) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateCountDownText()
                updateProgressBar()
            }

            override fun onFinish() {
                timeLeftInMillis = 0
                timerRunning = false
                updateButtons()
                updateProgressBar()
                playAlarmSound()
            }
        }.start()

        timerRunning = true
        updateButtons()
    }

    private fun playAlarmSound() {
        try {
            val uriString = AppPreferences.getDefaultRingtoneUri(requireContext())
            val uri = if (uriString.isNotEmpty()) {
                android.net.Uri.parse(uriString)
            } else {
                android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM)
                    ?: android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
            }
            val ringtone = android.media.RingtoneManager.getRingtone(requireContext(), uri)
            ringtone?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun pauseTimer() {
        timer?.cancel()
        timerRunning = false
        updateButtons()
    }

    private fun resetTimer() {
        timeLeftInMillis = initialTimeInMillis
        updateCountDownText()
        updateButtons()
        updateProgressBar()
        pauseTimer()
    }

    private fun updateCountDownText() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60

        val timeLeftFormatted = String.format("%02d:%02d", minutes, seconds)
        binding.circularProgressBar.setTimerText(timeLeftFormatted)
    }

    private fun updateButtons() {
        if (timerRunning) {
            binding.buttonStartPause.setImageDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.pause_24px)
            )
            binding.buttonReset.visibility = View.INVISIBLE
            binding.buttonSetTime.visibility = View.INVISIBLE
        } else {
            binding.buttonStartPause.setImageDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.play_arrow_24px)
            )
            binding.buttonSetTime.visibility = View.VISIBLE
            if (timeLeftInMillis < initialTimeInMillis) {
                binding.buttonReset.visibility = View.VISIBLE
            } else {
                binding.buttonReset.visibility = View.INVISIBLE
            }
        }
    }

    private fun showTimePickerDialog() {
        val currentMinutes = ((initialTimeInMillis / 1000) / 60).toInt()
        val currentSeconds = ((initialTimeInMillis / 1000) % 60).toInt()

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_set_time, null)
        val pickerMinutes = dialogView.findViewById<android.widget.NumberPicker>(R.id.numberPickerMinutes).apply {
            minValue = 0
            maxValue = 59
            value = currentMinutes
            wrapSelectorWheel = true
        }
        val pickerSeconds = dialogView.findViewById<android.widget.NumberPicker>(R.id.numberPickerSeconds).apply {
            minValue = 0
            maxValue = 59
            value = currentSeconds
            wrapSelectorWheel = true
        }

        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.set_brew_time)
            .setView(dialogView)
            .setPositiveButton("Set") { _, _ ->
                val minutes = pickerMinutes.value
                val seconds = pickerSeconds.value
                val totalSeconds = (minutes * 60) + seconds
                initialTimeInMillis = totalSeconds * 1000L
                timeLeftInMillis = initialTimeInMillis
                updateCountDownText()
                updateButtons()
                updateProgressBar()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateProgressBar() {
        val progress = if (initialTimeInMillis > 0) {
            (timeLeftInMillis.toFloat() / initialTimeInMillis.toFloat()) * 100f
        } else {
            0f
        }
        binding.circularProgressBar.setProgress(progress)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()
    }
}

class CircularProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var progress = 0f
    private var maxProgress = 100f
    private var circleColor = Color.BLUE
    private var circleWidth = 20f
    private var textColor = Color.BLACK
    private var textSize = 40f
    private var timerText: String = "00:00"
    private val rectF: RectF = RectF()
    private val backgroundPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E0E0E0")
        style = Paint.Style.STROKE
        strokeWidth = circleWidth
        strokeCap = Paint.Cap.ROUND
    }
    private val foregroundPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = circleColor
        style = Paint.Style.STROKE
        strokeWidth = circleWidth
        strokeCap = Paint.Cap.ROUND
    }
    private val textPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor
        textAlign = Paint.Align.CENTER
    }

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircularProgressBar)
        try {
            circleColor = typedArray.getColor(R.styleable.CircularProgressBar_circleColor, Color.BLUE)
            circleWidth = typedArray.getDimension(R.styleable.CircularProgressBar_circleWidth, 20f)
            textColor = typedArray.getColor(R.styleable.CircularProgressBar_textColor, Color.BLACK)
            textSize = typedArray.getDimension(R.styleable.CircularProgressBar_textSize, 40f)
            maxProgress = typedArray.getInt(R.styleable.CircularProgressBar_maxProgress, 100).toFloat()
        } finally {
            typedArray.recycle()
        }
        foregroundPaint.color = circleColor
        foregroundPaint.strokeWidth = circleWidth
        backgroundPaint.strokeWidth = circleWidth
        textPaint.color = textColor
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawOval(rectF, backgroundPaint)
        val angle = progressToAngle(progress)
        canvas.drawArc(rectF, -90f, angle, false, foregroundPaint)

        // Draw text in the center
        textPaint.textSize = calculateTextSize(width, height)
        val textBounds = Rect()
        textPaint.getTextBounds(timerText, 0, timerText.length, textBounds)
        val x = width / 2f
        val y = height / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText(timerText, x, y, textPaint)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val padding = circleWidth
        rectF.set(padding, padding, w - padding, h - padding)
    }

    fun setProgress(progress: Float) {
        this.progress = progress.coerceIn(0f, maxProgress)
        invalidate()
    }

    fun setTimerText(text: String) {
        timerText = text
        invalidate()
    }

    private fun progressToAngle(progress: Float): Float {
        return (progress / maxProgress) * 360f
    }

    private fun calculateTextSize(width: Int, height: Int): Float {
        // Calculate a text size that fits within the circle
        val minDimension = minOf(width, height)
        return minDimension / 4f // Adjust this factor as needed
    }
}