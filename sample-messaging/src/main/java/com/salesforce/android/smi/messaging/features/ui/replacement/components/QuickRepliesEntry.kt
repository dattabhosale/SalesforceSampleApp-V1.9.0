package com.salesforce.android.smi.messaging.features.ui.replacement.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.Alignment
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun QuickRepliesReplacementEntry(
    title: String,
    optionItems: List<OptionItem.TypedOptionItem.TitleOptionItem>,
    onSelection: ((OptionItem) -> Unit)
) {
    val shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
    val decodedTitle = remember(title) { EmojiTextDecoder.decode(title) }

    Column {
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
                color = WhatsAppTextColor,
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top)
            ) {
                repeat(optionItems.size) { index ->
                    val optionItem = optionItems[index]
                    val itemTitle = remember(optionItem.titleItem.title) { EmojiTextDecoder.decode(optionItem.titleItem.title) }
                    OutlinedButton(
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = WhatsAppTealColor),
                        modifier = Modifier.wrapContentSize(),
                        onClick = {
                            onSelection(OptionItem.SelectionsOptionItem(optionItem.optionId))
                        }
                    ) {
                        Text(text = itemTitle, color = WhatsAppTealColor)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun QuickRepliesReplacementEntryPreview() {
    QuickRepliesReplacementEntry(
        title = "Options",
        optionItems = listOf(
            OptionItem.TypedOptionItem.TitleOptionItem("1", "1", TitleItem.DefaultTitleItem("Option 1")),
            OptionItem.TypedOptionItem.TitleOptionItem("2", "1", TitleItem.DefaultTitleItem("Option 2"))
        )
    ) {
    }
}
