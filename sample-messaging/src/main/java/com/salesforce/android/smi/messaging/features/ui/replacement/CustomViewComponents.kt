package com.salesforce.android.smi.messaging.features.ui.replacement

import androidx.compose.runtime.Composable
import com.salesforce.android.smi.core.ConversationClient
import com.salesforce.android.smi.messaging.features.ui.replacement.components.DateBreakHeaderReplacementEntry
import com.salesforce.android.smi.messaging.features.ui.replacement.components.DisplayableOptionsReplacementEntry
import com.salesforce.android.smi.messaging.features.ui.replacement.components.PreChatSubmissionReceiptReplacementEntry
import com.salesforce.android.smi.messaging.features.ui.replacement.components.QuickRepliesReplacementEntry
import com.salesforce.android.smi.messaging.features.ui.replacement.components.TextMessageReplacementEntry
import com.salesforce.android.smi.messaging.features.ui.replacement.components.TypingStartedReplacementEntry
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.EntryPayload
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.message.component.optionItem.OptionItem.TypedOptionItem.TitleOptionItem
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.message.format.ChoicesFormat
import com.salesforce.android.smi.network.data.domain.conversationEntry.entryPayload.message.format.StaticContentFormat
import com.salesforce.android.smi.ui.ChatFeedEntry
import com.salesforce.android.smi.ui.ViewComponents
import com.salesforce.android.smi.ui.internal.common.domain.extensions.messageContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

open class OverridableUI {
    open fun renderMode(entry: ChatFeedEntry): EntryRenderMode = when (entry) {
        is ChatFeedEntry.ConversationEntryModel -> when (entry.payload) {
            is EntryPayload.MessagePayload -> when (entry.messageContent) {
                is StaticContentFormat.TextFormat,
                is ChoicesFormat.DisplayableOptionsFormat,
                is ChoicesFormat.QuickRepliesFormat -> EntryRenderMode.Replace
                else -> EntryRenderMode.Existing
            }
            else -> EntryRenderMode.Existing
        }
        else -> EntryRenderMode.Existing
    }

    inner class CustomViewComponents(private val conversationClient: ConversationClient) : ViewComponents {
        @Composable
        override fun ChatFeedEntry(entry: ChatFeedEntry, content: @Composable () -> Unit) {
            when (renderMode(entry)) {
                EntryRenderMode.None -> Unit
                EntryRenderMode.Existing -> content()
                EntryRenderMode.Replace -> CustomEntryContainer(entry, conversationClient, fallback = content)
            }
        }
    }
}

@Composable
private fun CustomEntryContainer(
    entry: ChatFeedEntry,
    conversationClient: ConversationClient?,
    fallback: @Composable () -> Unit
) {
    when (entry) {
        is ChatFeedEntry.ConversationEntryModel -> when (entry.payload) {
            is EntryPayload.MessagePayload -> {
                when (val messageContent = entry.messageContent) {
                    is StaticContentFormat.TextFormat -> TextMessageReplacementEntry(
                        isLocal = entry.isOutboundEntry,
                        text = messageContent.text
                    )

                    is ChoicesFormat.DisplayableOptionsFormat ->
                        DisplayableOptionsReplacementEntry(
                            messageContent.text,
                            messageContent.optionItems.filterIsInstance<TitleOptionItem>()
                        ) {
                            CoroutineScope(Dispatchers.Main).launch {
                                conversationClient?.sendReply(it)
                            }
                        }

                    is ChoicesFormat.QuickRepliesFormat -> QuickRepliesReplacementEntry(
                        messageContent.text,
                        messageContent.optionItems.filterIsInstance<TitleOptionItem>()
                    ) {
                        CoroutineScope(Dispatchers.Main).launch {
                            conversationClient?.sendReply(it)
                        }
                    }

                    // For Carousels, Rich Links, Attachments, WebViews, Articles, Cards - use standard SDK rich UI
                    else -> fallback()
                }
            }

            else -> fallback()
        }

        is ChatFeedEntry.DateBreakModel -> DateBreakHeaderReplacementEntry(timestamp = entry.timestamp)
        is ChatFeedEntry.PreChatReceiptModel -> PreChatSubmissionReceiptReplacementEntry {
            // navigation
        }

        is ChatFeedEntry.ProgressIndicatorModel -> {
            val participant = entry.participants.firstOrNull()
            if (entry.isActive && participant != null) {
                TypingStartedReplacementEntry(participant = participant)
            } else {
                fallback()
            }
        }

        else -> fallback()
    }
}
