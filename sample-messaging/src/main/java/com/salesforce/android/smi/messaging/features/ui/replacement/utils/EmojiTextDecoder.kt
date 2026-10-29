package com.salesforce.android.smi.messaging.features.ui.replacement.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.util.Linkify
import android.widget.TextView
import androidx.core.text.HtmlCompat
import coil3.asDrawable
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import java.util.regex.Matcher
import java.util.regex.Pattern

object EmojiTextDecoder {

    private val UNICODE_ESCAPE_PATTERN: Pattern = Pattern.compile("\\\\u([0-9a-fA-F]{4})")
    private val U_PLUS_PATTERN: Pattern = Pattern.compile("(?i)U\\+([0-9a-fA-F]{4,6})")
    private val HEX_PREFIX_PATTERN: Pattern = Pattern.compile("(?i)0x([0-9a-fA-F]{4,6})")

    fun decode(rawText: String?): String {
        return decodeToSpanned(rawText).toString()
    }

    /**
     * Decodes emojis, unicode escapes, and HTML entities while preserving
     * 100% of the message text formatting, line breaks, HTML tags, and links.
     */
    fun decodeToSpanned(rawText: String?): CharSequence {
        if (rawText.isNullOrEmpty()) return ""

        var text = decodeRaw(rawText)

        // Convert Markdown links [Title](https://...) -> <a href="https://...">Title</a>
        text = text.replace(Regex("\\[([^\\]]+)\\]\\((https?://[^\\)]+)\\)")) {
            "<a href=\"${it.groupValues[2]}\">${it.groupValues[1]}</a>"
        }

        // Convert Markdown images ![Alt](https://...) -> <img src="https://..."/>
        text = text.replace(Regex("!\\[([^\\]]*)\\]\\((https?://[^\\)]+)\\)")) {
            "<img src=\"${it.groupValues[2]}\"/>"
        }

        var spanned: CharSequence = HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_LEGACY)

        if (spanned.toString().contains("&#")) {
            spanned = HtmlCompat.fromHtml(spanned.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY)
        }

        return spanned
    }

    /**
     * Formats complete message text for a TextView while rendering inline images
     * and making web links 100% clickable.
     */
    fun formatMessageText(rawText: String?, textView: TextView, context: Context): CharSequence {
        if (rawText.isNullOrEmpty()) return ""

        var text = decodeRaw(rawText)

        // Convert Markdown links [Title](https://...) -> <a href="https://...">Title</a>
        text = text.replace(Regex("\\[([^\\]]+)\\]\\((https?://[^\\)]+)\\)")) {
            "<a href=\"${it.groupValues[2]}\">${it.groupValues[1]}</a>"
        }

        // Convert Markdown images ![Alt](https://...) -> <img src="https://..."/>
        text = text.replace(Regex("!\\[([^\\]]*)\\]\\((https?://[^\\)]+)\\)")) {
            "<img src=\"${it.groupValues[2]}\"/>"
        }

        // Convert standalone raw image URLs & Salesforce asset URLs -> <br/><img src="url"/><br/>
        val imageUrlRegex = Regex(
            "(?i)(?<!src=[\"'])(https?://[^\\s<\"']+(?:\\.(?:jpg|jpeg|png|gif|webp|svg)(?:\\?[^\\s<\"']*)?|/sfc/|/file-asset/|/cms/delivery/|/servlet\\.shepherd/|/ContentVersion/)[^\\s<\"']*)"
        )
        text = text.replace(imageUrlRegex) {
            "<br/><img src=\"${it.value}\"/><br/>"
        }

        // Parse with HtmlCompat using UrlImageGetter for inline images
        val spanned = HtmlCompat.fromHtml(
            text,
            HtmlCompat.FROM_HTML_MODE_LEGACY,
            UrlImageGetter(textView, context),
            null
        )

        // Apply Linkify to ensure all web URLs are clickable
        val spannable = if (spanned is Spannable) spanned else SpannableString(spanned)
        Linkify.addLinks(spannable, Linkify.WEB_URLS)

        return spannable
    }

    private fun decodeRaw(rawText: String): String {
        var text = rawText

        // Fix double HTML encoding (&amp;#128512; -> &#128512;)
        text = text.replace("&amp;#", "&#")

        // Unescape literal \uXXXX sequences (\ud83d\ude00)
        if (text.contains("\\u", ignoreCase = true)) {
            text = unescapeUnicode(text)
        }

        // Replace U+XXXX and 0xXXXX
        text = replacePattern(text, U_PLUS_PATTERN) { hex ->
            codePointToEmoji(hex.toInt(16))
        }
        text = replacePattern(text, HEX_PREFIX_PATTERN) { hex ->
            codePointToEmoji(hex.toInt(16))
        }

        return text
    }

    private fun codePointToEmoji(codePoint: Int): String {
        return try {
            if (Character.isValidCodePoint(codePoint)) {
                String(Character.toChars(codePoint))
            } else {
                ""
            }
        } catch (_: Exception) {
            ""
        }
    }

    private fun unescapeUnicode(input: String): String {
        val matcher = UNICODE_ESCAPE_PATTERN.matcher(input)
        val sb = StringBuffer()
        while (matcher.find()) {
            try {
                val hex = matcher.group(1)
                val code = hex!!.toInt(16).toChar()
                matcher.appendReplacement(sb, Matcher.quoteReplacement(code.toString()))
            } catch (_: Exception) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group()))
            }
        }
        matcher.appendTail(sb)
        return sb.toString()
    }

    private fun replacePattern(input: String, pattern: Pattern, transform: (String) -> String): String {
        val matcher = pattern.matcher(input)
        val sb = StringBuffer()
        while (matcher.find()) {
            try {
                val matchGroup = matcher.group(1)!!
                val replacement = transform(matchGroup)
                if (replacement.isNotEmpty()) {
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement))
                } else {
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group()))
                }
            } catch (_: Exception) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group()))
            }
        }
        matcher.appendTail(sb)
        return sb.toString()
    }

    private class UrlImageGetter(
        private val textView: TextView,
        private val context: Context
    ) : Html.ImageGetter {

        override fun getDrawable(source: String?): Drawable {
            if (source.isNullOrEmpty()) {
                return ColorDrawable(android.graphics.Color.TRANSPARENT)
            }

            val container = DrawableContainer()

            val request = ImageRequest.Builder(context)
                .data(source)
                .allowHardware(false)
                .target(
                    onSuccess = { image ->
                        val drawable = image.asDrawable(context.resources)
                        val displayWidth = (context.resources.displayMetrics.widthPixels * 0.70).toInt()
                        val intrinsicWidth = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else displayWidth
                        val intrinsicHeight = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else (displayWidth / 2)
                        val height = (displayWidth * intrinsicHeight) / intrinsicWidth

                        drawable.setBounds(0, 0, displayWidth, height)
                        container.drawable = drawable
                        container.setBounds(0, 0, displayWidth, height)

                        textView.post {
                            textView.text = textView.text
                            textView.invalidate()
                        }
                    }
                )
                .build()

            context.imageLoader.enqueue(request)

            return container
        }
    }

    private class DrawableContainer : Drawable() {
        var drawable: Drawable? = null

        override fun draw(canvas: Canvas) {
            drawable?.draw(canvas)
        }

        override fun setAlpha(alpha: Int) {
            drawable?.alpha = alpha
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            drawable?.colorFilter = colorFilter
        }

        @Deprecated("Deprecated in Java")
        override fun getOpacity(): Int {
            return drawable?.opacity ?: PixelFormat.TRANSLUCENT
        }
    }
}
