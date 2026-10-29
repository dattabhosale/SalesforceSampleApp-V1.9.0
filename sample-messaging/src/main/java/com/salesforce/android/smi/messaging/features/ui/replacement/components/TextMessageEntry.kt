package com.salesforce.android.smi.messaging.features.ui.replacement.components

import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.salesforce.android.smi.messaging.features.ui.replacement.utils.EmojiTextDecoder

private val WhatsAppOutboundBg = Color(0xFFE2F7CB)
private val WhatsAppInboundBg = Color(0xFFFFFFFF)
private val WhatsAppTextColor = Color(0xFF111B21)
private val WhatsAppLinkColor = Color(0xFF027EB5)

@Composable
internal fun TextMessageReplacementEntry(
    isLocal: Boolean,
    text: String?
) {
    val context = LocalContext.current
    val bubbleColor = if (isLocal) WhatsAppOutboundBg else WhatsAppInboundBg
    val bubbleShape = if (isLocal) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
    }

    MessageContainer(isLocal = isLocal) {
        Card(
            colors = CardDefaults.cardColors(containerColor = bubbleColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = bubbleShape,
            modifier = Modifier
                .wrapContentSize()
                .padding(vertical = 4.dp, horizontal = 4.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .wrapContentSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                AndroidView(
                    factory = { ctx ->
                        TextView(ctx).apply {
                            setTextColor(WhatsAppTextColor.toArgb())
                            setLinkTextColor(WhatsAppLinkColor.toArgb())
                            textSize = 15f
                            movementMethod = LinkMovementMethod.getInstance()
                            linksClickable = true
                            isFocusable = true
                            isClickable = true
                        }
                    },
                    update = { textView ->
                        textView.text = EmojiTextDecoder.formatMessageText(text, textView, context)
                    }
                )
            }
        }
    }
}

@Preview
@Composable
private fun TextMessageReplacementEntryPreview_Outbound() {
    TextMessageReplacementEntry(isLocal = true, text = "Leapmotor C11\nhttps://example.com/c11.jpg\nDetails: https://example.com")
}

@Preview
@Composable
private fun TextMessageReplacementEntryPreview_Inbound() {
    TextMessageReplacementEntry(isLocal = false, text = "Leapmotor T03\nhttps://example.com/t03.jpg\nDetails: https://example.com")
}
