package com.mrboomdev.awery.ui.widgets;

import android.animation.*
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.GradientDrawable.*
import android.graphics.drawable.GradientDrawable.Orientation.*
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatTextView
import ani.awery.R
import java.lang.Integer.min
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Expand the text within layout
 */
const val EXPAND_TYPE_LAYOUT = 0

/**
 * Expand the text as a popup
 */
const val EXPAND_TYPE_POPUP = 1

/**
 * Default expand type which will layout
 */
const val EXPAND_TYPE_DEFAULT = EXPAND_TYPE_LAYOUT

/**
 * This value define how much space should be consumed by ellipsize text to represent itself.
 */
const val ELLIPSIZE_TEXT_LENGTH_MULTIPLIER = 2.0

class ExpandableTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = R.attr.expandableTextView) : AppCompatTextView(context, attrs, defStyleAttr
) {
    private var mOriginalText: CharSequence? = ""
    private var mCollapsedLines = 0
    private var mReadMoreText: CharSequence = "Read more"
    private var mReadLessText: CharSequence = "Read less"
    var isExpanded: Boolean = false
        private set
    private var mAnimationDuration: Int? = 0
    private var foregroundColor: Int? = 0
    private var initialText = ""
    private var isUnderlined: Boolean? = false
    private var mEllipsizeTextColor: Int? = 0
    var expandType = EXPAND_TYPE_DEFAULT
        private set

    private lateinit var collapsedVisibleText: String

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if(event?.action == MotionEvent.ACTION_UP) {
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
            isExpanded = if (collapsedVisibleText.isAllTextVisible()) {
                true
            } else when (expandType) {
                EXPAND_TYPE_POPUP -> false
                else -> isExpanded
            }
            setEllipsizedText(isExpanded)
            setForeground(isExpanded)
        }
    }

    private fun toggleExpandableTextView() {
        //No expand/collapse needed if collapse text is identical to complete text
        if(collapsedVisibleText.isAllTextVisible()) {
            return
        }

        when (expandType) {
            EXPAND_TYPE_LAYOUT -> {

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
                            if (!isExpanded)
                                setEllipsizedText(isExpanded)
                        }

                        override fun onAnimationRepeat(animation: Animator) {}
                        override fun onAnimationCancel(animation: Animator) {}
                        override fun onAnimationStart(animation: Animator) {}
                    })
                }

                setEllipsizedText(isExpanded)
            }

            EXPAND_TYPE_POPUP -> {
                AlertDialog.Builder(context)
                    .setTitle("")
                    .setMessage(initialText)
                    .setNegativeButton(android.R.string.ok, null)
                    .show()
            }

            else -> throw UnsupportedOperationException("No toggle operation provided for expand type[$expandType]")
        }


    }

    private fun configureMaxLines() {
        if (mCollapsedLines < COLLAPSED_MAX_LINES) {
            maxLines = when (expandType) {
                EXPAND_TYPE_LAYOUT -> if (isExpanded) COLLAPSED_MAX_LINES else mCollapsedLines
                EXPAND_TYPE_POPUP -> mCollapsedLines
                else -> maxLines
            }
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

    fun setExpandType(expandType: Int): ExpandableTextView {
        this.expandType = expandType
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
                mReadMoreText = getString(R.styleable.ExpandableTextView_readMoreText) ?: READ_MORE
                mReadLessText = getString(R.styleable.ExpandableTextView_readLessText) ?: READ_LESS
                foregroundColor = getColor(R.styleable.ExpandableTextView_foregroundColor, Color.TRANSPARENT)
                isUnderlined = getBoolean(R.styleable.ExpandableTextView_isUnderlined, false)
                isExpanded = getBoolean(R.styleable.ExpandableTextView_textIsExpanded, false)
                mEllipsizeTextColor = getColor(R.styleable.ExpandableTextView_ellipsizeTextColor, Color.BLUE)
                expandType = getInt(R.styleable.ExpandableTextView_expandType, EXPAND_TYPE_DEFAULT)
            } finally {
                this.recycle()
            }
        }

        configureMaxLines()
    }

    private fun setEllipsizedText(isExpanded: Boolean) {
        if(initialText.isBlank())
            return

        text = if(collapsedVisibleText.isAllTextVisible()) {
            initialText
        } else {
            when (expandType) {
                EXPAND_TYPE_POPUP -> getCollapseText()
                EXPAND_TYPE_LAYOUT -> if (isExpanded) getExpandText() else getCollapseText()
                else -> throw UnsupportedOperationException("No supported expand mechanism provided for expand type[$expandType]")
            }
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
        } catch (e: Exception) {
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
        const val READ_MORE = "Read more"
        const val READ_LESS = "Read less"
    }
}