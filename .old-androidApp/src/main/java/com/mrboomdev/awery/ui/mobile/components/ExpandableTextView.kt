package com.mrboomdev.awery.ui.mobile.components

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.GradientDrawable.Orientation.BOTTOM_TOP
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.AttributeSet
import android.view.MotionEvent
import com.google.android.material.textview.MaterialTextView
import com.mrboomdev.awery.R
import com.mrboomdev.awery.generated.*
import com.mrboomdev.awery.platform.i18n
import java.lang.Integer.min
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * This value define how much space should be consumed by ellipsize text to represent itself.
 */
const val ELLIPSIZE_TEXT_LENGTH_MULTIPLIER = 2.0

class ExpandableTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.expandableTextView
) : MaterialTextView(
    context, attrs, defStyleAttr
) {
    private var mOriginalText: CharSequence? = ""
    private var mCollapsedLines = 0
    private var mReadMoreText: CharSequence
    private var mReadLessText: CharSequence
    var isExpanded: Boolean = false
        private set
    private var mAnimationDuration: Int? = 0
    private var foregroundColor: Int? = 0
    private var initialText = ""
    private var isUnderlined: Boolean? = false
    private var mEllipsizeTextColor: Int? = 0
    private lateinit var collapsedVisibleText: String

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if(event?.action == MotionEvent.ACTION_UP && (event.eventTime - event.downTime) < 500) {
            toggleExpandableTextView()
        }

        return super.onTouchEvent(event)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (initialText.isBlank()) {
            initialText = text.toString()
            collapsedVisibleText = collapsedVisibleText()
            //Override expand property in specific scenarios

            isExpanded = if(collapsedVisibleText.isAllTextVisible()) true else isExpanded

            setEllipsizedText(isExpanded)
            setForeground(isExpanded)
        }
    }

    private fun toggleExpandableTextView() {
        //No expand/collapse needed if collapse text is identical to complete text
        if(collapsedVisibleText.isAllTextVisible()) {
            return
        }

        isExpanded = !isExpanded
        configureMaxLines()

        val startHeight = measuredHeight

        measure(
            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        )

        val endHeight = measuredHeight

        animationSet(startHeight, endHeight).apply {
            duration = mAnimationDuration?.toLong()!!
            start()

            addListener(object : Animator.AnimatorListener {
                override fun onAnimationEnd(animation: Animator) {
                    if(!isExpanded)
                        setEllipsizedText(isExpanded)
                }

                override fun onAnimationRepeat(animation: Animator) {}
                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationStart(animation: Animator) {}
            })
        }

        setEllipsizedText(isExpanded)
    }

    private fun configureMaxLines() {
        if (mCollapsedLines < COLLAPSED_MAX_LINES) {
            maxLines = if (isExpanded) COLLAPSED_MAX_LINES else mCollapsedLines
        }
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        mOriginalText = text
        super.setText(text, type)
    }

    fun setReadMoreText(readMore: String): ExpandableTextView {
        mReadMoreText = readMore
        return this
    }

    fun setReadLessText(readLess: String): ExpandableTextView {
        mReadLessText = readLess
        return this
    }

    fun setCollapsedLines(collapsedLines: Int): ExpandableTextView {
        mCollapsedLines = collapsedLines
        return this
    }

    fun setIsExpanded(isExpanded: Boolean): ExpandableTextView {
        this.isExpanded = isExpanded
        return this
    }

    fun setAnimationDuration(animationDuration: Int): ExpandableTextView {
        mAnimationDuration = animationDuration
        return this
    }

    fun setIsUnderlined(isUnderlined: Boolean): ExpandableTextView {
        this.isUnderlined = isUnderlined
        return this
    }

    fun setEllipsizedTextColor(ellipsizeTextColor: Int): ExpandableTextView {
        mEllipsizeTextColor = ellipsizeTextColor
        return this
    }

    fun setForegroundColor(foregroundColor: Int): ExpandableTextView {
        this.foregroundColor = foregroundColor
        return this
    }

    fun toggle() {
        toggleExpandableTextView()
    }

    //private functions
    init {
        context.obtainStyledAttributes(attrs, R.styleable.ExpandableTextView).apply {
            try {
                mCollapsedLines = getInt(R.styleable.ExpandableTextView_collapsedLines, COLLAPSED_MAX_LINES)
                mAnimationDuration = getInt(R.styleable.ExpandableTextView_animDuration, DEFAULT_ANIM_DURATION)
                mReadMoreText = getString(R.styleable.ExpandableTextView_readMoreText) ?: "   ${i18n(Res.string.read_more)}"
                mReadLessText = getString(R.styleable.ExpandableTextView_readLessText) ?: "   ${i18n(Res.string.read_less)}"
                foregroundColor = getColor(R.styleable.ExpandableTextView_foregroundColor, Color.TRANSPARENT)
                isUnderlined = getBoolean(R.styleable.ExpandableTextView_isUnderlined, false)
                isExpanded = getBoolean(R.styleable.ExpandableTextView_textIsExpanded, false)
                mEllipsizeTextColor = getColor(R.styleable.ExpandableTextView_ellipsizeTextColor, Color.BLUE)
            } finally {
                this.recycle()
            }
        }

        configureMaxLines()
    }

    private fun setEllipsizedText(isExpanded: Boolean) {
        if(initialText.isBlank()) return

        text = if(collapsedVisibleText.isAllTextVisible()) initialText else {
             if(isExpanded) getExpandText() else getCollapseText()
        }
    }

    private fun getExpandText(): SpannableStringBuilder {
        return SpannableStringBuilder(initialText)
            .append(EMPTY_SPACE)
            .append(mReadLessText.toString().span())
    }

    private fun getCollapseText(): SpannableStringBuilder {
        val ellipseTextLength = ((mReadMoreText.length + DEFAULT_ELLIPSIZED_TEXT.length) * ELLIPSIZE_TEXT_LENGTH_MULTIPLIER).roundToInt()
        val textAvailableLength = max(0, collapsedVisibleText.length - ellipseTextLength)
        val ellipsizeAvailableLength = min(collapsedVisibleText.length, DEFAULT_ELLIPSIZED_TEXT.length)
        val readMoreAvailableLength = min(collapsedVisibleText.length - ellipsizeAvailableLength, mReadMoreText.length)

        return SpannableStringBuilder(collapsedVisibleText.substring(0, textAvailableLength))
            .append(DEFAULT_ELLIPSIZED_TEXT.substring(0, ellipsizeAvailableLength))
            .append(mReadMoreText.substring(0, readMoreAvailableLength).span())
    }

    private fun collapsedVisibleText(): String {
        try {
            var finalTextOffset = 0
            if (mCollapsedLines < COLLAPSED_MAX_LINES) {
                for (i in 0 until mCollapsedLines) {
                    val textOffset = layout.getLineEnd(i)
                    if (textOffset == initialText.length)
                        return initialText
                    else
                        finalTextOffset = textOffset
                }
                return initialText.substring(0, finalTextOffset)
            } else {
                return initialText
            }
        } catch(e: Exception) {
            e.printStackTrace()
            return initialText
        }
    }

    private fun setForeground(isExpanded: Boolean) {
        foreground = GradientDrawable(BOTTOM_TOP, intArrayOf(foregroundColor!!, Color.TRANSPARENT))
        foreground.alpha = if (isExpanded) {
            MIN_VALUE_ALPHA
        } else {
            MAX_VALUE_ALPHA
        }
    }

    @SuppressLint("ObjectAnimatorBinding")
    private fun animationSet(startHeight: Int, endHeight: Int): AnimatorSet {
        return AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofInt(
                    this,
                    ANIMATION_PROPERTY_MAX_HEIGHT,
                    startHeight,
                    endHeight
                ),
                ObjectAnimator.ofInt(
                    this@ExpandableTextView.foreground,
                    ANIMATION_PROPERTY_ALPHA,
                    foreground.alpha,
                    MAX_VALUE_ALPHA - foreground.alpha
                )
            )
        }
    }

    private fun String.isAllTextVisible(): Boolean = this == text

    private fun String.span(): SpannableString =
        SpannableString(this).apply {
            setSpan(
                ForegroundColorSpan(mEllipsizeTextColor!!),
                0,
                this.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            if (isUnderlined!!)
                setSpan(
                    UnderlineSpan(),
                    0,
                    this.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
        }

    companion object {
        const val MAX_VALUE_ALPHA = 255
        const val MIN_VALUE_ALPHA = 0
        const val ANIMATION_PROPERTY_MAX_HEIGHT = "maxHeight"
        const val ANIMATION_PROPERTY_ALPHA = "alpha"
        const val COLLAPSED_MAX_LINES = Int.MAX_VALUE
        const val DEFAULT_ANIM_DURATION = 450
        const val DEFAULT_ELLIPSIZED_TEXT = "..."
        const val EMPTY_SPACE = " "
    }
}