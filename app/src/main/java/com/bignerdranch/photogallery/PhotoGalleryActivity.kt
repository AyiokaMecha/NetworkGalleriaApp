package com.bignerdranch.photogallery

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.bignerdranch.photogallery.api.FlickrApi
import com.bignerdranch.photogallery.databinding.FragmentPhotoGalleryBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.create
import java.lang.Exception
import java.util.concurrent.TimeUnit

private const val POLL_WORK = "POLL_WORK"
class PhotoGalleryFragment : Fragment() {
    private var _binding : FragmentPhotoGalleryBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access"
        }

    private var searchView: SearchView? = null

    private val photoGalleryViewModel: PhotoGalleryViewModel by viewModels()

    private var pollingMenuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)  //when I have to include and options menu in my application appBar
//
//        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.UNMETERED).build()
//        val workRequest = OneTimeWorkRequest.Builder(PollWorker::class.java).setConstraints(constraints).build()
//        WorkManager.getInstance(requireContext()).enqueue(workRequest)


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPhotoGalleryBinding.inflate(inflater, container, false)
        binding.photoGrid.layoutManager = GridLayoutManager(context, 3)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_photo_gallery, menu)

        val searchItem: MenuItem = menu.findItem(R.id.menu_item_search)
        searchView = searchItem.actionView as? SearchView
        pollingMenuItem = menu.findItem(R.id.menu_item_toggle_polling)
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                photoGalleryViewModel.setQuery(query?: "")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_clear -> {
                photoGalleryViewModel.setQuery("")
                true
            }
            R.id.menu_item_toggle_polling -> {
                photoGalleryViewModel.toggleIsPolling()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//            photoGalleryViewModel.galleryItems.collect {
//                items ->
                photoGalleryViewModel.uiState.collect { state ->
                    searchView?.setQuery(state.query, false)
                    binding.photoGrid.adapter = PhotoListAdapter(state.images) { photoPageUri ->
//                        val intent = Intent(Intent.ACTION_VIEW, photoPageUri)
//                        startActivity(intent)
                        findNavController().navigate(
                            PhotoGalleryFragmentDirections.showPhoto(
                                photoPageUri
                            )
                        )
                    }
                    updatePollingState(state.isPolling)
                }
            }

        }
    }

    override fun onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu()
        searchView = null
        pollingMenuItem = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updatePollingState(isPolling: Boolean) {
        val toggleItemTitle = if (isPolling) {
            R.string.stop_polling
        } else {
            R.string.start_polling
        }
        pollingMenuItem?.setTitle(toggleItemTitle)

        if (isPolling) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .build()
            val periodicWorkRequest = PeriodicWorkRequestBuilder<PollWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(POLL_WORK, ExistingPeriodicWorkPolicy.KEEP, periodicWorkRequest)
        } else {
            WorkManager.getInstance(requireContext()).cancelUniqueWork(POLL_WORK)
        }
    }
}