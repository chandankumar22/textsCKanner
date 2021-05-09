package com.ck.dev.textsckanner.utils

sealed class TASK_STATE<String>(
    val msg: kotlin.String = ""
) {
    class PROGRESS(message:String):TASK_STATE<String>(message)
    class SUCCESS(message:String):TASK_STATE<String>(message)
    class FAILED(message:String):TASK_STATE<String>(message)

}