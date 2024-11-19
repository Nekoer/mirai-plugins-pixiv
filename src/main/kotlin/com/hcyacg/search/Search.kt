package com.hcyacg.search

import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.Message
import org.jsoup.HttpStatusException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException

interface Search {

    suspend fun load(event: GroupMessageEvent): List<Message>

    fun isNetworkException(e: Exception): Boolean {
        return e is HttpStatusException || e is SocketTimeoutException || e is ConnectException || e is SocketException || e is IOException
    }
}