package com.example.grpcsample.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grpcsample.BuildConfig
import com.example.grpcsample.data.GreeterRepository
import io.grpc.StatusException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class GreeterUiState(
    val name: String = "Android",
    val isLoading: Boolean = false,
    val messages: List<String> = emptyList(),
    val error: String? = null,
)

class GreeterViewModel : ViewModel() {

    private val repository = GreeterRepository(BuildConfig.GRPC_HOST, BuildConfig.GRPC_PORT)
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    private val _state = MutableStateFlow(GreeterUiState())
    val state: StateFlow<GreeterUiState> = _state.asStateFlow()

    private var streamJob: Job? = null

    fun onNameChange(name: String) = _state.update { it.copy(name = name) }

    fun sayHello() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val reply = repository.sayHello(_state.value.name)
                addMessage("[unary] ${reply.message}", reply.timestampMillis)
            } catch (e: StatusException) {
                _state.update { it.copy(error = "${e.status.code}: ${e.status.description ?: e.message}") }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun streamGreetings(count: Int = 5) {
        streamJob?.cancel()
        streamJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.streamGreetings(_state.value.name, count)
                .catch { e ->
                    val msg = if (e is StatusException) "${e.status.code}: ${e.status.description}" else e.toString()
                    _state.update { it.copy(error = msg) }
                }
                .onCompletion { _state.update { it.copy(isLoading = false) } }
                .collect { reply -> addMessage("[stream] ${reply.message}", reply.timestampMillis) }
        }
    }

    fun clear() = _state.update { it.copy(messages = emptyList(), error = null) }

    private fun addMessage(text: String, timestamp: Long) {
        val line = "${timeFormat.format(Date(timestamp))}  $text"
        _state.update { it.copy(messages = it.messages + line) }
    }

    override fun onCleared() {
        repository.close()
    }
}
