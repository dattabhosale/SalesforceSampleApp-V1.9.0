package com.salesforce.android.smi.messaging.features.ui

import com.salesforce.android.smi.network.data.domain.prechat.PreChatField
import java.util.logging.Level
import java.util.logging.Logger

open class PopulatePreChat {
    protected val logger: Logger = Logger.getLogger(this::class.java.name)
    open val defaultValue = "%S-Placeholder"

    open fun populate(preChatFields: List<PreChatField>): List<PreChatField> = preChatFields.onEach { field ->
        val nameClean = field.name.lowercase().replace("_", "").replace(" ", "")
        field.userInput = when {
            nameClean.contains("countrycode") -> "es"
            nameClean == "country" || nameClean.contains("countryname") -> "Spain"
            nameClean.contains("language") -> "es"
            nameClean.contains("customervehicle") || nameClean.contains("vehicle") -> "B10"
            nameClean.contains("firstname") || nameClean.contains("first") -> "John"
            nameClean.contains("lastname") || nameClean.contains("last") -> "Wick"
            else -> defaultValue.format(field.name)
        }
        logResult(field)
    }

    fun logResult(preChatField: PreChatField): PreChatField = preChatField.also {
        logger.log(Level.INFO, "Populate ${preChatField.name} = ${preChatField.userInput}")
    }
}
