package com.jarvis.assistant.data

data class ChatMessage(
    val role: String,        // "user" | "assistant" | "system"
    val content: String,
    val isAgentLog: Boolean = false
)

enum class StepStatus { PENDING, RUNNING, DONE, FAILED }

data class AgentStep(
    val title: String,
    val tool: String,
    val detail: String = "",
    val status: StepStatus = StepStatus.PENDING
)

data class AgentPlan(
    val goal: String,
    val steps: MutableList<AgentStep>
)
