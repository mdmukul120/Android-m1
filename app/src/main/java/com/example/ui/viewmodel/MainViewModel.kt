package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ContentSection
import com.example.data.model.LiveChannel
import com.example.data.model.MediaItem
import com.example.data.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MukulUiState(
    val isLoading: Boolean = true,
    val selectedTab: Int = 0,
    val heroMedia: MediaItem? = null,
    val sections: List<ContentSection> = emptyList(),
    val allChannels: List<LiveChannel> = emptyList(),
    val channelCategory: String = "All",
    val movieCategory: String = "All",
    val activePlayingMedia: MediaItem? = null,
    val activePlayingChannel: LiveChannel? = null,
    val watchlistIds: Set<String> = emptySet(),
    val searchQuery: String = "",
    val isSearchOpen: Boolean = false,
    val errorMessage: String? = null
)

class MainViewModel(
    private val repository: MediaRepository = MediaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MukulUiState())
    val uiState: StateFlow<MukulUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val (hero, sections, channels) = repository.getHomeContent()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        heroMedia = hero,
                        sections = sections,
                        allChannels = channels
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load content. Please check network."
                    )
                }
            }
        }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun playMedia(media: MediaItem) {
        _uiState.update {
            it.copy(
                activePlayingMedia = media,
                activePlayingChannel = null
            )
        }
    }

    fun playChannel(channel: LiveChannel) {
        _uiState.update {
            it.copy(
                activePlayingChannel = channel,
                activePlayingMedia = null
            )
        }
    }

    fun stopPlayback() {
        _uiState.update {
            it.copy(
                activePlayingMedia = null,
                activePlayingChannel = null
            )
        }
    }

    fun toggleWatchlist(id: String) {
        _uiState.update { state ->
            val updated = state.watchlistIds.toMutableSet()
            if (updated.contains(id)) {
                updated.remove(id)
            } else {
                updated.add(id)
            }
            state.copy(watchlistIds = updated)
        }
    }

    fun setChannelCategory(category: String) {
        _uiState.update { it.copy(channelCategory = category) }
    }

    fun setMovieCategory(category: String) {
        _uiState.update { it.copy(movieCategory = category) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setSearchOpen(open: Boolean) {
        _uiState.update { it.copy(isSearchOpen = open, searchQuery = if (!open) "" else it.searchQuery) }
    }
}
