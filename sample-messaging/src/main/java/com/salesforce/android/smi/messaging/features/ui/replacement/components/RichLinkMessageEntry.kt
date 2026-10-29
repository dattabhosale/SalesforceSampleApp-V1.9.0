package com.salesforce.android.smi.messaging.features.ui.replacement.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.imageLoader
import com.salesforce.android.smi.messaging.features.ui.replacement.utils.EmojiTextDecoder
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.message.component.attachment.FileAsset
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.message.component.optionItem.titleItem.TitleItem
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.message.component.richLink.LinkItem

private val WhatsAppInboundBg = Color(0xFFFFFFFF)
private val WhatsAppTextColor = Color(0xFF111B21)
private val WhatsAppLinkColor = Color(0xFF027EB5)
private val WhatsAppSubtextColor = Color(0xFF667781)

@Composable
internal fun RichLinkMessageReplacementEntry(
    isLocal: Boolean,
    linkItem: LinkItem,
    image: FileAsset.ImageAsset.RichLinkImage?
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)

    val title = EmojiTextDecoder.decode(linkItem.titleItem.title)
    val subtitle = linkItem.titleItem.subTitle?.let { EmojiTextDecoder.decode(it) }

    MessageContainer(isLocal = isLocal) {
        Card(
            colors = CardDefaults.cardColors(containerColor = WhatsAppInboundBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = shape,
            modifier = Modifier
                .wrapContentWidth()
                .padding(vertical = 4.dp, horizontal = 4.dp)
                .clickable {
                    try {
                        val rawUrl = linkItem.url
                        if (!rawUrl.isNullOrEmpty()) {
                            val targetUrl = if (!rawUrl.startsWith("http://") && !rawUrl.startsWith("https://")) {
                                "https://$rawUrl"
                            } else {
                                rawUrl
                            }
                            uriHandler.openUri(targetUrl)
                        }
                    } catch (_: Exception) {}
                }
        ) {
            Column(
                modifier = Modifier.wrapContentWidth()
            ) {
                Column(
                    Modifier
                        .padding(14.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text(text = title, color = WhatsAppTextColor)
                    Text(
                        text = linkItem.url,
                        color = WhatsAppLinkColor,
                        textDecoration = TextDecoration.Underline
                    )
                    subtitle?.let { Text(text = it, color = WhatsAppSubtextColor) }
                }
                image?.let { img ->
                    Box(contentAlignment = Alignment.Center) {
                        val imageModel: Any? = img.file?.path ?: img.file
                        AsyncImage(
                            imageLoader = context.imageLoader,
                            modifier = Modifier
                                .height(150.dp)
                                .width(250.dp),
                            model = imageModel,
                            contentDescription = "Rich Link Image"
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun RichLinkMessageReplacementEntryPreview_Outbound() {
    RichLinkMessageReplacementEntry(
        isLocal = true,
        linkItem = LinkItem(
            "www.slack.com",
            TitleItem.TitleLinkItem("Title", "Subtitle")
        ),
        image = FileAsset.ImageAsset.RichLinkImage(
            "id",
            "id",
            "https://static.scientificamerican.com/sciam/cache/file/C37174A9-488C-49D4-B9C79B4E8EB39287_source.jpg?w=1200",
            mimeType = "image/png"
        )
    )
}
