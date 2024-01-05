package com.bignerdranch.photogallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bignerdranch.photogallery.api.GalleryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PhotoGalleryViewModel : ViewModel() {
    private val photoRepository = PhotoRepository()
    private val preferenceRepository = PreferenceRepository.get()

//    private val _galleryItems: MutableStateFlow<List<GalleryItem>> = MutableStateFlow(emptyList())
//    val galleryItems: StateFlow<List<GalleryItem>>

        private val _uiState: MutableStateFlow<PhotoGalleryUiState> = MutableStateFlow(
            PhotoGalleryUiState()
        )
        val uiState: StateFlow<PhotoGalleryUiState>
            get() = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferenceRepository.storedQuery.collectLatest {
                storedQuery->
                try {
                    val items = fetchGalleryItems(storedQuery)
//                    _galleryItems.value = items
                    _uiState.update { oldState ->
                        oldState.copy(
                            images = items,
                            query = storedQuery
                        )
                    }
                } catch (ex: Exception) {
                    //do something
                }
            }

        }
    }

    fun setQuery(query: String) {
//        viewModelScope.launch { _galleryItems.value = fetchGalleryItems(query) }
        viewModelScope.launch { preferenceRepository.setStoredQuery(query) }
    }

    private suspend fun fetchGalleryItems(query: String): List<GalleryItem> {
        return if (query.isNotEmpty()) {
            photoRepository.searchPhotos(query)
        } else {
            photoRepository.fetchPhotos()
        }
    }
}

data class PhotoGalleryUiState(
    val images: List<GalleryItem> = listOf(),
    val query: String = "",
)