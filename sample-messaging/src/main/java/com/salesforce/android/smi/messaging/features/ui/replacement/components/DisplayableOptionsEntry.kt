package com.salesforce.android.smi.messaging.features.ui.replacement.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.salesforce.android.smi.messaging.features.ui.replacement.utils.EmojiTextDecoder
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.message.component.optionItem.OptionItem
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.message.component.optionItem.titleItem.TitleItem

private val WhatsAppInboundBg = Color(0xFFFFFFFF)
private val WhatsAppTextColor = Color(0xFF111B21)
private val WhatsAppTealColor = Color(0xFF00A884)

@Composable
internal fun DisplayableOptionsReplacementEntry(
    title: String,
    optionItems: List<OptionItem.TypedOptionItem.TitleOptionItem>,
    onSelection: ((OptionItem) -> Unit)
) {
    val shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
    val decodedTitle = remember(title) { EmojiTextDecoder.decode(title) }

    Card(
        colors = CardDefaults.cardColors(containerColor = WhatsAppInboundBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = shape,
        modifier = Modifier
            .wrapContentSize()
            .padding(vertical = 4.dp, horizontal = 4.dp)
    ) {
        Text(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            text = decodedTitle,
            color = WhatsAppTextColor
        )
        optionItems.forEach {
            val itemTitle = remember(it.titleItem.title) { EmojiTextDecoder.decode(it.titleItem.title) }
            OutlinedButton(
                colors = ButtonDefaults.outlinedButtonColors(contentColor = WhatsAppTealColor),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                onClick = {
                    onSelection(OptionItem.SelectionsOptionItem(it.optionId))
                }
            ) {
                Text(text = itemTitle, color = WhatsAppTealColor)
            }
        }
    }
}

@Preview
@Composable
private fun DisplayableOptionsReplacementEntryPreview() {
    DisplayableOptionsReplacementEntry(
        title = "Options",
        optionItems = listOf(
            OptionItem.TypedOptionItem.TitleOptionItem("1", "1", TitleItem.DefaultTitleItem("Option 1")),
            OptionItem.TypedOptionItem.TitleOptionItem("2", "1", TitleItem.DefaultTitleItem("Option 2"))
        )
    ) {
    }
}
